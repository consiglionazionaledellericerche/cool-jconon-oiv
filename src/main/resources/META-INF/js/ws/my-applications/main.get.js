define(['jquery', 'header', 'json!common', 'cnr/cnr.bulkinfo', 'cnr/cnr.search', 'cnr/cnr.url', 'i18n', 
  'cnr/cnr.ui', 'cnr/cnr.actionbutton', 'cnr/cnr.jconon', 'handlebars', 'cnr/cnr', 
  'moment', 'cnr/cnr.application', 'cnr/cnr.criteria', 'cnr/cnr.ace', 'cnr/cnr.call', 
  'cnr/cnr.node', 'json!cache', 'fp/fp.application','cnr/cnr.ui.widgets', 'cnr/cnr.ui.wysiwyg'],
  function ($, header, common, BulkInfo, Search, URL, i18n, 
    UI, ActionButton, jconon, Handlebars, CNR, 
    moment, Application, Criteria, Ace, Call, 
    Node, cache, ApplicationFp, Widgets, Wysiwyg) {
  "use strict";
  Widgets['ui.wysiwyg'] = Wysiwyg;
  var search,
    rootTypeId = 'F:jconon_application:folder',
    typeId = 'F:jconon_call:folder',
    myType = 'jconon_application:folder',
    elements = {
      table: $('#items'),
      pagination: $('#itemsPagination'),
      orderBy: $('#orderBy'),
      label: $('#emptyResultset'),
      total: $('#total')
    },
    bulkInfo,
    criteria = $('#criteria'),
    callId = URL.querystring.from['cmis:objectId'],
    callCodice = URL.querystring.from['callCodice'];


  function displayAttachments(el, type, displayFn, i18nModal) {
    var content = $('<div></div>').addClass('modal-inner-fix');
    jconon.findAllegati(el.id, content, type, true, displayFn, true, el);
    UI.modal(i18n[i18nModal || 'actions.attachments'], content, undefined, undefined, true);
  }

  function manageUrlParams() {
    if (callId) {
      URL.Data.node.node({
        data: {
          nodeRef : callId
        },
        success: function (data) {
          $('#items caption h2').text('DOMANDE RELATIVE AL BANDO ' + data['jconon_call:codice'] + ' - ' + data['jconon_call:sede']);
          bulkInfo.render();
        },
        error: function (jqXHR, textStatus, errorThrown) {
          CNR.log(jqXHR, textStatus, errorThrown);
        }
      });
    } else if (callCodice) {
      URL.Data.search.query({
        data: {
          q: "select cmis:objectId " +
            "from jconon_call:folder where jconon_call:codice = '" + callCodice + "'"
        },
        success: function (data) {
          callId = data.items[0]['cmis:objectId'];
          $('#items caption h2').text('DOMANDE RELATIVE AL BANDO ' + callCodice);
          bulkInfo.render();
          $('#items caption h2').after($('<div class="btn-group pull-right">').append(
              $('<a id="export-elenco" class="btn btn-success"><i class="icon-table"></i> Esporta Elenco</a>')).append(
              $('<a id="export-xls" class="btn btn-primary"><i class="icon-table"></i> Esporta dati in Excel</a>')));
        },
        error: function (jqXHR, textStatus, errorThrown) {
          CNR.log(jqXHR, textStatus, errorThrown);
        }
      });
    } else {
      bulkInfo.render();
    }
  }

  // filter ajax resultSet according to the Criteria form
  function filterFn(data) {

    var filtered = $.grep(data.items, function (el) {
      var callCode = bulkInfo.getDataValueById('filters-codice'),
        callFromDate = bulkInfo.getDataValueById('filters-da_data'),
        callToDate = bulkInfo.getDataValueById('filters-a_data'),
        callStatus = callId ? 'tutti' : bulkInfo.getDataValueById('filters-attivi_scaduti'),
        call = el.relationships.parent ? el.relationships.parent[0] : {},
        now = new Date(common.now),
        isActive = call['jconon_call:data_fine_invio_domande'] === "" ||
          (new Date(call['jconon_call:data_inizio_invio_domande']) < now && new Date(call['jconon_call:data_fine_invio_domande']) > now);

      if (callCode) {
        if (!new RegExp(callCode, "gi").test(call['jconon_call:codice'])) {
          return false;
        }
      }

      if (callFromDate) {
        if (new Date(call['jconon_call:data_fine_invio_domande']) < new Date(callFromDate)) {
          return false;
        }
      }

      if (callToDate) {
        if (new Date(call['jconon_call:data_fine_invio_domande']) > new Date(callToDate)) {
          return false;
        }
      }

      if (callStatus) {
        if (callStatus === 'attivi' && !isActive) {
          return false;
        }
        if (callStatus === 'scaduti' && isActive) {
          return false;
        }
      }
      return true;
    });
    data.items = filtered;
    data.totalNumItems = filtered.length;
    data.hasMoreItems = filtered.length > data.maxItemsPerPage;

    return data;
  }

  function filter() {
    search.execute();
  }

  function allegaDocumentoAllaDomanda(type, objectId, successCallback, bigmodal, callbackModal) {
    return Node.submission({
      nodeRef: objectId,
      objectType: type,
      crudStatus: "INSERT",
      requiresFile: true,
      bigmodal: bigmodal,
      callbackModal: callbackModal,
      showFile: true,
      externalData: [
        {
          name: 'aspect',
          value: 'P:jconon_attachment:generic_document'
        },
        {
          name: 'aspect',
          value: 'P:jconon_attachment:document_from_rdp'
        },
        {
          name: 'jconon_attachment:user',
          value: common.User.id
        }
      ],
      modalTitle: i18n[type],
      success: function (attachmentsData, data) {
        if (successCallback) {
          successCallback(attachmentsData, data);
        } else {
          $('#applyFilter').click();
        }
      },
      forbidArchives: true
    });
  }
  Handlebars.registerHelper('applicationStatus', function declare(code, dataInvioDomanda, dataUltimaModifica, dataScadenza) {
    var dateFormat = "DD/MM/YYYY HH:mm:ss",
      isTemp = (code === 'P' || code === 'I'),
      msg = i18n['label.application.stato.' + (code === 'I' ? 'P' : code)],
      item = $('<label class="label"></label>')
        .addClass(isTemp ? 'label-info' : 'label-success')
        .addClass(dataScadenza !== "" && (moment().diff(dataScadenza, 'days') > -7) ? 'animated flash' : '')
        .append(msg)
        .append(isTemp ? (' - ultima modifica ' + CNR.Date.format(dataUltimaModifica, '-', dateFormat)) : (' il ' + CNR.Date.format(dataInvioDomanda, '-', dateFormat)));
    return $('<div>').append(item).html();
  });

  Handlebars.registerHelper('iscrizioneElenco', function declare(numero, data, fascia_professionale_validata, fascia_professionale_attribuita) {
    var dateFormat = "DD/MM/YYYY",
      fascia = fascia_professionale_validata || fascia_professionale_attribuita,
      item = $('<label class="label label-info"></label>')
        .append('Iscritto in Elenco il ' + CNR.Date.format(data, '-', dateFormat) + 
          ' con progressivo n° '+ numero + 
          ' e fascia: ' + fascia);
    return $('<div>').append(item).html();
  });

  Handlebars.registerHelper('esclusioneRinuncia', function esclusioneRinunciaFn(esclusioneRinuncia, statoDomanda, rimossoElenco, dataRimozione) {

    var a, testo = rimossoElenco === true ? "Cancellato dall'Elenco in data " : "Escluso dall'Elenco in data ";
    testo += CNR.Date.format(dataRimozione, "-", "DD/MM/YYYY");
    if (esclusioneRinuncia) {
        a = $('<span class="label label-important animated flash"></span>').append(testo);
    }
    return $('<div>').append(a).html();
  });

  Handlebars.registerHelper('scadenza', function scadenza(date) {
    var isExpired = CNR.Date.isPast(new Date(date)),
      a = $('<span>' + i18n[isExpired ? "label.th.jconon_bando_data_scadenza_expired" : "label.th.jconon_bando_data_scadenza"] + '</span>')
        .append(' ')
        .addClass(isExpired ? 'text-error' : '')
        .append(CNR.Date.format(date, "-", "DD/MM/YYYY HH:mm:ss"));
    return $('<div>').append(a).html();
  });

  Handlebars.registerHelper('ifIn', function(elem, list, isRdp, options) {
    if(list.indexOf(elem) > -1 && isRdp) {
      return options.fn(this);
    }
    return options.inverse(this);
  });
  
  search = new Search({
    elements: elements,
    columns: ['cmis:parentId', 'jconon_application:stato_domanda', 'jconon_application:nome', 'jconon_application:cognome', 'jconon_application:data_domanda', 'jconon_application:codice_fiscale', 'jconon_application:data_nascita', 'jconon_application:esclusione_rinuncia', 'jconon_application:user'],
    fields: {
      'nome': null,
      'data di creazione': null,
      'Cognome': 'jconon_application:cognome',
      'Nome': 'jconon_application:nome',
      'Data domanda': 'jconon_application:data_domanda',
      'Data ultimo salvataggio': 'cmis:lastModificationDate',      
      'Stato domanda': 'jconon_application:stato_domanda',
      'Numero di iscrizione in elenco':  'jconon_application:progressivo_iscrizione_elenco'
    },
    orderBy: {
      field: 'jconon_application:cognome',
      asc: true
    },
    type: myType,
    maxItems: callId||callCodice ? undefined : 100,
    dataSource: function (page, settings, getUrlParams) {
      var deferred,
        baseCriteria = new Criteria().not(new Criteria().equals('jconon_application:stato_domanda', 'I').build()),
        criteria = new Criteria(),
        applicationStatus = bulkInfo.getDataValueById('filters-provvisorie_inviate'),
        user = bulkInfo.getDataValueById('user'),
        url;

      if (applicationStatus && applicationStatus !== 'tutte' && applicationStatus !== 'attive' && applicationStatus !== 'escluse') {
        baseCriteria.and(new Criteria().equals('jconon_application:stato_domanda', applicationStatus).build());
      }

      if (applicationStatus && applicationStatus === 'attive') {
        baseCriteria.and(new Criteria().isNotNull('jconon_application:progressivo_iscrizione_elenco').build());
      }

      if (applicationStatus && applicationStatus === 'escluse') {
        baseCriteria.and(new Criteria().equals('jconon_application:stato_domanda', 'C').build());
        baseCriteria.and(new Criteria().isNotNull('jconon_application:esclusione_rinuncia').build());
      }

      if (callId) {
        criteria.inTree(callId);
        if (user) {
          criteria.and(new Criteria().equals('jconon_application:user', user).build());
        }
      } else {
        criteria.equals('jconon_application:user', common.User.id);
      }
      settings.lastCriteria = criteria.and(baseCriteria.build()).build();

      $('#export-xls').off('click').on('click', function () {
        var close = UI.progress();
        jconon.Data.call.applications_single_call({
          type: 'GET',
          data:  getUrlParams(page),
          success: function (data) {
            var url = URL.template(jconon.URL.call.downloadXLS, {
              objectId: data.objectId,
              fileName: data.fileName,
              exportData: true,
              mimeType: 'application/vnd.ms-excel;charset=UTF-8'
            });     
            window.location = url;
          },
          complete: close,
          error: URL.errorFn
        });
      });
      $('#export-elenco').off('click').on('click', function () {
        var close = UI.progress(), data = getUrlParams(page);
        data.callId = callId;
        $.ajax({
          url: cache.baseUrl + "/rest/application-fp/applications-elenco.xls",
          type: 'GET',
          data:  data,
          success: function (data) {
            var url = URL.template(jconon.URL.call.downloadXLS, {
              objectId: data.objectId,
              fileName: data.fileName,
              exportData: true,
              mimeType: 'application/vnd.ms-excel;charset=UTF-8'
            });     
            window.location = url;
          },
          complete: close,
          error: URL.errorFn
        });
      });

      deferred = URL.Data.search.query({
        cache: false,
        queue: true,
        data: $.extend({}, getUrlParams(page), {
          fetchCmisObject: true,
          relationship: 'parent'
        })
      });

      deferred.done(function (data) {
        if (elements.total) {
          elements.total.text(data.totalNumItems + ' elementi trovati in totale');
        }
      });

      if (!callId) {
        deferred = deferred.pipe(filterFn);
      }

      return deferred;
    },
    display: {
      resultSet: function (resultSet, target) {
        var xhr = new BulkInfo({
          target: $('<tbody>').appendTo(target),
          handlebarsId: 'application-main-results',
          path: typeId,
          metadata: resultSet,
          handlebarsSettings: {
            call_type: typeId === rootTypeId ? true : false,
            callId: callId,
            isRdP: (Call.isRdP(resultSet[0].relationships.parent[0]['jconon_call:rdp']) || common.User.admin)
          }
        }).handlebars();

        xhr.done(function () {

          target
            .off('click')
            .on('click', '.requirements', function () {
              var data = $("<div></div>").addClass('modal-inner-fix').html($(this).data('content'));
              UI.modal('<i class="icon-info-sign text-info animated flash"></i> ' + i18n['label.th.jconon_bando_elenco_titoli_studio'], data);
            })
            .on('click', '.annotazione', function () {
              var data = $("<div></div>").addClass('modal-inner-fix').html($(this).data('content'));
              UI.modal('<i class="icon-pencil text-info animated flash"></i> Annotazioni', data);
              return false;
            })
            .on('click', '.code', function () {
              var data = $("<div></div>").addClass('modal-inner-fix').html($(this).data('content'));
              UI.modal('<i class="icon-info-sign text-info animated flash"></i> ' + i18n['label.call'], data);
            })
            .on('click', '.user', function (event) {
              var authority = $(event.target).attr('data-user');
              Ace.showMetadata(authority);
            });

          var rows = target.find('tbody tr');
          $.each(resultSet, function (index, el) {
            var target = $(rows.get(index)).find('td:last'),
              callData = el.relationships.parent[0],
              callAllowableActions = callData.allowableActions,
              dropdowns = {},
              bandoInCorso = (callData['jconon_call:data_fine_invio_domande'] === "" ||
                new Date(callData['jconon_call:data_fine_invio_domande']) > new Date(common.now)),
              displayActionButton = true,
              defaultChoice,
              customButtons = {
                select: false,
                permissions: false,
                remove: false,
                copy: false,
                cut: false,
                print: function () {
                  Application.print(el.id, el['jconon_application:stato_domanda'], bandoInCorso, el['jconon_application:data_domanda']);
                }
              };

            if (callData['jconon_call:elenco_sezioni_domanda'].indexOf('affix_tabTitoli') >= 0) {
              customButtons.attachments = function () {
                displayAttachments(el, 'jconon_attachment:generic_document', Application.displayTitoli);
              };
            }
            if (callData['jconon_call:elenco_sezioni_domanda'].indexOf('affix_tabCurriculum') >= 0) {
              customButtons.curriculum = function () {
                //Curriculum
                displayAttachments(el, 'jconon_attachment:cv_element', Application.displayCurriculum, 'actions.curriculum');
              };
            }
            if (callData['jconon_call:elenco_sezioni_domanda'].indexOf('affix_tabSchedaAnonima') >= 0) {
              customButtons.schedaAnonima = function () {
                //Scheda Anonima
                displayAttachments(el, 'jconon_scheda_anonima:document', ApplicationFp.displayEsperienzeOIV, 'actions.schedaAnonima');
              };
            }
            if (callData['jconon_call:elenco_sezioni_domanda'].indexOf('affix_tabElencoProdotti') >= 0) {
              customButtons.productList = function () {
                //Elenco Prodotti
                displayAttachments(el, 'cvpeople:noSelectedProduct', Application.displayProdotti, 'actions.productList');
              };
            }
            if (callData['jconon_call:elenco_sezioni_domanda'].indexOf('affix_tabProdottiScelti') >= 0) {
              customButtons.productSelected = function () {
                //Prodotti Scelti
                displayAttachments(el, 'cvpeople:selectedProduct', Application.displayProdottiScelti, 'actions.productSelected');
              };
            }
            //  Modifica
            customButtons.edit = function () {
              window.location = jconon.URL.application.manage + '?callId=' + callData['cmis:objectId'] + '&applicationId=' + el['cmis:objectId'];
            };
            if (common.User.admin || Call.isRdP(callData['jconon_call:rdp'])) {
              customButtons.assegna_fascia = function () {
                var content = $("<div></div>"),
                  bulkinfo = new BulkInfo({
                  target: content,
                  path: "P:jconon_application:aspect_fascia_professionale_attribuita",
                  objectId: el['cmis:objectId'],
                  formclass: 'form-inline',
                  name: 'default',
                  callback : {
                    afterCreateForm: function (form) {
                      form.find('#button_fascia_professionale_esegui_calcolo').off('click').on('click', function () {
                        $.ajax({
                          url: cache.baseUrl + "/rest/application-fp/applications-ricalcola-fascia",
                          type: 'GET',
                          data:  {
                            'applicationId' : el['cmis:objectId']
                          },
                          success: function (data) {
                            form.find('#fascia_professionale_validata').val(data['jconon_application:fascia_professionale_attribuita']);
                            UI.success("La fascia ricalcolata è: " + data['jconon_application:fascia_professionale_attribuita']);
                          },
                          error: URL.errorFn
                        });
                      });
                    }
                  }
                });
                bulkinfo.render();
                UI.modal('<i class="icon-edit"></i> Assegna Fascia', content, function () {
                  var close = UI.progress(), d = bulkinfo.getData();
                  d.push(
                    {
                      id: 'cmis:objectId',
                      name: 'cmis:objectId',
                      value: el['cmis:objectId']
                    },
                    {
                      name: 'aspect', 
                      value: 'P:jconon_application:aspect_fascia_professionale_attribuita'
                    },
                    {
                      name: 'jconon_application:fascia_professionale_esegui_calcolo',
                      value: false
                    }                        
                  );
                  jconon.Data.application.main({
                    type: 'PUT',
                    data: d,
                    success: function (data) {
                      UI.success(i18n['message.aggiornamento.fascia.eseguito']);
                      $('#applyFilter').click();
                    },
                    complete: close,
                    error: URL.errorFn
                  });
                });
              };
            }
            if (el['jconon_application:stato_domanda'] === 'P') {
              // provvisoria
              if (bandoInCorso) {
                if (common.User.admin || common.User.id === el['jconon_application:user']) {
                  defaultChoice = 'edit';
                } else {
                  customButtons.edit = false;
                  defaultChoice = 'print';
                }
              } else {
                //  label Scaduto
                $.each(customButtons, function (index, el) {
                  if (index !== "print" && index !== "duplicate") {
                    customButtons[index] = false;
                  }
                });
                customButtons.edit = false;
                displayActionButton = true;
              }
            } else if (el['jconon_application:stato_domanda'] === 'C') {
              // definitiva è editbile per ora solo da amministratori, poi sarà il RDP
              if (!common.User.admin) {
                customButtons.edit = false;
              }
              defaultChoice = 'print';

              if (bandoInCorso) {
                if (common.User.admin || common.User.id === el['jconon_application:user']) {
                  if (el.allowableActions.indexOf('CAN_CREATE_DOCUMENT') !== -1) {
                    customButtons.reopen = false;
                    customButtons.modificaProfilo = function () {
                      window.location = jconon.URL.application.manage + '?callId=' + el.relationships.parent[0]['cmis:objectId'] + '&applicationId=' + el['cmis:objectId'];                      
                    };
                  } else {
                    customButtons.reopen = function () {
                      Application.reopen(el, function () {
                        window.location = jconon.URL.application.manage + '?callId=' + el.relationships.parent[0]['cmis:objectId'] + '&applicationId=' + el['cmis:objectId'];
                      });
                    };
                    customButtons.modificaProfilo = false;
                  }
                }
                if (common.User.admin || Call.isRdP(callData['jconon_call:rdp'])) {
                  if (el['jconon_application:esclusione_rinuncia'] !== 'E') {
                    customButtons.escludi = function () {
                      var bulkInfoAllegato = allegaDocumentoAllaDomanda('D_jconon_esclusione_attachment_fp',
                        el['cmis:objectId'],
                        function (attachmentsData, data) {
                          jconon.Data.application.reject({
                            type: 'POST',
                            data: {
                              nodeRef : el['cmis:objectId'],
                              nodeRefDocumento : data['alfcmis:nodeRef']
                            },
                            success: function () {
                              $('#applyFilter').click();
                            },
                            error: jconon.error
                          });
                        }, true, function (modal) {
                            $(window).on('shown.bs.modal', function (event) {
                                var nome = el['jconon_application:nome'].replace(/^(.)|(\s|\-)(.)/g, function($word) {
                                    return $word.toUpperCase();
                                }), cognome = el['jconon_application:cognome'].replace(/^(.)|(\s|\-)(.)/g, function($word) {
                                    return $word.toUpperCase();
                                });
                                modal.find('#oggetto_notifica_email').val(i18n['app.name'] + ' - ' + i18n['mail.subject.esclusione']);
                                var testo = '<p>' + i18n['mail.confirm.application.1'];
                                    testo += el['jconon_application:sesso'] === 'M' ? ' dott.' : ' dott.ssa';
                                    testo += ' <b style="text-transform: capitalize;">' + nome + ' ' + cognome + '</b>, </p>';
                                    testo += '<p>' + i18n['mail.esclusione.application.1'] + '</p>';
                                    testo += '<p>' + i18n['mail.iscrizione.application.6'] + '</p><br>';
                                    testo += callData['jconon_call:requisiti'];

                                var textarea = modal.find('#testo_notifica_email');
                                textarea.val(testo);
                                var ck = textarea.ckeditor({
                                    toolbarGroups: [
                                        { name: 'clipboard', groups: ['clipboard'] },
                                        { name: 'basicstyles', groups: ['basicstyles'] },
                                        { name: 'paragraph', groups: ['list', 'align'] }],
                                        removePlugins: 'elementspath'
                                });
                                ck.editor.on('change', function () {
                                  var html = ck.val();
                                  textarea.parent().find('control-group widget').data('value', html || null);
                                });

                                ck.editor.on('setData', function (event) {
                                  var html = event.data.dataValue;
                                  textarea.parent().find('control-group widget').data('value', html || null);
                                });
                            });
                        }
                      );
                    };
                    if (el['jconon_application:progressivo_iscrizione_elenco'] == '') {
                      customButtons.inserisci = function () {
                        UI.confirm(i18n.prop('message.confirm.iscrizione.elenco', el['jconon_application:nome'], el['jconon_application:cognome']), function () {
                          var close = UI.progress();
                          jconon.Data.application.readmission({
                            type: 'POST',
                            data: {
                              nodeRef : el['cmis:objectId']
                            },
                            success: function () {
                              URL.Data.proxy.childrenGroup({
                                type: 'POST',
                                data: JSON.stringify({
                                  'parent_group_name': 'GROUP_ELENCO_OIV',
                                  'child_name': el['jconon_application:user']
                                }),
                                contentType: 'application/json'
                              });
                              UI.success('Iscrizione avvenuta correttamente.');
                              $('#applyFilter').click();
                            },
                            complete: close,
                            error: jconon.error
                          });                      
                        });  
                      }
                    }
                  }
                }                
              } else {
                if (el['jconon_application:esclusione_rinuncia'] !== 'E' && 
                    el['jconon_application:esclusione_rinuncia'] !== 'N' && 
                    el['jconon_application:esclusione_rinuncia'] !== 'R') {
                  dropdowns['<i class="icon-arrow-down"></i> Escludi'] = function () {
                    allegaDocumentoAllaDomanda('D:jconon_esclusione:attachment',
                      el['cmis:objectId'],
                      function () {
                        jconon.Data.application.reject({
                          type: 'POST',
                          data: {
                            nodeRef : el['cmis:objectId']
                          },
                          success: function () {
                            $('#applyFilter').click();
                          },
                          error: jconon.error
                        });
                      }
                      );
                  };
                }
                if (el['jconon_application:esclusione_rinuncia'] === 'E' ||
                    el['jconon_application:esclusione_rinuncia'] === 'N' ||
                    el['jconon_application:esclusione_rinuncia'] === 'R') {
                  dropdowns['<i class="icon-arrow-up"></i> Riammetti'] = function () {
                    allegaDocumentoAllaDomanda('D:jconon_riammissione:attachment',
                      el['cmis:objectId'],
                      function () {
                        jconon.Data.application.readmission({
                          type: 'POST',
                          data: {
                            nodeRef : el['cmis:objectId']
                          },
                          success: function () {
                            $('#applyFilter').click();
                          },
                          error: jconon.error
                        });
                      }
                      );
                  };
                }
                if (el['jconon_application:esclusione_rinuncia'] !== 'E' &&
                    el['jconon_application:esclusione_rinuncia'] !== 'N' &&
                    el['jconon_application:esclusione_rinuncia'] !== 'R') {
                  dropdowns['<i class="icon-arrow-down"></i> Rinuncia'] = function () {
                    allegaDocumentoAllaDomanda('D:jconon_rinuncia:attachment',
                      el['cmis:objectId'],
                      function () {
                        jconon.Data.application.waiver({
                          type: 'POST',
                          data: {
                            nodeRef : el['cmis:objectId']
                          },
                          success: function () {
                            $('#applyFilter').click();
                          },
                          error: jconon.error
                        });
                      }
                      );
                  };
                }
                dropdowns['<i class="icon-upload"></i> Comunicazione al candidato'] = function () {
                  allegaDocumentoAllaDomanda('D:jconon_comunicazione:attachment', el['cmis:objectId']);
                };
                dropdowns['<i class="icon-upload"></i> Convocazione al colloquio'] = function () {
                  allegaDocumentoAllaDomanda('D:jconon_convocazione:attachment', el['cmis:objectId']);
                };
                dropdowns['<i class="icon-pencil"></i> Reperibilità'] = function () {
                  var content = $("<div></div>").addClass('modal-inner-fix'),
                    bulkinfo = new BulkInfo({
                    target: content,
                    path: "F:jconon_application:folder",
                    objectId: el['cmis:objectId'],
                    formclass: 'form-horizontal jconon',
                    name: 'reperibilita'
                  });
                  bulkinfo.render();
                  UI.bigmodal('<i class="icon-pencil"></i> Reperibilità', content, function () {
                    var close = UI.progress(), d = bulkinfo.getData();
                    d.push({
                        id: 'cmis:objectId',
                        name: 'cmis:objectId',
                        value: el['cmis:objectId']
                    });
                    jconon.Data.application.main({
                      type: 'POST',
                      data: d,
                      success: function (data) {
                        UI.success(i18n['message.aggiornamento.application.reperibilita']);
                      },
                      complete: close,
                      error: URL.errorFn
                    });
                  });
                };
                dropdowns['<i class="icon-edit"></i> Punteggi'] = function () {
                  var content = $("<div></div>").addClass('modal-inner-fix'),
                    bulkinfo = new BulkInfo({
                    target: content,
                    path: "P:jconon_application:aspect_punteggi",
                    objectId: el['cmis:objectId'],
                    formclass: 'form-horizontal',
                    name: 'default',
                    callback : {
                      afterCreateForm: function (form) {
                        form.find('.control-group').not('.widget').addClass('widget');
                      }
                    }
                  });
                  bulkinfo.render();
                  UI.bigmodal('<i class="icon-edit"></i> Punteggi', content, function () {
                    var close = UI.progress(), d = bulkinfo.getData();
                    d.push(
                      {
                        id: 'cmis:objectId',
                        name: 'cmis:objectId',
                        value: el['cmis:objectId']
                      },
                      {
                        name: 'aspect', 
                        value: 'P:jconon_application:aspect_punteggi'
                      }
                    );
                    jconon.Data.application.main({
                      type: 'POST',
                      data: d,
                      success: function (data) {
                        UI.success(i18n['message.aggiornamento.application.punteggi']);
                      },
                      complete: close,
                      error: URL.errorFn
                    });
                  });
                };
                if (common.User.admin || Call.isRdP(callData['jconon_call:rdp'])) {
                  customButtons.operations = dropdowns;
                }
                if (callData['jconon_call:scheda_valutazione'] === true && (common.User.admin || Call.isCommissario(callData['jconon_call:commissione']))) {
                  customButtons.scheda_valutazione = function () {
                    URL.Data.search.query({
                      queue: true,
                      data: {
                        q: "select cmis:versionSeriesId from jconon_attachment:scheda_valutazione where IN_FOLDER ('" + el['cmis:objectId'] + "')"
                      }
                    }).done(function (rs) {
                      if (rs.totalNumItems === 0 || rs.items[0] === undefined) {
                        UI.confirm('Non &egrave; presente nessuna scheda di valutazione del candidato. Si vuole procedere con la sua predisposizione?', function () {
                          var close = UI.progress();
                          jconon.Data.application.print_scheda_valutazione({
                            type: 'POST',
                            data: {
                              nodeRef : el['cmis:objectId']
                            },
                            success: function (data) {
                              window.location = URL.urls.search.content + '?nodeRef=' + data.nodeRef;
                            },
                            complete: close,
                            error: jconon.error
                          });
                        });
                      } else {
                        window.location = jconon.URL.application.scheda_valutazione + '?applicationId=' + el['cmis:objectId'] + '&nodeRef=' + rs.items[0]['cmis:versionSeriesId'];
                      }
                    });
                  };
                } else {
                  customButtons.scheda_valutazione = false;
                }
              }
            }
            if (displayActionButton) {
              new ActionButton.actionButton({
                name: el.name,
                nodeRef: el.id,
                baseTypeId: el.baseTypeId,
                objectTypeId: el.objectTypeId,
                mimeType: el.contentType,
                allowableActions: el.allowableActions,
                defaultChoice: defaultChoice
              }, {
                edit: 'CAN_CREATE_DOCUMENT',
                scheda_valutazione: 'CAN_CREATE_DOCUMENT',
                operations: 'CAN_CREATE_DOCUMENT'
              }, customButtons, {
                print: 'icon-print',
                attachments : 'icon-download-alt',
                curriculum: 'icon-file-alt',
                schedaAnonima: 'icon-file-alt',
                productList: 'icon-list',
                productSelected: 'icon-list-ol',
                reopen: 'icon-share',
                modificaProfilo : 'icon-share',
                scheda_valutazione: 'icon-table',
                operations: 'icon-list',
                escludi: 'icon-arrow-down',
                inserisci: 'icon-arrow-up',
                assegna_fascia: 'icon-edit'
              }, undefined, true).appendTo(target);
            }
          });
        });
      }
    }
  });

  bulkInfo =  new BulkInfo({
    target: criteria,
    formclass: 'form-horizontal jconon',
    path: rootTypeId,
    name: 'filters',
    callback : {
      afterCreateForm: function () {
        // rearrange btn-group as btn-group-vertical
        $('#filters-attivi_scaduti').
          add('#filters-provvisorie_inviate')
          .addClass('btn-group-vertical');

        criteria.find('input:not("#user")').on('change', filter);

        $('#applyFilter').on('click', filter);

        criteria
          .find('.btn-group-vertical')
          .closest('.widget')
          .on('changeData', filter);

        $('#resetFilter').on('click', function () {
          criteria.find('input').val('');
          criteria.find('.widget').data('value', '');

          var btns = criteria.find('.btn-group-vertical .btn');

          btns
            .removeClass('active');

          criteria
            .find('.btn-group-vertical')
            .find('.default')
            .addClass('active');

        });

        filter();
        if (callId) {
          $('#filters .control-group').hide();
          $('#filters .authority')
            .show()
            .on('changeData', function (event, key, value) {
              if (value) {
                filter();
              }
            });
          $('#filters-provvisorie_inviate').parents('.control-group').show();
        } else {
          $('#filters .authority').hide();
          $('#export-div').remove();
        }
      }
    }
  });

  manageUrlParams();
  
});