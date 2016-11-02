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

import java.io.File;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;

import de.unidue.ltl.flextag.core.Classifier;
import de.unidue.ltl.flextag.core.DefaultFeatures;
import de.unidue.ltl.flextag.core.FlexTagTrainSaveModel;
import de.unidue.ltl.flextag.examples.util.DemoConstants;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import de.unidue.ltl.flextag.features.resources.BrownCluster;

/**
 * An example that shows how to train a model and store the model for later usage
 */
public class ExampleTrainStore
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleTrainStore().run();
    }

    public void run()
        throws Exception
    {
        String language = "en";
        String corpora = DemoConstants.TRAIN_FOLDER;
        String fileSuffix = "*.txt";

        CollectionReaderDescription trainReader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, corpora,
                LineTokenTagReader.PARAM_PATTERNS, fileSuffix);

        FlexTagTrainSaveModel flex = new FlexTagTrainSaveModel(trainReader,
                new File("target/trainedModel"));

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("TrainSaveModelDemo");

        // we additionally add a brown cluster and specify that we want to keep using the default
        // feature set, setting the last parameter to "false" will remove the default feature set
        // and only use the here specified features will be used.
        TcFeatureSet features = DefaultFeatures.getDefaultFeatures(Classifier.CRFSUITE);
        features.add(TcFeatureFactory.create(BrownCluster.class,
                BrownCluster.PARAM_RESOURCE_LOCATION, DemoConstants.BROWN_CLUSTER));
        flex.setFeatures(features);

        flex.execute();
    }

}
