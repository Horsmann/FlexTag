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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.crfsuite.task.CRFSuiteTestTask;
import org.dkpro.tc.ml.report.TcTaskTypeUtil;

/**
 * Determines the accuracy for each word class
 */
public class CvAvgPerWordClassReport
    extends BatchReportBase
    implements Constants
{
    static String OUTPUT_FILE = "wordClassPerformance.txt";

    Map<String, List<WordClass>> map = new HashMap<>();

    public void execute()
        throws Exception
    {
        StorageService storageService = getContext().getStorageService();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (TcTaskTypeUtil.isCrossValidationTask(storageService, subcontext.getId())) {
                File attributes = storageService.locateKey(subcontext.getId(), "ATTRIBUTES.txt");
                List<String> foldersOfSingleRuns = getFoldersOfSingleRuns(attributes);

                List<String> mla = new ArrayList<>();
                for (String context : foldersOfSingleRuns) {
                    if (TcTaskTypeUtil.isMachineLearningAdapterTask(storageService, context)) {
                        mla.add(context);
                    }
                }

                for (String mlaContext : mla) {
                    File locateKey = storageService.locateKey(mlaContext, Constants.ID_OUTCOME_KEY);
                    Map<String, WordClass> wcPerformances = getWcPerformances(locateKey);
                    for (String key : wcPerformances.keySet()) {
                        WordClass wc = wcPerformances.get(key);

                        List<WordClass> list = map.get(key);
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        list.add(wc);
                        map.put(key, list);
                    }
                }

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%20s\t%8s\t%5s\n", "PoS", "Occr.", "Acc"));

                List<String> keySet = new ArrayList<>(map.keySet());
                Collections.sort(keySet);
                for (String k : keySet) {
                    List<WordClass> list = map.get(k);

                    Double N = new Double(0);
                    double acc = 0;

                    for (WordClass wc : list) {
                        N += wc.getN();
                        acc += (wc.getCorrect() / wc.getN());
                    }
                    N /= list.size();
                    acc /= list.size();

                    sb.append(String.format("%20s\t%8d\t%5s\n", k, N.intValue(),
                            String.format("%3.1f", acc * 100)));
                }

                File locateKey = storageService.locateKey(subcontext.getId(), OUTPUT_FILE);
                FileUtils.writeStringToFile(locateKey, sb.toString(), "utf-8");
            }
        }

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

    private Map<String, WordClass> getWcPerformances(File locateKey)
        throws IOException
    {
        Map<String, WordClass> wcp = new HashMap<>();

        List<String> lines = FileUtils.readLines(locateKey);
        Map<String, String> labels = getLabels(lines);

        for (String l : lines) {
            if (l.startsWith("#")) {
                continue;
            }
            String[] entry = splitAtFirstEqualSignRightHandSide(l);

            String pg = entry[1];
            String[] split = pg.split(";");

            if (split.length < 2) {
                System.out.println("ERROR\t" + l);
                continue;
            }

            String prediction = labels.get(split[0]);
            String gold = labels.get(split[1]);

            WordClass wordClass = wcp.get(gold);
            if (wordClass == null) {
                wordClass = new WordClass();
            }
            if (gold.equals(prediction)) {
                wordClass.incrementCorrect();
            }
            else {
                wordClass.incrementIncorrect();
            }
            wcp.put(gold, wordClass);
        }
        return wcp;
    }

    private String[] splitAtFirstEqualSignRightHandSide(String l)
    {
        int equal = l.lastIndexOf("=");
        String lhs = l.substring(0, equal);
        String rhs = l.substring(equal + 1);

        return new String[] { lhs, rhs };
    }

    private Map<String, String> getLabels(List<String> lines)
    {
        for (String s : lines) {
            if (s.startsWith("#labels")) {
                return extractIdLabelMap(s);
            }
        }

        return null;
    }

    private Map<String, String> extractIdLabelMap(String s)
    {
        String wc = s.replaceAll("#labels ", "");

        String[] units = wc.split(" ");

        Map<String, String> id2label = new HashMap<>();

        for (String u : units) {
            String[] split = u.split("=");
            id2label.put(split[0], split[1]);
        }

        return id2label;
    }

    class WordClass
    {
        double correct = 0;
        double incorrect = 0;

        public Double getN()
        {
            return correct + incorrect;
        }

        public Double getCorrect()
        {
            return correct;
        }

        public void incrementCorrect()
        {
            correct++;
        }

        public void incrementIncorrect()
        {
            incorrect++;
        }
    }

}
