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
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.ml.Experiment_ImplBase;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

import de.unidue.ltl.flextag.core.uima.TcPosTaggingWrapper;

public abstract class FlexTagSetUp
    implements Constants
{
    protected String experimentName = "FlexTag";
    protected String language;
    protected TcFeatureSet features;
    protected Experiment_ImplBase batch;

    protected CollectionReaderDescription reader;
    protected String dataFolder;
    protected String fileSuffix;

    protected String posMappingLocation;
    protected AnalysisEngineDescription[] userPreprocessing;

    protected Classifier classifier;
    protected Dimension<?> dimClassificationArgs;
    
    protected List<Class<? extends Report>> reports;
    boolean useCoarse = false;

    public FlexTagSetUp(CollectionReaderDescription reader)
    {
        this.reader = reader;
        this.features = DefaultFeatures.getDefaultFeatures();

        this.classifier = Classifier.CRFSUITE;
        this.dimClassificationArgs = setDefaultCrfClassifier();
    }

    @SuppressWarnings("unchecked")
    private Dimension<?> setDefaultCrfClassifier()
    {
        return Dimension.create(DIM_CLASSIFICATION_ARGS, asList(new String[] {
                CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR }));
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
    public void setFeatures(boolean useDefaultFeatures, TcFeature... features)
    {
        if (useDefaultFeatures) {
            for(TcFeature tf : features){
                this.features.add(tf);
            }
        }
        else {
            this.features = new TcFeatureSet(features);
        }
    }
    
    /**
     * Sets new feature names and their parameters, the provided features can be added additionally
     * to the default features by setting a boolean value to <b>true</b> otherwise the by default
     * used feature set is <i>overwritten</i> with the here provided features!
     *
     * @param useDefaultFeatures
     *        if the default feature set shall be used 
     * @param features
     *        a feature set
     */
    public void setFeatures(boolean useDefaultFeatures, TcFeatureSet featureSet)
    {
        if (useDefaultFeatures) {
            for(TcFeature tf : featureSet){
                this.features.add(tf);
            }
        }
        else {
            this.features = featureSet;
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

    /**
     * FlexTag expects an environmental variable DKPRO_HOME to be set. If you do not have or do not
     * want to set it permanently you can programmatically set this folder here.
     */
    public void setDKProHomeFolder(String home)
    {
        System.setProperty("DKPRO_HOME", home);
    }

    protected Dimension<TcFeatureSet> wrapFeatures()
    {
        return Dimension.create(DIM_FEATURE_SET, features);
    }

    protected ParameterSpace assembleParameterSpace(Map<String, Object> dimReaders,
            Dimension<TcFeatureSet> dimFeatureSets)
    {
            return new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                    Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                    Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE), dimFeatureSets,
                    dimClassificationArgs);
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
        case LIBLINEAR:
            return LiblinearAdapter.class;
        case LIBSVM:
            return LibsvmAdapter.class;            
        default:
            throw new IllegalArgumentException(
                    "Classifier [" + classifier.toString() + "] is unknown");
        }

    }

    @SuppressWarnings("unchecked")
    public void setClassifier(Classifier classifier,
            List<Object> dimClassificationArgs)
    {
        this.classifier = classifier;
        this.dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS, dimClassificationArgs);
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

        if (userPreprocessing != null) {
            preprocessing.addAll(Arrays.asList(userPreprocessing));
        }

        preprocessing.add(AnalysisEngineFactory.createEngineDescription(TcPosTaggingWrapper.class,
                TcPosTaggingWrapper.PARAM_USE_COARSE_GRAINED, useCoarse));

        return AnalysisEngineFactory
                .createEngineDescription(preprocessing.toArray(new AnalysisEngineDescription[0]));
    }

    public void setPreprocessing(AnalysisEngineDescription... createEngineDescription)
    {
        userPreprocessing = createEngineDescription;
    }
    
    public void useCoarse(boolean useCoarse){
        this.useCoarse = useCoarse;
    }
    
    public void addReport(Class<? extends Report> report){
        reports.add(report);
    }

    protected void addReports(List<Class<? extends Report>> r)
    {
        for(Class<? extends Report> c : reports){
            batch.addReport(c);
        }
    }

    public abstract void execute()
        throws Exception;
}
