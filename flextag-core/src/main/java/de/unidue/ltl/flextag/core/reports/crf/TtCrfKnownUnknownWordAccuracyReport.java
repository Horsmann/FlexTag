/*******************************************************************************
 * Copyright 2016
 * Language Technology Lab
 * University of Duisburg-Essen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
        List<String> trainVocab = extractVocab(train);

        File test = buildFileLocation(store, testContextId,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        List<String> testVocab = extractVocab(test);

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
                pre.add(r);
            }

            return pre;
        }

        protected List<String> extractVocab(File train)
            throws Exception
        {
            List<String> training = new ArrayList<String>();
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

    private void evaluate(List<String> trainTokens, List<String> testTokens, List<String> pred,
            List<Double> in, List<Double> out)
    {
        double correct_in = 0;
        double incorrect_in = 0;
        double correct_out = 0;
        double incorrect_out = 0;
        
        Set<String> trainVocab = new HashSet<>(trainTokens);

        for (int i = 0; i < testTokens.size(); i++) {
            String testToken = testTokens.get(i);
            String string = pred.get(i);
            String[] split = string.split("\t");

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
