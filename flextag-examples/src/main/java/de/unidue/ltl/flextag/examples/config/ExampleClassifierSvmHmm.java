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

import java.util.Arrays;
import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.ml.svmhmm.util.OriginalTextHolderFeatureExtractor;

import de.unidue.ltl.flextag.core.FlexTagMachineLearningAdapter;
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
        String trainCorpora = "src/main/resources/train/";
        String trainFileSuffix = "*.txt";
        String testCorpora = "src/main/resources/test/";
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
        flex.setExperimentName("SvmHmm");

        // SvmHmm does not support String value feature, some of the provided feature do use string
        // values. Please be aware that a feature space which works for classifier A does not
        // necessarily work for classifier B
        flex.setFeatures(true, TcFeatureFactory.create(LuceneNGram.class), TcFeatureFactory.create(OriginalTextHolderFeatureExtractor.class));

        
        List<Object> classificationArgs = Arrays.asList("-c", "5.0", "-t", "2");
        flex.setMachineLearningClassifier(FlexTagMachineLearningAdapter.SVMHMM, classificationArgs);

        flex.execute(false);
    }

}
