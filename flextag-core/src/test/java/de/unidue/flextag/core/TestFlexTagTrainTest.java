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
package de.unidue.flextag.core;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.TcFeature;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.ml.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.liblinear.LiblinearAdapter;
import org.dkpro.tc.ml.libsvm.LibsvmAdapter;
import org.dkpro.tc.ml.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;
import org.junit.Test;
import org.mockito.Mockito;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.flextag.core.Classifier;
import de.unidue.ltl.flextag.core.FlexTagTrainTest;

public class TestFlexTagTrainTest
{
    @Test
    public void testSetUp() throws TextClassificationException{
        
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        CollectionReaderDescription test = Mockito.mock(CollectionReaderDescription.class);
        TcFeatureSet features = new TcFeatureSet();
        features.add(Mockito.mock(TcFeature.class));
        features.add(Mockito.mock(TcFeature.class));
        
        FlexTagTrainTest fts = new FlexTagTrainTest(train,test);
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
    public void testSetClassifiers() throws TextClassificationException{
        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
        CollectionReaderDescription test = Mockito.mock(CollectionReaderDescription.class);
        TcFeatureSet features = new TcFeatureSet();
        features.add(Mockito.mock(TcFeature.class));
        features.add(Mockito.mock(TcFeature.class));
        FlexTagTrainTest fts = new FlexTagTrainTest(train,test);
        
        fts.setClassifier(Classifier.LIBLINEAR, null);
        assertEquals(LiblinearAdapter.class, fts.getClassifier());
        
        fts.setClassifier(Classifier.LIBSVM, null);
        assertEquals(LibsvmAdapter.class, fts.getClassifier());
        
        fts.setClassifier(Classifier.WEKA, null);
        assertEquals(WekaClassificationAdapter.class, fts.getClassifier());
        
        fts.setClassifier(Classifier.SVMHMM, null);
        assertEquals(SVMHMMAdapter.class, fts.getClassifier());
    }
    
//    @Test
//    public void testPreprocessing() throws TextClassificationException, ResourceInitializationException {
//        CollectionReaderDescription train = Mockito.mock(CollectionReaderDescription.class);
//        CollectionReaderDescription test = Mockito.mock(CollectionReaderDescription.class);
//        TcFeatureSet features = new TcFeatureSet();
//        features.add(Mockito.mock(TcFeature.class));
//        features.add(Mockito.mock(TcFeature.class));
//        FlexTagTrainTest fts = new FlexTagTrainTest(train,test);
//        
//        fts.setPreprocessing(AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class));
//        AnalysisEngineDescription preprocessing = fts.getPreprocessing(false);
//    }

}
