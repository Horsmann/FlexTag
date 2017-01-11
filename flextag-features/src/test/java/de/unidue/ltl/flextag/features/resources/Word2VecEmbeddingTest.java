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
package de.unidue.ltl.flextag.features.resources;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.junit.Before;
import org.junit.Test;

public class Word2VecEmbeddingTest
{
    JCas jcas;
    TextClassificationTarget tokOne;
    TextClassificationTarget tokTwo;

    @Before
    public void setUp()
        throws UIMAException
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("The eskoasf");

        tokOne = new TextClassificationTarget(jcas, 0, 3);
        tokOne.addToIndexes();

        tokTwo = new TextClassificationTarget(jcas, 4, 11);
        tokTwo.addToIndexes();
    }

    @Test
    public void testFirstTokenCaseSensitiv()
        throws Exception
    {
        Word2VecEmbeddings featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                Word2VecEmbeddings.class, Word2VecEmbeddings.PARAM_RESOURCE_LOCATION,
                "src/test/resources/englishDummyEmbedding.txt.gz"));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokOne));
        assertEquals(50, extract.size());

        Feature firstVal = getFeature(extract, Word2VecEmbeddings.FEATURE_NAME + 1);
        assertEquals(0.028979, new Double((double) firstVal.getValue()).doubleValue(), 0.00001);

        Feature secondVal = getFeature(extract, Word2VecEmbeddings.FEATURE_NAME + 2);
        assertEquals(0.042376, new Double((double) secondVal.getValue()).doubleValue(), 0.00001);
    }

    @Test
    public void testFirstTokenLowerCased()
        throws Exception
    {
        Word2VecEmbeddings featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                Word2VecEmbeddings.class, Word2VecEmbeddings.PARAM_RESOURCE_LOCATION,
                "src/test/resources/englishDummyEmbedding.txt.gz",
                Word2VecEmbeddings.PARAM_USE_LOWER_CASE, true));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokOne));
        assertEquals(50, extract.size());

        Feature firstVal = getFeature(extract, Word2VecEmbeddings.FEATURE_NAME + 1);
        assertEquals(-0.126379, new Double((double) firstVal.getValue()).doubleValue(), 0.00001);

        Feature secondVal = getFeature(extract, Word2VecEmbeddings.FEATURE_NAME + 2);
        assertEquals(-0.156669, new Double((double) secondVal.getValue()).doubleValue(), 0.00001);
    }

    @Test
    public void testSecondTokenNotInEmbedding()
        throws Exception
    {
        Word2VecEmbeddings featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                Word2VecEmbeddings.class, Word2VecEmbeddings.PARAM_RESOURCE_LOCATION,
                "src/test/resources/englishDummyEmbedding.txt.gz"));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokTwo));
        assertEquals(50, extract.size());

        Feature firstVal = getFeature(extract, Word2VecEmbeddings.FEATURE_NAME + 1);
        assertEquals(0.0, new Double((double) firstVal.getValue()).doubleValue(), 0.1);

        Feature secondVal = getFeature(extract, Word2VecEmbeddings.FEATURE_NAME + 2);
        assertEquals(0.0, new Double((double) secondVal.getValue()).doubleValue(), 0.1);
    }

    private Feature getFeature(List<Feature> extract, String featureName)
    {
        for (Feature f : extract) {
            if (f.getName().equals(featureName)) {
                return f;
            }
        }

        return null;
    }

}
