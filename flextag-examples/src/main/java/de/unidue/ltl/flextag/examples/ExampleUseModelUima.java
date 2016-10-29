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

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.flextag.core.DefaultFeatures;
import de.unidue.ltl.flextag.core.FlexTagTrainSaveModel;
import de.unidue.ltl.flextag.core.uima.FlexTagUima;
import de.unidue.ltl.flextag.examples.util.DemoConstants;
import de.unidue.ltl.flextag.examples.util.LineTokenTagReader;
import de.unidue.ltl.flextag.examples.util.OutputPrinter;

/**
 * Performs a prediction task with an already trained model. The model is trained as part of this
 * demo. We will use a DKPro pipeline for using the trained model which requires tokenization of raw
 * texts and printing the output to sysout.
 */
public class ExampleUseModelUima
{
    public static void main(String[] args)
        throws Exception
    {
        new ExampleUseModelUima().run();
    }

    public void run()
        throws Exception
    {
        String modelFolder = "target/theModel";
        // train the model we will use later
        trainModel(modelFolder);

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TextReader.class, TextReader.PARAM_SOURCE_LOCATION,
                "src/main/resources/raw/rawText.txt", TextReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription seg = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription tagger = AnalysisEngineFactory.createEngineDescription(
                FlexTagUima.class, FlexTagUima.PARAM_LANGUAGE, "en",
                FlexTagUima.PARAM_MODEL_LOCATION, modelFolder);

        // a helper class which simply iterates each token and prints the predicted POS tag
        AnalysisEngineDescription printer = AnalysisEngineFactory
                .createEngineDescription(OutputPrinter.class);

        SimplePipeline.runPipeline(reader, seg, tagger, printer);
    }

    private static void trainModel(String folder)
        throws Exception
    {
        String language = "en";
        String corpora = DemoConstants.TRAIN_FOLDER;
        String fileSuffix = "*.txt";
        
        CollectionReaderDescription trainReader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_LANGUAGE, language,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, corpora,
                LineTokenTagReader.PARAM_PATTERNS, fileSuffix);

        FlexTagTrainSaveModel flex = new FlexTagTrainSaveModel(trainReader, new File(folder));

        if (System.getProperty("DKPRO_HOME") == null) {
            flex.setDKProHomeFolder("target/home");
        }
        flex.setFeatures(DefaultFeatures.getDefaultFeatures());
        flex.setExperimentName("ExampleUseModelDemo");
        flex.execute();
    }
}
