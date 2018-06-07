package it.cnr.si.cool.jconon.service.application;

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.ACLService;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeVersionService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.mail.MailService;
import it.cnr.cool.mail.model.AttachmentBean;
import it.cnr.cool.mail.model.EmailMessage;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.service.GroupService;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISAuthority;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.I18nService;
import it.cnr.cool.util.MimeTypes;
import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.cmis.model.JCONONDocumentType;
import it.cnr.si.cool.jconon.cmis.model.JCONONFolderType;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.flows.model.StartWorkflowResponse;
import it.cnr.si.cool.jconon.flows.model.TaskResponse;
import it.cnr.si.cool.jconon.model.ApplicationModel;
import it.cnr.si.cool.jconon.model.PrintParameterModel;
import it.cnr.si.cool.jconon.repository.ProtocolRepository;
import it.cnr.si.cool.jconon.service.QueueService;
import it.cnr.si.cool.jconon.service.cache.CompetitionFolderService;
import it.cnr.si.cool.jconon.service.call.CallService;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Cluster;

@Component
@Primary
public class ApplicationOIVService extends ApplicationService{

    private static final String JCONON_ATTACHMENT_ESPERIENZA_ANNOTAZIONE_MOTIVAZIONE = "jconon_attachment:esperienza_annotazione_motivazione";
	private static final String JCONON_APPLICATION_FASCIA_PROFESSIONALE_VALIDATA = "jconon_application:fascia_professionale_validata";
	public static final String P_JCONON_SCHEDA_ANONIMA_ESPERIENZA_NON_COERENTE = "P:jconon_scheda_anonima:esperienza_non_coerente";
	private static final String ELENCO_OIV_XLS = "elenco-oiv.xls";
	private static final String NUMERO_OIV_JSON = "elenco-oiv.json";

	private static final String OIV = "OIV";

	private static final String ISCRIZIONE_ELENCO = "ISCRIZIONE_ELENCO";

	private static final String JCONON_APPLICATION_ESEGUI_CONTROLLO_FASCIA = "jconon_application:esegui_controllo_fascia";

	private static final String JCONON_APPLICATION_FASCIA_PROFESSIONALE_ESEGUI_CALCOLO = "jconon_application:fascia_professionale_esegui_calcolo";

	private static final String JCONON_APPLICATION_PROGRESSIVO_ISCRIZIONE_ELENCO = "jconon_application:progressivo_iscrizione_elenco";
	private static final String JCONON_APPLICATION_DATA_ISCRIZIONE_ELENCO = "jconon_application:data_iscrizione_elenco";

	private static final String JCONON_APPLICATION_FL_INVIA_NOTIFICA_EMAIL = "jconon_application:fl_invia_notifica_email";
	private static final String JCONON_APPLICATION_OGGETTO_NOTIFICA_EMAIL = "jconon_application:oggetto_notifica_email";
	private static final String JCONON_APPLICATION_TESTO_NOTIFICA_EMAIL = "jconon_application:testo_notifica_email";

	public static final String INF250 = "<250", SUP250=">=250";

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
	public static final String EMAIL_DOMANDE_OIV = "EMAIL_DOMANDE_OIV";
    public static final String JCONON_APPLICATION_ACTIVITY_ID = "jconon_application:activityId";

    @Autowired
	private CMISService cmisService;
	@Autowired
	private I18nService i18nService;
	@Autowired
	private CommonsMultipartResolver resolver;
    @Autowired
    private QueueService queueService;
    @Autowired
    private PrintOIVService printService;
    @Autowired	
	private ApplicationContext context;
	@Autowired
	private MailService mailService;
	@Autowired
	private UserService userService;
    @Autowired
    private ProtocolRepository protocolRepository;
    @Autowired
    private Cluster cluster;
	@Autowired
	private CallService callService;
	@Autowired
	private NodeVersionService nodeVersionService;
	@Autowired
	private ACLService aclService;	
	@Autowired	
	private GroupService groupService;
    @Autowired
    private FlowsService flowsService;
    @Autowired
    private CompetitionFolderService competitionFolderService;

	@Value("${mail.from.default}")
	private String mailFromDefault;
	
	@Value("${user.admin.username}")
	private String adminUserName;
	
	@Override
	public Folder save(Session currentCMISSession,
			String contextURL, Locale locale,
			String userId, Map<String, Object> properties,
			Map<String, Object> aspectProperties) {
		String objectId = (String) properties.get(PropertyIds.OBJECT_ID);
		if (properties.containsKey(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ESEGUI_CALCOLO) &&
                properties.get(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ESEGUI_CALCOLO).equals("false")) {
			properties.put(JCONON_APPLICATION_ESEGUI_CONTROLLO_FASCIA, false);
			properties.remove(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ESEGUI_CALCOLO);
			aspectProperties.remove(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ESEGUI_CALCOLO);
            properties.put("jconon_application:fl_rimosso_elenco", false);
			Folder application = super.save(currentCMISSession, contextURL, locale, userId, properties, aspectProperties);
            super.readmission(currentCMISSession, objectId);
			return application;
		} else {
			eseguiCalcolo(objectId, aspectProperties);
			return super.save(currentCMISSession, contextURL, locale, userId, properties, aspectProperties);			
		}

	}

