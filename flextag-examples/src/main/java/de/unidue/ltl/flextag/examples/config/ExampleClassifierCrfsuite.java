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

import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.features.length.NrOfCharsUFE;

import de.unidue.ltl.flextag.core.FlexTagTrainTest;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;

public class ExampleClassifierCrfsuite
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleClassifierCrfsuite().run();
    }

    public void run()
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
        flex.setExperimentName("CrfsuiteConfiguration");

        flex.setFeatures(new String[] { NrOfCharsUFE.class.getName(), }, new String[] {}, true);

        // CRFSuite defines various algorithm to use for training which are defined over the
        // CRFSuiteAdapter constant. Some are slow on large data sets
        flex.setCrfsuiteClassifier(CRFSuiteAdapter.ALGORITHM_L2_STOCHASTIC_GRADIENT_DESCENT);

        flex.execute(false);
    }

}
