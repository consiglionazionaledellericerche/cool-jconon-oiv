package it.cnr.jconon.service.application;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.si.cool.jconon.CoolJcononApplication;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by francesco on 23/11/16.
 */


@RunWith(SpringRunner.class )
@SpringBootTest(classes = CoolJcononApplication.class)
public class ApplicationOIVServiceTest {

    @Autowired
    private ApplicationOIVService applicationOIVService;

    @Autowired
    private CMISService cmisService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationOIVServiceTest.class);

    @Test
    @Ignore
    public void eseguiCalcolo() throws Exception {

        Session adminSession = cmisService.createAdminSession();

        Map<String, Object> properties = new HashedMap();
        properties.put(PropertyIds.OBJECT_ID, "???");

        Map<String, Object> aspectProperties = new HashedMap();
        Folder folder = applicationOIVService.save(adminSession, "???", Locale.ITALIAN, "foo.bar", properties, aspectProperties);


        String objectId = "???";
        applicationOIVService.eseguiCalcolo(objectId, aspectProperties);

        assertTrue(false);



    }

}