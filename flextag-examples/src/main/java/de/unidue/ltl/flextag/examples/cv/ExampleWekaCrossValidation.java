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
package de.unidue.ltl.flextag.examples.cv;

import static java.util.Arrays.asList;

import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;

import de.unidue.ltl.flextag.core.Classifier;
import de.unidue.ltl.flextag.core.FlexTagCrossValidation;
import de.unidue.ltl.flextag.core.reports.adapter.cv.CvWekafAvgKnownUnknownAccuracyReport;
import de.unidue.ltl.flextag.examples.util.DemoConstants;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import weka.classifiers.trees.J48;

public class ExampleWekaCrossValidation
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleWekaCrossValidation().run();
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
        flex.setExperimentName("WekaCrossValidationDemo");

        flex.setFeatures(TcFeatureFactory.create(LuceneCharacterNGram.class));
        
        List<Object> configuration = asList(new Object[] { J48.class.getName() });
        flex.setClassifier(Classifier.WEKA, configuration);
        flex.addReport(CvWekafAvgKnownUnknownAccuracyReport.class);
        flex.execute();
    }

}
