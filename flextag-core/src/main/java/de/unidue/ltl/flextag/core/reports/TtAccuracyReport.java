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
package de.unidue.ltl.flextag.core.reports;

import java.io.File;
import java.util.Map;

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

public class TtAccuracyReport
    extends BatchReportBase
    implements Constants
{
    public void execute()
        throws Exception
    {

        /*
         * Iterate over all created folders in DKPRO_HOME that belong to the execution of this
         * experiment e.g. initialization, meta , training data feature extraction, test data
         * feature extraction
         */
        StorageService store = getContext().getStorageService();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(store, subcontext.getId())) {

                File id2outcomeFile = store.locateKey(subcontext.getId(), Constants.ID_OUTCOME_KEY);

                String learningMode = getLearningMode(subcontext.getId(), store);
                Id2Outcome o = new Id2Outcome(id2outcomeFile, learningMode);
                EvaluatorBase createEvaluator = EvaluatorFactory.createEvaluator(o, true, false);

                Map<String, Double> results = createEvaluator.calculateEvaluationMeasures();
                Double accuracy = results.get(Accuracy.class.getSimpleName());

                System.out.println(String.format("%n%nAccuracy: %.1f percent%n", accuracy * 100));
            }
        }
    }

    private String getLearningMode(String contextId, StorageService storageService)
    {
        return storageService
                .retrieveBinary(contextId, Task.DISCRIMINATORS_KEY, new PropertiesAdapter())
                .getMap().get(ExtractFeaturesTask.class.getName() + "|" + DIM_LEARNING_MODE);
    }
}
