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

package it.cnr.si.cool.jconon.service.application;

import it.cnr.si.cool.jconon.flows.model.ProcessInstanceResponse;
import it.cnr.si.cool.jconon.flows.model.StartWorkflowResponse;
import it.cnr.si.cool.jconon.flows.model.TaskResponse;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FlowsService {
    public static final String DD_MM_YYYY = "dd/MM/yyyy";
    @Autowired
    private OAuth2RestOperations restTemplate;
    @Value("${flows.taskComplete}")
    private String taskCompleteUrl;
    @Value("${flows.processInstance}")
    private String processInstanceUrl;
    @Value("${flows.currentTask}")
    private String currentTaskUrl;

    @Value("${flows.processDefinitionId}")
    private String processDefinitionId;

    private List<Esperienza> getEsperienze(ItemIterable<QueryResult> queryResultEsperienze, ItemIterable<QueryResult> queryResultOivs) {
        List<Esperienza> result = new ArrayList<>();
        int numeroEsperienza = 0;
        for (QueryResult resultEsperienza : queryResultEsperienze) {
            numeroEsperienza++;
            result.add(
                    new Esperienza()
                            .setIdEsperienza(resultEsperienza.getPropertyValueById(PropertyIds.OBJECT_ID))
                            .setNumeroEsperienza(numeroEsperienza)
                            .setDataInizio(
                                    DateTimeFormatter.ofPattern(DD_MM_YYYY).format(
                                            ZonedDateTime.ofInstant(
                                                    resultEsperienza.<GregorianCalendar>getPropertyValueById("jconon_attachment:esperienza_professionale_da").toInstant(),
                                                    ZoneId.systemDefault()
                                            )
                                    )
                            )
                            .setDataFine(
                                    Optional.ofNullable(resultEsperienza.<GregorianCalendar>getPropertyValueById("jconon_attachment:esperienza_professionale_a"))
                                            .map(Calendar::toInstant)
                                            .map(instant -> DateTimeFormatter.ofPattern(DD_MM_YYYY).format(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())))
                                            .orElse(null)
                            )
                            .setTipologiaEsperienza("Esperienza professionale")
                            .setCodIpa(resultEsperienza.getPropertyValueById("jconon_attachment:esperienza_professionale_cod_amm_ipa"))
                            .setDescrizioneIpa(
                                    Optional.ofNullable(resultEsperienza.<String>getPropertyValueById("jconon_attachment:esperienza_professionale_amministrazione"))
                                            .orElseGet(() -> resultEsperienza.getPropertyValueById("jconon_attachment:esperienza_professionale_datore_lavoro"))
                            )
                            .setAmbitoEsperienza(resultEsperienza.getPropertyValueById("jconon_attachment:esperienza_professionale_area_specializzazione"))
                            .setAttivitaSvolta(resultEsperienza.getPropertyValueById("jconon_attachment:esperienza_professionale_attivita_svolta"))
            );
        }
        for (QueryResult oiv : queryResultOivs) {
            numeroEsperienza++;
            result.add(
                    new Esperienza()
                            .setIdEsperienza(oiv.getPropertyValueById(PropertyIds.OBJECT_ID))
                            .setNumeroEsperienza(numeroEsperienza)
                            .setDataInizio(
                                    DateTimeFormatter.ofPattern(DD_MM_YYYY).format(
                                            ZonedDateTime.ofInstant(
                                                    oiv.<GregorianCalendar>getPropertyValueById("jconon_attachment:precedente_incarico_oiv_da").toInstant(),
                                                    ZoneId.systemDefault()
                                            )
                                    )
                            )
                            .setDataFine(
                                    Optional.ofNullable(oiv.<GregorianCalendar>getPropertyValueById("jconon_attachment:precedente_incarico_oiv_a"))
                                            .map(Calendar::toInstant)
                                            .map(instant -> DateTimeFormatter.ofPattern(DD_MM_YYYY).format(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())))
                                            .orElse(null)
                            )
                            .setTipologiaEsperienza("Incarichi OIV/Nuclei")
                            .setCodIpa(oiv.getPropertyValueById("jconon_attachment:precedente_incarico_oiv_cod_amm_ipa"))
                            .setDescrizioneIpa(
                                    oiv.getPropertyValueById("jconon_attachment:precedente_incarico_oiv_amministrazione")
                            )
                            .setNrDipendenti(Optional.ofNullable(oiv.<String>getPropertyValueById(ApplicationOIVService.JCONON_ATTACHMENT_PRECEDENTE_INCARICO_OIV_NUMERO_DIPENDENTI))
                                    .filter(s -> s.equalsIgnoreCase(ApplicationOIVService.SUP250))
                                    .map(s -> "maggioreUguale250")
                                    .orElse("minoreDi250"))
                            .setAttivitaSvolta(oiv.getPropertyValueById("jconon_attachment:precedente_incarico_oiv_ruolo"))
            );
        }
        return result;
    }

    public Boolean isProcessTerminated(String processInstanceId) {
        // Query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(processInstanceUrl)
                .queryParam("processInstanceId", processInstanceId).queryParam("detail", Boolean.FALSE);

        return Optional.ofNullable(restTemplate.getForEntity(builder.buildAndExpand().toUri(), ProcessInstanceResponse.class))
                .filter(processInstanceResponseResponseEntity -> processInstanceResponseResponseEntity.getStatusCode() == HttpStatus.OK)
                .map(processInstanceResponseResponseEntity -> processInstanceResponseResponseEntity.getBody())
                .map(processInstanceResponse -> Optional.ofNullable(processInstanceResponse.getEndTime()).isPresent())
                .orElse(false);
    }

    public ResponseEntity<TaskResponse> getCurrentTask(String processInstanceId) {
        // Query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(currentTaskUrl)
                .queryParam("processInstanceId", processInstanceId);
        return restTemplate.getForEntity(builder.buildAndExpand().toUri(), TaskResponse.class);
    }

    public ResponseEntity<StartWorkflowResponse> completeTask(Folder domanda,
                                                              TaskResponse currentTask,
                                                              String testoSoccorso,
                                                              List<Document> attachment, String task) throws IOException {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
        params.add("processDefinitionId", processDefinitionId);
        params.add("taskId", currentTask.getId());
        params.add("sceltaUtente", "invia_a_istruttoria");
        params.add(task.equals(TaskResponse.SOCCORSO_ISTRUTTORIO) ? "dataInvioSoccorsoIstruttorio" : "dataInvioNuovaDomanda", DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.ofInstant(Calendar.getInstance().toInstant(), ZoneId.systemDefault())));

        params.add("fasciaAppartenenzaProposta", domanda.<String>getPropertyValue("jconon_application:fascia_professionale_attribuita"));
        params.add(task.equals(TaskResponse.SOCCORSO_ISTRUTTORIO) ? "osservazioniSoccorsoRichiedente" : "osservazioniRichiedente", testoSoccorso);
        String variableFileName = task.equals(TaskResponse.SOCCORSO_ISTRUTTORIO) ? "osservazioni-soccorso-richiedente-allegato-" : "osservazioni-preavviso-rigetto-allegato-";
        AtomicInteger index = new AtomicInteger();
        attachment.stream()
                .forEach(document -> {
                    params.add(variableFileName + index.incrementAndGet(), new MultipartInputStreamFileResource(document.getContentStream().getStream(), document.getName()));
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(params, headers);
        final ResponseEntity<StartWorkflowResponse> startWorkflowResponseResponseEntity = restTemplate.postForEntity(taskCompleteUrl, entity, StartWorkflowResponse.class);
        return startWorkflowResponseResponseEntity;
    }

    public ResponseEntity<StartWorkflowResponse> startWorkflow(Folder domanda,
                                                               ItemIterable<QueryResult> esperienze,
                                                               ItemIterable<QueryResult> oivs,
                                                               MultipartFile fileDomanda,
                                                               Document cv, Document documentoRiconoscimento) throws IOException {
        final Optional<Object> progressivoIscrizioneInElenco =
                Optional.ofNullable(domanda.getPropertyValue("jconon_application:progressivo_iscrizione_elenco"));
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
        params.add("processDefinitionId", processDefinitionId);
        params.add("idDomanda", domanda.getId());
        params.add("titolo", domanda.getName());
        params.add("descrizione", domanda.getName());
        params.add("nomeRichiedente",
                domanda.<String>getPropertyValue("jconon_application:nome").toUpperCase()
                        .concat(" ")
                        .concat(domanda.<String>getPropertyValue("jconon_application:cognome").toUpperCase())
        );
        params.add("dataNascitaRichiedente", domanda.<GregorianCalendar>getPropertyValue("jconon_application:data_nascita").toInstant().toString());
        params.add("sessoRichiedente", domanda.<String>getPropertyValue("jconon_application:sesso"));
        params.add("codiceFiscaleRichiedente", domanda.<String>getPropertyValue("jconon_application:codice_fiscale"));
        params.add("emailRichiedente", domanda.<String>getPropertyValue("jconon_application:email_comunicazioni"));
        params.add("dataInvioDomanda", Optional.ofNullable(domanda.<GregorianCalendar>getPropertyValue("jconon_application:data_domanda"))
                .map(Calendar::toInstant)
                .map(instant -> DateTimeFormatter.ofPattern(DD_MM_YYYY).format(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())))
                .orElse(null));
        params.add("tipologiaRichiesta",
                progressivoIscrizioneInElenco
                        .map(o -> "modifica_fascia")
                        .orElse("Iscrizione"));
        if (progressivoIscrizioneInElenco.isPresent()) {
            params.add("dataIscrizioneElenco",
                    Optional.ofNullable(domanda.<GregorianCalendar>getPropertyValue("jconon_application:data_iscrizione_elenco"))
                            .map(Calendar::toInstant)
                            .map(instant -> DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())))
                            .orElse(null)
            );
            params.add("codiceIscrizioneElenco", progressivoIscrizioneInElenco.get());
        }
        params.add("fasciaAppartenenzaProposta", domanda.<String>getPropertyValue("jconon_application:fascia_professionale_attribuita"));

        params.add("valutazioneEsperienze_json", getEsperienze(esperienze, oivs));
        params.add("domanda", new MultipartInputStreamFileResource(fileDomanda.getInputStream(), fileDomanda.getOriginalFilename()));
        Optional.ofNullable(cv)
                .map(document -> new MultipartInputStreamFileResource(document.getContentStream().getStream(), document.getName()))
                .ifPresent(multipartInputStreamFileResource -> params.add("cv", multipartInputStreamFileResource));
        Optional.ofNullable(documentoRiconoscimento)
                .map(document -> new MultipartInputStreamFileResource(document.getContentStream().getStream(), document.getName()))
                .ifPresent(multipartInputStreamFileResource -> params.add("cartaIdentita", multipartInputStreamFileResource));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(params, headers);
        final ResponseEntity<StartWorkflowResponse> startWorkflowResponseResponseEntity = restTemplate.postForEntity(taskCompleteUrl, entity, StartWorkflowResponse.class);
        return startWorkflowResponseResponseEntity;
    }

    class MultipartInputStreamFileResource extends InputStreamResource {

        private final String filename;

        MultipartInputStreamFileResource(InputStream inputStream, String filename) {
            super(inputStream);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public long contentLength() throws IOException {
            return -1; // we do not want to generally read the whole stream into memory ...
        }
    }

    public class Esperienza {
        private String idEsperienza;
        private Integer numeroEsperienza;
        private String dataInizio;
        private String dataFine;
        private String tipologiaEsperienza;
        private String ambitoEsperienza;
        private String attivitaSvolta;
        private String annotazioniValutatore;
        private String codIpa;
        private String descrizioneIpa;
        private String nrDipendenti;

        public Esperienza() {
        }

        public String getIdEsperienza() {
            return idEsperienza;
        }

        public Esperienza setIdEsperienza(String idEsperienza) {
            this.idEsperienza = idEsperienza;
            return this;
        }

        public Integer getNumeroEsperienza() {
            return numeroEsperienza;
        }

        public Esperienza setNumeroEsperienza(Integer numeroEsperienza) {
            this.numeroEsperienza = numeroEsperienza;
            return this;
        }

        public String getDataInizio() {
            return dataInizio;
        }

        public Esperienza setDataInizio(String dataInizio) {
            this.dataInizio = dataInizio;
            return this;
        }

        public String getDataFine() {
            return dataFine;
        }

        public Esperienza setDataFine(String dataFine) {
            this.dataFine = dataFine;
            return this;
        }

        public String getTipologiaEsperienza() {
            return tipologiaEsperienza;
        }

        public Esperienza setTipologiaEsperienza(String tipologiaEsperienza) {
            this.tipologiaEsperienza = tipologiaEsperienza;
            return this;
        }

        public String getAmbitoEsperienza() {
            return ambitoEsperienza;
        }

        public Esperienza setAmbitoEsperienza(String ambitoEsperienza) {
            this.ambitoEsperienza = ambitoEsperienza;
            return this;
        }

        public String getAttivitaSvolta() {
            return attivitaSvolta;
        }

        public Esperienza setAttivitaSvolta(String attivitaSvolta) {
            this.attivitaSvolta = attivitaSvolta;
            return this;
        }

        public String getAnnotazioniValutatore() {
            return annotazioniValutatore;
        }

        public Esperienza setAnnotazioniValutatore(String annotazioniValutatore) {
            this.annotazioniValutatore = annotazioniValutatore;
            return this;
        }

        public String getDescrizioneIpa() {
            return descrizioneIpa;
        }

        public Esperienza setDescrizioneIpa(String descrizioneIpa) {
            this.descrizioneIpa = descrizioneIpa;
            return this;
        }

        public String getNrDipendenti() {
            return nrDipendenti;
        }

        public Esperienza setNrDipendenti(String nrDipendenti) {
            this.nrDipendenti = nrDipendenti;
            return this;
        }

        public String getCodIpa() {
            return codIpa;
        }

        public Esperienza setCodIpa(String codIpa) {
            this.codIpa = codIpa;
            return this;
        }
    }
}
