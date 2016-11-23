package it.cnr.jconon.service.application;

import it.cnr.si.cool.jconon.CoolJcononApplication;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by marco.spasiano on 22/11/16.
 */

@RunWith(SpringRunner.class )
@SpringBootTest(classes = CoolJcononApplication.class)
public class OIVScoreServiceTest {

    @Autowired
    private ApplicationOIVService applicationOIVService;

	private static List<Interval> oivPeriodSup250;

	private static List<Interval> oivPeriodInf250;


	@BeforeClass
	public static void before() {

		oivPeriodSup250 = Arrays.asList(
				new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)),
				new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2007,1,1))
		);


		oivPeriodInf250 = Arrays.asList(
				new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)),
				new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2007,1,1))

		);
	}

	@Test
	public void testAssegnaFasciaNull()  {

		List<Interval> esperienzePeriod = new ArrayList<Interval>();
		esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
		esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2004,1,1)));

		String fascia = applicationOIVService.assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250);
		assertNull(fascia);
	}


	@Test
	public void testAssegnaFascia1(){

		List<Interval> esperienzePeriod = new ArrayList<Interval>();
		esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
		esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2007,1,1)));

		String fascia = applicationOIVService.assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250);
		assertEquals(ApplicationOIVService.FASCIA1, fascia);
	}


	@Test
	public void testAssegnaFascia2() {

		List<Interval> esperienzePeriod = new ArrayList<>();
		esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
		esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2008,1,1)));

		String fascia = applicationOIVService.assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250);
		assertEquals(ApplicationOIVService.FASCIA2, fascia);
	}

	@Test
	public void testAssegnaFascia3(){

		List<Interval> esperienzePeriod = new ArrayList<>();
		esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
		esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2012,1,1)));

		String fascia = applicationOIVService.assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250);
		assertEquals(ApplicationOIVService.FASCIA3, fascia);
	}



}