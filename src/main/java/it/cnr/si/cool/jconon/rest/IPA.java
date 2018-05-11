package it.cnr.si.cool.jconon.rest;

import it.cnr.cool.security.SecurityChecked;
import it.cnr.si.cool.jconon.model.IPAAmministrazione;
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
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("ipa")
@Component
@SecurityChecked(needExistingSession = false, checkrbac = false)
public class IPA {
    private static final Logger LOGGER = LoggerFactory.getLogger(IPA.class);
    @Autowired
    private IPAService ipaService;

    private class ResponsePage implements Serializable {
        private final int total_count;
        private final List<IPAAmministrazione> items;

        public ResponsePage(int total_count, List<IPAAmministrazione> items) {
            this.total_count = total_count;
            this.items = items;
        }

        public int getTotal_count() {
            return total_count;
        }

        public List<IPAAmministrazione> getItems() {
            return items;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResponsePage that = (ResponsePage) o;
            return total_count == that.total_count &&
                    Objects.equals(items, that.items);
        }

        @Override
        public int hashCode() {
            return Objects.hash(total_count, items);
        }
    }

    @GET
    @Path("amministrazioni")
    @Produces(MediaType.APPLICATION_JSON)
    public Response amministrazioni(@Context HttpServletRequest request, @QueryParam("q") String q, @QueryParam("page") Integer page) throws IOException {
        final List<IPAAmministrazione> ipaAmministrazioneList = ipaService
                .amministrazioni()
                .values()
                .stream()
                .filter(ipaAmministrazione -> ipaAmministrazione.getDes_amm().toUpperCase().contains(q.toUpperCase()))
                .sorted((ipaAmministrazione, t1) -> ipaAmministrazione.getDes_amm().compareTo(t1.getDes_amm()))
                .collect(Collectors.toList());
        final int size = ipaAmministrazioneList.size();
        return Response.ok(
                new ResponsePage(
                        size,
                        ipaAmministrazioneList.subList(0, Math.min(page * 100, size))
                )
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