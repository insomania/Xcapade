angular.module('Capade.services', [])

.factory('UserService', function($q, $http, $ionicPopup) {
	return {
		login: function(user) {
			var q = $q.defer();
			if (!user || !user.Name || !user.Password)
				q.reject({
					status: 'error',
					message: 'Please input User Name and Password correctly'
				});
			$http.post(server, {
				mode: "login",
				user: user
			})
			.success(function(data) {
				q.resolve(data);
			})
			.error(function(err, status, headers, config) {
				console.log("Error:" + JSON.stringify(err));
				console.log("status:" + JSON.stringify(status));
				console.log("headers:" + JSON.stringify(headers));
				console.log("config:" + JSON.stringify(config));

				var alertPopup = $ionicPopup.alert({
					title: 'Error',
					template: "Could not connect to server.\n",
					okText: 'OK'
				});

				q.reject(err);
			});
			return q.promise;
		},
		facebooklogin: function(user) {
			var q = $q.defer();
			$http.post(server, {
				mode: "facebooklogin",
				user: user
			})
			.success(function(data) {
				q.resolve(data);
			})
			.error(function(err) {
				q.reject(err);
			})
			return q.promise;
		},
		signup: function(user) {
			var q = $q.defer();
			$http.post(server, {
				mode: "signup",
				user: user
			})
			.success(function(data) {
				q.resolve(data);
			})
			.error(function(err) {
				q.reject(err);
			})
			return q.promise;
		},
		forgotpass: function(email){
			var q = $q.defer();
			$http.post(server, {
				mode: "forgotpass",
				emailTxt: email
			})
			.success(function(data) {
				q.resolve(data);
			})
			.error(function(err, status, headers, config) {
				q.reject(err);
			});
			return q.promise;
		},
		getUserInformations: function(users) {
			var q = $q.defer();
			$http.post(server, {
				mode: "getuserinformation",
				users: users
			})
				.success(function(data) {
					q.resolve(data);
				})
				.error(function(err) {
					q.reject(err);
				})
			return q.promise;
		}
	}
})

