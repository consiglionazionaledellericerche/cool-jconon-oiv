package it.cnr.si.cool.jconon.rest;

import freemarker.template.TemplateException;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.service.application.ApplicationOIVService;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.chemistry.opencmis.client.api.Session;
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
}