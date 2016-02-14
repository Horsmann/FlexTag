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
package de.uniduel.ltl.flextag.features;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.util.FeatureUtil;
import org.dkpro.tc.api.type.TextClassificationUnit;
import org.junit.Before;
import org.junit.Test;

import de.unidue.ltl.flextag.features.IsAllCapitalized;

public class IsAllCapitalizedTest
{
    JCas jcas;
    TextClassificationUnit tokOne;
    TextClassificationUnit tokTwo;

    @Before
    public void setUp()
        throws UIMAException
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("Hi PETER");

        tokOne = new TextClassificationUnit(jcas, 0, 2);
        tokOne.addToIndexes();

        tokTwo = new TextClassificationUnit(jcas, 3, 8);
        tokTwo.addToIndexes();
    }

    @Test
    public void testFirstToken()
        throws Exception
    {
        IsAllCapitalized featureExtractor = FeatureUtil.createResource(IsAllCapitalized.class,
                new Object[] {});
        List<Feature> features = new ArrayList<Feature>(featureExtractor.extract(jcas, tokOne));

        assertEquals(1, features.size());

        String featureName = features.get(0).getName();
        Object featureValue = features.get(0).getValue();
        assertEquals(IsAllCapitalized.FEATURE_NAME, featureName);
        assertEquals(0, featureValue);
    }

    @Test
    public void testSecondToken()
        throws Exception
    {
        IsAllCapitalized featureExtractor = FeatureUtil.createResource(IsAllCapitalized.class,
                new Object[] {});
        List<Feature> features = new ArrayList<Feature>(featureExtractor.extract(jcas, tokTwo));

        assertEquals(1, features.size());

        String featureName = features.get(0).getName();
        Object featureValue = features.get(0).getValue();
        assertEquals(IsAllCapitalized.FEATURE_NAME, featureName);
        assertEquals(1, featureValue);
    }
}
