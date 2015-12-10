/* global angular, _ */

(function () {
  'use strict';

  angular.module('fammulfinApp', ['ngRoute', 'restangular', 'mgcrea.ngStrap', 'ui.gravatar', 'angular-loading-bar']);

  angular.module('fammulfinApp').config(function (RestangularProvider) {
    RestangularProvider.setBaseUrl('/api');
  });

  angular.module('fammulfinApp').config(function ($routeProvider) {
    $routeProvider.
            when('/dashboard', {templateUrl: 'partials/dashboard.html', controller: 'DashboardController as dc'}).
            when('/:groupId/group', {templateUrl: 'partials/group.html', controller: 'GroupController'}).
            when('/:groupId/entries', {redirectTo: '/:groupId/entries/all/' + (new Date()).getFullYear() + '/' + (1 + 1 * (new Date()).getMonth())}).
            when('/:groupId/entries/all/:year/:month?', {templateUrl: 'partials/entries.html', controller: 'EntriesController as ec'}).
            when('/:groupId/entries/chapter/:chapterId/:year?/:month?', {templateUrl: 'partials/entries.html', controller: 'EntriesController as ec'}).
            otherwise({redirectTo: '/dashboard'});
  });

  angular.module('fammulfinApp').constant('CONST', {
    currencies: {
      'EUR': {symbol: '€', name: "Euro"},
      'USD': {symbol: '$', name: "US dollar"},
      'JPY': {symbol: '¥', name: "Yen"},
      'GBP': {symbol: '£', name: "GB pound"}
    },
    chapterSeparator: '>',
    dateFormats: {
      log: 'yyyy-MM-dd HH:mm',
      short: 'dd/MM/yy'
    },
    monthNames: ['January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December']
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

  angular.module('fammulfinApp').directive('faShares', function () {
    return {
      restrict: 'A',
      scope: {
        shares: '=shares',
        percentage: '=percentage',
        amountValue: '=amountValue',
        usersMap: '=usersMap'
      },
      controller: function ($scope) {
        $scope.isJustAll = function () {
          return ($scope.shares.length===_.keys($scope.usersMap).length)&&$scope.areAllEquals();
        };
        $scope.areAllEquals = function () {
          if ($scope.shares.length <= 1) {
            return true;
          }
          for (var i = 1; i < $scope.shares.length; i++) {
            if ($scope.shares[i][1] != $scope.shares[i - 1][1]) {
              return false;
            }
          }
          return true;
        }
      },
      templateUrl: 'partials/shares-template.html'
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
        var dec = Math.round(100 * (Math.abs(val) - Math.floor(Math.abs(val))));
        if (dec == 0) {
          return '00';
        }
        if (dec < 10) {
          return '0' + dec;
        }
        return dec;
      }
      return val;
    };
  });

  angular.module('fammulfinApp').filter('numberSign', function () {
    return function (val) {
      if (angular.isNumber(val)) {
        return (val < 0) ? '-' : '';
      }
      return val;
    };
  });

  //http://stackoverflow.com/a/12506795
  angular.module('fammulfinApp').factory('Title', function () {
    var defaultTitle = 'Fammulfin';
    var context = '';
    var subContext = '';
    return {
      getTitle: function () {
        if (context == '') {
          return defaultTitle;
        }
        var title = context;
        if (subContext) {
          title = title + ' : ' + subContext;
        }
        return title;
      },
      setContext: function (newContext) {
        context = newContext;
      },
      setSubContext: function (newSubContext) {
        subContext = newSubContext;
      }
    };
  });

})();

