package it.cnr.jconon.service.application;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.service.I18nService;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

	public static final String 
		JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI = "jconon_attachment:precedente_incarico_oiv_numero_dipendenti",
		JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI = "jconon_application:precedente_incarico_oiv_numero_dipendenti",
		JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA = "jconon_application:fascia_professionale_attribuita",
		JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_A = "jconon_application:esperienza_professionale_a",
		JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_DA = "jconon_application:esperienza_professionale_da",
		JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_A = "jconon_application:precedente_incarico_oiv_a",
		JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_DA = "jconon_application:precedente_incarico_oiv_da",
		JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A = "jconon_attachment:precedente_incarico_oiv_a",
		JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA = "jconon_attachment:precedente_incarico_oiv_da",
		JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DA = "jconon_attachment:esperienza_professionale_da", 
		JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A = "jconon_attachment:esperienza_professionale_a",
		JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE = "jconon_scheda_anonima:esperienza_professionale",
		JCONON_SCHEDA_ANONIMA_PRECEDENTE_INCARICO_OIV = "jconon_scheda_anonima:precedente_incarico_oiv";

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationOIVService.class);

	
	public static final String FASCIA1 = "1", FASCIA2 = "2", FASCIA3 = "3";
	
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
	
	public void eseguiCalcolo(Map<String, Object> properties, Map<String, Object> aspectProperties) {
		Session adminSession = cmisService.createAdminSession();
		Folder application = (Folder) adminSession.getObject((String) properties.get(PropertyIds.OBJECT_ID));
		List<Interval> esperienzePeriod = new ArrayList<Interval>(), oivPeriodSup250 = new ArrayList<Interval>(), oivPeriodInf250 = new ArrayList<Interval>();
		Criteria criteria = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE);
		criteria.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterable = criteria.executeQuery(adminSession, false, adminSession.getDefaultContext());
		for (QueryResult esperienza : iterable.getPage(Integer.MAX_VALUE)) {
			Calendar da = esperienza.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DA),
				a = esperienza.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A);
			esperienzePeriod.add(new Interval().startDate(da).endDate(a));
		}		

		Criteria criteriaOIV = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_PRECEDENTE_INCARICO_OIV);
		criteriaOIV.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterableOIV = criteriaOIV.executeQuery(adminSession, false, adminSession.getDefaultContext());
		for (QueryResult oiv : iterableOIV.getPage(Integer.MAX_VALUE)) {
			Calendar da = oiv.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA),
				a = oiv.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A);
			if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(INF250)) {
				oivPeriodInf250.add(new Interval().startDate(da).endDate(a));
			} else if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(SUP250)) {
				oivPeriodSup250.add(new Interval().startDate(da).endDate(a));
			}
		}
		Calendar daOIV = (Calendar) aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_DA),
				aOIV = (Calendar) aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_A),
				daEsperienza = (Calendar) aspectProperties.get(JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_DA),
				aEsperienza = (Calendar) aspectProperties.get(JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_A);				
		if (daOIV != null && aOIV != null) {
			if (aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(INF250)) {
				oivPeriodInf250.add(new Interval().startDate(daOIV).endDate(aOIV));
			} else if (aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(SUP250)) {
				oivPeriodSup250.add(new Interval().startDate(daOIV).endDate(aOIV));
			}
		}
		if (daEsperienza != null && aEsperienza != null) {
			esperienzePeriod.add(new Interval().startDate(daEsperienza).endDate(aEsperienza));
		}
		aspectProperties.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250));		
	}

	public String assegnaFascia(List<Interval> esperienzePeriod, List<Interval> oivPeriodSup250, List<Interval> oivPeriodInf250) {
		Long daysEsperienza = Long.valueOf(0), daysOIVInf250 = Long.valueOf(0), daysOIVSup250 = Long.valueOf(0);		
		esperienzePeriod = overlapping(esperienzePeriod);
		oivPeriodSup250 = overlapping(oivPeriodSup250);
		oivPeriodInf250 = overlapping(oivPeriodInf250);
		LOGGER.info("esperienzePeriod: {}", esperienzePeriod);
		LOGGER.info("oivPeriodSup250: {}", oivPeriodSup250);
		LOGGER.info("oivPeriodInf250: {}", oivPeriodInf250);
		for (Interval interval : esperienzePeriod) {
			daysEsperienza = daysEsperienza + Duration.between(interval.getStartDate(), interval.getEndDate()).toDays();
		}
		for (Interval interval : oivPeriodInf250) {
			daysOIVInf250 = daysOIVInf250 + Duration.between(interval.getStartDate(), interval.getEndDate()).toDays();
		}
		for (Interval interval : oivPeriodSup250) {
			daysOIVSup250 = daysOIVSup250 + Duration.between(interval.getStartDate(), interval.getEndDate()).toDays();
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
					return FASCIA3;
				}
			} 
			if (years.intValue() >= 8) {
				if (yearsOIVINF250 + yearsOIVSUP250 >= 3) {
					return FASCIA2;
				}
			}
			if (years.intValue() >= 5) {
				return FASCIA1;
			}
		}
		return null;		
	}
	
	private List<Interval> overlapping(List<Interval> source) {
		source.stream().forEach(interval ->  {
			if (interval.getStartDate().isAfter(interval.getEndDate())) {
				throw new ClientMessageException(
						i18nService.getLabel("message.error.date.inconsistent", Locale.ITALIAN,  
								DateTimeFormatter.ofPattern("dd/MM/yyyy").format(ZonedDateTime.ofInstant(interval.getStartDate(), ZoneId.systemDefault())), 
								DateTimeFormatter.ofPattern("dd/MM/yyyy").format(ZonedDateTime.ofInstant(interval.getEndDate(), ZoneId.systemDefault()))));				
			}
		});
		Collections.sort(source);
		List<Interval> result = new ArrayList<Interval>();
		for (Interval interval : source) {
			if (result.isEmpty()) {
				result.add(interval);
			} else {
				Interval lastInsert = result.get(result.size() - 1);
				if (!interval.getEndDate().isAfter(lastInsert.getEndDate()))
					continue;
				if (!interval.getStartDate().isAfter(lastInsert.getEndDate()) && !interval.getEndDate().isBefore(lastInsert.getEndDate())) {
					result.add(new Interval(lastInsert.getStartDate(), interval.getEndDate()));
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