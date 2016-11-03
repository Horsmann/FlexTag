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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ExampleUseModelTest
{
    @Test
    public void runExample()
        throws Exception
    {
        List<String> tags = new ExampleUseModel().run();
        assertEquals(5, tags.size());
        assertEquals("IN", tags.get(0));
        assertEquals("VBG", tags.get(1));
        assertEquals("IN", tags.get(2));
        assertEquals("NN", tags.get(3));
        assertEquals(".", tags.get(4));
    }

}
