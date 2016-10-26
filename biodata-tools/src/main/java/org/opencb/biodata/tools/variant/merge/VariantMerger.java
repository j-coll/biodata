/**
 *
 */
package org.opencb.biodata.tools.variant.merge;

import htsjdk.variant.vcf.VCFConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opencb.biodata.models.feature.Genotype;
import org.opencb.biodata.models.variant.StudyEntry;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantVcfFactory;
import org.opencb.biodata.models.variant.avro.AlternateCoordinate;
import org.opencb.biodata.models.variant.avro.FileEntry;
import org.opencb.biodata.models.variant.avro.VariantType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Haimel mh719+git@cam.ac.uk
 *
 */
public class VariantMerger {

    @Deprecated
    public static final String VCF_FILTER = VCFConstants.GENOTYPE_FILTER_KEY;

    public static final String GENOTYPE_FILTER_KEY = VCFConstants.GENOTYPE_FILTER_KEY;

    //    public static final String VCF_FILTER = "FILTER";
    public static final String GT_KEY = VCFConstants.GENOTYPE_KEY;
    public static final String PASS_VALUE = "PASS";
    public static final String DEFAULT_FILTER_VALUE = ".";
    //    public static final String CALL_KEY = "CALL";

    /**
     *
     */
    public VariantMerger() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Create and returns a new Variant using the target as a
     * position template ONLY and merges the provided variants
     * for this position. <b> The target is not present in the
     * merged output!!!</b>
     *
     * @param template Template for position and study only
     * @param load Variants to merge for position
     * @return Variant new Variant object with merged information
     */
    public Variant mergeNew(Variant template,Collection<Variant> load){
        Variant current = createFromTemplate(template);
        merge(current,load);
        return current;
    }

    /**
     * Create an empty Variant (position, ref, alt) from a template with basic Study information without samples.
     * @param target Variant to take as a template
     * @return Variant filled with chromosome, start, end, ref, alt, study ID and format set to GT only, BUT no samples.
     */
    public Variant createFromTemplate(Variant target) {
        Variant var = new Variant(target.getChromosome(), target.getStart(), target.getEnd(), target.getReference(), target.getAlternate());
        var.setType(target.getType());
        for(StudyEntry tse : target.getStudies()){
            StudyEntry se = new StudyEntry(tse.getStudyId());
            se.setFiles(Collections.singletonList(new FileEntry("", "", new HashMap<>())));
            se.setFormat(Arrays.asList(GT_KEY, GENOTYPE_FILTER_KEY));
            se.setSamplesPosition(new HashMap<String, Integer>());
            se.setSamplesData(new ArrayList<List<String>>());
            var.addStudyEntry(se);
        }
        return var;
    }

    private void isValidVariant(Variant current) throws IllegalArgumentException{
        if (current.getType().equals(VariantType.NO_VARIATION)) {
            throw new IllegalStateException("Current variant can't be a NO_VARIANT");
        }

        // Validate variant information
//        ensureGtFormat(current);
        if (getStudy(current).getFormat() == null || getStudy(current).getFormat().isEmpty()) {
            throw new IllegalArgumentException("Format of sample data is empty!!!!!!");
        }
    }


    Variant merge(Variant current, Variant load){
        return merge(current, Collections.singleton(load));
    }

    /**
     * Merge a collection of variants into one variant.
     * @param current
     * @param load
     * @return Modified current object.
     */
    public Variant merge(Variant current, Collection<Variant> load){
        isValidVariant(current);
        // Build alt list
        List<Pair<Variant, List<AlternateCoordinate>>> loadAlts = load.stream()
                .map(v -> new ImmutablePair<>(v, buildAltList(v)))
                .filter(p -> hasAnyOverlap(current, p.getLeft(), p.getRight()))
                .collect(Collectors.toList());


        mergeVariants(current, loadAlts);
        return current;
    }

