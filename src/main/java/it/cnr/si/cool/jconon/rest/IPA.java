package it.cnr.si.cool.jconon.rest;

import it.cnr.cool.security.SecurityChecked;
import it.cnr.si.cool.jconon.service.IPAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.stream.Collectors;

@Path("ipa")
@Component
@SecurityChecked(needExistingSession = false, checkrbac = false)
public class IPA {
    private static final Logger LOGGER = LoggerFactory.getLogger(IPA.class);
    @Autowired
    private IPAService ipaService;

    @GET
    @Path("amministrazioni")
    @Produces(MediaType.APPLICATION_JSON)
    public Response amministrazioni(@Context HttpServletRequest request, @QueryParam("q") String q) throws IOException {
        return Response.ok(
                ipaService
                        .amministrazioni()
                        .values()
                        .stream()
                        .filter(ipaAmministrazione -> ipaAmministrazione.getDes_amm().toUpperCase().contains(q.toUpperCase()))
                        .collect(Collectors.toList())
        ).build();
    }

    @GET
    @Path("amministrazione")
    @Produces(MediaType.APPLICATION_JSON)
    public Response amministrazione(@Context HttpServletRequest request, @QueryParam("q") String q) throws IOException {
        return Response.ok(
                ipaService
                        .amministrazioni()
                        .values()
                        .stream()
                        .filter(ipaAmministrazione -> ipaAmministrazione.getDes_amm().equalsIgnoreCase(q))
                        .findFirst()
                        .orElse(null)
        ).build();
    }
}