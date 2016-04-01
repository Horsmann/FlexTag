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
package de.unidue.ltl.flextag.features;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

public class IsFirstLetterCapitalized
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    public final static String FEATURE_NAME = "isFirstCharCapital";

    public Set<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        String token = aClassificationUnit.getCoveredText();
        boolean flc = firstLetterCapitalized(token);
        Feature feature = new Feature(FEATURE_NAME, flc ? 1 : 0);

        Set<Feature> features = new HashSet<Feature>();
        features.add(feature);
        return features;
    }

    static boolean firstLetterCapitalized(String aCoveredText)
    {
        if (aCoveredText.isEmpty()) {
            return false;
        }

        return Character.isUpperCase(aCoveredText.charAt(0));
    }

}
