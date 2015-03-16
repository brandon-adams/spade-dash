'use strict';

angular.module('spadeApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
