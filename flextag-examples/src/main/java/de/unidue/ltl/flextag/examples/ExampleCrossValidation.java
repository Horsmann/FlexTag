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

import de.unidue.ltl.flextag.core.FlexTagCrossValidation;
import de.unidue.ltl.flextag.core.reports.adapter.cv.CvCrfAvgKnownUnknownAccuracyReport;
import de.unidue.ltl.flextag.examples.util.DemoConstants;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import de.unidue.ltl.flextag.features.resources.BrownCluster;

public class ExampleCrossValidation
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleCrossValidation().run();
    }

    public void run()
        throws Exception
    {
        String language = "en";
        String trainCorpora = DemoConstants.TRAIN_FOLDER_CROSS_VALIDATION;
        String trainFileSuffix = "*.txt";

        CollectionReaderDescription crd = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, trainCorpora,
                LineTokenTagReader.PARAM_PATTERNS, trainFileSuffix);

        FlexTagCrossValidation flex = new FlexTagCrossValidation(crd, 2);

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setExperimentName("CrfCrossValidationDemo");

        // we additionally add a brown cluster and specify that we want to keep using the default
        // feature set, setting the last parameter to "false" will remove the default feature set
        // and only use the here specified features will be used.
        flex.setFeatures(TcFeatureFactory.create(BrownCluster.class,
                BrownCluster.PARAM_RESOURCE_LOCATION, DemoConstants.BROWN_CLUSTER));
        flex.addReport(CvCrfAvgKnownUnknownAccuracyReport.class);
        flex.execute();
    }

}
