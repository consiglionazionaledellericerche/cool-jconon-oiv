package it.cnr.jconon.service.application;

import it.cnr.si.cool.jconon.CoolJcononApplication;
import it.cnr.si.cool.jconon.service.application.ApplicationOIVService;
import it.cnr.si.cool.jconon.service.application.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by marco.spasiano on 22/11/16.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoolJcononApplication.class, properties = "spring.profiles.active=fp")
public class OIVScoreServiceTest {

    public static GregorianCalendar
            QUATTRO_INIZIO = new GregorianCalendar(2004, Calendar.JANUARY, 1),
            QUATTRO_FINE = new GregorianCalendar(2004, Calendar.DECEMBER, 31),
            CINQUE_INIZIO = new GregorianCalendar(2005, Calendar.JANUARY, 1),
            CINQUE_FINE = new GregorianCalendar(2005, Calendar.DECEMBER, 31),
            SEI_INIZIO = new GregorianCalendar(2006, Calendar.JANUARY, 1),
            SEI_FINE = new GregorianCalendar(2006, Calendar.DECEMBER, 31),
            SETTE_INIZIO = new GregorianCalendar(2007, Calendar.JANUARY, 1),
            SETTE_FINE = new GregorianCalendar(2007, Calendar.DECEMBER, 31),
            OTTO_INIZIO = new GregorianCalendar(2008, Calendar.JANUARY, 1),
            OTTO_FINE = new GregorianCalendar(2008, Calendar.DECEMBER, 31),
            NOVE_INIZIO = new GregorianCalendar(2009, Calendar.JANUARY, 1),
            NOVE_FINE = new GregorianCalendar(2009, Calendar.DECEMBER, 31),
            DIECI_INIZIO = new GregorianCalendar(2010, Calendar.JANUARY, 1),
            DIECI_FINE = new GregorianCalendar(2010, Calendar.DECEMBER, 31),
            UNDICI_INIZIO = new GregorianCalendar(2011, Calendar.JANUARY, 1),
            UNDICI_FINE = new GregorianCalendar(2011, Calendar.DECEMBER, 31),
            DODICI_INIZIO = new GregorianCalendar(2012, Calendar.JANUARY, 1),
            DODICI_FINE = new GregorianCalendar(2012, Calendar.DECEMBER, 31),
            TREDICI_INIZIO = new GregorianCalendar(2013, Calendar.JANUARY, 1),
            TREDICI_FINE = new GregorianCalendar(2013, Calendar.DECEMBER, 31),
            QUATTORDICI_INIZIO = new GregorianCalendar(2014, Calendar.JANUARY, 1),
            QUATTORDICI_FINE = new GregorianCalendar(2014, Calendar.DECEMBER, 31),
            QUINDICI_INIZIO = new GregorianCalendar(2015, Calendar.JANUARY, 1),
            QUINDICI_FINE = new GregorianCalendar(2015, Calendar.DECEMBER, 31),
            SEDICI_INIZIO = new GregorianCalendar(2016, Calendar.JANUARY, 1),
            SEDICI_FINE = new GregorianCalendar(2016, Calendar.DECEMBER, 31);
    @Autowired
    private ApplicationOIVService applicationOIVService;

