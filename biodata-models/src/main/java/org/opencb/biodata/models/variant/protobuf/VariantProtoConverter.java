package org.opencb.biodata.models.variant.protobuf;

import com.google.protobuf.ByteString;
import org.opencb.biodata.models.feature.AllelesCode;
import org.opencb.biodata.models.feature.Genotype;
//import org.opencb.biodata.models.CommonTypesConverter;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSourceEntry;
import org.opencb.biodata.models.variant.annotation.*;
import org.opencb.biodata.models.variant.stats.VariantHardyWeinbergStats;
import org.opencb.biodata.models.variant.stats.VariantStats;

import java.util.*;

/**
 * Created by jacobo on 19/11/14.
 */
public class VariantProtoConverter {

    /* Parsers */
    public static Variant parseVariant(VariantProtos.Variant proto) {
        Map<String, Set<String>> hgvs = new HashMap<>();
        for (VariantProtos.Variant.KeyValueSet entry : proto.getHgvsList()) {
            Set<String> set = new HashSet<>(entry.getValueCount());
            set.addAll(entry.getValueList());
            hgvs.put(entry.getKey(), set);
        }
        Map<String, VariantSourceEntry> variantSourceEntries = new HashMap<>(proto.getSourceEntriesCount());
        for (VariantProtos.Variant.KeyVariantSourceEntry entry : proto.getSourceEntriesList()) {
            VariantProtos.VariantSourceEntry value = entry.getValue();
            VariantSourceEntry vs = parseVariantSourceEntry(value);
            variantSourceEntries.put(entry.getKey(), vs);
        }

        return new Variant(parseVariantType(proto.getType()), proto.getChromosome(), proto.getStart(),
                proto.getEnd(), proto.getLength(), proto.getReference(), proto.getAlternate(),
                proto.getId(), hgvs, variantSourceEntries, parseVariantAnnotation(proto.getAnnotation()));
    }

    public static VariantSourceEntry parseVariantSourceEntry(VariantProtos.VariantSourceEntry proto) {
        HashMap<String, Map<String, String>> samplesData = new HashMap<>();
        for (VariantProtos.VariantSourceEntry.KeyMap entry : proto.getSamplesDataList()) {
            samplesData.put(entry.getKey(), parseMap(entry.getValue()));
        }
        return new VariantSourceEntry(proto.getFileId(), proto.getStudyId(), ((String[]) proto.getSecondaryAlternatesList().toArray()), proto.getFormat(), samplesData,
                parseVariantStats(proto.getStats()), parseMap(proto.getAttributes()));
    }

    public static VariantStats parseVariantStats(VariantStatsProtos.VariantStats proto) {

        Map<Genotype, Integer> genotypeCount = new HashMap<>(proto.getGenotypesCountCount());
        for (VariantStatsProtos.VariantStats.Count entry : proto.getGenotypesCountList()) {
            genotypeCount.put(parseGenotype(entry.getKey()), entry.getCount());
        }
        Map<Genotype, Float> genotypeFreq = new HashMap<>(proto.getGenotypesFreqCount());
        for (VariantStatsProtos.VariantStats.Frequency entry : proto.getGenotypesFreqList()) {
            genotypeFreq.put(parseGenotype(entry.getKey()), entry.getFrequency());
        }

        VariantStats vs = new VariantStats(proto.getRefAllele(), proto.getAltAllele(), parseVariantType(proto.getType()),
                proto.getRefAlleleCount(), proto.getAltAlleleCount(), genotypeCount, proto.getMissingAlleles(),
                proto.getMissingGenotypes(), proto.getRefAlleleFreq(), proto.getAltAlleleFreq(), genotypeFreq, proto.getMaf(),
                proto.getMgf(), proto.getMafAllele(), proto.getMgfGenotype(), proto.getPassedFilters(),
                proto.getMendelianErrors(), proto.getCasesPercentDominant(), proto.getControlsPercentDominant(),
                proto.getCasesPercentRecessive(), proto.getControlsPercentRecessive(), proto.getQuality(),
                proto.getNumSamples(), parseHardyWeinberg(proto.getHardyWeinberg()));

        return vs;
    }

