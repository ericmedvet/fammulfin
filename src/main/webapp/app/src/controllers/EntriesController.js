/* global angular, _ */

(function () {
  'use strict';

  angular.module('fammulfinApp').controller('EntriesController', function (Restangular, Title, $routeParams, $scope, $modal) {
    var self = this;

    self.order = {
      field: 'date',
      'date': true,
      'amount.value': true,
      'payee': true
    };
    self.filterText = '';

    //from here should be put in service like this: http://stackoverflow.com/a/31967940/1003056
    // it does not work, since the chapters map is updated only on the service
    self.year = $routeParams.year;
    self.month = $routeParams.month;
    self.chapterId = $routeParams.chapterId;

    self.yearsRange = function (year) {
      return [year - 2, year - 1, year, year * 1 + 1, year * 1 + 2];
    }

    var updateGroup = function () {
      self.group = _.find($scope.mc.groups, function (group) {
        return group.id == $routeParams.groupId;
      });
      if (self.group !== undefined) {
        self.usersMap = {};
        _.each(self.group.usersMap, function (user) {
          self.usersMap[user[0].id] = user[1];
        });
        Title.setContext(self.group.name);
        Restangular.one("groups", $routeParams.groupId).getList("chapters").then(function (chapters) {
          self.chapters = chapters;
          updateChaptersMap();
        });
      }
    }

    var updateChaptersMap = function () {
      self.chaptersMap = {};
      _.each(self.chapters, function (chapter) {
        self.chaptersMap[chapter.id] = chapter;
      });
      var getChaptersIds = function (id) {
        if (self.chaptersMap[id].parentChapterKey === undefined) {
          return [id];
        }
        var ids = getChaptersIds(self.chaptersMap[id].parentChapterKey.id);
        ids.push(id);
        return ids;
      }
      _.each(self.chapters, function (chapter) {
        chapter.ids = getChaptersIds(chapter.id);
      });
      if (self.chapterId) {
        Title.setSubContext(self.chaptersMap[self.chapterId].name);
      }
    };

    updateGroup();

    $scope.$on("GroupsLoaded", updateGroup);

    var queryParams = {};
    if (self.year !== undefined) {
      queryParams.year = self.year;
      Title.setSubContext(self.year);
    }
    if (self.month !== undefined) {
      queryParams.month = self.month;
      Title.setSubContext(self.month + "/" + self.year);
    }
    if (self.chapterId !== undefined) {
      queryParams.chapterId = self.chapterId;
    }
    Restangular.one("groups", $routeParams.groupId).getList("entries", queryParams).then(function (entries) {
      self.entries = entries;
    });

    var editModal = $modal({
      templateUrl: "partials/entry-modal.html",
      show: false,
      scope: $scope
    });
    
    self.editEntry = function (entry) {
      editModal.$promise.then(function () {
        $scope.toEditEntry = Restangular.copy(entry);
        editModal.show();
      });
    }

  });

})();