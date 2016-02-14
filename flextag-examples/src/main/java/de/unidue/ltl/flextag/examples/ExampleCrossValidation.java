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

import de.unidue.ltl.flextag.core.FlexTagCrossValidation;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import de.unidue.ltl.flextag.features.resources.BrownCluster;

public class ExampleCrossValidation
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleCrossValidation().run();
    }
    
    public void run() throws Exception{
        String language = "en";

        Class<?> reader = LineTokenTagReader.class;

        String trainCorpora = "src/main/resources/cv/";
        String trainFileSuffix = "*.txt";

        FlexTagCrossValidation flex = new FlexTagCrossValidation(language, reader, trainCorpora,
                trainFileSuffix, 2);

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("CrossValidationDemo");

        // we additionally add a brown cluster and specify that we want to keep using the default
        // feature set, setting the last parameter to "false" will remove the default feature set
        // and only use the here specified features will be used.
        flex.setFeatures(new String[] { BrownCluster.class.getName() }, new Object[] {
                BrownCluster.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                "src/main/resources/res/dummyBrownCluster.txt.gz" }, false);

        flex.execute(false);
    }

}
