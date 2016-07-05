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
package de.unidue.ltl.flextag.examples.util;

import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class OutputPrinter
    extends JCasAnnotator_ImplBase
{

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        Collection<Sentence> sentences = JCasUtil.select(aJCas, Sentence.class);
        for (Sentence s : sentences) {
            List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, s);
            for (Token t : tokens) {
                System.out.print(t.getCoveredText() + "/" + t.getPos().getPosValue() + " ");
            }
            System.out.println();
        }
    }

}
