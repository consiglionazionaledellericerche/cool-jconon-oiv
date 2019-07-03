/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.jconon.service.application;

import it.cnr.si.cool.jconon.service.application.Interval;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by francesco on 23/11/16.
 */
public class IntervalTest {
    @Test
    public void hashCodeTest() throws Exception {

        Interval interval = new Interval(
                new GregorianCalendar(2000, 1, 1),
                new GregorianCalendar(2005, 1, 1)
        );
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

        assertEquals(1, intervalAfter.compareTo(intervalBefore));
        assertEquals(-1, intervalBefore.compareTo(intervalAfter));
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