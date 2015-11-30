/* global angular */

(function () {
  'use strict';

  angular.module('fammulfinApp').controller('MainController', function (Restangular, Title, $scope, CURRENCIES, CHAPTER_SEPARATOR, DATE_FORMATS) {
    var self = this;
    
    self.currencies = CURRENCIES;
    self.chapterSeparator = CHAPTER_SEPARATOR;
    self.dateFormats = DATE_FORMATS;
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