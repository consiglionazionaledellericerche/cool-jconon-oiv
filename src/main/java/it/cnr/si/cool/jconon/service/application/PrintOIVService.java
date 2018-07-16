package it.cnr.si.cool.jconon.service.application;

import it.cnr.cool.cmis.model.ACLType;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeVersionService;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.Pair;
import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.cmis.model.JCONONFolderType;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.service.PrintService;
import it.cnr.si.cool.jconon.service.cache.CompetitionFolderService;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.Order;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
@Component
@Primary
public class PrintOIVService extends PrintService {
	private static final String NO = "No";

	private static final String SI = "Si";

	private static final String DATA_DI_INIZIO_RAPPORTO_DI_LAVORO = "Data di inizio rapporto di lavoro";

	private static final String DATORE_DI_LAVORO_ATTUALE = "Datore di lavoro attuale";

	private static final String RUOLO2 = "Ruolo";

	private static final String SETTORE = "Settore";

	private static final String DIPENDENTE_PUBBLICO = "Dipendente Pubblico";

	private static final String POSIZIONE = "Posizione";

	private static final String OCCUPATO = "Occupato";

	private static final String FASCIA_PROFESSIONALE_VALIDATA = "Fascia Professionale Validata";

	private static final String JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DATORE_LAVORO = "jconon_attachment:esperienza_professionale_datore_lavoro";

	private static final String DATORE_DI_LAVORO = "Datore di Lavoro";

	private static final String ANNOTAZIONE = "Annotazione";

	private static final String RUOLO = RUOLO2;

	private static final String JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A = "jconon_attachment:esperienza_professionale_a";

	private static final String JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_RUOLO = "jconon_attachment:esperienza_professionale_ruolo";

	private static final String JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE = "jconon_scheda_anonima:esperienza_professionale";

	private static final Logger LOGGER = LoggerFactory.getLogger(PrintService.class);
	
	private static final String JCONON_SCHEDA_ANONIMA_DOCUMENT = "jconon_scheda_anonima:document";
	public static final String P_JCONON_APPLICATION_ASPECT_FASCIA_PROFESSIONALE_ATTRIBUITA = "P:jconon_application:aspect_fascia_professionale_attribuita";
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"), 
			dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");	
	private List<String> headCSVApplication = Arrays.asList(
			"Id","Data Iscrizione in Elenco", "Cognome","Nome","Data di nascita","Sesso","Nazione di nascita",
			"Luogo di nascita","Prov. di nascita","Nazione di Residenza","Provincia di Residenza",
			"Comune di Residenza","Indirizzo di Residenza","CAP di Residenza","Codice Fiscale",
			"Email","Email PEC","Nazione Reperibilita'","Provincia di Reperibilita'",
			"Comune di Reperibilita'","Indirizzo di Reperibilita'",
			"CAP di Reperibilita'","Telefono","Data Invio Domanda",
			"Laurea", "Università",
			"Fascia Professionale Attribuita",
			"Stato",
			"Stato Corrente",
			RUOLO,
			DATORE_DI_LAVORO,
			ANNOTAZIONE,
			FASCIA_PROFESSIONALE_VALIDATA,
			OCCUPATO,
			POSIZIONE,
			DIPENDENTE_PUBBLICO,
			SETTORE,
			RUOLO2,
			DATORE_DI_LAVORO_ATTUALE,
			DATA_DI_INIZIO_RAPPORTO_DI_LAVORO
			);
	
	private List<String> headDetailCSVApplication = Stream.concat(headCSVApplication.stream()
				.filter(x -> !x.equalsIgnoreCase(RUOLO))
				.filter(x -> !x.equalsIgnoreCase(ANNOTAZIONE))
				.filter(x -> !x.equalsIgnoreCase(FASCIA_PROFESSIONALE_VALIDATA))
				.filter(x -> !x.equalsIgnoreCase(DATORE_DI_LAVORO))
				.filter(x -> !x.equalsIgnoreCase(OCCUPATO))
				.filter(x -> !x.equalsIgnoreCase(POSIZIONE))
				.filter(x -> !x.equalsIgnoreCase(DIPENDENTE_PUBBLICO))
				.filter(x -> !x.equalsIgnoreCase(SETTORE))
				.filter(x -> !x.equalsIgnoreCase(RUOLO2))
				.filter(x -> !x.equalsIgnoreCase(DATORE_DI_LAVORO_ATTUALE))
				.filter(x -> !x.equalsIgnoreCase(DATA_DI_INIZIO_RAPPORTO_DI_LAVORO)), Arrays.asList(
			"Tipologia esperienza (Professionale/OIV)",
			"Area di specializzazione",
			"Attività svolta nell’area di specializzazione indicata",
			RUOLO,
			"Data inizio(Tipologia esperienza)",
			"Data fine(Tipologia esperienza)",
			"Non coerente", 
			"Motivazione", 
			ANNOTAZIONE,
			FASCIA_PROFESSIONALE_VALIDATA,
			OCCUPATO,
			POSIZIONE,
			DIPENDENTE_PUBBLICO,
			SETTORE,
			RUOLO2,
			DATORE_DI_LAVORO_ATTUALE,
			DATA_DI_INIZIO_RAPPORTO_DI_LAVORO).stream()).collect(Collectors.toList());
	private List<String> headCSVElenco = Arrays.asList(
			"Id","Nome", "Cognome","Data iscrizione");

