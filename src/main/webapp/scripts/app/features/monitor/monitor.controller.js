'use strict';

angular.module('spadeApp').controller('MonitorController',
		function($rootScope, $scope, $state, $timeout, Auth) {
			$scope.user = {};
			$scope.errors = {};

			$scope.rememberMe = true;
			$scope.name = 'Guy';
		});
