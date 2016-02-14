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
package de.unidue.ltl.flextag.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.ml.uima.TcAnnotator;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class FlexTag
{
    private AnalysisEngine flexTagEngine;

    public FlexTag(String modelLocation)
        throws Exception
    {
        flexTagEngine = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelLocation,
                TcAnnotator.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
                TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName());
    }

    public List<List<String>> tagSentences(List<List<String>> sentences)
        throws Exception
    {

        JCas jCas = JCasFactory.createJCas();

        StringBuilder sb = new StringBuilder();
        for (List<String> sentence : sentences) {
            int sentStart = sb.length();
            for (String token : sentence) {
                int start = sb.length();
                int end = sb.length() + token.length();

                Token t = new Token(jCas, start, end);
                t.addToIndexes();

                sb.append(token);
            }
            int sentEnd = sb.length();
            Sentence s = new Sentence(jCas, sentStart, sentEnd);
            s.addToIndexes();
        }
        jCas.setDocumentText(sb.toString().trim());

        flexTagEngine.process(jCas);

        return extractTags(jCas);
    }

    private List<List<String>> extractTags(JCas jCas)
    {
        List<List<String>> posTags = new ArrayList<List<String>>();
        Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
        for (Sentence s : sentences) {

            List<String> tags = new ArrayList<String>();
            List<TextClassificationOutcome> tcos = JCasUtil.selectCovered(jCas,
                    TextClassificationOutcome.class, s.getBegin(), s.getEnd());

            for (TextClassificationOutcome tco : tcos) {
                tags.add(tco.getOutcome());
            }
            posTags.add(tags);
        }
        return posTags;
    }

}