    private final static String PRECEDENTE_INCARICO_OIV = "Precedente Incarico OIV\n";
    private final static String ESPERIENZA_PROFESSIONALE = "Esperienza professionale\n";
    private List<String> headCSVApplicationAllIscritti = Arrays.asList(
            "Id","Data Iscrizione in Elenco", "Cognome","Nome","Data di nascita","Sesso","Nazione di nascita",
            "Luogo di nascita","Prov. di nascita","Nazione di Residenza","Provincia di Residenza",
            "Comune di Residenza","Indirizzo di Residenza","CAP di Residenza","Codice Fiscale",
            "Email","Email PEC","Nazione Reperibilita'","Provincia di Reperibilita'",
            "Comune di Reperibilita'","Indirizzo di Reperibilita'",
            "CAP di Reperibilita'","Telefono","Data Invio Domanda",
            "Laurea", "Università",
            "Fascia Professionale Attribuita",
            FASCIA_PROFESSIONALE_VALIDATA,
            OCCUPATO,
            POSIZIONE,
            DIPENDENTE_PUBBLICO,
            SETTORE,
            RUOLO2,
            DATORE_DI_LAVORO_ATTUALE,
            DATA_DI_INIZIO_RAPPORTO_DI_LAVORO,
            "Stato",
            "Stato Corrente",
            PRECEDENTE_INCARICO_OIV + "Da",
            PRECEDENTE_INCARICO_OIV + "A",
            PRECEDENTE_INCARICO_OIV + "Amministrazione pubblica",
            PRECEDENTE_INCARICO_OIV + "Sede",
            PRECEDENTE_INCARICO_OIV + "Comune",
            PRECEDENTE_INCARICO_OIV + "Indirizzo",
            PRECEDENTE_INCARICO_OIV + "CAP",
            PRECEDENTE_INCARICO_OIV + "Telefono",
            PRECEDENTE_INCARICO_OIV + "N. dipendenti",
            PRECEDENTE_INCARICO_OIV + "Ruolo",
            ESPERIENZA_PROFESSIONALE + "Da",
            ESPERIENZA_PROFESSIONALE + "A",
            ESPERIENZA_PROFESSIONALE + "Area di specializzazione",
            ESPERIENZA_PROFESSIONALE + "Attività svolta nell’area di specializzazione indicata",
            ESPERIENZA_PROFESSIONALE + "Datore di Lavoro/Committente",
            ESPERIENZA_PROFESSIONALE + "Ruolo",
            ESPERIENZA_PROFESSIONALE + "Comune",
            ESPERIENZA_PROFESSIONALE + "Stato",
            "Annotazioni"
    );

	public static final String SHEET_DOMANDE = "domande";
	
	@Autowired
	private CMISService cmisService;
	@Autowired
	private UserService userService;
	@Autowired
	private CompetitionFolderService competitionService;
	@Autowired
	private NodeVersionService nodeVersionService;	
	
	@Override
	public Pair<String, byte[]> printApplicationImmediate(Session cmisSession,
			String nodeRef, String contextURL, Locale locale) {
		Pair<String, byte[]>  result = super.printApplicationImmediate(cmisSession, nodeRef, contextURL, locale);
		Folder application = (Folder)cmisSession.getObject(nodeRef);
		String archiviaRicevutaReportModel = archiviaRicevutaReportModel(cmisService.createAdminSession(), application, new ByteArrayInputStream(result.getSecond()), 
				getNameRicevutaReportModel(cmisSession, application, locale), false);
		cmisService.createAdminSession().getObject(archiviaRicevutaReportModel).updateProperties(Collections.emptyMap(), 
				Collections.singletonList(P_JCONON_APPLICATION_ASPECT_FASCIA_PROFESSIONALE_ATTRIBUITA), Collections.emptyList());		
		return result;
	}
	@Override
	protected String getTitle(int i, Dichiarazioni dichiarazione) {
		if (dichiarazione.equals(Dichiarazioni.datiCNR) || dichiarazione.equals(Dichiarazioni.ulterioriDati))
			return "";
		return super.getTitle(i, dichiarazione);
	}
	
