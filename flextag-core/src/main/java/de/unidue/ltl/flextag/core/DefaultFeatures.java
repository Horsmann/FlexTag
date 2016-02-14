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

import org.dkpro.tc.features.ngram.LuceneCharacterNGramUFE;
import org.dkpro.tc.features.tcu.CurrentUnit;
import org.dkpro.tc.features.tcu.NextUnit;
import org.dkpro.tc.features.tcu.PrevUnit;

import de.unidue.ltl.flextag.features.IsFirstLetterCapitalized;

public class DefaultFeatures
{
    
    public static String[] getDefaultFeatures(){
        return new String [] {
                // Context features
                PrevUnit.class.getName(), CurrentUnit.class.getName(), NextUnit.class.getName(),
                
                // boolean features
                IsFirstLetterCapitalized.class.getName(),
                
                // character ngrams - uses default values for min N, max N and top N char ngrams used
                LuceneCharacterNGramUFE.class.getName()
        };
    }

    public static Object [] getDefaultFeatureParameter()
    {
        return new Object [] {
                // We override the default configuration here
                LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MIN_N, 2,
                LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MAX_N, 4,
                LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_USE_TOP_K, 750 
        };
    }

}
