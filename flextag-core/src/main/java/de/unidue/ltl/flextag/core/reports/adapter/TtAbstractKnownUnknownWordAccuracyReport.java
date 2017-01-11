/*******************************************************************************
 * Copyright 2017
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
package de.unidue.ltl.flextag.core.reports.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.report.TcTaskTypeUtil;

public abstract class TtAbstractKnownUnknownWordAccuracyReport
    extends BatchReportBase
    implements Constants
{

    public final static String UNKNOWN_WORDS = "unknown_words_acc.txt";
    public final static String KNOWN_WORDS = "known_words_acc.txt";
    private static final String ACCURACY = "ACC";
    private static final String NUM_INSTANCES = "NumInstances";

    protected String featureFile = null;
    protected String predictionFile = null;
    protected File outputFolder = null;
    protected String trainContextId = null;
    protected String testContextId = null;
    protected String predictionContextId = null;

    double invocabAccuracy = 0;
    double outvocabAccuracy = 0;
    
    int unknownInstances;
    int knownInstances;

    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

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
    }

    protected List<String> readPredictions(File id2o) throws IOException
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

    protected List<String> extractVocab(File train)
        throws Exception
    {
        List<String> training = new ArrayList<String>();
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(train), "UTF-8");
        BufferedReader br = new BufferedReader(streamReader);

        String next = null;
        while ((next = br.readLine()) != null) {

            if (next.startsWith("#")) {
                continue;
            }
            if (next.isEmpty()) {
                continue;
            }
            
            String word = extractUnit(next);
            training.add(word);
        }

        br.close();
        return training;
    }
    
    protected abstract String extractUnit(String next);

    protected void evaluate(List<String> trainTokens, List<String> testTokens, List<String> pred)
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
        invocabAccuracy = correct_in / (correct_in + incorrect_in);
        outvocabAccuracy = correct_out / (correct_out + incorrect_out);
        
        knownInstances = (int) (correct_in+incorrect_in);
        unknownInstances = (int) (correct_out + incorrect_out);
    }
    
    protected String[] splitPredictions(String string)
    {
        return string.split(" ");
    }

    protected File buildFileLocation(StorageService store, String context, String fileName)
    {
        return store.locateKey(context, fileName);
    }

    protected void writeResults()
        throws Exception
    {
        Map<String,String> known = new HashMap<>();
        known.put(ACCURACY, new Double(invocabAccuracy * 100).toString());
        known.put(NUM_INSTANCES, Integer.toString(knownInstances));
        FileOutputStream fos = new FileOutputStream(new File(outputFolder, KNOWN_WORDS));
        PropertiesAdapter adapter = new PropertiesAdapter(known, "Results on known tokens");
        adapter.write(fos);
        fos.close();

        Map<String,String> unknown = new HashMap<>();
        unknown.put(ACCURACY, new Double(outvocabAccuracy * 100).toString());
        unknown.put(NUM_INSTANCES, Integer.toString(unknownInstances));
        fos = new FileOutputStream(new File(outputFolder, UNKNOWN_WORDS));
        adapter = new PropertiesAdapter(unknown, "Results on unkown tokens");
        adapter.write(fos);
        fos.close();
    }

}
