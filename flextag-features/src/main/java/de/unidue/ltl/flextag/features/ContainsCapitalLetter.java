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
package de.unidue.ltl.flextag.features;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class ContainsCapitalLetter
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    public static final String FEATURE_NAME = "containsCapitalLetter";

    public Set<Feature> extract(JCas aView, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        String token = aTarget.getCoveredText();
        Feature feature = new Feature(FEATURE_NAME, anyLetterCapitalized(token) ? 1 : 0);

        Set<Feature> features = new HashSet<Feature>();
        features.add(feature);
        return features;
    }

    public static boolean anyLetterCapitalized(String aCoveredText)
    {
        if (aCoveredText.isEmpty()) {
            return false;
        }

        for (char c : aCoveredText.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }

        return false;
    }

}
