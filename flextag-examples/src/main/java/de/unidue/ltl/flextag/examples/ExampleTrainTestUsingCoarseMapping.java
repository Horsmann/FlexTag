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

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;

import de.unidue.ltl.flextag.core.DefaultFeatures;
import de.unidue.ltl.flextag.core.FlexTagTrainTest;
import de.unidue.ltl.flextag.examples.util.DemoConstants;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import de.unidue.ltl.flextag.features.resources.BrownCluster;

/**
 * An example which trains a model on the provided data and evaluates the trained model on provided
 * test data. We provide a mapping of the fine PoS tags to coarse word classes (similar to the
 * Universal PoS tagset) and run our experiment on coarse tags. We train and evaluate on coarse tags
 * by passing a mapping file and setting a flag.
 */
public class ExampleTrainTestUsingCoarseMapping
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleTrainTestUsingCoarseMapping().run();
    }

    public void run()
        throws Exception
    {
        String language = "en";
        String trainCorpora = DemoConstants.TRAIN_FOLDER;
        String trainFileSuffix = "*.txt";
        String testCorpora = DemoConstants.TEST_FOLDER;
        String testFileSuffix = "*.txt";

        /**
         * For many languages a <i>default</i> mapping is automatically loaded by using the language
         * code. This is usually what is considered as the <i>de-facto-standard</i>. This default
         * can be overwritten by providing an alternative mapping file.
         */

        CollectionReaderDescription trainReader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, trainCorpora,
                LineTokenTagReader.PARAM_PATTERNS, trainFileSuffix,
                LineTokenTagReader.PARAM_POS_MAPPING_LOCATION,
                "src/main/resources/mapping/en-default-pos.map"); // provide mapping here

        CollectionReaderDescription testReader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, testCorpora,
                LineTokenTagReader.PARAM_PATTERNS, testFileSuffix,
                LineTokenTagReader.PARAM_POS_MAPPING_LOCATION,
                "src/main/resources/mapping/en-default-pos.map"); // provide mapping here

        FlexTagTrainTest flex = new FlexTagTrainTest(trainReader, testReader);

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("TrainTestDemo");

        TcFeatureSet features = DefaultFeatures.getDefaultFeatures();
        features.add(TcFeatureFactory.create(BrownCluster.class,
                BrownCluster.PARAM_BROWN_CLUSTER_LOCATION, DemoConstants.BROWN_CLUSTER));
        flex.setFeatures(features);

        flex.useCoarse(true); // set to true to use the coarse mapped tags
        flex.execute();
    }

}
