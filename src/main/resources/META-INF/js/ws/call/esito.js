/*global params*/
define(['jquery', 'header', 'i18n', 'cnr/cnr', 'cnr/cnr.ui', 'cnr/cnr.bulkinfo',
  'cnr/cnr.jconon', 'cnr/cnr.ace', 'cnr/cnr.url', 'cnr/cnr.actionbutton', 'cnr/cnr.call', 'cnr/cnr.attachments', 'json!cache', 
  'cnr/cnr.ui.widgets', 'cnr/cnr.ui.wysiwyg', 'cnr/cnr.ace', 'json!common', 'fp/fp.application'
  ], function ($, header, i18n, CNR, UI, BulkInfo, jconon, ACE, URL, ActionButton, Call, Attachments, cache, Widgets, Wysiwyg, Ace, common, ApplicationFp) {
  "use strict";
  var ul = $('#affix'), content = $('#field'), forms = [], bulkinfo,
    toolbar = $('#toolbar-call'), jsonlistMacroCall = [], metadata = {},
    cmisObjectId = params['cmis:objectId'],
    copyFrom = params['copyFrom'],
    query = 'select this.jconon_call:codice, this.cmis:objectId, this.jconon_call:descrizione' +
    ' from ' + jconon.findCallQueryName(params['call-type']) + ' AS this ' +
    ' JOIN jconon_call:aspect_macro_call AS macro ON this.cmis:objectId = macro.cmis:objectId ' +
    ' order by this.cmis:lastModificationDate DESC', copyEnabled = false;

  function createAttachments(affix) {
    return new Attachments({
      affix: affix,
      objectTypes: cache.jsonlistCallEsitoAttachments,
      cmisObjectId: cmisObjectId,
      forbidArchives: false,
      maxUploadSize: true,
      search: {
        filter: false,
        type: 'jconon_attachment:call_fp_esito_abstract',
        displayRow: function (el, refreshFn) {
          return jconon.defaultDisplayDocument(el, refreshFn, false);
        }
      },
      buttonUploadLabel: 'Aggiungi allegato',
      submission: {
        requiresFile: true,
        showFile: true,
        bigmodal: false,
        externalData: [
          {
            name: 'jconon_attachment:user',
            value: common.User.userName
          },
          {
            name: 'inheritedPermission',
            value: false
          }          
        ]
      }
    });
  }
  function displayPartecipanti(el, refreshFn, permission, showLastModificationDate, showTitleAndDescription, extendButton, customIcons) {
    var tdText,
      tdButton,
      isFolder = el.baseTypeId === 'cmis:folder',
      item = $('<a href="#">' + el.name + '</a>'),
      customButtons = $.extend({}, {
        history : false,
        copy: false,
        cut: false
      }, extendButton),
      annotationAnagrafica = $('<span class="muted annotation"><b>' + el['jconon_attachment:esito_partecipanti_nome'] + ' ' + el['jconon_attachment:esito_partecipanti_cognome'] + '</b></span>'),
      annotationcf = $('<span class="muted annotation">Codice Fiscale: <b>' + el['jconon_attachment:esito_partecipanti_cf'] + '</b></span>'),
      annotationesito = $('<span class="muted annotation">Esito: <b>' + el['jconon_attachment:esito_partecipanti_esito'] + '</b></span>'),
      annotation = $('<span class="muted annotation">ultima modifica: ' + CNR.Date.format(el.lastModificationDate, null, 'DD/MM/YYYY H:mm') + '</span>');
    if (permission !== undefined) {
      customButtons.permissions = permission;
    }
    item.attr('href', URL.urls.search.content + '?nodeRef=' + el.id + '&guest=true');
    item.after(annotationAnagrafica);
    item.after(annotationcf);
    item.after(annotationesito);
    if (showLastModificationDate === false) {
      item.after('<span class="muted annotation">' + CNR.fileSize(el.contentStreamLength) + '</span>');
    } else {
      item.after(annotation.prepend(', ').prepend(CNR.fileSize(el.contentStreamLength)));
    }

    tdText = $('<td></td>')
      .addClass('span10')
      .append(CNR.mimeTypeIcon(el.contentType, el.name))
      .append(' ')
      .append(item);
    tdButton = $('<td></td>').addClass('span2').append(ActionButton.actionButton({
      name: el.name,
      nodeRef: el['alfcmis:nodeRef'],
      baseTypeId: el.baseTypeId,
      objectTypeId: el.objectTypeId,
      mimeType: el.contentType,
      allowableActions: el.allowableActions
    }, null, customButtons, customIcons, refreshFn));
    return $('<tr></tr>')
      .append(tdText)
      .append(tdButton);
  }

  function createAttachmentsPartecipanti(affix) {
    return new Attachments({
      affix: affix,
      objectTypes: cache.jsonlistCallEsitoPartecipantiAttachments,
      cmisObjectId: cmisObjectId,
      forbidArchives: false,
      maxUploadSize: true,
      search: {
        filter: false,
        type: 'jconon_attachment:call_fp_esito_partecipanti',
        displayRow: displayPartecipanti
      },
      buttonUploadLabel: 'Aggiungi partecipante',
      submission: {
        requiresFile: true,
        showFile: true,
        bigmodal: true,
        externalData: [
          {
            name: 'jconon_attachment:user',
            value: common.User.userName
          },
          {
            name: 'inheritedPermission',
            value: false
          }          
        ]
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

  $('#save').click(function () {
    bulkinfo.resetForm();
    var close = UI.progress();
    jconon.Data.call.main({
      type: 'POST',
      data: bulkinfo.getData(),
      success: function (data) {
        UI.success(i18n['message.operation.performed']);
      },
      complete: close,
      error: URL.errorFn
    });
  });
  function publishEsito(data, publish, callback) {
    var close = UI.progress();
    data.push({name: 'publish', value: publish});
    ApplicationFp.Data.call.publish_esito({
      type: 'POST',
      data: data,
      success: function (data) {
        UI.success(i18n['message.operation.performed'], function () {
          if (callback) {
            var pubblicato = data.published,
              removeClass = pubblicato ? 'icon-eye-open' : 'icon-eye-close',
              addClass = pubblicato ? 'icon-eye-close' : 'icon-eye-open',
              title = pubblicato ? i18n['button.unpublish'] : i18n['button.publish'];
            callback(pubblicato, removeClass, addClass, title, data);
          }
        });
      },
      complete: close,
      error: URL.errorFn
    });
  }
  $('#publish').click(function () {
    if (bulkinfo.validate()) {
      UI.confirm(i18n.prop('message.warning.publish'), function () {
        UI.confirm(i18n.prop('message.warning.publish.2'), function () {
          publishEsito(bulkinfo.getData(), $('#publish').find('i.icon-eye-open').length !== 0, function (published, removeClass, addClass, title, data) {
            metadata['jconon_call_procedura_comparativa:pubblicato_esito'] = published;
            if (published && !common.User.admin) {
              window.location.href = '/avvisi-pubblici-di-selezione-comparativa';
            }          
            $('#publish').html('<i class="' + addClass + '"></i> ' + (published ? i18n['button.unpublish.esito.portale'] : i18n['button.publish.esito.portale']));
          });
        });
      });
    } else {
      UI.alert(i18n['message.improve.required.fields']);
    }
  });

  $('#close').click(function () {
    UI.confirm(i18n.prop('message.exit.without.saving'), function () {
      window.location.href = cache.redirectUrl;
    });
  });

  function bulkInfoRender() {
    if (metadata) {
      var pubblicato = metadata['jconon_call_procedura_comparativa:pubblicato_esito'],
        removeClass = pubblicato ? 'icon-eye-open' : 'icon-eye-close',
        addClass = pubblicato ? 'icon-eye-close' : 'icon-eye-open',
        title = pubblicato ? i18n['button.unpublish.esito.portale'] : i18n['button.publish.esito.portale'];
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
            showAllegati, showAllegatiPartecipanti;
          div.addClass('well').append('<h1>' + i18n[section.attr('id')]
            + '</h1><hr></hr>');
          if (section.attr('id') === 'esito_sezione_allegati' && cmisObjectId) {
            showAllegati = createAttachments(div);
            showAllegati();
          }
          if (section.attr('id') === 'esito_sezione_cv' && cmisObjectId) {
            showAllegatiPartecipanti = createAttachmentsPartecipanti(div);
            showAllegatiPartecipanti();
          }
        },
        afterCreateForm: function (form) {
          form.find("[id$='_en']").parents("div.control-group").hide();
          $('body').scrollspy({ target: '.cnr-sidenav' });
          content.prepend($('<div class="well jumbotron"><h1>' + metadata['jconon_call_procedura_comparativa:amministrazione'] + '</h1></div>'));
        }
      }
    });
    bulkinfo.render();
  }

  function render() {
    URL.Data.bulkInfoForms({
      placeholder: {
        prefix: 'esito',
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

              metadata['jconon_call_procedura_comparativa:pubblicato_esito'] = undefined;
              metadata['jconon_call:codice'] = undefined;
              metadata['cmis:createdBy'] = undefined;
              metadata['cmis:objectId'] = undefined;
              metadata['cmis:parentId'] = undefined;
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