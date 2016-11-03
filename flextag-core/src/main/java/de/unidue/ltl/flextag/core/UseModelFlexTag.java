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

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.flextag.core.uima.FlexTagUima;

public class UseModelFlexTag
{
    AnalysisEngine engine;

    public UseModelFlexTag(String filePathToFolderContainingModel, String language)
        throws ResourceInitializationException
    {
        engine = AnalysisEngineFactory.createEngine(FlexTagUima.class, FlexTagUima.PARAM_LANGUAGE,
                language, FlexTagUima.PARAM_MODEL_LOCATION, filePathToFolderContainingModel);
    }

    public List<String> posTag(List<String> sentence)
        throws UIMAException
    {

        JCas jCas = JCasFactory.createJCas();
        StringBuilder documentText = new StringBuilder();

        int start = 0;
        for (int i = 0; i < sentence.size(); i++) {
            String token = sentence.get(i);
            documentText.append(token);

            Token t = new Token(jCas, start, documentText.length());
            t.addToIndexes();

            if (i + 1 < sentence.size()) {
                documentText.append(" ");
            }
            start = documentText.length();
        }
        jCas.setDocumentText(documentText.toString());
        
        Sentence s = new Sentence(jCas, 0, jCas.getDocumentText().length());
        s.addToIndexes();

        callFlexTag(jCas);

        List<String> posTags = new ArrayList<String>();
        JCasUtil.select(jCas, POS.class).forEach(x -> posTags.add(x.getPosValue()));

        return posTags;
    }

    private void callFlexTag(JCas jCas)
        throws AnalysisEngineProcessException
    {
        Collection<DocumentMetaData> metaData = JCasUtil.select(jCas, DocumentMetaData.class);
        if (metaData.size() == 0) {
            DocumentMetaData meta = new DocumentMetaData(jCas);
            meta.setDocumentId("1");
            meta.setDocumentTitle("");
            meta.addToIndexes();
        }

        engine.process(jCas);
    }

}