    public Variant merge(Variant current, List<Pair<Variant, List<AlternateCoordinate>>> vatToAlts) {
        isValidVariant(current);
        // Filter alt list
        List<Pair<Variant, List<AlternateCoordinate>>> loadAlts = vatToAlts.stream()
                .filter(p -> hasAnyOverlap(current, p.getLeft(), p.getRight()))
                .collect(Collectors.toList());
        mergeVariants(current, loadAlts);
        return current;
    }

    public static boolean hasAnyOverlap(Variant current, Variant other) {
        if (current.overlapWith(other, true)) {
            return true;
        }
        // SecAlt of query
        return other.getStudies().stream()
                .filter( s -> // foreach study
                        s.getSecondaryAlternates().stream()
                                .filter(a -> {
                                            // Avoid NPE
                                            a = copyAlt(other, a);
                                            return current.overlapWith(a.getChromosome(), a.getStart(), a.getEnd(), true);
                                        }
                                )
                                .findAny()
                                .isPresent()
                )
                .findAny()
                .isPresent();
    }

    public static boolean hasAnyOverlap(Variant current, Variant other, Collection<AlternateCoordinate> alts) {
        if (current.overlapWith(other, true)) {
            return true; // Important for REF region
        }
        return alts.stream().filter(a -> current.overlapWith(a.getChromosome(), a.getStart(), a.getEnd(), true))
                .findAny().isPresent();
    }

