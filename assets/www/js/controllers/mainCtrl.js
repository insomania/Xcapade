
myApp.controller('PreloadCtrl', function($scope, $state, $timeout, $ionicPlatform, $ionicPopup, $rootScope, $ionicLoading, $ionicDeploy, 
									$cordovaNetwork, $cordovaDevice, LocationService, localStorageService) {

	$scope.appInitSettings = function() {
		$ionicPlatform.ready(function() {
			//	check environment
			bBrowser = false;
			if(typeof(window.cordova) == "undefined") {
				bBrowser = true;
			}

			bOldPhone = false;
			if (!bBrowser && ionic.Platform.isAndroid()) {
				//		alert(JSON.stringify($cordovaDevice));
				//if(typeof(device) == "undefined" || typeof(device.version) == "undefined") {
				//  isLower = true;
				//}
				//else {
				var version = $cordovaDevice.getVersion();
				if (version.substr(0, 1) == "1" || version.substr(0, 1) == "2" || version.substr(0, 1) == "3")
					bOldPhone = true;
			}

			// Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
			// for form inputs)
			if(window.cordova) {
				if (window.cordova.plugins.Keyboard) {
					cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
				}
			}

			if (window.StatusBar) {
				// org.apache.cordova.statusbar required
				StatusBar.styleDefault();
			}

			function exitApp() {
				var confirmPopup = $ionicPopup.confirm({
				title: 'Exist',
				template: 'Are you sure?',
				okText: 'Ok',
				cancelText: 'Cancel'
				});
				confirmPopup.then(function(res) {
				if (res)
					navigator.app.exitApp();
				});
			}

			if(!localStorageService.get("location"))
				$ionicLoading.show({
					template: spinner + 'Getting current location...'
				});
			
			if(!bBrowser) {
				var type = $cordovaNetwork.getNetwork();
				// listen for Online event
				$rootScope.$on('$cordovaNetwork:online', function(event, networkState){
					var onlineState = networkState;
					//alert("Connected to network");					
					//LocationService.prepareLocating();
				});

				// listen for Offline event
				$rootScope.$on('$cordovaNetwork:offline', function(event, networkState){
					var offlineState = networkState;
					
					//	show alert
					var showPopup = function() {
						var confirmPopup = $ionicPopup.confirm({
							title: 'Alert',
							template: 'Sorry, This app can not run without internet connection.\nPlease try again after you connect to internet.',
							okText: 'Ok'
						});
						confirmPopup.then(function(res) {
							if ($cordovaNetwork.isOffline())
								showPopup();
						});
					}
				});		    	

		    	if(bOldPhone)
					setInterval(LocationService.prepareLocating, 10000);
				else {
					LocationService.prepareLocating();
				}
			}
			else {
				LocationService.prepareLocating();
			}

			bStart = true;

			//	Check Update
			if(!bBrowser) {
				console.log('Ionic Deploy: Checking for updates');
				$ionicDeploy.check().then(function(hasUpdate) {
					console.log('Ionic Deploy: Update available: ' + hasUpdate);
					//$scope.hasUpdate = hasUpdate;
					if(hasUpdate) {
						//	Update
						var confirmPopup = $ionicPopup.confirm({
							title: 'Alert',
							template: 'Update available. Do you want to download now?',
							okText: 'Ok'
						});
						confirmPopup.then(function(res) {
							if(!res) {
								return;
							}
							$ionicLoading.show({
								template: spinner + "Downloading contents..."
							});

							$ionicDeploy.download().then(function() {
								// Extract the updates
								$ionicLoading.show({
									template: spinner + "Extracting files..."
								});

								$ionicDeploy.extract().then(function() {
									$ionicLoading.show({
										template: spinner + "Reloading..."
									});
									// Load the updated version
									$ionicDeploy.load();
									$ionicLoading.hide();
								}, function(error) {
									// Error extracting
									$ionicLoading.hide();
									$ionicPopup.alert({
										title: 'Error',
										template: 'Extract Error',
										okText: 'OK'
									});
								}, function(progress) {
									// Do something with the zip extraction progress
									//$scope.extraction_progress = progress;
									console.log('extraction_progress', progress);
									$ionicLoading.show({
										template: "Extracting files...<br />" + Math.round(progress) + "% completed"
									});
								});
							}, function(err) {
								// Error downloading the updates
								$ionicLoading.hide();
								$ionicPopup.alert({
									title: 'Error',
									template: 'Download Error. ' + JSON.stringify(err),
									okText: 'OK'
								});
							}, function(progress) {
								// Do something with the download progress
								//$scope.download_progress = progress;
								console.log('download_progress', progress);
								$ionicLoading.show({
									template: "Downloading contents...<br />" + Math.round(progress) + "% completed"
								});
							});	
						});
					}
				}, function(err) {
					console.error('Ionic Deploy: Unable to check for updates', err);
				});
			}
		});
	}

	$scope.preload = function() {
		var success = function(message) {
			console.log("Preload success. --- ");
			$scope.appInitSettings();
			$state.go('app.welcome');
		}
		var failure = function(error) {
			$ionicPopup.alert({
				title: 'Fetching Error',
				template: "Can't load image assets. Please check your internet and open again.",
				okText: 'OK'
			});
			$scope.isLoadingSuccess = true;
			ionic.Platform.exitApp();
		}
		$timeout(function() {
			console.log("Preload Started. ---- ");
			cordova.AR.preload(["preload"], success, failure);
		}, 2000);
	}
	$scope.preload();
})

