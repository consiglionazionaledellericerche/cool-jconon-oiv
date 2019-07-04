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

package it.cnr.si.cool.jconon.configuration;

import it.cnr.cool.cmis.model.ACLType;
import it.cnr.cool.cmis.model.CoolPropertyIds;
import it.cnr.cool.cmis.service.ACLService;
import it.cnr.cool.cmis.service.CMISService;
import it.cnr.si.cool.jconon.cmis.model.JCONONDocumentType;
import it.cnr.si.cool.jconon.cmis.model.JCONONFolderType;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.service.call.CallOIVService;
import it.cnr.si.cool.jconon.service.call.CallService;
import it.cnr.si.opencmis.criteria.Criteria;
import it.cnr.si.opencmis.criteria.CriteriaFactory;
import it.cnr.si.opencmis.criteria.restrictions.Restrictions;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@DependsOn(value = {"RRDService", "competitionFolderService"})
public class OIVConfiguration {
    public static final String
            OIV = "OIV",
            F_JCONON_CALL_OIV_FOLDER = "F:jconon_call_oiv:folder";
    @Autowired
    private CMISService cmisService;

    @Autowired
    private CallOIVService callOIVService;

    @Autowired
    private ACLService aclService;

    @Value("${user.admin.username}")
    private String userId;

