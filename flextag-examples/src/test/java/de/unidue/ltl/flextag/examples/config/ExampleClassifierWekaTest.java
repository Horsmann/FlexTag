package de.unidue.ltl.flextag.examples.config;

import org.junit.Test;

public class ExampleClassifierWekaTest
{
    @Test
    public void testWekaSimple() throws Exception
    {
        new ExampleClassifierWeka().runSimple();
    }
    
    @Test
    public void testWekaComplex() throws Exception
    {
        new ExampleClassifierWeka().runComplex();
    }

}
