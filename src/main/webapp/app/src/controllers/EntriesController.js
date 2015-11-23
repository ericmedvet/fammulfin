/* global angular, _ */

(function () {
  'use strict';

  angular.module('fammulfinApp').controller('EntriesController', function (Restangular, $routeParams, $scope) {
    var self = this;

    //from here should be put in service like this: http://stackoverflow.com/a/31967940/1003056
    self.year = $routeParams.year;
    self.month = $routeParams.month;
    self.chapterId = $routeParams.chapterId;

    var updateGroup = function () {
      self.group = _.find($scope.mc.groups, function (group) {
        return group.id == $routeParams.groupId;
      });
      Restangular.one("groups", $routeParams.groupId).getList("chapters").then(function (chapters) {
        self.chapters = chapters;
        $scope.$broadcast("ChaptersLoaded");
      });
    }
    
    var updateChaptersMap = function() {
      self.chaptersMap = {};
      _.each(self.chapters, function(chapter) {
        self.chaptersMap[chapter.id] = chapter;
      });
    };
    
    updateGroup();

    $scope.$on("GroupsLoaded", updateGroup);
    $scope.$on("ChaptersLoaded", updateChaptersMap);

    var queryParams = {};
    if (self.year!==undefined) {
      queryParams.year = self.year;
    }
    if (self.month!==undefined) {
      queryParams.month = self.month;
    }
    if (self.chapterId!==undefined) {
      queryParams.chapterId = self.chapterId;
    }
    Restangular.one("groups", $routeParams.groupId).getList("entries", queryParams).then(function (entries) {
      self.entries = entries;
    });

  });

})();