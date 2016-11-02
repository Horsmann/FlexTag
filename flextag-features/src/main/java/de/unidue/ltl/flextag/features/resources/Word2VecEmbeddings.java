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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

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
 * Implementation assumes a file as provided by https://github.com/dav/word2vec i.e. 
 * Row 1: 20370 50
 * Row 2-N: WORD VAL1 VAL2 ... VAL N
 * The first row is skipped 
 */
public class Word2VecEmbeddings
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{

    public static final String PARAM_RESOURCE_LOCATION = "wordEmbedding";
    @ConfigurationParameter(name = PARAM_RESOURCE_LOCATION, mandatory = true)
    private File inputFile;
    
    public static final String PARAM_USE_LOWER_CASE = "useLowerCase";
    @ConfigurationParameter(name = PARAM_USE_LOWER_CASE, mandatory = true, defaultValue="false")
    private boolean lowerCase;
    
    public static final String FEATURE_NAME ="w2v_";

    private int vecLen;

    private HashMap<String, String[]> map = null;

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
        catch (TextClassificationException e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    public Set<Feature> extract(JCas aJcas, TextClassificationTarget aClassificationUnit)
        throws TextClassificationException
    {

        String unit = aClassificationUnit.getCoveredText();
        if(lowerCase){
            unit = unit.toLowerCase();
        }
        Set<Feature> features = createFeatures(unit);

        return features;
    }

    private Set<Feature> createFeatures(String unit)
    {
        Set<Feature> features = new HashSet<Feature>();

        String[] vector = map.get(unit);

        for (int i = 0; i < vecLen; i++) {
            features.add(new Feature(FEATURE_NAME + (i + 1), getValue(vector, i), isDefault(vector,i)));
        }
        return features;
    }

    private boolean isDefault(String[] vector, int i)
    {
        if (vector == null) {
            return true;
        }
        return false;
    }

    private Double getValue(String[] vector, int i)
    {
        if (vector == null) {
            return 0.0;
        }
        return Double.valueOf(vector[i]);
    }

    private void init()
        throws TextClassificationException
    {

        if (map != null) {
            return;
        }
        map = new HashMap<String, String[]>();

        try {

            BufferedReader bf = openFile();
            String line = bf.readLine(); // ignore the first line
            while ((line = bf.readLine()) != null) {
                String[] split = line.split(" ");
                vecLen = split.length - 1;
                List<String> vec = new ArrayList<String>();
                for (int i = 1; i < split.length; i++) {
                    vec.add(split[i]);
                }
                map.put(split[0], vec.toArray(new String[0]));
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
        if (inputFile.getAbsolutePath().endsWith(".gz")) {

            isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile)),
                    "UTF-8");
        }
        else {
            isr = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
        }
        return new BufferedReader(isr);
    }
}
