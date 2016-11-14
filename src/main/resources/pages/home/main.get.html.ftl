<div class="header container">
  <div class="jumbotron">
    <h1>
      <img class="logo-fp" alt="Emblema della Repubblica Italiana" id="logo" src="https://performance.gov.it/sites/all/themes/custom/portaletrasparenza_theme/img/logo.svg">
      ${message('label.home.title.h2')}
    </h1>
  </div>
</div>


<div class="container">
  <div class="container-fluid">
    <div class="row-fluid">
      <div class="list-main-call span12">
        <table class="table" id="items">
          <thead><tr>
            <th>
              <h3>${message('label.h3.call')}</h3>
            </th>
            <th>
              <div id="orderBy" class="btn-group">
                <a class="btn btn-mini dropdown-toggle" data-toggle="dropdown" href="#">
                  ${message('button.order.by')}
                  <span class="caret"></span>
                </a>
                <ul class="dropdown-menu"></ul>
              </div>
            </th></tr>
          </thead>
        </table>
        <div id="itemsPagination" class="pagination pagination-centered">
          <ul></ul>
        </div>
        <div id="emptyResultset" class="alert" style="display:none">${message('message.no.call')}</div>
      </div><!--/span-->
    </div><!--/row-->
  </div>
</div> <!-- /container -->
