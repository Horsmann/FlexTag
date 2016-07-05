/**
 * Copyright 2016
 * Language Technology Lab
 * University of Duisburg-Essen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
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
        flex.setCrfsuiteClassifier(CRFSuiteAdapter.ALGORITHM_AVERAGED_PERCEPTRON);

        flex.execute(false);
    }

}
