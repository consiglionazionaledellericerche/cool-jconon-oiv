<div class="header container">
  <div class="jumbotron">
    <h1>
      <img class="logo-fp" alt="Emblema della Repubblica Italiana" id="logo" src="https://performance.gov.it/sites/all/themes/custom/portaletrasparenza_theme/img/logo.svg">
      ${message('main.title')}
    </h1>
  </div>
</div>
<div class="container">
  <div class="container-fluid">
    <div class="row-fluid">
      <h2>Cos'è l'elenco</h2>
      <p>
Con D.M. (……..) è stato istituito presso il Dipartimento della funzione pubblica l’Elenco nazionale dei componenti degli Organismi indipendenti di valutazione (art. 6, commi 3 e 4, del decreto del Presidente della Repubblica del 9 maggio 2016, n.105).
L’iscrizione all’Elenco nazionale è condizione necessaria per la partecipazione alle procedure di nomina degli Organismi indipendenti di valutazione.
Le domande di iscrizione all’Elenco nazionale sono presentate dai soggetti dotati dei requisiti di cui all’articolo 2 del D.M.
La richiesta di iscrizione nell’elenco deve essere effettuata esclusivamente con modalità telematica, attraverso questo Portale, secondo quanto disposto dall’articolo 3 del richiamato decreto.
Al termine dell’inserimento dei dati verrà inviata una email di conferma all’indirizzo di posta elettronica fornito in fase di registrazione con allegata la  domanda di iscrizione. Nella domanda, scaricabile anche dalla pagina personale del profilo creato in fase di registrazione, sarà indicata la fascia professionale attribuita sulla base delle informazioni inserite. 
      ${message('mail.confirm.application.oiv', 'ELENCO_NAZIONALE_NOME_COGNOME', 'ELENCO_NAZIONALE_NOME_COGNOME')}
      <h2>Come iscriversi</h2>
        <p>
        Per inserire la domanda di iscrizione all’Elenco nazionale dei componenti degli organismi indipendenti di valutazione occorre accedere all'area utenti inserendo Nome utente e password scelti in fase di registrazione al sito. In fase di completamento della procedura di iscrizione sarà richiesta l’indicazione di un indirizzo di Posta Elettronica Certificata, il caricamento di copia del documento di identità e del curriculum vitae in formato europeo, datato e firmato.
        </p>
      <h5>
        <p>
          Per registrarsi utilizzare il link <a href="/create-account?guest=true">Nuova registrazione</a>.<br>
          Per maggiori informazioni consultare il capitolo "Accesso / registrazione in procedura" del Manuale Utente.<br> 
          Per ricevere assistenza utilizzare il servizio di <a href="/helpdesk">Helpdesk</a>
        </p>
        <#if !context.user.guest>
        <p>
          Per presentare la propria candidatura utilizzare i seguente <a href="/manage-application?callCodice=OIV">link</a>
        </p>
        </#if>
    </h5>
    </div><!--/row-->
  </div>
</div> <!-- /container -->
