package org.opencb.biodata.models.variant.annotation;


import org.opencb.biodata.models.feature.Gene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Cristina Yenyxe Gonzalez Garcia &lt;cyenyxe@ebi.ac.uk&gt;
 * @author Alejandro Aleman Ramos &lt;aaleman@cipf.es&gt;
 */
public class VariantAnnotation {

    private String chromosome;
    
    private int start;
    
    private int end;
    
    private String referenceAllele;

    private String alternativeAllele;

    private String id;

    private List<Xref> xrefs;

    private List<String> hgvs;

    private List<ConsequenceType> consequenceTypes;

    private List<Score> conservedRegionScores;

    private List<Score> proteinSubstitutionScores;

    private List<Frequency> populationFrequencies;

    private List<CaddScore> caddScores;

    private List<ExpressionValue> expressionValues;

    private Map<String, Object> clinicalData;

    private Map<String, String> additionalAttributes;

    public VariantAnnotation() {
        this(null, 0, 0, null, null);
    }

    @Deprecated
    public VariantAnnotation(String chromosome, int start, int end, String referenceAllele) {
        this(chromosome, start, end, referenceAllele, null);
    }

    public VariantAnnotation(String chromosome, int start, int end, String referenceAllele, String alternativeAllele) {
        this(chromosome, start, end, referenceAllele, alternativeAllele, null, null, null, null, null, null, null, null, null, null, null);
    }

