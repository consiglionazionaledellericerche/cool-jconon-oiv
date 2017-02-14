<html>
<body>

<p>${message('mail.confirm.application.1')} <#if folder.getPropertyValue("jconon_application:sesso") == "M">dott.<#else>dott.ssa</#if> <b>${folder.getPropertyValue("jconon_application:nome")?cap_first} ${folder.getPropertyValue("jconon_application:cognome")?cap_first}</b>,</p>
<p>${message('mail.esclusione.application.1')}</p>
<p>${message('mail.iscrizione.application.6')}</p>
<br>
${message('mail.esclusione.application.8')}<br>
${message('mail.esclusione.application.9')}<br>
<br>
${message('mail.esclusione.application.10')}<br>
${message('mail.esclusione.application.11')}<br>
${message('mail.esclusione.application.12')}<br>
${message('mail.esclusione.application.13')}<br>
${message('mail.esclusione.application.14')}<br>
${message('mail.esclusione.application.15')}<br>
${message('mail.esclusione.application.16')}<br>
</body>
</html>