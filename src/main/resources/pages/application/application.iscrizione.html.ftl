<html>
<body>
<#assign aDateTime = .now>
<p>${message('mail.confirm.application.1')} <b>${folder.getPropertyValue("jconon_application:nome")?cap_first} ${folder.getPropertyValue("jconon_application:cognome")?cap_first}</b>,</p>
<p>${message('mail.iscrizione.application.1', folder.getPropertyValue("jconon_application:data_domanda").time?string("dd/MM/yyyy '(h. ' HH:mm:ss')'"))}</p>
<p>${message('mail.iscrizione.application.2')} ${message('mail.iscrizione.application.fascia.' + folder.getPropertyValue("jconon_application:fascia_professionale_attribuita"))}</p>
<p>${message('mail.iscrizione.application.3', folder.getPropertyValue("jconon_application:progressivo_iscrizione_elenco"), aDateTime?string("dd MMMM yyyy"))}</p>
<p>${message('mail.iscrizione.application.4')}</p>
${message('mail.iscrizione.application.4.1')}
${message('mail.iscrizione.application.4.2')}
${message('mail.iscrizione.application.4.3')}
${message('mail.iscrizione.application.4.4')}
${message('mail.iscrizione.application.4.5')}
${message('mail.iscrizione.application.4.6')}
<p>${message('mail.iscrizione.application.5')}</p>
<p>${message('mail.iscrizione.application.6')}</p>
<br>
${call.getPropertyValue("jconon_call:requisiti")}
</body>
</html>
