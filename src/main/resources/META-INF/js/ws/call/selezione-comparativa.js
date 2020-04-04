/*global params*/
define(['jquery', 'i18n', 'header', 'cnr/cnr.actionbutton', 'cnr/cnr.search',
  'cnr/cnr.bulkinfo', 'cnr/cnr.ui',
  'json!common', 'cnr/cnr.jconon', 'cnr/cnr.url', 'cnr/cnr.call', 'cnr/cnr.ace', 'json!cache', 'fp/fp.application', 'cnr/cnr', 'cnr/cnr.attachments'
  ], function ($, i18n, header, ActionButton, Search, BulkInfo, UI, common, jconon, URL, Call, Ace, cache, ApplicationFp, CNR, Attachments) {
  "use strict";
  var rootTypeId = 'F:jconon_call_procedura_comparativa:folder',
    rootQueryTypeId = 'jconon_call_procedura_comparativa:folder root',
    ul = $('.cnraffix'),
    elements = {
      table: $('#items'),
      pagination: $('#itemsPagination'),
      orderBy: $('#orderBy'),
      label: $('#emptyResultset'),
      total: $('#total'),
    },
    search,
    bulkInfo;

  function isGestore() {
    return ((common.User.other && common.User.other.amministrazione) || common.User.admin || (common.User.groupsArray &&
        (common.User.groupsArray.indexOf('GROUP_CONCORSI') !== -1 || common.User.groupsArray.indexOf('GROUP_GESTORI_PROCEDURE_COMPARATIVE') !== -1)));
  }

  function init(value) {
      var criteria = ApplicationFp.getCriteria(bulkInfo, value);
      if (value === 'dapubblicare') {
        criteria.isNull('root.jconon_call:data_inizio_invio_domande');
      }
      criteria.list(search);
  }

  function manageFilterClick() {
    $('#applyFilter').on('click', function () {
      init($('#filters-attivi_scaduti > button.btn.active').attr('data-value'));
    });
    $('#filters-attivi_scaduti').closest('.widget').on('setData', function (event, key, value) {
      init(value);
    });
    $('#resetFilter').off('click').on('click', function () {
      $('#F\\:jconon_call_procedura_comparativa\\:folder input').val('');
      $('#F\\:jconon_call_procedura_comparativa\\:folder select').select2('val','');
      $('#filters-attivi_scaduti button').removeClass('active');
      $('#filters-attivi_scaduti button[data-value=attivi]').addClass('active');
      $('#F\\:jconon_call_procedura_comparativa\\:folder .widget:not(:has(#filters-attivi_scaduti))').data('value', '');
      $('#filters-attivi_scaduti').parents('.widget').data('value', 'attivi');
    });
  }

  function displayCall(typeId, queryTypeId) {
    URL.Data.bulkInfo({
      placeholder: {
        path: typeId,
        kind: 'column',
        name: 'default'
      },
      data: {
        guest : true
      },
      success: function (data) {
        var columns = [],
          sortFields = {
            nome: false,
            "data di creazione" : false
          };
        $.map(data[data.columnSets[0]], function (el) {
          if (el.inSelect !== false) {
            columns.push(el.property);
          }
        });
        $.each(data[data.columnSets[0]], function (index, el) {
          if (el['class'] && el['class'].split(' ').indexOf('sort') >= 0) {
            sortFields[i18n.prop(el.label, el.label)] = el.property;
          }
        });
        search = new Search({
          elements: elements,
          maxItems: 10,
          fetchCmisObject: false,
          type: queryTypeId,
          calculateTotalNumItems: true,
          fields: sortFields,
          dataSource: function (page, setting, getUrlParams) { 
            var deferred = URL.Data.search.query({
                queue: setting.disableRequestReplay,
                data: getUrlParams(page)
            });
            deferred.done(function (data) {
              if (elements.total) {
                elements.total.text('Avvisi pubblici trovati: ' + data.totalNumItems);
              }
            });
            return deferred;
          },
          mapping: function (mapping, doc) {
            $.each(data[data.columnSets[0]], function (index, el) {
              mapping[el.name] = doc[el.property] !== undefined ? doc[el.property] : null;
            });
            mapping.aspect = doc.aspect !== undefined ? doc.aspect : null;
            return mapping;
          },
          display: {
            resultSet: function (resultSet, target) {
            	displayRow(bulkInfo, search, typeId, rootTypeId, resultSet, target);
            }
          }
        });
        if (isGestore()) {
          init('dapubblicare');
        } else {
          $('#filters-attivi_scaduti button').removeClass('active');
          $('#filters-attivi_scaduti button[data-value=attivi]').addClass('active');
          init('attivi');          
        }
      }
    });
  }

  function displayRow(bulkInfo, search, typeId, rootTypeId, resultSet, target) {
    var xhr = new BulkInfo({
      target: $('<tbody>').appendTo(target),
      handlebarsId: 'selezioni-comparative-results',
      path: typeId,
      metadata: resultSet,
      handlebarsSettings: {
        call_type: typeId === rootTypeId ? true : false
      }
    }).handlebars();

    xhr.done(function () {
      target.off('click').on('click', '.displayMetadata', function (e) {
        var f = URL.Data.node.node;
        f({
          data: {
            "nodeRef" : e.currentTarget.id,
            "shortQNames" : true
          }
        }).done(function (metadata) {
          new BulkInfo({
            handlebarsId: 'zebra',
            name: 'display',
            path: rootTypeId,
            metadata: metadata
          }).handlebars().done(function (html) {
            var content = $('<div></div>').addClass('modal-inner-fix').append(html),
              title = i18n.prop("modal.title.view." + rootTypeId, 'Propriet&agrave;');
            UI.bigmodal(title, content);
          });
        });    
        return e.preventDefault();
      });

      var rows = target.find('tbody tr'),
        customButtons = {
          select: false,
          copy: false,
          cut: false
        };
      $.each(resultSet, function (index, el) {
        var secondaryObjectTypeIds = el['cmis:secondaryObjectTypeIds'] || el.aspect,
          isMacroCall = secondaryObjectTypeIds === null ? false : secondaryObjectTypeIds.indexOf('P:jconon_call:aspect_macro_call') >= 0,
          row,
          azioni,
          isActive = ApplicationFp.callIsActive(el.data_inizio_invio_domande, el.data_fine_invio_domande, el['jconon_call_procedura_comparativa:data_fine_proroga']);
        customButtons.attachments = function () {
          ApplicationFp.displayAttachments(el.id);
        };
        if ((isActive || el.data_inizio_invio_domande === null) && (!el['jconon_call:pubblicato'] || common.User.admin)) {
          customButtons.edit = function () {
            window.location = jconon.URL.call.manage + '?call-type=' + el.objectTypeId + '&cmis:objectId=' + el.id;
          };
          customButtons.remove = function () {
            Call.remove(el.codice, el.id, el.objectTypeId, function () {
              filter(bulkInfo, search);
            });
          };          
        } else {
          customButtons.edit = false;
          customButtons.remove = false;
        }
        if (common.User.admin) {
          customButtons.permissions = function () {
            Ace.panel(el['alfcmis:nodeRef'] || el['cmis:objectId'], el.name, null, false);
          };
        } else {
          customButtons.permissions = false;
        }
        if (el.data_inizio_invio_domande && (!el['jconon_call_procedura_comparativa:pubblicato_esito'] || common.User.admin)) {
          customButtons.prorogaTermini = function () {
            var content = $("<div></div>"),
              bulkinfo = new BulkInfo({
                target: content,
                path: "D:jconon_attachment:call_fp_procedura_comparativa_proroga",
                formclass: 'form-inline jconon',
                name: 'default'
              }),
              container = $('<div class="fileupload fileupload-new" data-provides="fileupload"></div>'),
              input = $('<div class="input-append"></div>'),
              btn = $('<span class="btn btn-file btn-primary"></span>'),
              inputFile = $('<input type="file" name="prorogatermini"/>'),
              btnPrimary,
              modal;

            btn
              .append('<span class="fileupload-new"><i class="icon-upload"></i> Carica file</span>')
              .append('<span class="fileupload-exists">Cambia</span>')
              .append(inputFile);

            input
              .append('<div class="uneditable-input input-xlarge"><i class="icon-file fileupload-exists"></i><span class="fileupload-preview"></span></div>')
              .append(btn)
              .appendTo(container);

            content.append(container);
            container.before("<hr>");  
            // set widget 'value'
            function setValue(value) {
              container.data('value', value);
            }

            setValue(null);
            input.append('<a href="#" class="btn fileupload-exists" data-dismiss="fileupload">Rimuovi</a>');
            inputFile.on('change', function (e) {
              var path = $(e.target).val();
              setValue(path);
            });

            function sendFile() {
              if (bulkinfo.validate()) {
                var fd = new CNR.FormData();                
                fd.data.append("objectId", el['cmis:objectId']);                
                $.each(inputFile[0].files || [], function (i, file) {
                  fd.data.append('prorogatermini', file);
                });
                $.each(bulkinfo.getData(), function (index, item) {
                  fd.data.append(item.name, item.value || '');
                });
                var close = UI.progress();
                $.ajax({
                    type: "POST",
                    url: cache.baseUrl + "/rest/call-fp/carica-proroga",
                    data:  fd.getData(),
                    enctype: fd.contentType,
                    processData: false,
                    contentType: false,
                    dataType: "json",
                    success: function(data){
                      UI.success(i18n.prop('message.allegato.proroga.done'), function () {
                        $('#resetFilter').click();
                      });
                    },
                    complete: close,
                    error: URL.errorFn
                });                
              } else {
                return false;
              } 
            }
            bulkinfo.render();
            UI.modal('<i class="icon-time"></i> ' + i18n.prop('actions.prorogaTermini'), content, sendFile);              
          };
          customButtons.altriDocumenti = function () {
            var applicationAttachments = [{
              "key": "D:jconon_attachment:call_fp_altri_documenti",
              "label": "D:jconon_attachment:call_fp_altri_documenti",
              "description": "Altri documenti",
              "defaultLabel": "Altri documenti",
              "id": "D:jconon_attachment:call_fp_altri_documenti"
            }], content = $("<div></div>").addClass('modal-inner-fix'), 
            bigModal,               
            attachment = new Attachments({
              isSaved: true,
              affix: content,
              objectTypes: applicationAttachments,
              cmisObjectId: el['cmis:objectId'],
              search: {
                type: 'jconon_attachment:call_fp_altri_documenti',
                displayRow: function (el, refreshFn) {
                  return jconon.defaultDisplayDocument(el, refreshFn, false);
                },
                fetchCmisObject: true,
                calculateTotalNumItems: true,
                maxItems: 10,
                filter: false
              }
            });
            attachment();
            bigModal = UI.bigmodal('<i class="icon-upload-alt"></i> Altri Documenti', content);            
          };
        } else {
          customButtons.prorogaTermini = false;
          customButtons.altriDocumenti = false;  
        }

        if (!isActive && el.data_inizio_invio_domande && (!el['jconon_call_procedura_comparativa:pubblicato_esito'] || common.User.admin)) {
          customButtons.esitoSelezione = function () {
            window.location = 'esito-call?call-type=' + el.objectTypeId + '&cmis:objectId=' + el.id;
          };          
        } else {
          customButtons.esitoSelezione = false;  
        }
        azioni = new ActionButton.actionButton({
          name: el.name,
          nodeRef: el.id,
          baseTypeId: el.baseTypeId,
          objectTypeId: el.objectTypeId,
          mimeType: el.contentType,
          allowableActions: el.allowableActions,
          defaultChoice: isMacroCall ? 'detail' : 'application'
        }, {esitoSelezione: 'CAN_CREATE_DOCUMENT', prorogaTermini: 'CAN_CREATE_DOCUMENT', altriDocumenti : 'CAN_CREATE_DOCUMENT'},
          customButtons, {
            attachments : 'icon-download-alt',
            esitoSelezione : 'icon-share-alt',
            prorogaTermini : 'icon-time',
            altriDocumenti : 'icon-upload-alt'
          }, undefined, true);
        row = $(rows.get(index));
        azioni.appendTo(row.find('td:last'));
      });
    });
  }

  function changeActiveState(btn) {
    btn.parents('ul').find('.active').removeClass('active');
    btn.parent('li').addClass('active');
  }

  bulkInfo = new BulkInfo({
    target: $('#criteria-call'),
    formclass: 'form-inline jconon',
    path: rootTypeId,
    name: 'all-filters',
    callback : {
      beforeCreateElement: function (item) {
        if (item.name === 'filters-codice') {
          item.val = params.query;
        } else if (item.name === 'call-type') {
          item.jsonlist = callTypes;
        }
        if (params[item.name]) {
          item.val = params[item.name];
        }
      },
      afterCreateForm: function (form) {
        form.keypress(function (e) {
          if (e.which == 13) {
            $('#applyFilter').click();
            return false;    //<---- Add this line
          }
        });
        if (isGestore()) {
          $('#createNew').removeClass('hide');
        }
        manageFilterClick();
        displayCall(rootTypeId, rootQueryTypeId);
      }
    }
  });
  bulkInfo.render();
});