    private static VariantHardyWeinbergStats parseHardyWeinberg(VariantStatsProtos.VariantHardyWeinbergStats proto) {
        return new VariantHardyWeinbergStats(proto.getChi2(), proto.getPValue(), proto.getN(), proto.getNAA(),
                proto.getNAla(), proto.getNLala(), proto.getEAA(), proto.getEAla(), proto.getELala(), proto.getP(),
                proto.getQ());
    }

    private static Genotype parseGenotype(VariantStatsProtos.Genotype proto) {
        int[] allelesIdx = new int[proto.getAllelesIdxCount()];
        int i = 0;
        for (Integer integer : proto.getAllelesIdxList()) {
            allelesIdx[i++] = integer;
        }
        return new Genotype(proto.getReference(), proto.getAlternate(), allelesIdx, proto.getPhased(), parseAllelesCode(proto), proto.getCount());

    }

    private static AllelesCode parseAllelesCode(VariantStatsProtos.Genotype proto) {
        return AllelesCode.valueOf(proto.getCode().name());
    }

    private static Variant.VariantType parseVariantType(VariantStatsProtos.VariantType type) {
        return Variant.VariantType.valueOf(type.name());
    }

    public static VariantAnnotation parseVariantAnnotation(VariantAnnotationProtos.VariantAnnotation proto) {
        VariantAnnotation va = new VariantAnnotation(proto.getChromosome(), proto.getStart(), proto.getEnd(), proto.getReferenceAllele(), proto.getAlternativeAllele());

        va.setId(proto.getId());

        List<CaddScore> caddScores = new LinkedList<>();
        for (VariantAnnotationProtos.CaddScore caddScore : proto.getCaddScoresList()) {
            caddScores.add(parseCaddScore(caddScore));
        }
        va.setCaddScores(caddScores);


        List<ConsequenceType> consequenceTypes = new LinkedList<>();
        for (VariantAnnotationProtos.ConsequenceType consequenceType : proto.getConsequenceTypesList()) {
            consequenceTypes.add(parseConsequenceType(consequenceType));
        }
        va.setConsequenceTypes(consequenceTypes);


        List<Score> conservedRegionScores = new LinkedList<>();
        for (VariantAnnotationProtos.Score score : proto.getConservedRegionScoresList()) {
            conservedRegionScores.add(parseScore(score));
        }
        va.setConservedRegionScores(conservedRegionScores);

        List<ExpressionValue> expressionValues = new LinkedList<>();
        for (VariantAnnotationProtos.ExpressionValue expressionValue : proto.getExpressionValuesList()) {
            expressionValues.add(parseExpressionValue(expressionValue));
        }
        va.setExpressionValues(expressionValues);

        va.setHgvs(proto.getHgvsList());

        List<Frequency> frequencies = new LinkedList<>();
        for (VariantAnnotationProtos.Frequency frecuency : proto.getPopulationFrequenciesList()) {
            frequencies.add(parseFrequency(frecuency));
        }
        va.setPopulationFrequencies(frequencies);


        List<Score> proteinSubstitutionScores = new LinkedList<>();
        for (VariantAnnotationProtos.Score score : proto.getProteinSubstitutionScoresList()) {
            proteinSubstitutionScores.add(parseScore(score));
        }
        va.setProteinSubstitutionScores(proteinSubstitutionScores);


        List<Xref> xrefs = new LinkedList<>();
        for (VariantAnnotationProtos.XRef xRef : proto.getXrefsList()) {
            xrefs.add(parseXref(xRef));
        }
        va.setXrefs(xrefs);


        va.setAdditionalAttributes(parseMap(proto.getAdditionalAttributes()));
        va.setClinicalData(parseMap(proto.getClinicalData()));

        return va;
    }

    public static Xref parseXref(VariantAnnotationProtos.XRef proto) {
        return new Xref(proto.getId(), proto.getSrc());
    }

