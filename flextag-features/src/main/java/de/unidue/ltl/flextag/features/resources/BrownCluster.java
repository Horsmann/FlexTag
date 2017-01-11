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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

public class BrownCluster
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    public static final String NOT_SET = "*";

    public static final String FEATURE_NAME = "brown_";

    public static final String PARAM_RESOURCE_LOCATION = "brownClusterLocations";
    @ConfigurationParameter(name = PARAM_RESOURCE_LOCATION, mandatory = true)
    private File inputFile;

    /**
     * Defines in which sizes the bit string information is set as feature for instance bit string
     * 1111000 - granularity 1 will set set following feature values {1,11,111,1111,11110, ...} -
     * granularity 2 will set set following feature values {11,1111,111100, ...} - granularity 3
     * will set set following feature values {111,111100, ...} default is 2
     */
    public static final String PARAM_CODE_GRANULARITY = "brownGranularity";
    @ConfigurationParameter(name = PARAM_CODE_GRANULARITY, mandatory = true, defaultValue = "2")
    private int stepSize;

    public static final String PARAM_LOWER_CASE = "doBrownLowerCase";
    @ConfigurationParameter(name = PARAM_LOWER_CASE, mandatory = true, defaultValue = "true")
    boolean lowerCase;

    public static final String PARAM_NORMALIZATION = "doBrownNormalization";
    @ConfigurationParameter(name = PARAM_NORMALIZATION, mandatory = true, defaultValue = "true")
    boolean normalize;

    private HashMap<String, String> map = null;

    int maxClustLength = 0;

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

    public Set<Feature> extract(JCas aJcas, TextClassificationTarget aClassificationUnit)
        throws TextClassificationException
    {
        String unit = aClassificationUnit.getCoveredText();

        if (lowerCase) {
            unit = unit.toLowerCase();
        }

        if (normalize) {
            String workingCopy = normalizeUrls(unit, "<URL>");
            workingCopy = normalizeEmails(workingCopy, "<EMAIL>");
            workingCopy = normalizeAtMentions(workingCopy, "<ATMENTION>");
            workingCopy = normalizeHashTags(workingCopy, "<HASHTAG>");
            unit = workingCopy;
        }

        Set<Feature> features = createFeatures(unit);

        return features;
    }

    private Set<Feature> createFeatures(String unit)
    {
        Set<Feature> features = new HashSet<Feature>();

        String bitCode = map.get(unit);

        if (bitCode == null) {
            for (int i = 0; i < maxClustLength; i = i + stepSize) {
                features.add(new Feature(FEATURE_NAME + (i + stepSize), NOT_SET, true));
            }
            return features;
        }

        for (int i = 0; i < maxClustLength; i = i + stepSize) {
            boolean dummy=false;
            String subCode = null;
            //the bit code is to short
            if (bitCode.length() < i + stepSize) {
                
                //a few bits are new but not full step size so use the string as is
                if (i <= bitCode.length()) {
                    subCode = bitCode;
                }
                else {
                    // not new information, set dummy value
                    subCode = NOT_SET;
                    dummy = true;
                }
            }
            else {
                subCode = bitCode.substring(0, i + stepSize);
            }
            features.add(new Feature(FEATURE_NAME + (i + stepSize), subCode, dummy));
            // System.out.println(subCode);
        }

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

                if (split[0].length() > maxClustLength) {
                    maxClustLength = split[0].length();
                }

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
        InputStreamReader isr = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
        return new BufferedReader(isr);
    }

    public static String replaceTwitterPhenomenons(String input, String replacement)
    {
        /* Email and atmention are sensitive to order of execution */
        String workingCopy = input;
        workingCopy = normalizeUrls(workingCopy, replacement);
        workingCopy = normalizeEmails(workingCopy, replacement);
        workingCopy = normalizeAtMentions(workingCopy, replacement);
        workingCopy = normalizeHashTags(workingCopy, replacement);
        return workingCopy;
    }

    public static String normalizeHashTags(String input, String replacement)
    {
        String HASHTAG = "#[a-zA-Z0-9-_]+";
        String normalized = input.replaceAll(HASHTAG, replacement);
        return normalized;
    }

    public static String normalizeEmails(String input, String replacement)
    {
        String PREFIX = "[a-zA-Z0-9-_\\.]+";
        String SUFFIX = "[a-zA-Z0-9-_]+";

        String EMAIL_REGEX = PREFIX + "@" + SUFFIX + "\\." + "[a-zA-Z]+";
        String normalize = input.replaceAll(EMAIL_REGEX, replacement);
        return normalize;
    }

    public static String normalizeAtMentions(String input, String replacement)
    {
        String AT_MENTION_REGEX = "@[a-zA-Z0-9_-]+";
        String normalize = input.replaceAll(AT_MENTION_REGEX, replacement);
        return normalize;
    }

    public static String normalizeUrls(String input, String replacement)
    {
        String URL_CORE_REGEX = "[\\/\\\\.a-zA-Z0-9-_]+";

        String normalized = input.replaceAll("http:" + URL_CORE_REGEX, replacement);
        normalized = normalized.replaceAll("https:" + URL_CORE_REGEX, replacement);
        normalized = normalized.replaceAll("www\\." + URL_CORE_REGEX, replacement);

        return normalized;
    }

    public int getStepSize()
    {
        return stepSize;
    }

    public int getMaxLength()
    {
        return maxClustLength;
    }

}
