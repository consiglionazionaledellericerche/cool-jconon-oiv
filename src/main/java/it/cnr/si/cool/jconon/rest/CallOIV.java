package it.cnr.si.cool.jconon.rest;

import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeMetadataService;
import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.mock.RequestUtils;
import it.cnr.si.cool.jconon.service.call.CallOIVService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("call-fp")
@Component
@SecurityChecked(needExistingSession=true, checkrbac=false)
public class CallOIV {
	private static final Logger LOGGER = LoggerFactory.getLogger(CallOIV.class);	
	@Autowired
	private CallOIVService callService;
	@Autowired
	private CMISService cmisService;
	@Autowired
	private NodeMetadataService nodeMetadataService;
	
	@POST
	@Path("publish-esito")
	@Produces(MediaType.APPLICATION_JSON)
	public Response publishCall(@Context HttpServletRequest request, @CookieParam("__lang") String lang, MultivaluedMap<String, String> formParams) {
		ResponseBuilder rb;
		try {
			Map<String, String[]> formParamz = new HashMap<String, String[]>();
			formParamz.putAll(request.getParameterMap());
			if (formParams != null && !formParams.isEmpty())
				formParamz.putAll(RequestUtils.extractFormParams(formParams));
			Session cmisSession = cmisService.getCurrentCMISSession(request);
			String userId = cmisService.getCMISUserFromSession(request).getId();
			Folder call = callService.publishEsito(cmisSession, cmisService.getCurrentBindingSession(request),
					formParamz.get(PropertyIds.OBJECT_ID)[0], Boolean.valueOf(formParamz.get("publish")[0]), userId);
			Map<String, Object> result = new HashMap<>();
			result.put("published", Boolean.valueOf(formParamz.get("publish")[0]));
			result.put(CoolPropertyIds.ALFCMIS_NODEREF.value(), call.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString());
			rb = Response.ok(result);
		} catch (ClientMessageException e) {
			LOGGER.error("error publishing call", e);
			rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("message", e.getMessage()));
		}
		return rb.build();
	}
}