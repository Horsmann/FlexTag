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
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.report.BatchTrainTestReport;

import de.unidue.ltl.flextag.core.reports.TtPoSTagPrecisionRecallF1;
import de.unidue.ltl.flextag.core.reports.TtAccuracyReport;

public class FlexTagTrainTest
    extends FlexTagSetUp
{
    private CollectionReaderDescription testReader;

    public FlexTagTrainTest(CollectionReaderDescription readerTrain,
            CollectionReaderDescription readerTest)
                throws TextClassificationException
    {
        super(readerTrain);
        this.testReader = readerTest;

        this.reports = initTrainTestReports();
    }

    private List<Class<? extends Report>> initTrainTestReports()
    {
        List<Class<? extends Report>> r = new ArrayList<>();
        r.add(BatchTrainTestReport.class);
        r.add(TtAccuracyReport.class);
        r.add(TtPoSTagPrecisionRecallF1.class);
        return r;
    }

    /**
     * If you test data use a different input format and you need another reader than the one
     * specified in the constructor you can set it here.
     * 
     * @param reader
     *        a collection reader description for reading the test data
     */
    public void setTestReader(CollectionReaderDescription reader)
    {
        testReader = reader;
    }

    private Map<String, Object> wrapReaders()
        throws ResourceInitializationException
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, super.reader);
        dimReaders.put(DIM_READER_TEST, testReader);

        return dimReaders;
    }

    @Override
    public void wire()
        throws Exception
    {
        checkFeatureSpace();
        
        Map<String, Object> dimReaders = wrapReaders();
        Dimension<TcFeatureSet> dimFeatureSets = wrapFeatures();

        ParameterSpace pSpace = assembleParameterSpace(dimReaders, dimFeatureSets);

        //configure
        batch = new ExperimentTrainTest(experimentName, getClassifier());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.setPreprocessing(getPreprocessing());
        
        for(Class<? extends Report> r : reports){
            batch.addReport(r);
        }
        
        didWire = true;
    }

}
