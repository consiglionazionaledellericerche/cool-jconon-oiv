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
import java.util.Optional;
import java.util.function.Supplier;
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
        private final long total_count;
        private final List<IPAAmministrazione> items;

        public ResponsePage(long total_count, List<IPAAmministrazione> items) {
            this.total_count = total_count;
            this.items = items;
        }

        public long getTotal_count() {
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
        Supplier<Stream<IPAAmministrazione>> ipaAmministrazioni = () ->
        {
            try {
                return ipaService.amministrazioni()
                .values()
                .stream()
                .filter(ipaAmministrazione -> ipaAmministrazione.getDes_amm().toUpperCase().contains(q.toUpperCase()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        final long size = ipaAmministrazioni.get().count();
        return Response.ok(
                new ResponsePage(
                        size,
                        ipaAmministrazioni
                                .get()
                                .limit(Math.min(page * 100, size))
                                .sorted((ipaAmministrazione, t1) -> ipaAmministrazione.getDes_amm().compareTo(t1.getDes_amm()))
                                .collect(Collectors.toList())
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