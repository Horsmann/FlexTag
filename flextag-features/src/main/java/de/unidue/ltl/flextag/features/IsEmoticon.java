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
import java.util.regex.Pattern;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class IsEmoticon
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    public static final String FEATURE_NAME = "isSmiley";

    public Set<Feature> extract(JCas aView, TextClassificationTarget aClassificationUnit)
        throws TextClassificationException
    {
        String token = aClassificationUnit.getCoveredText();
        boolean isSmiley = isEmoticon(token);
        Feature feature;
        if (isSmiley) {
            feature = new Feature(FEATURE_NAME, 1);
        }
        else {
            feature = new Feature(FEATURE_NAME, 0, true);
        }

        Set<Feature> features = new HashSet<Feature>();
        features.add(feature);
        return features;
    }

    static boolean isEmoticon(String u)
    {
        if (isFwd3ElementSmiley(u)) {
            return true;
        }

        if (isBckwd3ElementSmiley(u)) {
            return true;
        }

        if (isFwd2ElementSmiley(u)) {
            return true;
        }

        if (isBckwd2ElementSmiley(u)) {
            return true;
        }

        if (isHorizontalSmiley(u)) {
            return true;
        }
        
        if(isSurrogatePairEmoji(u)){
            return true;
        }

        return false;
    }

    private static boolean isSurrogatePairEmoji(String u)
    {
        return Pattern.matches("[\uD83C-\uDBFF\uDC00-\uDFFF]+", u);
    }

    private static boolean isHorizontalSmiley(String u)
    {
        return Pattern.matches("[\\^<>\\(\\)\\-\"='\\*oO0]+[\\._]*[\\^<>\\(\\)\\-\"='\\*oO0]+", u);
    }

    private static boolean isBckwd2ElementSmiley(String u)
    {
        return Pattern.matches("[dDpP\\)\\(\\[\\]][:;xX8]", u);
    }

    private static boolean isBckwd3ElementSmiley(String u)
    {
        return Pattern.matches("[dDpP\\)\\(\\[\\]]+[\\-oO][:;xX8]", u);
    }

    private static boolean isFwd3ElementSmiley(String u)
    {
        return Pattern.matches("[:;xX8][\\-oO][dDpP\\)\\(\\[\\]\\\\/\\\\]+", u);
    }

    private static boolean isFwd2ElementSmiley(String u)
    {
        return Pattern.matches("[:;xX8][dDpPcx\\)\\(\\[\\]]", u);
    }

}
