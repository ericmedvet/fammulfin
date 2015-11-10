/* global angular */

(function () {
  'use strict';

  angular.module('fammulfinApp').controller('MainController', function (Restangular, CURRENCIES, CHAPTER_SEPARATOR, DATE_FORMATS) {
    var self = this;
    
    self.currencies = CURRENCIES;
    self.chapterSeparator = CHAPTER_SEPARATOR;
    self.dateFormats = DATE_FORMATS;

    Restangular.all("groups").getList().then(function (groups) {
      self.groups = groups;
    });

    Restangular.one("users", "me").get().then(function (user) {
      self.user = user;
    });
  });

})();