define(['jquery', 'json!common', 'i18n', 'ws/header.common', 'cnr/cnr.url', 'cnr/cnr.ui', 'moment', 'cnr/cnr', 'noty', 'noty-layout', 'noty-theme'], function ($, common, i18n, headerCommon, URL, UI, moment, CNR) {
  "use strict";

  var params = URL.querystring.from,
    daysFromLastNews;

  headerCommon.addMenu($("#manage-call"), common.enableTypeCalls, 'manage-call?call-type=');

  headerCommon.arrangeSubMenus($('.navbar'));

  headerCommon.resizeNavbar(100);

  if (common.profile === 'fp') {
    $('#wrap').append('<div class="development"></div>');
  }
});