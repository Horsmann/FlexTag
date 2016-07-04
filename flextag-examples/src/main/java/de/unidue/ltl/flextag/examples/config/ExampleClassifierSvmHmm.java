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
package de.unidue.ltl.flextag.examples.config;

import org.dkpro.lab.task.Dimension;
import org.dkpro.tc.features.length.NrOfCharsUFE;
import org.dkpro.tc.svmhmm.task.SVMHMMTestTask;
import org.dkpro.tc.svmhmm.util.OriginalTextHolderFeatureExtractor;

import de.unidue.ltl.flextag.core.FlexTagTrainTest;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;

public class ExampleClassifierSvmHmm
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleClassifierSvmHmm().run();
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
        flex.setExperimentName("SvmHmm");

        // SvmHmm does not support String value feature, some of the provided feature do use string
        // values. Please be aware that a feature space which works for classifier A does not
        // necessarily work for classifier B
        flex.setFeatures(new String[] { NrOfCharsUFE.class.getName(),

                // This feature is required by SvmHmm to be present
                OriginalTextHolderFeatureExtractor.class.getName()

        }, new String[] {}, true);

        Dimension<Double> dimClassificationArgsC = Dimension.create(SVMHMMTestTask.PARAM_C, 1.0,
                5.0);

        // various orders of dependencies of transitions in HMM (max 3)
        Dimension<Integer> dimClassificationArgsT = Dimension.create(SVMHMMTestTask.PARAM_ORDER_T,
                1);

        // various orders of dependencies of emissions in HMM (max 1)
        Dimension<Integer> dimClassificationArgsE = Dimension.create(SVMHMMTestTask.PARAM_ORDER_E,
                0);

        flex.setSvmHmmClassifier(dimClassificationArgsC, dimClassificationArgsT,
                dimClassificationArgsE);

        flex.execute(false);
    }

}
