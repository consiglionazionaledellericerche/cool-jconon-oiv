/*global params*/
define(['jquery', 'i18n', 'header', 'cnr/cnr.actionbutton', 'cnr/cnr.search',
  'cnr/cnr.bulkinfo', 'cnr/cnr.ui',
  'json!common', 'cnr/cnr.jconon', 'cnr/cnr.url', 'cnr/cnr.call', 'cnr/cnr.ace', 'json!cache', 'fp/fp.application'
  ], function ($, i18n, header, ActionButton, Search, BulkInfo, UI, common, jconon, URL, Call, Ace, cache, ApplicationFp) {
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
    return (common.User.other.amministrazione || common.User.admin || (common.User.groupsArray && common.User.groupsArray.indexOf('GROUP_CONCORSI') !== -1));
  }

  function init(value) {
      var criteria = jconon.getCriteria(bulkInfo, value);
      if (value === 'dapubblicare') {
        criteria.isNull('root.jconon_call:data_inizio_invio_domande');
      }
      criteria.list(search);
  }

  function manageFilterClick() {
    $('#applyFilter').on('click', function () {
      init($('#filters-attivi_scaduti > button.btn').attr('data-value'));
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
        if (isGestore) {
          init('dapubblicare');
        } else {
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
          isActive = Call.isActive(el.data_inizio_invio_domande, el.data_fine_invio_domande);
        customButtons.attachments = function () {
          ApplicationFp.displayAttachments(el.id);
        };
        if ((isActive || el.data_inizio_invio_domande === '') && (!el['jconon_call:pubblicato'] || common.User.admin)) {
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
        }, {esitoSelezione: 'CAN_CREATE_DOCUMENT' },
          customButtons, {
            attachments : 'icon-download-alt',
            esitoSelezione : 'icon-share-alt'
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
        if (isGestore) {
          $('#createNew').removeClass('hide');
        }
        manageFilterClick();
        displayCall(rootTypeId, rootQueryTypeId);
      }
    }
  });
  bulkInfo.render();
});