    public static Frequency parseFrequency(VariantAnnotationProtos.Frequency proto) {
        return new Frequency(proto.getStudy(), proto.getSuperPopulation(), proto.getPopulation(), proto.getFrequency());
    }

    public static ExpressionValue parseExpressionValue(VariantAnnotationProtos.ExpressionValue proto) {
        return new ExpressionValue(proto.getTissueName(), proto.getExperiment(), proto.getValue());
    }

    public static Score parseScore(VariantAnnotationProtos.Score proto) {
        return new Score(proto.getScore(), proto.getSrc(), proto.getVersion());
    }

    private static ConsequenceType parseConsequenceType(VariantAnnotationProtos.ConsequenceType proto) {
        ConsequenceType consequenceType = new ConsequenceType(
                proto.getGeneName(), proto.getEnsembleGeneId(), proto.getEnsembleTranscriptId(), proto.getSOAccession(),
                proto.getSOName(), proto.getRelativePosition(), proto.getCodon(), proto.getAaChange());
        return consequenceType;
    }

    public static CaddScore parseCaddScore(VariantAnnotationProtos.CaddScore caddScore) {
        return new CaddScore(caddScore.getTranscriptId(), caddScore.getCScore(), caddScore.getRawScore());
    }

    public static Map<String, String> parseMap(VariantAnnotationProtos.Map map) {
        HashMap<String, String> hashMap = new HashMap<>();
        for (VariantAnnotationProtos.Map.KeyValue entry : map.getEntryList()) {
            hashMap.put(entry.getKey(), entry.getValue());
        }
        return hashMap;
    }

    public static Map<String, Object> parseMap(VariantAnnotationProtos.RawMap map) {
        HashMap<String, Object> hashMap = new HashMap<>();
        for (VariantAnnotationProtos.RawMap.KeyValue entry : map.getEntryList()) {
            hashMap.put(entry.getKey(), entry.getValue().toByteArray());
        }
        return hashMap;
    }

    /* Serializers */
    public static VariantProtos.Variant serializeVariant(Variant variant) {
        VariantProtos.Variant.Builder builder = VariantProtos.Variant.newBuilder()
                .setChromosome(variant.getChromosome())
                .setStart(variant.getStart())
                .setEnd(variant.getEnd())
                .setAlternate(variant.getAlternate())
                .setReference(variant.getReference())
                .setType(serializeVariantType(variant.getType()))
                .setLength(variant.getLength());

        if(variant.getId() != null) {
            builder.setId(variant.getId());
        }

        if(variant.getAnnotation() != null) {
            builder.setAnnotation(serializeVariantAnnotation(variant.getAnnotation()));
        }

        for (Map.Entry<String, Set<String>> entry : variant.getHgvs().entrySet()) {
            builder.addHgvs(VariantProtos.Variant.KeyValueSet
                            .newBuilder()
                            .setKey(entry.getKey())
                            .addAllValue(entry.getValue())
            );
        }

        for (Map.Entry<String, VariantSourceEntry> entry : variant.getSourceEntries().entrySet()) {
            builder.addSourceEntries(VariantProtos.Variant.KeyVariantSourceEntry
                    .newBuilder()
                    .setKey(entry.getKey())
                    .setValue(serializeVariantSourceEntry(entry.getValue()))
            );
        }

        return builder.build();
    }

    public static VariantProtos.VariantSourceEntry serializeVariantSourceEntry(VariantSourceEntry variantSourceEntry) {
        VariantProtos.VariantSourceEntry.Builder b = VariantProtos.VariantSourceEntry
                .newBuilder();
        if(variantSourceEntry.getFileId() != null) b.setFileId(variantSourceEntry.getFileId());
        if(variantSourceEntry.getFormat() != null) b.setFormat(variantSourceEntry.getFormat());
        if(variantSourceEntry.getStudyId()!= null) b.setStudyId(variantSourceEntry.getStudyId());
        if(variantSourceEntry.getStats()  != null) b.setStats(serializeVariantStats(variantSourceEntry.getStats()));
        return b.build();
    }

