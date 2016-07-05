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
package de.unidue.ltl.flextag.examples;

import de.unidue.ltl.flextag.core.FlexTagTrainTest;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import de.unidue.ltl.flextag.features.resources.BrownCluster;

/**
 * An example which trains a model on the provided data and evaluates the trained model on provided
 * test data
 */
public class ExampleTrainTestWithPreprocessing
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleTrainTestWithPreprocessing().run();
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
        flex.setExperimentName("TrainTestDemo");

        // we additionally add a brown cluster and specify that we want to keep using the default
        // feature set, setting the last parameter to "false" will remove the default feature set
        // and only use the here specified features will be used.
        flex.setFeatures(new String[] { BrownCluster.class.getName() },
                new Object[] { BrownCluster.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                        "src/main/resources/res/dummyBrownCluster.txt.gz" },
                false);

        flex.execute(false);
    }

}
