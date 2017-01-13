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
            <div class="text-center">
              <div class="btn-group">
                <button id="applyFilter" type="button" class="btn btn-primary btn-small"><i class="icon-filter icon-white"></i> Filtra</button>
                <button id="resetFilter" class="btn btn-small"><i class="icon-repeat"></i> Reset</button>
              </div>
            </div>
          </div>
        </div>
      </div><!--/span-->
      </#if>
      <div class="list-main-call <#if args.callCodice?? || args['cmis:objectId']??>span9<#else>span12</#if> table-responsive">
        <table class="table table-striped" id="items">
          <caption><h2 class="jumbotron header well">Le mie domande</h2></caption>
        </table>
        <div id="itemsPagination" class="pagination pagination-centered">
          <ul></ul>
        </div>
        <small id="total" class="muted pull-right"></small>
        <p>
          <div id="emptyResultset" class="alert">
            <strong>Non e' stata presentata nessuna domanda</strong>
          </div>
      </div><!--/span-->
    </div><!--/row-->
  </div>
</div> <!-- /container -->