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
package de.unidue.ltl.flextag.examples.config;

import static java.util.Arrays.asList;

import java.util.List;

import org.dkpro.tc.features.ngram.LuceneCharacterNGramUFE;

import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import de.unidue.ltl.flextag.core.FlexTagTrainTest;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;

public class ExampleClassifierWeka
{
    public static void main(String[] args)
        throws Exception
    {
        // Weka's classifier offer various configuration parameters this demo shows how to use Weka
        // classifier in their plain mode and with provided configuration parameters
        new ExampleClassifierWeka().runSimple();
        new ExampleClassifierWeka().runComplex();
    }

    public void runSimple()
        throws Exception
    {
        String language = "en";

        Class<?> reader = LineTokenTagReader.class;

        String trainCorpora = "src/main/resources/train/";
        String trainFileSuffix = "*.txt";

        String testDataFolder = "src/main/resources/test/";
        String testFileSuffix = "*.txt";

        FlexTagTrainTest flex = new FlexTagTrainTest(language, reader, trainCorpora,
                trainFileSuffix, testDataFolder, testFileSuffix);

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("WekaConfiguration");

        flex.setFeatures(new String[] { LuceneCharacterNGramUFE.class.getName() },
                new Object[] { LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MIN_N, 2,
                        LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MAX_N, 4,
                        LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_USE_TOP_K, 50 },
                false);

        List<String> configuration = asList(new String[] { SMO.class.getName() });
        flex.setWekaClassifier(configuration);

        flex.execute(false);
    }

    public void runComplex()
        throws Exception
    {
        String language = "en";

        Class<?> reader = LineTokenTagReader.class;

        String trainCorpora = "src/main/resources/train/";
        String trainFileSuffix = "*.txt";

        String testDataFolder = "src/main/resources/test/";
        String testFileSuffix = "*.txt";

        FlexTagTrainTest flex = new FlexTagTrainTest(language, reader, trainCorpora,
                trainFileSuffix, testDataFolder, testFileSuffix);

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("WekaConfiguration");

        flex.setFeatures(new String[] { LuceneCharacterNGramUFE.class.getName() },
                new Object[] { LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MIN_N, 2,
                        LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MAX_N, 4,
                        LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_USE_TOP_K, 50 },
                false);

        List<String> configuration = asList(new String[] { SMO.class.getName(), "-C", "1.0", "-K",
                PolyKernel.class.getName() + " " + "-C -1 -E 2" });
        flex.setWekaClassifier(configuration);

        flex.execute(false);
    }

}
