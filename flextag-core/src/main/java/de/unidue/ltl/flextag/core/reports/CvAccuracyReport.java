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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.ReportConstants;
import org.dkpro.tc.crfsuite.task.CRFSuiteTestTask;
import org.dkpro.tc.ml.ExperimentCrossValidation;

/**
 * This report only prints sysout messages to point the user to the ouput directory which is used to
 * store all results
 */
public class CvAccuracyReport
    extends BatchReportBase
    implements Constants
{

    public void execute()
        throws Exception
    {
        
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().contains(ExperimentCrossValidation.class.getSimpleName())) {
                StorageService storageService = getContext().getStorageService();
                File properties = storageService.locateKey(subcontext.getId(), "PROPERTIES.txt");
                List<String> foldersOfSingleRuns = getFoldersOfSingleRuns(properties);
                
                Double sumAcc = 0.0;
                for(String folder : foldersOfSingleRuns){
                    File file = storageService.locateKey(folder, Constants.RESULTS_FILENAME);
                    Properties p = new Properties();
                    p.load(new FileInputStream(file));
                    String property = p.getProperty(ReportConstants.PCT_CORRECT);
                    Double runAcc = Double.valueOf(property);
                    sumAcc +=runAcc;
                }
                
                Double average = sumAcc / foldersOfSingleRuns.size();
                
                System.out.println("\nAverage accuracy over all folds: "
                        + String.format("%.1f percent\n", average*100));
                System.out.println("Many more results are provided in the DKPRO_HOME folder ["
                        + System.getProperty("DKPRO_HOME") + "]\nin the folder ["
                        + getContext().getId() + "]");
            }
        }
        
        
}
    
    private List<String> getFoldersOfSingleRuns(File propertiesTXT)
            throws Exception {
        List<String> readLines = FileUtils.readLines(propertiesTXT);

        int idx = 0;
        for (String line : readLines) {
            if (line.startsWith("#")) {
                idx++;
                continue;
            }
            break;
        }
        String line = readLines.get(idx);
        int start = line.indexOf("[");
        int end = line.indexOf("]");
        String subTasks = line.substring(start, end);

        String[] tasks = subTasks.split(",");

        List<String> results = new ArrayList<>();

        for (String task : tasks) {
            if (task.contains(CRFSuiteTestTask.class.getSimpleName())) {
                results.add(task.trim());
            }
        }

        return results;
    }
}
