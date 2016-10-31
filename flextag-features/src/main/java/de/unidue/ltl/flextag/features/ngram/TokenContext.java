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

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.util.NGramUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This feature is a token context provider. It is meant to be used for classifier which do not
 * support string values i.e. all except CRFsuite. The features builds a frequency distribution
 * about the top N most frequent tokens. The index parameter defines on which field the focus lies
 * relative to the current token. Thus, -1 will set a boolean word to 1 iff the word is preceding
 * the current token AND the previous token is one of the N most frequent ones.
 */
public class TokenContext
    extends LookUpWithLucene
    implements FeatureExtractor
{
    public static final String PARAM_TARGET_INDEX = "PARAM_INDEX_TARGET_INDEX";
    @ConfigurationParameter(name = PARAM_TARGET_INDEX, mandatory = true)
    protected int shiftIdx;

    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
        super.extract(jcas, target);
        Set<Feature> features = new HashSet<Feature>();

        FrequencyDistribution<String> documentNgrams = null;

        Token token = get(jcas, target);

        if (token.getBegin() >= 0) {
            TextClassificationTarget prev = new TextClassificationTarget(jcas, token.getBegin(),
                    token.getEnd());

            documentNgrams = NGramUtils.getAnnotationNgrams(jcas, prev, ngramLowerCase,
                    filterPartialStopwordMatches, 1, 1, stopwords);
        }
        else {
            documentNgrams = new FrequencyDistribution<String>();
        }

        for (String topNgram : topKSet.getKeys()) {

            if (documentNgrams.getKeys().contains(topNgram)) {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0, true));
            }
        }
        return features;
    }

    public Token get(JCas jcas, TextClassificationTarget target)
    {
        Integer integer = super.unitBegin2Idx.get(target.getBegin());
        if (integer + shiftIdx < 0) {
            return new Token(jcas, -1, -1);
        }
        if (integer + shiftIdx >= unitBegin2Idx.size()) {
            return new Token(jcas, -1, -1);
        }

        return units.get(integer + shiftIdx);
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "tokenContext" + shiftIdx;
    }
}
