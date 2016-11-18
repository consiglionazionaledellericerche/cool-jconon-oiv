package it.cnr.jconon.service.application;

import it.cnr.cool.cmis.service.CMISService;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.time.Duration;
import java.util.Calendar;
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

@Component("applicationService")
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
		return super.sendApplication(currentCMISSession, applicationSourceId, contextURL, locale, userId, properties, aspectProperties);
	}
	
	private void eseguiCalcolo(Map<String, Object> properties, Map<String, Object> aspectProperties) {
		Session adminSession = cmisService.createAdminSession();
		Folder application = (Folder) adminSession.getObject((String) properties.get(PropertyIds.OBJECT_ID));
		Long daysEsperienza = Long.valueOf(0), daysOIVInf250 = Long.valueOf(0), daysOIVSup250 = Long.valueOf(0);
		Criteria criteria = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE);
		criteria.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterable = criteria.executeQuery(adminSession, false, adminSession.getDefaultContext());
		for (QueryResult esperienza : iterable.getPage(Integer.MAX_VALUE)) {
			Calendar da = esperienza.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DA),
				a = esperienza.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A);
			daysEsperienza = daysEsperienza + Duration.between(da.toInstant(), a.toInstant()).toDays();
		}		

		Criteria criteriaOIV = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_PRECEDENTE_INCARICO_OIV);
		criteriaOIV.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterableOIV = criteriaOIV.executeQuery(adminSession, false, adminSession.getDefaultContext());
		for (QueryResult oiv : iterableOIV.getPage(Integer.MAX_VALUE)) {
			Calendar da = oiv.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA),
				a = oiv.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A);
			if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(INF250)) {
				daysOIVInf250 = daysOIVInf250 + Duration.between(da.toInstant(), a.toInstant()).toDays();				
			} else if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(SUP250)) {
				daysOIVSup250 = daysOIVSup250 + Duration.between(da.toInstant(), a.toInstant()).toDays();
			}
		}
		Calendar daOIV = (Calendar) aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_DA),
				aOIV = (Calendar) aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_A),
				daEsperienza = (Calendar) aspectProperties.get(JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_DA),
				aEsperienza = (Calendar) aspectProperties.get(JCONON_APPLICATION_ESPERIENZA_PROFESSIONALE_A);				
		if (daOIV != null && aOIV != null) {
			if (aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(INF250)) {
				daysOIVInf250 = daysOIVInf250 + Duration.between(daOIV.toInstant(), aOIV.toInstant()).toDays();
			} else if (aspectProperties.get(JCONON_APPLICATION_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(SUP250)) {
				daysOIVSup250 = daysOIVSup250 + Duration.between(daOIV.toInstant(), aOIV.toInstant()).toDays();				
			}
		}
		if (daEsperienza != null && aEsperienza != null) {
			daysEsperienza = daysEsperienza + Duration.between(daEsperienza.toInstant(), aEsperienza.toInstant()).toDays();
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
			aspectProperties.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, "");
		}
	}
}
