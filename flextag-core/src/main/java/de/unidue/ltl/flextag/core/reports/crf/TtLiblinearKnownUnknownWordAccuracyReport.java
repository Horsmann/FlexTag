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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;

public class TtLiblinearKnownUnknownWordAccuracyReport
    extends TtAbstractKnownUnknownWordAccuracyReport
{
    
    {
        TCMachineLearningAdapter adapter = LiblinearAdapter.getInstance();
        featureFile = adapter.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        predictionFile = adapter.getFrameworkFilename(AdapterNameEntries.predictionsFile);
    }

    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        super.execute();

        File train = buildFileLocation(store, trainContextId,
                TEST_TASK_OUTPUT_KEY + "/" + "index2InstanceId.txt");
        List<String> trainTokens = extractVocab(train);

        File test = buildFileLocation(store, testContextId,
                TEST_TASK_OUTPUT_KEY + "/" + "index2InstanceId.txt");
        List<String> testTokens = extractVocab(test);
        
        File p = buildFileLocation(store, predictionContextId, Constants.ID_OUTCOME_KEY);
        List<String> pred = loadPredictions(p);
        outputFolder = p.getParentFile();
        
        evaluate(trainTokens, testTokens, pred);
        writeResults();
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

    

    protected String extractUnit(String next)
    {
        int start = next.lastIndexOf("_");
        String word = next.substring(start + 1);
        return word;
    }
}
