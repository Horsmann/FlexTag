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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.InvalidXMLException;
import org.dkpro.lab.reporting.Report;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.ml.Experiment_ImplBase;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;
import org.junit.Test;
import org.mockito.Mockito;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.flextag.core.uima.TcPosTaggingWrapper;

public class TestFlexTagSetterGetter
{
    @Test
    public void testSetUp()
        throws TextClassificationException
    {

        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        CollectionReaderDescription test = Mockito.mock(CollectionReaderDescription.class);
        TcFeatureSet features = new TcFeatureSet();
        features.add(Mockito.mock(TcFeature.class));
        features.add(Mockito.mock(TcFeature.class));

        FlexTagTrainTest fts = new FlexTagTrainTest(train, test);
        fts.setExperimentName("Test");
        fts.setFeatures(features);

        assertEquals(CRFSuiteAdapter.class, fts.getClassifier());
        assertEquals(2, fts.getFeatures().size());
        assertEquals("Test", fts.getExperimentName());
        assertEquals(3, fts.getReports().size());
        fts.removeReports();
        assertEquals(0, fts.getReports().size());
    }

    @Test
    public void testSetClassifiers()
        throws TextClassificationException
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        CollectionReaderDescription test = Mockito.mock(CollectionReaderDescription.class);
        FlexTagTrainTest fts = new FlexTagTrainTest(train, test);

        fts.setClassifier(Classifier.LIBLINEAR, null);
        assertEquals(LiblinearAdapter.class, fts.getClassifier());

        fts.setClassifier(Classifier.LIBSVM, null);
        assertEquals(LibsvmAdapter.class, fts.getClassifier());

        fts.setClassifier(Classifier.WEKA, null);
        assertEquals(WekaClassificationAdapter.class, fts.getClassifier());

