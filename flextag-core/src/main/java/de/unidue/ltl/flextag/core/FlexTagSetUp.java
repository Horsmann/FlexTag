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
import org.dkpro.lab.Lab;
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
    protected TcFeatureSet features;
    protected Experiment_ImplBase batch;

    protected CollectionReaderDescription reader;

    protected String posMappingLocation;
    protected AnalysisEngineDescription[] userPreprocessing;

    protected Classifier classifier;
    protected Dimension<?> dimClassificationArgs;

    protected List<Class<? extends Report>> reports;
    boolean useCoarse = false;
    boolean didWire = false;

    public FlexTagSetUp(CollectionReaderDescription reader)
    {
        this.reader = reader;

        this.classifier = Classifier.CRFSUITE;
        this.dimClassificationArgs = setDefaultCrfConfiguration();
    }

    @SuppressWarnings("unchecked")
    private Dimension<?> setDefaultCrfConfiguration()
    {
        return Dimension.create(DIM_CLASSIFICATION_ARGS, asList(new String[] {
                CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR }));
    }

    public List<Class<? extends Report>> getReports()
    {
        return reports;
    }

    /**
     * Sets a new feature set
     *
     * @param featureSet
     *            a feature set
     */
    public void setFeatures(TcFeatureSet featureSet)
    {
        this.features = featureSet;
    }

    /**
     * Removes all reports
     */
    public void removeReports()
    {
        this.reports = new ArrayList<>();
    }

    /**
     * Sets a new feature set
     *
     * @param features
     *            an array of features which are added to the current features. If no features have
     *            been set yet the feature set is initialized.
     */
    public void setFeatures(TcFeature... features)
    {
        if (this.features == null) {
            this.features = new TcFeatureSet(features);
        }
        else {
            for (TcFeature f : features) {
                this.features.add(f);
            }
        }
    }

    /**
     * Sets an own experiment name which will make it easier to locate the result folders in the
     * DKPRO_HOME folder
     * 
     * @param experimentName
     *            the name of the experiment
     */
    public void setExperimentName(String experimentName)
    {
        this.experimentName = experimentName;
    }

    public String getExperimentName()
    {
        return this.experimentName;
    }

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

    public Class<? extends TCMachineLearningAdapter> getClassifier()
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

    public TcFeatureSet getFeatures()
    {
        return this.features;
    }

    @SuppressWarnings("unchecked")
    public void setClassifier(Classifier classifier, List<Object> dimClassificationArgs)
    {
        this.classifier = classifier;
        this.dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                dimClassificationArgs);
    }

    /**
     * Gets the current pre-processing set up
     * @return Pre-processing pipeline
     * @throws ResourceInitializationException
     *             for erroneous configurations
     */
    public AnalysisEngineDescription getPreprocessing()
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

    public void useCoarse(boolean useCoarse)
    {
        this.useCoarse = useCoarse;
    }

    public void addReport(Class<? extends Report> report)
    {
        reports.add(report);
    }

    public void addReports(List<Class<? extends Report>> r)
    {
        for (Class<? extends Report> c : r) {
            reports.add(c);
        }
    }

    protected void checkFeatureSpace()
    {
        if (features == null || features.isEmpty()) {
            throw new IllegalStateException("The feature space contains no feature extractors");
        }
    }

    public abstract void wire()
        throws Exception;
    
    Experiment_ImplBase getLabTask(){
        return batch;
    }
    
    public void execute() throws Exception{
        if(!didWire){
            wire();
        }
        Lab.getInstance().run(batch);
    }
}
