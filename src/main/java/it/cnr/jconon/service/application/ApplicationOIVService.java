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
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationOIVService.class);

	private static final String JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A = "jconon_attachment:precedente_incarico_oiv_a";
	private static final String JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA = "jconon_attachment:precedente_incarico_oiv_da";
	private static final String JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DA = "jconon_attachment:esperienza_professionale_da", 
			JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A = "jconon_attachment:esperienza_professionale_a";
	private static final String JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE = "jconon_scheda_anonima:esperienza_professionale",
			JCONON_SCHEDA_ANONIMA_PRECEDENTE_INCARICO_OIV = "jconon_scheda_anonima:precedente_incarico_oiv";
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
		Long days = Long.valueOf(0);
		Criteria criteria = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE);
		criteria.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterable = criteria.executeQuery(adminSession, false, adminSession.getDefaultContext());
		for (QueryResult esperienza : iterable.getPage(Integer.MAX_VALUE)) {
			Calendar da = esperienza.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DA),
				a = esperienza.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A);
			days = days + Duration.between(da.toInstant(), a.toInstant()).toDays();
		}		

		Criteria criteriaOIV = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_PRECEDENTE_INCARICO_OIV);
		criteriaOIV.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterableOIV = criteriaOIV.executeQuery(adminSession, false, adminSession.getDefaultContext());
		for (QueryResult oiv : iterableOIV.getPage(Integer.MAX_VALUE)) {
			Calendar da = oiv.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA),
				a = oiv.<Calendar>getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A);
			days = days + Duration.between(da.toInstant(), a.toInstant()).toDays();
		}
		Calendar daOIV = (Calendar) aspectProperties.get("jconon_application:precedente_incarico_oiv_da"),
				aOIV = (Calendar) aspectProperties.get("jconon_application:precedente_incarico_oiv_a"),
				daEsperienza = (Calendar) aspectProperties.get("jconon_application:esperienza_professionale_da"),
				aEsperienza = (Calendar) aspectProperties.get("jconon_application:esperienza_professionale_a");
		if (daOIV != null && aOIV != null) {
			days = days + Duration.between(daOIV.toInstant(), aOIV.toInstant()).toDays();
		}
		if (daEsperienza != null && aEsperienza != null) {
			days = days + Duration.between(daEsperienza.toInstant(), aEsperienza.toInstant()).toDays();
		}
		LOGGER.info("DAYS", days);
		if (!days.equals(Long.valueOf(0)) ) {
			Long years = days/new Long(365);
			LOGGER.info("YEARS", years);
			if (years.intValue() > 12) {
				aspectProperties.put("jconon_application:fascia_professionale_attribuita", "1");
			} else if (years.intValue() > 8) {
				aspectProperties.put("jconon_application:fascia_professionale_attribuita", "2");				
			} else {
				aspectProperties.put("jconon_application:fascia_professionale_attribuita", "3");
			}
		}
	}
}
