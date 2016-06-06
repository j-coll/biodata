package org.opencb.biodata.tools.variant.stats.writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.models.variant.StudyEntry;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.stats.VariantStats;
import org.opencb.biodata.tools.variant.converter.VariantStatsToPopulationFrequencyConverter;
import org.opencb.commons.io.DataWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Exports the given variant stats into a Json format.
 *
 * The writer will transform the VariantStats into PopulationFrequency objects using
 * the {@link VariantStatsToPopulationFrequencyConverter}
 * The output variants won't contain any StudyEntries or other extra annotations
 *
 * Created on 01/06/16.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class VariantStatsPopulationFrequencyExporter implements DataWriter<Variant> {


    private ObjectWriter objectWriter;
    private OutputStream outputStream;
    private final VariantStatsToPopulationFrequencyConverter converter;

    public VariantStatsPopulationFrequencyExporter(OutputStream outputStream) {
        this.outputStream = outputStream;
        converter = new VariantStatsToPopulationFrequencyConverter();

    }

    @Override
    public boolean pre() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        objectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        objectWriter = objectMapper.writerFor(Variant.class);

        return true;
    }


    @Override
    public boolean write(List<Variant> batch) {
        for (Variant variant : batch) {
            write(variant);
        }
        return true;
    }

    @Override
    public boolean write(Variant variant) {
        ArrayList<PopulationFrequency> frequencies = new ArrayList<>();
        for (StudyEntry studyEntry : variant.getStudies()) {
            for (Map.Entry<String, VariantStats> cohortEntry : studyEntry.getStats().entrySet()) {
                String studyId = studyEntry.getStudyId();
                studyId = studyId.substring(studyId.lastIndexOf(":") + 1);
                frequencies.add(converter.convert(studyId,
                        cohortEntry.getKey(),
                        cohortEntry.getValue(), variant.getReference(), variant.getAlternate()));
            }
        }
        Variant cellbaseVar = new Variant(variant.toString());
        VariantAnnotation annotation = new VariantAnnotation();
        annotation.setPopulationFrequencies(frequencies);
        cellbaseVar.setAnnotation(annotation);
        try {
            objectWriter.writeValue(outputStream, cellbaseVar);
            outputStream.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return true;
    }
}
