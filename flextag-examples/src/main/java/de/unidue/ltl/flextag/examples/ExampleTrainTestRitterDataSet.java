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

import de.unidue.ltl.flextag.core.FlexTagTrainTest;
import de.unidue.ltl.flextag.examples.util.DemoConstants;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import de.unidue.ltl.flextag.features.IsNumber;
import de.unidue.ltl.flextag.features.resources.BrownCluster;
import de.unidue.ltl.flextag.features.twitter.IsHashtag;
import de.unidue.ltl.flextag.features.twitter.IsRetweet;
import de.unidue.ltl.flextag.features.twitter.IsUserMention;

/**
 * An example which trains a model on the provided data and evaluates the trained model on provided
 * test data. Unlike the other test cases this one uses a much larger data set and more features to
 * produce some more serious output than the other examples which use only toy-setups for
 * demonstrations
 */
public class ExampleTrainTestRitterDataSet
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleTrainTestRitterDataSet().run();
    }

    public void run()
        throws Exception
    {
        String language = "en";
        String trainCorpora = "src/main/resources/ritter/train";
        String trainFileSuffix = "*.txt";
        String testCorpora = "src/main/resources/ritter/test";
        String testFileSuffix = "*.txt";

        CollectionReaderDescription trainReader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, trainCorpora,
                LineTokenTagReader.PARAM_PATTERNS, trainFileSuffix);

        CollectionReaderDescription testReader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, testCorpora,
                LineTokenTagReader.PARAM_PATTERNS, testFileSuffix);

        FlexTagTrainTest flex = new FlexTagTrainTest(trainReader, testReader);

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("RitterRichFeatureSetTrainTestDemo");

        flex.setFeatures(true,
                TcFeatureFactory.create(BrownCluster.class,
                        BrownCluster.PARAM_BROWN_CLUSTER_LOCATION, DemoConstants.BROWN_CLUSTER),
                TcFeatureFactory.create(IsHashtag.class),
                TcFeatureFactory.create(IsUserMention.class),
                TcFeatureFactory.create(IsNumber.class),
                TcFeatureFactory.create(IsRetweet.class));

        flex.execute();
    }

}
