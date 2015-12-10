/* global angular */

(function () {
  'use strict';

  angular.module('fammulfinApp').controller('MainController', function (Restangular, Title, $scope, CONST) {
    var self = this;
    
    self.CONST = CONST;
    self.title = Title;

    Restangular.all('groups').getList().then(function (groups) {
      self.groups = groups;
      $scope.$broadcast("GroupsLoaded");
    });
    
    Restangular.one('users', 'me').get().then(function (user) {
      self.user = user;
    });
    
  });

})();