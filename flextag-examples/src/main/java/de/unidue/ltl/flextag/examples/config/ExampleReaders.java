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

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.unidue.ltl.flextag.core.FlexTagTrainTest;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;

/**
 * An example that addresses configuration of readers
 */
public class ExampleReaders
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleReaders().run();
    }

    public void run()
        throws Exception
    {
        String language = "en";

        Class<?> reader = LineTokenTagReader.class;

        String trainCorpora = "src/main/resources/train/";
        String trainFileSuffix = "*.txt";

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
        String testDataFolder = "src/main/resources/test/**/*"; // read all sub-folders found under
                                                                // the test folders
        String testFileSuffix = "*.xml";

        FlexTagTrainTest flex = new FlexTagTrainTest(language, reader, trainCorpora,
                trainFileSuffix, testDataFolder, testFileSuffix);

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("ReaderDemo");

        // The reader set in the constructor cannot handle the xml format of the test-data, we thus
        // set a different reader.
        flex.setTestReader(TeiReader.class);

        flex.execute(false);
    }

}