.factory('LocationService', function($cordovaGeolocation, $ionicPopup, $timeout, $ionicLoading, $cordovaDevice, localStorageService) {
	var temppos = {
		latitude: 37.618039,
		longitude: -122.081451
	};
	return {
		prepareLocating: function() {
			//  location variable : 0 => should prepare, 1 => in getting, 2 => GPS Setting(or other errors), {latitude: ..., longitude: ...}			
			if(bBrowser || !bOldPhone) {
				localStorageService.set('locationflag', 1); //  set flag to running

				//self.locate()
        $cordovaGeolocation
					.getCurrentPosition({
						maximumAge: 0,
						timeout: 30000,
						enableHighAccuracy: false
					})
					.then(function(position) {

						//alert(position.coords.latitude + ", " + position.coords.longitude);

						var pos = {
							latitude: position.coords.latitude,
							longitude: position.coords.longitude
						}

						localStorageService.set('location', pos);
						localStorageService.set('locationflag', 10);

						glocation = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);

						$ionicLoading.hide();
					}, function(error) {
						if(bBrowser) {
							console.log("Location Service Failed");
							localStorageService.set("locationflag", 10)
							localStorageService.set('location', temppos);

							glocation = new google.maps.LatLng(temppos.latitude, temppos.longitude);
						}
						else {
							localStorageService.set('locationflag', 2);
							if(typeof(error.message) != "undefined") {
								$ionicPopup.alert({
									title: 'Error',
									template: "Location Service Failed\nReason: " + error.message,
									okText: 'OK'
								});

								//	if(error.code == 3)	//	timeout
							}
							else {
								$ionicPopup.alert({
									title: 'Error',
									template: "Location Service Failed\nReason: " + JSON.stringify(error),
									okText: 'OK'
								});
							}
						}

						$ionicLoading.hide();
					});
				
				var watchOptions = {
					frequency : 5000,
					timeout : 30000,
					enableHighAccuracy: false // may cause errors if true
				};

				var watch = $cordovaGeolocation.watchPosition(watchOptions);

				watch.promise.then(null, function(err) {}, function(position) {
					var pos = {
						latitude: position.coords.latitude,
						longitude: position.coords.longitude
					}
					localStorageService.set('location', pos);
					localStorageService.set('locationflag', 10);

					glocation = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
				});
			}
			else { 
				if (bStart) {
					$ionicPopup.alert({
						title: 'Warning',
						template: "This is an old phone. Location would be wrong.",
						okText: 'OK'
					});
					bStart = false;
				}
				var noop = function() {};
				localStorageService.set('locationflag', 1); //  getting
				window.locationService.getCurrentPosition(function(pos) {
					window.locationService.stop(noop, noop);
					localStorageService.set('location', pos.coords);
					localStorageService.set('locationflag', 10);

					glocation = new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude);

					$ionicLoading.hide();
				}, function(e) {
					window.locationService.stop(noop, noop);
					localStorageService.set('location', temppos);

					glocation = new google.maps.LatLng(temppos.latitude, temppos.longitude);

					$ionicLoading.hide();
				});
			}
		},
		getCurrentPosition: function() {

			$cordovaGeolocation.getCurrentPosition({
				maximumAge: 0,
				timeout: 30000,
				enableHighAccuracy: false
			})
			.then(function(position) {
				var pos = {
					latitude: position.coords.latitude,
					longitude: position.coords.longitude
				}
				localStorageService.set('location', pos);
				localStorageService.set('locationflag', 10);
			});

			var pos = localStorageService.get("location");
			var locationflag = localStorageService.get("locationflag");

			if (locationflag == 2) {
				console.log("Location Error");

				if(!bBrowser) {
					if (ionic.Platform.isIOS()) {
						if(!bGPSAlert) {
							bGPSAlert = true;
							var message = "GPS Setting is turned off. Please turn on GPS Settings and click OK."
							var confirmPopup = $ionicPopup.confirm({
								title: 'Error',
								template: message,
								okText: 'Ok' //,
								//  cancelText: '取消'
							});
							confirmPopup.then(function(res) {
								bGPSAlert = false;
							});

							pos = temppos;
						}
					} else {
						//  use temporary position
						//alert("Temporary position as Location Error");
						pos = temppos;
					}
				}
			}

			if(!pos || typeof(pos.latitude) == "undefined")
				pos = temppos;
			return pos;
		},
		rad: function(x) {
			return x * Math.PI / 180;
		},
		getDistance: function(p1, p2) {
			var R = 6378137; // Earthâ€™s mean radius in meter
			var dLat = this.rad(p2.lat() - p1.lat());
			var dLong = this.rad(p2.lng() - p1.lng());
			var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
				Math.cos(this.rad(p1.lat())) * Math.cos(this.rad(p2.lat())) *
				Math.sin(dLong / 2) * Math.sin(dLong / 2);
			var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			var d = R * c;
			return d; // returns the distance in meter
		}
	}
})

.factory('SpotService', function($q, $http, $state) {
	return {
		getSpots: function(type, loc, auth, user) { //  qr : 0, free : 1, all : -1
			var q = $q.defer();
			$http.post(server, {
				mode: "getspot",
				type: type,
				location: loc,
				auth: auth,
				user: user
			})
			.success(function(data) {
				if (data.error && data.error == "Authorization failed") {
					//$state.go("welcome");
					q.reject(data['spots']);
				}
				q.resolve(data['spots']);
			})
			.error(function(err) {
				q.reject(err);
			})
			return q.promise;
		},
		getFoundSpots: function() {
			var q = $q.defer();
			$http.post(server, {
				mode: "getfoundspot"
			})
			.success(function(data) {
				if (data.error && data.error == "Authorization failed") {
					//$state.go("welcome");
					q.reject(data);
				}

				q.resolve(data);
			})
			.error(function(err) {
				q.reject(err);
			})
			return q.promise;
		},
		reportSpot: function(id, content) {
			var q = $q.defer();
			$http.post(server, {
				mode: "reportspot",
				id: id,
				content: content
			})
			.success(function(data) {
				if (data.error && data.error == "Authorization failed") {
					//$state.go("welcome");
					q.reject(data);
				}

				q.resolve(data);
			})
			.error(function(err) {
				q.reject(err);
			})
			return q.promise;
		},
		revealSecret: function(id, coins, spotId){
			var q = $q.defer();
			$http.post(server, {
				mode: "revealsecret",
				id: id,
				coins: coins,
				spotId: spotId
			})
			.success(function(data) {
				if (data.error && data.error == "Authorization failed") {
					//$state.go("welcome");
					q.reject(data);
				}

				q.resolve(data);
			})
			.error(function(err) {
				q.reject(err);
			})
			return q.promise;
		}
	}
})

