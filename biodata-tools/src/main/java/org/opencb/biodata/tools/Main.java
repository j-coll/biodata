package org.opencb.biodata.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantFileMetadata;
import org.opencb.biodata.models.variant.metadata.VariantStudyMetadata;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.biodata.tools.variant.VariantVcfHtsjdkReader;
import org.opencb.biodata.tools.variant.converters.VCFExporter;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.run.ParallelTaskRunner;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tukaani.xz.UnsupportedOptionsException;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

/**
 * Created on 13/09/17.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class Main {

    @Parameters(commandNames = {"normalize"}, commandDescription = "Normalize variants")
    public static class Options {
        @Parameter(names = {"-i", "--input"}, description = "Input vcf file", required = true)
        Path input;

        @Parameter(names = {"-o", "--output"}, description = "Output file or folder")
        Path output;

        @Parameter(names = {"-h", "--help"}, description = "Prints help", help = true)
        boolean help;

        @Parameter(names = {"--decompose"}, description = "Decompose MNVs")
        boolean decomposeMNV;

        @Parameter(names = {"--generate-ref-blocks"}, description = "Generate reference blocks when normalizing INDELs")
        boolean generateRefBlocks;
    }

    public static void main(String[] args) throws IOException, ExecutionException {
        JCommander jCommander = new JCommander();
        Options normalizeOptions = new Options();
        jCommander.addCommand(normalizeOptions);

        try {
            jCommander.parse(args);
            if (normalizeOptions.help) {
                jCommander.usage();
                System.exit(0);
            }
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jCommander.usage();
            System.exit(1);
        }

        String output;
        if (normalizeOptions.output  == null) {
            normalizeOptions.output = Paths.get("").toAbsolutePath();
        }
        if (normalizeOptions.output.toFile().isDirectory()) {
            output = normalizeOptions.output.resolve(normalizeOptions.input.getFileName().toString() + ".json.gz").toString();
        } else {
            output = normalizeOptions.output.toString();
        }

        System.err.println("Write output at : " + output);

        VariantStudyMetadata metadata = readMetadata(normalizeOptions.input);

        try (InputStream is = FileUtils.newInputStream(normalizeOptions.input);
             OutputStream os = new GZIPOutputStream(new FileOutputStream(output))) {
            VariantNormalizer normalizer = new VariantNormalizer(true, true, normalizeOptions.decomposeMNV).setGenerateReferenceBlocks(normalizeOptions.generateRefBlocks);
            VariantVcfHtsjdkReader reader = new VariantVcfHtsjdkReader(is, metadata, normalizer);
            ParallelTaskRunner.Config config = ParallelTaskRunner.Config.builder().setBatchSize(10).setSorted(true).setNumTasks(1).build();
            DataWriter<Variant> writer;
            if (output.endsWith("json.gz")) {
                writer = new VariantJsonWriter(os);
            } else if (output.endsWith("vcf.gz")) {

                VCFExporter exporter = new VCFExporter(metadata);
                writer = new DataWriter<Variant>() {
                    @Override
                    public boolean open() {
                        exporter.open(os);
                        return true;
                    }

                    @Override
                    public boolean write(List<Variant> list) {
                        exporter.export(list);
                        return true;
                    }

                    @Override
                    public boolean close() {
                        try {
                            exporter.close();
                        } catch (IOException e) {
                            throw Throwables.propagate(e);
                        }
                        return true;
                    }
                };
            } else {
                throw new UnsupportedOptionsException("Unknown output file extension");
            }
            ParallelTaskRunner<Variant, Variant> ptr = new ParallelTaskRunner<>(reader, list -> list, writer, config);
            ptr.run();
        }

    }

    public static VariantStudyMetadata readMetadata(Path input) throws IOException {
        try (InputStream is = FileUtils.newInputStream(input)) {

            VariantStudyMetadata studyMetadata = new VariantFileMetadata("f", input.toString()).toVariantStudyMetadata("s");
            VariantVcfHtsjdkReader reader = new VariantVcfHtsjdkReader(is, studyMetadata);
            reader.open();
            reader.pre();
            reader.post();
            reader.close();

            return studyMetadata;
        }

    }

    public static class VariantJsonWriter implements DataWriter<Variant> {

        private final VariantFileMetadata fileMetadata;
        // Null if OutputStreams were directly provided
        private final Path outdir;

        protected JsonFactory factory;
        protected ObjectMapper jsonObjectMapper;

        protected JsonGenerator variantsGenerator;

        private OutputStream variantsStream;

        private Logger logger = LoggerFactory.getLogger(VariantJsonWriter.class);

        private long numVariantsWritten;
        private boolean closeStreams;

        public VariantJsonWriter(VariantFileMetadata fileMetadata, @Nullable Path outdir) {
            Objects.requireNonNull(fileMetadata, "VariantFileMetadata can not be null");
            this.fileMetadata = fileMetadata;
            this.outdir = (outdir != null) ? outdir : Paths.get("").toAbsolutePath();
            this.factory = new JsonFactory();
            this.jsonObjectMapper = new ObjectMapper(this.factory);
            this.numVariantsWritten = 0;
            closeStreams = true;
        }

        public VariantJsonWriter(OutputStream variantsStream) {
            this(null, variantsStream, null);
        }

        public VariantJsonWriter(VariantFileMetadata fileMetadata, OutputStream variantsStream, OutputStream fileStream) {
            this.fileMetadata = fileMetadata;
            this.outdir = null;
            this.variantsStream = variantsStream;
            this.factory = new JsonFactory();
            this.jsonObjectMapper = new ObjectMapper(this.factory);
            this.numVariantsWritten = 0;
            closeStreams = false;
        }

        @Override
        public boolean open() {
            try {
                if (outdir != null) {
                   variantsStream = new GZIPOutputStream(new FileOutputStream(outdir.toFile()));
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return true;
        }

        @Override
        public boolean pre() {
            jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
            try {
                variantsGenerator = factory.createGenerator(variantsStream);
            } catch (IOException ex) {
                close();
                throw new UncheckedIOException(ex);
            }

            return true;
        }

        @Override
        public boolean write(Variant variant) {
            try {
                variantsGenerator.writeObject(variant);
                variantsGenerator.writeRaw('\n');
            } catch (IOException ex) {
                logger.error(variant.toString(), ex);
                close();
                throw new UncheckedIOException(ex);
            }
            return true;
        }

        @Override
        public boolean write(List<Variant> batch) {
            for (Variant variant : batch) {
                write(variant);
            }

            numVariantsWritten += batch.size();
            // TODO: Use ProgressLogger here
            if (numVariantsWritten % 1000 == 0) {
                Variant lastVariantInBatch = batch.get(batch.size() - 1);
                logger.info("{}\tvariants written upto position {}:{}",
                        numVariantsWritten, lastVariantInBatch.getChromosome(), lastVariantInBatch.getStart());
            }

            return true;
        }

        @Override
        public boolean post() {
            try {
                variantsStream.flush();
                variantsGenerator.flush();

            } catch (IOException ex) {
                close();
                throw new UncheckedIOException(ex);
            }
            return true;
        }

        @Override
        public boolean close() {
            try {
                if (closeStreams) {
                    variantsGenerator.close();
                }
            } catch (IOException ex) {
                logger.error("", ex);
                return false;
            }
            return true;
        }

    }

}