    @PostConstruct
    public void createCallOIV() throws Exception {
        Session session = cmisService.createAdminSession();
        Criteria criteria = CriteriaFactory.createCriteria(JCONONFolderType.JCONON_CALL.queryName());
        criteria.add(Restrictions.eq(JCONONPropertyIds.CALL_CODICE.value(), OIV));
        ItemIterable<QueryResult> results = criteria.executeQuery(session, false, session.getDefaultContext());
        if (results.getTotalNumItems() == 0) {
            Map<String, Object> properties = Stream.of(
                    new AbstractMap.SimpleEntry<>(PropertyIds.NAME, "BANDO OIV"),
                    new AbstractMap.SimpleEntry<>(PropertyIds.OBJECT_TYPE_ID, F_JCONON_CALL_OIV_FOLDER),
                    new AbstractMap.SimpleEntry<>(CallOIVService.OIV_CALL_DATA_INIZIO_INVIO_DOMANDE, new GregorianCalendar(2000, 1, 1)),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_ELENCO_ASSOCIATIONS.value(),
                            Arrays.asList(
                                    JCONONDocumentType.JCONON_ATTACHMENT_DOCUMENTO_RICONOSCIMENTO.value(),
                                    JCONONDocumentType.JCONON_ATTACHMENT_CURRICULUM_VITAE.value()
                            )
                    ),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_ELENCO_SEZIONI_DOMANDA.value(),
                            Arrays.asList(
                                    "affix_tabAnagrafica", "affix_tabResidenza", "affix_tabDatiCNR",
                                    "affix_tabDichiarazioni", "affix_tabSchedaAnonima", "affix_tabUlterioriDati",
                                    "affix_tabTitoli", "affix_tabReperibilita", "affix_tabCurriculum"
                            )
                    ),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_ELENCO_SEZIONE_SCHEDE_ANONIME.value(),
                            Arrays.asList(
                                    "D:jconon_scheda_anonima:esperienza_professionale",
                                    "D:jconon_scheda_anonima:precedente_incarico_oiv"
                            )
                    ),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_BLOCCO_INVIO_DOMANDE.value(), Boolean.FALSE),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_ELENCO_FIELD_NOT_REQUIRED.value(),
                            Arrays.asList(
                                    JCONONPropertyIds.APPLICATION_EMAIL_PEC_COMUNICAZIONI.value()
                            )
                    ),
                    new AbstractMap.SimpleEntry<>(CallOIVService.OIV_CALL_DATA_FINE_INVIO_DOMANDE, new GregorianCalendar(2030, 1, 1)),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_ELENCO_SEZIONE_CURRICULUM.value(),
                            Arrays.asList(
                                    "D:jconon_attachment:specializzazioni_post_lauream",
                                    "D:jconon_attachment:altre_specializzazioni"
                            )
                    ),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_DESCRIZIONE.value(), "<p>richiesta di iscrizione ai sensi dell&rsquo;" +
                            "art. 3 del Decreto Ministeriale del 2 dicembre 2016 nell&rsquo;" +
                            "Elenco nazionale dei componenti degli Organismi indipendenti di valutazione istituito presso il " +
                            "Dipartimento della funzione pubblica (art. 6, commi 3 e 4, del decreto del " +
                            "Presidente della Repubblica del 9 maggio 2016, n. 105)</p>"),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_DESCRIZIONE_RIDOTTA.value(), "<p><b>Dipartimento della funzione pubblica</b></p>"),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_REQUISITI.value(), "<p style=\"margin-left:0cm; margin-right:0cm\">" +
                            "cons. Marco DE GIORGI<br/>" +
                            "Direttore Generale</p>" +
                            "<p style=\"margin-left:0cm; margin-right:0cm\">" +
                            "<b>Ufficio per la Valutazione della Performance</b><br/>" +
                            "Dipartimento della Funzione Pubblica<br/>" +
                            "Presidenza del Consiglio dei Ministri<br/>" +
                            "via del Sudario n. 49<br/>" +
                            "00186 Roma<br/>" +
                            "0039 06 6899 - 7584<br/>" +
                            "0039 06 6899 - 7147/7636</p>"),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_CODICE.value(), OIV),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_PUBBLICATO.value(), Boolean.FALSE),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_REQUISITI_EN.value(),
                            "<p>si invia il file allegato, relativo a quanto indicato in oggetto.<br/>" +
                                    "Cordiali saluti.</p><p><br/>" +
                                    "<b>Segreteria Ufficio per la Valutazione della Performance</b><br/>" +
                                    "Dipartimento della Funzione Pubblica<br/>" +
                                    "Presidenza del Consiglio dei Ministri<br/>" +
                                    "via del Sudario n. 49<br/>" +
                                    "00186 Roma<br/>" +
                                    "0039 06 6899 - 7584<br/>" +
                                    "mail &nbsp;elenco.oiv@governo.it</p>"),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_ELENCO_ASPECTS.value(),
                            Arrays.asList(
                                    "P:jconon_application:aspect_possesso_cittadinanza_italiana",
                                    "P:jconon_application:aspect_godimento_diritti_ue",
                                    "P:jconon_application:aspect_condanne_penali_required",
                                    "P:jconon_application:aspect_laurea",
                                    "P:jconon_application:aspect_esperienza_professionale",
                                    "P:jconon_application:aspect_sentenza_giudicato",
                                    "P:jconon_application:aspect_condanne_contabili",
                                    "P:jconon_application:aspect_rimosso_incarico_oiv",
                                    "P:jconon_application:aspect_incarichi_pubblici_apicali",
                                    "P:jconon_application:aspect_precedente_incarico_oiv"
                            )
                    ),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_ELENCO_ASPECTS_ULTERIORI_DATI.value(),
                            Arrays.asList(
                                    "P:jconon_application:aspect_fascia_professionale_attribuita"
                            )
                    ),
                    new AbstractMap.SimpleEntry<>(JCONONPropertyIds.CALL_ELENCO_ASPECTS_SEZIONE_CNR.value(),
                            Arrays.asList(
                                    "P:jconon_application:aspect_situazione_lavorativa"
                            )
                    )
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            final Folder callOIV = callOIVService.save(
                    cmisService.createAdminSession(),
                    cmisService.getAdminSession(),
                    null,
                    Locale.ITALY,
                    userId,
                    properties,
                    Collections.emptyMap()
            );
            aclService.addAcl(cmisService.getAdminSession(),
                    callOIV.getProperty(CoolPropertyIds.ALFCMIS_NODEREF.value()).getValueAsString(),
                    Stream.of(
                            new AbstractMap.SimpleEntry<String, ACLType>(CallService.GROUP_CONCORSI, ACLType.Coordinator),
                            new AbstractMap.SimpleEntry<String, ACLType>(CallService.GROUP_EVERYONE, ACLType.Consumer))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
            callOIV.createDocument(
                    Stream.of(
                            new AbstractMap.SimpleEntry<String, String>(PropertyIds.NAME, "labels.json"),
                            new AbstractMap.SimpleEntry<String, String>(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                    new ContentStreamImpl("labels.json", BigInteger.ZERO, "application/json",
                            this.getClass().getResourceAsStream("/labels-call-oiv.json")),
                    VersioningState.MAJOR
            );

        }
    }
}
