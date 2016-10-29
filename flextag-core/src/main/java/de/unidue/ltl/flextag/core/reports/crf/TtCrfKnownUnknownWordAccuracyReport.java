package de.unidue.ltl.flextag.core.reports.crf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.report.TcTaskTypeUtil;

public class TtCrfKnownUnknownWordAccuracyReport
    extends BatchReportBase
    implements Constants
{
    public static List<Double> in = new ArrayList<>();
    public static List<Double> out = new ArrayList<>();

    static String featureFile = null;
    static String predictionFile = null;
    public static String mappingFile = null;

    static File outputFolder = null;

    
    {
        TCMachineLearningAdapter adapter = CRFSuiteAdapter.getInstance();
        featureFile = adapter.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        predictionFile = adapter.getFrameworkFilename(AdapterNameEntries.predictionsFile);
    }
    
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        String trainContextId = null;
        String testContextId = null;
        String predictionContextId = null;
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(store, subcontext.getId())) {
                predictionContextId = subcontext.getId();
            }
            if (TcTaskTypeUtil.isFeatureExtractionTestTask(store, subcontext.getId())) {
                testContextId = subcontext.getId();
            }
            if (TcTaskTypeUtil.isFeatureExtractionTrainTask(store, subcontext.getId())) {
                trainContextId = subcontext.getId();
            }
        }

        File train = buildFileLocation(store, trainContextId,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        Set<String> trainVocab = extractVocab(train);

        File test = buildFileLocation(store, testContextId,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        List<String> testVocab = readTest(test);

        File p = buildFileLocation(store, predictionContextId, predictionFile);
        outputFolder = p.getParentFile();
        List<String> pred = readPredictions(p);
        evaluate(trainVocab, testVocab, pred, in, out);
        writeUnknownKnownFine();
    }

    protected List<String> readPredictions(File p)
            throws IOException
        {
            List<String> pre = new ArrayList<>();
            List<String> readLines = FileUtils.readLines(p);
            int i = 0;
            for (String r : readLines) {
                if (r.isEmpty()) {
                    continue;
                }
                if (r.startsWith("#") && i == 0) {
                    i++;
                    continue;
                }
                pre.add(r.split("\t")[1]);
            }

            return pre;
        }

        protected Set<String> extractVocab(File train)
            throws Exception
        {
            Set<String> training = new HashSet<String>();
            InputStreamReader streamReader = new InputStreamReader(new FileInputStream(train), "UTF-8");
            BufferedReader br = new BufferedReader(streamReader);

            String next = null;
            while ((next = br.readLine()) != null) {

                if (next.isEmpty()) {
                    continue;
                }
                String word = extractUnit(next);
                training.add(word);
            }

            br.close();
            return training;
        }

    private void evaluate(Set<String> trainVocab, List<String> testVocab, List<String> pred,
            List<Double> in, List<Double> out)
    {
        double correct_in = 0;
        double incorrect_in = 0;
        double correct_out = 0;
        double incorrect_out = 0;

        for (int i = 0; i < testVocab.size(); i++) {
            String string = testVocab.get(i);

            String[] split = string.split("\t");

            if (trainVocab.contains(split[0])) {
                if (pred.get(i).equals(split[1])) {
                    correct_in++;
                }
                else {
                    incorrect_in++;
                }
            }
            else {
                if (pred.get(i).equals(split[1])) {
                    correct_out++;
                }
                else {
                    incorrect_out++;
                }
            }

        }
        in.add(correct_in / (correct_in + incorrect_in));
        out.add(correct_out / (correct_out + incorrect_out));
    }

    private List<String> readTest(File test)
        throws Exception
    {
        List<String> lines = new ArrayList<>();
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(test), "UTF-8");
        BufferedReader br = new BufferedReader(streamReader);

        String next = null;
        while ((next = br.readLine()) != null) {

            if (next.isEmpty()) {
                continue;
            }

            String word = extractUnit(next);
            String tag = extractTag(next);
            lines.add(word + "\t" + tag);
        }

        br.close();

        return lines;
    }

    private String extractTag(String next)
    {
        String[] split = next.split("\t");
        return split[0];
    }

    private File buildFileLocation(StorageService store, String context, String fileName)
    {
        return store.locateKey(context, fileName);
    }

    protected String extractUnit(String next)
    {
        int start = next.indexOf(ID_FEATURE_NAME);
        int end = next.indexOf("\t", start);
        if (end == -1) {
            end = next.length();
        }
        start = next.lastIndexOf("_", end);

        String word = next.substring(start + 1, end);

        return word;
    }

    public final static String UNKNOWN_WORDS_FINE = "unknown_words_acc.txt";
    public final static String KNOWN_WORDS_FINE = "known_words_acc.txt";

    public static void writeUnknownKnownFine()
        throws IOException
    {

        Double avg_in = 0.0;
        for (Double i : in) {
            avg_in += i;
        }
        avg_in /= in.size();

        Double avg_out = 0.0;
        for (Double i : out) {
            avg_out += i;
        }
        avg_out /= out.size();

        FileUtils.write(new File(outputFolder, UNKNOWN_WORDS_FINE),
                String.format("%.1f", avg_out * 100));
        FileUtils.write(new File(outputFolder, KNOWN_WORDS_FINE),
                String.format("%.1f", avg_in * 100));
    }

}