        fts.setClassifier(Classifier.SVMHMM, null);
        assertEquals(SVMHMMAdapter.class, fts.getClassifier());
    }

    @Test
    public void testPreprocessingTrainTest()
        throws Exception
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        CollectionReaderDescription test = Mockito.mock(CollectionReaderDescription.class);
        FlexTagTrainTest fts = new FlexTagTrainTest(train, test);

        fts.setPreprocessing(
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class));
        Set<String> aeNames = getAeInPreprocessingPipeline(fts.getPreprocessing());

        assertTrue(aeNames.contains(TcPosTaggingWrapper.class.getName()));
        assertTrue(aeNames.contains(BreakIteratorSegmenter.class.getName()));
    }

    @Test
    public void testPreprocessingCrossValidation()
        throws Exception
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        FlexTagCrossValidation fts = new FlexTagCrossValidation(train, 2);

        fts.setPreprocessing(
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class));
        Set<String> aeNames = getAeInPreprocessingPipeline(fts.getPreprocessing());

        assertTrue(aeNames.contains(TcPosTaggingWrapper.class.getName()));
        assertTrue(aeNames.contains(BreakIteratorSegmenter.class.getName()));
    }

    @Test
    public void testPreprocessingTrainStore()
        throws Exception
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        FlexTagTrainSaveModel fts = new FlexTagTrainSaveModel(train, new File("target/out"));

        fts.setPreprocessing(
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class));
        Set<String> aeNames = getAeInPreprocessingPipeline(fts.getPreprocessing());

        assertTrue(aeNames.contains(TcPosTaggingWrapper.class.getName()));
        assertTrue(aeNames.contains(BreakIteratorSegmenter.class.getName()));
    }

    private Set<String> getAeInPreprocessingPipeline(AnalysisEngineDescription preprocessing)
        throws InvalidXMLException
    {
        Set<String> names = new HashSet<>();

        Map<String, ResourceSpecifier> delegateAnalysisEngineSpecifiers = preprocessing
                .getDelegateAnalysisEngineSpecifiers();
        for (String k : delegateAnalysisEngineSpecifiers.keySet()) {
            AnalysisEngineDescription resourceSpecifier = (AnalysisEngineDescription) delegateAnalysisEngineSpecifiers
                    .get(k);
            String annotatorImplementationName = resourceSpecifier.getAnnotatorImplementationName();
            names.add(annotatorImplementationName);
        }
        return names;
    }

    @Test
    public void testSettingFeaturesSingle()
        throws Exception
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        FlexTagCrossValidation fts = new FlexTagCrossValidation(train, 2);

        TcFeatureSet set = new TcFeatureSet(Mockito.mock(TcFeature.class));
        fts.setFeatures(set);

        assertEquals(1, fts.getFeatures().size());

        fts.setFeatures(Mockito.mock(TcFeature.class));
        fts.setFeatures(Mockito.mock(TcFeature.class));

        assertEquals(3, fts.getFeatures().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionMissingFeaturesSaveModel()
        throws Exception
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        FlexTagTrainSaveModel fts = new FlexTagTrainSaveModel(train, new File("target/out"));
        fts.execute();
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionMissingFeaturesTrainTest()
        throws Exception
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        CollectionReaderDescription test = Mockito.mock(CollectionReaderDescription.class);
        FlexTagTrainTest fts = new FlexTagTrainTest(train, test);
        fts.execute();
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionMissingFeaturesCrossValidation()
        throws Exception
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        FlexTagCrossValidation fts = new FlexTagCrossValidation(train, 2);
        fts.execute();
    }

    @Test
    public void testReportCv()
        throws Exception
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        FlexTagCrossValidation fts = new FlexTagCrossValidation(train, 2);

        assertEquals(3, fts.getReports().size());
        fts.addReport(ReportBase.class);
        assertEquals(4, fts.getReports().size());
        fts.removeReports();
        assertEquals(0, fts.getReports().size());
        
        List<Class<? extends Report>> reps = new ArrayList<>();
        reps.add(ReportBase.class);
        reps.add(ReportBase.class);
        fts.addReports(reps);
        assertEquals(2, fts.getReports().size());
    }
    
    @Test
    public void testReportTrainTest()
        throws Exception
    {
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        CollectionReaderDescription test = Mockito.mock(CollectionReaderDescription.class);
        FlexTagTrainTest cv = new FlexTagTrainTest(train, test);

        assertEquals(3, cv.getReports().size());
        cv.addReport(ReportBase.class);
        assertEquals(4, cv.getReports().size());
        cv.removeReports();
        assertEquals(0, cv.getReports().size());
    }
    
    @Test
    public void testWiringTrainTest() throws Exception{
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        CollectionReaderDescription test = Mockito.mock(CollectionReaderDescription.class);
        FlexTagTrainTest tt = new FlexTagTrainTest(train, test);
        tt.setFeatures(Mockito.mock(TcFeature.class));
        tt.setFeatures(Mockito.mock(TcFeature.class));
        tt.wire();
        
        Experiment_ImplBase labTask = tt.getLabTask();
        ParameterSpace parameterSpace = labTask.getParameterSpace();
        Iterator<Map<String, Object>> iterator = parameterSpace.iterator();
        
        Set<String> expectedDimensions = new HashSet<>();
        expectedDimensions.add("readerTrain");
        expectedDimensions.add("readerTest");
        expectedDimensions.add("featureSet");
        
        while(iterator.hasNext()){
            Map<String, Object> next = iterator.next();
            for(String k : next.keySet()){
                if(k.equals("readerTrain")){
                    expectedDimensions.remove("readerTrain");
                }
                if(k.equals("readerTest")){
                    expectedDimensions.remove("readerTest");
                }
                if(k.equals("featureSet")){
                    expectedDimensions.remove("featureSet");
                    TcFeatureSet object = (TcFeatureSet) next.get(k);
                    assertEquals(2, object.size());
                }
            }
        }
        
        assertTrue(expectedDimensions.isEmpty());
        assertEquals(3, labTask.getReports().size());
    }
    
    @Test
    public void testWiringCrossValidation() throws Exception{
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        FlexTagCrossValidation cv = new FlexTagCrossValidation(train, 2);
        cv.setFeatures(Mockito.mock(TcFeature.class));
        cv.setFeatures(Mockito.mock(TcFeature.class));
        cv.wire();
        
        Experiment_ImplBase labTask = cv.getLabTask();
        ParameterSpace parameterSpace = labTask.getParameterSpace();
        Iterator<Map<String, Object>> iterator = parameterSpace.iterator();
        
        Set<String> expectedDimensions = new HashSet<>();
        expectedDimensions.add("readerTrain");
        expectedDimensions.add("featureSet");
        
        while(iterator.hasNext()){
            Map<String, Object> next = iterator.next();
            for(String k : next.keySet()){
                if(k.equals("readerTrain")){
                    expectedDimensions.remove("readerTrain");
                }
                if(k.equals("featureSet")){
                    expectedDimensions.remove("featureSet");
                    TcFeatureSet object = (TcFeatureSet) next.get(k);
                    assertEquals(2, object.size());
                }
            }
        }
        
        assertTrue(expectedDimensions.isEmpty());
        assertEquals(3, labTask.getReports().size());
    }
    
    @Test
    public void testWiringSaveModel() throws Exception{
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        FlexTagTrainSaveModel ts = new FlexTagTrainSaveModel(train, new File("target"));
        ts.setFeatures(Mockito.mock(TcFeature.class));
        ts.setFeatures(Mockito.mock(TcFeature.class));
        ts.wire();
        
        Experiment_ImplBase labTask = ts.getLabTask();
        ParameterSpace parameterSpace = labTask.getParameterSpace();
        Iterator<Map<String, Object>> iterator = parameterSpace.iterator();
        
        Set<String> expectedDimensions = new HashSet<>();
        expectedDimensions.add("readerTrain");
        expectedDimensions.add("featureSet");
        
        while(iterator.hasNext()){
            Map<String, Object> next = iterator.next();
            for(String k : next.keySet()){
                if(k.equals("readerTrain")){
                    expectedDimensions.remove("readerTrain");
                }
                if(k.equals("featureSet")){
                    expectedDimensions.remove("featureSet");
                    TcFeatureSet object = (TcFeatureSet) next.get(k);
                    assertEquals(2, object.size());
                }
            }
        }
        
        assertTrue(expectedDimensions.isEmpty());
        assertEquals(0, labTask.getReports().size());
    }
}
