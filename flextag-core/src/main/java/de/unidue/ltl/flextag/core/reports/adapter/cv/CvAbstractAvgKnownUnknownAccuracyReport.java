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
package de.unidue.ltl.flextag.core.reports.adapter.cv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.report.TcTaskTypeUtil;

/**
 * This report only prints sysout messages to point the user to the ouput directory which is used to
 * store all results
 */
public abstract class CvAbstractAvgKnownUnknownAccuracyReport
    extends BatchReportBase
    implements Constants
{
    public final static String UNKNOWN_WORDS = "unknown_words_acc.txt";
    public final static String KNOWN_WORDS = "known_words_acc.txt";
    private static final String ACCURACY = "AvgAcc";
    private static final String NUM_INSTANCES = "AvgNumInstances";

    List<Double> inVocab = new ArrayList<>();
    List<Double> outVocab = new ArrayList<>();

    List<Integer> inVocabCount = new ArrayList<>();
    List<Integer> outVocabCount = new ArrayList<>();

    protected String featureFile;
    
    String trainContext = null;
    String testContext = null;
    String mlAdapterContext = null;

    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (!TcTaskTypeUtil.isCrossValidationTask(store, subcontext.getId())) {
                continue;
            }
            File outputFolder = getContext().getStorageService().locateKey(subcontext.getId(), "");
            File attributes = store.locateKey(subcontext.getId(), "ATTRIBUTES.txt");
            List<List<String>> foldersOfSingleRuns = getFoldersOfSingleRuns(attributes);

            for (List<String> s : foldersOfSingleRuns) {
                for (String c : s) {
                    if (TcTaskTypeUtil.isMachineLearningAdapterTask(store, c)) {
                        mlAdapterContext = c;
                    }
                    if (TcTaskTypeUtil.isFeatureExtractionTrainTask(store, c)) {
                        trainContext = c;
                    }
                    if (TcTaskTypeUtil.isFeatureExtractionTestTask(store, c)) {
                        testContext = c;
                    }
                }

                processIteration();
            }
            writeResults(outputFolder, inVocab, outVocab, outVocabCount, outVocabCount);
        }
    }

    protected abstract void processIteration() throws Exception;

    private void writeResults(File outputFolder, List<Double> inVocab, List<Double> outVocab,
            List<Integer> inVocabCount, List<Integer> outVocabCount)
                throws Exception
    {
        double avgKnownAcc = averageDouble(inVocab);
        int avgKnownInstances = averageInt(inVocabCount);
        Map<String, String> known = new HashMap<>();
        known.put(ACCURACY, new Double(avgKnownAcc).toString());
        known.put(NUM_INSTANCES, new Integer(avgKnownInstances).toString());
        FileOutputStream fos = new FileOutputStream(new File(outputFolder, KNOWN_WORDS));
        PropertiesAdapter adapter = new PropertiesAdapter(known, "Results on known tokens");
        adapter.write(fos);
        fos.close();

        double avgUnknownAcc = averageDouble(outVocab);
        int avgUnknownInstances = averageInt(outVocabCount);
        Map<String, String> unknown = new HashMap<>();
        unknown.put(ACCURACY, new Double(avgUnknownAcc).toString());
        unknown.put(NUM_INSTANCES, new Integer(avgUnknownInstances).toString());
        fos = new FileOutputStream(new File(outputFolder, UNKNOWN_WORDS));
        adapter = new PropertiesAdapter(unknown, "Results on unknown tokens");
        adapter.write(fos);
        fos.close();
    }

    protected int averageInt(List<Integer> data)
    {
        double avg=0;
        for(Integer i : data){
            avg+=i;
        }
        
        return (int) (avg / data.size());
    }

    protected double averageDouble(List<Double> data)
    {
        double avg = 0;
        for (Double d : data) {
            avg += d;
        }

        return avg / data.size() * 100;
    }

    protected void evaluate(List<String> trainTokens, List<String> testTokens, List<String> pred,
            List<Double> inVocab, List<Double> outVocab, List<Integer> inVocabCount,
            List<Integer> outVocabCount)
    {
        double correct_in = 0;
        double incorrect_in = 0;
        double correct_out = 0;
        double incorrect_out = 0;

        Set<String> trainVocab = new HashSet<>(trainTokens);

        for (int i = 0; i < testTokens.size(); i++) {
            String testToken = testTokens.get(i);
            String string = pred.get(i);
            String[] split = splitPredictions(string);

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
        double invocabAccuracy = correct_in / (correct_in + incorrect_in);
        double outvocabAccuracy = correct_out / (correct_out + incorrect_out);

        int knownInstances = (int) (correct_in + incorrect_in);
        int unknownInstances = (int) (correct_out + incorrect_out);

        inVocab.add(invocabAccuracy);
        outVocab.add(outvocabAccuracy);
        inVocabCount.add(knownInstances);
        outVocabCount.add(unknownInstances);
    }

    protected String[] splitPredictions(String string)
    {
        return string.split(" ");
    }

    protected File buildFileLocation(StorageService store, String context, String fileName)
    {
        return store.locateKey(context, fileName);
    }

    protected abstract List<String> extractVocab(File train)
        throws Exception;

    private List<List<String>> getFoldersOfSingleRuns(File attributesTXT)
        throws Exception
    {
        List<String> readLines = FileUtils.readLines(attributesTXT);

        int idx = 0;
        for (String line : readLines) {
            if (line.startsWith("Subtask")) {
                break;
            }
            idx++;
        }
        String line = readLines.get(idx);
        int start = line.indexOf("[") + 1;
        int end = line.indexOf("]");
        String subTasks = line.substring(start, end);

        String[] tasks = subTasks.split(",");

        List<List<String>> results = new ArrayList<>();

        List<String> t = new ArrayList<>();
        for (String task : tasks) {
            task = task.trim();
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(), task)
                    || TcTaskTypeUtil.isFeatureExtractionTrainTask(getContext().getStorageService(),
                            task)
                    || TcTaskTypeUtil.isFeatureExtractionTestTask(getContext().getStorageService(),
                            task)) {
                t.add(task.trim());
            }
            if (t.size() == 3) {
                results.add(t);
                t = new ArrayList<>();
            }
        }

        return results;
    }

    protected List<String> readPredictions(File id2o)
        throws IOException
    {
        List<String> out = new ArrayList<>();
        Map<String, String> mapping = new HashMap<>();
        for (String l : FileUtils.readLines(id2o, "utf-8")) {
            if (l.startsWith("#labels")) {
                loadMapping(l, mapping);
            }
            if (l.startsWith("#")) {
                continue;
            }
            if(l.trim().isEmpty()){
                continue;
            }
            int lastIndexOf = l.lastIndexOf("=");
            String v = l.substring(lastIndexOf+1);
            String[] split2 = v.split(";");
            
            String g = mapping.get(split2[0]);
            String p = mapping.get(split2[1]);
            out.add(g + " " + p);
        }

        return out;
    }

    private void loadMapping(String l, Map<String, String> mapping)
    {
        l = l.replaceAll("#labels", "").trim();
        String[] split = l.split(" ");
        for (String s : split) {
            String[] split2 = s.split("=");
            mapping.put(split2[0], split2[1]);
        }
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
}
