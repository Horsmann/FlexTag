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
package de.unidue.ltl.flextag.features;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IsEmoticonTest {
	
	@Test
	public void noEmoticon(){
		assertFalse(IsEmoticon.isEmoticon("."));
		assertFalse(IsEmoticon.isEmoticon(")"));
		assertFalse(IsEmoticon.isEmoticon("!?"));
		assertFalse(IsEmoticon.isEmoticon("d."));
		assertFalse(IsEmoticon.isEmoticon("00."));
	}
	
	@Test
	public void isFwd2CharEmoticon(){
		//fwd
		assertTrue(IsEmoticon.isEmoticon(":)"));
		assertTrue(IsEmoticon.isEmoticon(";)"));
		assertTrue(IsEmoticon.isEmoticon(":D"));
		assertTrue(IsEmoticon.isEmoticon(":p"));
		assertTrue(IsEmoticon.isEmoticon(":P"));
		assertTrue(IsEmoticon.isEmoticon("xD"));
		assertTrue(IsEmoticon.isEmoticon("XD"));
		assertTrue(IsEmoticon.isEmoticon(";D"));
		assertTrue(IsEmoticon.isEmoticon(":c"));
		assertTrue(IsEmoticon.isEmoticon(":x"));
	}
	
	@Test
	public void isFwd3CharEmoticon(){
		assertTrue(IsEmoticon.isEmoticon(":-)"));
		assertTrue(IsEmoticon.isEmoticon(":-)))))"));
		assertTrue(IsEmoticon.isEmoticon(";-)"));
		assertTrue(IsEmoticon.isEmoticon(";o)"));
		assertTrue(IsEmoticon.isEmoticon(":o)"));
		assertTrue(IsEmoticon.isEmoticon(":O)"));
		assertTrue(IsEmoticon.isEmoticon(":O))))"));
		assertTrue(IsEmoticon.isEmoticon(";O)"));
		assertTrue(IsEmoticon.isEmoticon("8-)"));
		assertTrue(IsEmoticon.isEmoticon(":-)"));
		assertTrue(IsEmoticon.isEmoticon(":-/"));
		assertTrue(IsEmoticon.isEmoticon(":-\\"));
	}

	@Test
	public void isBckwd2CharEmoticon(){
		assertTrue(IsEmoticon.isEmoticon("(:"));
		assertTrue(IsEmoticon.isEmoticon("(;"));
		assertTrue(IsEmoticon.isEmoticon("D:"));
	}
	
	@Test
	public void isBckwd3CharEmoticon(){
		assertTrue(IsEmoticon.isEmoticon("(-:"));
		assertTrue(IsEmoticon.isEmoticon("(-;"));
		assertTrue(IsEmoticon.isEmoticon("D-:"));
		assertTrue(IsEmoticon.isEmoticon("D-8"));
	}
	
	@Test
	public void isSurrogateEmoji(){
	    assertTrue(IsEmoticon.isEmoticon("😆"));
	    assertTrue(IsEmoticon.isEmoticon("😛"));
	    assertTrue(IsEmoticon.isEmoticon("🐺"));
	    assertTrue(IsEmoticon.isEmoticon("🤑"));
	    assertTrue(IsEmoticon.isEmoticon("💩"));
	}
	
	@Test
	public void isHorizontalEmoticon(){
	    assertTrue(IsEmoticon.isEmoticon("^^"));
		assertTrue(IsEmoticon.isEmoticon("-.-"));
		assertTrue(IsEmoticon.isEmoticon("*.*'"));
		assertTrue(IsEmoticon.isEmoticon("0.o"));
		assertTrue(IsEmoticon.isEmoticon("O.o"));
		assertTrue(IsEmoticon.isEmoticon("o.o"));
		assertTrue(IsEmoticon.isEmoticon("o.O"));
		assertTrue(IsEmoticon.isEmoticon("o.0"));
		assertTrue(IsEmoticon.isEmoticon("*.*"));
		assertTrue(IsEmoticon.isEmoticon("*_*"));
		assertTrue(IsEmoticon.isEmoticon("=.=\""));
		assertTrue(IsEmoticon.isEmoticon("(o.O)"));
		assertTrue(IsEmoticon.isEmoticon("(o.o)"));
		assertTrue(IsEmoticon.isEmoticon("(0.0)"));
		assertTrue(IsEmoticon.isEmoticon("(0.0)"));
	}
}
