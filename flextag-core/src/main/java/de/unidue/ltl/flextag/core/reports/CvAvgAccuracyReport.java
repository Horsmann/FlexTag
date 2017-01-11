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
package de.unidue.ltl.flextag.core.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import org.dkpro.tc.evaluation.measures.label.Accuracy;
import org.dkpro.tc.ml.report.TcTaskTypeUtil;

/**
 * This report only prints sysout messages to point the user to the ouput directory which is used to
 * store all results
 */
public class CvAvgAccuracyReport
    extends BatchReportBase
    implements Constants
{

    private static final String OUTPUT_FILE = "avgAccuracy.txt";

    public void execute()
        throws Exception
    {
        StorageService storageService = getContext().getStorageService();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (TcTaskTypeUtil.isCrossValidationTask(storageService, subcontext.getId())) {
                File attributes = storageService.locateKey(subcontext.getId(), "ATTRIBUTES.txt");
                List<String> foldersOfSingleRuns = getFoldersOfSingleRuns(attributes);

                Double sumAcc = 0.0;
                for (String folder : foldersOfSingleRuns) {

                    File id2outcomeFile = storageService.locateKey(folder,
                            Constants.ID_OUTCOME_KEY);
                    String learningMode = getLearningMode(folder, storageService);
                    Id2Outcome o = new Id2Outcome(id2outcomeFile, learningMode);
                    EvaluatorBase createEvaluator = EvaluatorFactory.createEvaluator(o, true,
                            false);

                    Double accuracy = createEvaluator.calculateEvaluationMeasures()
                            .get(Accuracy.class.getSimpleName());
                    sumAcc += accuracy;
                }

                Double average = sumAcc / foldersOfSingleRuns.size();

                System.out.println("\nAverage accuracy over all folds: "
                        + String.format("%.1f percent", average * 100));

                File locateKey = storageService.locateKey(subcontext.getId(), OUTPUT_FILE);
                FileUtils.writeStringToFile(locateKey,
                        String.format("Avg-Accuracy: %.1f", average * 100), "utf-8");
            }
        }

    }

    private String getLearningMode(String contextId, StorageService storageService)
    {
        return storageService
                .retrieveBinary(contextId, Task.DISCRIMINATORS_KEY, new PropertiesAdapter())
                .getMap().get(ExtractFeaturesTask.class.getName() + "|" + DIM_LEARNING_MODE);
    }

    private List<String> getFoldersOfSingleRuns(File attributesTXT)
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

        List<String> results = new ArrayList<>();

        for (String task : tasks) {
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(getContext().getStorageService(),
                    task.trim())) {
                results.add(task.trim());
            }
        }

        return results;
    }
}
