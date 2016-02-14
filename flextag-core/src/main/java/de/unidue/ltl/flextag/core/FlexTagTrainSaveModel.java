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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.ml.ExperimentSaveModel;

import de.unidue.ltl.flextag.core.uima.TcPosTaggingWrapper;

public class FlexTagTrainSaveModel
    extends FlexTagSetUp
{
    private File modelOutputFolder;

    public FlexTagTrainSaveModel(String language, Class<?> reader, String dataFolder,
            String fileSuffix, File modelOutputFolder)
    {
        super(language, reader, dataFolder, fileSuffix);
        this.modelOutputFolder = modelOutputFolder;
    }

    @Override
    public void execute(boolean useCoarse)
        throws Exception
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();
        dimReaders = wrapReader(dimReaders, DIM_READER_TRAIN, reader, DIM_READER_TRAIN_PARAMS,
                dataFolder, fileSuffix, posMappingLocation);

        Dimension<List<String>> dimFeatureSets = wrapFeatures();
        Dimension<List<Object>> dimPipelineParameters = wrapFeatureParameters();

        ParameterSpace pSpace = assembleParameterSpace(dimReaders, dimFeatureSets,
                dimPipelineParameters);

        ExperimentSaveModel batch = new ExperimentSaveModel(experimentName, getClassifier(),
                modelOutputFolder);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        batch.setPreprocessing(AnalysisEngineFactory.createEngineDescription(
                TcPosTaggingWrapper.class, TcPosTaggingWrapper.PARAM_USE_COARSE_GRAINED, useCoarse));

        Lab.getInstance().run(batch);
    }
}
