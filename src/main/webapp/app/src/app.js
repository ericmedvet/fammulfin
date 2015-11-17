/* global angular */

(function () {
  angular.module('fammulfinApp', ['ngRoute', 'restangular', 'mgcrea.ngStrap']);

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
  
})();