    public static VariantStatsProtos.VariantStats serializeVariantStats(VariantStats stats) {

        VariantStatsProtos.VariantStats.Builder builder = VariantStatsProtos.VariantStats.newBuilder();

        builder.setRefAllele(stats.getRefAllele())
                .setAltAllele(stats.getAltAllele())
                .setType(serializeVariantType(stats.getVariantType()));

        // Allele and genotype counts
        builder.setRefAlleleCount(stats.getRefAlleleCount());
        builder.setAltAlleleCount(stats.getAltAlleleCount());
        for (Map.Entry<Genotype, Integer> count : stats.getGenotypesCount().entrySet()) {
            VariantStatsProtos.VariantStats.Count.Builder countBuilder = VariantStatsProtos.VariantStats.Count.newBuilder();
            countBuilder.setKey(serializeGenotype(count.getKey()));
            countBuilder.setCount(count.getValue());
            builder.addGenotypesCount(countBuilder.build());
        }

        // Missing values
        builder.setMissingAlleles(stats.getMissingAlleles());
        builder.setMissingGenotypes(stats.getMissingGenotypes());

        // Allele and genotype frequencies
        builder.setRefAlleleFreq(stats.getRefAlleleFreq());
        builder.setAltAlleleFreq(stats.getAltAlleleFreq());
        for (Map.Entry<Genotype, Float> freq : stats.getGenotypesFreq().entrySet()) {
            VariantStatsProtos.VariantStats.Frequency.Builder countBuilder = VariantStatsProtos.VariantStats.Frequency.newBuilder();
            countBuilder.setKey(serializeGenotype(freq.getKey()));
            countBuilder.setFrequency(freq.getValue());
            builder.addGenotypesFreq(countBuilder.build());
        }

        // MAF and MGF
        builder.setMaf(stats.getMaf());
        builder.setMgf(stats.getMgf());
        builder.setMafAllele(stats.getMafAllele());
        builder.setMgfGenotype(stats.getMgfGenotype());


        // Miscellaneous
        builder.setPassedFilters(stats.hasPassedFilters());

        builder.setQuality(stats.getQuality());
        builder.setNumSamples(stats.getNumSamples());


        // Optional fields, they require pedigree information
        if(stats.getMendelianErrors() != -1)           builder.setMendelianErrors(stats.getMendelianErrors());

        if(stats.getCasesPercentDominant() != -1)      builder.setCasesPercentDominant(stats.getCasesPercentDominant());
        if(stats.getControlsPercentDominant() != -1)   builder.setControlsPercentDominant(stats.getControlsPercentDominant());
        if(stats.getCasesPercentRecessive() != -1)     builder.setCasesPercentRecessive(stats.getCasesPercentRecessive());
        if(stats.getControlsPercentRecessive() != -1)  builder.setControlsPercentRecessive(stats.getControlsPercentRecessive());

        if(stats.getHw() != null)                      builder.setHardyWeinberg(serializeHadryWeinberg(stats.getHw()));

        return builder.build();
    }

    private static VariantStatsProtos.VariantType serializeVariantType(Variant.VariantType type) {
        return VariantStatsProtos.VariantType.valueOf(type.name());
    }

    private static VariantStatsProtos.VariantHardyWeinbergStats serializeHadryWeinberg(VariantHardyWeinbergStats hw) {
        return VariantStatsProtos.VariantHardyWeinbergStats
                .newBuilder()
                .setChi2(hw.getChi2())
                .setPValue(hw.getpValue())
                .setN(hw.getN())
                .setNAA(hw.getN_AA())
                .setNAla(hw.getN_Aa())
                .setNLala(hw.getN_aa())
                .setEAA(hw.getE_AA())
                .setEAla(hw.getE_Aa())
                .setELala(hw.getE_aa())
                .setP(hw.getP())
                .setQ(hw.getQ()).build();
    }

