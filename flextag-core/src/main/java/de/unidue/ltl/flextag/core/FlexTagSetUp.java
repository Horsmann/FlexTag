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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.weka.WekaClassificationAdapter;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.unidue.ltl.flextag.core.uima.TcPosTaggingWrapper;

public abstract class FlexTagSetUp
    implements Constants
{
    protected String experimentName = "FlexTag";
    protected String language;
    protected String[] featureNames;
    protected Object[] featureParameters;

    protected Class<?> reader;
    protected String dataFolder;
    protected String fileSuffix;

    protected String posMappingLocation;
    protected AnalysisEngineDescription[] userPreprocessing;

    protected FlexTagMachineLearningAdapter classifier;
    protected List<Dimension<?>> classificationArgs;

    public FlexTagSetUp(String language, Class<?> reader, String dataFolder, String fileSuffix)
    {
        this.language = language;

        this.reader = reader;
        this.dataFolder = dataFolder;
        this.fileSuffix = fileSuffix;

        this.featureNames = DefaultFeatures.getDefaultFeatures();
        this.featureParameters = DefaultFeatures.getDefaultFeatureParameter();

        this.classifier = FlexTagMachineLearningAdapter.CRFSUITE;
        this.classificationArgs = setCrfsuiteDefaultClassificationArgs();
    }

    @SuppressWarnings("unchecked")
    private List<Dimension<?>> setCrfsuiteDefaultClassificationArgs()
    {
        List<Dimension<?>> dims = new ArrayList<>();

        dims.add(Dimension.create(DIM_CLASSIFICATION_ARGS, asList(new String[] {
                CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR })));

        return dims;
    }

    /**
     * Sets new feature names and their parameters, the provided features can be added additionally
     * to the default features by setting a boolean value to <b>true</b> otherwise the by default
     * used feature set is <i>overwritten</i> with the here provided features!
     * 
     * @param features
     * @param featureParameters
     * @param useDefaultFeatures
     */
    public void setFeatures(String[] features, Object[] featureParameters,
            boolean useDefaultFeatures)
    {
        if (useDefaultFeatures) {
            addNewFeaturesToTheDefaultFeatures(features);
            addNewFeatureParameterToTheDefaultFeatures(featureParameters);
        }
        else {
            this.featureNames = features;
            this.featureParameters = featureParameters;
        }
    }

    /**
     * Sets an own experiment name which will make it easier to locate the result folders in the
     * DKPRO_HOME folder
     * 
     * @param experimentName
     */
    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    private void addNewFeatureParameterToTheDefaultFeatures(Object[] featureParameters)
    {
        List<Object> param = new ArrayList<Object>(
                Arrays.asList(DefaultFeatures.getDefaultFeatureParameter()));
        ArrayList<Object> newParameters = new ArrayList<Object>(Arrays.asList(featureParameters));
        param.addAll(newParameters);
        this.featureParameters = param.toArray(new Object[0]);
    }

    private void addNewFeaturesToTheDefaultFeatures(String[] features)
    {
        List<String> feat = new ArrayList<String>(
                Arrays.asList(DefaultFeatures.getDefaultFeatures()));
        List<String> newFeatures = new ArrayList<String>(Arrays.asList(features));
        feat.addAll(newFeatures);
        this.featureNames = feat.toArray(new String[0]);
    }

    /**
     * FlexTag expects an environmental variable DKPRO_HOME to be set. If you do not have or do not
     * want to set it permanently you can programmatically set this folder here.
     */
    public void setDKProHomeFolder(String home)
    {
        System.setProperty("DKPRO_HOME", home);
    }

    /**
     * Specifies a mapping of the part-of-speech tags found in the training data to the main word
     * classes as defined in the DKPro framework (e.g. noun, verb, adjective). This mapping enables
     * it to remove fine-grained word class distinctions without having to edit the training data.
     * If this mapping is not provided a default mapping based on the provided language is loaded
     * instead.
     */
    public void setTrainingPosMappingLocation(String posMappingLocation)
    {
        this.posMappingLocation = posMappingLocation;
    }

    protected Map<String, Object> wrapReader(Map<String, Object> dim, String dimReader,
            Class<?> reader, String dimReaderParm, String dataFolder, String fileSuffix,
            String posMappingLocation)
    {
        dim.put(dimReader, reader);

        List<Object> readerParam = Arrays.asList(ComponentParameters.PARAM_LANGUAGE, language,
                ComponentParameters.PARAM_SOURCE_LOCATION, dataFolder,
                ComponentParameters.PARAM_PATTERNS, fileSuffix);
        if (posMappingLocation != null) {
            readerParam.add(ComponentParameters.PARAM_POS_MAPPING_LOCATION);
            readerParam.add(posMappingLocation);
        }
        dim.put(dimReaderParm, readerParam);

        return dim;
    }

    @SuppressWarnings("unchecked")
    protected Dimension<List<Object>> wrapFeatureParameters()
    {
        return Dimension.create(DIM_PIPELINE_PARAMS, Arrays.asList(featureParameters));
    }

    @SuppressWarnings("unchecked")
    protected Dimension<List<String>> wrapFeatures()
    {
        return Dimension.create(DIM_FEATURE_SET, Arrays.asList(featureNames));
    }

    protected ParameterSpace assembleParameterSpace(Map<String, Object> dimReaders,
            Dimension<List<String>> dimFeatureSets, Dimension<List<Object>> dimPipelineParameters)
    {
        switch (classifier) {
        case SVMHMM: {
            Dimension<?> dimClassificationArgsC = classificationArgs.get(0);
            Dimension<?> dimClassificationArgsT = classificationArgs.get(1);
            Dimension<?> dimClassificationArgsE = classificationArgs.get(2);
            return new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                    Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                    Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE),
                    dimPipelineParameters, dimFeatureSets, dimClassificationArgsC,
                    dimClassificationArgsT, dimClassificationArgsE);
        }
        default: {
            Dimension<?> dimClassificationArgs = classificationArgs.get(0);
            return new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                    Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                    Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE),
                    dimPipelineParameters, dimFeatureSets, dimClassificationArgs);
        }

        }
    }

    protected Class<? extends TCMachineLearningAdapter> getClassifier()
    {
        switch (classifier) {
        case CRFSUITE:
            return CRFSuiteAdapter.class;
        case SVMHMM:
            return SVMHMMAdapter.class;
        case WEKA:
            return WekaClassificationAdapter.class;
        default:
            throw new IllegalArgumentException(
                    "Classifier [" + classifier.toString() + "] is unknown");
        }

    }

    public void setMachineLearningClassifier(FlexTagMachineLearningAdapter classifier,
            Dimension<Double> dimClassificationArgsC)
    {
        this.classifier = classifier;
    }

    public void setSvmHmmClassifier(Dimension<Double> dimClassificationArgsC,
            Dimension<Integer> dimClassificationArgsT, Dimension<Integer> dimClassificationArgsE)
    {
        classifier = FlexTagMachineLearningAdapter.SVMHMM;

        classificationArgs = new ArrayList<>();
        classificationArgs.add(dimClassificationArgsC);
        classificationArgs.add(dimClassificationArgsT);
        classificationArgs.add(dimClassificationArgsE);
    }

    @SuppressWarnings("unchecked")
    public void setCrfsuiteClassifier(String algorithm)
    {
        classifier = FlexTagMachineLearningAdapter.CRFSUITE;

        classificationArgs = new ArrayList<>();
        classificationArgs
                .add(Dimension.create(DIM_CLASSIFICATION_ARGS, asList(new String[] { algorithm })));
    }

    @SuppressWarnings("unchecked")
    public void setWekaClassifier(List<String> args)
    {
        classifier = FlexTagMachineLearningAdapter.WEKA;
        classificationArgs = new ArrayList<>();
        classificationArgs.add(Dimension.create(DIM_CLASSIFICATION_ARGS, args));

    }

    /**
     * @param useCoarse
     *            The POS tags are automatically mapped to their coarse value if this is set to true
     * @return Preprocessing pipeline
     * @throws ResourceInitializationException
     *             for erroneous configurations
     */
    protected AnalysisEngineDescription getPreprocessing(boolean useCoarse)
        throws ResourceInitializationException
    {
        List<AnalysisEngineDescription> preprocessing = new ArrayList<>();
        //
        preprocessing.add(AnalysisEngineFactory.createEngineDescription(TcPosTaggingWrapper.class,
                TcPosTaggingWrapper.PARAM_USE_COARSE_GRAINED, useCoarse));

        if (userPreprocessing != null) {
            preprocessing.addAll(Arrays.asList(userPreprocessing));
        }

        return AnalysisEngineFactory
                .createEngineDescription(preprocessing.toArray(new AnalysisEngineDescription[0]));
    }

    public void setPreprocessing(AnalysisEngineDescription... createEngineDescription)
    {
        userPreprocessing = createEngineDescription;
    }

    public abstract void execute(boolean useCoarse)
        throws Exception;
}
