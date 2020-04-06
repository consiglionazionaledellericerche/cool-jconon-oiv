/*global params*/
define(['jquery', 'header', 'i18n', 'cnr/cnr', 'cnr/cnr.ui', 'cnr/cnr.bulkinfo',
  'cnr/cnr.jconon', 'cnr/cnr.ace', 'cnr/cnr.url', 'cnr/cnr.call', 'cnr/cnr.attachments', 'json!cache', 'cnr/cnr.ui.widgets', 'cnr/cnr.ui.wysiwyg', 'cnr/cnr.ace', 'json!common', 'fp/fp.application'
  ], function ($, header, i18n, CNR, UI, BulkInfo, jconon, ACE, URL, Call, Attachments, cache, Widgets, Wysiwyg, Ace, common, ApplicationFp) {
  "use strict";
  var ul = $('#affix'), content = $('#field'), forms = [], bulkinfo,
    toolbar = $('#toolbar-call'), jsonlistMacroCall = [], metadata = {},
    cmisObjectId = params['cmis:objectId'],
    copyFrom = params['copyFrom'],
    query = 'select this.jconon_call:codice, this.cmis:objectId, this.jconon_call:descrizione' +
    ' from ' + jconon.findCallQueryName(params['call-type']) + ' AS this ' +
    ' JOIN jconon_call:aspect_macro_call AS macro ON this.cmis:objectId = macro.cmis:objectId ' +
    ' order by this.cmis:lastModificationDate DESC', copyEnabled = false;

  Widgets['ui.wysiwyg'] = Wysiwyg;
  $('#copy').prop('disabled', true);
  $.each(common.enableTypeCalls, function (key, elType) {
    if (elType.id === params['call-type']) {
      copyEnabled = true;
    }
  });
  if (cmisObjectId && copyEnabled) {
    $('#copy').prop('disabled', false).removeClass('disabled');
  }

  function createAttachments(affix) {
    return new Attachments({
      affix: affix,
      objectTypes: cache.jsonlistCallAttachments,
      cmisObjectId: cmisObjectId,
      forbidArchives: false,
      maxUploadSize: true,
      search: {
        type: 'jconon_attachment:document',
        filter: false,
        displayRow: function (el, refreshFn) {
          return jconon.defaultDisplayDocument(el, refreshFn, false);
        }
      }
    });
  }


  function changeActiveState(btn) {
    btn.parents('ul').find('.active').removeClass('active');
    btn.parent('li').addClass('active');
  }

  function showGestore() {
    var divGestore = $('#gestore'),
      gestore = metadata['cmis:createdBy'] || common.User.id,
      a = $('<a href="#undefined">' + gestore + '</a>').click(function () {
        Ace.showMetadata(gestore);
      });
    if (gestore) {
      divGestore.append('<i class="icon-user"></i> Gestore: ').append(a);
    }
  }

  $('#delete').click(function () {
    if (cmisObjectId) {
      Call.remove($('#codice').val(), cmisObjectId, params['call-type'], function () {
        window.location.href = jconon.URL.call.manage + '?call-type=' + params['call-type'];
      });
    }
  });

  $('#save').click(function () {
    bulkinfo.resetForm();
    var close = UI.progress();
    jconon.Data.call.main({
      type: 'POST',
      data: bulkinfo.getData(),
      success: function (data) {
        if (!cmisObjectId) {
          cmisObjectId = data['cmis:objectId'];
          bulkinfo.addFormItem('cmis:objectId', cmisObjectId);
          metadata = data;
          $('#affix_sezione_allegati div.well h1').text(i18n['affix_sezione_allegati']);
          var showAllegati = createAttachments($('#affix_sezione_allegati div.well'));
          showAllegati();
        }
        UI.success(i18n['message.operation.performed']);
      },
      complete: close,
      error: URL.errorFn
    });
  });
  $('#publish').click(function () {
    if (bulkinfo.validate()) {
      UI.confirm(i18n.prop('message.warning.publish'), function () {
        UI.confirm(i18n.prop('message.warning.publish.2'), function () {
          Call.publish(bulkinfo.getData(), $('#publish').find('i.icon-eye-open').length !== 0, function (published, removeClass, addClass, title, data) {
            metadata['jconon_call:pubblicato'] = published;
            if (published && !common.User.admin) {
              window.location.href = '/avvisi-pubblici-di-selezione-comparativa';
            }
            $('#publish').html('<i class="' + addClass + '"></i> ' + (published ? i18n['button.unpublish.portale'] : i18n['button.publish.portale']));
          });
        });
      });
    } else {
      var msg = content
        .children('form')
        .validate()
        .errorList
        .map(function (item) {
          if ($(item.element).hasClass('widget')) {
            return $(item.element).find('label').text();
          } else {
            return $(item.element).parents('.control-group').find('label').text();
          }
        })
        .filter(function (x) {
          return x.trim().length > 0;
        })
        .map(function (x) {
          return x.length > 50 ? x.substr(0, 50) + "\u2026" : x;
        })
        .join('<br>');
      UI.alert(i18n['message.improve.required.fields'] + '<br><br>' + msg)
    }
  });

  $('#close').click(function () {
    UI.confirm(i18n.prop('message.exit.without.saving'), function () {
      window.location.href = cache.redirectUrl;
    });
  });

  function onChangeNumeroDipendenti(data) {
    var options = content.find('#fascia_professionale option:contains("2")');
    if (data === 'Maggiore o uguale a 250') {
      options.attr('disabled', 'disabled');
      options.removeAttr('selected');
    } else if (data) {
      options.removeAttr('disabled');
    }
    if (data) {
      content.find('#fascia_professionale').trigger('change');
    }
  }

  function manangeClickNumeroDipendenti() {
    $('#numero_dipendenti > button.btn').on("click", function () {
      onChangeNumeroDipendenti($(this).attr('data-value'));
    });
  }

  function onChangeTipologiaOIV(data, onChange) {
    var select = content.find('#tipologia_selezione'), 
      options =  content.find('#tipologia_selezione option:selected'),
      optionsNotMonocratico = content.find('#tipologia_selezione option').not("[value='OIV Monocratico']").not("[value='']"),
      optionsMonocratico = content.find('#tipologia_selezione option:contains("OIV Monocratico")');
    if (onChange) {
      options.removeAttr('selected');
      select.val('');      
    }
    if (data === 'Monocratico') {
      optionsNotMonocratico.attr('disabled', 'disabled');
      optionsMonocratico.removeAttr('disabled');
    } else if (data === 'Collegiale') {
      optionsMonocratico.attr('disabled', 'disabled');
      optionsNotMonocratico.removeAttr('disabled');
    }
    if (data) {
      select.trigger('change');
    }
  }

  function manangeClickTipologiaOIV() {
    $('#tipologia_oiv').on("change", function () {
      onChangeTipologiaOIV($( "#tipologia_oiv option:selected" ).text(), true);
    });
  }

  function onChangeTipologiaSelezione(data) {
    var selectPresidente = content.find('#fascia_professionale'),
      selectComponente = content.find('#fascia_professionale_comp2, #fascia_professionale_comp3'),
      optionsPresidente = content.find('#fascia_professionale :not(option:empty)'),
      optionsComponente = content.find('#fascia_professionale_comp2 :not(option:empty), #fascia_professionale_comp3 :not(option:empty)'),
      compensoPresidente = content.find('#compenso_annuo_presidente'),
      compensoComponente = content.find('#compenso_annuo_componente, #compenso_annuo_altro_componente');
    if (data === 'OIV Monocratico' || data === 'Presidente') {      
      selectPresidente.removeAttr('disabled');
      compensoPresidente.removeAttr('disabled');
      optionsPresidente.removeAttr('disabled');

      optionsComponente.removeAttr('selected');
      selectComponente.attr('disabled', 'disabled');
      optionsComponente.attr('disabled', 'disabled');
      compensoComponente.val('');
      compensoComponente.attr('disabled', 'disabled');
    } else if (data) {
      selectComponente.removeAttr('disabled');
      optionsComponente.removeAttr('disabled');
      compensoComponente.removeAttr('disabled');
      if (data === 'Intero Collegio') {
        selectPresidente.removeAttr('disabled');
        compensoPresidente.removeAttr('disabled');
        optionsPresidente.removeAttr('disabled');
      } else {
        optionsPresidente.removeAttr('selected');
        selectPresidente.attr('disabled', 'disabled');
        optionsPresidente.attr('disabled', 'disabled');
        compensoPresidente.val('');
        compensoPresidente.attr('disabled', 'disabled');
      }
    }
    selectPresidente.trigger('change');
    selectComponente.trigger('change');
    onChangeNumeroDipendenti($('#numero_dipendenti > button.btn.active').attr('data-value'));
  }

  function manangeClickTipologiaSelezione() {
    $('#tipologia_selezione').on("change", function () {
      onChangeTipologiaSelezione($( "#tipologia_selezione option:selected" ).text());
    });
  }

  function bulkInfoRender() {
    if (metadata) {
      var pubblicato = metadata['jconon_call:pubblicato'],
        removeClass = pubblicato ? 'icon-eye-open' : 'icon-eye-close',
        addClass = pubblicato ? 'icon-eye-close' : 'icon-eye-open',
        title = pubblicato ? i18n['button.unpublish.portale'] : i18n['button.publish.portale'];        
      if (pubblicato && !common.User.admin) {
        $('#publish').hide();
      }
      $('#publish').html('<i class="' + addClass + '"></i> ' + title);
      showGestore();
    }
    bulkinfo = new BulkInfo({
      target: content,
      formclass: 'form-horizontal jconon',
      path: params['call-type'],
      name: forms,
      metadata: metadata,
      callback: {        
        afterCreateSection: function (section) {
          var div = section.find(':first-child'),
            showAllegati;
          if (section.attr('id') === 'affix_sezione_allegati' && !cmisObjectId) {
            div.addClass('well').append('<h1>' + i18n[section.attr('id') + '_initial']
              + '</h1><hr></hr>');          
          } else {
            div.addClass('well').append('<h1>' + i18n[section.attr('id')]
              + '</h1><hr></hr>');
          }
          if (section.attr('id') === 'affix_sezione_allegati' && cmisObjectId) {
            showAllegati = createAttachments(div);
            showAllegati();
          }
        },
        afterCreateForm: function (form) {
          onChangeNumeroDipendenti(metadata['jconon_call_procedura_comparativa:numero_dipendenti']);          
          manangeClickNumeroDipendenti();
          onChangeTipologiaOIV(metadata['jconon_call_procedura_comparativa:tipologia_oiv']);
          manangeClickTipologiaOIV();
          onChangeTipologiaSelezione(metadata['jconon_call_procedura_comparativa:tipologia_selezione']);
          manangeClickTipologiaSelezione();

          $('body').scrollspy({ target: '.cnr-sidenav' });
          content.prepend($('<div class="well jumbotron"><h1>' + i18n.prop(params['call-type']) + '</h1></div>'));
        },
        beforeCreateElement: function (item) {
          if (item.name === 'compenso_annuo_altro_componente' ||
              item.name === 'compenso_annuo_presidente' ||
              item.name === 'compenso_annuo_componente') {
            if (item.val) {
              item.val = currencyFormat(item.val);
            }
          }
        }
      }
    });
    bulkinfo.render();
  }

  function currencyFormat(num) {
    return (
        Number(num)
          .toFixed(2) // always two decimal digits
          .replace('.', ',') // replace decimal point character with ,
          .replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1.')
      ) // use . as a separator
  }

  function render() {
    URL.Data.bulkInfoForms({
      placeholder: {
        prefix: 'affix',
        path: params['call-type'],
        kind: 'forms'
      },
      success: function (data) {
        $.each(data, function (index, el) {
          forms[index] = el;
          var li = $('<li></li>'),
            a = $('<a href="#' + el + '"><i class="icon-chevron-right"></i>' + i18n.prop(el, el) + '</a>').click(function (eventObject) {
              changeActiveState($(eventObject.target));
            });
          if (index === 0) {
            li.addClass('active');
          }
          li.append(a).appendTo(ul);
        });
        if (cmisObjectId) {
          URL.Data.node.node({
            data: {
              nodeRef : cmisObjectId
            },
            type: 'GET',
            success: function (data) {
              metadata = data;
              if (data['cmis:secondaryObjectTypeIds'].indexOf('P:jconon_call:aspect_macro_call') >= 0) {
                metadata['add-remove-aspect'] = 'add-P:jconon_call:aspect_macro_call';
              }
              bulkInfoRender();
            },
            error: function (jqXHR, textStatus, errorThrown) {
              CNR.log(jqXHR, textStatus, errorThrown);
            }
          });
        } else if (copyFrom) {
          URL.Data.node.node({
            data: {
              nodeRef : copyFrom
            },
            type: 'GET',
            success: function (data) {
              metadata = data;

              metadata['jconon_call:pubblicato'] = undefined;
              metadata['jconon_call:codice'] = undefined;
              metadata['cmis:createdBy'] = undefined;
              metadata['cmis:objectId'] = undefined;
              metadata['cmis:parentId'] = undefined;

              if (data['cmis:secondaryObjectTypeIds'].indexOf('P:jconon_call:aspect_macro_call') >= 0) {
                metadata['add-remove-aspect'] = 'add-P:jconon_call:aspect_macro_call';
              }
              bulkInfoRender();
            },
            error: function (jqXHR, textStatus, errorThrown) {
              CNR.log(jqXHR, textStatus, errorThrown);
            }
          });          
        } else {
          bulkInfoRender();
        }
      }
    });
  };
  function init() {
    URL.Data.search.query({
      queue: true,
      data: {
        q: query
      }
    }).done(function (rs) {
      $.map(rs.items, function (item) {
        jsonlistMacroCall.push({
          "key" : item['cmis:objectId'],
          "label" : item['jconon_call:codice'],
          "defaultLabel" : item['jconon_call:codice']
        });
      });
      render();
    }).fail(function (jqXHR, textStatus, errorThrown) {
      CNR.log(jqXHR, textStatus, errorThrown);
    });    
  };
  if (!params['call-type']) {
    UI.error('Valorizzare il tipo di Bando');
  } else {
    if (cmisObjectId) {
      var xhr = Call.loadLabels(cmisObjectId);
      xhr.done(function () {
        init();
      });
    } else {
      init();
    }
  }
  $('button', toolbar).tooltip({
    placement: 'bottom',
    container: toolbar
  });
});