    private static VariantStatsProtos.Genotype serializeGenotype(Genotype genotype) {
        VariantStatsProtos.Genotype.Builder builder = VariantStatsProtos.Genotype.newBuilder();
        if(genotype.getReference() != null) builder.setReference(genotype.getReference());
        if(genotype.getAlternate() != null) builder.setAlternate(genotype.getAlternate());
        builder.setPhased(genotype.isPhased());
        for (int i : genotype.getAllelesIdx()) {
            builder.addAllelesIdx(i);
        }
        builder.setCount(genotype.getCount());
        builder.setCode(serializeAllelesCode(genotype));
        return builder.build();
    }

    private static VariantStatsProtos.AllelesCode serializeAllelesCode(Genotype genotype) {
        return VariantStatsProtos.AllelesCode.valueOf(genotype.getCode().name());
    }

    public static VariantAnnotationProtos.VariantAnnotation serializeVariantAnnotation(VariantAnnotation variantAnnotation) {
        VariantAnnotationProtos.VariantAnnotation.Builder builder = VariantAnnotationProtos.VariantAnnotation
                .newBuilder()
                .setChromosome(variantAnnotation.getChromosome())
                .setStart(variantAnnotation.getStart())
                .setEnd(variantAnnotation.getEnd())
                .setAlternativeAllele(variantAnnotation.getAlternativeAllele())
                .setReferenceAllele(variantAnnotation.getReferenceAllele());

        if(variantAnnotation.getId() != null) builder.setId(variantAnnotation.getId());
        if(variantAnnotation.getHgvs()!= null) builder.addAllHgvs(variantAnnotation.getHgvs());

        if(variantAnnotation.getCaddScores() != null) {
            for (CaddScore caddScore : variantAnnotation.getCaddScores()) {
                builder.addCaddScores(serializeCaddScore(caddScore));
            }
        }
        if(variantAnnotation.getConsequenceTypes() != null) {
            for (ConsequenceType consequenceType : variantAnnotation.getConsequenceTypes()) {
                builder.addConsequenceTypes(serializeConsequenceType(consequenceType));
            }
        }
        if(variantAnnotation.getProteinSubstitutionScoresList() != null) {
            for (Score score : variantAnnotation.getProteinSubstitutionScoresList()) {
                builder.addProteinSubstitutionScores(serializeScore(score));
            }
        }
        if(variantAnnotation.getConservedRegionScores() != null) {
            for (Score score : variantAnnotation.getConservedRegionScores()) {
                builder.addConservedRegionScores(serializeScore(score));
            }
        }
        if(variantAnnotation.getExpressionValues() != null) {
            for (ExpressionValue expressionValue : variantAnnotation.getExpressionValues()) {
                builder.addExpressionValues(serializeExpressionValue(expressionValue));
            }
        }
        if(variantAnnotation.getPopulationFrequencies() != null) {
            for (Frequency frequency : variantAnnotation.getPopulationFrequencies()) {
                builder.addPopulationFrequencies(serializeFrequency(frequency));
            }
        }
        if(variantAnnotation.getXrefs() != null) {
            for (Xref xref : variantAnnotation.getXrefs()) {
                builder.addXrefs(serializeXref(xref));
            }
        }

        if(variantAnnotation.getAdditionalAttributes() != null) {
            builder.setAdditionalAttributes(serializeMap(variantAnnotation.getAdditionalAttributes()));
        }
//        CommonTypesConverter.stringObjectMapToProto(variantAnnotation.getAdditionalAttributes());
//        variantAnnotation.getAdditionalAttributes()
////        variantAnnotation.getClinicalData()


        return builder.build();
    }

    public static VariantAnnotationProtos.XRef serializeXref(Xref xref) {
        VariantAnnotationProtos.XRef.Builder b = VariantAnnotationProtos.XRef.newBuilder();
        if(xref.getSrc() != null) b.setSrc(xref.getSrc());
        if(xref.getId() != null) b.setId(xref.getId());
        return b.build();
    }

