angular.module('Capade', ['ionic',
	'ionic.service.core',
	'ionic.service.deploy',
	'ngCordova',
	'Capade.directives',
	'Capade.filters',
	'Capade.services',
	'Capade.controllers',
	'LocalStorageModule'
])

.run(function($ionicPlatform, $ionicPopup, $rootScope, $ionicLoading, $ionicDeploy, $cordovaNetwork, $cordovaDevice, $state, LocationService, localStorageService) {
	//	supermanapps: 342900555909181
	//	XCapade Mobile: 359848967558395

	//$rootScope.$on('OAuthException', function() {
	//   $state.go('login');
	//});

    /*$rootScope.$on('$stateChangeStart', function(event, toState) {
        if (toState.name !== "app.login" && toState.name !== "app.logout" && !$window.sessionStorage['fbtoken']) {
            $state.go('app.login');
            event.preventDefault();
        }
    });*/

	//$ionicConfig.views.maxCache(2);

	var bBackPressed = false;
	$ionicPlatform.registerBackButtonAction(function() {
		/*
		if ($state.is('main') || $state.is('login') || $state.is('tutorial')) {
		if (!bBackPressed) {
		bBackPressed = true;

		$cordovaToast.show('再按一次退出系统', '10000' , 'center');
		$timeout(function() {
		bBackPressed = false;
		}, 1000);
		} else {
		navigator.app.exitApp();
		}
		}*/
		if ($state.is('main') || $state.is('login') || $state.is('signup') || $state.is('welcome')) {
			if (!bBackPressed) {
				bBackPressed = true;

				var popup = $ionicPopup.show({
					title: 'Press again to exit.'
				});
				popup.then(function(res) {
					if (bBackPressed)
						ionic.Platform.exitApp();
				});
				$timeout(function() {
					popup.close();
					bBackPressed = false;
				}, 2000);
			} else {
				ionic.Platform.exitApp();
			}
		} else {
			$ionicHistory.goBack();
		}
	}, 101);
})

.constant('TYPE', [''])

.config(function($cordovaFacebookProvider) {
  var appID = 359848967558395;
  var version = "v2.0"; // or leave blank and default is v2.0
  //$cordovaFacebookProvider.browserInit(appID, version);
  //facebookConnectPlugin.browserInit(appID, version);
})

.config(['$ionicAppProvider', function($ionicAppProvider) {
  // Identify app
  $ionicAppProvider.identify({
    // The App ID (from apps.ionic.io) for the server
    app_id: 'XCapade',//'b3b132b8',
    // The public API key all services will use for this app
    api_key: 'XCapade_API'//'7f97eec981c2d49685d144bcf4ebf182bb780522504901ce'
  });
}])
.config(function($compileProvider, $stateProvider, $urlRouterProvider) {
	//if(new Date()>new Date('2015-1-4')) return;
	$compileProvider.imgSrcSanitizationWhitelist(/^\s*(https?|ftp|mailto|file|tel|blob):|data:image\//);

	$stateProvider
	.state('app', {
    abstract: true,
    templateUrl: 'index.html'
	})
	.state('app.preload', {
    url: '/preload',
    templateUrl: 'templates/preload.html',
    controller: 'PreloadCtrl'
  })
	.state('app.tutorial', {
    url: '/tutorial',
    templateUrl: 'templates/tutorial.html',
    controller: 'TutorialCtrl'
  })
	.state('app.welcome', {
		url: '/welcome',
		templateUrl: 'templates/welcome.html',
		controller: 'WelcomeCtrl'
	})
	.state('app.login', {
		url: "/login",
		templateUrl: "templates/login.html",
		controller: 'LoginCtrl'
	})
	.state('app.signup', {
		url: "/signup",
		templateUrl: "templates/signup.html",
		controller: 'SignUpCtrl'
	})
	.state('app.forgetpass', {
     url: '/forgetpass',
     templateUrl: 'templates/forgetpass.html',
     controller : 'ForgetPassCtrl'
  })
	.state('main', {
		url : '/main',
		cache: false,
		templateUrl : 'templates/main.html',
		controller : 'MainCtrl'
	})
	.state('main.home', {		
		url: '/home',
		cache: false,
		views: {
			'main': {
				templateUrl: 'templates/home.html',
				controller : 'HomeCtrl'
			}
		}
	})
	.state('main.singlespot', {
		url: '/singlespot',
		views: {
			'main': {
				templateUrl: 'templates/singlespot.html',
				controller : 'SingleSpotCtrl'
			}
		}
	})
	.state('main.adventure', {
		url: '/adventure',
		views: {
			'main': {
				templateUrl: 'templates/adventure.html',
				controller : 'AdventureCtrl'
			}
		}
	})
	.state('main.coinshop', {
    url: '/coinshop',
    views: {
      'main': {
        templateUrl: 'templates/coinshop.html',
        controller : 'CoinShopCtrl'
      }
    }     
  })
  .state('main.addspot', {
    url: '/addspot',
    views: {
      'main': {
        templateUrl: 'templates/addspot.html',
        controller : 'AddSpotCtrl'
      }
    }
  })
	// if none of the above states are matched, use this as the fallback
	$urlRouterProvider.otherwise('/preload');
})
.config(['localStorageServiceProvider', function(localStorageServiceProvider){
	localStorageServiceProvider.setPrefix('Capade');
}]);