	@Override
	protected int getFirstLetterOfDichiarazioni() {
		return 97;
	}
	
	@Override
	public String getNameRicevutaReportModel(Session cmisSession,
			Folder application, Locale locale) throws CMISApplicationException {
		Folder call = (Folder) cmisSession.getObject(application.getParentId());
		LocalDate data = Optional.ofNullable(application.<Calendar>getPropertyValue((JCONONPropertyIds.APPLICATION_DATA_DOMANDA.value()))).
				map(x -> x.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).orElse(LocalDate.now());
		return call.getPropertyValue(JCONONPropertyIds.CALL_CODICE.value())+
				"-RD-" +
				application.getPropertyValue(JCONONPropertyIds.APPLICATION_USER.value())+
				"-" +
				data +
				".pdf";
	}

	private OperationContext getMinimalContext(Session session){
        OperationContext context = new OperationContextImpl(session.getDefaultContext());
        context.setIncludeAcls(false);
        context.setIncludeAllowableActions(false);
        context.setIncludePathSegments(false);
        return context;
    }

	private Stream<Folder> getAllApplication(Session session) {
	    final ItemIterable<CmisObject> cmisObjects = session.queryObjects(JCONONFolderType.JCONON_APPLICATION.value(),
                "NOT jconon_application:stato_domanda = 'I' AND IN_TREE('" + session.getRootFolder().getId() + "')",
                false, getMinimalContext(session));
        List<Folder> applicationList = new ArrayList<Folder>();
        for (CmisObject application : cmisObjects.getPage(Integer.MAX_VALUE)) {
            applicationList.add((Folder)application);
        }
        return applicationList.stream().sorted((app1, app2) ->
                Optional.ofNullable(app1.<Calendar>getPropertyValue(JCONONPropertyIds.APPLICATION_DATA_DOMANDA.value())).orElse(Calendar.getInstance()).compareTo(
                        Optional.ofNullable(app2.<Calendar>getPropertyValue(JCONONPropertyIds.APPLICATION_DATA_DOMANDA.value())).orElse(Calendar.getInstance())));
    }

