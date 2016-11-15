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
      <h2>Come iscriversi</h2>
      <p>
      Per inserire la domanda di iscrizione all’Elenco nazionale dei componenti degli organismi indipendenti di valutazione occorre accedere all'area utenti inserendo Nome utente e password scelti in fase di registrazione al sito. In fase di completamento della procedura di iscrizione sarà richiesto il caricamento del curriculum vitae in formato europeo, datato e firmato e di copia del documento di identità.
      </p>
      <h5>
        <p>
          Per registrarsi utilizzare il link <a href="/create-account?guest=true">Nuova registrazione</a>. 
          Si ricorda che in fase di registrazione sarà richiesta l’indicazione di un indirizzo di Posta Elettronica.
          Per maggiori informazioni consultare il capitolo "Accesso / registrazione in procedura" del Manuale Utente. 
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
