package it.cnr.si.cool.jconon.service.application;

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.mail.MailService;
import it.cnr.cool.mail.model.AttachmentBean;
import it.cnr.cool.mail.model.EmailMessage;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.I18nService;
import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.cmis.model.JCONONDocumentType;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.model.ApplicationModel;
import it.cnr.si.cool.jconon.model.PrintParameterModel;
import it.cnr.si.cool.jconon.service.PrintService;
import it.cnr.si.cool.jconon.service.QueueService;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Component
@Primary
public class ApplicationOIVService extends ApplicationService{

	private static final String INF250 = "<250", SUP250=">=250";

	public static final String 
		JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI = "jconon_attachment:precedente_incarico_oiv_numero_dipendenti",
		JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA = "jconon_application:fascia_professionale_attribuita",
		JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A = "jconon_attachment:precedente_incarico_oiv_a",
		JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA = "jconon_attachment:precedente_incarico_oiv_da",
		JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DA = "jconon_attachment:esperienza_professionale_da", 
		JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A = "jconon_attachment:esperienza_professionale_a",
		JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE = "jconon_scheda_anonima:esperienza_professionale",
		JCONON_SCHEDA_ANONIMA_PRECEDENTE_INCARICO_OIV = "jconon_scheda_anonima:precedente_incarico_oiv";
	private static final BigDecimal DAYSINYEAR = BigDecimal.valueOf(365);
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationOIVService.class);

	
	public static final String FASCIA1 = "1", FASCIA2 = "2", FASCIA3 = "3";
	
	@Autowired
	private CMISService cmisService;
	@Autowired
	private I18nService i18nService;
	@Autowired
	private CommonsMultipartResolver resolver;
    @Autowired
    private QueueService queueService;
    @Autowired
    private PrintService printService;
    @Autowired	
	private ApplicationContext context;
	@Autowired
	private MailService mailService;
	@Value("${mail.from.default}")
	private String mailFromDefault;
	
	@Override
	public Folder save(Session currentCMISSession,
			String contextURL, Locale locale,
			String userId, Map<String, Object> properties,
			Map<String, Object> aspectProperties) {
		String objectId = (String) properties.get(PropertyIds.OBJECT_ID);
		eseguiCalcolo(objectId, aspectProperties);
		return super.save(currentCMISSession, contextURL, locale, userId, properties, aspectProperties);
	}
	
	@Override
	public Map<String, String> sendApplication(Session currentCMISSession, final String applicationSourceId, final String contextURL, 
			final Locale locale, String userId, Map<String, Object> properties, Map<String, Object> aspectProperties) {
		String objectId = (String) properties.get(PropertyIds.OBJECT_ID);
		eseguiCalcolo(objectId, aspectProperties);
		Optional.ofNullable(aspectProperties.get(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA)).orElseThrow(() -> new ClientMessageException(
				i18nService.getLabel("message.error.domanda.fascia", Locale.ITALIAN)));		
		return super.sendApplication(currentCMISSession, applicationSourceId, contextURL, locale, userId, properties, aspectProperties);
	}
	
	public void eseguiCalcolo(String objectId, Map<String, Object> aspectProperties) {
		Session adminSession = cmisService.createAdminSession();
		Folder application = (Folder) adminSession.getObject(objectId);
		List<Interval> oivPeriodSup250 = new ArrayList<>(), oivPeriodInf250 = new ArrayList<>();
		List<Interval> esperienzePeriod =  esperienzePeriod(getQueryResultEsperienza(adminSession, application));
		ItemIterable<QueryResult> queryResultsOiv = getQueryResultsOiv(adminSession, application);
		for (QueryResult oiv : queryResultsOiv) {
			Calendar da = oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA),
				a = oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A);
			if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(INF250)) {
				oivPeriodInf250.add(new Interval(da, a));
			} else if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(SUP250)) {
				oivPeriodSup250.add(new Interval(da, a));
			}
		}
		String fascia = assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250);
		LOGGER.info("fascia attribuita a {}: {}", objectId, fascia);
		aspectProperties.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, fascia);
	}

	private List<Interval> esperienzePeriod(ItemIterable<QueryResult> queryResultEsperienza) {
		List<Interval> esperienzePeriod = new ArrayList<>();
		for (QueryResult esperienza : queryResultEsperienza) {
			Calendar da = esperienza.getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DA),
				a = esperienza.getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A);
			esperienzePeriod.add(new Interval(da, a));
		}
		return esperienzePeriod;
	}

	private ItemIterable<QueryResult> getQueryResultsOiv(Session adminSession, Folder application) {
		Criteria criteriaOIV = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_PRECEDENTE_INCARICO_OIV);
		criteriaOIV.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterableOIV = criteriaOIV.executeQuery(adminSession, false, adminSession.getDefaultContext());
		return iterableOIV.getPage(Integer.MAX_VALUE);
	}

	private ItemIterable<QueryResult> getQueryResultEsperienza(Session adminSession, Folder application) {
		Criteria criteria = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE);
		criteria.add(Restrictions.inFolder(application.getId()));
		ItemIterable<QueryResult> iterable = criteria.executeQuery(adminSession, false, adminSession.getDefaultContext());
		return iterable.getPage(Integer.MAX_VALUE);
	}

	public String assegnaFascia(final List<Interval> esperienzePeriodList, final List<Interval> oivPeriodSup250List, final List<Interval> oivPeriodInf250List) {
		BigDecimal daysEsperienza = BigDecimal.ZERO, daysOIVInf250 = BigDecimal.ZERO, daysOIVSup250 = BigDecimal.ZERO;
		/**
		 * Per il calcolo dell'esperienza bisogna tener conto anche dell'esperienza OIV
		 */
		List<Interval> periodo = new ArrayList<Interval>();
		periodo.addAll(esperienzePeriodList);
		periodo.addAll(oivPeriodSup250List);
		periodo.addAll(oivPeriodInf250List);
		
		List<Interval> esperienzePeriod = overlapping(periodo);
		List<Interval> oivPeriodSup250 = overlapping(oivPeriodSup250List);
		List<Interval> oivPeriodInf250 = overlapping(oivPeriodInf250List);
		LOGGER.info("esperienzePeriod: {}", esperienzePeriod);
		LOGGER.info("oivPeriodSup250: {}", oivPeriodSup250);
		LOGGER.info("oivPeriodInf250: {}", oivPeriodInf250);
		for (Interval interval : esperienzePeriod) {
			daysEsperienza = daysEsperienza.add(BigDecimal.valueOf(Duration.between(interval.getStartDate(), interval.getEndDate()).toDays()));
		}
		for (Interval interval : oivPeriodInf250) {
			daysOIVInf250 = daysOIVInf250.add(BigDecimal.valueOf(Duration.between(interval.getStartDate(), interval.getEndDate()).toDays()));
		}
		for (Interval interval : oivPeriodSup250) {
			daysOIVSup250 = daysOIVSup250.add(BigDecimal.valueOf(Duration.between(interval.getStartDate(), interval.getEndDate()).toDays()));
		}

		return getFascia(daysEsperienza, daysOIVInf250, daysOIVSup250);
	}

	private String getFascia(final BigDecimal daysEsperienza, final BigDecimal daysOIVInf250, final BigDecimal daysOIVSup250) {
		LOGGER.info("Days Esperienza: {}", daysEsperienza);
		LOGGER.info("Days OIV Inf 250: {}", daysOIVInf250);
		LOGGER.info("Days OIV Sup 250: {}", daysOIVSup250);

		if (!Long.valueOf(0).equals(daysEsperienza) ) {
			Long years = 
					daysEsperienza.divide(DAYSINYEAR, RoundingMode.HALF_UP).longValue(),
					yearsOIVINF250 = daysOIVInf250.divide(DAYSINYEAR, RoundingMode.HALF_UP).longValue(),
					yearsOIVSUP250 = daysOIVSup250.divide(DAYSINYEAR, RoundingMode.HALF_UP).longValue();
			LOGGER.info("YEARS: {}", years);
			if (years >= 12 && yearsOIVSUP250 >= 3) {
				return FASCIA3;
			}
			if (years.intValue() >= 8 && yearsOIVINF250 + yearsOIVSUP250 >= 3) {
				return FASCIA2;
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

	@Override
	public void delete(Session cmisSession, String contextURL, String objectId) {
    	Folder application = loadApplicationById(cmisService.createAdminSession(), objectId, null); 
    	String docId = printService.findRicevutaApplicationId(cmisSession, application);
		try {
			if (docId != null) {
				Document latestDocumentVersion = (Document) cmisSession.getObject(cmisSession.getLatestDocumentVersion(docId, true, cmisSession.getDefaultContext()));
		    	Optional.ofNullable(latestDocumentVersion.<String>getPropertyValue(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA)).ifPresent(fascia -> {
	    			throw new ClientMessageException(
	    					i18nService.getLabel("message.error.domanda.cannot.deleted", Locale.ITALIAN, fascia));
		    	});			
			}
		} catch (CmisObjectNotFoundException _ex) {
			LOGGER.warn("There is no major version for application id : {}", objectId);
		}
		super.delete(cmisSession, contextURL, objectId);
	}
	
	public Map<String, Object> sendApplicationOIV(Session session, HttpServletRequest req, CMISUser user) throws CMISApplicationException, IOException, TemplateException {
		final String userId = user.getId();
    	MultipartHttpServletRequest mRequest = resolver.resolveMultipart(req);
		String idApplication = mRequest.getParameter("objectId");
		LOGGER.debug("send application : {}", idApplication);
    	MultipartFile file = mRequest.getFile("domandapdf");
    	Optional.ofNullable(file).orElseThrow(() -> new ClientMessageException("Allegare la domanda firmata!"));    	
    	Folder application = loadApplicationById(cmisService.createAdminSession(), idApplication, null); 
    	Folder call = loadCallById(session, application.getProperty(PropertyIds.PARENT_ID).getValueAsString());

    	String docId = printService.findRicevutaApplicationId(session, application);
		try {
			Optional.ofNullable(docId).orElseThrow(() -> new ClientMessageException(
					i18nService.getLabel("message.error.domanda.print.not.found", Locale.ITALIAN)));
			if (!session.getObject(docId).getSecondaryTypes().stream().
				filter(x -> x.getId().equals(PrintOIVService.P_JCONON_APPLICATION_ASPECT_FASCIA_PROFESSIONALE_ATTRIBUITA)).findAny().isPresent()){
    			throw new ClientMessageException(
    					i18nService.getLabel("message.error.domanda.print.not.found", Locale.ITALIAN));				
			}			
			Document latestDocumentVersion = (Document) session.getObject(session.getLatestDocumentVersion(docId, true, session.getDefaultContext()));
	    	Optional.ofNullable(latestDocumentVersion.<String>getPropertyValue(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA)).ifPresent(fascia -> {
	    		if (fascia.equals(application.getPropertyValue(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA))) {
	    			throw new ClientMessageException(
	    					i18nService.getLabel("message.error.domanda.fascia.equals", Locale.ITALIAN, fascia));
	    		}
	    	});			
		} catch (CmisObjectNotFoundException _ex) {
			LOGGER.warn("There is no major version for application id : {}", idApplication);
		}
		    	
    	ApplicationModel applicationModel = new ApplicationModel(application, session.getDefaultContext(), i18nService.loadLabels(Locale.ITALIAN), getContextURL(req));  
    	applicationModel.getProperties().put(PropertyIds.OBJECT_ID, idApplication);
    	sendApplication(cmisService.createAdminSession(), idApplication, getContextURL(req), Locale.ITALIAN, userId, applicationModel.getProperties(), applicationModel.getProperties());
    	
    	Map<String, Object> objectPrintModel = new HashMap<String, Object>();    	
		objectPrintModel.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, application.getPropertyValue(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA));
		objectPrintModel.put(PropertyIds.OBJECT_TYPE_ID, JCONONDocumentType.JCONON_ATTACHMENT_APPLICATION.value());
		objectPrintModel.put(PropertyIds.NAME, file.getOriginalFilename());
    	printService.archiviaRicevutaReportModel(cmisService.createAdminSession(), application, objectPrintModel, file.getInputStream(), file.getOriginalFilename(), true);
    	
    	Map<String, Object> mailModel = new HashMap<String, Object>();
		List<String> emailList = new ArrayList<String>();
		emailList.add(user.getEmail());
		mailModel.put("contextURL", getContextURL(req));
		mailModel.put("folder", application);
		mailModel.put("call", call);
		mailModel.put("message", context.getBean("messageMethod", Locale.ITALIAN));
		mailModel.put("email_comunicazione", user.getEmail());
		EmailMessage message = new EmailMessage();
		message.setRecipients(emailList);		
		message.setBccRecipients(Arrays.asList(mailFromDefault));
		String body = Util.processTemplate(mailModel, "/pages/application/application.registration.html.ftl");
		message.setSubject(i18nService.getLabel("subject-info", Locale.ITALIAN) + i18nService.getLabel("subject-confirm-domanda", Locale.ITALIAN, 
				call.getProperty(JCONONPropertyIds.CALL_CODICE.value()).getValueAsString()));
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(JCONONPropertyIds.APPLICATION_DUMMY.value(), "{\"stampa_archiviata\" : true}");
		application.updateProperties(properties);					
		message.setBody(body);
		message.setAttachments(Arrays.asList(new AttachmentBean(file.getOriginalFilename(), file.getBytes())));
		mailService.send(message);
    	
		return Collections.singletonMap("email_comunicazione", user.getEmail());
	}
	
	public String getContextURL(HttpServletRequest req) {
		return req.getScheme() + "://" + req.getServerName() + ":"
				+ req.getServerPort() + req.getContextPath();
	}
	
	@Override
	protected void addToQueueForSend(String id, String contextURL, boolean email) {
		queueService.queueAddContentToApplication().add(new PrintParameterModel(id, contextURL, email));
	}
	
}
