package de.unidue.ltl.flextag.core.uima;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationUnit;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TcPosTaggingWrapper
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_USE_COARSE_GRAINED = "useCoarseGrained";
    @ConfigurationParameter(name = PARAM_USE_COARSE_GRAINED, mandatory = true, defaultValue = "false")
    protected boolean useCoarseGrained;

    int tcId = 0;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        for (Sentence sent : JCasUtil.select(aJCas, Sentence.class)) {
            TextClassificationSequence sequence = new TextClassificationSequence(aJCas,
                    sent.getBegin(), sent.getEnd());
            sequence.addToIndexes();

            List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, sent);

            for (Token token : tokens) {
                TextClassificationUnit unit = new TextClassificationUnit(aJCas, token.getBegin(),
                        token.getEnd());
                unit.setId(tcId++);
                unit.setSuffix(token.getCoveredText());
                unit.addToIndexes();

                TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas,
                        token.getBegin(), token.getEnd());
                outcome.setOutcome(getTextClassificationOutcome(aJCas, unit));
                outcome.addToIndexes();
            }

        }
    }

    public String getTextClassificationOutcome(JCas jcas, TextClassificationUnit unit)
    {
        List<POS> posList = JCasUtil.selectCovered(jcas, POS.class, unit);

        String outcome = "";
        if (useCoarseGrained) {
            outcome = posList.get(0).getClass().getSimpleName();
        }
        else {
            outcome = posList.get(0).getPosValue();
        }
        return outcome;
    }

}
