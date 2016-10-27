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

import java.io.File;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.features.ngram.LuceneCharacterNGram;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.flextag.core.FlexTagTrainSaveModel;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;

/**
 * An example which demonstrates the use of the preprocessing. We will trains a model on a raw text
 * file which is first segemented and than tagged with the Stanford tagger during preprocessing. The
 * Stanford prediction are used as gold labels for training our own model
 */
public class ExampleTrainTestWithPreprocessing
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleTrainTestWithPreprocessing().run();
    }

    public void run()
        throws Exception
    {
        String language = "en";
        String corpora = "src/main/resources/raw/";
        String fileSuffix = "*.txt";

        CollectionReaderDescription trainReader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, corpora,
                LineTokenTagReader.PARAM_PATTERNS, fileSuffix);

        FlexTagTrainSaveModel flex = new FlexTagTrainSaveModel(trainReader,
                new File(System.getProperty("user.home") + "/Desktop/flexOut"));

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder(System.getProperty("user.home") + "/Desktop/");
        }
        flex.setExperimentName("FlexTest");

        flex.setFeatures(false, TcFeatureFactory.create(LuceneCharacterNGram.class,
                LuceneCharacterNGram.PARAM_NGRAM_MIN_N, 2, LuceneCharacterNGram.PARAM_NGRAM_MAX_N,
                4, LuceneCharacterNGram.PARAM_NGRAM_USE_TOP_K, 50));

        flex.setPreprocessing(
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
                AnalysisEngineFactory.createEngineDescription(StanfordPosTagger.class, // We specify
                                                                                       // not model
                                                                                       // here and
                                                                                       // we use the
                                                                                       // default
                                                                                       // model -
                                                                                       // you can
                                                                                       // specify
                                                                                       // other
                                                                                       // model
                                                                                       // versions
                                                                                       // by setting
                                                                                       // a
                                                                                       // parameter
                                                                                       // wit the
                                                                                       // model's
                                                                                       // name
                        StanfordPosTagger.PARAM_LANGUAGE, "en"));

        flex.execute();
    }

}
