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

public class IsNumTest {

	@Test
	public void runTest(){
		assertTrue(IsNumber.is("234"));
		assertTrue(IsNumber.is("3:23"));
		assertTrue(IsNumber.is("3293.78"));
		assertTrue(IsNumber.is("3,293.78"));
		assertTrue(IsNumber.is("3$"));
		assertTrue(IsNumber.is("3.2%"));
		assertTrue(IsNumber.is("332-22-2"));
		assertTrue(IsNumber.is("23/22/1"));
		
		assertFalse(IsNumber.is("hi!"));
		assertFalse(IsNumber.is("one"));
		assertFalse(IsNumber.is("two"));
	}
}
