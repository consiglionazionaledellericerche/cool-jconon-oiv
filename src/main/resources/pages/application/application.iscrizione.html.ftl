<html>
<body>
<#assign aDateTime = .now>
<p>${message('mail.confirm.application.1')} <b>${folder.getPropertyValue("jconon_application:nome")?cap_first} ${folder.getPropertyValue("jconon_application:cognome")?cap_first}</b>,</p>
<p>${message('mail.iscrizione.application.1', folder.getPropertyValue("jconon_application:data_domanda").time?string("dd/MM/yyyy HH:mm:ss"))}</p>
<p>${message('mail.iscrizione.application.2')} ${message('mail.iscrizione.application.fascia.' + folder.getPropertyValue("jconon_application:fascia_professionale_attribuita"))}</p>
<p>${message('mail.iscrizione.application.3', folder.getPropertyValue("jconon_application:progressivo_iscrizione_elenco"), aDateTime?string("dd MMMM yyyy"))}</p>
<p>${message('mail.iscrizione.application.4')}</p>
<p>${message('mail.iscrizione.application.5')}</p>
<p>${message('mail.iscrizione.application.6')}</p>
<br>
${message('mail.iscrizione.application.7')}<br>
${message('mail.iscrizione.application.8')}<br>
<br>
${message('mail.iscrizione.application.9')}<br>
${message('mail.iscrizione.application.10')}<br>
${message('mail.iscrizione.application.11')}<br>
${message('mail.iscrizione.application.12')}<br>
${message('mail.iscrizione.application.13')}<br>
${message('mail.iscrizione.application.14')}<br>
${message('mail.iscrizione.application.15')}<br>
</body>
</html>
