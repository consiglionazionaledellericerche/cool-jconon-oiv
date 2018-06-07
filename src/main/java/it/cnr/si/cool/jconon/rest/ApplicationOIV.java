package it.cnr.si.cool.jconon.rest;

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeMetadataService;
import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.service.application.ApplicationOIVService;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response esperienzaNonCoerente(@Context HttpServletRequest req,
                                          @FormParam(PropertyIds.OBJECT_ID) String objectId,
                                          @FormParam("callId") String callId,
                                          @FormParam("aspect") String aspect,
                                          @FormParam("jconon_attachment:esperienza_non_coerente_motivazione") String motivazione) throws IOException{
		ResponseBuilder rb;
		try {
			String userId = cmisService.getCMISUserFromSession(req).getId();
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
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response esperienzaCoerente(@Context HttpServletRequest req,
                                       @FormParam(PropertyIds.OBJECT_ID) String objectId,
                                       @FormParam("callId") String callId,
                                       @FormParam("aspect") String aspect,
                                       @FormParam("userName") String userName) throws IOException{
		ResponseBuilder rb;
		try {
			String userId = cmisService.getCMISUserFromSession(req).getId();
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
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response annotazioni(@Context HttpServletRequest req,
                                @FormParam(PropertyIds.OBJECT_ID) String objectId,
                                @FormParam("callId") String callId,
                                @FormParam("applicationId") String applicationId,
                                @FormParam("aspect") String aspect,
                                @FormParam("jconon_attachment:esperienza_annotazione_motivazione") String motivazione) throws IOException{
		ResponseBuilder rb;
		try {
			String userId = cmisService.getCMISUserFromSession(req).getId();
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

	@POST
	@Path("message")
	@Produces(MediaType.APPLICATION_JSON)
	public Response message(@Context HttpServletRequest req,
									  @FormParam("nodeRef") String nodeRef, @FormParam("nodeRefDocumento") String nodeRefDocumento) throws IOException{
        ResponseBuilder rb;
	    LOGGER.debug("Message for application:" + nodeRef);

		applicationOIVService.message(cmisService.getCurrentCMISSession(req),
				nodeRef, nodeRefDocumento);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("nodeRef", nodeRef);
        rb = Response.ok(model);
        return rb.build();
	}

    @GET
    @Path("iscrivi-inelenco")
    @Produces(MediaType.APPLICATION_JSON)
    public Response iscriviInElanco(@Context HttpServletRequest req, @QueryParam("idDomanda") String idDomanda) throws IOException{
        LOGGER.debug("Iscrizione in elenco della domanda: {}", idDomanda);
        return Response.ok(Collections.singletonMap("progressivo",
                applicationOIVService.iscriviInElenco(cmisService.getCurrentCMISSession(req), idDomanda))).build();
    }

	@POST
	@Path("soccorso-istruttorio")
	@Produces(MediaType.APPLICATION_JSON)
    public Response soccorsoIstruttorio(@Context HttpServletRequest req, @QueryParam("idDomanda") String idDomanda, @QueryParam("fileName") String fileName) throws IOException{
        ResponseBuilder rb;
        try {
            Session session = cmisService.getCurrentCMISSession(req);
            Map<String, Object> model = applicationOIVService.soccorsoIstruttorio(session, req, idDomanda, fileName, cmisService.getCMISUserFromSession(req));
            rb = Response.ok(model);
        } catch (ClientMessageException | CMISApplicationException | TemplateException e) {
            LOGGER.warn("send error", e);
            rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("message", e.getMessage()));
        }
        return rb.build();
	}

    @GET
    @Path("soccorso-istruttorio")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isFlussosoccorsoIstruttorio(@Context HttpServletRequest req, @QueryParam("idDomanda") String idDomanda) throws IOException{
        ResponseBuilder rb;
        try {
            Session session = cmisService.getCurrentCMISSession(req);
            rb = Response.ok(applicationOIVService.isStatoFlussoSoccorsoIstruttorio(session, idDomanda));
        } catch (ClientMessageException | CMISApplicationException e) {
            LOGGER.warn("send error", e);
            rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("message", e.getMessage()));
        }
        return rb.build();
    }
}