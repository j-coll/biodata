package org.opencb.biodata.formats.alignment.sam.io;

import net.sf.samtools.BAMFileWriter;
import org.opencb.biodata.models.alignment.AlignmentHeader;

import java.nio.file.Path;

import org.opencb.biodata.formats.alignment.io.AlignmentDataReader;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/3/13
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentBamDataWriter extends AlignmentSamDataWriter  {


    public AlignmentBamDataWriter(Path output, AlignmentHeader header) {
        super(output, header);
    }

    public AlignmentBamDataWriter(Path output, AlignmentDataReader reader) {
        super(output, reader);
    }

    @Override
    public boolean open() {
        writer = new BAMFileWriter(this.output.toFile());
        return true;
    }

}
