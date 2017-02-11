<div class="container">
  <div class="container-fluid">
    <div class="row-fluid">
      <#if args.callCodice?? || args['cmis:objectId']??>
      <div class="span3">
        <div class="cnr-sidenav">
          <ul class="nav nav-list cnraffix" ></ul>
        </div>
        <br/>
        <div>
          <div id="criteria">
            <div class="pull-right">              
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
        </div>
      </div><!--/span-->
      </#if>
      <div class="list-main-call <#if args.callCodice?? || args['cmis:objectId']??>span9<#else>span12</#if> table-responsive">
        <table class="table table-striped" id="items">
          <caption><h2 class="jumbotron header well">Il mio profilo</h2></caption>
        </table>
        <div id="itemsPagination" class="pagination pagination-centered">
          <ul></ul>
        </div>
        <p>
          <div id="emptyResultset" class="alert">
            <strong>Non e' stata presentata nessuna domanda nell'Elenco nazionale OIV, per presentarne una utilizzare il seguente <a href="manage-application?callCodice=OIV">link</a></strong>
          </div>
        <small id="total" class="muted pull-right"></small>          
      </div><!--/span-->
    </div><!--/row-->
  </div>
</div> <!-- /container -->