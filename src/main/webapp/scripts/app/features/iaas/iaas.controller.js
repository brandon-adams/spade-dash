'use strict';

angular.module('spadeApp')
     .controller('IaasController', function ($modal, $scope) {
        
    	
    	 var app = this;

        app.closeAlert = function () {
            app.reason = null;
        };

        app.open = function () {
            var modalInstance = $modal.open({
                templateUrl: 'scripts/app/features/iaas/iaas.html',
                controller: 'ModalCtrl',
                controllerAs: 'modal'
            });

            modalInstance.result
                .then(function (data) {
                    app.closeAlert();
                    app.summary = data;
                }, function (reason) {
                    app.reason = reason;
                });
        };
    })
    .controller('ModalCtrl', function ($modalInstance,$scope,$mdDialog,$http) {
    	
    	$scope.applications = 
        {
          "api": "v0.0.4",
          "time": 1426011638988,
          "label": "extra",
          "items": [
            {
              "image": "sewatech\/modcluster",
              "os": "ubuntu",
              "app": "apache"
            },
            {
              "image": "bradams\/devops:nginx-ubuntu",
              "os": "ubuntu",
              "app": "nginx"
            },
            {
              "image": "bradams\/devops:wildfly-ubuntu",
              "os": "ubuntu",
              "app": "wildfly"
            },
            {
              "image": "bradams\/devops:tomcat-ubuntu",
              "os": "ubuntu",
              "app": "tomcat"
            },
            {
              "image": "partlab\/ubuntu-mongodb",
              "os": "ubuntu",
              "app": "mongodb"
            },
            {
              "image": "bradams\/devops:mysql-ubuntu",
              "os": "ubuntu",
              "app": "mysql"
            },
            {
              "image": "bradams\/devops:apache-fedora",
              "os": "fedora",
              "app": "apache"
            },
            {
              "image": "bradams\/devops:nginx-fedora",
              "os": "fedora",
              "app": "nginx"
            },
            {
              "image": "bradams\/devops:cluster",
              "os": "fedora",
              "app": "wildfly"
            },
            {
              "image": "bradams\/devops:tomcat-fedora",
              "os": "fedora",
              "app": "tomcat"
            },
            {
              "image": "bradams\/devops:mongodb-fedora",
              "os": "fedora",
              "app": "mongodb"
            },
            {
              "image": "jdeathe\/centos-ssh-mysql",
              "os": "fedora",
              "app": "mysql"
            }
          ]
        };

    	var appFilter = function(){
    		var n = {},uniqueApps = [];
//    		var defaultApp = {
//    				"image": "",
//    	              "os": "",
//    	              "app": "Select an Application"	
//    		}
//    		uniqueApps.push(defaultApp);
    		
    		for(var i = 0; i < $scope.applications.items.length; i++){
    			if(!n[$scope.applications.items[i].app]){
    				n[$scope.applications.items[i].app] = true;
    				uniqueApps.push($scope.applications.items[i]);
    			}
    		}
    		
    		
    		return uniqueApps;
    	}
    	
    	$scope.uniqueApps = appFilter();
    	
    	for(var i = 0; i < $scope.uniqueApps.length;i++){
    		console.log($scope.uniqueApps[i]);
    	}
//    	console.log($scope.uniqueApps);

    	$scope.isDisabled = true;
    	
    	$scope.defaultPod = {
    			name : 'Not Yet Specified',
    			os: ' None Selected',
              	app : 'None Selected',
              	replicas : 0
              };
    	
    	$scope.pod = {
    			name : $scope.defaultPod.name,
    			os: $scope.defaultPod.os,
              	app : $scope.defaultPod.app,
              	replicas : $scope.defaultPod.replicas
              };
     	 
    	
     	 $scope.launch = function(pod){
//     		{ "name": "demo-app", "os": "ubuntu", "app": "mongodb", "replicas": 1 }

//      		console.log("Pod Stats: " + pod.osName + " " +  pod.selectedApp + " " + pod.appName + " " + pod.replicaCount);
     		 console.log(pod);
     		 
     		 $http.post("http://192.168.0.95:8080/spade/api/demo/env", pod)
     		 	.success(function(data){
     		 		console.log("success data returned ====> " + data);
     		 });
     		 
     		 
     		 
     		 
//     		$http.get("http://192.168.0.95:8080/spade/api/proj")
//			.success(function(data) {
//					console.log(data);
//					$scope.info = data;
//				})
//				
//			.error(function(data, status, headers, config) {
//				$scope.info = data;
//				$scope.projects = data.items;
//
//				console.log(data.items);
//				console.log(data);
//				console.log(status);
//				console.log(headers);
//				console.log(config);
//		});
     		 
     		 
     		 
      	}
     	 
     	$scope.alert = '';
     	  $scope.showAlert = function() {
     	    $mdDialog.show(
     	      $mdDialog.alert()
     	        .title('This is an alert title')
     	        .content('You can specify some description text in here.')
     	        .ariaLabel('Password notification')
     	        .ok('Got it!')
//     	        .targetEvent(ev)
     	    );
     	  };
     	  
    	var modal = this;

        modal.steps = ['one', 'two', 'three'];
        modal.step = 0;
//        modal.wizard = {tacos: 2};

        modal.isFirstStep = function () {
            return modal.step === 0;
        };

        modal.isLastStep = function () {
            return modal.step === (modal.steps.length - 1);
        };

        modal.isCurrentStep = function (step) {
            return modal.step === step;
        };

        modal.setCurrentStep = function (step) {
            modal.step = step;
        };

        modal.getCurrentStep = function () {
            return modal.steps[modal.step];
        };

        modal.getNextLabel = function () {
        	if(modal.isLastStep()){
        		$scope.isDisabled = !modal.launchReady();
        		return 'Click Above To Launch Your Pod';
        	} else{
        		return 'Next'
        	}
//            return (modal.isLastStep()) ? 'Launch' : 'Next';
        };
        
        modal.launchReady = function(){
        	if(angular.equals($scope.pod.os,$scope.defaultPod.os) ||
        			angular.equals($scope.pod.app,$scope.defaultPod.app) ||
        			angular.equals($scope.pod.name,$scope.defaultPod.name) ||
        			angular.equals($scope.pod.replicas,$scope.defaultPod.replicas ||
        			$scope.pod.replicas < 0		) 
        	){
        		return false;
        	} else {
        		return true;
        	}
        }

        modal.handlePrevious = function () {
            modal.step -= (modal.isFirstStep()) ? 0 : 1;
        };

        modal.handleNext = function () {
            if (modal.isLastStep()) {
                $modalInstance.close(modal.wizard);
            } else {
                modal.step += 1;
            }
        };

        modal.dismiss = function(reason) {
            $modalInstance.dismiss(reason);
        };
    });