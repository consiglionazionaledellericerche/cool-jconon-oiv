/*global params*/
define(['jquery', 'header', 'i18n', 'cnr/cnr.ui', 'cnr/cnr.bulkinfo', 'json!common', 'cnr/cnr.jconon', 'cnr/cnr.url',
    'cnr/cnr.application', 'cnr/cnr.attachments', 'json!cache', 'cnr/cnr.call', 'cnr/cnr', 'cnr/cnr.ui.widgets', 'fp/fp.application',
    'cnr/cnr.ui.wysiwyg', 'cnr/cnr.ui.country', 'cnr/cnr.ui.city'],
  function ($, header, i18n, UI, BulkInfo, common, jconon, URL,
    Application, Attachments, cache, Call, CNR, Widgets, ApplicationFp) {
  "use strict";

  var content = $('#field'), bulkinfo, forms = [], aspects = [],
    cmisObjectId, metadata = {}, dataPeopleUser,
    callId = params.callId,
    callCodice = params.callCodice,
    toolbar = $('#toolbar-call'),
    charCodeAspect = 97,
    preview = params.preview,
    showTitoli, showCurriculum, showProdottiScelti, showProdotti, showSchedeAnonime,
    applicationAttachments, curriculumAttachments, prodottiAttachments, schedeAnonimeAttachments,
    buttonPeople  = $('<button type="button" class="btn btn-small"><i class="icon-folder-open"></i> ' + i18n['button.explorer.people'] + '</button>'),
    buttonPeopleScelti  = $('<button type="button" class="btn btn-small"><i class="icon-folder-open"></i> ' + i18n['button.explorer.people'] + '</button>'),
    refreshFnProdotti, refreshFnProdottiScelti,
    saved,
    optionPubblico = [
      'Pubblica amministrazione','Forze armate','Magistratura','Università'
    ],
    ruoloLavorativo = [
      {
        key : 'Dirigente',
        defaultLabel: 'Dirigente',
        label: 'label.ruolo.lavorativo.Dirigente',
        group: 'Pubblica amministrazione'
      },{
        key : 'Funzionario',
        defaultLabel: 'Funzionario',
        label: 'label.ruolo.lavorativo.Funzionario',
        group: 'Pubblica amministrazione'
      },{
        key : 'Impiegato',
        defaultLabel: 'Impiegato',
        label: 'label.ruolo.lavorativo.Impiegato',
        group: 'Pubblica amministrazione'
      },{
        key : 'ALTRO',
        defaultLabel: 'ALTRO',
        label: 'label.ruolo.lavorativo.ALTRO',
        group: 'Pubblica amministrazione'
      },{
        key : 'Ufficiale',
        defaultLabel: 'Ufficiale',
        label: 'label.ruolo.lavorativo.Ufficiale',
        group: 'Forze armate'
      },{
        key : 'Sottufficiale/ispettore',
        defaultLabel: 'Sottufficiale/ispettore',
        label: 'label.ruolo.lavorativo.Sottufficiale/ispettore',
        group: 'Forze armate'
      },{
        key : 'Sovraintendente',
        defaultLabel: 'Sovraintendente',
        label: 'label.ruolo.lavorativo.Sovraintendente',
        group: 'Forze armate'
      },{
        key : 'ALTRO',
        defaultLabel: 'ALTRO',
        label: 'label.ruolo.lavorativo.ALTRO',
        group: 'Forze armate'
      },{
        key : 'Magistrato ordinario',
        defaultLabel: 'Magistrato ordinario',
        label: 'label.ruolo.lavorativo.Magistratoordinario',
        group: 'Magistratura'
      },{
        key : 'Magistrato amministrativo',
        defaultLabel: 'Magistrato amministrativo',
        label: 'label.ruolo.lavorativo.Magistratoamministrativo',
        group: 'Magistratura'
      },{
        key : 'Magistrato contabile',
        defaultLabel: 'Magistrato contabile',
        label: 'label.ruolo.lavorativo.Magistratocontabile',
        group: 'Magistratura'
      },{
        key : 'Magistrato onorario',
        defaultLabel: 'Magistrato onorario',
        label: 'label.ruolo.lavorativo.Magistratoonorario',
        group: 'Magistratura'
      },{
        key : 'ALTRO',
        defaultLabel: 'ALTRO',
        label: 'label.ruolo.lavorativo.ALTRO',
        group: 'Magistratura'
      },{
        key : 'Professore ordinario',
        defaultLabel: 'Professore ordinario',
        label: 'label.ruolo.lavorativo.Professoreordinario',
        group: 'Università'
      },{
        key : 'Professore associato',
        defaultLabel: 'Professore associato',
        label: 'label.ruolo.lavorativo.Professoreassociato',
        group: 'Università'
      },{
        key : 'Ricercatore',
        defaultLabel: 'Ricercatore',
        label: 'label.ruolo.lavorativo.Ricercatore',
        group: 'Università'
      },{
        key : 'ALTRO',
        defaultLabel: 'ALTRO',
        label: 'label.ruolo.lavorativo.ALTRO',
        group: 'Università'
      },{
        key : 'Imprenditore',
        defaultLabel: 'Imprenditore',
        label: 'label.ruolo.lavorativo.Imprenditore',
        group: 'Settore privato'
      },{
        key : 'Dirigente',
        defaultLabel: 'Dirigente',
        label: 'label.ruolo.lavorativo.Dirigente',
        group: 'Settore privato'
      },{
        key : 'Quadro',
        defaultLabel: 'Quadro',
        label: 'label.ruolo.lavorativo.Quadro',
        group: 'Settore privato'
      },{
        key : 'Impiegato',
        defaultLabel: 'Impiegato',
        label: 'label.ruolo.lavorativo.Impiegato',
        group: 'Settore privato'
      },{
        key : 'ALTRO',
        defaultLabel: 'ALTRO',
        label: 'label.ruolo.lavorativo.ALTRO',
        group: 'Settore privato'
      },{
        key : 'Avvocato',
        defaultLabel: 'Avvocato',
        label: 'label.ruolo.lavorativo.Avvocato',
        group: 'Libera professione'
      },{
        key : 'Commercialista/revisore',
        defaultLabel: 'Commercialista/revisore',
        label: 'label.ruolo.lavorativo.Commercialista/revisore',
        group: 'Libera professione'
      },{
        key : 'Consulente',
        defaultLabel: 'Consulente',
        label: 'label.ruolo.lavorativo.Consulente',
        group: 'Libera professione'
      },{
        key : 'ALTRO',
        defaultLabel: 'ALTRO',
        label: 'label.ruolo.lavorativo.ALTRO',
        group: 'Libera professione'
      }
    ];
  if (content.hasClass('error-allegati-empty')) {
    UI.alert(content.data('message') || i18n['message.error.allegati.empty'], null, null, true);
  }

  $('.cnr-sidenav').affix({
    offset: {
      top: 290,
      bottom: 270
    }
  });

  if (preview) {
    $('#send,#save,#delete').prop('disabled', true);
  }
  function isSaved() {
    return saved || preview;
  }

  function setObjectValue(obj, value) {
    if (obj) {
      obj.val(value);
      if (obj.parents(".widget").size() > 0) {
        obj.trigger('change');
      } else {
        obj.trigger('blur');
      }
    }
  }

  function createTitoli(affix) {
    return new Attachments({
      isSaved: isSaved,
      affix: affix,
      objectTypes: applicationAttachments,
      cmisObjectId: cmisObjectId,
      search: {
        type: 'jconon_attachment:generic_document',
        displayRow: Application.displayTitoli,
        fetchCmisObject: true,
        maxItems: 5,
        filter: false
      },
      submission: {
        externalData: [
          {
            name: 'aspect',
            value: 'P:jconon_attachment:generic_document'
          },
          {
            name: 'jconon_attachment:user',
            value: dataPeopleUser.userName
          }
        ]
      }
    });
  }
  function displayTotalNumItems(affix, documents) {
    var label = documents.totalNumItems < 0 ? 
      i18n.prop('label.righe.has.more.items', documents.maxItemsPerPage) :
      i18n.prop('label.righe.visualizzate', documents.totalNumItems);  
    affix.find('h1').after('<sub class="total pull-right">' + label + '</sub>');
  }

  function createProdottiScelti(affix, isMoveable) {
    return new Attachments({
      isSaved: isSaved,
      affix: affix,
      objectTypes: prodottiAttachments,
      cmisObjectId: cmisObjectId,
      search: {
        type: 'cvpeople:commonMetadata',
        join: 'cvpeople:selectedProduct',
        isAspect: true,
        filter: false,
        includeAspectOnQuery: true,
        label: 'label.count.no.prodotti',
        displayRow: function (el, refreshFn) {
          refreshFnProdottiScelti = refreshFn;
          return Application.displayProdottiScelti(el, refreshFn, refreshFnProdotti, isMoveable);
        },
        displayAfter: function (documents, refreshFn, resultSet, isFilter) {
          if (!isFilter) {
            affix.find('sub.total').remove();
            displayTotalNumItems(affix, documents);
          }
        },
        maxItems: 5,
        mapping: function (mapping) {
          mapping.parentId = cmisObjectId;
          return mapping;
        }
      },
      input: {
        rel: {
          "cmis:sourceId" : null,
          "cmis:relObjectTypeId" : 'R:jconon_attachment:in_prodotto'
        }
      },
      submission: {
        externalData: [
          {
            name: 'aspect',
            value: 'P:cvpeople:selectedProduct'
          },
          {
            name: 'jconon_attachment:user',
            value: dataPeopleUser.userName
          }
        ],
        multiple: true,
        bigmodal: true
      },
      otherButtons: [
        {
          button : buttonPeopleScelti,
          add : function (type, cmisObjectId, refreshFn) {
            Application.people(type, cmisObjectId, 'P:cvpeople:selectedProduct', refreshFn, dataPeopleUser);
          }
        }
      ]
    });
  }

  function createProdotti(affix, isMoveable) {
    return new Attachments({
      isSaved: isSaved,
      affix: affix,
      objectTypes: prodottiAttachments,
      cmisObjectId: cmisObjectId,
      search: {
        type: 'cvpeople:commonMetadata',
        join: 'cvpeople:noSelectedProduct',
        isAspect: true,
        displayRow: function (el, refreshFn) {
          refreshFnProdotti = refreshFn;
          return Application.displayProdotti(el, refreshFn, refreshFnProdottiScelti, isMoveable);
        },
        displayAfter: function (documents, refreshFn, resultSet, isFilter) {
          if (!isFilter) {
            affix.find('sub.total').remove();
            displayTotalNumItems(affix, documents);
          }
        },
        maxItems: 5,
        filter: false,
        includeAspectOnQuery: true,
        label: 'label.count.no.prodotti',
        mapping: function (mapping) {
          mapping.parentId = cmisObjectId;
          return mapping;
        }
      },
      submission: {
        externalData: [
          {
            name: 'aspect',
            value: 'P:cvpeople:noSelectedProduct'
          },
          {
            name: 'jconon_attachment:user',
            value: dataPeopleUser.userName
          }
        ],
        requiresFile: false,
        showFile: false,
        bigmodal: true
      },
      otherButtons: [{
        button : buttonPeople,
        add : function (type, cmisObjectId, refreshFn) {
          Application.people(type, cmisObjectId, 'P:cvpeople:noSelectedProduct', refreshFn, dataPeopleUser);
        }
      }]
    });
  }

  function createCurriculum(affix) {
    return new Attachments({
      isSaved: isSaved,
      affix: affix,
      objectTypes: curriculumAttachments,
      cmisObjectId: cmisObjectId,
      search: {
        type: 'jconon_attachment:cv_element',
        displayRow: Application.displayCurriculum,
        displayAfter: function (documents, refreshFn, resultSet, isFilter) {
          if (!isFilter) {
            affix.find('sub.total').remove();
            displayTotalNumItems(affix, documents);
          }
        },
        fetchCmisObject: true,
        maxItems: 5,
        filter: false,
        filterOnType: true,
        includeAspectOnQuery: true,
        label: 'label.count.no.curriculum',
        mapping: function (mapping) {
          mapping.parentId = cmisObjectId;
          mapping['jconon_call:elenco_sezioni_curriculum'] = metadata['jconon_call:elenco_sezioni_curriculum'];
          return mapping;
        }
      },
      buttonUploadLabel: 'Aggiungi riga',
      submission: {
        requiresFile: false,
        showFile: false,
        bigmodal: true,
        externalData: [
          {
            name: 'jconon_attachment:user',
            value: dataPeopleUser.userName
          }
        ]
      }
    });
  }

  function createSchedeAnonime(affix) {
    return new Attachments({
      isSaved: isSaved,
      affix: affix,
      objectTypes: schedeAnonimeAttachments,
      cmisObjectId: cmisObjectId,
      search: {
        type: 'jconon_scheda_anonima:document',
        displayRow: ApplicationFp.displayEsperienzeOIV,
        displayAfter: function (documents, refreshFn, resultSet, isFilter) {
          if (!isFilter) {
            affix.find('sub.total').remove();
            displayTotalNumItems(affix, documents);
          }
        },
        fetchCmisObject: true,
        calculateTotalNumItems: true,
        maxItems: 5,
        filter: false,
        filterOnType: true,
        includeAspectOnQuery: true,
        label: 'label.count.no.curriculum',
        mapping: function (mapping) {
          mapping.parentId = cmisObjectId;
          mapping['jconon_call:elenco_schede_anonime'] = metadata['jconon_call:elenco_schede_anonime'];
          return mapping;
        }
      },
      buttonUploadLabel: 'Aggiungi riga',
      submission: {
        requiresFile: false,
        showFile: false,
        bigmodal: true,
        externalData: [
          {
            name: 'jconon_attachment:user',
            value: dataPeopleUser.userName
          }
        ]
      }
    });
  }

  function manageIntestazione(call, application) {
    var descRid = null,
      existApplication = application && application["jconon_application:stato_domanda"] !== 'I',
      isTemp = existApplication && application["jconon_application:stato_domanda"] === 'P',
      lastName = application && application["jconon_application:cognome"] !== undefined ? application["jconon_application:cognome"] : dataPeopleUser.lastName,
      firstName = application && application["jconon_application:nome"] !== undefined ? application["jconon_application:nome"] : dataPeopleUser.firstName;
    if (call["cmis:objectTypeId"] === 'F:jconon_call_mobility_open:folder') {
      $('#application-title').hide();
    } else if (call["cmis:objectTypeId"] === 'F:jconon_call_mobility:folder') {
      $('#application-title').append(i18n['application.title.mobility']);
    } else {
      $('#application-title').append(i18n['application.title']);
    }

    $('#call-codice')
      .prepend(i18n['label.jconon_bando_selezione'] + ' ' + call["jconon_call:codice"])
      .on('click', 'button', function () {
        Call.displayAttachments(callId);
      });
    $('#call-desc').append(call["jconon_call:descrizione"]);
    if (call["jconon_call:sede"] && call["jconon_call:sede"].length) {
      descRid = (descRid !== null ? descRid + '</br>' : '') + call["jconon_call:sede"];
    } else if (call["jconon_call:elenco_settori_tecnologici"] && call["jconon_call:elenco_settori_tecnologici"].length) {
      /*jslint unparam: true*/
      $.each(call["jconon_call:elenco_settori_tecnologici"], function (index, el) {
        descRid = (descRid !== null ? descRid + ' - ' + el : i18n['label.th.jconon_bando_elenco_settori_tecnologici'] + ': ' + el);
      });
      /*jslint unparam: false*/
    } else if (call["jconon_call:elenco_macroaree"] && call["jconon_call:elenco_macroaree"].length) {
      /*jslint unparam: true*/
      $.each(call["jconon_call:elenco_macroaree"], function (index, el) {
        descRid = (descRid !== null ? descRid + ' - ' + el : i18n['label.th.jconon_bando_elenco_macroaree'] + ': ' + el);
      });
      /*jslint unparam: false*/
    }
    $('#call-desc-rid').append(call["jconon_call:descrizione_ridotta"] + (descRid !== null ? descRid : ""));
    $('#appl-rich').append(i18n['application.text.sottoscritto.' + (application['jconon_application:sesso'] !== "" ? application['jconon_application:sesso'] : 'M')] + ' ' + firstName.toUpperCase() + ' ' + lastName.toUpperCase() + '</br>' +
      (call['cmis:objectTypeId'] === 'F:jconon_call_employees:folder' ? i18n['cm.matricola'] + ': ' + dataPeopleUser.matricola + ' - ' + i18n['cm.email'] + ': ' + dataPeopleUser.email + '</br>' : '') +
      (call['cmis:objectTypeId'] === 'F:jconon_call_mobility_open:folder' ? '' : i18n['application.text.chiede.partecipare.predetta.procedura']));
  }

  function changeActiveState(btn) {
    btn.parents('ul').find('.active').removeClass('active');
    btn.parent('li').addClass('active');
  }

  function onChangeDipendentePubblico(data, onload) {
    var optionsPubblico = content.find('#situazione_lavorativa_settore option').filter(
        function(i, e) {
          return optionPubblico.indexOf($(e).text()) !== -1
        }
    ),optionsPrivato = content.find('#situazione_lavorativa_settore option').filter(
        function(i, e) {
          return optionPubblico.indexOf($(e).text()) === -1
        }
    );
    if (data === 'true') {
      optionsPrivato.attr('disabled', 'disabled');
      optionsPubblico.removeAttr('disabled');
    } else if (data === 'false') {
      optionsPubblico.attr('disabled', 'disabled');
      optionsPrivato.removeAttr('disabled');
    }
    if (!onload) {
        content.find('#situazione_lavorativa_settore').val('');
        content.find('#situazione_lavorativa_settore').trigger('change');
    }
  }

  function manangeClickDipendentePubblico() {
    $('#fl_dipendente_pubblico > button.btn').on("click", function () {
      onChangeDipendentePubblico($(this).attr('data-value'), false);
    });
  }

  function onChangeSettore(data, onChange) {
    var select = content.find('#situazione_lavorativa_ruolo'), 
      options = content.find('#situazione_lavorativa_ruolo option'),
      optionsGroup = content.find('#situazione_lavorativa_ruolo optgroup[label!="' + data + '"] option');
    if (onChange) {
      options.removeAttr('selected');
      select.val('');      
    }
    options.removeAttr('disabled');
    optionsGroup.attr('disabled', 'disabled');
    select.trigger('change');
  }

  function manangeClickSettore() {
    $('#situazione_lavorativa_settore').on("change", function () {
      onChangeSettore($("#situazione_lavorativa_settore option:selected" ).text(), true);
    });
  }

  function manageNazioni(value, fieldsItaly, fieldsForeign) {
    if (value && value.toUpperCase() === 'ITALIA') {
      fieldsForeign.val('').trigger('blur');
      fieldsItaly.parents(".control-group").show();
      fieldsForeign.parents(".control-group").hide();
    } else {
      fieldsItaly.val('').trigger('change');
      fieldsItaly.parents(".control-group").hide();
      fieldsForeign.parents(".control-group").show();
    }
  }

  function manageNazioneNascita(value) {
    var fieldsItaly = content.find("#comune_nascita"),
      fieldsForeign = content.find("#comune_nascita_estero");
    manageNazioni(value, fieldsItaly, fieldsForeign);
  }

  function manageNazioneResidenza(value) {
    var fieldsItaly = content.find("#comune_residenza"),
      fieldsForeign = content.find("#comune_residenza_estero");
    manageNazioni(value, fieldsItaly, fieldsForeign);
  }

  function manageNazioneComunicazioni(value) {
    var fieldsItaly = content.find("#comune_comunicazioni"),
      fieldsForeign = content.find("#comune_comunicazioni_estero");
    manageNazioni(value, fieldsItaly, fieldsForeign);
  }

  function tabAnagraficaFunction() {
    /*jslint unparam: true*/
    $('#nazione_nascita').parents('.widget').bind('changeData', function (event, key, value) {
      if (key === 'value') {
        manageNazioneNascita(value);
      }
    });
    /*jslint unparam: false*/
    manageNazioneNascita($("#nazione_nascita").attr('value'));
  }

  function tabResidenzaFunction() {
    /*jslint unparam: true*/
    $('#nazione_residenza').parents('.widget').bind('changeData', function (event, key, value) {
      if (key === 'value') {
        manageNazioneResidenza(value);
      }
    });
    /*jslint unparam: false*/
    manageNazioneResidenza($("#nazione_residenza").attr('value'));
  }

  function tabReperibilitaFunction() {
    /*jslint unparam: true*/
    $('#nazione_comunicazioni').parents('.widget').bind('changeData', function (event, key, value) {
      if (key === 'value') {
        manageNazioneComunicazioni(value);
      }
    });
    /*jslint unparam: false*/
    manageNazioneComunicazioni($("#nazione_comunicazioni").attr('value'));
    $("#copyFromTabResidenza").click(function () {
      UI.confirm(i18n.prop('message.copy.residenza'), function () {
        var nazioneVal = content.find("#nazione_residenza").val();
        setObjectValue(content.find("#nazione_comunicazioni"), nazioneVal);
        if (nazioneVal.toUpperCase() === 'ITALIA') {
          setObjectValue(content.find("#comune_comunicazioni"), content.find("#comune_residenza").val());
        } else {
          setObjectValue(content.find("#comune_comunicazioni_estero"), content.find("#comune_residenza_estero").val());
        }
        setObjectValue(content.find("#cap_comunicazioni"), content.find("#cap_residenza").val());
        setObjectValue(content.find("#indirizzo_comunicazioni"), content.find("#indirizzo_residenza").val());
        setObjectValue(content.find("#num_civico_comunicazioni"), content.find("#num_civico_residenza").val());
        manageNazioneComunicazioni(nazioneVal);
      });
    });
  }

  function bulkInfoRender(call) {
    cmisObjectId = metadata['cmis:objectId'];
    bulkinfo =  new BulkInfo({
      target: content,
      formclass: 'form-horizontal jconon',
      path: 'F:jconon_application:folder',
      name: forms,
      metadata: metadata,
      callback: {
        beforeCreateElement: function (item) {
          if (item.name === 'elenco_lingue_conosciute') {
            var jsonlistLingueConosciute = [];
            if (call["jconon_call:elenco_lingue_da_conoscere"] !== undefined) {
              $.each(call["jconon_call:elenco_lingue_da_conoscere"], function (index, el) {
                jsonlistLingueConosciute.push({
                  "key" : el,
                  "label" : el,
                  "defaultLabel" : el
                });
              });
              item.jsonlist = jsonlistLingueConosciute;
            }
          }
          if (item.name === 'email_pec_comunicazioni') {
            item.class = 'input-xlarge';
          }
          if (item.name === 'email_comunicazioni') {
            item.class = 'input-xlarge';
          }
          if (item.name === 'situazione_lavorativa_ruolo') {
            item.jsonlist = ruoloLavorativo;
          }
        },
        afterCreateForm: function (form) {
          var rows = form.find('#affix_tabDichiarazioni table tr'),
            labelKey = 'text.jconon_application_dichiarazione_sanzioni_penali_' + call['jconon_call:codice'],
            labelSottoscritto = i18n['application.text.sottoscritto.lower.' + (metadata['jconon_application:sesso'] !== "" ? metadata['jconon_application:sesso'] : 'M')],
            labelValue = i18n.prop(labelKey, labelSottoscritto);
          /*jslint unparam: true*/
          $.each(rows, function (index, el) {
            var td = $(el).find('td:last');
            if (td.find("[data-toggle=buttons-radio]").size() > 0) {
              td.find('label:first').addClass('span10').removeClass('control-label');
              td.find('.controls:first').addClass('span2');
            }
          });
          /*jslint unparam: false*/
          form.find('#affix_tabDichiarazioniConclusive label').addClass('span10').removeClass('control-label');
          form.find('#affix_tabDichiarazioniConclusive .controls').addClass('span2');
          if (labelValue === labelSottoscritto) {
            labelValue = i18n.prop('text.jconon_application_dichiarazione_sanzioni_penali', labelSottoscritto);
          }
          $('#fl_dichiarazione_sanzioni_penali').parents('div.widget').children('label').text(labelValue);
          $('#fl_dichiarazione_dati_personali').parents('div.widget').children('label').text(i18n.prop('text.jconon_application_dichiarazione_dati_personali', labelSottoscritto));
          $.each(call["jconon_call:elenco_field_not_required"], function (index, el) {
            var input = form.find("input[name='" + el + "']"),
              widget = form.find("#" + el.substr(el.indexOf(':') + 1)).parents('.widget');
            if (input.length !== 0) {
              input.rules('remove', 'required');
            }
            if (widget.length !== 0) {
              widget.rules('remove', 'requiredWidget');
            }
          });
          form.find('input.datepicker.input-small').addClass('input-medium').removeClass('inpt-small');
          form.find('#fascia_professionale_attribuita').parents('.control-group').after('<div class="alert alert-warning">Il calcolo della fascia verrà eseguito dopo il salvataggio.</div>');
          form.find("label[for='fascia_professionale_attribuita']").addClass('span8').append("&nbsp;&nbsp;");
          tabAnagraficaFunction();
          tabResidenzaFunction();
          tabReperibilitaFunction();
          manangeClickDipendentePubblico();
          manangeClickSettore();
          onChangeDipendentePubblico(String(metadata['jconon_application:fl_dipendente_pubblico']), true);
          onChangeSettore(metadata['jconon_application:situazione_lavorativa_settore']);
        },
        afterCreateSection: function (section) {
          var div = section.find(':first-child'),
            jsonlistApplicationNoAspects = (metadata['jconon_application:fl_cittadino_italiano'] ? cache.jsonlistApplicationNoAspectsItalian : cache.jsonlistApplicationNoAspectsForeign),
            loadAspect;
          if (section.attr('id').indexOf('affix') !== -1) {
            div.addClass('well').append('<h1>' + i18n[section.attr('id')] + '</h1><hr></hr>');
            if (section.attr('id') === 'affix_tabDichiarazioni') {
              div.append($('<table></table>').addClass('table table-bordered'));
            } else if (section.attr('id') === 'affix_tabTitoli' && cmisObjectId) {
              showTitoli = createTitoli(div);
              showTitoli();
            } else if (section.attr('id') === 'affix_tabCurriculum' && cmisObjectId) {
              showCurriculum = createCurriculum(div);
              showCurriculum();
            } else if (section.attr('id') === 'affix_tabProdottiScelti' && cmisObjectId) {
              showProdottiScelti = createProdottiScelti(div, call["jconon_call:elenco_sezioni_domanda"].indexOf('affix_tabElencoProdotti') !== -1);
              showProdottiScelti();
            } else if (section.attr('id') === 'affix_tabElencoProdotti' && cmisObjectId) {
              showProdotti = createProdotti(div, call["jconon_call:elenco_sezioni_domanda"].indexOf('affix_tabProdottiScelti') !== -1);
              showProdotti();
            } else if (section.attr('id') === 'affix_tabSchedaAnonima' && cmisObjectId) {
              showSchedeAnonime = createSchedeAnonime(div);
              showSchedeAnonime();
            }
          } else {
            loadAspect = true;
            /*jslint unparam: true*/
            $.each(jsonlistApplicationNoAspects, function (index, el) {
              if (el.key === section.attr('id')) {
                loadAspect = false;
              }
            });
            /*jslint unparam: false*/
            if (loadAspect) {
              if ((metadata['jconon_application:fl_cittadino_italiano'] && cache.jsonlistApplicationNoAspectsItalian.indexOf(section.attr('id')) === -1) ||
                  !(metadata['jconon_application:fl_cittadino_italiano'] && cache.jsonlistApplicationNoAspectsForeign.indexOf(section.attr('id')) === -1)) {
                if (call["jconon_call:elenco_aspects"].indexOf(section.attr('id')) !== -1) {
                  $('<tr></tr>')
                    .append('<td>' + String.fromCharCode(charCodeAspect++) + '</td>')
                    .append($('<td>').append(div))
                    .appendTo(content.find("#affix_tabDichiarazioni > :last-child > :last-child"));
                } else if (call["jconon_call:elenco_aspects_sezione_cnr"].indexOf(section.attr('id')) !== -1) {
                  div.appendTo(content.find("#affix_tabDatiCNR > :last-child"));
                } else if (call["jconon_call:elenco_aspects_ulteriori_dati"].indexOf(section.attr('id')) !== -1) {
                  div.appendTo(content.find("#affix_tabUlterioriDati > :last-child"));
                }
              }
            }
            section.hide();
          }
        }
      }
    });
    bulkinfo.render();
    bulkinfo.addFormItem('cmis:parentId', callId);
    /*jslint unparam: true*/
    $.each(aspects, function (index, el) {
      bulkinfo.addFormItem('aspect', el);
    });
    /*jslint unparam: false*/
    bulkinfo.addFormItem('cmis:objectId', metadata['cmis:objectId']);
  }

  function render(call, application) {
    var ul = $('.cnraffix'),
      print_dic_sost = $('<button class="btn btn-info" type="button">' + i18n['label.print.dic.sost'] + '</button>').on('click', function () {
        window.location = jconon.URL.application.print_dic_sost + '?applicationId=' + cmisObjectId;
      });
    $.each(call["jconon_call:elenco_sezioni_domanda"], function (index, el) {
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
    if (call["jconon_call:print_dic_sost"]) {
      $('.cnr-sidenav')
        .append('<br/>')
        .append(print_dic_sost);
    }
    aspects = call["jconon_call:elenco_aspects"]
      .concat(call["jconon_call:elenco_aspects_sezione_cnr"])
      .concat(call["jconon_call:elenco_aspects_ulteriori_dati"]);
    /*jslint unparam: true*/
    $.each(aspects, function (index, el) {
      forms[forms.length] = el;
    });
    /*jslint unparam: false*/
    metadata = $.extend({}, call, application);
    saved = metadata['jconon_application:stato_domanda'] !== 'I';
    bulkInfoRender(call);
  }


  $('#save').click(function () {
    bulkinfo.resetForm();
    var close = UI.progress();
    jconon.Data.application.main({
      type: 'POST',
      data: bulkinfo.getData(),
      success: function (data) {
        if (!cmisObjectId) {
          cmisObjectId = data.id;
          bulkinfo.addFormItem('cmis:objectId', cmisObjectId);
          UI.success(i18n['message.creazione.application']);
        } else {
          UI.success(i18n['message.aggiornamento.application']);
        }
        $('#fascia_professionale_attribuita').val(data['jconon_application:fascia_professionale_attribuita'] || '');
        saved = true;
      },
      complete: close,
      error: URL.errorFn
    });
  });
  $('#send').click(function () {
    var message = 'message.conferma.application.question',
      placeholder = '';
    if (metadata["jconon_call:elenco_sezioni_domanda"].indexOf('affix_tabProdottiScelti') !== -1 &&
        $('#affix_tabProdottiScelti').find('table:visible').length === 0) {
      placeholder = i18n.prop('message.conferma.application.prodotti.scelti');
    }
    UI.confirm(i18n.prop(message, placeholder), function () {
      if (bulkinfo.validate()) {
        jconon.Data.application.main({
          type: 'POST',
          data: bulkinfo.getData(),
          success: function (result) {
            $('#fascia_professionale_attribuita').val(result['jconon_application:fascia_professionale_attribuita'] || '');
            var container = $('<div class="fileupload fileupload-new" data-provides="fileupload"></div>'),
              input = $('<div class="input-append"></div>'),
              btn = $('<span class="btn btn-file btn-primary"></span>'),
              inputFile = $('<input type="file" name="domandapdf"/>'),
              btnPrimary,
              m,
              newPrint = $('<button class="btn btn-success"><i class="icon-file"></i> Scarica stampa</button>');

            btn
              .append('<span class="fileupload-new"><i class="icon-upload"></i> Upload Domanda firmata</span>')
              .append('<span class="fileupload-exists">Cambia</span>')
              .append(inputFile);

            input
              .append('<div class="uneditable-input input-xlarge"><i class="icon-file fileupload-exists"></i><span class="fileupload-preview"></span></div>')
              .append(btn)
              .appendTo(container);

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
              var fd = new CNR.FormData();
              fd.data.append("objectId", cmisObjectId);
              $.each(inputFile[0].files || [], function (i, file) {
                fd.data.append('domandapdf', file);
              });
              var close = UI.progress();
              $.ajax({
                  type: "POST",
                  url: cache.baseUrl + "/rest/application-fp/send-application",
                  data:  fd.getData(),
                  enctype: fd.contentType,
                  processData: false,
                  contentType: false,
                  dataType: "json",
                  success: function(data){
                    UI.success(i18n.prop('message.conferma.application.done', data.email_comunicazione), function () {
                      window.location.href = cache.baseUrl + "/my-applications";
                    });
                  },
                  complete: close,
                  error: URL.errorFn
              });
            }

            m = UI.modal('<i class="icon-upload animated flash"></i> Invia domanda', container, sendFile);
            btnPrimary = m.find(".modal-footer").find(".btn-primary");
            btnPrimary.before(newPrint);
            newPrint.click(function () {
              window.location = 'rest/application/print-immediate?nodeRef=' + cmisObjectId;
            });
            saved = true;
          },
          error: URL.errorFn
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
  });
  $('#close').click(function () {
    UI.confirm(i18n.prop('message.exit.without.saving'), function () {
      window.location.href = document.referrer;
    });
  });
  $('#print').click(function () {
    Application.print(cmisObjectId, metadata.allowableActions.indexOf('CAN_CREATE_DOCUMENT') !== -1 ? 'P' : metadata['jconon_application:stato_domanda'], true);
  });
  $('#delete').click(function () {
    Application.remove(cmisObjectId, function () {
      window.location.href = cache.redirectUrl + "/home";
    });
  });
  function main() {
    var xhr = Call.loadLabels(callId);
    xhr.done(function () {
      URL.Data.node.node({
        data: {
          excludePath : true,
          nodeRef : callId,
          cachable: !preview
        },
        callbackErrorFn: jconon.callbackErrorFn,
        success: function (dataCall) {
          applicationAttachments = Application.completeList(
            dataCall['jconon_call:elenco_association'],
            cache.jsonlistApplicationAttachments
          );
          curriculumAttachments = Application.completeList(
            dataCall['jconon_call:elenco_sezioni_curriculum'],
            cache.jsonlistApplicationCurriculums
          );
          prodottiAttachments = Application.completeList(
            dataCall['jconon_call:elenco_prodotti'],
            cache.jsonlistApplicationProdotti
          );
          schedeAnonimeAttachments = Application.completeList(
            dataCall['jconon_call:elenco_schede_anonime'],
            cache.jsonlistApplicationSchedeAnonime
          );
          jconon.Data.application.main({
            type: 'GET',
            queue: true,
            placeholder: {
              callId: callId,
              applicationId: params.applicationId,
              userId: common.User.id,
              preview: preview
            },
            error: function (jqXHR, textStatus, errorThrown) {
              var jsonMessage = JSON.parse(jqXHR.responseText);
              if (jsonMessage && jsonMessage.message === 'message.error.domanda.inviata.accesso') {
                UI.alert(i18n['message.application.alredy.send'], undefined, function () {
                  window.location.href = '/my-applications';
                });
              } else {
                URL.errorFn(jqXHR, textStatus, errorThrown, this);
              }
            },
            callbackErrorFn: jconon.callbackErrorFn
          }).done(function (dataApplication) {
            var message = $('#surferror').text() || 'Errore durante il recupero della domanda';
            if (!common.User.admin && common.User.id !== dataApplication['jconon_application:user']) {
              UI.error(i18n['message.error.caller.user'], function () {
                window.location.href = cache.redirectUrl;
              });
            } else {
              URL.Data.proxy.people({
                type: 'GET',
                contentType: 'application/json',
                placeholder: {
                  user_id: dataApplication['jconon_application:user']
                },
                success: function (data) {
                  dataPeopleUser = data;
                  manageIntestazione(dataCall, dataApplication);
                  render(dataCall, dataApplication);
                },
                error: function () {
                  UI.error(i18n['message.user.not.found']);
                  window.location.href = cache.redirectUrl;
                }
              });
            }
            toolbar.show();
          });
        }
      });
    });
  }
  $('button', toolbar).tooltip({
    placement: 'bottom',
    container: toolbar
  });
  if (callCodice) {
    URL.Data.search.query({
      data: {
        q: "select cmis:objectId " +
          "from jconon_call:folder where jconon_call:codice = '" + callCodice + "'"
      },
      success: function (data) {
        callId = data.items[0]['cmis:objectId'];
        main();
      }
    });

  } else {
    main();
  }
});