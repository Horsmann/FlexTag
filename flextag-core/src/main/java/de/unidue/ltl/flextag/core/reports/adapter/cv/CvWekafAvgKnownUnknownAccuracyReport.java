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
package de.unidue.ltl.flextag.core.reports.adapter.cv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

/**
 * This report only prints sysout messages to point the user to the ouput directory which is used to
 * store all results
 */
public class CvWekafAvgKnownUnknownAccuracyReport
    extends CvAbstractAvgKnownUnknownAccuracyReport
{
    private String featureFile;

    {
        TCMachineLearningAdapter adapter = WekaClassificationAdapter.getInstance();
        featureFile = adapter.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
    }

    @Override
    protected void processIteration()
        throws Exception
    {
        StorageService store = getContext().getStorageService();
        File train = buildFileLocation(store, trainContext,
                TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        List<String> trainVocab = extractVocab(train);

        File test = buildFileLocation(store, testContext, TEST_TASK_OUTPUT_KEY + "/" + featureFile);
        List<String> testVocab = extractVocab(test);

        File p = buildFileLocation(store, mlAdapterContext, Constants.ID_OUTCOME_KEY);
        List<String> pred = readPredictions(p);
        evaluate(trainVocab, testVocab, pred, inVocab, outVocab, inVocabCount, outVocabCount);
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
