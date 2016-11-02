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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * A feature extractor that uses a Brown cluster resource. We take the format of the resource as
 * created by this implementation as reference: https://github.com/percyliang/brown-cluster.git
 */
public class BrownCluster
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    static final String FEATURE_NAME_16 = "brown16_";
    static final String FEATURE_NAME_14 = "brown14_";
    static final String FEATURE_NAME_12 = "brown12_";
    static final String FEATURE_NAME_10 = "brown10_";
    static final String FEATURE_NAME_08 = "brown08_";
    static final String FEATURE_NAME_06 = "brown06_";
    static final String FEATURE_NAME_04 = "brown04_";
    static final String FEATURE_NAME_02 = "brown02_";

    static final String FEATURE_NOVALUE = "*";

    public static final String PARAM_RESOURCE_LOCATION = "brownLocation";
    @ConfigurationParameter(name = PARAM_RESOURCE_LOCATION, mandatory = true)
    File inputFile;

    public static final String PARAM_USE_NORMALIZATION = "doBrownNormalization";
    @ConfigurationParameter(name = PARAM_USE_NORMALIZATION, mandatory = true, defaultValue = "true")
    Boolean normalize;

    public static final String PARAM_USE_LOWER_CASE = "doBrownLowerCase";
    @ConfigurationParameter(name = PARAM_USE_LOWER_CASE, mandatory = true, defaultValue = "true")
    Boolean lowerCase;

    private HashMap<String, String> map = null;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            init();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    public Set<Feature> extract(JCas aJcas, TextClassificationTarget aTarget)
        throws TextClassificationException
    {
        String unit = aTarget.getCoveredText();

        if (lowerCase) {
            unit = unit.toLowerCase();
        }

        if (normalize) {
            unit = normalizeUrls(unit, "<URL>");
            unit = normalizeEmails(unit, "<EMAIL>");
            unit = normalizeAtMentions(unit, "<ATMENTION>");
            unit = normalizeHashTags(unit, "<HASHTAG>");
        }

        Set<Feature> features = createFeatures(unit);

        return features;
    }

    private Set<Feature> createFeatures(String unit)
    {
        Set<Feature> features = new HashSet<Feature>();

        String bitCode = map.get(unit);

        features.add(new Feature(FEATURE_NAME_16,
                bitCode != null && bitCode.length() >= 16 ? bitCode.substring(0, 16)
                        : FEATURE_NOVALUE));
        features.add(new Feature(FEATURE_NAME_14,
                bitCode != null && bitCode.length() >= 14 ? bitCode.substring(0, 14)
                        : FEATURE_NOVALUE));
        features.add(new Feature(FEATURE_NAME_12,
                bitCode != null && bitCode.length() >= 12 ? bitCode.substring(0, 12)
                        : FEATURE_NOVALUE));
        features.add(new Feature(FEATURE_NAME_10,
                bitCode != null && bitCode.length() >= 10 ? bitCode.substring(0, 10)
                        : FEATURE_NOVALUE));
        features.add(new Feature(FEATURE_NAME_08,
                bitCode != null && bitCode.length() >= 8 ? bitCode.substring(0, 8)
                        : FEATURE_NOVALUE));
        features.add(new Feature(FEATURE_NAME_06,
                bitCode != null && bitCode.length() >= 6 ? bitCode.substring(0, 6)
                        : FEATURE_NOVALUE));
        features.add(new Feature(FEATURE_NAME_04,
                bitCode != null && bitCode.length() >= 4 ? bitCode.substring(0, 4)
                        : FEATURE_NOVALUE));
        features.add(new Feature(FEATURE_NAME_02,
                bitCode != null && bitCode.length() >= 2 ? bitCode.substring(0, 2)
                        : FEATURE_NOVALUE));

        return features;
    }

    private void init()
        throws TextClassificationException
    {

        if (map != null) {
            return;
        }
        map = new HashMap<String, String>();

        try {

            BufferedReader bf = openFile();
            String line = null;
            while ((line = bf.readLine()) != null) {
                String[] split = line.split("\t");
                map.put(split[1], split[0]);
            }

        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
    }

    private BufferedReader openFile()
        throws Exception
    {
        InputStreamReader isr = null;

        try {
            isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile)),
                    "UTF-8");
        }
        catch (ZipException ze) {
            isr = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
        }

        return new BufferedReader(isr);
    }

    private String normalizeHashTags(String input, String replacement)
    {
        String HASHTAG = "#[a-zA-Z0-9-_]+";
        String normalized = input.replaceAll(HASHTAG, replacement);
        return normalized;
    }

    private String normalizeEmails(String input, String replacement)
    {
        String PREFIX = "[a-zA-Z0-9-_\\.]+";
        String SUFFIX = "[a-zA-Z0-9-_]+";

        String EMAIL_REGEX = PREFIX + "@" + SUFFIX + "\\." + "[a-zA-Z]+";
        String normalize = input.replaceAll(EMAIL_REGEX, replacement);
        return normalize;
    }

    private String normalizeAtMentions(String input, String replacement)
    {
        String AT_MENTION_REGEX = "@[a-zA-Z0-9_-]+";
        String normalize = input.replaceAll(AT_MENTION_REGEX, replacement);
        return normalize;
    }

    private String normalizeUrls(String input, String replacement)
    {
        String URL_CORE_REGEX = "[\\/\\\\.a-zA-Z0-9-_]+";

        String normalized = input.replaceAll("http:" + URL_CORE_REGEX, replacement);
        normalized = normalized.replaceAll("https:" + URL_CORE_REGEX, replacement);
        normalized = normalized.replaceAll("www\\." + URL_CORE_REGEX, replacement);

        return normalized;
    }

}
