<html>
<body>

<p>${message('mail.confirm.application.1')} <#if folder.getPropertyValue("jconon_application:sesso") == "M">dott.<#else>dott.ssa</#if> <b>${folder.getPropertyValue("jconon_application:nome")?cap_first} ${folder.getPropertyValue("jconon_application:cognome")?cap_first}</b>,</p>
<p>${message('mail.esclusione.application.1')}</p>
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