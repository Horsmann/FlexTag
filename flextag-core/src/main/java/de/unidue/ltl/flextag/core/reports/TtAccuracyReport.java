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
import java.io.FileInputStream;
import java.util.Properties;

import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.util.ReportConstants;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

public class TtAccuracyReport
    extends BatchReportBase
    implements Constants
{
    public void execute()
        throws Exception
    {

        String evaluationFolderName = getContext().getId();

        /*
         * Iterate over all created folders in DKPRO_HOME that belong to the execution of this
         * experiment e.g. initialization, meta , training data feature extraction, test data
         * feature extraction
         */
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().contains(CRFSuiteTestTask.class.getName())) {
                StorageService storageService = getContext().getStorageService();

                String crfFolderName = subcontext.getId();

                File storageFolder = storageService.locateKey(subcontext.getId(), "");
                File evaluation = new File(storageFolder,
                        new CRFSuiteAdapter()
                                .getFrameworkFilename(AdapterNameEntries.evaluationFile));

                
                Properties p = new Properties();
                p.load(new FileInputStream(evaluation));
                String property = p.getProperty(ReportConstants.PCT_CORRECT);
                String value = property.replaceAll(",", ".");
                double accuracy = Double.valueOf(value) * 100;

                System.out.println("\n\nAccuracy: " + String.format("%.1f percent\n", accuracy));
                System.out
                        .println("Further results are provided in the DKPRO_HOME folder in the folder starting with ["
                                + CRFSuiteTestTask.class.getSimpleName()
                                + "] and [Evaluation]\n\n"
                                + "DKPRO_HOME="
                                + System.getProperty("DKPRO_HOME")
                                + "\n"
                                + "i.e.\t* ["
                                + crfFolderName
                                + "]\n\t* ["
                                + evaluationFolderName
                                + "]");
            }
        }
    }
}
