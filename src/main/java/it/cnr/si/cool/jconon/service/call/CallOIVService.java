package it.cnr.si.cool.jconon.service.call;

import it.cnr.cool.cmis.model.ACLType;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.ACLService;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.FolderService;
import it.cnr.cool.exception.CoolException;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.I18nService;
import it.cnr.cool.util.MimeTypes;
import it.cnr.cool.util.StringUtil;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.cmis.model.JCONONFolderType;
import it.cnr.si.cool.jconon.cmis.model.JCONONPolicyType;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.repository.CacheRepository;
import it.cnr.si.cool.jconon.service.cache.CompetitionFolderService;
import it.spasia.opencmis.criteria.Criteria;
import it.spasia.opencmis.criteria.CriteriaFactory;
import it.spasia.opencmis.criteria.restrictions.Restrictions;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Primary
public class CallOIVService extends CallService {
    private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_ORA_FINE_PROROGA = "jconon_call_procedura_comparativa:ora_fine_proroga";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_DATA_FINE_PROROGA = "jconon_call_procedura_comparativa:data_fine_proroga";
	private static final String JCONON_ATTACHMENT_PROCEDURA_COMPARATIVA_ORA_FINE_PROROGA = "jconon_attachment:procedura_comparativa_ora_fine_proroga";
	private static final String JCONON_ATTACHMENT_PROCEDURA_COMPARATIVA_DATA_FINE_PROROGA = "jconon_attachment:procedura_comparativa_data_fine_proroga";
	private static final String D_JCONON_ATTACHMENT_CALL_FP_ESITO_PROVVEDIMENTO_NOMINA = "D:jconon_attachment:call_fp_esito_provvedimento_nomina";
	private static final String D_JCONON_ATTACHMENT_CALL_FP_ESITO_ELENCO_CODICI_ISCRIZIONE = "D:jconon_attachment:call_fp_esito_elenco_codici_iscrizione";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_TIPOLOGIA_SELEZIONE = "jconon_call_procedura_comparativa:tipologia_selezione";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_ORA_FINE_INVIO_DOMANDE = "jconon_call_procedura_comparativa:ora_fine_invio_domande";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_NUMERO_DIPENDENTI = "jconon_call_procedura_comparativa:numero_dipendenti";
	private static final String FASCIA_3 = "Fascia 3";
	private static final String MAGGIORE_O_UGUALE_A_250 = "Maggiore o uguale a 250";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE_COMP3 = "jconon_call_procedura_comparativa:fascia_professionale_comp3";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE_COMP2 = "jconon_call_procedura_comparativa:fascia_professionale_comp2";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE = "jconon_call_procedura_comparativa:fascia_professionale";
	private static final String COMPONENTE_I = "Componente/i";
	private static final String INTERO_COLLEGIO = "Intero Collegio";
	private static final String PRESIDENTE = "Presidente";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_DATA_PUBBLICAZIONE_ESITO = "jconon_call_procedura_comparativa:data_pubblicazione_esito";
	private static final String JCONON_CALL_PROCEDURA_COMPARATIVA_PUBBLICATO_ESITO = "jconon_call_procedura_comparativa:pubblicato_esito";
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
	@Autowired
    private CMISService cmisService;
	@Value("${user.admin.username}")
	private String adminUserName;

