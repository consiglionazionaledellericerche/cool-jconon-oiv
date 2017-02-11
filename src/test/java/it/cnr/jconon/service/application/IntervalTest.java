package it.cnr.jconon.service.application;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.junit.Test;

import it.cnr.si.cool.jconon.service.application.Interval;

import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

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

    public static Session getRepositorySession()
    {

        Map<String, String> sessionParameters = new HashMap<String, String>();
        sessionParameters.put(SessionParameter.ATOMPUB_URL, "http://alfresco-community.test.si.cnr.it/alfresco/api/-default-/public/cmis/versions/1.1/atom");
        sessionParameters.put("org.apache.chemistry.opencmis.binding.spi.type","atompub");
        sessionParameters.put(SessionParameter.USER, "admin");
        sessionParameters.put(SessionParameter.PASSWORD, "admin");
        sessionParameters.put(SessionParameter.REPOSITORY_ID, "-default-");
        Session session = SessionFactoryImpl.newInstance().createSession(sessionParameters);
        return session;
    }    
    public static void main(String[] args) {
		Session session = getRepositorySession();
		CmisObject utente = session.getObject("orkspace://SpacesStore/393e50a2-1a47-42f1-838a-fc0bee1a9bc5");
	
		session.bulkUpdateProperties(Collections.singletonList(utente), 
				Collections.singletonMap("fpperson:amministrazione", Arrays.asList("COMUNE DI BRUINO","Unione dei Comuni delle Valli Joniche dei Peloritani","Consiglio per la ricerca in agricoltura e l’analisi dell’economia agraria (CREA)")), 
				Collections.singletonList("P:fpperson:metadati"), Collections.emptyList()
		);		
		
	}
}