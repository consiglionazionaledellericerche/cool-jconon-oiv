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
      <p>${message('label.home.elenco')}</p>
      <h2>Come iscriversi</h2>
      <p>${message('label.home.comeiscriversi')}</p>
      <h5>
        <p>
          <#if context.user.guest>
          Per accedere utilizzare il link <a href="/login">${message('label.login')}</a>.<br>
          Per registrarsi utilizzare il link <a href="/create-account?guest=true">Nuova registrazione</a>.<br>
          </#if>
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
