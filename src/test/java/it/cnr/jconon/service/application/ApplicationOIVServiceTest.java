package it.cnr.jconon.service.application;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.si.cool.jconon.CoolJcononApplication;
import it.cnr.si.cool.jconon.service.application.ApplicationOIVService;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
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
    
    public static void main(String[] args) {
		aggiornaProcedureComparative();
	}
    
	public static void aggiornaProcedureComparative() {
		Session session = getRepositorySession("admin","admin");
		ItemIterable<QueryResult> query = session.query("select cmis:objectId from jconon_call_procedura_comparativa:folder", false, session.getDefaultContext());
		for (QueryResult queryResult : query.getPage(Integer.MAX_VALUE)) {
			CmisObject procedura = session.getObject(queryResult.<String>getPropertyValueById(PropertyIds.OBJECT_ID));
			LOGGER.info("AGGIORNATA PROCEDURA {}", procedura.getName());
	        Map<String, Object> properties = new HashMap<String, Object>();
	        properties.put("jconon_call_procedura_comparativa:data_fine_proroga", null);
	        procedura.updateProperties(properties);				
		}		
	}    
    
    public static Session getRepositorySession(String userName, String password)
    {

        Map<String, String> sessionParameters = new HashMap<String, String>();
        sessionParameters.put(SessionParameter.ATOMPUB_URL, "http://alfresco-community.test.si.cnr.it/alfresco/api/-default-/public/cmis/versions/1.1/atom");
        sessionParameters.put("org.apache.chemistry.opencmis.binding.spi.type","atompub");
        sessionParameters.put(SessionParameter.USER, userName);
        sessionParameters.put(SessionParameter.PASSWORD, password);
        sessionParameters.put(SessionParameter.REPOSITORY_ID, "-default-");
        Session session = SessionFactoryImpl.newInstance().createSession(sessionParameters);
        return session;
    }
    

}