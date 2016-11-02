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
package de.unidue.ltl.flextag.features.ngram;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ExternalResourceDescription;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureStore;
import org.dkpro.tc.api.features.Instance;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.io.JsonDataWriter;
import org.dkpro.tc.core.util.TaskUtils;
import org.dkpro.tc.fstore.simple.DenseFeatureStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LuceneNgramUnitTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static String EXTRACTOR_NAME = "133";

    @Test
    public void testLuceneMetaCollectorOutput()
        throws Exception
    {
        File luceneFolder = folder.newFolder();

        runMetaCollection(luceneFolder);
        File output = runFeatureExtractor(luceneFolder);
        evaluateExtractedFeatures(output);
    }

    private void evaluateExtractedFeatures(File output)
        throws Exception
    {
        Gson gson = new Gson();
        FeatureStore fs = gson.fromJson(
                FileUtils.readFileToString(new File(output, JsonDataWriter.JSON_FILE_NAME)),
                DenseFeatureStore.class);

        assertEquals(6, fs.getNumberOfInstances());
        Iterator<Instance> iterator = fs.getInstances().iterator();
        List<Feature> setFeats = new ArrayList<Feature>();
        while (iterator.hasNext()) {
            Instance next = iterator.next();
            List<Feature> arrayList = new ArrayList<Feature>(next.getFeatures());
            for(Feature f : arrayList){
                Double d = Double.valueOf(f.getValue().toString());
                if(d.doubleValue() > 0){
                    setFeats.add(f);
                }
            }
        }

        assertEquals(5, setFeats.size());
        
    }

    private File runFeatureExtractor(File luceneFolder)
        throws Exception
    {
        File outputPath = folder.newFolder();
        
        Object[] parameters = new Object[] {
                TokenContext.PARAM_TARGET_INDEX, "1",
                TokenContext.PARAM_UNIQUE_EXTRACTOR_NAME,
                EXTRACTOR_NAME, TokenContext.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
                LuceneUniGramMetaCollector.PARAM_TARGET_LOCATION, luceneFolder.toString()
                };

        ExternalResourceDescription featureExtractor = ExternalResourceFactory
                .createExternalResourceDescription(TokenContext.class, parameters);
        List<ExternalResourceDescription> fes = new ArrayList<>();
        fes.add(featureExtractor);

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en",
                TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/text/input.txt",
                TestReaderSingleLabel.PARAM_SUPPRESS_DOCUMENT_ANNOTATION, true);

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription unitAnno = AnalysisEngineFactory
                .createEngineDescription(EachTokenAsUnitAnnotator.class);

        AnalysisEngineDescription featExtractorConnector = TaskUtils.getFeatureExtractorConnector(
                outputPath.getAbsolutePath(), JsonDataWriter.class.getName(),
                Constants.LM_SINGLE_LABEL, Constants.FM_UNIT, DenseFeatureStore.class.getName(),
                false, false, false, new ArrayList<>(), false, fes);
        
        SimplePipeline.runPipeline(reader, segmenter, unitAnno, featExtractorConnector);

        return outputPath;
    }

    private void runMetaCollection(File luceneFolder)
        throws Exception
    {

        Object[] parameters = new Object[] { LuceneUniGramMetaCollector.PARAM_UNIQUE_EXTRACTOR_NAME,
                EXTRACTOR_NAME,
                TokenContext.PARAM_SOURCE_LOCATION, luceneFolder.toString(),
                LuceneUniGramMetaCollector.PARAM_TARGET_LOCATION, luceneFolder.toString()
                };

        List<Object> parameterList = new ArrayList<Object>(Arrays.asList(parameters));

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TestReaderSingleLabel.class, TestReaderSingleLabel.PARAM_LANGUAGE, "en",
                TestReaderSingleLabel.PARAM_SOURCE_LOCATION, "src/test/resources/text/input.txt");

        AnalysisEngineDescription segmenter = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription metaCollector = AnalysisEngineFactory
                .createEngineDescription(LuceneUniGramMetaCollector.class, parameterList.toArray());

        // run meta collector
        SimplePipeline.runPipeline(reader, segmenter, metaCollector);
    }

}
