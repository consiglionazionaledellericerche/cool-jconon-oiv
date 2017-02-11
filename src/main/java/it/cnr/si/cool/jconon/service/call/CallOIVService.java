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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CallOIVService.class);	
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_AMMINISTRAZIONE = "jconon_call_procedura_comparativa:amministrazione";
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

	@Override
	public Folder publish(Session cmisSession,
			BindingSession currentBindingSession, String userId,
			String objectId, boolean publish, String contextURL, Locale locale) {
        final Folder call = (Folder) cmisSession.getObject(objectId);
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        today = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_YEAR));
        if (call.getType().getId().equalsIgnoreCase(F_JCONON_CALL_PROCEDURA_COMPARATIVA_FOLDER)) {
	        Map<String, ACLType> aces = new HashMap<String, ACLType>();
	        aces.put(GROUP_EVERYONE, ACLType.Consumer);
	    	CMISUser user = userService.loadUserForConfirm(userId);    	
	        if (!publish && !(user.isAdmin() || isMemeberOfConcorsiGroup(user))) {
	        	throw new ClientMessageException("message.error.call.cannnot.modify");
	        }

	        Criteria criteria = CriteriaFactory.createCriteria("jconon_attachment:call_fp");
	        criteria.add(Restrictions.inFolder(call.getId()));
            if (criteria.executeQuery(cmisSession, false, cmisSession.getDefaultContext()).getTotalNumItems() == 0)
                throw new ClientMessageException("message.error.call.attachment.not.present");

            Map<String, Object> properties = new HashMap<String, Object>();
	        properties.put(JCONONPropertyIds.CALL_PUBBLICATO.value(), publish);        
	        if (publish) {
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
	            if ((Boolean)call.getPropertyValue(JCONONPropertyIds.CALL_PUBBLICATO.value()) && !(user.isAdmin() || isMemeberOfConcorsiGroup(user))) {
	            	throw new ClientMessageException("message.error.call.cannnot.modify");
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
