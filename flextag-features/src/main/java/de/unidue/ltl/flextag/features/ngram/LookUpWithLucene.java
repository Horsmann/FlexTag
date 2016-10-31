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
package de.unidue.ltl.flextag.features.ngram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.meta.MetaCollectorConfiguration;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.dkpro.tc.features.ngram.LuceneNGram;
import org.dkpro.tc.features.ngram.base.LuceneFeatureExtractorBase;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class LookUpWithLucene
    extends LuceneFeatureExtractorBase
{
    private String lastSeenDocumentId = "";

    protected HashMap<Integer, Boolean> idx2SequenceBegin = new HashMap<Integer, Boolean>();
    protected HashMap<Integer, Boolean> idx2SequenceEnd = new HashMap<Integer, Boolean>();

    protected HashMap<Integer, Token> begin2Unit = new HashMap<Integer, Token>();
    protected HashMap<Integer, Integer> unitBegin2Idx = new HashMap<Integer, Integer>();
    protected HashMap<Integer, Integer> unitEnd2Idx = new HashMap<Integer, Integer>();
    protected List<Token> units = new ArrayList<Token>();

    public Set<Feature> extract(JCas aView, TextClassificationTarget atarget)
        throws TextClassificationException
    {
        if (isTheSameDocument(aView)) {
            return null;
        }
        begin2Unit = new HashMap<Integer, Token>();
        unitBegin2Idx = new HashMap<Integer, Integer>();
        idx2SequenceBegin = new HashMap<Integer, Boolean>();
        idx2SequenceEnd = new HashMap<Integer, Boolean>();
        units = new ArrayList<Token>();

        int i = 0;
        for (Token t : JCasUtil.select(aView, Token.class)) {
            Integer begin = t.getBegin();
            Integer end = t.getEnd();
            begin2Unit.put(begin, t);
            unitBegin2Idx.put(begin, i);
            unitEnd2Idx.put(end, i);
            units.add(t);
            i++;
        }
        for (Sentence sequence : JCasUtil.select(aView, Sentence.class)) {
            Integer begin = sequence.getBegin();
            Integer end = sequence.getEnd();
            Integer idxStartUnit = unitBegin2Idx.get(begin);
            Integer idxEndUnit = unitEnd2Idx.get(end);
            idx2SequenceBegin.put(idxStartUnit, true);
            idx2SequenceEnd.put(idxEndUnit, true);
        }
        
        return null;
    }

    private boolean isTheSameDocument(JCas aView)
    {
        String currentId = aView.getDocumentText();
        boolean isSame = currentId.equals(lastSeenDocumentId);
        lastSeenDocumentId = currentId;
        
        return isSame;
    }

    @Override
    public List<MetaCollectorConfiguration> getMetaCollectorClasses(Map<String, Object> parameterSettings)
        throws ResourceInitializationException
    {
        return Arrays.asList(new MetaCollectorConfiguration(LuceneUniGramMetaCollector.class,
                parameterSettings).addStorageMapping(
                        LuceneUniGramMetaCollector.PARAM_TARGET_LOCATION,
                        LuceneNGram.PARAM_SOURCE_LOCATION,
                        LuceneUniGramMetaCollector.LUCENE_DIR));
    }

    @Override
    protected String getFieldName()
    {
        return LuceneNGram.LUCENE_NGRAM_FIELD;
    }

    @Override
    protected int getTopN()
    {
        return ngramUseTopK;
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "prevToken";
    }

}
