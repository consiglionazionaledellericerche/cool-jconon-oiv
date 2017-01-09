package it.cnr.si.cool.jconon.service.application;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.util.Pair;
import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.service.PrintService;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
@Component
@Primary
public class PrintOIVService extends PrintService {
	public static final String P_JCONON_APPLICATION_ASPECT_FASCIA_PROFESSIONALE_ATTRIBUITA = "P:jconon_application:aspect_fascia_professionale_attribuita";
	
	@Autowired
	private CMISService cmisService;
	
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
}
