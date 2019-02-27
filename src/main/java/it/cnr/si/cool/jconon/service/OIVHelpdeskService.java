package it.cnr.si.cool.jconon.service;

import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.si.cool.jconon.exception.HelpDeskNotConfiguredException;
import it.cnr.si.cool.jconon.model.HelpdeskBean;
import it.cnr.si.cool.jconon.service.helpdesk.HelpdeskService;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

@Service
@Primary
public class OIVHelpdeskService extends HelpdeskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OIVHelpdeskService.class);
    @Value("${helpdesk.pest.url}")
    private String helpdeskPestURL;
    @Value("${helpdesk.username}")
    private String userName;
    @Value("${helpdesk.password}")
    private String password;
    @Value("${mail.from.default}")
    private String sender;

    @Override
    public String sendReopenMessage(HelpdeskBean hdBean) throws MailException, IOException {
        return sendOIVReopenMessage(hdBean, null);
    }

    @Override
    public String post(HelpdeskBean hdBean, MultipartFile allegato, CMISUser user) throws IOException, MailException, CmisObjectNotFoundException {
        hdBean.setMatricola("0");
        if (user != null && !user.isGuest()
                && user.getFirstName() != null
                && user.getFirstName().equals(hdBean.getFirstName())
                && user.getLastName() != null
                && user.getLastName().equals(hdBean.getLastName())
                && user.getMatricola() != null) {
            hdBean.setMatricola(String.valueOf(user.getMatricola()));
        }
        // eliminazione caratteri problematici
        hdBean.setSubject(cleanText(hdBean.getSubject()));
        hdBean.setFirstName(cleanText(hdBean.getFirstName()));
        hdBean.setLastName(cleanText(hdBean.getLastName()));
        hdBean.setMessage(cleanText(hdBean.getMessage()));
        hdBean.setEmail(hdBean.getEmail().trim());
        hdBean.setConfirmRequested(Optional.ofNullable(user).filter(CMISUser::isGuest).map(cmisUser -> Boolean.TRUE).orElse(Boolean.FALSE));

        Integer category = Integer.valueOf(hdBean.getCategory());
        try {
            if (getEsperti(category).equals("{}")) {
                LOGGER.error("La categoria con id " + category + " (Bando \"" + hdBean.getCall() + "\") NON HA NESSUN ESPERTO!");
            }
            if (category == 1) {
                LOGGER.warn("Il Bando \"" + hdBean.getCall() + "\" NON HA NESSUN ID ASSOCIATO ALLA CATEGORIA " + hdBean.getProblemType() + " !");
            }
        } catch (HelpDeskNotConfiguredException _ex) {
        }
        return sendOIVReopenMessage(hdBean, allegato);
    }

    private String testoSegnalazione(HelpdeskBean hdBean) {
        // aggiunge il footer al messaggio
        StringBuilder testo = new StringBuilder();
        testo.append(hdBean.getMessage());
        testo.append("\n\n");
        testo.append("Utente: ");
        testo.append(hdBean.getFirstName());
        testo.append(" ");
        testo.append(hdBean.getLastName());
        testo.append("  Matricola: ");
        testo.append(hdBean.getMatricola());
        testo.append("  Email: ");
        testo.append(hdBean.getEmail());
        testo.append("  Tel: ");
        testo.append(hdBean.getPhoneNumber());
        testo.append("  Data: ");
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy (HH:mm:ss)");
        testo.append(formatter.format(Calendar.getInstance().getTime()));
        testo.append("  IP: ");
        testo.append(hdBean.getIp());
        return testo.toString();
    }

    private String sendOIVReopenMessage(HelpdeskBean hdBean, MultipartFile allegato) throws MailException, IOException {
        JSONObject json = new JSONObject();
        json.put("titolo", hdBean.getCall() + " - " + hdBean.getSubject());
        json.put("categoria", hdBean.getCategory());
        json.put("categoriaDescrizione", hdBean.getCall() + " - " + hdBean.getProblemType());
        json.put("firstName", hdBean.getFirstName());
        json.put("familyName", hdBean.getLastName());
        json.put("email", hdBean.getEmail());
        json.put("descrizione", testoSegnalazione(hdBean));
        json.put("confirmRequested", Optional.ofNullable(hdBean.isConfirmRequested()).map(aBoolean -> {
            if (aBoolean)
                return "y";
            return "n";
        }).orElse("y"));
        UrlBuilder url = new UrlBuilder(helpdeskPestURL);
        EntityEnclosingMethod method;
        if (Optional.ofNullable(hdBean.getId()).isPresent()) {
            json.put("idSegnalazione", hdBean.getId());
            json.put("stato", 1);
            json.put("nota", testoSegnalazione(hdBean));
            json.put("login", "mail");
            method = new PostMethod(url.toString());
        } else {
            method = new PutMethod(url.toString());
        }
        try {
            method.setRequestEntity(new StringRequestEntity(json.toString(), "application/json", "UTF-8"));
            HttpClient httpClient = getHttpClient();
            int statusCode = httpClient.executeMethod(method);
            if (statusCode != HttpStatus.CREATED.value() && statusCode != HttpStatus.NO_CONTENT.value()) {
                LOGGER.error("Errore in fase di creazione segnalazione helpdesk dalla URL: {} JSON {}", helpdeskPestURL, json);
                LOGGER.error(method.getResponseBodyAsString());
            } else {
                String id = method.getResponseBodyAsString();
                if (allegato != null && !allegato.isEmpty()) {
                    UrlBuilder urlAllegato = new UrlBuilder(helpdeskPestURL.concat("/").concat(id));
                    PostMethod methodAllegato = new PostMethod(urlAllegato.toString());
                    try {
                        FilePart filePart = new FilePart("allegato", new ByteArrayPartSource(allegato.getName(), allegato.getBytes()));
                        Part[] parts = {filePart};
                        methodAllegato.setRequestEntity(new MultipartRequestEntity(parts, methodAllegato.getParams()));
                        int statusCodeAllegato = httpClient.executeMethod(methodAllegato);
                        if (HttpStatus.NO_CONTENT.value() != statusCodeAllegato) {
                            LOGGER.error("Errore in fase di creazione allegato helpdesk dalla URL {} id {}", helpdeskPestURL, id);
                            LOGGER.error(methodAllegato.getResponseBodyAsString());
                        }
                    } finally {
                        methodAllegato.releaseConnection();
                    }
                }
                LOGGER.debug(method.getResponseBodyAsString());
                return id;
            }
        } catch (IOException e) {
            LOGGER.error("Errore in fase di creazione della segnalazione helpdesk - "
                    + e.getMessage() + " dalla URL:" + helpdeskPestURL, e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    private HttpClient getHttpClient() {
        HttpClient httpClient = new HttpClient();
        Credentials credentials = new UsernamePasswordCredentials(userName, password);
        httpClient.getState().setCredentials(AuthScope.ANY, credentials);
        return httpClient;
    }

    private String cleanText(String text) {
        if (text == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 32)
                continue;
            if (c == 224 || c == 225)
                sb.append('a');
            else if (c == 232 || c == 233)
                sb.append('e');
            else if (c == 236 || c == 237)
                sb.append('i');
            else if (c == 242 || c == 243)
                sb.append('o');
            else if (c == 249 || c == 250)
                sb.append('u');
            else if (c < 127)
                sb.append(c);
        }
        return sb.toString();
    }
}
