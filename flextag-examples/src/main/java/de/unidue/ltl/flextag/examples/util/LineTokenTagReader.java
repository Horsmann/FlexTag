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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class LineTokenTagReader
    extends JCasResourceCollectionReader_ImplBase
{

    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String mappingPosLocation;

    public static final String PARAM_POS_TAGSET = ComponentParameters.PARAM_POS_TAG_SET;
    @ConfigurationParameter(name = PARAM_POS_TAGSET, mandatory = false)
    protected String posTagset;

    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, defaultValue = "UTF-8")
    protected String encoding;

    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;
    
    public static final String PARAM_LOWER_CASE = "PARAM_LOWER_CASE";
    @ConfigurationParameter(name = PARAM_LOWER_CASE, mandatory = false, defaultValue="false")
    private boolean lowerCase;

    public static final String PARAM_SEQUENCES_PER_CAS = "PARAM_SEQUENCES_PER_CAS";
    @ConfigurationParameter(name = PARAM_SEQUENCES_PER_CAS, mandatory = true, defaultValue = "1000")
    private int seqLimit;

    public static final String ENCODING_AUTO = "auto";

    private MappingProvider posMappingProvider;

    private BufferedReader br;

    private List<List<String>> sequences = new ArrayList<List<String>>();

    private List<BufferedReader> bfs = new ArrayList<BufferedReader>();
    private int currentReader = 0;

    private int instanceId = 1;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        posMappingProvider = new MappingProvider();
        posMappingProvider.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/"
                        + "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
        posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        posMappingProvider.setDefault("tagger.tagset", "default");
        posMappingProvider.setOverride(MappingProvider.LOCATION, mappingPosLocation);
        posMappingProvider.setOverride(MappingProvider.LANGUAGE, language);
        posMappingProvider.setOverride("tagger.tagset", posTagset);

        try {
            for (Resource r : getResources()) {
                String name = r.getResource().getFile().getName();
                InputStreamReader is = null;
                if (name.endsWith(".gz")) {
                    is = new InputStreamReader(new GZIPInputStream(r.getInputStream()), encoding);
                }
                else {
                    is = new InputStreamReader(r.getInputStream(), encoding);
                }
                br = new BufferedReader(is);
                bfs.add(br);
            }
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {

        DocumentMetaData md = new DocumentMetaData(aJCas);
        md.setDocumentTitle("");
        md.setDocumentId("" + (instanceId++));
        md.setLanguage(language);
        md.addToIndexes();

        try {
            posMappingProvider.configure(aJCas.getCas());
        }
        catch (AnalysisEngineProcessException e1) {
            throw new CollectionException(e1);
        }

        StringBuilder documentText = new StringBuilder();

        int seqStart = 0;
        for (int k = 0; k < sequences.size(); k++) {

            List<String> sequence = sequences.get(k);

            for (int i = 0; i < sequence.size(); i++) {
                String pairs = sequence.get(i).replaceAll(" +", " ");

                int idxLastSpace = pairs.lastIndexOf(" ");
                String token = pairs.substring(0, idxLastSpace);
                String tag = pairs.substring(idxLastSpace+1);

                int tokenLen = token.length();
                if(lowerCase){
                    token = token.toLowerCase();
                }
                documentText.append(token);

                int tokStart = documentText.length() - tokenLen;
                int tokEnd = documentText.length();
                Token t = new Token(aJCas, tokStart, tokEnd);
                t.addToIndexes();

                if (i + 1 < sequence.size()) {
                    documentText.append(" ");
                }

                Type posTag = posMappingProvider.getTagType(tag);
                POS pos = (POS) aJCas.getCas().createAnnotation(posTag, t.getBegin(),
                        t.getEnd());
                pos.setPosValue(tag);
                pos.addToIndexes();
                t.setPos(pos);
            }
            Sentence sentence = new Sentence(aJCas, seqStart, documentText.length());
            sentence.addToIndexes();

            if (k + 1 < sequences.size()) {
                documentText.append(" ");
            }
            seqStart = documentText.length();

        }
        aJCas.setDocumentText(documentText.toString());
    }

    public boolean hasNext()
        throws IOException, CollectionException
    {
        BufferedReader br = getBufferedReader();

        int readSeq = 0;
        sequences = new ArrayList<List<String>>();
        List<String> sequence = new ArrayList<>();
        String readLine = null;
        while ((readLine = br.readLine()) != null) {
            if (readLine.isEmpty()) {
                readSeq++;
                if (readSeq == seqLimit) {
                    break;
                }
                sequences.add(sequence);
                sequence = new ArrayList<>();
                continue;
            }
            sequence.add(readLine);
        }
        if (!sequence.isEmpty()) {
            sequences.add(sequence);
        }
        if (!sequences.isEmpty()) {
            return true;
        }

        return closeReaderOpenNext();

    }

    private boolean closeReaderOpenNext()
        throws CollectionException, IOException
    {
        try {
            bfs.get(currentReader).close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (currentReader + 1 < bfs.size()) {
            currentReader++;
            return hasNext();
        }
        return false;
    }

    private BufferedReader getBufferedReader()
    {
        return bfs.get(currentReader);
    }

    public Progress[] getProgress()
    {
        return null;
    }
}