	public void publish(BindingSession cmisSession,
			final String nodeRef, final String userId, final boolean publish, final boolean esito) {
		String link = cmisService.getBaseURL().concat(
				"service/cnr/jconon/procedura-comparativa/publish");
		UrlBuilder url = new UrlBuilder(link);
		Response resp = cmisService.getHttpInvoker(cmisSession).invokePOST(url,
				MimeTypes.JSON.mimetype(), new Output() {
					@Override
					public void write(OutputStream out) throws Exception {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("nodeRef", nodeRef);
						jsonObject.put("userid", userId);
						jsonObject.put("publish", publish);
						jsonObject.put("esito", esito);
						out.write(jsonObject.toString().getBytes());
					}
				}, cmisSession);
		int status = resp.getResponseCode();
		if (status == HttpStatus.SC_NOT_FOUND
				|| status == HttpStatus.SC_BAD_REQUEST
				|| status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			throw new CoolException("Publish failed for procedura-comparativa: "
					+ resp.getErrorContent());
	}

	public Folder publishEsito(Session cmisSession,
			BindingSession currentBindingSession, String objectId, boolean publish, String userId) {
        final Folder call = (Folder) cmisSession.getObject(objectId);
        Calendar today = Calendar.getInstance();
        CMISUser user = userService.loadUserForConfirm(userId);    	
        if (!publish && !(user.isAdmin() || isMemeberOfConcorsiGroup(user))) {
        	throw new ClientMessageException("message.error.call.cannnot.modify");
        }
        List<CmisObject> children = new ArrayList<CmisObject>();
        call.getChildren().forEach(cmisObject -> {
        	children.add(cmisObject);
        });
        children.stream().filter(cmisObject -> 
        	cmisObject.getType().getId().equals(D_JCONON_ATTACHMENT_CALL_FP_ESITO_ELENCO_CODICI_ISCRIZIONE))
        	.findAny()
        	.orElseThrow(() -> new ClientMessageException("message.error.call.publish.esito.attachment.elenco"));
        children.stream().filter(cmisObject -> 
	    	cmisObject.getType().getId().equals(D_JCONON_ATTACHMENT_CALL_FP_ESITO_PROVVEDIMENTO_NOMINA))
	    	.findAny()
	    	.orElseThrow(() -> new ClientMessageException("message.error.call.publish.esito.attachment.nomina"));
        
        publish(cmisService.getAdminSession(), 
        		call.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString(), 
        		publish ? adminUserName : call.getPropertyValue(PropertyIds.CREATED_BY), 
        		publish,
        		true);
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
        today.set(Calendar.HOUR_OF_DAY, 1);
        if (call.getType().getId().equalsIgnoreCase(F_JCONON_CALL_PROCEDURA_COMPARATIVA_FOLDER)) {

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
	        dataFineInvioDomande(properties);
	        if (publish) {
		        Criteria criteriaProcedureComparative = CriteriaFactory.createCriteria(JCONON_CALL_PROCEDURA_COMPARATIVA_FOLDER);
		        criteriaProcedureComparative.add(Restrictions.eq(JCONON_CALL_PROCEDURA_COMPARATIVA_AMMINISTRAZIONE, call.getPropertyValue(JCONON_CALL_PROCEDURA_COMPARATIVA_AMMINISTRAZIONE)));
		        criteriaProcedureComparative.add(Restrictions.ne(PropertyIds.OBJECT_ID, call.getId()));
		        criteriaProcedureComparative.add(Restrictions.eq(JCONONPropertyIds.CALL_PUBBLICATO.value(), true));
		        criteriaProcedureComparative.add(Restrictions.ge(JCONONPropertyIds.CALL_DATA_FINE_INVIO_DOMANDE.value(), Calendar.getInstance().getTime()));
	            if (criteriaProcedureComparative.executeQuery(cmisSession, false, cmisSession.getDefaultContext()).getTotalNumItems() > 0)
	                throw new ClientMessageException("message.error.publish.call.alredy.active");
	        	properties.put(JCONONPropertyIds.CALL_DATA_INIZIO_INVIO_DOMANDE.value(), today);	        	
	        } else {
		        properties.put(JCONONPropertyIds.CALL_DATA_INIZIO_INVIO_DOMANDE.value(), null);        
	        }
	        call.updateProperties(properties, true);
	        publish(cmisService.getAdminSession(), 
	        		call.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString(), 
	        		publish ? adminUserName : call.getPropertyValue(PropertyIds.CREATED_BY), 
	        		publish,
	        		false);
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
    
    private void dataFineInvioDomande(Map<String, Object> properties) {
        Optional<Calendar> dataFineInvioDomandeOpt = Optional.ofNullable((Calendar)properties.get(JCONONPropertyIds.CALL_DATA_FINE_INVIO_DOMANDE.value()));
        Optional<String> oraFineInvioDomande = Optional.ofNullable((String)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_ORA_FINE_INVIO_DOMANDE));
        if (dataFineInvioDomandeOpt.isPresent()) {
        	Calendar dataFineInvioDomande = Calendar.getInstance(TimeZone.getDefault());
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
	        dataFineInvioDomande(properties);
	        Optional<String> numeroDipendentiOptional = Optional.ofNullable((String)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_NUMERO_DIPENDENTI));
	        Optional<List<String>> fasciaProfessionaleOptional = Optional.ofNullable((List<String>)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE));
	        if (numeroDipendentiOptional.isPresent() && fasciaProfessionaleOptional.isPresent()  && 
	        	numeroDipendentiOptional.filter(x -> x.equals(MAGGIORE_O_UGUALE_A_250)).isPresent() &&
	        			fasciaProfessionaleOptional.get().stream().anyMatch(x -> !x.equals(FASCIA_3))){
	        		throw new ClientMessageException("message.error.fascia.presidente");
	        }
	        Optional<String> tipologiaSelezioneOptional = Optional.ofNullable((String)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_TIPOLOGIA_SELEZIONE));
	        if (tipologiaSelezioneOptional.isPresent() && 
	        		Arrays.asList(PRESIDENTE, INTERO_COLLEGIO).stream().anyMatch(tipoSelezione -> tipoSelezione.equals(tipologiaSelezioneOptional.get())) &&
	        		!Optional.ofNullable((List<String>)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE)).filter(x -> !x.isEmpty()).isPresent()) {
	        	throw new ClientMessageException("message.error.fascia.presidente.empty");
	        }	        
	        if (tipologiaSelezioneOptional.isPresent() && 
	        		Arrays.asList(INTERO_COLLEGIO).stream().anyMatch(tipoSelezione -> tipoSelezione.equals(tipologiaSelezioneOptional.get())) && (
	        				!Optional.ofNullable((List<String>)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE)).filter(x -> !x.isEmpty()).isPresent() ||
	        				!Optional.ofNullable((List<String>)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE_COMP2)).filter(x -> !x.isEmpty()).isPresent() ||
	        				!Optional.ofNullable((List<String>)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE_COMP3)).filter(x -> !x.isEmpty()).isPresent())
	        		) {
	        	throw new ClientMessageException("message.error.fascia.empty");
	        }	        
	        if (tipologiaSelezioneOptional.isPresent() && 
	        		Arrays.asList(COMPONENTE_I).stream().anyMatch(tipoSelezione -> tipoSelezione.equals(tipologiaSelezioneOptional.get())) && (
	        				!Optional.ofNullable((List<String>)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE_COMP2)).filter(x -> !x.isEmpty()).isPresent() &&
	        				!Optional.ofNullable((List<String>)properties.get(JCONON_CALL_PROCEDURA_COMPARATIVA_FASCIA_PROFESSIONALE_COMP3)).filter(x -> !x.isEmpty()).isPresent())
	        		) {
	        	throw new ClientMessageException("message.error.fascia.componente.empty");
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
                properties.put(JCONONPropertyIds.CALL_DATA_INIZIO_INVIO_DOMANDE.value(), null);
        		properties.put(JCONON_CALL_PROCEDURA_COMPARATIVA_DATA_FINE_PROROGA, null);
                
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

	public Map<String, Object> caricaProroga(Session session, CMISUser cmisUserFromSession, String idCall, String dataProroga,
			String oraProroga, String title, String description, MultipartFile file) throws IOException, ParseException {
		Folder call = (Folder) session.getObject(idCall);
		Calendar calcolaDataProroga = calcolaDataProroga(dataProroga, oraProroga);
		//controllo sulle date
		if (calcolaDataProroga.before(Optional.ofNullable(call.getPropertyValue(JCONON_CALL_PROCEDURA_COMPARATIVA_DATA_FINE_PROROGA))
				.orElse(call.getPropertyValue(JCONONPropertyIds.CALL_DATA_FINE_INVIO_DOMANDE.value())))) {
			throw new ClientMessageException("La data di proroga deve essere successiva alla data presente sulla procedura comparativa!");
		}
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(JCONONPolicyType.TITLED_ASPECT.value()));
		properties.put(PropertyIds.OBJECT_TYPE_ID, "D:jconon_attachment:call_fp_procedura_comparativa_proroga");		
		properties.put(PropertyIds.NAME, file.getOriginalFilename());
		properties.put(JCONON_ATTACHMENT_PROCEDURA_COMPARATIVA_DATA_FINE_PROROGA, calcolaDataProroga);
		properties.put(JCONON_ATTACHMENT_PROCEDURA_COMPARATIVA_ORA_FINE_PROROGA, oraProroga);
		properties.put("cm:title", title);
		properties.put("cm:description", description);		
		ContentStream contentStream = new ContentStreamImpl(file.getOriginalFilename(), BigInteger.valueOf(file.getSize()), 
				file.getContentType(), file.getInputStream());
		Document proroga = call.createDocument(properties, contentStream, VersioningState.MAJOR);
		aclService.setInheritedPermission(cmisService.getAdminSession(), proroga.<String>getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), false);
		aclService.changeOwnership(cmisService.getAdminSession(), proroga.<String>getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), 
				adminUserName, false, Collections.emptyList());
        Map<String, ACLType> aces = new HashMap<String, ACLType>();
        aces.put(GROUP_EVERYONE, ACLType.Consumer);
        aclService.addAcl(cmisService.getAdminSession(), proroga.<String>getPropertyValue(CoolPropertyIds.ALFCMIS_NODEREF.value()), aces);	            
				
		Map<String, Object> propertiesCall = new HashMap<String, Object>();
		propertiesCall.put(JCONON_CALL_PROCEDURA_COMPARATIVA_DATA_FINE_PROROGA, calcolaDataProroga);
		propertiesCall.put(JCONON_CALL_PROCEDURA_COMPARATIVA_ORA_FINE_PROROGA, oraProroga);
		call.updateProperties(propertiesCall, true);
		
		return null;
	}

	private Calendar calcolaDataProroga(String dataProroga, String oraProroga) throws ParseException {
        Calendar dataFineInvioDomandeOpt = Calendar.getInstance(TimeZone.getDefault());
        dataFineInvioDomandeOpt.setTime(StringUtil.CMIS_DATEFORMAT.parse(dataProroga));        
        Optional<String> oraFineInvioDomande = Optional.ofNullable(oraProroga).filter(x -> x.length() > 0);
    	Calendar dataFineInvioDomande = Calendar.getInstance(TimeZone.getDefault());
    	dataFineInvioDomande.set(Calendar.YEAR, dataFineInvioDomandeOpt.get(Calendar.YEAR));
    	dataFineInvioDomande.set(Calendar.MONTH, dataFineInvioDomandeOpt.get(Calendar.MONTH));
    	dataFineInvioDomande.set(Calendar.DAY_OF_MONTH, dataFineInvioDomandeOpt.get(Calendar.DAY_OF_MONTH));
    	dataFineInvioDomande.set(Calendar.SECOND, 59);
    	if (oraFineInvioDomande.isPresent()) {
        	dataFineInvioDomande.set(Calendar.HOUR_OF_DAY, Integer.valueOf(oraFineInvioDomande.get().split(":")[0]));
        	dataFineInvioDomande.set(Calendar.MINUTE, Integer.valueOf(oraFineInvioDomande.get().split(":")[1]));	        		
    	} else {
        	dataFineInvioDomande.set(Calendar.HOUR_OF_DAY, 23);
        	dataFineInvioDomande.set(Calendar.MINUTE, 59);
    	}    	
		return dataFineInvioDomande;
	}
	
}
