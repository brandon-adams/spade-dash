'use strict';

angular.module('spadeApp')
    .controller('NavbarController', function ($scope, $location, $state, Auth, Principal, $modal) {
    	$scope.app = this;
    	
    	$scope.app.closeAlert = function(){
    		$scope.app.reason = null;
    	}
    	
    	$scope.app.open = function(){
    		var modalInstance = $modal.open({
    			templateUrl: 'scripts/app/features/iaas/iaas.html',
    			controller: 'ModalCtrl',
    			controllerAs: 'modal'
    		});
    		
    		modalInstance.result
            .then(function (data) {
            	$scope.app.closeAlert();
            	$scope.app.summary = data;
            }, function (reason) {
            	$scope.app.reason = reason;
            });
    		
    	}
    	
    	
        $scope.isAuthenticated = Principal.isAuthenticated;
        $scope.isInRole = Principal.isInRole;
        $scope.$state = $state;

        $scope.logout = function () {
            Auth.logout();
            $state.go('home');
        };
    })
