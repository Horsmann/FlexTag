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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class PoSDictionaryTagFeature
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    public static final String PARAM_RESOURCE_LOCATION = "dictLocation";
    @ConfigurationParameter(name = PARAM_RESOURCE_LOCATION, mandatory = true)
    protected String dictionaryLocation;

    public static final String PARAM_LOAD_DICT_LOWER_CASE = "dictLowerCase";
    @ConfigurationParameter(name = PARAM_LOAD_DICT_LOWER_CASE, mandatory = true, defaultValue = "true")
    private boolean lowerCase;

    public static final String FEATURE_NAME = "tag_";

    private Map<String, String[]> map = new HashMap<>();

    public static final String UNKNOWN = "*";

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            loadDictionary();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    private void loadDictionary()
        throws IOException
    {
        List<String> readLines = FileUtils.readLines(new File(dictionaryLocation), "utf-8");

        for (String l : readLines) {
            String[] split = l.split("\t");
            String token = split[0];

            List<String> tags = new ArrayList<String>();
            for (int i = 1; i < split.length && i <= 3; i++) {
                tags.add(split[i]);
            }
            while (tags.size() < 3) {
                tags.add(UNKNOWN);
            }

            if (lowerCase) {
                token = token.toLowerCase();
            }

            map.put(token, tags.toArray(new String[0]));
        }
    }

    public Set<Feature> extract(JCas aView, TextClassificationTarget aClassificationUnit)
        throws TextClassificationException
    {
        Set<Feature> features = new HashSet<Feature>();

        String tokenText = aClassificationUnit.getCoveredText();

        if (lowerCase) {
            tokenText = tokenText.toLowerCase();
        }

        String[] mft = getMostFrequentTag(tokenText);

        features.add(new Feature(FEATURE_NAME + "1", mft[0], mft[0].equals(UNKNOWN)));
        features.add(new Feature(FEATURE_NAME + "2", mft[1], mft[1].equals(UNKNOWN)));
        features.add(new Feature(FEATURE_NAME + "3", mft[2], mft[2].equals(UNKNOWN)));

        return features;
    }

    private String[] getMostFrequentTag(String tokenText)
    {
        String[] mft = map.get(tokenText);
        if (mft != null) {
            return mft;
        }
        mft = map.get(tokenText.toLowerCase());
        if (mft != null) {
            return mft;
        }

        return new String[] { UNKNOWN, UNKNOWN, UNKNOWN };
    }

}
