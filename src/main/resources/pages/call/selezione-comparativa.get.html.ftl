<div class="container">
  <div class="container-fluid">
    <div class="row-fluid">
      <div class="span3">
        <div id="criteria">
          <div class="pull-right">
            <p>              
            <a id="createNew" type="button" class="btn btn-primary hide" href="/manage-call?call-type=F:jconon_call_procedura_comparativa:folder"><i class="icon-plus icon-white"></i> Inserisci nuovo annuncio</a>
            </p>
            <div id="orderBy" class="btn-group" style="display: block;">
              <a class="btn btn-success dropdown-toggle" data-toggle="dropdown" href="#">
                ${message('button.order.by')}
                <span class="caret"></span>
              </a>
              <ul class="dropdown-menu"></ul>
            </div>
            <p>
              <div class="btn-group">
                <button id="applyFilter" type="button" class="btn btn-primary btn-small"><i class="icon-filter icon-white"></i> Filtra</button>
                <button id="resetFilter" class="btn btn-small"><i class="icon-repeat"></i> Reset</button>
              </div>
            </p>
          </div>
        </div>
      </div><!--/span-->
      <div class="list-main-call span9">
        <h3>${message('title.selezioni.comparative')}</h3>
        <table class="table table-striped fixed-layout" id="items"></table>
        <div id="itemsPagination" class="pagination pagination-centered">
          <ul></ul>
        </div>
        <p>
          <div id="emptyResultset" class="alert" style="display:none">${message('message.no.selezioni.comparative')}</div>
        </p>
        <small id="total" class="muted pull-right"></small>
      </div><!--/span-->
    </div><!--/row-->
  </div>
</div> <!-- /container -->