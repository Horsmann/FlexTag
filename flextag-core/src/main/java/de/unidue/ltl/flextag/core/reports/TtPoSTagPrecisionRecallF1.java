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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.report.TcTaskTypeUtil;

/**
 * Determines the accuracy for each word class
 */
public class TtPoSTagPrecisionRecallF1
    extends BatchReportBase
    implements Constants
{
    static String OUTPUT_FILE = "wordClassPerformance.txt";

    public void execute()
        throws Exception
    {
        StorageService storageService = getContext().getStorageService();
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (TcTaskTypeUtil.isMachineLearningAdapterTask(storageService, subcontext.getId())) {
                File locateKey = storageService.locateKey(subcontext.getId(),
                        Constants.ID_OUTCOME_KEY);

                String report = generateWordClassReport(locateKey);

                File targetFile = storageService.locateKey(subcontext.getId(), OUTPUT_FILE);
                FileUtils.writeStringToFile(targetFile, report);
            }
        }
    }

    private String generateWordClassReport(File locateKey)
        throws IOException
    {
        Map<String, WordClass> wcp = getWcPerformances(locateKey);

        StringBuilder sb = new StringBuilder();
        List<String> keySet = new ArrayList<String>(wcp.keySet());
        Collections.sort(keySet);

        sb.append(String.format("#%10s\t%5s\t%5s\t%5s\t%5s%n", "Class", "Occr", "Prec.", "Reca.",
                "F1"));
        for (String k : keySet) {
            WordClass wc = wcp.get(k);

            sb.append(String.format("%10s", k) + "\t" + String.format("%5d", wc.frequency) + "\t"
                    + String.format("%5.2f", wc.precision) + "\t" + String.format("%5.2f", wc.recall)
                    + "\t" + String.format("%5.2f", wc.f1) + "\n");
        }

        return sb.toString();

    }

    private Map<String, WordClass> getWcPerformances(File locateKey)
        throws IOException
    {
        Map<String, WordClass> wcp = new HashMap<>();

        List<String> lines = FileUtils.readLines(locateKey);
        Map<String, String> labels = getLabels(lines);

        List<String> predictions = new ArrayList<>();
        List<String> gold = new ArrayList<>();

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

            String p = labels.get(split[0]);
            String g = labels.get(split[1]);

            predictions.add(p);
            gold.add(g);
        }

        List<String> allGoldTags = new ArrayList<>(new HashSet<>(gold));
        Collections.sort(allGoldTags);

        for (String t : allGoldTags) {
            double tp = 0, fp = 0, tn = 0, fn = 0;
            long freq = 0;
            for (int i = 0; i < gold.size(); i++) {
                String g = gold.get(i);
                String p = predictions.get(i);

                if (!g.equals(t) && !p.equals(t)) {
                    tn++;
                }
                else if (!g.equals(t) && p.equals(t)) {
                    fp++;
                }
                else if (g.equals(t) && !p.equals(t)) {
                    fn++;
                }
                else if (g.equals(t) && p.equals(t)) {
                    tp++;
                }

                if (t.equals(g)) {
                    freq++;
                }
            }

            double recall = tp / (tp + fp);
            double precision = tp / (tp + fn);
            double f1 = (2 * (precision * recall)) / (precision + recall);

            wcp.put(t, new WordClass(precision, recall, f1, freq));
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


}
