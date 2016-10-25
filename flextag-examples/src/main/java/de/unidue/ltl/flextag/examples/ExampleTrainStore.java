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

import org.apache.uima.collection.CollectionReader;
import org.dkpro.tc.api.features.TcFeatureFactory;

import de.unidue.ltl.flextag.core.FlexTagTrainSaveModel;
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

        Class<? extends CollectionReader> reader = LineTokenTagReader.class;

        String corpora = "src/main/resources/train/";
        String fileSuffix = "*.txt";

        FlexTagTrainSaveModel flex = new FlexTagTrainSaveModel(language, reader, corpora,
                fileSuffix, new File("target/trainedModel"));

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("TrainSaveModelDemo");

        // we additionally add a brown cluster and specify that we want to keep using the default
        // feature set, setting the last parameter to "false" will remove the default feature set
        // and only use the here specified features will be used.
        flex.setFeatures(false, TcFeatureFactory.create(BrownCluster.class, BrownCluster.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                "src/main/resources/res/dummyBrownCluster.txt.gz"));

        flex.execute(false);
    }

}
