package it.cnr.jconon.service.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import it.cnr.si.cool.jconon.CoolJcononApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by marco.spasiano on 22/11/16.
 */

@RunWith(SpringRunner.class )
@SpringBootTest(classes = CoolJcononApplication.class)
public class OIVScoreServiceTest {

    @Autowired
    private ApplicationOIVService applicationOIVService;

	public static GregorianCalendar 
			QUATTRO_INIZIO 		= new GregorianCalendar(2004,1,1),
			QUATTRO_FINE 		= new GregorianCalendar(2004,12,31),
			CINQUE_INIZIO 		= new GregorianCalendar(2005,1,1),
			CINQUE_FINE 		= new GregorianCalendar(2005,12,31),
			SEI_INIZIO 			= new GregorianCalendar(2006,1,1),
			SEI_FINE 			= new GregorianCalendar(2006,12,31),
			SETTE_INIZIO 		= new GregorianCalendar(2007,1,1),
			SETTE_FINE 			= new GregorianCalendar(2007,12,31),
			OTTO_INIZIO 		= new GregorianCalendar(2008,1,1),
			OTTO_FINE 			= new GregorianCalendar(2008,12,31),
			NOVE_INIZIO 		= new GregorianCalendar(2009,1,1),
			NOVE_FINE 			= new GregorianCalendar(2009,12,31),
			DIECI_INIZIO 		= new GregorianCalendar(2010,1,1),
			DIECI_FINE 			= new GregorianCalendar(2010,12,31),
			UNDICI_INIZIO 		= new GregorianCalendar(2011,1,1),
			UNDICI_FINE 		= new GregorianCalendar(2011,12,31),
			DODICI_INIZIO 		= new GregorianCalendar(2012,1,1),
			DODICI_FINE 		= new GregorianCalendar(2012,12,31),
			TREDICI_INIZIO 		= new GregorianCalendar(2013,1,1),
			TREDICI_FINE 		= new GregorianCalendar(2013,12,31),
			QUATTORDICI_INIZIO 	= new GregorianCalendar(2014,1,1),
			QUATTORDICI_FINE 	= new GregorianCalendar(2014,12,31),			
			QUINDICI_INIZIO 	= new GregorianCalendar(2015,1,1),
			QUINDICI_FINE 		= new GregorianCalendar(2015,12,31),			
			SEDICI_INIZIO 		= new GregorianCalendar(2016,1,1),
			SEDICI_FINE 		= new GregorianCalendar(2016,12,31);

	@Test
	public void testAssegnaFasciaA()  {
		assertNull(applicationOIVService.assegnaFascia(
				Arrays.asList(new Interval(TREDICI_INIZIO, SEDICI_FINE)), 
				Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)), 
				Collections.emptyList()));
	}

	@Test
	public void testAssegnaFasciaB()  {
		assertEquals(ApplicationOIVService.FASCIA1, 
				applicationOIVService.assegnaFascia(
						Collections.emptyList(), 
						Arrays.asList(new Interval(DODICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaC()  {
		assertEquals(ApplicationOIVService.FASCIA1, 
				applicationOIVService.assegnaFascia(
						Collections.emptyList(), 
						Arrays.asList(new Interval(DODICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}
	
	@Test
	public void testAssegnaFasciaD()  {
		assertEquals(ApplicationOIVService.FASCIA1, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(DODICI_INIZIO, SEDICI_FINE)), 
						Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaE()  {
		assertEquals(ApplicationOIVService.FASCIA1, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(DODICI_INIZIO, QUINDICI_FINE)), 
						Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaF()  {
		assertEquals(ApplicationOIVService.FASCIA1, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(NOVE_INIZIO, QUINDICI_FINE)), 
						Arrays.asList(new Interval(QUINDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaFBIS()  {
		assertEquals(ApplicationOIVService.FASCIA1, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(NOVE_INIZIO, SEDICI_FINE)), 
						Arrays.asList(new Interval(QUINDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaG()  {
		assertEquals(ApplicationOIVService.FASCIA1, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(CINQUE_INIZIO, SEDICI_FINE)), 
						Arrays.asList(new Interval(QUINDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaH()  {
		assertEquals(ApplicationOIVService.FASCIA2, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(NOVE_INIZIO, QUINDICI_FINE)), 
						Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaI()  {
		assertEquals(ApplicationOIVService.FASCIA2, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(NOVE_INIZIO, SEDICI_FINE)), 
						Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaJ()  {
		assertEquals(ApplicationOIVService.FASCIA2, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(OTTO_INIZIO, DODICI_FINE)), 
						Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaK()  {
		assertEquals(ApplicationOIVService.FASCIA3, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(CINQUE_INIZIO, SEDICI_FINE)), 
						Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaL()  {
		assertEquals(ApplicationOIVService.FASCIA3, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(CINQUE_INIZIO, TREDICI_FINE)), 
						Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}

	@Test
	public void testAssegnaFasciaM()  {
		assertEquals(ApplicationOIVService.FASCIA3, 
				applicationOIVService.assegnaFascia(
						Arrays.asList(new Interval(QUATTRO_INIZIO, DODICI_FINE)), 
						Arrays.asList(new Interval(QUATTORDICI_INIZIO, SEDICI_FINE)), 
						Collections.emptyList()
				)
		);
	}	

	@Test
	public void testAssegnaFasciaNonConsegutivi()  {
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
	public void testAssegnaFasciaNonConsegutiviCustom()  {
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