    private int generateAllEsperienze(Session session, Folder applicationObject, int applicationNumber, int index, CMISUser user, HSSFSheet sheet) {
        Criteria criteriaOIV = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_DOCUMENT);
        criteriaOIV.add(Restrictions.inTree(applicationObject.getId()));
        ItemIterable<QueryResult> iterableOIV = criteriaOIV.executeQuery(session, false, getMinimalContext(session));
        for (QueryResult oiv : iterableOIV.getPage(Integer.MAX_VALUE)) {
            Document oivObject = (Document) session.getObject(String.valueOf(oiv.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue()), getMinimalContext(session));
            getRecordCSV(session, applicationObject, oivObject, applicationNumber, user, sheet, null, null, index++);
        }
        return index;
    }

    private int generateLastEsperienze(Session session, Folder applicationObject, int applicationNumber, int index, CMISUser user, HSSFSheet sheet) {
        QueryResult lastEsperienza = getLastEsperienza(session, applicationObject, getMinimalContext(session));
        getRecordCSV(session, applicationObject, null, applicationNumber, user, sheet,
                Optional.ofNullable(lastEsperienza).map(x -> x.<String>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_RUOLO)).orElse(""),
                Optional.ofNullable(lastEsperienza).map(x -> x.<String>getPropertyValueById(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DATORE_LAVORO)).orElse(""),
                index++);
        return index;
    }

	public HSSFWorkbook generateXLS(Session session, boolean detail, boolean withEsperienze) {
    	HSSFWorkbook wb = createHSSFWorkbook(detail ? headDetailCSVApplication : headCSVApplication);
    	HSSFSheet sheet = wb.getSheet(SHEET_DOMANDE);
    	int index = 1;
        Stream<Folder> sorted = getAllApplication(session);
        if (withEsperienze) {
			int applicationNumber = 0;
			for (Folder applicationObject : sorted.collect(Collectors.toList())) {
				applicationNumber++;
				if (detail) {
                    index = generateAllEsperienze(session, applicationObject, applicationNumber, index, null, sheet);
				} else {
                    index = generateLastEsperienze(session, applicationObject, applicationNumber, index, null, sheet);
				}
			}
		}
        autoSizeColumns(wb);
        return wb;
	}

	public HSSFWorkbook createHSSFWorkbookAllEsperienze() {
        return createHSSFWorkbook(headDetailCSVApplication);
    }

    public HSSFWorkbook createHSSFWorkbookLastEsperienze() {
        return createHSSFWorkbook(headCSVApplication);
    }

    public void generateXLS(Session session, HSSFWorkbook wbAllEsperienze, HSSFWorkbook wbLastEsperienze) {
        HSSFSheet sheetAllEsperienze = wbAllEsperienze.getSheet(SHEET_DOMANDE);
        HSSFSheet sheetLastEsperienze = wbLastEsperienze.getSheet(SHEET_DOMANDE);
        int indexAllEsperienze = 1;
        int indexLastEsperienze = 1;
        Stream<Folder> sorted = getAllApplication(session);
        int applicationNumber = 0;
        for (Folder applicationObject : sorted.collect(Collectors.toList())) {
            CMISUser user = userService.loadUserForConfirm(applicationObject.getPropertyValue("jconon_application:user"));
            applicationNumber++;
            indexAllEsperienze = generateAllEsperienze(session, applicationObject, applicationNumber, indexAllEsperienze, user, sheetAllEsperienze);
            indexLastEsperienze = generateLastEsperienze(session, applicationObject, applicationNumber, indexLastEsperienze, user, sheetLastEsperienze);
        }
        autoSizeColumns(wbAllEsperienze);
        autoSizeColumns(wbLastEsperienze);
    }

    public Map<String, Object> extractionApplicationForAllIscritti(
            Session session, String query, String contexURL, String userId)
            throws IOException {
        Map<String, Object> model = new HashMap<String, Object>();
        HSSFWorkbook wb = generateXLSAllIscritti(session, query);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        wb.write(stream);
        Files.write(Paths.get("/home/mspasiano/iscritti_oiv.xls"), stream.toByteArray());

        //Document doc = createXLSDocument(session, wb, userId);
        //model.put("objectId", doc.getId());
        //model.put("nameBando", "OIV");
        return model;
    }

    public HSSFWorkbook generateXLSAllIscritti(Session session, String query) {
        HSSFWorkbook wb = createHSSFWorkbook(headCSVApplicationAllIscritti);
        HSSFSheet sheet = wb.getSheet(SHEET_DOMANDE);
        int index = 1;
        ItemIterable<QueryResult> applications = session.query(query, false);
        OperationContext context = session.getDefaultContext();
        context.setIncludeAcls(true);
        int applicationNumber = 0;
        for (QueryResult application : applications.getPage(Integer.MAX_VALUE)) {
            Folder applicationObject = (Folder) session.getObject(String.valueOf(application.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue()), context);
            CMISUser user = new CMISUser("admin");// userService.loadUserForConfirm(applicationObject.getPropertyValue("jconon_application:user"));
            applicationNumber++;
            Criteria criteriaOIV = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_DOCUMENT);
            criteriaOIV.add(Restrictions.inFolder(applicationObject.getId()));
            ItemIterable<QueryResult> iterableOIV = criteriaOIV.executeQuery(session, false, session.getDefaultContext());
            for (QueryResult oiv : iterableOIV.getPage(Integer.MAX_VALUE)) {
                Document oivObject = (Document) session.getObject(String.valueOf(oiv.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue()));
                getRecordCSVAllIscritti(session, applicationObject, oivObject, applicationNumber, user, sheet, null, null, index++);
            }
        }
        autoSizeColumns(wb);
        return wb;
    }

	private QueryResult getLastEsperienza(Session session, Folder applicationObject, OperationContext context) {
    	Criteria criteriaEsperienza = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_ESPERIENZA_PROFESSIONALE);
    	criteriaEsperienza.addColumn(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_RUOLO);
    	criteriaEsperienza.addColumn(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_DATORE_LAVORO);    	
		criteriaEsperienza.add(Restrictions.inTree(applicationObject.getId()));
		criteriaEsperienza.addOrder(Order.desc(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A));
		ItemIterable<QueryResult> iterableEsperienza = criteriaEsperienza.executeQuery(session, false, context);
		for (QueryResult esperienza : iterableEsperienza.getPage(1)) {
			return esperienza;
		}
		return null;
	}
	@Override
	public Map<String, Object> extractionApplicationForSingleCall(
			Session session, String query, String contexURL, String userId)
			throws IOException {
    	Map<String, Object> model = new HashMap<String, Object>();
    	HSSFWorkbook wb = generateXLS(session, false, true);
        Document doc = createXLSDocument(session, wb, userId);
        model.put("objectId", doc.getId());
        model.put("nameBando", "OIV");        
		return model;
	}

	public HSSFWorkbook getWorkbookForElenco(Session session, String query, String userId, String callId) throws IOException{
    	HSSFWorkbook wb = createHSSFWorkbook(headCSVElenco);
    	HSSFSheet sheet = wb.getSheet(SHEET_DOMANDE);
    	Criteria criteria = CriteriaFactory.createCriteria(JCONONFolderType.JCONON_APPLICATION.queryName());    	
		criteria.addColumn(PropertyIds.OBJECT_ID);
		criteria.add(Restrictions.inTree(callId));
		criteria.add(Restrictions.isNotNull("jconon_application:progressivo_iscrizione_elenco"));
		criteria.addOrder(Order.asc("jconon_application:progressivo_iscrizione_elenco"));
		ItemIterable<QueryResult> iterable = criteria.executeQuery(session, false, session.getDefaultContext());
		int index = 1;
    	for (QueryResult queryResult : iterable.getPage(Integer.MAX_VALUE)) {
    		Folder application = (Folder) session.getObject(String.valueOf(queryResult.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue()));
            /**
             * Test se rimosso dall'elenco
             */
    		if (!Optional.ofNullable(application.getProperty("jconon_application:fl_rimosso_elenco"))
                    .map(objectProperty -> objectProperty.<Boolean>getValue())
                    .orElse(false)) {
                getRecordElencoCSV(session, application, sheet, index++);
            }
    	}
    	autoSizeColumns(wb);
    	return wb;
	}
	
	public Map<String, Object> extractionApplicationForElenco(Session session, String query, String userId, String callId) throws IOException{
		Map<String, Object> model = new HashMap<String, Object>();
    	HSSFWorkbook wb = getWorkbookForElenco(session, query, userId, callId);
        Document doc = createXLSDocument(session, wb, userId);
        model.put("objectId", doc.getId());
		return model;
	}
	
	private void getRecordElencoCSV(Session session, Folder application, HSSFSheet sheet, int index) {
    	int column = 0;
    	HSSFRow row = sheet.createRow(index);
    	row.createCell(column++).setCellValue(Optional.ofNullable(application.getPropertyValue("jconon_application:progressivo_iscrizione_elenco")).
    			map(numero -> String.valueOf(numero)).orElse(""));
    	row.createCell(column++).setCellValue(application.<String>getPropertyValue("jconon_application:nome").toUpperCase());    	
    	row.createCell(column++).setCellValue(application.<String>getPropertyValue("jconon_application:cognome").toUpperCase());
    	row.createCell(column++).setCellValue(Optional.ofNullable(application.getPropertyValue("jconon_application:data_iscrizione_elenco")).map(map -> 
			dateFormat.format(((Calendar)application.getPropertyValue("jconon_application:data_iscrizione_elenco")).getTime())).orElse(""));
	}

	public String archiviaRicevutaReportModel(Session cmisSession, Folder application,Map<String, Object> properties,
			InputStream is, String nameRicevutaReportModel, boolean confermata) throws CMISApplicationException {
		try {
			ContentStream contentStream = new ContentStreamImpl(nameRicevutaReportModel,
					BigInteger.valueOf(is.available()),
					"application/pdf",
					is);
			String docId = findRicevutaApplicationId(cmisSession, application);
			if (docId!=null) {
				try{
					Document doc = (Document) cmisSession.getObject(docId);
					if (confermata) {
						doc.updateProperties(properties, true);
						doc.setContentStream(contentStream, true, true);
						doc = doc.getObjectOfLatestVersion(false);
						LOGGER.info("Start checkin application:{} with name {}", doc.getId(), nameRicevutaReportModel);
						docId = checkInPrint(cmisService.getAdminSession(), doc.<String>getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), is, nameRicevutaReportModel);
						LOGGER.info("End checkin application:{} with name {}", doc.getId(), nameRicevutaReportModel);
					} else {
						doc = cmisSession.getLatestDocumentVersion(doc.updateProperties(properties, true));
						doc.setContentStream(contentStream, true, true);
						doc = doc.getObjectOfLatestVersion(false);
						docId = doc.getId();						
					}
				}catch (CmisObjectNotFoundException e) {
					LOGGER.warn("cmis object not found {}", nameRicevutaReportModel, e);
					docId = createApplicationDocument(application, contentStream, properties);
				}catch(CmisStreamNotSupportedException ex) {
					LOGGER.error("Cannot set Content Stream on id:"+ docId + " ------" + ex.getErrorContent(), ex);
					throw ex;
				}
			} else {
				docId = createApplicationDocument(application, contentStream, properties);
			}
			return docId;
		} catch(CmisContentAlreadyExistsException _ex) {
			LOGGER.warn("File della domanda {} alredy exist", nameRicevutaReportModel, _ex);
			throw new ClientMessageException("Il file " + nameRicevutaReportModel +" è già presente come allegato!");
		} catch (Exception e) {
			throw new CMISApplicationException("Error in JASPER", e);
		}
	}

	@Override
	protected boolean isConfirmed(Folder application) {
		return false;
	}
	
	private String createApplicationDocument(Folder application, ContentStream contentStream, Map<String, Object> properties){
		Document doc = application.createDocument(properties, contentStream, VersioningState.MINOR);
		nodeVersionService.addAutoVersion(doc, false);
		return doc.getId();
	}	

    private void getRecordCSV(Session session, Folder applicationObject, Document oivObject, int applicationNumber, CMISUser user, HSSFSheet sheet, String lastRuolo, String lastDatoreLavoro, int index) {
    	int column = 0;
    	HSSFRow row = sheet.createRow(index);
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getPropertyValue("jconon_application:progressivo_iscrizione_elenco")).
    			map(numero -> String.valueOf(numero)).orElse(""));
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getPropertyValue("jconon_application:data_iscrizione_elenco")).map(map -> 
			dateFormat.format(((Calendar)applicationObject.getPropertyValue("jconon_application:data_iscrizione_elenco")).getTime())).orElse(""));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:cognome").toUpperCase());
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:nome").toUpperCase());    	
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getProperty("jconon_application:data_nascita").getValue()).map(
    			map -> dateFormat.format(((Calendar)map).getTime())).orElse(""));    	
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:sesso"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:nazione_nascita"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:comune_nascita"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:provincia_nascita"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:nazione_residenza"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:provincia_residenza"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:comune_residenza"));   			
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getProperty("jconon_application:indirizzo_residenza")).map(Property::getValueAsString).orElse("").concat(" - ").concat(
    					Optional.ofNullable(applicationObject.getProperty("jconon_application:num_civico_residenza")).map(Property::getValueAsString).orElse("")));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:cap_residenza"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:codice_fiscale"));
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:email_comunicazioni"))
                .filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(user).map(CMISUser::getEmail).orElse("")));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:email_pec_comunicazioni"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:nazione_comunicazioni"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:provincia_comunicazioni"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:comune_comunicazioni"));
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getProperty("jconon_application:indirizzo_comunicazioni")).map(Property::getValueAsString).orElse("").concat(" - ").concat(
    					Optional.ofNullable(applicationObject.getProperty("jconon_application:num_civico_comunicazioni")).map(Property::getValueAsString).orElse("")));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:cap_comunicazioni"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:telefono_comunicazioni"));
    	
    	Calendar data = Optional.ofNullable(applicationObject.<Calendar>getPropertyValue("jconon_application:data_domanda")).orElse(
    			applicationObject.<Calendar>getPropertyValue("jconon_application:data_ultimo_invio"));    	
    	row.createCell(column++).setCellValue(Optional.ofNullable(data).map(map -> 
    			dateTimeFormat.format((data).getTime())).orElse(""));
    	
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:tipo_laurea"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:istituto_laurea"));
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:fascia_professionale_attribuita")).orElse("")); 
    	row.createCell(column++).setCellValue(ApplicationService.StatoDomanda.fromValue(applicationObject.getPropertyValue("jconon_application:stato_domanda")).displayValue());
    	if (applicationObject.getAcl() != null && applicationObject.getAcl().getAces().stream().anyMatch(
    			x -> x.isDirect() && x.getPermissions().stream().anyMatch(permission -> permission.contains(ACLType.Consumer.name()))
    					&& x.getPrincipal().getId().equals(applicationObject.<String>getPropertyValue("jconon_application:user")))) {
    		if (applicationObject.<String>getPropertyValue("jconon_application:esclusione_rinuncia") != null){
        		row.createCell(column++).setCellValue("ESCLUSA");        			        			
    		} else {
        		row.createCell(column++).setCellValue("INVIATA");        			
    		}
    	} else {
    		row.createCell(column++).setCellValue("MODIFICA PROFILO");
    	}
    	if (oivObject != null) {
        	row.createCell(column++).setCellValue(oivObject.getType().getDisplayName());
        	if (oivObject.getType().getId().equalsIgnoreCase("D:jconon_scheda_anonima:precedente_incarico_oiv")) {
        		row.createCell(column++).setCellValue("");
        		row.createCell(column++).setCellValue("");    		
        		row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:precedente_incarico_oiv_ruolo"));
            	row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_da")).map(map -> 
        			dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_da")).getTime())).orElse(""));
            	row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_a")).map(map -> 
        			dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_a")).getTime())).orElse(""));    		
        	} else if (oivObject.getType().getId().equalsIgnoreCase("D:jconon_scheda_anonima:esperienza_professionale")) {
        		row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_professionale_area_specializzazione")).orElse(""));
        		row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_professionale_attivita_svolta")).orElse(""));    		
        		row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_RUOLO));
        		row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:esperienza_professionale_da")).map(map -> 
        			dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:esperienza_professionale_da")).getTime())).orElse(""));
            	row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A)).map(map -> 
        			dateFormat.format(((Calendar)oivObject.getPropertyValue(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A)).getTime())).orElse(""));    		    		
        	}
        	row.createCell(column++).setCellValue(oivObject.getSecondaryTypes().stream().anyMatch(x -> x.getId().equals(ApplicationOIVService.P_JCONON_SCHEDA_ANONIMA_ESPERIENZA_NON_COERENTE)));
        	row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_non_coerente_motivazione")).orElse(""));
        	row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_annotazione_motivazione")).orElse(""));
    	} else {
    		row.createCell(column++).setCellValue(lastRuolo);
    		row.createCell(column++).setCellValue(lastDatoreLavoro);    		
        	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_attachment:esperienza_annotazione_motivazione")).orElse(""));
    	}
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:fascia_professionale_validata")).orElse(""));
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<Boolean>getPropertyValue("jconon_application:fl_occupato")).map(x -> x ? SI : NO).orElse(""));
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:non_occupato")).orElse(""));    	
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<Boolean>getPropertyValue("jconon_application:fl_dipendente_pubblico")).map(x -> x ? SI : NO).orElse(""));
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:situazione_lavorativa_settore")).orElse(""));    	    	
       	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:situazione_lavorativa_ruolo")).orElse(""));    	    	
       	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:situazione_lavorativa_datore_lavoro")).orElse(""));    	    	
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getPropertyValue("jconon_application:situazione_lavorativa_data_inizio_lavoro")).map(map -> 
			dateFormat.format(((Calendar)applicationObject.getPropertyValue("jconon_application:situazione_lavorativa_data_inizio_lavoro")).getTime())).orElse(""));    		   	
   }

    private void getRecordCSVAllIscritti(Session session, Folder applicationObject, Document oivObject, int applicationNumber, CMISUser user, HSSFSheet sheet, String lastRuolo, String lastDatoreLavoro, int index) {
        int column = 0;
        HSSFRow row = sheet.createRow(index);
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getPropertyValue("jconon_application:progressivo_iscrizione_elenco")).
                map(numero -> String.valueOf(numero)).orElse(""));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getPropertyValue("jconon_application:data_iscrizione_elenco")).map(map ->
                dateFormat.format(((Calendar)applicationObject.getPropertyValue("jconon_application:data_iscrizione_elenco")).getTime())).orElse(""));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:cognome").toUpperCase());
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:nome").toUpperCase());
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getProperty("jconon_application:data_nascita").getValue()).map(
                map -> dateFormat.format(((Calendar)map).getTime())).orElse(""));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:sesso"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:nazione_nascita"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:comune_nascita"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:provincia_nascita"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:nazione_residenza"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:provincia_residenza"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:comune_residenza"));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getProperty("jconon_application:indirizzo_residenza")).map(Property::getValueAsString).orElse("").concat(" - ").concat(
                Optional.ofNullable(applicationObject.getProperty("jconon_application:num_civico_residenza")).map(Property::getValueAsString).orElse("")));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:cap_residenza"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:codice_fiscale"));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:email_comunicazioni")).filter(s -> !s.isEmpty()).orElse(user.getEmail()));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:email_pec_comunicazioni"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:nazione_comunicazioni"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:provincia_comunicazioni"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:comune_comunicazioni"));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getProperty("jconon_application:indirizzo_comunicazioni")).map(Property::getValueAsString).orElse("").concat(" - ").concat(
                Optional.ofNullable(applicationObject.getProperty("jconon_application:num_civico_comunicazioni")).map(Property::getValueAsString).orElse("")));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:cap_comunicazioni"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:telefono_comunicazioni"));

        Calendar data = Optional.ofNullable(applicationObject.<Calendar>getPropertyValue("jconon_application:data_domanda")).orElse(
                applicationObject.<Calendar>getPropertyValue("jconon_application:data_ultimo_invio"));
        row.createCell(column++).setCellValue(Optional.ofNullable(data).map(map ->
                dateTimeFormat.format((data).getTime())).orElse(""));

        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:tipo_laurea"));
        row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:istituto_laurea"));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:fascia_professionale_attribuita")).orElse(""));

        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:fascia_professionale_validata")).orElse(""));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<Boolean>getPropertyValue("jconon_application:fl_occupato")).map(x -> x ? SI : NO).orElse(""));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:non_occupato")).orElse(""));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<Boolean>getPropertyValue("jconon_application:fl_dipendente_pubblico")).map(x -> x ? SI : NO).orElse(""));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:situazione_lavorativa_settore")).orElse(""));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:situazione_lavorativa_ruolo")).orElse(""));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.<String>getPropertyValue("jconon_application:situazione_lavorativa_datore_lavoro")).orElse(""));
        row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getPropertyValue("jconon_application:situazione_lavorativa_data_inizio_lavoro")).map(map ->
                dateFormat.format(((Calendar)applicationObject.getPropertyValue("jconon_application:situazione_lavorativa_data_inizio_lavoro")).getTime())).orElse(""));

        row.createCell(column++).setCellValue(ApplicationService.StatoDomanda.fromValue(applicationObject.getPropertyValue("jconon_application:stato_domanda")).displayValue());
        if (applicationObject.getAcl() != null && applicationObject.getAcl().getAces().stream().anyMatch(
                x -> x.isDirect() && x.getPermissions().stream().anyMatch(permission -> permission.contains(ACLType.Consumer.name()))
                        && x.getPrincipal().getId().equals(applicationObject.<String>getPropertyValue("jconon_application:user")))) {
            if (applicationObject.<String>getPropertyValue("jconon_application:esclusione_rinuncia") != null){
                row.createCell(column++).setCellValue("ESCLUSA");
            } else {
                row.createCell(column++).setCellValue("INVIATA");
            }
        } else {
            row.createCell(column++).setCellValue("MODIFICA PROFILO");
        }
        if (oivObject != null) {
            if (oivObject.getType().getId().equalsIgnoreCase("D:jconon_scheda_anonima:precedente_incarico_oiv")) {
                row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_da")).map(map ->
                        dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_da")).getTime())).orElse(""));
                row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_a")).map(map ->
                        dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_a")).getTime())).orElse(""));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:precedente_incarico_oiv_amministrazione"));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:precedente_incarico_oiv_sede"));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:precedente_incarico_oiv_comune"));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:precedente_incarico_oiv_indirizzo"));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:precedente_incarico_oiv_cap"));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:precedente_incarico_oiv_telefono"));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:precedente_incarico_oiv_numero_dipendenti"));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:precedente_incarico_oiv_ruolo"));
                column = column + 8;
                row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_annotazione_motivazione")).orElse(""));
            } else if (oivObject.getType().getId().equalsIgnoreCase("D:jconon_scheda_anonima:esperienza_professionale")) {
                column = column + 10;
                row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:esperienza_professionale_da")).map(map ->
                        dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:esperienza_professionale_da")).getTime())).orElse(""));
                row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A)).map(map ->
                        dateFormat.format(((Calendar)oivObject.getPropertyValue(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_A)).getTime())).orElse(""));
                row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_professionale_area_specializzazione")).orElse(""));
                row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_professionale_attivita_svolta")).orElse(""));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_professionale_datore_lavoro"));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue(JCONON_ATTACHMENT_ESPERIENZA_PROFESSIONALE_RUOLO));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_professionale_citta"));
                row.createCell(column++).setCellValue(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_professionale_stato_estero"));
                row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.<String>getPropertyValue("jconon_attachment:esperienza_annotazione_motivazione")).orElse(""));
            }
        }
    }
}