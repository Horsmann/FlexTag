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
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class IsNumber
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    private static final String FEATURE_NAME = "isNum";

    public Set<Feature> extract(JCas aView, TextClassificationTarget aClassificationUnit)
        throws TextClassificationException
    {

        boolean isNum = is(aClassificationUnit.getCoveredText());
        Feature feature;

        if (isNum) {
            feature = new Feature(FEATURE_NAME, 1);
        }
        else {
            feature = new Feature(FEATURE_NAME, 0, true);
        }
        Set<Feature> features = new HashSet<Feature>();
        features.add(feature);
        return features;
    }

    static boolean is(String coveredText)
    {
        return isPure(coveredText) || isTime(coveredText) || isDotCommaSeparatedNum(coveredText)
                || isNumberWithUnitOrMiscSymbols(coveredText);
    }

    private static boolean isNumberWithUnitOrMiscSymbols(String coveredText)
    {
        return isPure(coveredText.replaceAll("[\\.,\\-%////\\€¥\\$]+", ""));
    }

    static boolean isDotCommaSeparatedNum(String coveredText)
    {

        String value = coveredText.replaceAll(",", "").replaceAll("\\.", "");
        return isPure(value) && !value.isEmpty();
    }

    static boolean isTime(String coveredText)
    {
        return coveredText.matches("[0-9]+:[0-9]+");
    }

    static boolean isPure(String coveredText)
    {
        Set<Character> chars = new HashSet<>();
        for (char c : coveredText.toCharArray()) {
            chars.add(c);
        }

        for (char c : chars) {
            if (!(c >= '0' && c <= '9')) {
                return false;
            }
        }

        return true;
    }
}
