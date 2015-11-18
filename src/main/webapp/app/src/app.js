/* global angular */

(function () {
  'use strict';

  angular.module('fammulfinApp', ['ngRoute', 'restangular', 'mgcrea.ngStrap', 'ui.gravatar']);

  angular.module('fammulfinApp').config(function (RestangularProvider) {
    RestangularProvider.setBaseUrl('/api');
  });

  angular.module('fammulfinApp').config(function ($routeProvider) {
    $routeProvider.
            when('/dashboard', {templateUrl: 'partials/dashboard.html', controller: 'DashboardController as dc'}).
            when('/:groupId/group/', {templateUrl: 'partials/group.html', controller: 'GroupController'}).
            when('/:groupId/entries/', {redirectTo: '/:groupId/entries/' + (new Date()).getFullYear() + '/' + (1 + 1 * (new Date()).getMonth())}).
            when('/:groupId/entries/:year/:month', {templateUrl: 'partials/entries.html', controller: 'EntriesController as ec'}).
            otherwise({redirectTo: '/dashboard'});
  });

  angular.module('fammulfinApp').constant('CURRENCIES', {
    'eur': {symbol: '€', name: "Euro"},
    'usd': {symbol: '$', name: "US dollar"},
    'jpy': {symbol: '¥', name: "Yen"},
    'gbp': {symbol: '£', name: "GB pound"}
  });

  angular.module('fammulfinApp').constant('CHAPTER_SEPARATOR', ">");

  angular.module('fammulfinApp').constant('DATE_FORMATS', {
    log: 'yyyy-MM-dd HH:mm',
    short: 'dd/MM/yy'
  });

  angular.module('fammulfinApp').directive('faAmount', function () {
    return {
      restrict: 'A',
      scope: {
        amount: '=amount'
      },
      templateUrl: 'partials/amount-template.html'
    };
  });

  angular.module('fammulfinApp').filter('numberIntPart', function () {
    return function (val) {
      if (angular.isNumber(val)) {
        return Math.floor(Math.abs(val));
      }
      return val;
    };
  });

  angular.module('fammulfinApp').filter('numberDecimalPart', function () {
    return function (val) {
      if (angular.isNumber(val)) {
        var dec = Math.round(100*(Math.abs(val)-Math.floor(Math.abs(val))));
        if (dec==0) {
          return '00';
        }
        return dec;
      }
      return val;
    };
  });

  angular.module('fammulfinApp').filter('numberSign', function () {
    return function (val) {
      if (angular.isNumber(val)) {
        return (val<0)?'-':'';
      }
      return val;
    };
  });

})();

