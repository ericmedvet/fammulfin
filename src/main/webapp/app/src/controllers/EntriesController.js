/* global angular, _ */

(function () {
  'use strict';

  angular.module('fammulfinApp').controller('EntriesController', function (Restangular, $routeParams, $scope) {
    var self = this;

    self.year = $routeParams.year;
    self.month = $routeParams.month;

    var updateGroup = function () {
      self.group = _.find($scope.mc.groups, function (group) {
        return group.id == $routeParams.groupId;
      });
    }
    
    updateGroup();

    $scope.$on("GroupsLoaded", updateGroup);

    Restangular.one("groups", $routeParams.groupId).getList("entries", {year: self.year, month: self.month}).then(function (entries) {
      self.entries = entries;
    });

  });

})();