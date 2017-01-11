/**
 * Copyright 2017
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
import java.util.Arrays;
import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;

import de.unidue.ltl.flextag.core.FlexTagTrainSaveModel;
import de.unidue.ltl.flextag.core.UseModelFlexTag;
import de.unidue.ltl.flextag.examples.util.DemoConstants;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import de.unidue.ltl.flextag.features.resources.BrownCluster;

/**
 * Performs a prediction task with an already trained model. The model is trained as part of this
 * demo.
 */
public class ExampleUseModel
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleUseModel().run();
    }

    public List<String> run()
        throws Exception
    {
        String modelFolder = "target/theModel";
        // train the model we will use later
        trainModel(modelFolder);

        UseModelFlexTag useModel = new UseModelFlexTag(modelFolder, "en");

        // We expect as input a list of tokens that shall be tagged
        List<String> sentence = Arrays.asList(new String[] { "I", "go", "to", "sleep", "!" });

        // We receive in return a list containing the predicted PoS tags
        List<String> posTags = useModel.posTag(sentence);

        for (int i = 0; i < sentence.size(); i++) {
            System.out.print(sentence.get(i) + "/" + posTags.get(i));
            if (i + 1 < sentence.size()) {
                System.out.print(" ");
            }
        }
        return posTags;
    }

    private static void trainModel(String folder)
        throws Exception
    {
        String language = "en";
        String corpora = DemoConstants.TRAIN_FOLDER;
        String fileSuffix = "*.txt";

        CollectionReaderDescription trainReader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, corpora,
                LineTokenTagReader.PARAM_PATTERNS, fileSuffix);

        FlexTagTrainSaveModel flex = new FlexTagTrainSaveModel(trainReader, new File(folder));

        flex.setFeatures(TcFeatureFactory.create(BrownCluster.class,
                BrownCluster.PARAM_RESOURCE_LOCATION, DemoConstants.BROWN_CLUSTER));

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("ExampleUseModelDemo");
        flex.execute();
    }
}