//	Tutorial Page Controller
.controller('TutorialCtrl', function($scope,$state,$ionicViewService,localStorageService) {
	//window.localStorage['didTutorial'] = false;// For Test
	var startApp = function() {
		//$ionicViewService.clearHistory();
		if (localStorageService.get("user")){
			$state.go('main.home');
		}
		else{
			$state.go('app.welcome');
		}
	};

	//localStorage.didTutorial = 'false';
	//if ($cordovaNetwork.isOffline() === false) {
	//  alert("Please connect to Internet.");
	//}

	if (localStorage.didTutorial === "true") {
		startApp();
	} else {
		setTimeout(function() {
		//  splashscreen is only for real device
		if (typeof(navigator.splashscreen) !== "undefined")
			navigator.splashscreen.hide();
		}, 750);
	}

	$scope.gotoMain = function() {
		localStorage.didTutorial = true;
		startApp();
	}

	$scope.slideHasChanged = function(index) {};
})

//	Welcome Page controller (including facebook/normal login buttons)
.controller('WelcomeCtrl', function($scope,$state,$ionicViewService,$ionicLoading,$ionicPopup,localStorageService,UserService,$cordovaFacebook,$ionicHistory,$timeout) {
	console.log("WelcomeCtrl Started. ---- ");
	$ionicHistory.clearCache();
	$ionicHistory.clearHistory();

	if (localStorageService.get("user")){
		$state.go('main.home');
	}
	$scope.gotoLogin = function() {
		$ionicViewService.clearHistory();
		// localStorage.didTutorial = true;
		// startApp(sMode);
		$state.go('app.login');
	}
	$scope.gotoSignUp = function() {
		$ionicViewService.clearHistory();
		$state.go('app.signup');
	}
	$scope.fbLogin = function() {
		$cordovaFacebook.login(["public_profile", "email", "user_friends"])
			.then(function(success) {
				$ionicLoading.show({
					template: spinner + 'Please wait...'
				});
				
				$cordovaFacebook.api("/me?fields=id,name,email,picture.type(large)", ["public_profile", "email", "user_friends"])
					.then(function(success) {
						// success
						$scope.user = {id: success.id, name: success.name, email: success.email, picture: success.picture.data.url};
						$cordovaFacebook.api("/me/friends?fields=id,name,email,picture.type(large)", ["public_profile", "email", "user_friends"])
							.then(function(result) {
								var friends = [];
								for(var i = 0; i < result.data.length; i++) {
									var friend = {ID: 0, FacebookID: result.data[i].id};
									friends.push(friend);
								}

								UserService.facebooklogin($scope.user).then(function(data) {
									$ionicLoading.hide();

									if(typeof(data.ID) == "undefined") {
										var alertPopup = $ionicPopup.alert({
											title: 'Error',
											template: 'An error occured.\n<br />' + data.error,
											okText: 'Ok'
										});
										return;
									}

									if(!data.Photo || data.Photo == "") {
										data.Photo = "img/no-avatar-ff.png";
									}

									localStorageService.set("user", data);
									localStorageService.set("login_auth", "facebookloginAuth");

									if(friends.length > 0) {

										UserService.getUserInformations(friends).then(function(users) {
											for(i = 0; i < users.length; i++) {
												if(users[i].Photo == "")	users[i].Photo = "img/no-avatar-ff.png";
											}
											$scope.friends = users;
											localStorageService.set("friends", users);

											$state.go("main.home");
										})
										.catch(function(err) {
											console.log("An error occured while getting informations of friends : " + JSON.stringify(err));
											$ionicLoading.hide();
										})
									}
									else {
										localStorageService.set("friends", []);
										$state.go("main.home");
									}
								})
								.catch(function(err) {
									$ionicLoading.hide();
								});
							}, function (error) {
								// error
								$ionicLoading.hide();
								console.log(JSON.stringify(error));
							});
					}, function (error) {
						// error
						$ionicLoading.hide();
						console.log(JSON.stringify(error));
					});
			}, function (error) {
				// error
				$ionicLoading.hide();
				console.log(JSON.stringify(error));
			});
	}
})
.controller('SignUpCtrl', function($scope,$state, $stateParams,$ionicLoading,$ionicSideMenuDelegate,localStorageService,UserService,$cordovaCamera,$ionicScrollDelegate,$timeout) {
	$scope.isOverScroll = false;
	$scope.signup_error = "";
	$scope.newuser = {};

	$scope.keyboardFocus = function(handleValue){
		$scope.isOverScroll = true;
		$timeout(function() {
		    $ionicScrollDelegate.scrollTop(true);
		}, 300);
	}
	$scope.keyboardBlur = function(handleValue){
		$scope.isOverScroll = false;
		$timeout(function() {
		    $ionicScrollDelegate.scrollTop(true);
		}, 300);
	}

	$scope.checkIfEnterKeyWasPressed = function($event){
		var keyCode = $event.which || $event.keyCode;
		if (keyCode === 13) {
			$scope.onBtnNextClick();
		}
	};
	$scope.onAddAvatarImg = function() {
		var options = {
			quality: 75,
			destinationType: Camera.DestinationType.FILE_URI,
			sourceType: Camera.PictureSourceType.CAMERA,
			allowEdit: false,
			encodingType: Camera.EncodingType.JPEG,
			popoverOptions: CameraPopoverOptions,
			//saveToPhotoAlbum: true,
			targetWidth: 1000,
			targetHeight: 1000
		};

		$cordovaCamera.getPicture(options)
			.then(function(imageURI) {
				
			}, function(error) {
				var alertPopup = $ionicPopup.alert({
					title: 'Error',
					template: JSON.stringify(error),
					okText: 'OK'
				});
			});
	}
	$scope.onBtnNextClick = function() {
		if(!$scope.newuser.Name || $scope.newuser.Name == "") {
			$scope.signup_error = "Please input user name.";
			return;
		}
		if(!$scope.newuser.Email || $scope.newuser.Email == "") {
			$scope.signup_error = "Please input Email.";
			return;
		}
		if($scope.newuser.Email.indexOf("@") <= 0 || $scope.newuser.Email.indexOf(".") <= 0 || $scope.newuser.Email.indexOf("@") == $scope.newuser.Email.length - 1) {
			$scope.signup_error = "Please input correct Email.";
			return;
		}
		if(!$scope.newuser.Password || !$scope.newuser.Confirm || $scope.newuser.Password == "" || $scope.newuser.Password != $scope.newuser.Confirm) {
			$scope.signup_error = "Please input correct password.";
			return;
		}

		$ionicLoading.show({
			template: 'Please wait...'
		});
		UserService.signup($scope.newuser).then(function(data) {
			$ionicLoading.hide();

			if(data.ID == "-1")
				$scope.signup_error = "This user name is already in use. Please use another.";
			else {
				if(!data.Photo || data.Photo == "") {
					data.Photo = "img/no-avatar-ff.png";
				}
				
				data.Password = $scope.newuser.Password;
				localStorageService.set("user", data);
				localStorageService.set("login_auth", "signup");
				$state.go("main.home");
			}
		}).
		catch(function(error) {
			$ionicLoading.hide();

			$scope.signup_error = "Could not connect to server.";
		});
	}

	$scope.gotoWelcome = function() {
		$state.go("app.welcome");
	}

})	
//	Normal Login Controller
.controller('LoginCtrl', function($scope,$state,$stateParams,$ionicLoading,$ionicSideMenuDelegate,localStorageService,UserService,$ionicScrollDelegate,$timeout) {
	$scope.isOverScroll = false;

	$scope.user = localStorageService.get("user") || {};

	$scope.message = "";
	if(localStorageService.get("isRememberValue") == 1){
		$scope.rememberme = true;
	}
	else{
		$scope.rememberme = false;
	}

	if($scope.rememberme){		
		$scope.user.Name = localStorageService.get("loginUserName") || "";		
		$scope.user.Password = localStorageService.get("loginUserPassword") || "";
	}

	$scope.keyboardFocus = function(handleValue){
		$scope.isOverScroll = true;
		$timeout(function() {
		    $ionicScrollDelegate.scrollTop(true);
		}, 300);
	}
	$scope.keyboardBlur = function(handleValue){
		$scope.isOverScroll = false;
		$timeout(function() {
		    $ionicScrollDelegate.scrollTop(true);
		}, 300);
	}

	$scope.isRemember = function() {
		if($scope.rememberme == false) {
            $scope.rememberme = true;
            localStorageService.set("isRememberValue", 1);00
        } else {
            $scope.rememberme = false;
            localStorageService.set("isRememberValue", 0);
        }
	}
	$scope.checkIfEnterKeyWasPressed = function($event){
		var keyCode = $event.which || $event.keyCode;
		if (keyCode === 13) {
			$scope.onLogin();
		}
	};
	$scope.onLogin = function() {
		//$state.go("main.home");
		$ionicLoading.show({
			template: 'Please wait...'
		});

		UserService.login($scope.user).then(function(data) {
			$ionicLoading.hide();

			if(typeof(data.ID) == "undefined" || data.ID == "-1")
				if(typeof(data.error) != "undefined")
					$scope.message = data.error;
				else
					$scope.message = "Login information is not correct.";
			else {
				$scope.message = "";
				if(!data.Photo || data.Photo == "") {
					data.Photo = "img/no-avatar-ff.png";
				}

				localStorageService.set("loginUserName", $scope.user.Name);
				localStorageService.set("loginUserPassword", $scope.user.Password);
				
				data.error = null;
				data.Name = $scope.user.Name;
				data.Password = $scope.user.Password;
				localStorageService.set("user", data);
				localStorageService.set("login_auth", "loggedIn");
				$state.go("main.home");

				var friends = [];
				//friends.push({ID: 0, FacebookID: "380154292195654"});
				//friends.push({ID: 0, FacebookID: "965646660135594"});
				//friends.push({ID: 0, FacebookID: "540580432747145"});
				//friends.push({ID: 0, FacebookID: "10153361392483836"});
				//friends.push({ID: 0, FacebookID: "10153427310493028"});
				//friends.push({ID: 0, FacebookID: "10206966418705440"});
				//friends.push({ID: 0, FacebookID: "10152851686952032"});
				//friends.push({ID: 0, FacebookID: "1610250899249546"});
				//friends.push({ID: 0, FacebookID: "987594934607433"});
				//friends.push({ID: 0, FacebookID: "10204830318263187"});

				if(friends.length > 0) {
					UserService.getUserInformations(friends).then(function(users) {
						// console.log(JSON.stringify(users));
						for(i = 0; i < users.length; i++) {
							if(users[i].Photo == "")	users[i].Photo = "img/no-avatar-ff.png";
						}
						$scope.friends = users;
						localStorageService.set("friends", users);

						$state.go("main.home");
					})
					.catch(function(err) {
						console.log(JSON.stringify(err));
						$ionicLoading.hide();
					})
				}
				else {
					localStorageService.set("friends", []);
					$state.go("main.home");
				}
			}
		}).
		catch(function(error) {
			$ionicLoading.hide();
			if(typeof(error.message) != "undefined") {
				$scope.message = error.message;
			}
			else
				$scope.message = "Could not connect to server.\n";
		});
	}

	$scope.onBtnForgotClick = function() {
		$state.go("app.forgetpass");
	}

	$scope.gotoWelcome = function() {
		$state.go("app.welcome");
	}

})
.controller('ForgetPassCtrl', function($scope,$state,$cordovaFacebook,$ionicSideMenuDelegate,$window,localStorageService,$ionicPopup,$ionicLoading) {
	$scope.message = "";

	$scope.gotoWelcome = function() {
		$state.go("app.login");
	}
	$scope.checkIfEnterKeyWasPressed = function($event){
		var keyCode = $event.which || $event.keyCode;
		if (keyCode === 13) {
			$scope.onLogin();
		}
	};
	$scope.onSubmit = function(){
		//$state.go("main.home");
		$ionicLoading.show({
			template: 'Please wait...'
		});
		UserService.forgotpass($scope.user.email).then(function(data) {
			$ionicLoading.hide();
			if(typeof(data.ID) == "undefined" || data.ID == "-1"){
				if(typeof(data.error) != "undefined")
					$scope.message = data.error;
				else
					$scope.message = "Can't find that email, sorry.";
			}
			else {
				var alertPopup = $ionicPopup.alert({
					title: 'Successfully Sent',
					template: "Check your email for a link to reset your password. If it doesn't appear within a few minutes, check your spam folder.",
					okText: 'OK'
				});
				$state.go("main.login");
			}
		}).
		catch(function(error) {
			$ionicLoading.hide();
			if(typeof(error.message) != "undefined") {
				$scope.message = error.message;
			}
			else
				$scope.message = "Could not connect to server.\n";
		});
	}
})
//	Main Controller including Left Menu 
.controller('MainCtrl', function($scope,$state,$cordovaFacebook,$cordovaCamera,$ionicSideMenuDelegate,$stateParams,localStorageService,$ionicPopup,$ionicLoading,$ionicViewService,UserService,LocationService,$timeout) {

	$scope.user = localStorageService.get("user") || {};
	$scope.spots = localStorageService.get("spots") || [];
	$scope.routes = localStorageService.get("routes") || [];
	$scope.spotName = "";
	$scope.foundroutescount = 0;

	if(!$scope.user || $scope.user.Name == "" || $scope.user.Password == "")
		$state.go("app.login");

	$scope.toggleMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}

	$scope.logout = function() {
		localStorageService.remove("user");
		localStorageService.remove("mode");
		localStorageService.remove("login_auth");
		localStorageService.remove("friends");
		localStorageService.remove("spots");
		localStorageService.remove("routes");
		localStorageService.remove("foundspots");
		localStorageService.remove("current_spot");
		
		if(typeof(facebookConnectPlugin) != "undefined")
			$cordovaFacebook.logout()
				.then(function(success) {
				// success
				}, function (error) {
				// error
				});

		$state.go("app.welcome");
	}

	$scope.onHelp = function() {
		localStorage.didTutorial = false;
		$state.go("app.tutorial");
	}

	$scope.onHome = function() {
		//$ionicViewService.clearHistory();
		$ionicSideMenuDelegate.toggleLeft();
		$state.go("main.home");
	}
	var update = function() {
		$scope.spots = localStorageService.get("spots") || [];
		$scope.routes = localStorageService.get("routes") || [];
		$scope.foundspots = localStorageService.get("foundspots") || [];

		$scope.foundroutescount = 0;
		for(var i = 0; i < $scope.routes.length; i++) {
			if($scope.routes[i].Found == $scope.routes[i].spots.length)
				$scope.foundroutescount++;
		}

		currentUserUpdate();

	}
	setInterval(update, 2000);

	$scope.onGetCoins = function(){
		$ionicSideMenuDelegate.toggleLeft();
		$state.go("main.coinshop");
	}

	var currentUserUpdate = function(){
		var arrayUsers = [];
		arrayUsers.push({ID: $scope.user.ID});

	  UserService.getUserInformations(arrayUsers).then(function(users) {
			// console.log(JSON.stringify(users));
			if (typeof users[0].Photo == "undefined") {
				$ionicPopup.alert({
					title: 'Error',
					template: "An authentication error has occurred. Please login again.",
					okText: 'OK'
				});
				$scope.logout();
			}
			else {
				if (users[0].Photo == null) {
					users[0].Photo = "img/no-avatar-ff.png";
				}
				$scope.user = users[0];
				localStorageService.set("user", users[0]);
			}
		})
	}
	$scope.onAddSpot = function() {
		
		var success = function(message) {	    
			$ionicSideMenuDelegate.toggleLeft();
			
			console.log("Current View name: " + $state.current.name);
			if($state.current.name != "main.home") {
				$state.go("main.home");
			}
			else {
				$scope.$broadcast("reloadSpotData");
			}
	  }
	  var failure = function(error) {
	    $ionicPopup.alert({
				title: 'Error',
				template: "Can't connect to the camera.",
				okText: 'OK'
			});
    }

    var pos = LocationService.getCurrentPosition();
    spot = [$scope.user.ID, pos.latitude, pos.longitude];

		cordova.AR.creation(spot, success, failure);
	}
})


