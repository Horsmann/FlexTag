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
package de.unidue.ltl.flextag.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.ml.ExperimentTrainTest;

import de.unidue.ltl.flextag.core.reports.TtAccuracyPerWordClassReport;
import de.unidue.ltl.flextag.core.reports.TtAccuracyReport;
import de.unidue.ltl.flextag.core.uima.TcPosTaggingWrapper;

public class FlexTagTrainTest
    extends FlexTagSetUp
{
    private CollectionReaderDescription testReader;

    public FlexTagTrainTest(CollectionReaderDescription readerTrain, CollectionReaderDescription readerTest)
    {
        super(readerTrain);

        this.testReader = readerTest;

        this.features = DefaultFeatures.getDefaultFeatures();
    }

    /**
     * If you test data use a different input format and you need another reader than the one
     * specified in the constructor you can set it here.
     * 
     * @param reader
     */
    public void setTestReader(CollectionReaderDescription reader)
    {
        testReader = reader;
    }

    private Map<String, Object> wrapReaders() throws ResourceInitializationException
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, super.reader);
        dimReaders.put(DIM_READER_TEST,  testReader);

        return dimReaders;
    }

    @Override
    public void execute()
        throws Exception
    {

        Map<String, Object> dimReaders = wrapReaders();
        Dimension<TcFeatureSet> dimFeatureSets = wrapFeatures();

        ParameterSpace pSpace = assembleParameterSpace(dimReaders, dimFeatureSets);
        
        ExperimentTrainTest batch = new ExperimentTrainTest(experimentName, getClassifier());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.addReport(TtAccuracyReport.class);
        batch.addReport(TtAccuracyPerWordClassReport.class);
        batch.setPreprocessing(AnalysisEngineFactory.createEngineDescription(
                TcPosTaggingWrapper.class, TcPosTaggingWrapper.PARAM_USE_COARSE_GRAINED, useCoarse));

        Lab.getInstance().run(batch);

    }

}
