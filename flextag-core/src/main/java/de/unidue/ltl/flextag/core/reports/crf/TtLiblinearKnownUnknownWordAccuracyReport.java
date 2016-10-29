package de.unidue.ltl.flextag.core.reports.crf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.report.TcTaskTypeUtil;

public class TtLiblinearKnownUnknownWordAccuracyReport
    extends BatchReportBase
    implements Constants
{
    
    {
        TCMachineLearningAdapter adapter = LiblinearAdapter.getInstance();
        featureFile = adapter.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        predictionFile = adapter.getFrameworkFilename(AdapterNameEntries.predictionsFile);
    }

    public static List<Double> in = new ArrayList<>();
    public static List<Double> out = new ArrayList<>();

    static String featureFile = null;
    static String predictionFile = null;
    public static String mappingFile = null;

    static File outputFolder = null;

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
                TEST_TASK_OUTPUT_KEY + "/" + "index2InstanceId.txt");
        List<String> trainTokens = extractVocab(train);

        File test = buildFileLocation(store, testContextId,
                TEST_TASK_OUTPUT_KEY + "/" + "index2InstanceId.txt");
        List<String> testTokens = extractVocab(test);
        
        File p = buildFileLocation(store, predictionContextId, Constants.ID_OUTCOME_KEY);
        List<String> pred = loadPredictions(p);
        outputFolder = p.getParentFile();
        
        evaluate(trainTokens, testTokens, pred, in, out);
        writeUnknownKnownFine();
    }

    private List<String> loadPredictions(File id2o) throws IOException
    {
        List<String> out = new ArrayList<>();
        Map<String,String> mapping = new HashMap<>();
        for(String l :FileUtils.readLines(id2o, "utf-8")){
            if(l.startsWith("#labels")){
                loadMapping(l, mapping);
            }
            if(l.startsWith("#")){
                continue;
            }
            String[] split = l.split("=");
            String[] split2 = split[1].split(";");
            String g = mapping.get(split2[0]);
            String p = mapping.get(split2[1]);
            out.add(g+" "+p);
        }
        
        return out;
    }

    private void loadMapping(String l, Map<String, String> mapping)
    {
        l = l.replaceAll("#labels", "").trim();
        String[] split = l.split(" ");
        for(String s : split){
            String[] split2 = s.split("=");
            mapping.put(split2[0], split2[1]);
        }
    }

    private List<String> extractVocab(File test) throws Exception
    {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(test), "UTF-8");
        BufferedReader br = new BufferedReader(streamReader);
        
        List<String> tokens = new ArrayList<>();
        
        String next = null;
        while ((next = br.readLine()) != null) {
            if (next.startsWith("#")) {
                continue;
            }
            if (next.isEmpty()) {
                continue;
            }
            
            String word = extractUnit(next);
            tokens.add(word);
        }
        
        br.close();
        return tokens;
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
                pre.add(r);
            }

            return pre;
        }

    private void evaluate(List<String> trainTokens, List<String> testTokens, List<String> pred,
            List<Double> in, List<Double> out)
    {
        double correct_in = 0;
        double incorrect_in = 0;
        double correct_out = 0;
        double incorrect_out = 0;
        
        Set<String> trainVocab = new HashSet<>(trainTokens);

        for (int i = 0; i < testTokens.size(); i++) {
            
            String string = pred.get(i);
            String[] split = string.split(" ");
            String testToken = testTokens.get(i);
            if (trainVocab.contains(testToken)) {
                if (split[0].equals(split[1])) {
                    correct_in++;
                }
                else {
                    incorrect_in++;
                }
            }
            else {
                if (split[0].equals(split[1])) {
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

    private File buildFileLocation(StorageService store, String context, String fileName)
    {
        return store.locateKey(context, fileName);
    }

    protected String extractUnit(String next)
    {
        int start = next.lastIndexOf("_");
        String word = next.substring(start + 1);
        return word;
    }

    public final static String UNKNOWN_WORDS_ACC = "unknown_words_acc.txt";
    public final static String KNOWN_WORDS_ACC = "known_words_acc.txt";

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

        FileUtils.write(new File(outputFolder, UNKNOWN_WORDS_ACC),
                String.format("%.1f", avg_out * 100));
        FileUtils.write(new File(outputFolder, KNOWN_WORDS_ACC),
                String.format("%.1f", avg_in * 100));
    }

}