    @Test
    public void testAssegnaFasciaNASCA() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(new GregorianCalendar(1986, Calendar.OCTOBER, 20), new GregorianCalendar(2017, Calendar.FEBRUARY, 7))),
                        Arrays.asList(new Interval(new GregorianCalendar(2014, Calendar.APRIL, 16), new GregorianCalendar(2017, Calendar.FEBRUARY, 7))),
                        Arrays.asList(new Interval(new GregorianCalendar(2016, Calendar.JANUARY, 7), new GregorianCalendar(2017, Calendar.FEBRUARY, 7)))
                )
        );
    }

    @Test
    public void testAssegnaFasciaCHIRICO() {
        assertNull(applicationOIVService.assegnaFascia(
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList(new Interval(new GregorianCalendar(2012, Calendar.JULY, 1), new GregorianCalendar(2017, Calendar.JANUARY, 18)))
                )
        );
    }

    @Test
    public void testAssegnaFasciaRUGGIERO() {
        assertEquals(ApplicationOIVService.FASCIA2,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(QUATTRO_INIZIO, SEDICI_FINE)),
                        Arrays.asList(new Interval(new GregorianCalendar(2010, Calendar.APRIL, 30), new GregorianCalendar(2012, Calendar.DECEMBER, 17))),
                        Arrays.asList(new Interval(new GregorianCalendar(2014, Calendar.JUNE, 1), new GregorianCalendar(2017, Calendar.JANUARY, 16)))
                )
        );
    }

    @Test
    public void testAssegnaFasciaCANITANO() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(NOVE_INIZIO, QUINDICI_FINE)),
                        Collections.emptyList(),
                        Arrays.asList(new Interval(new GregorianCalendar(2014, Calendar.MAY, 22), new GregorianCalendar(2017, Calendar.JANUARY, 11)))
                )
        );
    }

    @Test
    public void testAssegnaFasciaA() {
        assertNull(applicationOIVService.assegnaFascia(
                Arrays.asList(new Interval(TREDICI_INIZIO, SEDICI_FINE)),
                Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)),
                Collections.emptyList()));
    }

    @Test
    public void testAssegnaFasciaB() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Collections.emptyList(),
                        Arrays.asList(new Interval(DODICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaC() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Collections.emptyList(),
                        Arrays.asList(new Interval(DODICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaD() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(DODICI_INIZIO, SEDICI_FINE)),
                        Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaE() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(DODICI_INIZIO, QUINDICI_FINE)),
                        Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaF() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(NOVE_INIZIO, QUINDICI_FINE)),
                        Arrays.asList(new Interval(QUINDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaFBIS() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(NOVE_INIZIO, SEDICI_FINE)),
                        Arrays.asList(new Interval(QUINDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaG() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(CINQUE_INIZIO, SEDICI_FINE)),
                        Arrays.asList(new Interval(QUINDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaH() {
        assertEquals(ApplicationOIVService.FASCIA2,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(NOVE_INIZIO, QUINDICI_FINE)),
                        Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaI() {
        assertEquals(ApplicationOIVService.FASCIA2,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(NOVE_INIZIO, SEDICI_FINE)),
                        Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaJ() {
        assertEquals(ApplicationOIVService.FASCIA2,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(OTTO_INIZIO, DODICI_FINE)),
                        Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaK() {
        assertEquals(ApplicationOIVService.FASCIA3,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(CINQUE_INIZIO, SEDICI_FINE)),
                        Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaL() {
        assertEquals(ApplicationOIVService.FASCIA3,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(CINQUE_INIZIO, TREDICI_FINE)),
                        Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaM() {
        assertEquals(ApplicationOIVService.FASCIA3,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(new Interval(QUATTRO_INIZIO, DODICI_FINE)),
                        Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaNonConsegutivi() {
        assertEquals(ApplicationOIVService.FASCIA1,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(
                                new Interval(SEI_INIZIO, SEI_FINE),
                                new Interval(DIECI_INIZIO, DODICI_FINE),
                                new Interval(QUATTORDICI_INIZIO, QUATTORDICI_FINE)
                        ),
                        Arrays.asList(
                                new Interval(UNDICI_INIZIO, UNDICI_FINE),
                                new Interval(QUINDICI_INIZIO, SEDICI_FINE)
                        ),
                        Collections.emptyList()
                )
        );
    }

    @Test
    public void testAssegnaFasciaNonConsegutiviCustom() {
        assertEquals(ApplicationOIVService.FASCIA2,
                applicationOIVService.assegnaFascia(
                        Arrays.asList(
                                new Interval(SEI_INIZIO, SEI_FINE),
                                new Interval(DIECI_INIZIO, DODICI_FINE),
                                new Interval(QUATTORDICI_INIZIO, QUATTORDICI_FINE)
                        ),
                        Arrays.asList(
                                new Interval(OTTO_INIZIO, OTTO_FINE),
                                new Interval(QUINDICI_INIZIO, SEDICI_FINE)
                        ),
                        Collections.emptyList()
                )
        );
    }

}