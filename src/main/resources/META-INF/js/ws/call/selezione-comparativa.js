/*global params*/
define(['jquery', 'i18n', 'header', 'cnr/cnr.actionbutton', 'cnr/cnr.search',
  'cnr/cnr.bulkinfo', 'cnr/cnr.ui',
  'json!common', 'cnr/cnr.jconon', 'cnr/cnr.url', 'cnr/cnr.call', 'cnr/cnr.ace', 'json!cache'
  ], function ($, i18n, header, ActionButton, Search, BulkInfo, UI, common, jconon, URL, Call, Ace, cache) {
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
  function manageFilterClick() {
    $('#applyFilter').on('click', function () {
      Call.filter(bulkInfo, search);
    });
    $('#filters-attivi_scaduti').closest('.widget').on('setData', function (event, key, value) {
      Call.filter(bulkInfo, search, null, null, null, value);
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
        Call.filter(bulkInfo, search);
      }
    });
  }

  function displayRow(bulkInfo, search, typeId, rootTypeId, resultSet, target) {
    var xhr = new BulkInfo({
      target: $('<tbody>').appendTo(target),
      handlebarsId: 'call-main-results',
      path: typeId,
      metadata: resultSet,
      handlebarsSettings: {
        call_type: typeId === rootTypeId ? true : false
      }
    }).handlebars();

    xhr.done(function () {
      target.off('click').on('click', '.requirements', function (e) {
        var data = $("<div></div>").addClass('modal-inner-fix').html($(this).data('content'));
        UI.modal('<i class="icon-info-sign text-info animated flash"></i> ' + i18n['label.th.jconon_bando_elenco_titoli_studio'], data);
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
          azioni;
        customButtons.attachments = function () {
          Call.displayAttachments(el.id);
        };
        customButtons.edit = function () {
          window.location = jconon.URL.call.manage + '?call-type=' + el.objectTypeId + '&cmis:objectId=' + el.id;
        };
        customButtons.remove = function () {
          Call.remove(el.codice, el.id, el.objectTypeId, function () {
            filter(bulkInfo, search);
          });
        };
        if (common.User.admin) {
          customButtons.permissions = function () {
            Ace.panel(el['alfcmis:nodeRef'] || el['cmis:objectId'], el.name, null, false);
          };
        } else {
          customButtons.permissions = false;
        }
        customButtons.publish = function (that) {
          var published = el['jconon_call:pubblicato'],
            message = published ? i18n['message.action.unpublish'] : i18n['message.action.publish'];
          UI.confirm(message, function () {
            Call.publish([
              {name: 'cmis:objectId', value: el.id},
              {name: 'cmis:objectTypeId', value: el.objectTypeId},
              {name: 'skip:save', value: true}
            ], !published, function (pubblicato, removeClass, addClass, title) {
              el['jconon_call:pubblicato'] = pubblicato;
              that.find('i')
                .removeClass(removeClass)
                .addClass(addClass);
            });
          });
        };
        azioni = new ActionButton.actionButton({
          name: el.name,
          nodeRef: el.id,
          baseTypeId: el.baseTypeId,
          objectTypeId: el.objectTypeId,
          mimeType: el.contentType,
          allowableActions: el.allowableActions,
          defaultChoice: isMacroCall ? 'detail' : 'application'
        }, {publish: 'CAN_APPLY_ACL'},
          customButtons, {
            attachments : 'icon-download-alt',
            publish: el['jconon_call:pubblicato'] ? 'icon-eye-close' : 'icon-eye-open',
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
    target: $('#criteria'),
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
        manageFilterClick();
        displayCall(rootTypeId, rootQueryTypeId);
      }
    }
  });
  bulkInfo.render();
});