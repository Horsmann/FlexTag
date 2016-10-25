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

import org.apache.uima.collection.CollectionReader;
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
    private Class<? extends CollectionReader> testReader;
    private String testDataFolder;
    private String testFileSuffix;
    private String posTestingMappingLocation;

    public FlexTagTrainTest(String language, Class<? extends CollectionReader> reader, String trainDataFolder,
            String trainFileSuffix, String testDataFolder, String testFileSuffix)
    {
        super(language, reader, trainDataFolder, trainFileSuffix);

        this.testReader = reader;
        this.testDataFolder = testDataFolder;
        this.testFileSuffix = testFileSuffix;

        this.features = DefaultFeatures.getDefaultFeatures();
    }

    /**
     * If you test data use a different input format and you need another reader than the one
     * specified in the constructor you can set it here.
     * 
     * @param reader
     */
    public void setTestReader(Class<? extends CollectionReader> reader)
    {
        testReader = reader;
    }

    private Map<String, Object> wrapReaders() throws ResourceInitializationException
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders.put(DIM_READER_TRAIN, createReader(reader, dataFolder, fileSuffix, posMappingLocation));
        dimReaders.put(DIM_READER_TEST, createReader(testReader, testDataFolder, testFileSuffix, posTestingMappingLocation));

        return dimReaders;
    }

    /**
     * Specifies a mapping of the part-of-speech tags found in the test data to the main word
     * classes as defined in the DKPro framework (e.g. noun, verb, adjective). This mapping enables
     * it to remove fine-grained word class distinctions without having to edit the training data.
     * If this mapping is not provided a default mapping based on the provided language is loaded
     * instead.
     */
    public void setTestingPosMappingLocation(String posMappingLocation)
    {
        this.posTestingMappingLocation = posMappingLocation;
    }

    @Override
    public void execute(boolean useCoarse)
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
