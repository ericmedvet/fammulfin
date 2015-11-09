/* global angular */

(function () {
  'use strict';

  angular.module('fammulfinApp').controller('HelloWorldController', ['$log', HelloWorldController]);

  function HelloWorldController($log) {
    var self = this;

    self.msg = "ciao mondissimo";
  }

})();