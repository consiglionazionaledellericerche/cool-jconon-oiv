package it.cnr.jconon.service.application;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.service.I18nService;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class ApplicationOIVService extends ApplicationService{
	private static final String INF250 = "<250", SUP250=">250";

	private static final String JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI = "jconon_attachment:precedente_incarico_oiv_numero_dipendenti";
	private static final String JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI = "jconon_application:precedente_incarico_oiv_numero_dipendenti";

	private static final String JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA = "jconon_application:fascia_professionale_attribuita";

	private static final String JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_A = "jconon_application:esperienza_professionale_a";

	private static final String JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_DA = "jconon_application:esperienza_professionale_da";

	private static final String JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_A = "jconon_application:precedente_incarico_oiv_a";

	private static final String JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_DA = "jconon_application:precedente_incarico_oiv_da";

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationOIVService.class);

	private static final String JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A = "jconon_attachment:precedente_incarico_oiv_a";
	private static final String JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA = "jconon_attachment:precedente_incarico_oiv_da";
	private static final String JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DA = "jconon_attachment:esperienza_professionale_da", 
			JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A = "jconon_attachment:esperienza_professionale_a";
	private static final String JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE = "jconon_scheda_anonima:esperienza_professionale",
			JCONON_SCHEDA_ANONIMA_PRECEDENTE_INCARICO_OIV = "jconon_scheda_anonima:precedente_incarico_oiv";
	
	private static final String FASCIA1 = "1", FASCIA2 = "2", FASCIA3 = "3";
	@Autowired
	private CMISService cmisService;
	@Autowired
	private I18nService i18nService;
	

	@Override
	public Folder save(Session currentCMISSession,
			String contextURL, Locale locale,
			String userId, Map<String, Object> properties,
			Map<String, Object> aspectProperties) {
		eseguiCalcolo(properties, aspectProperties);
		return super.save(currentCMISSession, contextURL, locale, userId, properties, aspectProperties);
	}
	
	@Override
	public Map<String, String> sendApplication(Session currentCMISSession, final String applicationSourceId, final String contextURL, 
			final Locale locale, String userId, Map<String, Object> properties, Map<String, Object> aspectProperties) {
		eseguiCalcolo(properties, aspectProperties);
		if (aspectProperties.get(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA) == null)
			throw new ClientMessageException(
					i18nService.getLabel("message.error.domanda.fascia", Locale.ITALIAN));			
		return super.sendApplication(currentCMISSession, applicationSourceId, contextURL, locale, userId, properties, aspectProperties);
	}
	
	private void eseguiCalcolo(Map<String, Object> properties, Map<String, Object> aspectProperties) {
		Session adminSession = cmisService.createAdminSession();
		Folder application = (Folder) adminSession.getObject((String) properties.get(PropertyIds.OBJECT_ID));
		List<Interval> esperienzePeriod = new ArrayList<Interval>(), oivPeriodSup250 = new ArrayList<Interval>(), oivPeriodInf250 = new ArrayList<Interval>();
		
		Long daysEsperienza = Long.valueOf(0), daysOIVInf250 = Long.valueOf(0), daysOIVSup250 = Long.valueOf(0);
		Criteria criteria = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE);
		criteria.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterable = criteria.executeQuery(adminSession, false, adminSession.getDefaultContext());
		for (QueryResult esperienza : iterable.getPage(Integer.MAX_VALUE)) {
			Calendar da = esperienza.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DA),
				a = esperienza.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A);
			esperienzePeriod.add(INTERVAL(da, a));
		}		

		Criteria criteriaOIV = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_PRECEDENTE_INCARICO_OIV);
		criteriaOIV.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterableOIV = criteriaOIV.executeQuery(adminSession, false, adminSession.getDefaultContext());
		for (QueryResult oiv : iterableOIV.getPage(Integer.MAX_VALUE)) {
			Calendar da = oiv.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA),
				a = oiv.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A);
			if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(INF250)) {
				oivPeriodInf250.add(INTERVAL(da, a));
			} else if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(SUP250)) {
				oivPeriodSup250.add(INTERVAL(da, a));
			}
		}
		Calendar daOIV = (Calendar) aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_DA),
				aOIV = (Calendar) aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_A),
				daEsperienza = (Calendar) aspectProperties.get(JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_DA),
				aEsperienza = (Calendar) aspectProperties.get(JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_A);				
		if (daOIV != null && aOIV != null) {
			if (aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(INF250)) {
				oivPeriodInf250.add(INTERVAL(daOIV, aOIV));
			} else if (aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(SUP250)) {
				oivPeriodSup250.add(INTERVAL(daOIV, aOIV));
			}
		}
		if (daEsperienza != null && aEsperienza != null) {
			esperienzePeriod.add(INTERVAL(daEsperienza, aEsperienza));
		}
		esperienzePeriod = overlapping(esperienzePeriod);
		oivPeriodSup250 = overlapping(oivPeriodSup250);
		oivPeriodInf250 = overlapping(oivPeriodInf250);
		LOGGER.info("esperienzePeriod: {}", esperienzePeriod);
		LOGGER.info("oivPeriodSup250: {}", oivPeriodSup250);
		LOGGER.info("oivPeriodInf250: {}", oivPeriodInf250);
		for (Interval interval : esperienzePeriod) {
			daysEsperienza = daysEsperienza + Duration.between(interval.startDate, interval.endDate).toDays();
		}
		for (Interval interval : oivPeriodInf250) {
			daysOIVInf250 = daysOIVInf250 + Duration.between(interval.startDate, interval.endDate).toDays();
		}
		for (Interval interval : oivPeriodSup250) {
			daysOIVSup250 = daysOIVSup250 + Duration.between(interval.startDate, interval.endDate).toDays();
		}

		LOGGER.info("Days Esperienza: {}", daysEsperienza);
		LOGGER.info("Days OIV Inf 250: {}", daysOIVInf250);
		LOGGER.info("Days OIV Sup 250: {}", daysOIVSup250);

		if (!daysEsperienza.equals(Long.valueOf(0)) ) {
			Long years = daysEsperienza/new Long(365),
					yearsOIVINF250 = daysOIVInf250/new Long(365),
					yearsOIVSUP250 = daysOIVSup250/new Long(365);
			LOGGER.info("YEARS: {}", years);
			if (years >= 12) {
				if (yearsOIVSUP250 >= 3) {
					aspectProperties.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, FASCIA3);
					return;
				}
			} 
			if (years.intValue() >= 8) {
				if (yearsOIVINF250 + yearsOIVSUP250 >= 3) {
					aspectProperties.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, FASCIA2);
					return;
				}
			}
			if (years.intValue() >= 5) {
				aspectProperties.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, FASCIA1);
				return;
			}
			aspectProperties.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, null);
		}
	}

	public static Interval INTERVAL(Calendar startDate, Calendar endDate){
		return new Interval(startDate.toInstant(), endDate.toInstant());
	}
	
	private static class Interval implements Comparable<Interval>{
		private final Instant startDate;
		private final Instant endDate;
				
		public Interval(Instant startDate, Instant endDate) {
			super();
			this.startDate = startDate;
			this.endDate = endDate;
		}

		@Override
		public int compareTo(Interval o) {
			if (o.startDate.isAfter(startDate))
				return -1;
			if (o.startDate.isBefore(startDate))
				return 1;			
			return 0;
		}
		@Override
	    public String toString() {
	        return startDate + ".." + endDate;
	    }		
	}
	
	private List<Interval> overlapping(List<Interval> source) {
		Collections.sort(source);
		List<Interval> result = new ArrayList<Interval>();
		for (Interval interval : source) {
			if (result.isEmpty()) {
				result.add(interval);
			} else {
				Interval lastInsert = result.get(result.size() - 1);
				if (interval.startDate.isAfter(lastInsert.startDate) && interval.endDate.isBefore(lastInsert.endDate))
					continue;
				if (interval.startDate.isAfter(lastInsert.startDate) && interval.startDate.isBefore(lastInsert.endDate) && interval.endDate.isAfter(lastInsert.endDate)) {
					result.add(new Interval(lastInsert.startDate, interval.endDate));					
					result.remove(lastInsert);
				} else {
					result.add(interval);
				}
				
			}
		}
		Collections.sort(result);
		return result;
	}
}