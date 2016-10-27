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

import static java.util.Arrays.asList;

import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;

import de.unidue.ltl.flextag.core.Classifier;
import de.unidue.ltl.flextag.core.FlexTagTrainTest;
import de.unidue.ltl.flextag.examples.util.DemoConstants;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;

public class ExampleClassifierLiblinear
{
    public static void main(String[] args)
        throws Exception
    {
        // Weka's classifier offer various configuration parameters this demo shows how to use Liblinear
        // classifier in their plain mode and with provided configuration parameters
        new ExampleClassifierLiblinear().runSimple();
        new ExampleClassifierLiblinear().runComplex();
    }

    public void runSimple()
        throws Exception
    {
        String language = "en";
        String trainCorpora = DemoConstants.TRAIN_FOLDER;
        String trainFileSuffix = "*.txt";
        String testCorpora= DemoConstants.TEST_FOLDER;
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
        flex.setExperimentName("LiblinearConfiguration");

        flex.setFeatures(false, TcFeatureFactory.create(LuceneCharacterNGram.class,
                LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 2, LuceneCharacterNGram.PARAM_NGRAM_MAX_N,
                4, LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 50));

        List<Object> configuration = asList(new Object[] {  "-s", "3" });
        
        flex.setClassifier(Classifier.LIBLINEAR, configuration);
        
        flex.execute();
    }

    public void runComplex()
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
        flex.setExperimentName("LiblinearConfiguration");

        flex.setFeatures(false, TcFeatureFactory.create(LuceneCharacterNGram.class,
                LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 2, LuceneCharacterNGram.PARAM_NGRAM_MAX_N,
                4, LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 50));

        List<Object> configuration = asList(new Object[] { "-c", "100", "-e", "0.2", "-s", "3" });
        flex.setClassifier(Classifier.LIBLINEAR, configuration);

        flex.execute();
    }

}
