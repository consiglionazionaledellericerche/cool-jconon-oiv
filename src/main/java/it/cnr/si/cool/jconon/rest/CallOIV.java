/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.si.cool.jconon.rest;

import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.NodeMetadataService;
import it.cnr.cool.security.SecurityChecked;
import it.cnr.cool.web.scripts.exception.CMISApplicationException;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.mock.RequestUtils;
import it.cnr.si.cool.jconon.service.call.CallOIVService;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

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
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Path("call-fp")
@Component
@SecurityChecked(needExistingSession = true, checkrbac = false)
public class CallOIV {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallOIV.class);
    @Autowired
    private CallOIVService callService;
    @Autowired
    private CMISService cmisService;
    @Autowired
    private NodeMetadataService nodeMetadataService;
    @Autowired
    private CommonsMultipartResolver resolver;

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

    @POST
    @Path("carica-proroga")
    @Produces(MediaType.APPLICATION_JSON)
    public Response caricaProroga(@Context HttpServletRequest req) throws IOException {
        ResponseBuilder rb;
        try {
            Session session = cmisService.getCurrentCMISSession(req);
            MultipartHttpServletRequest mRequest = resolver.resolveMultipart(req);
            String idCall = mRequest.getParameter("objectId");
            String dataProroga = mRequest.getParameter("jconon_attachment:procedura_comparativa_data_fine_proroga");
            String oraProroga = mRequest.getParameter("jconon_attachment:procedura_comparativa_ora_fine_proroga");
            String title = Optional.of(mRequest.getParameter("cm:title")).filter(x -> x.length() > 0).orElse(null);
            String description = Optional.of(mRequest.getParameter("cm:description")).filter(x -> x.length() > 0).orElse(null);
            LOGGER.debug("carica proroga su procedura comparativa : {}", idCall);
            MultipartFile file = mRequest.getFile("prorogatermini");
            Optional.ofNullable(file).orElseThrow(() -> new ClientMessageException("Allegare il file della proroga dei termini!"));
            Map<String, Object> model = callService.caricaProroga(session, cmisService.getCMISUserFromSession(req), idCall, dataProroga, oraProroga, title, description, file);
            rb = Response.ok(model);
        } catch (ClientMessageException | CMISApplicationException | ParseException e) {
            LOGGER.warn("send error", e);
            rb = Response.status(Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("message", e.getMessage()));
        }
        return rb.build();
    }

}