    public VariantAnnotation(String chromosome, int start, int end, String referenceAllele, String alternativeAllele,
                             String id, List<Xref> xrefs, List<String> hgvs, List<ConsequenceType> consequenceTypes,
                             List<Score> conservedRegionScores, List<Score> proteinSubstitutionScores,
                             List<Frequency> populationFrequencies, List<CaddScore> caddScores,
                             List<ExpressionValue> expressionValues, Map<String, Object> clinicalData,
                             Map<String, String> additionalAttributes) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.referenceAllele = referenceAllele;
        this.alternativeAllele = alternativeAllele;
        this.id = id;
        this.xrefs = xrefs;
        this.hgvs = hgvs;
        this.consequenceTypes = consequenceTypes;
        this.conservedRegionScores = conservedRegionScores;
        this.proteinSubstitutionScores = proteinSubstitutionScores;
        this.populationFrequencies = populationFrequencies;
        this.caddScores = caddScores;
        this.expressionValues = expressionValues;
        this.clinicalData = clinicalData;
        this.additionalAttributes = additionalAttributes;
    }

    @Override
    public String toString() {
        return "VariantAnnotation{" +
                "chromosome='" + chromosome + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", referenceAllele='" + referenceAllele + '\'' +
                ", alternativeAllele='" + alternativeAllele + '\'' +
                ", id='" + id + '\'' +
                ", xrefs=" + xrefs +
                ", hgvs=" + hgvs +
                ", consequenceTypes=" + consequenceTypes +
                ", conservedRegionScores=" + conservedRegionScores +
                ", proteinSubstitutionScores=" + proteinSubstitutionScores +
                ", populationFrequencies=" + populationFrequencies +
                ", caddScores=" + caddScores +
                ", expressionValues=" + expressionValues +
                ", clinicalData=" + clinicalData +
                ", additionalAttributes=" + additionalAttributes +
                '}';
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getReferenceAllele() {
        return referenceAllele;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
    }

    public String getAlternativeAllele() {
        return alternativeAllele;
    }

    public void setAlternativeAllele(String alternativeAllele) {
        this.alternativeAllele = alternativeAllele;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Xref> getXrefs() {
        return xrefs;
    }

    public void setXrefs(List<Xref> xrefs) {
        this.xrefs = xrefs;
    }

    public List<String> getHgvs() {
        return hgvs;
    }

    public void setHgvs(List<String> hgvs) {
        this.hgvs = hgvs;
    }

    public List<ConsequenceType> getConsequenceTypes() {
        return consequenceTypes;
    }

    public void setConsequenceTypes(List<ConsequenceType> consequenceTypes) {
        this.consequenceTypes = consequenceTypes;
    }

    public List<Score> getConservedRegionScores() {
        return conservedRegionScores;
    }

    public void setConservedRegionScores(List<Score> conservedRegionScores) {
        this.conservedRegionScores = conservedRegionScores;
    }

    public void setProteinSubstitutionScores(List<Score> proteinSubstitutionScores) {
        this.proteinSubstitutionScores = proteinSubstitutionScores;
    }

    public List<Frequency> getPopulationFrequencies() {
        return populationFrequencies;
    }

    public void setPopulationFrequencies(List<Frequency> populationFrequencies) {
        this.populationFrequencies = populationFrequencies;
    }

    public List<CaddScore> getCaddScores() {
        return caddScores;
    }

    public void setCaddScores(List<CaddScore> caddScores) {
        this.caddScores = caddScores;
    }

    public List<ExpressionValue> getExpressionValues() {
        return expressionValues;
    }

    public void setExpressionValues(List<ExpressionValue> expressionValues) {
        this.expressionValues = expressionValues;
    }

    public Map<String, Object> getClinicalData() {
        return clinicalData;
    }

    public void setClinicalData(Map<String, Object> clinicalData) {
        this.clinicalData = clinicalData;
    }

    public Map<String, String> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, String> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }



//    public Set<Gene> getGenes() {
//        return genes;
//    }

//    public void setGenes(Set<Gene> genes) {
//        this.genes = genes;
//    }

    @Deprecated
    public void addGene(Gene gene) {
        // TODO: broken compatibility with VariantGenesAnnotator
    }

//    public void addGene(Gene gene) {
//        this.genes.add(gene);
//    }
//
    @Deprecated
    public Map<String, List<VariantEffect>> getEffects() {
        // TODO: broken compatibility with VariantConsequenceTypeAnnotator
        return new HashMap<>();
    }

//    public Map<String, List<VariantEffect>> getEffects() {
//        return effects;
//    }
//
//    public void setEffects(Map<String, List<VariantEffect>> effects) {
//        this.effects = effects;
//    }
//
    @Deprecated
    public void addEffect(String key, VariantEffect effect) {
        // TODO: compatibility is broken with Variant object. We no longer want an effects attribute in VariantAnnotation
    }

//    public void addEffect(String key, VariantEffect effect) {
//        List<VariantEffect> ct = effects.get(key);
//        if (ct == null) {
//            ct = new ArrayList<>();
//            effects.put(key, ct);
//        }
//
//        ct.add(effect);
//    }
//

    @Deprecated
    public Map<String, Set<Frequency>> getFrequencies() {
        return new HashMap<>(); // TODO: broken compatibility with VariantEffectConverter
    }

//    public Map<String, Set<Frequency>> getFrequencies() {
//        return frequencies;
//    }
//
//    public Set<Frequency> getFrequenciesBySuperPopulation(String population) {
//        return frequencies.get(population);
//    }
//
//    public void setFrequencies(Map<String, Set<Frequency>> frequencies) {
//        this.frequencies = frequencies;
//    }
//
//    public boolean addFrequency(Frequency frequency) {
//        Set<Frequency> frequenciesBySuperPopulation = frequencies.get(frequency.getSuperPopulation());
//        if (frequenciesBySuperPopulation == null) {
//            frequenciesBySuperPopulation = new HashSet<>();
//            frequencies.put(frequency.getSuperPopulation(), frequenciesBySuperPopulation);
//        }
//        return frequenciesBySuperPopulation.add(frequency);
//    }

    @Deprecated
    public ProteinSubstitutionScores getProteinSubstitutionScores() {
        // TODO: broken compatibility with VariantPolyphenSIFTAnnotator
        return new ProteinSubstitutionScores();
    }

    public List<Score> getProteinSubstitutionScoresList() { //TODO: ChangeName
        return proteinSubstitutionScores;
    }

//    public List<Score> getProteinSubstitutionScores() {
//        return proteinSubstitutionScores;
//    }
//
//    public void setProteinSubstitutionScores(List<Score> proteinSubstitutionScores) {
//        this.proteinSubstitutionScores = proteinSubstitutionScores;
//    }

    @Deprecated
    public RegulatoryEffect getRegulatoryEffect() {
        // TODO: broken compatibility with VariantEffectConverter
        return new RegulatoryEffect();
    }

//    public RegulatoryEffect getRegulatoryEffect() {
//        return regulatoryEffect;
//    }

//    public void setRegulatoryEffect(RegulatoryEffect regulatoryEffect) {
//        this.regulatoryEffect = regulatoryEffect;
//    }

//    @Override
//    public String toString() {
//        return "VariantAnnotation{" +
//                "chromosome='" + chromosome + '\'' +
//                ", start=" + start +
//                ", end=" + end +
//                ", referenceAllele='" + referenceAllele + '\'' +
//                ", proteinSubstitutionScores=" + proteinSubstitutionScores +
//                '}';
//    }
}