	@Override
	public void addCoordinatorToConcorsiGroup(String nodeRef) {
		/**
		 * Non aggiunge mai il gruppo concorsi come coordinator
		 */
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

	public Map<String, Object> ricalcolaFascia(Session session, String applicationId) {
		Map<String, Object> result = new HashMap<String, Object>();
		eseguiCalcolo(applicationId,result);
		return result;
	}
	
	private String eseguiCalcolo(String objectId) {
		Session adminSession = cmisService.createAdminSession();
		Folder application = (Folder) adminSession.getObject(objectId);
		List<Interval> oivPeriodSup250 = new ArrayList<>(), oivPeriodInf250 = new ArrayList<>();
		List<Interval> esperienzePeriod =  esperienzePeriod(getQueryResultEsperienza(adminSession, application));
		ItemIterable<QueryResult> queryResultsOiv = getQueryResultsOiv(adminSession, application);
		for (QueryResult oiv : queryResultsOiv) {
			if (oiv.getPropertyMultivalueById(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).stream().anyMatch(x -> x.equals(P_JCONON_SCHEDA_ANONIMA_ESPERIENZA_NON_COERENTE)))
				continue;			
			Calendar da = oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_DA),
				a = oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_A);
			if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(INF250)) {
				oivPeriodInf250.add(new Interval(da, a));
			} else if (oiv.getPropertyValueById(JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI).equals(SUP250)) {
				oivPeriodSup250.add(new Interval(da, a));
			}
		}
		return assegnaFascia(esperienzePeriod, oivPeriodSup250, oivPeriodInf250);		
	}
	
	public void eseguiCalcolo(String objectId, Map<String, Object> aspectProperties) {
		String fascia = eseguiCalcolo(objectId);
		LOGGER.info("fascia attribuita a {}: {}", objectId, fascia);
		aspectProperties.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, fascia);
	}

	private List<Interval> esperienzePeriod(ItemIterable<QueryResult> queryResultEsperienza) {
		List<Interval> esperienzePeriod = new ArrayList<>();
		for (QueryResult esperienza : queryResultEsperienza) {
			if (esperienza.getPropertyMultivalueById(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).stream().anyMatch(x -> x.equals(P_JCONON_SCHEDA_ANONIMA_ESPERIENZA_NON_COERENTE)))
				continue;
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
		BigDecimal daysEsperienza = BigDecimal.ZERO, daysOIV = BigDecimal.ZERO, daysOIVSup250 = BigDecimal.ZERO;
		/**
		 * Per il calcolo dell'esperienza bisogna tener conto anche dell'esperienza OIV
		 */
		List<Interval> periodo = new ArrayList<Interval>();
		periodo.addAll(esperienzePeriodList);
		periodo.addAll(oivPeriodSup250List);
		periodo.addAll(oivPeriodInf250List);
		
		List<Interval> oivPeriod = new ArrayList<Interval>();
		oivPeriod.addAll(oivPeriodSup250List);
		oivPeriod.addAll(oivPeriodInf250List);
		
		List<Interval> esperienzePeriod = overlapping(periodo);
		List<Interval> oivPeriodAll = overlapping(oivPeriod);
		List<Interval> oivPeriodSup250 = overlapping(oivPeriodSup250List);

		LOGGER.info("esperienzePeriod: {}", esperienzePeriod);
		LOGGER.info("oivPeriodSup250: {}", oivPeriodSup250);
		LOGGER.info("oivPeriodInf250: {}", oivPeriodAll);
		for (Interval interval : esperienzePeriod) {
			daysEsperienza = daysEsperienza.add(BigDecimal.valueOf(Duration.between(interval.getStartDate(), interval.getEndDate()).toDays())).add(BigDecimal.ONE);
		}
		for (Interval interval : oivPeriodAll) {
			daysOIV = daysOIV.add(BigDecimal.valueOf(Duration.between(interval.getStartDate(), interval.getEndDate()).toDays())).add(BigDecimal.ONE);
		}
		for (Interval interval : oivPeriodSup250) {
			daysOIVSup250 = daysOIVSup250.add(BigDecimal.valueOf(Duration.between(interval.getStartDate(), interval.getEndDate()).toDays())).add(BigDecimal.ONE);
		}
		return getFascia(daysEsperienza, daysOIV, daysOIVSup250);
	}

	private String getFascia(final BigDecimal daysEsperienza, final BigDecimal daysOIV, final BigDecimal daysOIVSup250) {
		LOGGER.info("Days Esperienza: {}", daysEsperienza);
		LOGGER.info("Days OIV: {}", daysOIV);
		LOGGER.info("Days OIV Sup 250: {}", daysOIVSup250);

		if (!Long.valueOf(0).equals(daysEsperienza) ) {
			Long 
				years = daysEsperienza.divide(DAYSINYEAR, RoundingMode.DOWN).longValue(),
				yearsOIVSUP250 = daysOIVSup250.divide(DAYSINYEAR, RoundingMode.DOWN).longValue(),
				yearsOIV = daysOIV.divide(DAYSINYEAR, RoundingMode.DOWN).longValue();
			LOGGER.info("YEARS: {}", years);
			if (years >= 12 && yearsOIVSUP250 >= 3) {
				return FASCIA3;
			}
			if (years.intValue() >= 8 && yearsOIV >= 3) {
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


    public Map<String, Object> responseSoccorsoIstruttorio(Session session, HttpServletRequest req, String idDomanda, String idDocumento, CMISUser user) throws CMISApplicationException, IOException, TemplateException {
        Folder application = loadApplicationById(session, idDomanda, null);
        OperationContext context = session.getDefaultContext();
        context.setIncludeRelationships(IncludeRelationships.SOURCE);
        final Document document = Optional.ofNullable(session.getObject(idDocumento, context))
                .filter(Document.class::isInstance)
                .map(Document.class::cast)
                .orElseThrow(() -> new RuntimeException("File for soccorso istruttorio is not present in request"));
        String testo = document.getPropertyValue("jconon_attachment:testo_response_soccorso_istruttorio");
        final List<Document> allegati = document.getRelationships().stream()
                .filter(relationship -> relationship.getType().getId().equals("R:jconon_attachment:response_soccorso_istruttorio"))
                .map(relationship -> relationship.getTarget())
                .filter(Document.class::isInstance)
                .map(Document.class::cast)
                .collect(Collectors.toList());
        TaskResponse currentTask = Optional.ofNullable(flowsService.getCurrentTask(application.<String>getPropertyValue(JCONON_APPLICATION_ACTIVITY_ID)))
                .filter(processInstanceResponseResponseEntity -> processInstanceResponseResponseEntity.getStatusCode() == HttpStatus.OK)
                .map(taskResponseResponseEntity -> taskResponseResponseEntity.getBody()).orElseThrow(() -> new RuntimeException("Task corrente non trovato!"));

        flowsService.completeTask(application,currentTask,testo, allegati);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("jconon_application:fl_soccorso_istruttorio", false);
        cmisService.createAdminSession().getObject(idDomanda).updateProperties(properties);

        return Collections.emptyMap();
    }

    public Map<String, Object> soccorsoIstruttorio(Session session, HttpServletRequest req, String idDomanda, String fileName, CMISUser user) throws CMISApplicationException, IOException, TemplateException {
		final String userId = user.getId();
		MultipartHttpServletRequest mRequest = resolver.resolveMultipart(req);
        MultipartFile file = Optional.ofNullable(mRequest.getFile("file"))
                .orElseThrow(() -> new RuntimeException("File for soccorso istruttorio is not present in request"));

		LOGGER.debug("soccorso istruttorio application : {}", idDomanda);
        Folder application = loadApplicationById(session, idDomanda, null);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("jconon_application:fl_soccorso_istruttorio", true);
        application.updateProperties(properties);

        ContentStream contentStream = new ContentStreamImpl(fileName,
                BigInteger.valueOf(file.getInputStream().available()),
                MimeTypes.PDF.mimetype(),
                file.getInputStream());
        Map<String, Object> propertiesFile = new HashMap<String, Object>();
        propertiesFile.put(PropertyIds.NAME, fileName);
        propertiesFile.put(PropertyIds.OBJECT_TYPE_ID, "D:jconon_attachment:soccorso_istruttorio");

        final Document document = application.createDocument(propertiesFile, contentStream, VersioningState.MAJOR);

        return Collections.singletonMap("idDocumento", document.getId());
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
    	Boolean eseguiControlloFascia = Optional.ofNullable(application.<Boolean>getPropertyValue(JCONON_APPLICATION_ESEGUI_CONTROLLO_FASCIA)).orElse(true);
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
	    		if (eseguiControlloFascia && fascia.equals(application.getPropertyValue(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA))) {
	    			throw new ClientMessageException(
	    					i18nService.getLabel("message.error.domanda.fascia.equals", Locale.ITALIAN, fascia));
	    		}
	    	});			
		} catch (CmisObjectNotFoundException _ex) {
			LOGGER.warn("There is no major version for application id : {}", idApplication);
		}
		if (!eseguiControlloFascia) {			
			Map<String, Object> propertiesFascia = new HashMap<String, Object>();
			propertiesFascia.put(JCONON_APPLICATION_ESEGUI_CONTROLLO_FASCIA, true);
			application.updateProperties(propertiesFascia);
		}

        ApplicationModel applicationModel = new ApplicationModel(application, session.getDefaultContext(), i18nService.loadLabels(Locale.ITALIAN), getContextURL(req));
        applicationModel.getProperties().put(PropertyIds.OBJECT_ID, idApplication);
        sendApplication(cmisService.createAdminSession(), idApplication, getContextURL(req), Locale.ITALIAN, userId, applicationModel.getProperties(), applicationModel.getProperties());

        if (Optional.ofNullable(application.<String>getPropertyValue(JCONON_APPLICATION_ACTIVITY_ID))
                .filter(processInstanceId -> flowsService.isProcessTerminated(processInstanceId)).isPresent()) {
            throw new ClientMessageException("La domanda è in fase di valutazione, non è possibile procedere con un nuovo invio!");
        } else {
            final ResponseEntity<StartWorkflowResponse> startWorkflowResponseResponseEntity = flowsService.startWorkflow(application,
                    getQueryResultEsperienza(session, application),
                    getQueryResultsOiv(session, application),
                    file,
                    Optional.ofNullable(competitionFolderService.findAttachmentId(session, application.getId(), JCONONDocumentType.JCONON_ATTACHMENT_CURRICULUM_VITAE))
                            .map(id ->  session.getObject(id))
                            .filter(Document.class::isInstance)
                            .map(Document.class::cast)
                            .orElse(null),
                    Optional.ofNullable(competitionFolderService.findAttachmentId(session, application.getId(), JCONONDocumentType.JCONON_ATTACHMENT_DOCUMENTO_RICONOSCIMENTO))
                            .map(id ->  session.getObject(id))
                            .filter(Document.class::isInstance)
                            .map(Document.class::cast)
                            .orElse(null)
            );
            application.updateProperties(Collections.singletonMap(JCONON_APPLICATION_ACTIVITY_ID, startWorkflowResponseResponseEntity.getBody().getId()));
            LOGGER.info(String.valueOf(startWorkflowResponseResponseEntity.getBody()));
        }
        Map<String, Object> objectPrintModel = new HashMap<String, Object>();
        objectPrintModel.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA, application.getPropertyValue(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA));
        objectPrintModel.put(PropertyIds.OBJECT_TYPE_ID, JCONONDocumentType.JCONON_ATTACHMENT_APPLICATION.value());
        objectPrintModel.put(PropertyIds.NAME, file.getOriginalFilename());
        printService.archiviaRicevutaReportModel(cmisService.createAdminSession(), application,
				objectPrintModel, file.getInputStream(), file.getOriginalFilename(), true);

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

	public Integer iscriviInElenco(Session currentCMISSession, String nodeRef) {
		Session session = cmisService.createAdminSession();
		Folder application = loadApplicationById(session, nodeRef, null);
		Folder call = loadCallById(currentCMISSession, application.getProperty(PropertyIds.PARENT_ID).getValueAsString());
		try {
			final Optional<BigInteger> progressivoIscrizione = Optional.ofNullable(application.<BigInteger>getPropertyValue(JCONON_APPLICATION_PROGRESSIVO_ISCRIZIONE_ELENCO));
			Integer numProgressivo =
					progressivoIscrizione
						.map(BigInteger::intValue)
						.orElseGet(() -> protocolRepository.getNumProtocollo(ISCRIZIONE_ELENCO, OIV).intValue() + 1);
			try {
				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put(JCONON_APPLICATION_FASCIA_PROFESSIONALE_VALIDATA,
						Optional.ofNullable(application.<String>getPropertyValue(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA)).orElse(null));
				properties.put(JCONON_APPLICATION_PROGRESSIVO_ISCRIZIONE_ELENCO, numProgressivo);
				properties.put(JCONON_APPLICATION_DATA_ISCRIZIONE_ELENCO, Calendar.getInstance());
				application = (Folder) application.updateProperties(properties);
				LOGGER.info("Assegnato progressivo {} alla domanda {}", numProgressivo, nodeRef);
				CMISUser user;
				try {
					user = userService.loadUserForConfirm(
							application.getPropertyValue(JCONONPropertyIds.APPLICATION_USER.value()));
				} catch (CoolUserFactoryException e) {
					throw new ClientMessageException("User not found of application " + nodeRef, e);
				}
				String email = Optional.ofNullable(application.<String>getPropertyValue(JCONONPropertyIds.APPLICATION_EMAIL_COMUNICAZIONI.value())).orElse(user.getEmail());
				try {
					Map<String, Object> mailModel = new HashMap<String, Object>();
					List<String> emailList = new ArrayList<String>();
					emailList.add(email);
					mailModel.put("folder", application);
					mailModel.put("call", call);
					mailModel.put("message", context.getBean("messageMethod", Locale.ITALIAN));
					mailModel.put("email_comunicazione", email);
					EmailMessage message = new EmailMessage();
					message.setRecipients(emailList);
					message.setBccRecipients(Arrays.asList(mailFromDefault));
					String body = Util.processTemplate(mailModel, "/pages/application/application.iscrizione.html.ftl");
					message.setSubject(i18nService.getLabel("app.name", Locale.ITALIAN) + " - " + i18nService.getLabel("mail.subject.iscrizione", Locale.ITALIAN, numProgressivo));
					message.setBody(body);
					mailService.send(message);
				} catch (TemplateException | IOException e) {
					LOGGER.error("Cannot send email for readmission applicationId: {}", nodeRef, e);
				}
			} finally {
			    if (!progressivoIscrizione.isPresent())
				    protocolRepository.putNumProtocollo(ISCRIZIONE_ELENCO, OIV, numProgressivo.longValue());
			}
			return numProgressivo;
		} catch(CmisVersioningException _ex) {
			throw new ClientMessageException("Assegnazione progressivo in corso non è possibile procedere!");
		}
	}

	@Override
	public void readmission(Session currentCMISSession, String nodeRef) {
		iscriviInElenco(currentCMISSession, nodeRef);
	}

	private void messageToUser(Folder application, Folder call, Document doc) {
		CMISUser user;
		try {
			user = userService.loadUserForConfirm(
					application.getPropertyValue(JCONONPropertyIds.APPLICATION_USER.value()));
		} catch (CoolUserFactoryException e) {
			throw new ClientMessageException("User not found of application " + application.getId(), e);
		}
		String email = Optional.ofNullable(application.<String>getPropertyValue(JCONONPropertyIds.APPLICATION_EMAIL_COMUNICAZIONI.value())).orElse(user.getEmail());
		try {
			Map<String, Object> mailModel = new HashMap<String, Object>();
			List<String> emailList = new ArrayList<String>();
			emailList.add(email);
			mailModel.put("folder", application);
			mailModel.put("call", call);
			mailModel.put("message", context.getBean("messageMethod", Locale.ITALIAN));
			mailModel.put("email_comunicazione", email);
			EmailMessage message = new EmailMessage();
			message.setRecipients(emailList);
			message.setBccRecipients(Arrays.asList(mailFromDefault));
			message.setSubject(doc.<String>getPropertyValue(JCONON_APPLICATION_OGGETTO_NOTIFICA_EMAIL));
			message.setBody(doc.<String>getPropertyValue(JCONON_APPLICATION_TESTO_NOTIFICA_EMAIL));
			if (Optional.ofNullable(doc.getContentStream()).isPresent())
			    message.setAttachments(Arrays.asList(new AttachmentBean(doc.getName(), IOUtils.toByteArray(doc.getContentStream().getStream()))));
			mailService.send(message);
		} catch (IOException e) {
			LOGGER.error("Cannot send email for applicationId: {}", application.getId(), e);
		}
	}

	public void message(Session currentCMISSession, String nodeRef, String nodeRefDocumento) {
		Folder application = loadApplicationById(currentCMISSession, nodeRef, null);
		Folder call = loadCallById(currentCMISSession, application.getProperty(PropertyIds.PARENT_ID).getValueAsString());
		Document doc = (Document) currentCMISSession.getObject(nodeRefDocumento);
		if (doc.<Boolean>getPropertyValue(JCONON_APPLICATION_FL_INVIA_NOTIFICA_EMAIL)) {
		    messageToUser(application, call, doc);
		}
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("jconon_application:data_invio_comunicazione", Calendar.getInstance());
		cmisService.createAdminSession().getObject(application.getId()).updateProperties(properties);
	}

	@Override
	public void reject(Session currentCMISSession, String nodeRef, String nodeRefDocumento) {
		super.reject(currentCMISSession, nodeRef, nodeRefDocumento);
    	Folder application = loadApplicationById(currentCMISSession, nodeRef, null);
    	Folder call = loadCallById(currentCMISSession, application.getProperty(PropertyIds.PARENT_ID).getValueAsString());
    	Document doc = (Document) currentCMISSession.getObject(nodeRefDocumento);
    	if (doc.<Boolean>getPropertyValue(JCONON_APPLICATION_FL_INVIA_NOTIFICA_EMAIL)) {
            messageToUser(application, call, doc);
    	}
    	Map<String,Object> properties = new HashMap<String,Object>();
        properties.put("jconon_application:fl_rimosso_elenco",
                Optional.ofNullable(application.getPropertyValue("jconon_application:progressivo_iscrizione_elenco")).map(o -> Boolean.TRUE).orElse(Boolean.FALSE));
        properties.put("jconon_application:data_rimozione_elenco", Calendar.getInstance());
        cmisService.createAdminSession().getObject(application.getId()).updateProperties(properties);
	}

	public Map<String, Object> extractionApplicationForElenco(Session session, String query, String userId, String callId) throws IOException {
		return printService.extractionApplicationForElenco(session, query, userId, callId);
	}

    public Map<String, Object> extractionApplicationForAllIscritti(
            Session session, String query, String contexURL, String userId)
            throws IOException {
	    return printService.extractionApplicationForAllIscritti(session, query, contexURL, userId);
    }

    @Scheduled(cron="0 0 22 * * MON-FRI")
    public void estraiExcelOIV() {
        List<String> members = cluster
                .getMembers()
                .stream()
                .map(member -> member.getUuid())
                .sorted()
                .collect(Collectors.toList());

        String uuid = cluster.getLocalMember().getUuid();
        if( 0 == members.indexOf(uuid)) {
        	Session session = cmisService.createAdminSession();
        	Criteria criteria = CriteriaFactory.createCriteria(JCONONFolderType.JCONON_CALL.queryName());    	
    		criteria.addColumn(PropertyIds.OBJECT_ID);
    		criteria.add(Restrictions.eq(JCONONPropertyIds.CALL_CODICE.value(), "OIV"));
    		ItemIterable<QueryResult> iterable = criteria.executeQuery(session, false, session.getDefaultContext());
        	for (QueryResult queryResult : iterable.getPage(Integer.MAX_VALUE)) {
        		try {
            		Folder call = (Folder) session.getObject(String.valueOf(queryResult.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue()));        		
            		List<String> emailList = groupService.children(EMAIL_DOMANDE_OIV, cmisService.getAdminSession())
            				.stream()
            				.filter(x -> !x.getShortName().equals("app.performance"))
            				.map(CMISAuthority::getShortName)
            				.collect(Collectors.toList())
            				.stream()
            				.map(user -> userService.loadUserForConfirm(user).getEmail())
            				.collect(Collectors.toList());        		
            		HSSFWorkbook wb = printService.generateXLS(cmisService.createAdminSession(), "select cmis:objectId from jconon_application:folder where NOT jconon_application:stato_domanda = 'I' AND IN_TREE('" + call.getId() +"')" , true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
            		wb.write(stream);			        		
        			EmailMessage message = new EmailMessage();
        			message.setRecipients(emailList);
        			message.setSubject(i18nService.getLabel("app.name", Locale.ITALIAN) + " - " + "Estrazione domande");
        			message.setBody("In allegato l'estrazione delle domande");
        			message.setAttachments(Arrays.asList(new AttachmentBean("DOMANDE_OIV.xls", stream.toByteArray())));
        			mailService.send(message);
        		}catch (IOException e) {
        			LOGGER.error("Cannot estraiExcelOIV", e);
        		}    	
        	}        	        	
        }
    }
    @Scheduled(cron="0 0 21 * * *")
    public void estraiElencoOIV() {
        List<String> members = cluster
                .getMembers()
                .stream()
                .map(member -> member.getUuid())
                .sorted()
                .collect(Collectors.toList());

        String uuid = cluster.getLocalMember().getUuid();

        if( 0 == members.indexOf(uuid)) {
            try {
            	Session session = cmisService.createAdminSession();
            	Criteria criteria = CriteriaFactory.createCriteria(JCONONFolderType.JCONON_CALL.queryName());    	
        		criteria.addColumn(PropertyIds.OBJECT_ID);
        		criteria.add(Restrictions.eq(JCONONPropertyIds.CALL_CODICE.value(), "OIV"));
        		ItemIterable<QueryResult> iterable = criteria.executeQuery(session, false, session.getDefaultContext());
            	for (QueryResult queryResult : iterable.getPage(Integer.MAX_VALUE)) {
            		Folder call = (Folder) session.getObject(String.valueOf(queryResult.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue()));
                	HSSFWorkbook wb = printService.getWorkbookForElenco(cmisService.createAdminSession(), null, null, call.getId());

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
            		wb.write(stream);			
            		ContentStreamImpl contentStream = new ContentStreamImpl();
            		contentStream.setMimeType("application/vnd.ms-excel");
            		contentStream.setStream(new ByteArrayInputStream(stream.toByteArray()));
            		String docId = callService.findAttachmentName(session, call.getId(), ELENCO_OIV_XLS);
            		if (docId == null) {
                		Map<String, Object> properties = new HashMap<String, Object>();
                		properties.put(PropertyIds.NAME, ELENCO_OIV_XLS);
                		properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
                		Document createDocument = call.createDocument(properties, contentStream, VersioningState.MAJOR);
                		nodeVersionService.addAutoVersion(createDocument, false);
            		} else {
            			((Document)session.getObject(docId)).setContentStream(contentStream, true);
            		}
            		int numberOfRows = wb.getSheet(PrintOIVService.SHEET_DOMANDE).getLastRowNum();
            		ContentStreamImpl contentStreamCount = new ContentStreamImpl();
            		contentStreamCount.setMimeType("application/json");            		
            		contentStreamCount.setStream(new ByteArrayInputStream(new ObjectMapper().writeValueAsBytes(Collections.singletonMap("totalNumItems", numberOfRows))));
            		String docIdConta = callService.findAttachmentName(session, call.getId(), NUMERO_OIV_JSON);
            		if (docIdConta == null) {
                		Map<String, Object> properties = new HashMap<String, Object>();
                		properties.put(PropertyIds.NAME, NUMERO_OIV_JSON);
                		properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
                		Document createDocument = call.createDocument(properties, contentStreamCount, VersioningState.MAJOR);
                		nodeVersionService.addAutoVersion(createDocument, false);
            		} else {
            			((Document)session.getObject(docIdConta)).setContentStream(contentStreamCount, true);
            		}
            	}            	
			} catch (Exception e) {
	            LOGGER.error("Estrazione elenco OIV XLS failed", e);
			}
            LOGGER.info("{} is the chosen one for Estrazione elenco OIV XLS", uuid);
        } else {
            LOGGER.info("{} is NOT the chosen one for Estrazione elenco OIV XLS", uuid);
        }

    }	
	
	public List<String> checkApplicationOIV(Session session,
			String userId, CMISUser cmisUserFromSession) {
		List<String> result = new ArrayList<String>();
		try {
			CMISUser user = userService.loadUserForConfirm(userId);
			if (!user.isAdmin())
				throw new ClientMessageException("Only Admin");
		} catch (CoolUserFactoryException e) {
			throw new ClientMessageException("User not found " + userId, e);
		}		
    	Criteria criteria = CriteriaFactory.createCriteria(JCONONFolderType.JCONON_APPLICATION.queryName());
		criteria.addColumn(PropertyIds.OBJECT_ID);
		criteria.add(Restrictions.eq(JCONONPropertyIds.APPLICATION_STATO_DOMANDA.value(), StatoDomanda.CONFERMATA.getValue()));
		ItemIterable<QueryResult> iterable = criteria.executeQuery(session, false, session.getDefaultContext());
		result.add("NOME,COGNOME,CODICE_FISCALE,NUMERO ELENCO,FASCIA ATTRIBUITA,FASCIA CALCOLATA");			
		for (QueryResult queryResult : iterable.getPage(Integer.MAX_VALUE)) {
        	Folder application = loadApplicationById(session, queryResult.<String>getPropertyValueById(PropertyIds.OBJECT_ID), null); 
			Optional<String> fasciaAttribuita = Optional.ofNullable(application.<String>getPropertyValue(JCONON_APPLICATION_FASCIA_PROFESSIONALE_ATTRIBUITA));
			Optional<String> fasciaCalcolata = Optional.ofNullable(eseguiCalcolo(application.getId()));
			result.add(
					application.<String>getPropertyValue(JCONONPropertyIds.APPLICATION_NOME.value()).toUpperCase()
				.concat(",")
				.concat(application.<String>getPropertyValue(JCONONPropertyIds.APPLICATION_COGNOME.value()).toUpperCase())
				.concat(",")
				.concat(application.<String>getPropertyValue(JCONONPropertyIds.APPLICATION_CODICE_FISCALE.value()).toUpperCase())
				.concat(",")
				.concat(String.valueOf(Optional.ofNullable(application.<BigInteger>getPropertyValue(JCONON_APPLICATION_PROGRESSIVO_ISCRIZIONE_ELENCO)).orElse(BigInteger.ZERO)))				
				.concat(",")
				.concat(fasciaAttribuita.orElse(""))
				.concat(",")
				.concat(fasciaCalcolata.orElse("")));			
    	}		
		return result;
	}

	public Folder getOIVCall(Session session) {
	    Folder call = null;
        final ItemIterable<QueryResult> query = session.query("select cmis:objectId from jconon_call_oiv:folder", false);
        for (QueryResult queryResult : query) {
            call = Optional.ofNullable(session.getObject(queryResult.<String>getPropertyValueById(PropertyIds.OBJECT_ID)))
                    .map(Folder.class::cast).orElse(null);
        }
        return call;
    }

	public void esperienzaNonCoerente(String userId, String objectId, String callId, String aspect, String motivazione) {
		Session session = cmisService.createAdminSession();
        Folder call = Optional.ofNullable(callId)
                .map(id -> loadCallById(session, callId))
                .map(Folder.class::cast)
                .orElseGet(() -> getOIVCall(session));
		try {
			CMISUser user = userService.loadUserForConfirm(userId);
			if (!(user.isAdmin() || callService.isMemeberOfRDPGroup(user, call)))
				throw new ClientMessageException("Only Admin or RdP");
		} catch (CoolUserFactoryException e) {
			throw new ClientMessageException("User not found " + userId, e);
		}		
		CmisObject object = session.getObject(objectId);		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("jconon_attachment:esperienza_non_coerente_motivazione", motivazione);
		object.updateProperties(properties, Collections.singletonList(aspect), Collections.emptyList());
		aclService.changeOwnership(cmisService.getAdminSession(), object.<String>getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), 
				adminUserName, false, Collections.emptyList());
	}

	public void esperienzaAnnotazione(String userId, String objectId, String callId, String applicationId, String aspect, String motivazione) {
		Session session = cmisService.createAdminSession();
		Folder call = loadCallById(session, callId);
		try {
			CMISUser user = userService.loadUserForConfirm(userId);
			if (!(user.isAdmin() || callService.isMemeberOfRDPGroup(user, call)))
				throw new ClientMessageException("Only Admin or RdP");
		} catch (CoolUserFactoryException e) {
			throw new ClientMessageException("User not found " + userId, e);
		}		
		Optional<String> motivazioneOpt = Optional.ofNullable(motivazione);
		CmisObject object = session.getObject(objectId);		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(JCONON_ATTACHMENT_ESPERIENZA_ANNOTAZIONE_MOTIVAZIONE, motivazione);
		object.updateProperties(
				motivazioneOpt.map(x -> properties).orElse(Collections.emptyMap()), 
				motivazioneOpt.map(x->Collections.singletonList(aspect)).orElse(Collections.emptyList()), 
				motivazioneOpt.map(x->Collections.<String>emptyList()).orElse(Collections.singletonList(aspect)));

		Criteria criteria = CriteriaFactory.createCriteria("jconon_scheda_anonima:esperienza_annotazioni");    	
		criteria.addColumn(JCONON_ATTACHMENT_ESPERIENZA_ANNOTAZIONE_MOTIVAZIONE);
		criteria.add(Restrictions.inFolder(applicationId));
		ItemIterable<QueryResult> iterable = criteria.executeQuery(session, false, session.getDefaultContext());
		List<String> annotazioni = new ArrayList<String>();
		iterable.forEach(
				q -> annotazioni.add(q.<String>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_ANNOTAZIONE_MOTIVAZIONE)
		));

		motivazione = Optional.of(annotazioni.stream().collect(Collectors.joining(", "))).filter(x -> x.length() > 0).orElse(null);
		motivazioneOpt = Optional.ofNullable(motivazione);
		properties.put(JCONON_ATTACHMENT_ESPERIENZA_ANNOTAZIONE_MOTIVAZIONE, motivazione);
		
		
		Folder domanda = (Folder) session.getObject(applicationId);
		domanda.updateProperties(
				motivazioneOpt.map(x -> properties).orElse(Collections.emptyMap()),
				motivazioneOpt.map(x->Collections.singletonList(aspect)).orElse(Collections.emptyList()), 
				motivazioneOpt.map(x->Collections.<String>emptyList()).orElse(Collections.singletonList(aspect)));
	}

	public void esperienzaCoerente(String userId, String objectId, String callId, String aspect, String userName) {
		Session session = cmisService.createAdminSession();
		Folder call = loadCallById(session, callId);
		try {
			CMISUser user = userService.loadUserForConfirm(userId);
			if (!(user.isAdmin() || callService.isMemeberOfRDPGroup(user, call)))
				throw new ClientMessageException("Only Admin or RdP");
		} catch (CoolUserFactoryException e) {
			throw new ClientMessageException("User not found " + userId, e);
		}		
		CmisObject object = session.getObject(objectId);
		object.updateProperties(Collections.emptyMap(), Collections.emptyList(), Collections.singletonList(aspect));
		aclService.changeOwnership(cmisService.getAdminSession(), object.<String>getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), 
				userName, false, Collections.emptyList());
	}
	
	@Override
	protected boolean isDomandaInviata(Folder application, CMISUser loginUser) {		
		return super.isDomandaInviata(application, loginUser) && 
				!application.getAllowableActions().getAllowableActions().stream().anyMatch(x -> x.equals(Action.CAN_CREATE_DOCUMENT));
	}

	@Override
	public void reopenApplication(Session currentCMISSession,
			String applicationSourceId, String contextURL, Locale locale,
			String userId) {
		try {
			OperationContext oc = new OperationContextImpl(currentCMISSession.getDefaultContext());
			oc.setFilterString(PropertyIds.OBJECT_ID);			
			currentCMISSession.getObject(applicationSourceId, oc);
		}catch (CmisPermissionDeniedException _ex) {
			throw new ClientMessageException("user.cannot.access.to.application", _ex);
		}
		final Folder newApplication = loadApplicationById(currentCMISSession, applicationSourceId, null);
		if (newApplication.getPropertyValue(JCONONPropertyIds.APPLICATION_ESCLUSIONE_RINUNCIA.value()) != null &&
				newApplication.getPropertyValue(JCONONPropertyIds.APPLICATION_ESCLUSIONE_RINUNCIA.value()).equals(StatoDomanda.ESCLUSA.getValue())) {
			throw new ClientMessageException("La domanda è stata esclusa, non è possibile modificarla nuovamente!");
		}
		if (Optional.ofNullable(newApplication.<String>getPropertyValue(JCONON_APPLICATION_ACTIVITY_ID))
				.filter(processInstanceId -> !flowsService.isProcessTerminated(processInstanceId)).isPresent()) {
                throw new ClientMessageException("La domanda è in fase di valutazione, non è possibile modificarla!");
		} else {
            super.reopenApplication(currentCMISSession, applicationSourceId, contextURL,
                    locale, userId);
        }
	}

    public void reopenApplicationForSoccorso(Session currentCMISSession, final String applicationSourceId, final String contextURL, Locale locale, String userId) {
        /**
         * Load application source with user session if user cannot access to application
         * throw an exception
         */
        try {
            OperationContext oc = new OperationContextImpl(currentCMISSession.getDefaultContext());
            oc.setFilterString(PropertyIds.OBJECT_ID);
            currentCMISSession.getObject(applicationSourceId, oc);
        }catch (CmisPermissionDeniedException _ex) {
            throw new ClientMessageException("user.cannot.access.to.application", _ex);
        }
        final Folder newApplication = loadApplicationById(currentCMISSession, applicationSourceId, null);
        final Folder call = loadCallById(currentCMISSession, newApplication.getParentId(), null);
        if (newApplication.getPropertyValue(JCONONPropertyIds.APPLICATION_DATA_DOMANDA.value()) == null ||
                !newApplication.getPropertyValue(JCONONPropertyIds.APPLICATION_STATO_DOMANDA.value()).equals(StatoDomanda.CONFERMATA.getValue())) {
            throw new ClientMessageException("message.error.domanda.no.confermata");
        }
        try {
            callService.isBandoInCorso(call,
                    userService.loadUserForConfirm(userId));
        } catch (CoolUserFactoryException e) {
            throw new CMISApplicationException("Error loading user: " + userId, e);
        }
        String link = cmisService.getBaseURL().concat("service/cnr/jconon/manage-application/reopen");
        UrlBuilder url = new UrlBuilder(link);
        Response resp = cmisService.getHttpInvoker(cmisService.getAdminSession()).invokePOST(url, MimeTypes.JSON.mimetype(),
                new Output() {
                    @Override
                    public void write(OutputStream out) throws Exception {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("applicationSourceId", newApplication.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString());
                        jsonObject.put("groupRdP", "GROUP_" + call.getPropertyValue(JCONONPropertyIds.CALL_RDP.value()));
                        out.write(jsonObject.toString().getBytes());
                    }
                }, cmisService.getAdminSession());
        int status = resp.getResponseCode();
        if (status == org.apache.commons.httpclient.HttpStatus.SC_NOT_FOUND|| status == org.apache.commons.httpclient.HttpStatus.SC_BAD_REQUEST|| status == org.apache.commons.httpclient.HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw new CMISApplicationException("Reopen Application error. Exception: " + resp.getErrorContent());
        }
        cmisService.createAdminSession().getObject(newApplication).updateProperties(Collections.singletonMap(JCONON_APPLICATION_ESEGUI_CONTROLLO_FASCIA, false));
    }

    public boolean isStatoFlussoSoccorsoIstruttorio(Session currentCMISSession, final String applicationSourceId) {
        final Folder newApplication = loadApplicationById(currentCMISSession, applicationSourceId, null);
        final String currentTaskName = Optional.ofNullable(flowsService.getCurrentTask(newApplication.<String>getPropertyValue(JCONON_APPLICATION_ACTIVITY_ID)))
                .filter(processInstanceResponseResponseEntity -> processInstanceResponseResponseEntity.getStatusCode() == HttpStatus.OK)
                .map(taskResponseResponseEntity -> taskResponseResponseEntity.getBody())
                .map(TaskResponse::getName)
                .orElse("");
        LOGGER.info("Current Task Name {}", currentTaskName);
        return currentTaskName.equalsIgnoreCase(TaskResponse.SOCCORSO_ISTRUTTORIO);
    }

    public Map<String, String> scaricaSoccorsoIstruttorio(Session currentCMISSession, final String applicationSourceId) {
        Map<String, String> result = new HashMap<>();
        Criteria criteria = CriteriaFactory.createCriteria("jconon_attachment:soccorso_istruttorio");
        criteria.addColumn(PropertyIds.OBJECT_ID);
        criteria.addColumn(PropertyIds.NAME);
        criteria.add(Restrictions.inFolder(applicationSourceId));
        ItemIterable<QueryResult> iterable = criteria.executeQuery(currentCMISSession, false, currentCMISSession.getDefaultContext());
        for (QueryResult queryResult : iterable) {
            result.put(PropertyIds.OBJECT_ID, queryResult.getPropertyValueById(PropertyIds.OBJECT_ID));
            result.put(PropertyIds.NAME, queryResult.getPropertyValueById(PropertyIds.NAME));
        }
        return result;
    }
}
