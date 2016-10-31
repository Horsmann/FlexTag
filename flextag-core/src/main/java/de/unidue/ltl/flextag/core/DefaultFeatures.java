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
package de.unidue.ltl.flextag.core;

import static org.dkpro.tc.api.features.TcFeatureFactory.create;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;
import org.dkpro.tc.features.tcu.TargetSurfaceFormContextFeature;

import de.unidue.ltl.flextag.features.IsFirstLetterCapitalized;
import de.unidue.ltl.flextag.features.ngram.TokenContext;

public class DefaultFeatures
{

    public static TcFeatureSet getDefaultFeatures(Classifier c)
    {
        TcFeatureSet set = new TcFeatureSet();

        if (c == Classifier.CRFSUITE) {
            set.add(create(TargetSurfaceFormContextFeature.class,
                    TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, -1));
            set.add(create(TargetSurfaceFormContextFeature.class,
                    TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, 0));
            set.add(create(TargetSurfaceFormContextFeature.class,
                    TargetSurfaceFormContextFeature.PARAM_RELATIVE_TARGET_ANNOTATION_INDEX, +1));
        }
        else {
            set.add(create(TokenContext.class, TokenContext.PARAM_TARGET_INDEX,
                    -1, TokenContext.PARAM_NGRAM_USE_TOP_K, 1000));
            set.add(create(TokenContext.class, TokenContext.PARAM_TARGET_INDEX,
                    0, TokenContext.PARAM_NGRAM_USE_TOP_K, 1000));
            set.add(create(TokenContext.class, TokenContext.PARAM_TARGET_INDEX,
                    +1, TokenContext.PARAM_NGRAM_USE_TOP_K, 1000));
        }
        set.add(create(IsFirstLetterCapitalized.class));
        set.add(create(LuceneCharacterNGram.class, LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 1,
                LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 1,
                LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 50));
        set.add(create(LuceneCharacterNGram.class, LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 2,
                LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 2,
                LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 750));
        set.add(create(LuceneCharacterNGram.class, LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 3,
                LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 3,
                LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 750));
        set.add(create(LuceneCharacterNGram.class, LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 4,
                LuceneCharacterNGram.PARAM_NGRAM_MAX_N, 4,
                LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 750));

        return set;
    }
}
