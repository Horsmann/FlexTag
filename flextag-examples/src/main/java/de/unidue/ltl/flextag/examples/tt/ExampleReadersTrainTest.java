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
package de.unidue.ltl.flextag.examples.tt;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.unidue.ltl.flextag.core.DefaultFeatures;
import de.unidue.ltl.flextag.core.FlexTagTrainTest;
import de.unidue.ltl.flextag.examples.util.DemoConstants;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;

/**
 * An example that addresses configuration of readers
 */
public class ExampleReadersTrainTest
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleReadersTrainTest().run();
    }

    public void run()
        throws Exception
    {
        String language = "en";
        String trainCorpora = DemoConstants.TRAIN_FOLDER;
        String trainFileSuffix = "*.txt";

        CollectionReaderDescription trainReader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, trainCorpora,
                LineTokenTagReader.PARAM_PATTERNS, trainFileSuffix);

        /*
         * We read here a file in the TEI-XML file format. DKPro provides a reader for this data
         * format which we will use. Many format readers for various common formats are already
         * available in DKPro. For a list of all supported file formats take a look at this website:
         * https://dkpro.github.io/dkpro-core/releases/1.7.0/formats/ Here we use the TEIREADER
         * which reads the teil-xml data format.
         * 
         * Furthermore, the suffix at the end of the test folder paths will recursively read all
         * .xml files in all sub-folders of the specified test folder path.
         */
        String testCorpora = "src/main/resources/test/**/*"; // read all sub-folders found under
                                                             // the test folders
        String testFileSuffix = "*.xml";

        CollectionReaderDescription testReader = CollectionReaderFactory.createReaderDescription(
                TeiReader.class, TeiReader.PARAM_LANGUAGE, language,
                TeiReader.PARAM_SOURCE_LOCATION, testCorpora, TeiReader.PARAM_PATTERNS,
                testFileSuffix);

        FlexTagTrainTest flex = new FlexTagTrainTest(trainReader, testReader);

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("ReaderDemo");
        flex.setFeatures(DefaultFeatures.getDefaultFeatures());
        flex.execute();
    }

}