.factory('RouteService', function($q, $http, $state) {
	return {
		getRoutes: function(loc) {
			var q = $q.defer();
			$http.post(server, {
				mode: "getroute",
				location: loc
			})
			.success(function(data) {
				if (data.error && data.error == "Authorization failed") {
					//$state.go("welcome");
					q.reject(data);
				}

				q.resolve(data);
			})
			.error(function(err) {
				q.reject(err);
			})
			return q.promise;
		},
		reportSpot: function(id, content) {
			var q = $q.defer();
			$http.post(server, {
				mode: "reportspot",
				id: id,
				content: content
			})
			.success(function(data) {
				if (data.error && data.error == "Authorization failed") {
					//$state.go("welcome");
					q.reject(data);
				}

				q.resolve(data);
			})
			.error(function(err) {
				q.reject(err);
			})
			return q.promise;
		}
	}
})
.factory('AWSS3Service', function($q, $http, $ionicPopup) {
	AWS.config.accessKeyId = AWS_ACCESS_KEY_ID;
	AWS.config.secretAccessKey = AWS_SECRET_ACCESS_KEY;
	AWS.config.region = AWS_REGION;
		
	return {
		getSceneImagesListObjects: function(prefix){
			var q = $q.defer();
			var params = { 
				Bucket: AWS_BUCKET_NAME,
				Delimiter: '/',
				Prefix: prefix
			}
			var bucket = new AWS.S3({ params: params });
	    	bucket.listObjects(function(err, data) {
	    		if (err){
	    			q.reject(data);
	    		} else{
	    			q.resolve(data);
	    		}
	    	});

	    	return q.promise;
		},
		getSceneImageObject: function(prefix, key){
			var q = $q.defer();
			var params = { 
				Bucket: AWS_BUCKET_NAME,
				Delimiter: '/',
				Prefix: prefix
			}
			var bucket = new AWS.S3({ params: params });
			bucket.getObject({Key: key},function(err,file){
				if (err){
	    			q.reject(err);
	    		} else{
	    			q.resolve(file);
	    		}
			});

			return q.promise;
		},
		getPublicSceneImageURL: function(key){
			var params = { 
				Bucket: AWS_BUCKET_NAME,
				Key: key
			}
			// create the AWS.Request object
		    var bucket = new AWS.S3({ params: params });
			return bucket.getSignedUrl('getObject', params);
		}
	}
})
.factory('IAPService', function($q, $http, $ionicPopup) {
	return {
		registerPurchase: function(userid, type){
			if(type == "goldenMember") {
				coins = coinsNumber0;
			}
			else if (type == "coins20") {
				coins = coinsNumber1;
			}
			else if (type == "coin50") {
				coins = coinsNumber2;
			}
			else if (type == "coin80") {
				coins = coinsNumber3;
			}
			var q = $q.defer();			
			$http.post(server, {
				mode: "coinpurchase",
				userId: userid,
				type: type,
				coins: coins
			})
			.success(function(data) {
				q.resolve(data);
			})
			.error(function(err, status, headers, config) {
				console.log("Error:" + JSON.stringify(err));
				console.log("status:" + JSON.stringify(status));
				console.log("headers:" + JSON.stringify(headers));
				console.log("config:" + JSON.stringify(config));

				var alertPopup = $ionicPopup.alert({
					title: 'Error',
					template: "Could not connect to server.\n",
					okText: 'OK'
				});

				q.reject(err);
			});
			return q.promise;
		}
	}
});



