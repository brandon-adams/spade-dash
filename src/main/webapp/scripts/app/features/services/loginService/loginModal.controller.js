'use strict';

angular.module('spadeApp')
    .controller('LoginModalController', function ($scope) {

  this.cancel = $scope.$dismiss;
  $scope.email = '';
  $scope.password = '';
//  this.submit
  
  $scope.login = function (email, password) {
	 console.log('email: ' + email + '\npassword ' + password);
	 this.cancel = $scope.$dismiss;
//	 		.then(function (user) {
//	 			$scope.$close(user);
//    });
  };

});