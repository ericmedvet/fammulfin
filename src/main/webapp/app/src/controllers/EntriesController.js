/* global angular */

(function () {
  'use strict';

  angular.module('fammulfinApp').controller('EntriesController', function (Restangular, $routeParams) {
    var self = this;

    self.year = $routeParams.year;
    self.month = $routeParams.month;

    self.group = _.find(function (group) {
      return group.id == $routeParams.groupId;
    });

    Restangular.one("groups", $routeParams.groupId).getList("entries", {year: self.year, month: self.month}).then(function (entries) {
      self.entries = entries;
    });

  });

})();