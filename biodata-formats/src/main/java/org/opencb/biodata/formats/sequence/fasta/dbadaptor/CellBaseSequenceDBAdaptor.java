package org.opencb.biodata.formats.sequence.fasta.dbadaptor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.feature.Region;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by jacobo on 14/08/14.
 */
public class CellBaseSequenceDBAdaptor extends SequenceDBAdaptor {

    private static final String EBI_CELLBASEHOST = "http://www.ebi.ac.uk/cellbase/webservices/rest";
    private static final String EBI_VERSION = "v3";
    private static final String EBI_SPECIES = "hsapiens";

    private static final String CIPF_CELLBASEHOST = "http://ws.bioinfo.cipf.es/cellbase/rest";
    private static final String CIPF_VERSION = "v2";
    private static final String CIPF_SPECIES = "hsa";

    private static final String DEFAULT_CELLBASEHOST = EBI_CELLBASEHOST;
    private static final String DEFAULT_VERSION = EBI_VERSION;
    private static final String DEFAULT_SPECIES = EBI_SPECIES;

    private long queryTime = 0;
    private final String cellbaseHost;
    private final String version;
    private final String species;

    private ObjectMapper mapper;
    private JsonFactory factory;

    public CellBaseSequenceDBAdaptor() {
        cellbaseHost = DEFAULT_CELLBASEHOST;
        species = DEFAULT_SPECIES;
        version = DEFAULT_VERSION;
    }

    CellBaseSequenceDBAdaptor(String location) {
        switch(location.toLowerCase()) {
            case "cipf":
                cellbaseHost =  CIPF_CELLBASEHOST;
                species =       CIPF_SPECIES;
                version =       CIPF_VERSION;
                break;
            case "ebi":
                cellbaseHost =  EBI_CELLBASEHOST;
                species =       EBI_SPECIES;
                version =       EBI_VERSION;
                break;
            default:
                cellbaseHost =  DEFAULT_CELLBASEHOST;
                species =       DEFAULT_SPECIES;
                version =       DEFAULT_VERSION;
                break;
        }
    }

    public CellBaseSequenceDBAdaptor(Path credentialsPath) {
        super(credentialsPath);
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(new FileInputStream(credentialsPath.toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        cellbaseHost = properties.getProperty("CELLBASE.HOST", DEFAULT_CELLBASEHOST);
        version = properties.getProperty("CELLBASE.VERSION", DEFAULT_VERSION);
        species = properties.getProperty("CELLBASE.SPECIES", DEFAULT_SPECIES);
    }

    @Override
    public String getSequence(Region region) throws IOException {
        return getSequence(region, species);
    }
    @Override
    public String getSequence(Region region, String species) throws IOException {
        if(region.getEnd() < region.getStart()) {
            return "";
        }

        //Cellbase can't accept negative starting region. Have to be adjusted.
        Region adjustedRegion = new Region(region.getChromosome(), region.getStart(), region.getEnd());
        if(region.getStart() <= 0) {
            adjustedRegion.setStart(1);
        }

        long start = System.currentTimeMillis();

        String urlString = cellbaseHost + "/" + version + "/" + species + "/genomic/region/" + adjustedRegion.toString() + "/sequence?of=json";

        URL url = new URL(urlString);
        InputStream is = url.openConnection().getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));


        JsonParser jp = factory.createParser(br);
        JsonNode o = mapper.readTree(jp);

        String sequence = "";
        try {
            switch(version) {
                case "v2": {
                    sequence = o.get(0).get("sequence").asText();
                    break;
                }
                case "v3": {
                    sequence = o.get("response").get(0).get("result").get("sequence").asText();
                    break;
                }
                default : {
                    throw new UnsupportedOperationException("Unsupported cellBase version : " + version);
                }
            }
        } catch (NullPointerException e) {
            String error = "Error in " + this.getClass().getName() + ".getSequence " +
                    "Region: = " + region + ", " +
                    "error: " + (o.get("error") != null? o.get("error").asText() : "null") + ", " +
                    "url: " + urlString;
            System.out.println(error);
            throw new IOException(error);
        }


        br.close();


        if(sequence.length() == 0 && region.getEnd()-region.getStart() > 0) { //FIXME JJ: Recursive call to solve one undocumented feature of cellbase (AKA: bug)
            //  See:
            //      http://www.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/region/1:249250000-249250621/sequence?of=json
            //      http://www.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/region/1:249250000-249250622/sequence?of=json
            return getSequence(new Region(region.getChromosome(), region.getStart(), region.getEnd() - (region.getEnd()-region.getStart())* 9 / 10), species);
        }

        long end = System.currentTimeMillis();
        queryTime += end-start;

        //If a negative region was requested, the negative start will be filled with 'N'.
        if(region.getStart() != adjustedRegion.getStart()) {
            int n = adjustedRegion.getStart() - region.getStart();
            char[] chars = new char[n];
            Arrays.fill(chars, 'N');
            sequence = new String(chars) + sequence;
        }
        return sequence;
    }

    public long getQueryTime(){
        return queryTime;
    }

    public void resetQueryTime(){
        queryTime = 0;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void open() throws IOException {
        //TODO: Check service connection
        mapper = new ObjectMapper();
        factory = mapper.getFactory();
    }
}
