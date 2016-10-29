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
import org.dkpro.tc.ml.report.TcTaskTypeUtil;

public abstract class TtAbstractKnownUnknownWordAccuracyReport
    extends BatchReportBase
    implements Constants
{

    public final static String UNKNOWN_WORDS_FINE = "unknown_words_acc.txt";
    public final static String KNOWN_WORDS_FINE = "known_words_acc.txt";

    String featureFile = null;
    String predictionFile = null;
    File outputFolder = null;
    String trainContextId = null;
    String testContextId = null;
    String predictionContextId = null;

    double invocabAccuracy = 0;
    double outvocabAccuracy = 0;

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

    protected abstract List<String> readPredictions(File p)
        throws IOException;

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
    }

    protected abstract String[] splitPredictions(String string);

    protected File buildFileLocation(StorageService store, String context, String fileName)
    {
        return store.locateKey(context, fileName);
    }

    protected void writeResults()
        throws IOException
    {
        FileUtils.write(new File(outputFolder, UNKNOWN_WORDS_FINE),
                String.format("%.1f", outvocabAccuracy * 100));
        FileUtils.write(new File(outputFolder, KNOWN_WORDS_FINE),
                String.format("%.1f", invocabAccuracy * 100));
    }

}
