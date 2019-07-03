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

package it.cnr.jconon.service.application;

import freemarker.template.TemplateException;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.si.cool.jconon.CoolJcononApplication;
import it.cnr.si.cool.jconon.service.application.ApplicationOIVService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;

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
