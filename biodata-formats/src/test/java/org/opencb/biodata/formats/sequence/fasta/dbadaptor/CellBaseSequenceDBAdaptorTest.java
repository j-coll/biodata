package org.opencb.biodata.formats.sequence.fasta.dbadaptor;

import org.junit.Test;
import org.opencb.biodata.models.feature.Region;

import java.io.IOException;

import static org.junit.Assert.*;

public class CellBaseSequenceDBAdaptorTest {
    @Test
    public void test() throws IOException {
        CellBaseSequenceDBAdaptor ebiAdaptor = new CellBaseSequenceDBAdaptor("ebi");
        CellBaseSequenceDBAdaptor cipfAdaptor = new CellBaseSequenceDBAdaptor("cipf");

        Region region = new Region("17", -1, 50);

        //Expected:
        //("17", -1, 50) -> NNAAGCTTCTCACCCTGTTCCTGCATAGATAATTGCATGACAATTGCCTTGT

        ebiAdaptor.open();
        cipfAdaptor.open();

        String ebiAdaptorSequence = ebiAdaptor.getSequence(region);
        System.out.println(ebiAdaptorSequence + " length: " + ebiAdaptorSequence.length());
        String cipfAdaptorSequence = cipfAdaptor.getSequence(region);
        System.out.println(cipfAdaptorSequence + " length: " + cipfAdaptorSequence.length());

        assertEquals(ebiAdaptorSequence, cipfAdaptorSequence);
    }
}