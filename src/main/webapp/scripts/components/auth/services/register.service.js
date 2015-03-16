'use strict';

angular.module('spadeApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


