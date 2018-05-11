package it.cnr.jconon.service.application;

import it.cnr.si.cool.jconon.CoolJcononApplication;
import it.cnr.si.cool.jconon.model.IPAAmministrazione;
import it.cnr.si.cool.jconon.service.IPAService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoolJcononApplication.class, properties = "spring.profiles.active=fp")
public class IPATest {
    @Inject
    private IPAService ipaService;

    @Test
    public void testAmministrazioniIPA() throws IOException, URISyntaxException {
        final Map<String, IPAAmministrazione> amministrazioni = ipaService.amministrazioni();
        Assert.assertTrue(amministrazioni.values().stream().anyMatch(
                s -> s.getDes_amm().equalsIgnoreCase("Consiglio Nazionale delle Ricerche - CNR")
        ));
    }
}
