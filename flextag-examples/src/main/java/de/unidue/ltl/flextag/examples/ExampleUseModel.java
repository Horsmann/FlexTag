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
package de.unidue.ltl.flextag.examples;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.unidue.ltl.flextag.core.FlexTagTrainSaveModel;
import de.unidue.ltl.flextag.core.FlexTagUseModel;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import de.unidue.ltl.flextag.features.resources.BrownCluster;

public class ExampleUseModel
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleUseModel().run();
    }

    public void run()
        throws Exception
    {
        String modelFolder = "target/theModel";
        // train the model we will use later
        trainModel(modelFolder);

        FlexTagUseModel useModel = new FlexTagUseModel(modelFolder, "en");

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
    }

    private static void trainModel(String folder)
        throws Exception
    {
        String language = "en";
        Class<?> reader = LineTokenTagReader.class;

        String corpora = "src/main/resources/train/";
        String fileSuffix = "*.txt";

        FlexTagTrainSaveModel flex = new FlexTagTrainSaveModel(language, reader, corpora,
                fileSuffix, new File(folder));

        flex.setFeatures(new String[] { BrownCluster.class.getName() }, new Object[] {
                BrownCluster.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                "src/main/resources/res/dummyBrownCluster.txt.gz" }, false);

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("ExampleUseModelDemo");
        flex.execute(false);
    }
}
