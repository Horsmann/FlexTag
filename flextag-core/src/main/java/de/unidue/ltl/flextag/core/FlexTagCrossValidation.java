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
package de.unidue.ltl.flextag.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.report.BatchCrossValidationReport;

import de.unidue.ltl.flextag.core.reports.CvAvgAccuracyReport;
import de.unidue.ltl.flextag.core.reports.CvAvgPerWordClassReport;

public class FlexTagCrossValidation
    extends FlexTagSetUp
{

    private List<Class<? extends Report>> innerReports = new ArrayList<>();
    private int numberOfFolds;

    public FlexTagCrossValidation(CollectionReaderDescription reader, int numberOfFolds)
        throws TextClassificationException
    {
        super(reader);
        this.numberOfFolds = numberOfFolds;
        this.reports = initCrossValidationReports();
    }

    private List<Class<? extends Report>> initCrossValidationReports()
    {
        List<Class<? extends Report>> r = new ArrayList<>();
        r.add(BatchCrossValidationReport.class);
        r.add(CvAvgAccuracyReport.class);
        r.add(CvAvgPerWordClassReport.class);
        return r;
    }

    public void addInnerReports(Class<? extends Report> innerReport)
    {
        innerReports.add(innerReport);
    }

    public void removeReports()
    {
        reports = new ArrayList<>();
        innerReports = new ArrayList<>();
    }

    @Override
    public void wire()
        throws Exception
    {
        checkFeatureSpace();
        Map<String, Object> dimReaders = new HashMap<>();

        dimReaders.put(DIM_READER_TRAIN, reader);
        Dimension<TcFeatureSet> dimFeatureSets = wrapFeatures();
        ParameterSpace pSpace = assembleParameterSpace(dimReaders, dimFeatureSets);

        batch = new ExperimentCrossValidation(experimentName, getClassifier(), numberOfFolds);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.setPreprocessing(getPreprocessing());

        for(Class<? extends Report> r : reports){
            batch.addReport(r);
        }
        for (Class<? extends Report> r : innerReports) {
            batch.addInnerReport(r);
        }

        didWire = true;
    }

}
