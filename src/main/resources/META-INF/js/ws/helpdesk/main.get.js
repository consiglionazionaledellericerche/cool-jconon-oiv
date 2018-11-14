define(['jquery', 'header', 'cnr/cnr.bulkinfo', 'cnr/cnr', 'cnr/cnr.url', 'cnr/cnr.jconon', 'json!common', 'cnr/cnr.ui', 'i18n', 'json!cache', 'cnr/cnr.call', 'cnr/cnr.search'], function($, header, BulkInfo, CNR, URL, jconon, common, UI, i18n, cache, Call, Search) {
  "use strict";

  var bulkinfoReopen,
    bulkinfo,
    nameForm = 'helpDesk',
    helpDesk = $('#helpdesk2'),
    callTypes = [],
    ul = $('.cnraffix'),
    helpDeskBulkinfo = $('<div id="helpdesk"></div>'),
    inputFile = $('<div class="control-group form-horizontal"><label for="message" class="control-label">' + i18n['label.allega'] + '</label><div class="controls"> <input type="file" title="Search file to attach" name="allegato" /> </div> </div>'),
    btnSend = $('<div class="text-center"> <button id="send" name="send" class="btn btn-primary">' + i18n['button.send'] + '<i class="ui-button-icon-secondary ui-icon ui-icon-mail-open" ></i></button> </div>'),
    btnReopen = $('<div class="text-center"> <button id="sendReopen" class="btn btn-primary">' + i18n['button.send'] + '<i class="ui-button-icon-secondary ui-icon ui-icon-mail-open" ></i></button> </div>');


  function bulkinfoFunction() {
    bulkinfo = new BulkInfo({
      target: helpDeskBulkinfo,
      path: 'helpdeskBulkInfo',
      name: nameForm,
      callback: {
        beforeCreateElement: function(item) {
          if (item.name === 'call-type' || item.name === 'call') {
            item.widget = '';
            item.inputType = 'TEXT';
          }
        },
        afterCreateForm: function() {
          var ids = {};
          // riempio alcuni campi in casi di utente loggato
          if (!common.User.guest) {
            $('#firstName').val(common.User.firstName);
            $('#firstName').attr("readonly", "true");
            $('#lastName').val(common.User.lastName);
            $('#lastName').attr("readonly", "true");
            $('#email').val(common.User.email);
            $('#confirmEmail').val(common.User.email);
            $('#email').attr("readonly", "true");
          }
          helpDesk.append(inputFile);
          helpDesk.append(btnSend);

          $('#call').parents('.control-group').remove();
          $('#call-type').parents('.control-group').remove();

          $('#send').click(ids, function() {
            sendFunction(ids);
          });


          function sendFunction(ids) {
            var formData = new CNR.FormData(),
              nameCall = $('#call').val(),
              call,
              problemType = $('#problemType .active').data('value'),
              idCategory;

            if (bulkinfo.validate()) {
              $.each(bulkinfo.getData(), function(index, item) {
                //cmis:objectTypeId è il parametro sul "tipo" di bando e non viene passato
                if (item.name !== 'cmis:objectTypeId') {
                  formData.data.append(item.name, item.value);
                }
              });
              formData.data.append('allegato', $('input[type=file]')[0].files[0]);

              formData.data.set('call', i18n.prop('app.name'));

              if (problemType === 'Problema Tecnico') {
                idCategory = 13;
              } else if (problemType === 'Problema Normativo') {
                idCategory = 14;
              } else {
                UI.info('Occorre selezionare almeno un "Problema"');
                return false;
              }
              //setto l'id della categoria nel formData
              formData.data.append('category', idCategory);

              if (idCategory !== null && nameCall !== null) {
                jconon.Data.helpdesk.send({
                  type: 'POST',
                  data: formData.getData(),
                  contentType: formData.contentType,
                  processData: false,
                  success: function(data) {
                    //Scrivo il messaggio di successo in grassetto e raddoppio i </br>
                    helpDesk.remove();
                    $('#intestazione').html(i18n['message.helpdesk.send.success'].replace(/<\/br>/g, "</br></br>")).addClass('alert alert-success').css("font-weight", "Bold");
                  },
                  error: function(data) {
                    UI.error(i18n['message.helpdesk.oiv.send.failed']);
                  }
                });
              } else {
                if (idCategory === null) {
                  UI.info('Selezionare almeno un "Bando di riferimento"');
                }
              }
            }
            return false;
          }
        }
      }
    });
  }


  function loadPage() {
    bulkinfoFunction();
    bulkinfo.render();
    helpDesk.append(helpDeskBulkinfo);
  }

  // helpdesk in caso di "reopen"
  if (URL.querystring.from.id && URL.querystring.from.azione) {
    bulkinfoReopen = new BulkInfo({
      target: helpDesk,
      path: 'helpdeskBulkInfo',
      name: 'reopen_HelpDesk',
      callback: {
        afterCreateForm: function() {
          $('#helpdeskBulkInfo').append(btnReopen);
          $('#sendReopen').click(function() {
            var formData = new CNR.FormData(),
              fd;
            $.each(bulkinfoReopen.getData(), function(index, el) {
              formData.data.append(el.name, el.value);
            });
            fd = formData.getData();
            fd.append('id', URL.querystring.from.id);
            fd.append('azione', URL.querystring.from.azione);
            if (bulkinfoReopen.validate()) {
              jconon.Data.helpdesk.send({
                type: 'POST',
                data: fd,
                contentType: formData.contentType,
                processData: false,
                success: function(data) {
                  UI.info(i18n['message.reopen.helpdesk.send.success'], function() {
                    window.location = URL.urls.root;
                  });
                },
                error: function() {
                  UI.error(i18n['message.reopen.helpdesk.send.failed'], function() {
                    window.location = URL.urls.root;
                  });
                }
              });
            }
            return false;
          });
        }
      }
    });
    bulkinfoReopen.render();
  } else {
    if (!common.User.guest) {
      // se l'utente è loggato carico meno campi e alcuni campi vengono valorizzati
      nameForm = 'user_HelpDesk';
    }
    loadPage();
  }
});
