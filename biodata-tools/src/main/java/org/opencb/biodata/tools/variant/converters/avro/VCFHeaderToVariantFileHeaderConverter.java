package org.opencb.biodata.tools.variant.converters.avro;

import htsjdk.variant.vcf.*;
import org.opencb.biodata.models.variant.metadata.VariantFileHeader;
import org.opencb.biodata.models.variant.metadata.VariantFileHeaderLine;
import org.opencb.biodata.tools.Converter;

import java.util.*;
import java.util.function.Function;

/**
 * Created on 09/08/17.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class VCFHeaderToVariantFileHeaderConverter  implements Converter<VCFHeader, VariantFileHeader> {

    @Override
    public VariantFileHeader convert(VCFHeader header) {

        ArrayList<VariantFileHeaderLine> lines = new ArrayList<>();
        HashMap<String, String> attributes = new HashMap<>();

        VariantFileHeader avroHeader = new VariantFileHeader();
        for (VCFHeaderLine line : header.getMetaDataInInputOrder()) {
            if (line.getKey().equalsIgnoreCase("fileFormat")) {
                avroHeader.setVersion(line.getValue());
                continue;
            }

            if (line.getValue().isEmpty()) {
                if ( line instanceof VCFCompoundHeaderLine)  {
                    VCFCompoundHeaderLine vcfLine = (VCFCompoundHeaderLine) line;
                    String number;
                    if (vcfLine.isFixedCount()) {
                        number = String.valueOf(vcfLine.getCount());
                    } else if (vcfLine.getCountType().equals(VCFHeaderLineCount.UNBOUNDED)) {
                        number = ".";
                    } else {
                        number = vcfLine.getCountType().toString();
                    }
                    lines.add(VariantFileHeaderLine.newBuilder()
                            .setKey(vcfLine.getKey())
                            .setId(vcfLine.getID())
                            .setDescription(vcfLine.getDescription())
                            .setType(vcfLine.getType().toString())
                            .setNumber(number).build());

                } else if ( line instanceof VCFSimpleHeaderLine ) {
                    Map<String, String> map = VCFHeaderLineTranslator.parseLine(VCFHeaderVersion.VCF4_2, line.toString(), null);
                    VariantFileHeaderLine.Builder builder = VariantFileHeaderLine.newBuilder();
                    setValue(map, "ID", builder::setId);
                    setValue(map, "Description", builder::setDescription);
                    setValue(map, "Number", builder::setNumber);
                    setValue(map, "Type", builder::setType);
                    builder.setKey(line.getKey());
                    builder.setAttributes(map);
                    lines.add(builder.build());
                }
            } else {
                attributes.put(line.getKey(), line.getValue());
            }

        }
        avroHeader.setLines(lines);
        avroHeader.setAttributes(attributes);

        return avroHeader;
    }

    private void setValue(Map<String, String> map, String key, Function<String, VariantFileHeaderLine.Builder> method) {
        if (map.containsKey(key)) {
            method.apply(map.remove(key));
        }
    }

    private void putNotNull(Map<String, String> map, String key, Object value) {
        if (value != null) {
            map.put(key, value.toString());
        }
    }
}