    private String variantToString(Variant v) {
        StringBuilder sb = new StringBuilder(v.getChromosome());
        sb.append(":").append(v.getStart()).append("-").append(v.getEnd());
        sb.append(v.getReference().isEmpty() ? "-" : v.getReference());
        sb.append(":").append(v.getAlternate().isEmpty() ? "-" : v.getAlternate()).append("[");
        StudyEntry se = v.getStudies().get(0);
        List<List<String>> sd = se.getSamplesData();
        for(String sn : se.getSamplesName()){
            Integer pos = se.getSamplesPosition().get(sn);
            if (pos >= sd.size()) {
                sb.append(sn).append(":S;");
            } else if (null == sd.get(pos) || sd.get(pos).size() < 1) {
                sb.append(sn).append(":G;");
            } else {
                String gt = sd.get(pos).get(0); // GT
                sb.append(sn).append(":").append(gt).append(";");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Merge variants into current object.
     * @param current Variant object - in/out
     * @param varToAlts List of Variant with matching ALTs (ALT and secondary ALTs)
     */
    void mergeVariants(Variant current, List<Pair<Variant, List<AlternateCoordinate>>> varToAlts) {
        StudyEntry currentStudy = getStudy(current);

        String defaultFilterValue = currentStudy.getFiles().isEmpty() ? DEFAULT_FILTER_VALUE
                : currentStudy.getFiles().get(0).getAttributes().getOrDefault(VariantVcfFactory.FILTER, DEFAULT_FILTER_VALUE);
        ensureFormat(currentStudy, GENOTYPE_FILTER_KEY, defaultFilterValue);

        List<String> orderedSamplesName = new ArrayList<>(currentStudy.getOrderedSamplesName());
        Set<String> currSampleNames = new HashSet<>(currentStudy.getSamplesName());


        // Build ALT index
        List<AlternateCoordinate> altList = buildAltsList(current, varToAlts.stream().map(p -> p.getRight()).collect(Collectors.toList()));
        Map<AlternateCoordinate, Integer> altIdx = index(altList);

        // Update SecALt list
        currentStudy.setSecondaryAlternates(altList.subList(1,altList.size()));
        Map<String, Integer> formatPositions = new HashMap<>(currentStudy.getFormatPositions());
        Map<String, Integer> additionalForamt = new HashMap<>(formatPositions);
        additionalForamt.remove(GT_KEY);
        additionalForamt.remove(GENOTYPE_FILTER_KEY);

        if (!formatPositions.keySet().contains(GT_KEY)) {
            throw new IllegalStateException("Current study expected to contain 'GT'");
        }
        if (! (formatPositions.get(GT_KEY).equals(0))) {
            throw new IllegalStateException("Current study expected to be in order of 'GT'");
        }
        Map<String, String> sampleToGt = sampleToGt(current);
        Map<String, String> sampleToFilter = sampleToSampleData(current, GENOTYPE_FILTER_KEY);
        Map<String, Map<Integer, String>> sampleToAdditional = new HashMap<>();
        additionalForamt.forEach((k,id) -> {
            Map<String, String> sampleToValue = sampleToSampleData(current, k);
            sampleToValue.forEach((s,v) -> {
                Map<Integer, String> keyMap = sampleToAdditional.get(s);
                if (keyMap == null) {
                    keyMap = new HashMap<Integer, String>();
                    sampleToAdditional.put(s, keyMap);
                }
                keyMap.put(id, v);
            });
        });

        varToAlts.forEach(e -> {
            Variant other = e.getKey();
            Map<Integer, AlternateCoordinate> otherAltIdx = index(e.getValue()).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            StudyEntry otherStudy = getStudy(other);
            Map<String, String> otherSampleToGt = sampleToGt(other);
            Map<String, String> otherSampleToFilter = otherStudy.getFormat().contains(GENOTYPE_FILTER_KEY)
                    ? sampleToSampleData(other, GENOTYPE_FILTER_KEY)
                    : sampleToAttribute(other, VariantVcfFactory.FILTER);
            Map<String, Map<Integer, String>> otherSampleToAdditionalFormats = new HashMap<>();
            additionalForamt.forEach((ks,k) -> {
                Map<String, String> data = sampleToSampleData(other, ks);
                data.forEach((s,v) -> {
                    Map<Integer, String> otherAdditional = otherSampleToAdditionalFormats.get(s);
                    if (null == otherAdditional) {
                        otherAdditional = new HashMap<>();
                        otherSampleToAdditionalFormats.put(s, otherAdditional);
                    }
                    otherAdditional.put(k, v);
                });
            });

            checkForDuplicates(current, other, currentStudy, otherStudy, e.getValue());

            // Add GT data for each sample to current Variant
            otherStudy.getOrderedSamplesName().forEach(sampleName -> {
                boolean alreadyInStudy = currSampleNames.contains(sampleName);
                if (!alreadyInStudy) {
                    orderedSamplesName.add(sampleName);
                    currSampleNames.add(sampleName);
                }

                // GT data
                boolean isGtUpdated = false;
                String gt = otherSampleToGt.get(sampleName);
                if (StringUtils.isBlank(gt)) {
                    throw new IllegalStateException(String.format("No GT found for sample %s in \nVariant: %s\nIndex:%s",
                            sampleName, other.getImpl(), sampleToGt));
                }
                String updatedGt = updateGT(gt, altIdx, otherAltIdx);
                if (alreadyInStudy) {
                    String currGT = sampleToGt.get(sampleName);
                    List<String> gtlst = new ArrayList<>(Arrays.asList(currGT.split(",")));
                    if (!gtlst.contains(updatedGt)) {
                        gtlst.add(updatedGt);
                        updatedGt = StringUtils.join(gtlst, ',');
                        isGtUpdated = true;
                    }
                    sampleToGt.put(sampleName, updatedGt);
                }
                sampleToGt.put(sampleName, updatedGt);

                // Filter
                String filter = otherSampleToFilter.getOrDefault(sampleName, DEFAULT_FILTER_VALUE);
                if (alreadyInStudy && isGtUpdated) {
                    String currFilter = sampleToFilter.get(sampleName);
                    List<String> filterLst = new ArrayList<>(Arrays.asList(currFilter.split(",")));
                    filterLst.add(filter);
                    filter = StringUtils.join(filterLst, ',');
                }
                sampleToFilter.put(sampleName, filter);

                // Additional data
                Map<Integer, String> otherAdditional = otherSampleToAdditionalFormats.get(sampleName);
                if (otherAdditional != null) {
                    if (!sampleToAdditional.containsKey(sampleName)) {
                        sampleToAdditional.put(sampleName, otherAdditional);
                    } else {
                        throw new IllegalStateException("TODO - merge additional formats!!!");
                    }
                }
            });
            mergeFiles(current, other, currentStudy, otherStudy);
        });
        updateStudy(currentStudy, sampleToGt, sampleToFilter, sampleToAdditional, orderedSamplesName);
    }

    /**
     * Update study with Annotations.
     * @param study
     * @param sampleToGt
     * @param sampleToFilter
     * @param sampleToAdditional
     * @param orderedSamplesName
     */
    private void updateStudy(
            StudyEntry study, Map<String, String> sampleToGt, Map<String, String> sampleToFilter,
            Map<String, Map<Integer, String>> sampleToAdditional, List<String> orderedSamplesName) {
        List<String> format = study.getFormat();
        String[] formatTemplate = new String[format.size()];
        Arrays.fill(formatTemplate, StringUtils.EMPTY);
        LinkedHashMap<String, Integer> samplesPosition = new LinkedHashMap<>();
        int sampleSize = orderedSamplesName.size();
        for (int i = 0; i < sampleSize; i++) {
            samplesPosition.put(orderedSamplesName.get(i), i);
        }
        List<String>[] samplesData = new List[sampleSize];
        // Init format data with empty string.
        for (int i = 0; i < sampleSize; ++i) {
            samplesData[i] = new ArrayList<>(Arrays.asList(formatTemplate));
        }
        for (int i = 0; i < format.size(); i++) {
            int pos = i;
            switch (format.get(i)) {
                case GT_KEY:
                    sampleToGt.forEach((k,v) -> samplesData[samplesPosition.get(k)].set(pos,v));
                    break;
                case GENOTYPE_FILTER_KEY:
                    sampleToFilter.forEach((k,v) -> samplesData[samplesPosition.get(k)].set(pos,v));
                    break;
                default:
                    break;
            }
        }
        // and additional values
        sampleToAdditional.forEach((k,m) -> m.forEach((pos, val) -> samplesData[samplesPosition.get(k)].set(pos, val)));

        for (int i = 0; i < samplesData.length; i++) {
            if (null == samplesData[i]) {
                throw new IllegalStateException("Position " + i + " of " + samplesData.length + " not filled!!!");
            }
        }
        study.setSamplesPosition(samplesPosition);
        study.setSamplesData(Arrays.asList(samplesData));
    }

    private String updateGT(String gt, Map<AlternateCoordinate, Integer> curr, Map<Integer, AlternateCoordinate> other) {
        Genotype gto = new Genotype(gt);
        int[] idx = gto.getAllelesIdx();
        int len = idx.length;
        IntStream.range(0, len).boxed().filter(i -> idx[i] > 0 && idx[i] <= other.size())
                .forEach(i -> gto.updateAlleleIdx(i, curr.get(other.get(idx[i]))));
        if (!gto.isPhased()) {
            Arrays.sort(idx);
        }
        return gto.toGenotypeString();
    }

    private List<AlternateCoordinate> buildAltsList (Variant current, Collection<List<AlternateCoordinate>> alts) {
        List<AlternateCoordinate> currAlts = buildAltList(current);
        Set<AlternateCoordinate> altSets = new HashSet<>(currAlts);
        alts.forEach(l -> altSets.addAll(l));
        // remove current alts
        altSets.removeAll(currAlts);
        currAlts.addAll(altSets);
        return currAlts;
    }

    private Map<AlternateCoordinate, Integer> index(List<AlternateCoordinate> alts) {
        Map<AlternateCoordinate, Integer> altIdx = new HashMap<>();
        AtomicInteger pos = new AtomicInteger(1); // Start at 1 -> first Alt genotype
        for (AlternateCoordinate a : alts) {
            altIdx.put(a, pos.getAndIncrement());
        }
        return altIdx;
    }

    private boolean checkForDuplicates(Variant current, Variant other, StudyEntry currentStudy, StudyEntry otherStudy, List<AlternateCoordinate> otherAlts) {
        Set<String> duplicateSamples = otherStudy.getSamplesName().stream()
                .filter(s -> currentStudy.getSamplesName().contains(s))
                .collect(Collectors.toSet());
        if (!duplicateSamples.isEmpty()) {
            Map<String, String> currSampleToGts = sampleToGt(current);
            List<AlternateCoordinate> currAlts = new ArrayList<>();
            currAlts.add(getMainAlternate(current));
            currAlts.addAll(currentStudy.getSecondaryAlternates());
            for (String dupSample : duplicateSamples) {
                String currGt = currSampleToGts.get(dupSample);
                Set<Integer> gtIdxSet = Arrays.stream(currGt.split(","))
                        .flatMap(e -> Arrays.stream(e.split("/")))
                        .flatMap(e -> Arrays.stream(e.split("\\|")))
                        .filter(g -> NumberUtils.isNumber(g))
                        .map(g -> Integer.valueOf(g))
                        .filter(g -> g > 0)
                        .collect(Collectors.toSet());
                // Find same alleles in current and other for this individual
                List<AlternateCoordinate> currConflict = gtIdxSet.stream()
                        .map(g -> currAlts.get(g - 1))
                        .filter(a -> otherAlts.stream().filter(o -> isSameVariant(a, o)).count() > 0)
                        .collect(Collectors.toList());
                // Only Throw Exception in case of duplicated Alt for same individual
                if (!currConflict.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Duplicated entries during merging: Issue with ID ");
                    sb.append(dupSample).append("; Variants: ");
                    currConflict.forEach(a -> sb.append("\n").append(a));
                    sb.append(";");
                    throw new IllegalStateException(sb.toString());
                }
            }
        }
        return false;
    }


    protected void validateAlternate(AlternateCoordinate alt) {
        if (alt.getChromosome() == null) {
            throw new IllegalStateException("Chromosome of alt is null: " + alt);
        }
        if (alt.getStart() == null) {
            throw new IllegalStateException("Start of alt is null: " + alt);
        }
        if (alt.getEnd() == null) {
            throw new IllegalStateException("End of alt is null: " + alt);
        }
        if (alt.getReference() == null) {
            throw new IllegalStateException("Reference of alt is null: " + alt);
        }
        if (alt.getAlternate() == null) {
            throw new IllegalStateException("Alternate of alt is null: " + alt);
        }
    }

    protected boolean equals(AlternateCoordinate alt1, AlternateCoordinate alt2) {
        return getStart(alt2).equals(getStart(alt1)) && getEnd(alt2).equals(getEnd(alt1))
                && getReference(alt2).equals(getReference(alt1))
                && getAlternate(alt2).equals(getAlternate(alt1));
    }

    private String getReference(AlternateCoordinate s) {
        return s.getReference();
    }

    private String getAlternate(AlternateCoordinate s) {
        return s.getAlternate();
    }

    private Integer getStart(AlternateCoordinate s) {
        return s.getStart();
    }

    private Integer getEnd(AlternateCoordinate s) {
        return s.getEnd();
    }

    /**
     * Build a list of all the alternates from a variant. Includes the main and the secondary alternates.
     * @param variant
     * @return
     */
    public static List<AlternateCoordinate> buildAltList(Variant variant) {
        AlternateCoordinate mainAlternate = getMainAlternate(variant);
        List<AlternateCoordinate> alternates = new ArrayList<>();
        if (!mainAlternate.getType().equals(VariantType.NO_VARIATION)) {
            alternates.add(mainAlternate);
        }
        StudyEntry se = getStudy(variant);
        if(se.getSecondaryAlternates() != null){
            se.getSecondaryAlternates().forEach( alt -> alternates.add(copyAlt(variant, alt)));
        }
        return alternates;
    }

    private static AlternateCoordinate copyAlt(Variant var, AlternateCoordinate orig) {
        AlternateCoordinate copy = new AlternateCoordinate();
        copy.setChromosome(orig.getChromosome() == null ? var.getChromosome() : orig.getChromosome());
        copy.setStart(orig.getStart() == null ? var.getStart() : orig.getStart());
        copy.setEnd(orig.getEnd() == null ? var.getEnd() : orig.getEnd());
        copy.setReference(orig.getReference() == null ? var.getReference() : orig.getReference());
        copy.setAlternate(orig.getAlternate() == null ? var.getAlternate() : orig.getAlternate());
        copy.setType(orig.getType() == null ? var.getType() : orig.getType());
        return copy;
    }

    public static AlternateCoordinate getMainAlternate(Variant variant) {
        return new AlternateCoordinate(variant.getChromosome(), variant.getStart(), variant.getEnd(),
                variant.getReference(), variant.getAlternate(), variant.getType());
    }

    private void mergeFiles(Variant current, Variant other, StudyEntry currentStudy, StudyEntry otherStudy) {
        String call = other.getStart() + ":" + other.getReference() + ":" + other.getAlternate() + ":0";

        List<FileEntry> files = otherStudy.getFiles().stream().map(fileEntry -> FileEntry.newBuilder(fileEntry).build()).collect(Collectors.toList());
        if (!current.toString().equals(other.toString())) {
            for (FileEntry file : files) {
                if (file.getCall() == null || file.getCall().isEmpty()) {
                    file.setCall(call);
                }
            }
        }
        StudyEntry study = currentStudy;
        try {
            study.getFiles().addAll(files);
        } catch (UnsupportedOperationException e) {
            // If the files list was unmodifiable, clone the list and add.
            study.setFiles(new LinkedList<>(study.getFiles()));
            study.getFiles().addAll(files);
        }
    }

    private Map<String, String> sampleToAttribute(Variant var, String key) {
        StudyEntry se = getStudy(var);
        String value = se.getFiles().get(0).getAttributes().getOrDefault(key, "");
        if (StringUtils.isBlank(value)) {
            return Collections.emptyMap();
        }
        return se.getSamplesName().stream().collect(Collectors.toMap(e -> e, e -> value));
    }

    private Map<String, String> sampleToGt(Variant load) {
        return sampleToSampleData(load, GT_KEY);
    }

    private Map<String, String> sampleToSampleData(Variant var, String key){
        StudyEntry se = getStudy(var);
        return se.getSamplesName().stream()
                .filter(e -> StringUtils.isNotBlank(se.getSampleData(e, key))) // check for NULL or empty string
                .collect(Collectors.toMap(e -> e, e -> se.getSampleData(e, key)));
    }

    static StudyEntry getStudy(Variant variant) {
        return variant.getStudies().get(0);
    }

    /**
     * Ensures that all the samples contains the required format value.
     * @param studyEntry
     * @param formatValue
     * @param defaultValue
     */
    private void ensureFormat(StudyEntry studyEntry, String formatValue, String defaultValue) {
        if (!studyEntry.getFormat().contains(formatValue)) {
            studyEntry.addFormat(formatValue);
            if (studyEntry.getSamplesData() != null && !studyEntry.getSamplesData().isEmpty()) {
                for (String sampleName : studyEntry.getOrderedSamplesName()) {
                    studyEntry.addSampleData(sampleName, GENOTYPE_FILTER_KEY, defaultValue);
                }
            }
        }
    }

    static boolean onSameVariant (Variant a, Variant b){
        return a.onSameRegion(b)
                && StringUtils.equals(a.getReference(), b.getReference())
                && StringUtils.equals(a.getAlternate(), b.getAlternate());
    }

    public static boolean isSameVariant(Variant a, AlternateCoordinate b){
        return StringUtils.equals(a.getChromosome(), b.getChromosome())
                && a.getStart().equals(b.getStart())
                && a.getEnd().equals(b.getEnd())
                && StringUtils.equals(a.getReference(), b.getReference())
                && StringUtils.equals(a.getAlternate(), b.getAlternate());
    }

    public static boolean isSameVariant(AlternateCoordinate a, AlternateCoordinate b){
        return StringUtils.equals(a.getChromosome(), b.getChromosome())
                && a.getStart().equals(b.getStart())
                && a.getEnd().equals(b.getEnd())
                && StringUtils.equals(a.getReference(), b.getReference())
                && StringUtils.equals(a.getAlternate(), b.getAlternate());
    }
}
