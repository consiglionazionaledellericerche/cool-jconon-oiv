package it.cnr.jconon.service.application;

import freemarker.template.TemplateException;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.si.cool.jconon.CoolJcononApplication;
import it.cnr.si.cool.jconon.model.IPAAmministrazione;
import it.cnr.si.cool.jconon.service.IPAService;
import it.cnr.si.cool.jconon.service.application.ApplicationOIVService;
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
public class MailTest {
    @Inject
    private ApplicationOIVService applicationOIVService;

    @Test
    public void testSendComunicazione() throws IOException, URISyntaxException {
        final CMISUser cmisUser = new CMISUser();
        cmisUser.setEmail("marco.spasiano@cnr.it");
        cmisUser.setFirstName("Marco");
        cmisUser.setLastName("Spasiano");
        try {
            applicationOIVService.notificaMail(cmisUser);
        } catch (TemplateException e) {
        }
    }
}
