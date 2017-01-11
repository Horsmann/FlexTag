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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;

public class TtSvmHmmKnownUnknownWordAccuracyReport
    extends TtAbstractKnownUnknownWordAccuracyReport
{

    {
        TCMachineLearningAdapter adapter = new SVMHMMAdapter();
        featureFile = adapter.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
        predictionFile = adapter.getFrameworkFilename(AdapterNameEntries.predictionsFile);
    }

    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        super.execute();

        File train = buildFileLocation(store, trainContextId,
                TEST_TASK_OUTPUT_KEY + "/" + "feature-vectors.txt");
        List<String> trainTokens = extractVocab(train);

        File test = buildFileLocation(store, testContextId,
                TEST_TASK_OUTPUT_KEY + "/" + "feature-vectors.txt");
        List<String> testTokens = extractVocab(test);

        File p = buildFileLocation(store, predictionContextId, Constants.ID_OUTCOME_KEY);
        List<String> pred = readPredictions(p);
        outputFolder = p.getParentFile();

        evaluate(trainTokens, testTokens, pred);
        writeResults();
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
            next = next.trim();
            String word = extractUnit(next);
            training.add(word);
        }

        br.close();
        return training;
    }

    protected String extractUnit(String next)
    {
        int lastIndexOf = next.lastIndexOf(" ");
        return next.substring(lastIndexOf + 1);
    }

}
