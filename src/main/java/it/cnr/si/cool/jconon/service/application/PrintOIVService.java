package it.cnr.si.cool.jconon.service.application;

import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.service.PrintService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
@Component
@Primary
public class PrintOIVService extends PrintService {
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
