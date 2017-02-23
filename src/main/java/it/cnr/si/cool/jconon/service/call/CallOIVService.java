package it.cnr.si.cool.jconon.service.call;

import it.cnr.cool.cmis.model.ACLType;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.ACLService;
import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.I18nService;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.cmis.model.JCONONFolderType;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.repository.CacheRepository;
import it.cnr.si.cool.jconon.service.cache.CompetitionFolderService;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CallOIVService extends CallService {
    private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_DATA_PUBBLICAZIONE_ESITO = "jconon_call_procedura_comparativa:data_pubblicazione_esito";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_PUBBLICATO_ESITO = "jconon_call_procedura_comparativa:pubblicato_esito";
	private static final String SELEZIONATO = "selezionato";
	private static final String JCONON_ATTACHMENT_ESITO_PARTECIPANTI_ESITO = "jconon_attachment:esito_partecipanti_esito";
	private static final String D_JCONON_ATTACHMENT_CALL_FP_ESITO_VERBALE = "D:jconon_attachment:call_fp_esito_verbale";
	private static final String D_JCONON_ATTACHMENT_CALL_FP_ESITO_ALTRI_DOCUMENTI = "D:jconon_attachment:call_fp_esito_altri_documenti";
	private static final String D_JCONON_ATTACHMENT_CALL_FP_ESITO_PARTECIPANTI = "D:jconon_attachment:call_fp_esito_partecipanti";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_AMMINISTRAZIONE = "jconon_call_procedura_comparativa:amministrazione";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_FOLDER = "jconon_call_procedura_comparativa:folder";
	private static final String JCONON_ATTACHMENT_CALL_FP = "jconon_attachment:call_fp";
	private static final Logger LOGGER = LoggerFactory.getLogger(CallOIVService.class);	
	private static final String F_JCONON_CALL_PROCEDURA_COMPARATIVA_FOLDER = "F:jconon_call_procedura_comparativa:folder";
    @Autowired
    private I18nService i18NService;
    @Autowired
    private FolderService folderService;
	@Autowired
	private CompetitionFolderService competitionService;
    @Autowired
    private ACLService aclService;
    @Autowired
    private UserService userService;
	@Autowired
    private CacheRepository cacheRepository;

	public Folder publishEsito(Session cmisSession,
			BindingSession currentBindingSession, String objectId, boolean publish, String userId) {
        final Folder call = (Folder) cmisSession.getObject(objectId);
        Calendar today = Calendar.getInstance();
        CMISUser user = userService.loadUserForConfirm(userId);    	
        if (!publish && !(user.isAdmin() || isMemeberOfConcorsiGroup(user))) {
        	throw new ClientMessageException("message.error.call.cannnot.modify");
        }
        call.getChildren().forEach(cmisObject -> {
        	if (Arrays.asList(
        			D_JCONON_ATTACHMENT_CALL_FP_ESITO_VERBALE, 
        			D_JCONON_ATTACHMENT_CALL_FP_ESITO_ALTRI_DOCUMENTI, 
        			D_JCONON_ATTACHMENT_CALL_FP_ESITO_PARTECIPANTI
        		).stream().anyMatch(x -> x.equals(cmisObject.getType().getId()))) {
        		if (D_JCONON_ATTACHMENT_CALL_FP_ESITO_PARTECIPANTI.equals(cmisObject.getType().getId()) && 
        				cmisObject.getPropertyValue(JCONON_ATTACHMENT_ESITO_PARTECIPANTI_ESITO).equals(SELEZIONATO) ) {
        			aclService.setInheritedPermission(currentBindingSession, cmisObject.<String>getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), publish);
        		} else if (!D_JCONON_ATTACHMENT_CALL_FP_ESITO_PARTECIPANTI.equals(cmisObject.getType().getId())){
        			aclService.setInheritedPermission(currentBindingSession, cmisObject.<String>getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), publish);
        		}
        	}
        });
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(JCONON_CALL_PROCEDURA_COMPARATIVA_PUBBLICATO_ESITO, publish);        
        properties.put(JCONON_CALL_PROCEDURA_COMPARATIVA_DATA_PUBBLICAZIONE_ESITO, publish ? today : null);
        call.updateProperties(properties, true);
        return call;
	}
	@Override
	public Folder publish(Session cmisSession,
			BindingSession currentBindingSession, String userId,
			String objectId, boolean publish, String contextURL, Locale locale) {
        final Folder call = (Folder) cmisSession.getObject(objectId);
        Calendar today = Calendar.getInstance();
        today.add(Calendar.HOUR, -1);
        if (call.getType().getId().equalsIgnoreCase(F_JCONON_CALL_PROCEDURA_COMPARATIVA_FOLDER)) {
	        Map<String, ACLType> aces = new HashMap<String, ACLType>();
	        aces.put(GROUP_EVERYONE, ACLType.Consumer);
	    	CMISUser user = userService.loadUserForConfirm(userId);    	
	        if (!publish && !(user.isAdmin() || isMemeberOfConcorsiGroup(user))) {
	        	throw new ClientMessageException("message.error.call.cannnot.modify");
	        }

	        Criteria criteria = CriteriaFactory.createCriteria(JCONON_ATTACHMENT_CALL_FP);
	        criteria.add(Restrictions.inFolder(call.getId()));
            if (criteria.executeQuery(cmisSession, false, cmisSession.getDefaultContext()).getTotalNumItems() == 0)
                throw new ClientMessageException("message.error.call.attachment.not.present");

            Map<String, Object> properties = new HashMap<String, Object>();
	        properties.put(JCONONPropertyIds.CALL_PUBBLICATO.value(), publish);        
	        if (publish) {
		        Criteria criteriaProcedureComparative = CriteriaFactory.createCriteria(JCONON_CALL_PROCEDURA_COMPARATIVA_FOLDER);
		        criteriaProcedureComparative.add(Restrictions.eq(JCONON_CALL_PROCEDURA_COMPARATIVA_AMMINISTRAZIONE, call.getPropertyValue(JCONON_CALL_PROCEDURA_COMPARATIVA_AMMINISTRAZIONE)));
		        criteriaProcedureComparative.add(Restrictions.ne(PropertyIds.OBJECT_ID, call.getId()));
		        criteriaProcedureComparative.add(Restrictions.eq(JCONONPropertyIds.CALL_PUBBLICATO.value(), true));
		        criteriaProcedureComparative.add(Restrictions.ge(JCONONPropertyIds.CALL_DATA_FINE_INVIO_DOMANDE.value(), Calendar.getInstance().getTime()));
	            if (criteriaProcedureComparative.executeQuery(cmisSession, false, cmisSession.getDefaultContext()).getTotalNumItems() > 0)
	                throw new ClientMessageException("message.error.publish.call.alredy.active");

	        	properties.put(JCONONPropertyIds.CALL_DATA_INIZIO_INVIO_DOMANDE.value(), today);
	        	aclService.addAcl(currentBindingSession, call.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString(), aces);
	        } else {
		        properties.put(JCONONPropertyIds.CALL_DATA_INIZIO_INVIO_DOMANDE.value(), null);        
	        	aclService.removeAcl(currentBindingSession, call.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString(), aces);
	        }
	        call.updateProperties(properties, true);
	        return call;			
		} else {
			return super.publish(cmisSession, currentBindingSession, userId, objectId,
					publish, contextURL, locale);			
		}
	}
	
    public String getCodiceBandoTruncated(Folder call) {
    	if (call.getType().getId().equalsIgnoreCase(F_JCONON_CALL_PROCEDURA_COMPARATIVA_FOLDER)) {
    		return call.getName();
    	} else {
    		return super.getCodiceBandoTruncated(call);
    	}
    }
	
    @Override
    public void delete(Session cmisSession, String contextURL, String objectId,
    		String objectTypeId, String userId) {
        Folder call = (Folder) cmisSession.getObject(objectId);    	
    	CMISUser user = userService.loadUserForConfirm(userId);    	
        if ((Boolean)call.getPropertyValue(JCONONPropertyIds.CALL_PUBBLICATO.value()) && !(user.isAdmin() || isMemeberOfConcorsiGroup(user))) {
        	throw new ClientMessageException("message.error.call.cannnot.modify");
        }    	
        Criteria criteria = CriteriaFactory.createCriteria(JCONONFolderType.JCONON_APPLICATION.queryName());
        criteria.add(Restrictions.ne(JCONONPropertyIds.APPLICATION_STATO_DOMANDA.value(), "I"));
        criteria.add(Restrictions.inFolder(objectId));
        ItemIterable<QueryResult> applications = criteria.executeQuery(cmisSession, false, cmisSession.getDefaultContext());
        if (applications.getTotalNumItems() > 0)
            throw new ClientMessageException("message.error.call.cannot.delete");
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Try to delete :" + objectId);
        call.deleteTree(true, UnfileObject.DELETE, true);
    }
	@SuppressWarnings("unchecked")
	@Override
	public Folder save(Session cmisSession, BindingSession bindingSession,
			String contextURL, Locale locale, String userId,
			Map<String, Object> properties, Map<String, Object> aspectProperties) {
		String tipologiaBando = (String)properties.get(PropertyIds.OBJECT_TYPE_ID);
		if (tipologiaBando.equalsIgnoreCase(F_JCONON_CALL_PROCEDURA_COMPARATIVA_FOLDER)) {
	        Folder call = null;
	        properties.putAll(aspectProperties);
	        String amministrazione = (String)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_AMMINISTRAZIONE);
			
	        /**
	         * Verifico inizialmente se sto in creazione del Bando
	         */
	        if (amministrazione == null)
	            throw new ClientMessageException("message.error.required.amministrazione");
	        String name = amministrazione.concat("_").concat(UUID.randomUUID().toString());			
	        properties.put(PropertyIds.NAME, folderService.integrityChecker(name));
	        Optional<Calendar> dataFineInvioDomandeOpt = Optional.ofNullable((Calendar)properties.get(JCONONPropertyIds.CALL_DATA_FINE_INVIO_DOMANDE.value()));
	        Optional<String> oraFineInvioDomande = Optional.ofNullable((String)properties.get("jconon_call_procedura_comparativa:ora_fine_invio_domande"));
	        if (dataFineInvioDomandeOpt.isPresent()) {
	        	Calendar dataFineInvioDomande = Calendar.getInstance();
	        	dataFineInvioDomande.set(Calendar.YEAR, dataFineInvioDomandeOpt.get().get(Calendar.YEAR));
	        	dataFineInvioDomande.set(Calendar.MONTH, dataFineInvioDomandeOpt.get().get(Calendar.MONTH));
	        	dataFineInvioDomande.set(Calendar.DAY_OF_MONTH, dataFineInvioDomandeOpt.get().get(Calendar.DAY_OF_MONTH));
	        	dataFineInvioDomande.set(Calendar.SECOND, 59);
	        	if (oraFineInvioDomande.isPresent()) {
		        	dataFineInvioDomande.set(Calendar.HOUR_OF_DAY, Integer.valueOf(oraFineInvioDomande.get().split(":")[0]));
		        	dataFineInvioDomande.set(Calendar.MINUTE, Integer.valueOf(oraFineInvioDomande.get().split(":")[1]));	        		
	        	} else {
		        	dataFineInvioDomande.set(Calendar.HOUR_OF_DAY, 23);
		        	dataFineInvioDomande.set(Calendar.MINUTE, 59);
	        	}
	        	properties.put(JCONONPropertyIds.CALL_DATA_FINE_INVIO_DOMANDE.value(), dataFineInvioDomande);
	        }
	        Optional<String> numeroDipendentiOptional = Optional.ofNullable((String)properties.get("jconon_call_procedura_comparativa:numero_dipendenti"));
	        Optional<List<String>> fasciaProfessionaleOptional = Optional.ofNullable((List<String>)properties.get("jconon_call_procedura_comparativa:fascia_professionale"));
	        if (numeroDipendentiOptional.isPresent() && fasciaProfessionaleOptional.isPresent()  && 
	        	numeroDipendentiOptional.filter(x -> x.equals("Maggiore o uguale a 250")).isPresent() &&
	        			fasciaProfessionaleOptional.get().stream().anyMatch(x -> !x.equals("Fascia 3"))){
	        		throw new ClientMessageException("message.error.fascia.presidente");
	        }
	        Optional<String> tipologiaSelezioneOptional = Optional.ofNullable((String)properties.get("jconon_call_procedura_comparativa:tipologia_selezione"));
	        if (tipologiaSelezioneOptional.isPresent() && 
	        		Arrays.asList("Presidente", "Intero Collegio").stream().anyMatch(tipoSelezione -> tipoSelezione.equals(tipologiaSelezioneOptional.get())) &&
	        		!Optional.ofNullable((List<String>)properties.get("jconon_call_procedura_comparativa:fascia_professionale")).filter(x -> !x.isEmpty()).isPresent()) {
	        	throw new ClientMessageException("message.error.fascia.presidente.empty");
	        }	        
	        if (tipologiaSelezioneOptional.isPresent() && 
	        		Arrays.asList("Intero Collegio").stream().anyMatch(tipoSelezione -> tipoSelezione.equals(tipologiaSelezioneOptional.get())) && (
	        				!Optional.ofNullable((List<String>)properties.get("jconon_call_procedura_comparativa:fascia_professionale")).filter(x -> !x.isEmpty()).isPresent() ||
	        				!Optional.ofNullable((List<String>)properties.get("jconon_call_procedura_comparativa:fascia_professionale_comp2")).filter(x -> !x.isEmpty()).isPresent() ||
	        				!Optional.ofNullable((List<String>)properties.get("jconon_call_procedura_comparativa:fascia_professionale_comp3")).filter(x -> !x.isEmpty()).isPresent())
	        		) {
	        	throw new ClientMessageException("message.error.fascia.empty");
	        }	        
	        
	        if (properties.get(PropertyIds.OBJECT_ID) == null) {
                properties.put(PropertyIds.PARENT_ID, cacheRepository.getCompetitionFolder().getId());
                properties.put(JCONONPropertyIds.CALL_CODICE.value(), "");
                properties.put(JCONONPropertyIds.CALL_DESCRIZIONE.value(), "");
                properties.put(JCONONPropertyIds.CALL_REQUISITI_LINK.value(), "");
                properties.put(JCONONPropertyIds.CALL_REQUISITI.value(), "");
                properties.put(JCONONPropertyIds.CALL_ELENCO_ASSOCIATIONS.value(), Arrays.asList(""));
                properties.put(JCONONPropertyIds.CALL_HAS_MACRO_CALL.value(), Boolean.FALSE);
                properties.put(JCONONPropertyIds.CALL_ELENCO_SEZIONI_DOMANDA.value(), Arrays.asList(""));                
                call = (Folder) cmisSession.getObject(
	                    cmisSession.createFolder(properties, new ObjectIdImpl((String) properties.get(PropertyIds.PARENT_ID))));
	            aclService.setInheritedPermission(bindingSession, call.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString(), false);
	            creaGruppoRdP(call, userId);	            
	            Map<String, ACLType> aces = new HashMap<String, ACLType>();
	            aces.put(GROUP_CONCORSI, ACLType.Coordinator);
	            aclService.addAcl(bindingSession, call.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString(), aces);	            
	        } else {
	            call = (Folder) cmisSession.getObject((String) properties.get(PropertyIds.OBJECT_ID));
	        	CMISUser user = userService.loadUserForConfirm(userId); 
	        	if (isBandoInCorso(call)) {
		            if ((Boolean)call.getPropertyValue(JCONONPropertyIds.CALL_PUBBLICATO.value()) && !(user.isAdmin() || isMemeberOfConcorsiGroup(user))) {
		            	throw new ClientMessageException("message.error.call.cannnot.modify");
		            }            	        		
	        	} else {
		            if ((Boolean)call.getPropertyValue(JCONON_CALL_PROCEDURA_COMPARATIVA_PUBBLICATO_ESITO) && !(user.isAdmin() || isMemeberOfConcorsiGroup(user))) {
		            	throw new ClientMessageException("message.error.call.cannnot.modify");
		            }            	        			        		
	        	}
	            call.updateProperties(properties, true);
	        }
			return call;
		} else {
			return super.save(cmisSession, bindingSession, contextURL, locale, userId,
					properties, aspectProperties);			
		}
	}
	
}
