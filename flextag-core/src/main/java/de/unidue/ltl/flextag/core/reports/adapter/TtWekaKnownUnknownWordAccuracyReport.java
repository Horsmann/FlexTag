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
package de.unidue.ltl.flextag.core.reports.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

public class TtWekaKnownUnknownWordAccuracyReport
    extends TtAbstractKnownUnknownWordAccuracyReport
{
    {
        TCMachineLearningAdapter adapter = WekaClassificationAdapter.getInstance();
        featureFile = adapter.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        predictionFile = adapter.getFrameworkFilename(AdapterNameEntries.predictionsFile);
    }

    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        super.execute();

        File train = buildFileLocation(store, trainContextId,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        List<String> trainVocab = extractVocab(train);

        File test = buildFileLocation(store, testContextId,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        List<String> testVocab = extractVocab(test);

        File p = buildFileLocation(store, predictionContextId, Constants.ID_OUTCOME_KEY);
        outputFolder = p.getParentFile();
        List<String> pred = readPredictions(p);
        evaluate(trainVocab, testVocab, pred);
        writeResults();
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
    protected String extractUnit(String next)
    {
        int lastIndexOf = next.lastIndexOf(",");
        next = next.substring(0, lastIndexOf);
        next = next.replaceAll("(,0|,1)+", "");

        next = next.replaceAll("([0-9]+_[0-9]+_[0-9]+_)", "");
        
        return next;
    }

    @Override
    protected List<String> extractVocab(File train)
        throws Exception
    {
        List<String> training = new ArrayList<String>();
        InputStreamReader streamReader = new InputStreamReader(
                new GZIPInputStream(new FileInputStream(train)), "UTF-8");
        BufferedReader br = new BufferedReader(streamReader);

        String next = null;
        while ((next = br.readLine()) != null) {

            if (next.startsWith("@")) {
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
}
