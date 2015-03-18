'use strict';

angular.module('spadeApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('stats', {
                parent: 'features',
                url: '/stats',
                data: {
                    roles: [], 
                    pageTitle: 'stats.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/features/stats/mesosStats.html',
                        controller: 'MesosStatsController'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('stats');
                        return $translate.refresh();
                    }]
                }
            });
    });
