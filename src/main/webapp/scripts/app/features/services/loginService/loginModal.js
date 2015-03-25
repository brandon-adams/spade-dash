angular.module('spadeApp').service('loginModal', function ($modal, $rootScope) {

  function assignCurrentUser (user) {
    $rootScope.currentUser = user;
    return user;
  }

  return function() {
    var instance = $modal.open({
      templateUrl: 'scripts/app/account/login/login.html',
      controller: 'LoginController',
      controllerAs: 'LoginController'
    })

    return instance.result.then(assignCurrentUser);
  };

});