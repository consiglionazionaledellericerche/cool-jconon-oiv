package it.cnr.jconon.service.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import it.cnr.si.cool.jconon.CoolJcononApplication;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

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

    @Test
    public void eseguiCalcolo()  {
    	List<Interval> esperienzePeriod = new ArrayList<Interval>();
    	esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
    	esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2007,1,1)));
    	
    	List<Interval> oivPeriodSup250 = new ArrayList<Interval>();
    	oivPeriodSup250.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
    	oivPeriodSup250.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2007,1,1)));

    	List<Interval> oivPeriodInf250 = new ArrayList<Interval>();
    	oivPeriodInf250.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
    	oivPeriodInf250.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2007,1,1)));

    	assertEquals(ApplicationOIVService.FASCIA1, applicationOIVService.assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250));

    	esperienzePeriod.clear();
    	esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
    	esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2004,1,1)));
    	
    	assertNull(applicationOIVService.assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250));

    	esperienzePeriod.clear();
    	esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
    	esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2008,1,1)));
    	
    	assertEquals(ApplicationOIVService.FASCIA2, applicationOIVService.assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250));

    	esperienzePeriod.clear();
    	esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2003,1,1)));
    	esperienzePeriod.add(new Interval().startDate(new GregorianCalendar(2000,1,1)).endDate(new GregorianCalendar(2012,1,1)));
    	
    	assertEquals(ApplicationOIVService.FASCIA3, applicationOIVService.assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250));
    	
    }
}