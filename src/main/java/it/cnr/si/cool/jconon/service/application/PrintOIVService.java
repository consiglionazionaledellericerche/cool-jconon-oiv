package it.cnr.si.cool.jconon.service.application;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.util.Pair;
import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.service.PrintService;
import it.cnr.si.cool.jconon.service.cache.CompetitionFolderService;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
@Component
@Primary
public class PrintOIVService extends PrintService {
	private static final String JCONON_SCHEDA_ANONIMA_DOCUMENT = "jconon_scheda_anonima:document";
	public static final String P_JCONON_APPLICATION_ASPECT_FASCIA_PROFESSIONALE_ATTRIBUITA = "P:jconon_application:aspect_fascia_professionale_attribuita";
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"), 
			dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");	
	private List<String> headCSVApplication = Arrays.asList(
			"Id","Cognome","Nome","Data di nascita","Sesso","Nazione di nascita",
			"Luogo di nascita","Prov. di nascita","Nazione di Residenza","Provincia di Residenza",
			"Comune di Residenza","Indirizzo di Residenza","CAP di Residenza","Codice Fiscale",
			"Email","Email PEC","Nazione Reperibilita'","Provincia di Reperibilita'",
			"Comune di Reperibilita'","Indirizzo di Reperibilita'",
			"CAP di Reperibilita'","Telefono","Data Invio Domanda",
			"Laurea", "Università",
			"Tipologia esperienza (Professionale/OIV)",
			"Data inizio(Tipologia esperienza)",
			"Data fine(Tipologia esperienza)"
			);
    private static final String SHEET_DOMANDE = "domande";
	
	@Autowired
	private CMISService cmisService;
	@Autowired
	private UserService userService;
	@Autowired
	private CompetitionFolderService competitionService;
	
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
	
	@Override
	public Map<String, Object> extractionApplicationForSingleCall(
			Session session, String query, String contexURL, String userId)
			throws IOException {
    	Map<String, Object> model = new HashMap<String, Object>();
    	HSSFWorkbook wb = createHSSFWorkbook(headCSVApplication);
    	HSSFSheet sheet = wb.getSheet(SHEET_DOMANDE);
    	int index = 1;
        Folder callObject = null;
        ItemIterable<QueryResult> applications = session.query(query, false);
        for (QueryResult application : applications.getPage(Integer.MAX_VALUE)) {
        	Folder applicationObject = (Folder) session.getObject(String.valueOf(application.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue()));
        	callObject = (Folder) session.getObject(applicationObject.getParentId());
        	CMISUser user = userService.loadUserForConfirm(applicationObject.getPropertyValue("jconon_application:user"));

        	Criteria criteriaOIV = CriteriaFactory.createCriteria(JCONON_SCHEDA_ANONIMA_DOCUMENT);
    		criteriaOIV.add(Restrictions.inFolder(applicationObject.getId()));
    		ItemIterable<QueryResult> iterableOIV = criteriaOIV.executeQuery(session, false, session.getDefaultContext());
    		for (QueryResult oiv : iterableOIV.getPage(Integer.MAX_VALUE)) {
            	Document oivObject = (Document) session.getObject(String.valueOf(oiv.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue()));            	
            	getRecordCSV(session, callObject, applicationObject, oivObject, user, contexURL, sheet, index++);    			
    		}
		}
        autoSizeColumns(wb);
        Document doc = createXLSDocument(session, wb, userId);
        model.put("objectId", doc.getId());
        model.put("nameBando", competitionService.getCallName(callObject));        
		return model;
	}
	
    private void getRecordCSV(Session session, Folder callObject, Folder applicationObject, Document oivObject, CMISUser user, String contexURL, HSSFSheet sheet, int index) {
    	int column = 0;
    	HSSFRow row = sheet.createRow(index);
    	row.createCell(column++).setCellValue(user.getId());
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
    	row.createCell(column++).setCellValue(Optional.ofNullable(applicationObject.getPropertyValue("jconon_application:data_domanda")).map(map -> 
    			dateTimeFormat.format(((Calendar)applicationObject.getPropertyValue("jconon_application:data_domanda")).getTime())).orElse(""));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:tipo_laurea"));
    	row.createCell(column++).setCellValue(applicationObject.<String>getPropertyValue("jconon_application:istituto_laurea"));
    	row.createCell(column++).setCellValue(oivObject.getType().getDisplayName());
    	if (oivObject.getType().getId().equalsIgnoreCase("D:jconon_scheda_anonima:precedente_incarico_oiv")) {
        	row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_da")).map(map -> 
    			dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_da")).getTime())).orElse(""));
        	row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_a")).map(map -> 
    			dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:precedente_incarico_oiv_a")).getTime())).orElse(""));    		
    	} else if (oivObject.getType().getId().equalsIgnoreCase("D:jconon_scheda_anonima:esperienza_professionale")) {
        	row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:esperienza_professionale_da")).map(map -> 
    			dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:esperienza_professionale_da")).getTime())).orElse(""));
        	row.createCell(column++).setCellValue(Optional.ofNullable(oivObject.getPropertyValue("jconon_attachment:esperienza_professionale_a")).map(map -> 
    			dateFormat.format(((Calendar)oivObject.getPropertyValue("jconon_attachment:esperienza_professionale_a")).getTime())).orElse(""));    		    		
    	}
    }
}