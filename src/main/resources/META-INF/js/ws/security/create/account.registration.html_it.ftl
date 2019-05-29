<html>
<body>
<div style="font-size: 11pt">Credenziali di accesso per ${account.fullName}</div>
<hr/>
<p>Nome utente: ${account.userName}</p>
<hr/>
<p>${message('mail.account.registration.3')}</p>
<p><a href="${url}/rest/security/confirm-account?userid=${account.userName}&pin=${account.pin}">Attivazione utenza</a>.</p>
<p>Se il link non dovesse funzionare trascriva il testo sottostante nella barra degli indirizzi del suo browser.</p>
<p>${url}/rest/security/confirm-account?userid=${account.userName}&pin=${account.pin}</p>
<br/>
<p>${message('mail.account.registration.5')}</p>
<p><a href="${url}/login">${url}/login</a></p>
<p>Trascorsa un'ora dalla ricezione l'utenza viene eliminata, pertanto occorre ripetere ex novo la procedura di registrazione.</p>
<p>Per richieste di informazioni o assistenza, inviare una segnalazione utilizzando la sezione <a href="${url}/helpdesk">helpdesk</a> del sito.</p>
<hr/>
<p>Questo messaggio e' stato generato da un sistema automatico.</p>
<p>Si prega di non rispondere.</p>
</body>
</html>
