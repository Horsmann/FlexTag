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

public class BrownClusterTest
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
        jcas.setDocumentText("@I Gonna");

        tokOne = new TextClassificationTarget(jcas, 0, 2);
        tokOne.addToIndexes();

        tokTwo = new TextClassificationTarget(jcas, 3, 8);
        tokTwo.addToIndexes();
    }

    @Test
    public void testFirstToken()
        throws Exception
    {
        BrownCluster featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                BrownCluster.class, BrownCluster.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                "src/test/resources/dummyBrownCluster.txt.gz",
                BrownCluster.PARAM_BROWN_CLUSTER_NORMALIZATION, true, BrownCluster.PARAM_LOWER_CASE,
                true));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokOne));
        assertEquals(8, extract.size());

        Feature brown16 = getFeature(extract, BrownCluster.FEATURE_NAME_16);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown16.getValue());

        Feature brown14 = getFeature(extract, BrownCluster.FEATURE_NAME_14);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown14.getValue());

        Feature brown12 = getFeature(extract, BrownCluster.FEATURE_NAME_12);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown12.getValue());

        Feature brown10 = getFeature(extract, BrownCluster.FEATURE_NAME_10);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown10.getValue());

        Feature brown08 = getFeature(extract, BrownCluster.FEATURE_NAME_08);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown08.getValue());

        Feature brown06 = getFeature(extract, BrownCluster.FEATURE_NAME_06);
        assertEquals("111110", brown06.getValue());

        Feature brown04 = getFeature(extract, BrownCluster.FEATURE_NAME_04);
        assertEquals("1111", brown04.getValue());

        Feature brown02 = getFeature(extract, BrownCluster.FEATURE_NAME_02);
        assertEquals("11", brown02.getValue());

    }

    @Test
    public void testFirstTokenNoCompressedCluster()
        throws Exception
    {
        BrownCluster featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                BrownCluster.class, BrownCluster.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                "src/test/resources/dummyBrownCluster.txt.gz",
                BrownCluster.PARAM_BROWN_CLUSTER_NORMALIZATION, true, BrownCluster.PARAM_LOWER_CASE,
                true));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokOne));
        assertEquals(8, extract.size());

        Feature brown16 = getFeature(extract, BrownCluster.FEATURE_NAME_16);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown16.getValue());

        Feature brown14 = getFeature(extract, BrownCluster.FEATURE_NAME_14);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown14.getValue());

        Feature brown12 = getFeature(extract, BrownCluster.FEATURE_NAME_12);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown12.getValue());

        Feature brown10 = getFeature(extract, BrownCluster.FEATURE_NAME_10);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown10.getValue());

        Feature brown08 = getFeature(extract, BrownCluster.FEATURE_NAME_08);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown08.getValue());

        Feature brown06 = getFeature(extract, BrownCluster.FEATURE_NAME_06);
        assertEquals("111110", brown06.getValue());

        Feature brown04 = getFeature(extract, BrownCluster.FEATURE_NAME_04);
        assertEquals("1111", brown04.getValue());

        Feature brown02 = getFeature(extract, BrownCluster.FEATURE_NAME_02);
        assertEquals("11", brown02.getValue());

    }

    @Test
    public void testSecondToken()
        throws Exception
    {
        BrownCluster featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                BrownCluster.class, BrownCluster.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                "src/test/resources/dummyBrownCluster.txt.gz",
                BrownCluster.PARAM_BROWN_CLUSTER_NORMALIZATION, true, BrownCluster.PARAM_LOWER_CASE,
                true));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokTwo));
        assertEquals(8, extract.size());

        Feature brown16 = getFeature(extract, BrownCluster.FEATURE_NAME_16);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown16.getValue());

        Feature brown14 = getFeature(extract, BrownCluster.FEATURE_NAME_14);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown14.getValue());

        Feature brown12 = getFeature(extract, BrownCluster.FEATURE_NAME_12);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown12.getValue());

        Feature brown10 = getFeature(extract, BrownCluster.FEATURE_NAME_10);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown10.getValue());

        Feature brown08 = getFeature(extract, BrownCluster.FEATURE_NAME_08);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown08.getValue());

        Feature brown06 = getFeature(extract, BrownCluster.FEATURE_NAME_06);
        assertEquals("001100", brown06.getValue());

        Feature brown04 = getFeature(extract, BrownCluster.FEATURE_NAME_04);
        assertEquals("0011", brown04.getValue());

        Feature brown02 = getFeature(extract, BrownCluster.FEATURE_NAME_02);
        assertEquals("00", brown02.getValue());

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

    @Test
    public void testNoNormalization()
        throws Exception
    {
        BrownCluster featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                BrownCluster.class, BrownCluster.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                "src/test/resources/dummyBrownCluster.txt.gz",
                BrownCluster.PARAM_BROWN_CLUSTER_NORMALIZATION, false, BrownCluster.PARAM_LOWER_CASE,
                true));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokOne));
        assertEquals(8, extract.size());

        Feature brown16 = getFeature(extract, BrownCluster.FEATURE_NAME_16);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown16.getValue());

        Feature brown14 = getFeature(extract, BrownCluster.FEATURE_NAME_14);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown14.getValue());

        Feature brown12 = getFeature(extract, BrownCluster.FEATURE_NAME_12);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown12.getValue());

        Feature brown10 = getFeature(extract, BrownCluster.FEATURE_NAME_10);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown10.getValue());

        Feature brown08 = getFeature(extract, BrownCluster.FEATURE_NAME_08);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown08.getValue());

        Feature brown06 = getFeature(extract, BrownCluster.FEATURE_NAME_06);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown06.getValue());

        Feature brown04 = getFeature(extract, BrownCluster.FEATURE_NAME_04);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown04.getValue());

        Feature brown02 = getFeature(extract, BrownCluster.FEATURE_NAME_02);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown02.getValue());

    }

    @Test
    public void testNoLowerCasing()
        throws Exception
    {
        
        BrownCluster featureExtractor = FeatureUtil.createResource(TcFeatureFactory.create(
                BrownCluster.class, BrownCluster.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                "src/test/resources/dummyBrownCluster.txt.gz",
                BrownCluster.PARAM_BROWN_CLUSTER_NORMALIZATION, true, BrownCluster.PARAM_LOWER_CASE,
                false));

        List<Feature> extract = new ArrayList<Feature>(featureExtractor.extract(jcas, tokTwo));
        assertEquals(8, extract.size());
        
        Feature brown16 = getFeature(extract, BrownCluster.FEATURE_NAME_16);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown16.getValue());

        Feature brown14 = getFeature(extract, BrownCluster.FEATURE_NAME_14);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown14.getValue());

        Feature brown12 = getFeature(extract, BrownCluster.FEATURE_NAME_12);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown12.getValue());

        Feature brown10 = getFeature(extract, BrownCluster.FEATURE_NAME_10);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown10.getValue());

        Feature brown08 = getFeature(extract, BrownCluster.FEATURE_NAME_08);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown08.getValue());

        Feature brown06 = getFeature(extract, BrownCluster.FEATURE_NAME_06);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown06.getValue());

        Feature brown04 = getFeature(extract, BrownCluster.FEATURE_NAME_04);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown04.getValue());

        Feature brown02 = getFeature(extract, BrownCluster.FEATURE_NAME_02);
        assertEquals(BrownCluster.FEATURE_NOVALUE, brown02.getValue());

    }

}
