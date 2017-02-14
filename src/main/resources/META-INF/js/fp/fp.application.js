/* javascript closure providing all the search functionalities */
define(['jquery', 'cnr/cnr', 'i18n', 'json!common', 'cnr/cnr.actionbutton', 'cnr/cnr.ui', 'cnr/cnr.jconon', 'cnr/cnr.url',
  'cnr/cnr.search', 'cnr/cnr.criteria', 'cnr/cnr.node', 'cnr/cnr.bulkinfo', 'json!cache', 'cnr/cnr.application', 'cnr/cnr.call'
  ], function ($, CNR, i18n, common, ActionButton, UI, jconon, URL, Search, Criteria, Node, BulkInfo, cache, Application, Call) {
  "use strict";
  var urls = {
    call : {
      publish_esito: 'rest/call-fp/publish-esito'
    }
  },
    defaults = {},
    settings = defaults;

  function init(options) {
    settings = $.extend({}, defaults, options);
  }

  function displayEsperienzeOIV(el, refreshFn) {
    var tdText,
      tdButton,
      isNonCoerente = el['cmis:secondaryObjectTypeIds'].indexOf('P:jconon_scheda_anonima:esperienza_non_coerente') !== -1,
      isRdP = el.parentProp ? (Call.isRdP(el.parentProp.relationships.parent[0]['jconon_call:rdp']) || common.User.admin) : false,
      callId = el.parentProp ? el.parentProp.relationships.parent[0]['cmis:objectId'] : undefined,
      userName = el.parentProp ? el.parentProp['jconon_application:user'] : undefined,
      title = el['jconon_attachment:esperienza_professionale_datore_lavoro'] ||
        el['jconon_attachment:aspect_specializzazioni_universita'] ||
        el['jconon_attachment:precedente_incarico_oiv_amministrazione'],
      ruolo = el['jconon_attachment:esperienza_professionale_ruolo'] ||
        el['jconon_attachment:precedente_incarico_oiv_ruolo'],
      esperienza = el['jconon_attachment:esperienza_professionale_da'] ||
        el['jconon_attachment:esperienza_professionale_a'],
      esperienzaOIV = el['jconon_attachment:precedente_incarico_oiv_da'] ||
        el['jconon_attachment:precedente_incarico_oiv_a'],
      item = $('<a href="#">' + title + '</a>').on('click', function () {
        Node.displayMetadata(el.objectTypeId, el.id, true);
        return false;
      }),
      annotationObjectType = $('<span class="annotation"><strong>' + i18n[el.objectTypeId] + '</strong></span>'),
      periodoEsperienzaDa = el['jconon_attachment:esperienza_professionale_da'] ? ('dal ' + CNR.Date.format(el['jconon_attachment:esperienza_professionale_da'], null, 'DD/MM/YYYY')) : '',
      periodoEsperienzaA = el['jconon_attachment:esperienza_professionale_a'] ? (' al ' + CNR.Date.format(el['jconon_attachment:esperienza_professionale_a'], null, 'DD/MM/YYYY')) : '',
      annotationPeriodoEsperienza = $('<span class="muted annotation"><strong>Periodo di attività: </strong>' + periodoEsperienzaDa + periodoEsperienzaA + '</span>'),
      periodoOIVDa = el['jconon_attachment:precedente_incarico_oiv_da'] ? ('dal ' + CNR.Date.format(el['jconon_attachment:precedente_incarico_oiv_da'], null, 'DD/MM/YYYY')) : '',
      periodoOIVA = el['jconon_attachment:precedente_incarico_oiv_a'] ? (' al ' + CNR.Date.format(el['jconon_attachment:precedente_incarico_oiv_a'], null, 'DD/MM/YYYY')) : '',
      annotationPeriodoOIV = $('<span class="muted annotation"><strong>Periodo di attività: </strong>' + periodoOIVDa + periodoOIVA + '</span>'),
      annotationRuolo = $('<span class="muted annotation"><strong>Ruolo:</strong> ' + ruolo  + '</span>'),
      modalTitle = title + ' Ruolo: ' + ruolo,
      tdNonCoerente = $('<td>').addClass('span5'),
      motivazione = el['jconon_attachment:esperienza_non_coerente_motivazione'],
      spanNonCoerente = $('<span>').addClass('text-error animated flash').appendTo(tdNonCoerente);
    if (esperienza) {
      item.after(annotationPeriodoEsperienza);      
    }
    if (esperienzaOIV) {
      item.after(annotationPeriodoOIV);      
    }
    if (ruolo) {
      item.after(annotationRuolo);
    }
    if (isNonCoerente && isRdP) {
      if (motivazione !== '') {
        $('<a href="#">').addClass('text-error').append(i18n.prop('label.esperienza.non.coerente')).off('click').on('click', function () {
          UI.alert(motivazione);
          return false;
        }).appendTo(spanNonCoerente);
      } else {
        spanNonCoerente.append(i18n.prop('label.esperienza.non.coerente'));
      }
    }

    tdText = $('<td></td>')
      .addClass('span5')
      .append(annotationObjectType)
      .append(item);
    tdButton = $('<td></td>').addClass('span2').append(ActionButton.actionButton({
      name: el.name,
      nodeRef: el.id,
      baseTypeId: el.baseTypeId,
      objectTypeId: el.objectTypeId,
      mimeType: el.contentType,
      allowableActions: el.allowableActions,
      defaultChoice: 'select'
    }, {copy_curriculum: 'CAN_UPDATE_PROPERTIES'}, {
      permissions : false,
      history : false,
      copy: false,
      cut: false,
      update: false,
      remove: function () {
        UI.confirm('Sei sicuro di voler eliminare la riga "' +  title  + '"?', function () {
          Node.remove(el.id, refreshFn);
        });
      },
      edit: function () {
        Application.editProdotti(el, title, refreshFn);
      },
      copy_curriculum: function () {
        Application.editProdotti(el, title, refreshFn, true);
      },
      coerente: isRdP && isNonCoerente ? function () {
          var d = [
            {
              id: 'cmis:objectId',
              name: 'cmis:objectId',
              value: el['cmis:objectId']
            },
            {
              name: 'aspect', 
              value: 'P:jconon_scheda_anonima:esperienza_non_coerente'
            },
            {
              name: 'userName', 
              value: userName
            },            
            {
              name: 'callId', 
              value: callId
            }
          ];
          $.ajax({
            url: cache.baseUrl + "/rest/application-fp/esperienza-coerente",
            type: 'POST',
            data:  d,
            success: function (data) {
              UI.success(i18n['message.esperienza.coerente.eseguito'], refreshFn);
            },
            complete: close,
            error: URL.errorFn
          });
      } : false,
      noncoerente: isRdP && !isNonCoerente ? function () {
         var content = $("<div></div>"),
            bulkinfo = new BulkInfo({
              target: content,
              path: "P:jconon_scheda_anonima:esperienza_non_coerente",
              objectId: el.id,
              formclass: 'form-inline',
              name: 'default'
            });
          bulkinfo.render();
          UI.modal('<i class="icon-edit"></i> ' + modalTitle, content, function () {
            var close = UI.progress(), d = bulkinfo.getData();
            d.push(
              {
                id: 'cmis:objectId',
                name: 'cmis:objectId',
                value: el['cmis:objectId']
              },
              {
                name: 'aspect', 
                value: 'P:jconon_scheda_anonima:esperienza_non_coerente'
              },
              {
                name: 'callId', 
                value: callId
              }                        
            );
            $.ajax({
              url: cache.baseUrl + "/rest/application-fp/esperienza-noncoerente",
              type: 'POST',
              data:  d,
              success: function (data) {
                UI.success(i18n['message.esperienza.noncoerente.eseguito'], refreshFn);
              },
              complete: close,
              error: URL.errorFn
            });
          });
      } : false,
      paste: Application.getTypeForDropDown('jconon_call:elenco_sezioni_curriculum', el, title, refreshFn),
      move: Application.getTypeForDropDown('jconon_call:elenco_sezioni_curriculum', el, title, refreshFn, true)
    }, {copy_curriculum: 'icon-copy', paste: 'icon-paste', move: 'icon-move', noncoerente: 'icon-minus', coerente: 'icon-plus'}, refreshFn, true));
    return $('<tr></tr>')
      .append(tdText)
      .append(tdNonCoerente)
      .append(tdButton);
  }
  /* Revealing Module Pattern */
  return {
    displayEsperienzeOIV: displayEsperienzeOIV,
    URL: urls,
    Data: URL.initURL(urls),
    init: init    
  };
});