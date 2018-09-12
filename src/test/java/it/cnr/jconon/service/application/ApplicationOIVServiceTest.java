package it.cnr.jconon.service.application;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.cmis.service.impl.ObjectTypeCacheImpl;
import it.cnr.cool.util.StringUtil;
import it.cnr.si.cool.jconon.CoolJcononApplication;
import it.cnr.si.cool.jconon.service.application.ApplicationOIVService;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * Created by francesco on 23/11/16.
 */


public class ApplicationOIVServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationOIVServiceTest.class);

    private static Calendar calcolaDataProroga(String dataProroga, String oraProroga) throws ParseException {
        Calendar dataFineInvioDomandeOpt = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.ITALY);
        dataFineInvioDomandeOpt.setTime(StringUtil.CMIS_DATEFORMAT.parse(dataProroga));        
        Optional<String> oraFineInvioDomande = Optional.ofNullable(oraProroga).filter(x -> x.length() > 0);
    	Calendar dataFineInvioDomande = Calendar.getInstance(Locale.ITALY);
    	dataFineInvioDomande.set(Calendar.YEAR, dataFineInvioDomandeOpt.get(Calendar.YEAR));
    	dataFineInvioDomande.set(Calendar.MONTH, dataFineInvioDomandeOpt.get(Calendar.MONTH));
    	dataFineInvioDomande.set(Calendar.DAY_OF_MONTH, dataFineInvioDomandeOpt.get(Calendar.DAY_OF_MONTH));
    	dataFineInvioDomande.set(Calendar.SECOND, 59);
    	if (oraFineInvioDomande.isPresent()) {
        	dataFineInvioDomande.set(Calendar.HOUR_OF_DAY, Integer.valueOf(oraFineInvioDomande.get().split(":")[0]));
        	dataFineInvioDomande.set(Calendar.MINUTE, Integer.valueOf(oraFineInvioDomande.get().split(":")[1]));	        		
    	} else {
        	dataFineInvioDomande.set(Calendar.HOUR_OF_DAY, 23);
        	dataFineInvioDomande.set(Calendar.MINUTE, 59);
    	}    	
		return dataFineInvioDomande;
	}

	public static void controllaPermessi() {
		Session session = getRepositorySession("admin", "******");
        OperationContext defaultContext = session.getDefaultContext();
        defaultContext.setIncludeAcls(true);
        ItemIterable<QueryResult> query = session.query("select cmis:objectId from jconon_application:folder WHERE (IN_TREE ('a5ed6f55-f674-4925-885a-1f52307a63e0') AND ((NOT ((jconon_application:stato_domanda = 'I')) AND ((jconon_application:stato_domanda = 'C')))))", false, defaultContext);
		for (QueryResult queryResult : query.getPage(Integer.MAX_VALUE)) {
			CmisObject domanda = session.getObject(queryResult.<String>getPropertyValueById(PropertyIds.OBJECT_ID));
			domanda.getAcl().getAces().stream()
                    .filter(ace -> ace.getPrincipalId().equals("GROUP_CONCORSI"))
                    .filter(ace -> ace.getPermissions().contains("{http://www.alfresco.org/model/content/1.0}cmobject.Coordinator"))
                    .forEach(ace -> {
                        //domanda.removeAcl(Arrays.asList(ace), AclPropagation.REPOSITORYDETERMINED);
                        LOGGER.info("Permesso {} domanda {}", ace, domanda.getId());
                    });
		}
	}

	public static void aggiornaPermessi() {
        Session session = getRepositorySession("admin", "******");
        OperationContext defaultContext = session.getDefaultContext();
        defaultContext.setIncludeAcls(true);
        String[] domande = {
                "efdabc75-4bbf-436e-9a2b-f70c2e4eca93"
        };
        final Ace group_concorsi = new AccessControlEntryImpl(
                new AccessControlPrincipalDataImpl("GROUP_CONCORSI"),
                Collections.singletonList("{http://www.alfresco.org/model/content/1.0}cmobject.Contributor")
        );

        Arrays.asList(domande).stream().forEach(s -> {
            CmisObject domanda = session.getObject(s);
            domanda.addAcl(Collections.singletonList(group_concorsi), AclPropagation.REPOSITORYDETERMINED);
        });
    }

    public static void main(String[] args) {
        checkoutProtocollo();
    }

    public static void checkoutProtocollo() {
    	String protocolPath = "/Data Dictionary/Web Applications/jconon/WEB-INF/classes/protocollo.json";
		Session session = getRepositorySession("marco.spasiano","0508jada");
		Document document = (Document) session.getObjectByPath(protocolPath);
		document.refresh();
		if (true) {
			document.checkOut();
		}
		InputStream is = document.getContentStream().getStream();
		try {
			LOGGER.info(IOUtils.toString(is));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void riammettiNellElenco() {
		Session session = getRepositorySession("admin","Alf4FpPw");
		final CmisObject object = session.getObject("11c408f8-55da-4a81-b11d-d3fd76458fbe");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("jconon_application:esclusione_rinuncia", null);
		map.put("jconon_application:fl_rimosso_elenco", false);
		map.put("jconon_application:data_rimozione_elenco", null);
		object.updateProperties(map);
	}

	public static void escludiDallElenco() {
        Session session = getRepositorySession("admin","Alf4FpPw");
        final CmisObject object = session.getObject("b7356c91-1cfd-40eb-ac35-d43bfcfef416");
        Map<String, Object> map = new HashMap<String, Object>();
		map.put("jconon_application:esclusione_rinuncia", "R");
        map.put("jconon_application:fl_rimosso_elenco", true);
        map.put("jconon_application:data_rimozione_elenco", new GregorianCalendar(2017,Calendar.SEPTEMBER,6));
        object.updateProperties(map);
    }

	public static void aggiornaProcedureComparative() {
		Session session = getRepositorySession("admin","**********");
		ItemIterable<QueryResult> query = session.query("select cmis:objectId from jconon_call_procedura_comparativa:folder", false, session.getDefaultContext());
		for (QueryResult queryResult : query.getPage(Integer.MAX_VALUE)) {
			CmisObject procedura = session.getObject(queryResult.<String>getPropertyValueById(PropertyIds.OBJECT_ID));
			LOGGER.info("AGGIORNATA PROCEDURA {}", procedura.getName());
	        Map<String, Object> properties = new HashMap<String, Object>();
	        properties.put("jconon_call_procedura_comparativa:data_fine_proroga", null);
	        procedura.updateProperties(properties);				
		}		
	}    
    
	public static void aggiornaFascia() {
		Session session = getRepositorySession("admin","**********");
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(1,"1");
		map.put(2,"2");
		map.put(3,"1");
		map.put(4,"2");
		map.put(5,"2");
		map.put(6,"1");
		map.put(7,"2");
		map.put(8,"1");
		map.put(9,"1");
		map.put(10,"3");
		map.put(11,"3");
		map.put(11,"2");
		map.put(12,"2");
		map.put(13,"1");
		map.put(14,"1");
		map.put(15,"1");
		map.put(16,"1");
		map.put(17,"1");
		map.put(18,"1");
		map.put(19,"2");
		map.put(20,"2");
		map.put(21,"2");
		map.put(22,"2");
		map.put(23,"2");
		map.put(24,"1");
		map.put(25,"2");
		map.put(26,"2");
		map.put(27,"1");
		map.put(28,"1");
		map.put(29,"3");
		map.put(30,"1");
		map.put(31,"1");
		map.put(32,"1");
		map.put(33,"1");
		map.put(34,"1");
		map.put(35,"1");
		map.put(36,"2");
		map.put(37,"1");
		map.put(38,"1");
		map.put(39,"1");
		map.put(40,"1");
		map.put(41,"1");
		map.put(42,"1");
		map.put(43,"1");
		map.put(44,"1");
		map.put(45,"1");
		map.put(46,"1");
		map.put(47,"1");
		map.put(48,"1");
		map.put(49,"1");
		map.put(50,"1");
		map.put(51,"1");
		map.put(52,"1");
		map.put(53,"1");
		map.put(54,"2");
		map.put(55,"1");
		map.put(56,"1");
		map.put(57,"1");
		map.put(58,"1");
		map.put(59,"3");
		map.put(60,"1");
		map.put(61,"3");
		map.put(62,"1");
		map.put(63,"2");
		map.put(64,"1");
		map.put(65,"1");
		map.put(66,"1");
		map.put(67,"2");
		map.put(68,"2");
		map.put(69,"1");
		map.put(70,"2");
		map.put(71,"1");
		map.put(72,"1");
		map.put(73,"2");
		map.put(74,"2");
		map.put(75,"2");
		map.put(76,"2");
		map.put(77,"2");
		map.put(78,"1");
		map.put(79,"3");
		map.put(80,"1");
		map.put(81,"3");
		map.put(82,"2");
		map.put(83,"3");
		map.put(85,"3");
		map.put(86,"3");
		map.put(87,"2");
		map.put(88,"2");
		map.put(89,"2");
		map.put(90,"3");
		map.put(91,"1");
		map.put(92,"1");
		map.put(93,"1");
		map.put(94,"1");
		map.put(95,"2");
		map.put(96,"1");
		map.put(97,"1");
		map.put(98,"3");
		map.put(99,"3");
		map.put(100,"3");
		map.put(101,"3");
		map.put(102,"1");
		map.put(103,"3");
		map.put(104,"2");
		map.put(105,"1");
		map.put(106,"3");
		map.put(107,"2");
		map.put(108,"3");
		map.put(109,"1");
		map.put(110,"3");
		map.put(111,"1");
		map.put(112,"1");
		map.put(113,"1");
		map.put(114,"3");
		map.put(115,"1");
		map.put(116,"2");
		map.put(117,"1");
		map.put(118,"3");
		map.put(119,"2");
		map.put(120,"3");
		map.put(121,"1");
		map.put(122,"2");
		map.put(123,"2");
		map.put(124,"2");
		map.put(125,"1");
		map.put(126,"1");
		map.put(127,"2");
		map.put(128,"1");
		map.put(129,"1");
		map.put(130,"1");
		map.put(131,"1");
		map.put(132,"3");
		map.put(133,"3");
		map.put(134,"3");
		map.put(135,"1");
		map.put(136,"2");
		map.put(137,"2");
		map.put(138,"3");
		map.put(139,"1");
		map.put(140,"3");
		map.put(141,"1");
		map.put(142,"1");
		map.put(143,"2");
		map.put(144,"1");
		map.put(145,"1");
		map.put(146,"2");
		map.put(147,"2");
		map.put(148,"1");
		map.put(149,"1");
		map.put(150,"1");
		map.put(151,"1");
		map.put(152,"2");
		map.put(153,"3");
		map.put(154,"2");
		map.put(155,"1");
		map.put(156,"2");
		map.put(157,"1");
		map.put(158,"2");
		map.put(159,"2");
		map.put(160,"3");
		map.put(161,"3");
		map.put(162,"2");
		map.put(163,"2");
		map.put(164,"3");
		map.put(165,"2");
		map.put(166,"2");
		map.put(167,"1");
		map.put(168,"3");
		map.put(169,"1");
		map.put(170,"1");
		map.put(171,"1");
		map.put(172,"1");
		map.put(173,"1");
		map.put(174,"1");
		map.put(175,"3");
		map.put(176,"3");
		map.put(177,"1");
		map.put(178,"3");
		map.put(179,"2");
		map.put(180,"2");
		map.put(181,"2");
		map.put(182,"3");
		map.put(183,"3");
		map.put(184,"2");
		map.put(185,"2");
		map.put(186,"1");
		map.put(187,"2");
		map.put(188,"1");
		map.put(189,"1");
		map.put(190,"2");
		map.put(191,"2");
		map.put(192,"3");
		map.put(193,"1");
		map.put(194,"1");
		map.put(195,"1");
		map.put(196,"2");
		map.put(197,"1");
		map.put(198,"1");
		map.put(199,"1");
		map.put(200,"1");
		map.put(201,"1");
		map.put(202,"3");
		map.put(203,"2");
		map.put(204,"3");
		map.put(205,"1");
		map.put(206,"2");
		map.put(207,"3");
		map.put(208,"3");
		map.put(209,"3");
		map.put(210,"2");
		map.put(211,"1");
		map.put(212,"1");
		map.put(213,"1");
		map.put(214,"2");
		map.put(215,"1");
		map.put(216,"1");
		map.put(217,"1");
		map.put(218,"1");
		map.put(219,"2");
		map.put(220,"1");
		map.put(221,"2");
		map.put(222,"3");
		map.put(223,"1");
		map.put(224,"3");
		map.put(225,"3");
		map.put(226,"1");
		map.put(227,"1");
		map.put(228,"1");
		map.put(229,"1");
		map.put(230,"1");
		map.put(231,"2");
		map.put(232,"3");
		map.put(233,"1");
		map.put(234,"1");
		map.put(235,"3");
		map.put(236,"1");
		map.put(237,"3");
		map.put(238,"1");
		map.put(239,"1");
		map.put(240,"1");
		map.put(241,"1");
		map.put(242,"1");
		map.put(243,"1");
		map.put(244,"2");
		map.put(245,"1");
		map.put(246,"1");
		map.put(247,"1");
		map.put(248,"1");
		map.put(249,"1");
		map.put(251,"2");
		map.put(252,"1");
		map.put(253,"1");
		map.put(254,"2");
		map.put(255,"1");
		for (Integer numeroIscrizione : map.keySet()) {
			ItemIterable<QueryResult> query = session.query("select cmis:objectId from jconon_application:folder where jconon_application:progressivo_iscrizione_elenco = "+ numeroIscrizione, false, session.getDefaultContext());
			for (QueryResult queryResult : query.getPage(Integer.MAX_VALUE)) {
				CmisObject domanda = session.getObject(queryResult.<String>getPropertyValueById(PropertyIds.OBJECT_ID));
				if (!domanda.getPropertyValue("jconon_application:fascia_professionale_attribuita").equals(map.get(numeroIscrizione))) {
					LOGGER.info("{} --> Fascia attribuita:{} Fascia validata:{}", domanda.getName(), domanda.getPropertyValue("jconon_application:fascia_professionale_attribuita"), map.get(numeroIscrizione));
				}

				Map<String, Serializable> properties = new HashMap<String, Serializable>();
				properties.put("jconon_application:fascia_professionale_validata", map.get(numeroIscrizione));
				domanda.updateProperties(properties);
			}		
		}
		
	}

	public static void aggiornaEseguiControlloFascia() {
		Session session = getRepositorySession("admin","Alf4FpPw");
		ItemIterable<QueryResult> query = session.query("select application.cmis:objectId from jconon_application:folder application " +
				"join jconon_application:aspect_fascia_professionale_attribuita AS fascia " +
				"on fascia.cmis:objectId = application.cmis:objectId " +
				"where application.jconon_application:esegui_controllo_fascia = false " +
				"", false);
		for (QueryResult queryResult : query.getPage(Integer.MAX_VALUE)) {
			CmisObject domanda = session.getObject(queryResult.<String>getPropertyValueById(PropertyIds.OBJECT_ID));
            LOGGER.info(domanda.getName());
            Map<String, Serializable> properties = new HashMap<String, Serializable>();
            properties.put("jconon_application:esegui_controllo_fascia", Boolean.TRUE);
            //domanda.updateProperties(properties);
		}
	}

    public static Session getRepositorySession(String userName, String password)
    {

        Map<String, String> sessionParameters = new HashMap<String, String>();
        sessionParameters.put(SessionParameter.BROWSER_URL, "http://alfresco.ecaasdfp.cloudspc.it/alfresco/api/-default-/public/cmis/versions/1.1/browser");
        sessionParameters.put("org.apache.chemistry.opencmis.binding.spi.type","browser");
        sessionParameters.put(SessionParameter.USER, userName);
        sessionParameters.put(SessionParameter.PASSWORD, password);
        sessionParameters.put(SessionParameter.REPOSITORY_ID, "-default-");
        sessionParameters.put("org.apache.chemistry.opencmis.cache.types.classname", ObjectTypeCacheImpl.class.getName());
        sessionParameters.put("org.apache.chemistry.opencmis.locale.iso3166", Locale.ITALY.getCountry());
        sessionParameters.put("org.apache.chemistry.opencmis.locale.iso639", Locale.ITALY.getLanguage());
        sessionParameters.put("org.apache.chemistry.opencmis.locale.variant", Locale.ITALY.getVariant());
        Session session = SessionFactoryImpl.newInstance().createSession(sessionParameters);
        return session;
    }

    public static void aggiornaFasciaAttribuita() {
		List<String> codici = new ArrayList<String>();
		codici.add("CDLVCN68C26D708F");
		codici.add("CGLNLP65B14E716K");
		codici.add("DLLDNL62E47B180J");
		codici.add("FLNLGU71T44E290Y");
		codici.add("LFLMGS47S59A509Q");
		codici.add("NRDMNT63R41F839R");
		codici.add("RTLMRA58S19F839Y");
		codici.add("PNRSVT53H23B872P");
		codici.add("RGRMRA68E20A783F");
		codici.add("RBRGNN63C26A571M");
		codici.add("LNGVGN83B42G273H");
		codici.add("DCRWTR73A01H703S");
		codici.add("ZZILRC71T20F839P");
		codici.add("MNTGRD59M09F913B");
		codici.add("QRNLRA87E49B157P");
		codici.add("RNCVCN64M10H703F");
		codici.add("RGHBBR70A45H501A");
		codici.add("MLLFBA64D01H501G");
		codici.add("SLRLCN74C21F839G");
		codici.add("TRCSDR58S06C129V");
		codici.add("SRRMHL74P18L418F");
		codici.add("CSCPRI77D09D505K");


		List<String> collect = codici.stream().map(x -> "'" + x + "'").collect(Collectors.toList());
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put("jconon_application:fascia_professionale_attribuita","0");
		properties.put("jconon_application:fascia_professionale_esegui_calcolo", "false");

		Session session = getRepositorySession("admin","*****");
		ItemIterable<QueryResult> query = session.query("select cmis:objectId from jconon_application:folder where jconon_application:codice_fiscale IN ("
				+ String.join(",",collect) + ")", false, session.getDefaultContext());
		for (QueryResult queryResult : query.getPage(Integer.MAX_VALUE)) {
			CmisObject domanda = session.getObject(queryResult.<String>getPropertyValueById(PropertyIds.OBJECT_ID));
			String fasciaAttribuita = domanda.getPropertyValue("jconon_application:fascia_professionale_attribuita");
			if (!fasciaAttribuita.equals("0")) {
				LOGGER.info("{} --> Fascia attribuita:{}", domanda.getName(), fasciaAttribuita);
				//domanda.updateProperties(properties);
			}
		}
	}

}