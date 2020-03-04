package com.example.grocerylist3;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class MarketTest {
    Market testMarket1;

    @Before
    public void setUp() throws Exception {
        testMarket1 = new Market("Countdown", "Northlands", 2, true);
    }

    @After
    public void tearDown() throws Exception {
        testMarket1 = null;
    }

    @Test
    public void extractNameAndLocation() {
        String expectedName1 = "Countdown";
        String expectedLocation1 = "Northlands";

        String[] nameAndLocation1 = Market.extractNameAndLocation(testMarket1.toString());
        String outputName1 = nameAndLocation1[0];
        String outputLocation1 = nameAndLocation1[1];


        assertEquals(expectedName1, outputName1);
        assertEquals(expectedLocation1, outputLocation1);
    }

    @Test
    public void toString1() {
        String expected1 = "Countdown (Northlands)";

        String output1 = testMarket1.toString();

        assertEquals(expected1, output1);
    }
}