    public static VariantAnnotationProtos.Frequency serializeFrequency(Frequency frequency) {
        VariantAnnotationProtos.Frequency.Builder b = VariantAnnotationProtos.Frequency
                .newBuilder()
                .setFrequency(frequency.getFrequency());
        if(frequency.getPopulation() != null) b.setPopulation(frequency.getPopulation());
        if(frequency.getStudy() != null) b.setStudy(frequency.getStudy());
        if(frequency.getSuperPopulation() != null) b.setSuperPopulation(frequency.getSuperPopulation());
        return b.build();
    }

    public static VariantAnnotationProtos.ExpressionValue serializeExpressionValue(ExpressionValue expressionValue) {
        VariantAnnotationProtos.ExpressionValue.Builder b = VariantAnnotationProtos.ExpressionValue
                .newBuilder()
                .setValue(expressionValue.getValue());
        if(expressionValue.getExperiment() != null) b.setExperiment(expressionValue.getExperiment());
        if(expressionValue.getTissueName() != null) b.setTissueName(expressionValue.getTissueName());
        return b.build();
    }

    public static VariantAnnotationProtos.Score serializeScore(Score score) {
        VariantAnnotationProtos.Score.Builder s = VariantAnnotationProtos.Score
                .newBuilder()
                .setScore(score.getScore());
        if(score.getSrc() != null) s.setSrc(score.getSrc());
        if(score.getVersion() != null) s.setVersion(score.getVersion());
        return s.build();
    }

    public static VariantAnnotationProtos.ConsequenceType serializeConsequenceType(ConsequenceType consequenceType) {
        VariantAnnotationProtos.ConsequenceType.Builder c = VariantAnnotationProtos.ConsequenceType
                .newBuilder()
                .setSOAccession(consequenceType.getSOAccession())
                .setRelativePosition(consequenceType.getRelativePosition());
        if(consequenceType.getAaChange() != null)               c.setAaChange(consequenceType.getAaChange());
        if(consequenceType.getSOName() != null)                 c.setSOName(consequenceType.getSOName());
        if(consequenceType.getCodon() != null)                  c.setCodon(consequenceType.getCodon());
        if(consequenceType.getEnsemblGeneId() != null)          c.setEnsembleGeneId(consequenceType.getEnsemblGeneId());
        if(consequenceType.getEnsemblTranscriptId() != null)    c.setEnsembleTranscriptId(consequenceType.getEnsemblTranscriptId());
        if(consequenceType.getGeneName() != null)               c.setGeneName(consequenceType.getGeneName());
        return c.build();
    }

    public static VariantAnnotationProtos.CaddScore serializeCaddScore(CaddScore caddScore) {
        VariantAnnotationProtos.CaddScore.Builder c = VariantAnnotationProtos.CaddScore.newBuilder()
                .setCScore(caddScore.getcScore())
                .setRawScore(caddScore.getRawScore());
        if (caddScore.getTranscriptId() != null)                c.setTranscriptId(caddScore.getTranscriptId());
        return c.build();
    }

    public static VariantAnnotationProtos.Map serializeMap(Map<String, String> map) {
        VariantAnnotationProtos.Map.Builder mapBuilder = VariantAnnotationProtos.Map.newBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            mapBuilder.addEntry(VariantAnnotationProtos.Map.KeyValue.newBuilder().setKey(entry.getKey()).setValue(entry.getValue()).build());
        }
        return mapBuilder.build();
    }

    public static VariantAnnotationProtos.RawMap rawMap(Map<String, Object> map) {
        VariantAnnotationProtos.RawMap.Builder mapBuilder = VariantAnnotationProtos.RawMap.newBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            mapBuilder.addEntry(VariantAnnotationProtos.RawMap.KeyValue
                    .newBuilder()
                    .setKey(entry.getKey())
                    .setValue(ByteString.copyFrom(entry.getValue().toString().getBytes()))
                    .build()
            );
        }
        return mapBuilder.build();
    }

}
