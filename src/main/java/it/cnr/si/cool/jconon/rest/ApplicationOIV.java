package it.cnr.si.cool.jconon.rest;

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeMetadataService;
import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.service.application.ApplicationOIVService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("application-fp")
@Component
@SecurityChecked(needExistingSession=true, checkrbac=false)
public class ApplicationOIV {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationOIV.class);	
	@Autowired
	private ApplicationOIVService applicationOIVService;
	@Autowired
	private CMISService cmisService;
	@Autowired
	private NodeMetadataService nodeMetadataService;

	@POST
	@Path("send-application")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendApplication(@Context HttpServletRequest req) throws IOException{
		ResponseBuilder rb;
		try {
			Session session = cmisService.getCurrentCMISSession(req);		
			Map<String, Object> model = applicationOIVService.sendApplicationOIV(session, req, cmisService.getCMISUserFromSession(req));
			rb = Response.ok(model);			
		} catch (ClientMessageException | CMISApplicationException | TemplateException e) {
			LOGGER.warn("send error", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("message", e.getMessage()));
		}
		return rb.build();
	}
	
	@GET
	@Path("check-application")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkApplication(@Context HttpServletRequest req) throws IOException{
		ResponseBuilder rb;
		try {
			Session session = cmisService.getCurrentCMISSession(req);		
			String userId = cmisService.getCMISUserFromSession(req).getId();			
			List<String> model = applicationOIVService.checkApplicationOIV(session, userId, cmisService.getCMISUserFromSession(req));
			rb = Response.ok(model);			
		} catch (ClientMessageException | CMISApplicationException  e) {
			LOGGER.warn("check error", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("message", e.getMessage()));
		}
		return rb.build();
	}

	@GET
	@Path("applications-elenco.xls")
	@Produces(MediaType.APPLICATION_JSON)
	public Response extractionApplicationForSingleCall(@Context HttpServletRequest req, @QueryParam("q") String query, @QueryParam("callId") String callId) throws IOException{
		LOGGER.debug("Extraction application from query:" + query);
		ResponseBuilder rb;
        Session session = cmisService.getCurrentCMISSession(req);
		try {
			Map<String, Object> model = applicationOIVService.extractionApplicationForElenco(session, query, cmisService.getCMISUserFromSession(req).getId(), callId);
			model.put("fileName", "elenco-oiv");
			rb = Response.ok(model);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}
		return rb.build();
	}	

	@POST
	@Path("esperienza-noncoerente")
	@Produces(MediaType.APPLICATION_JSON)
	public Response esperienzaNonCoerente(@Context HttpServletRequest req, MultivaluedMap<String, String> formParams) throws IOException{
		ResponseBuilder rb;
		try {
			String userId = cmisService.getCMISUserFromSession(req).getId();
			String objectId = formParams.getFirst(PropertyIds.OBJECT_ID),
				callId = formParams.getFirst("callId"),
				aspect = formParams.getFirst("aspect"),
				motivazione = formParams.getFirst("jconon_attachment:esperienza_non_coerente_motivazione");			
			applicationOIVService.esperienzaNonCoerente(userId, objectId, callId, aspect, motivazione);
			rb = Response.ok();			
		} catch (ClientMessageException | CMISApplicationException e) {
			LOGGER.error("esperienzaNonCoerente error", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("message", e.getMessage()));
		}
		return rb.build();
	}

	@POST
	@Path("esperienza-coerente")
	@Produces(MediaType.APPLICATION_JSON)
	public Response esperienzaCoerente(@Context HttpServletRequest req, MultivaluedMap<String, String> formParams) throws IOException{
		ResponseBuilder rb;
		try {
			String userId = cmisService.getCMISUserFromSession(req).getId();
			String objectId = formParams.getFirst(PropertyIds.OBJECT_ID),
				callId = formParams.getFirst("callId"),
				aspect = formParams.getFirst("aspect"),
				userName = formParams.getFirst("userName");			
			applicationOIVService.esperienzaCoerente(userId, objectId, callId, aspect, userName);
			rb = Response.ok();			
		} catch (ClientMessageException | CMISApplicationException e) {
			LOGGER.error("esperienzaNonCoerente error", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("message", e.getMessage()));
		}
		return rb.build();
	}

	@POST
	@Path("esperienza-annotazioni")
	@Produces(MediaType.APPLICATION_JSON)
	public Response annotazioni(@Context HttpServletRequest req, MultivaluedMap<String, String> formParams) throws IOException{
		ResponseBuilder rb;
		try {
			String userId = cmisService.getCMISUserFromSession(req).getId();
			String objectId = formParams.getFirst(PropertyIds.OBJECT_ID),
				callId = formParams.getFirst("callId"),
				applicationId = formParams.getFirst("applicationId"),
				aspect = formParams.getFirst("aspect"),
				motivazione = Optional.ofNullable(formParams.getFirst("jconon_attachment:esperienza_annotazione_motivazione")).filter(x -> x.length() > 0).orElse(null);
			applicationOIVService.esperienzaAnnotazione(userId, objectId, callId, applicationId, aspect, motivazione);
			rb = Response.ok();			
		} catch (ClientMessageException | CMISApplicationException e) {
			LOGGER.error("esperienzaNonCoerente error", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("message", e.getMessage()));
		}
		return rb.build();
	}
	
	@GET
	@Path("applications-ricalcola-fascia")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ricalcolaFascia(@Context HttpServletRequest req, @QueryParam("applicationId") String applicationId) throws IOException{
		ResponseBuilder rb;
        Session session = cmisService.getCurrentCMISSession(req);
		try {
			Map<String, Object> model = applicationOIVService.ricalcolaFascia(session, applicationId);
			rb = Response.ok(model);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR);
		}
		return rb.build();
	}	
	
}