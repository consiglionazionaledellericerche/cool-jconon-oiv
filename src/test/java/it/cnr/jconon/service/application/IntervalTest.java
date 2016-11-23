package it.cnr.jconon.service.application;

import org.junit.Test;

import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by francesco on 23/11/16.
 */
public class IntervalTest {
    @Test
    public void hashCodeTest() throws Exception {

        Interval interval = new Interval(new GregorianCalendar(2000, 1, 1),
                new GregorianCalendar(2005, 1, 1));

        assertEquals(472588928, interval.hashCode());

    }

    @Test
    public void compareTo() throws Exception {
        Interval interval1 = new Interval(new GregorianCalendar(2000, 1, 1),
                new GregorianCalendar(2005, 1, 1));

        Interval sameInterval = new Interval(new GregorianCalendar(2000, 1, 1),
                new GregorianCalendar(2005, 1, 1));

        assertEquals(0, interval1.compareTo(sameInterval));

    }

    @Test
    public void compareToDifferent() throws Exception {

        Interval intervalBefore = new Interval(new GregorianCalendar(2000, 1, 1),
                new GregorianCalendar(2005, 1, 1));

        Interval intervalAfter = new Interval(new GregorianCalendar(2009, 1, 1),
                new GregorianCalendar(2011, 1, 1));

        assertEquals(1, intervalAfter.compareTo(intervalBefore) );
        assertEquals(-1, intervalBefore.compareTo(intervalAfter) );
    }




    @Test
    public void equals() throws Exception {

        Interval interval1 = new Interval(new GregorianCalendar(2000, 1, 1),
                new GregorianCalendar(2005, 1, 1));

        Interval sameInterval = new Interval(new GregorianCalendar(2000, 1, 1),
                new GregorianCalendar(2005, 1, 1));

        assertTrue(interval1.equals(sameInterval));
    }

}