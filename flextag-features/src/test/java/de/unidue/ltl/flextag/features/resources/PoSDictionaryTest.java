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

public class PoSDictionaryTest
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
        jcas.setDocumentText("on Gonna");

        tokOne = new TextClassificationTarget(jcas, 0, 2);
        tokOne.addToIndexes();

        tokTwo = new TextClassificationTarget(jcas, 3, 8);
        tokTwo.addToIndexes();
    }

    @Test
    public void testFirstToken()
        throws Exception
    {
        PoSDictionaryTagFeature featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                PoSDictionaryTagFeature.class, PoSDictionaryTagFeature.PARAM_RESOURCE_LOCATION,
                "src/test/resources/posDictDummy.txt", PoSDictionaryTagFeature.PARAM_LOAD_DICT_LOWER_CASE, true));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokOne));
        assertEquals(3, extract.size());

        Feature dict = getFeature(extract, PoSDictionaryTagFeature.FEATURE_NAME + "1");
        assertEquals("ADP", dict.getValue());

        dict = getFeature(extract, PoSDictionaryTagFeature.FEATURE_NAME + "2");
        assertEquals("PART", dict.getValue());
        
        dict = getFeature(extract, PoSDictionaryTagFeature.FEATURE_NAME + "3");
        assertEquals("X", dict.getValue());
    }

    @Test
    public void testSecondToken()
        throws Exception
    {
        PoSDictionaryTagFeature featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                PoSDictionaryTagFeature.class, PoSDictionaryTagFeature.PARAM_RESOURCE_LOCATION,
                "src/test/resources/posDictDummy.txt", PoSDictionaryTagFeature.PARAM_LOAD_DICT_LOWER_CASE, true));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokTwo));
        assertEquals(3, extract.size());

        Feature dict = getFeature(extract, PoSDictionaryTagFeature.FEATURE_NAME + "1");
        assertEquals("VERB", dict.getValue());
        
        dict = getFeature(extract, PoSDictionaryTagFeature.FEATURE_NAME + "2");
        assertEquals("X", dict.getValue());
        
        dict = getFeature(extract, PoSDictionaryTagFeature.FEATURE_NAME + "3");
        assertEquals(PoSDictionaryTagFeature.UNKNOWN, dict.getValue());
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
