
myApp.controller('SingleSpotCtrl', function($scope,$state,$ionicLoading,$ionicModal,$ionicPopup,$cordovaBarcodeScanner,$timeout,$ionicSideMenuDelegate,$compile,localStorageService,SpotService,LocationService,AWSS3Service,$rootScope,$ionicSlideBoxDelegate,$cordovaFacebook) {
	var spots = localStorageService.get("spots") || [];
	var selected = localStorageService.get("current_spot");
	$scope.user = localStorageService.get("user") || {};


	$scope.isRevealSecret = false;
	$scope.sceneImageClass_loading = true;

	//	limit to qr spots
	$scope.spots = [];
	for(var i = 0; i < spots.length; i++) {
		if(spots[i].Type == 0)
			$scope.spots.push(spots[i]);
	}

	$scope.mode = localStorageService.get("mode");
	$scope.foundspots = localStorageService.get("foundspots") || [];

	var prevSel = -1;

	$scope.toggleMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}
	$scope.onShare = function() {
		$ionicLoading.show({
			template: spinner + 'Please wait...'
		});

		facebookConnectPlugin.getLoginStatus(
			function(success) {
				var postImg = AWSS3Service.getPublicSceneImageURL($scope.spot.SceneImageURLList[0]);

				if(success.status == "connected"){
					$scope.showFBDialog(postImg);
				}
				else{
					$cordovaFacebook.login(["public_profile", "email", "user_friends"])
					.then(function(success) {
						$scope.showFBDialog(postImg);
					}, function (error) {
						// error
						$ionicLoading.hide();
					});
				}
			}
		);
	}

	$scope.showFBDialog = function(postImg){
		$cordovaFacebook.showDialog({
	        method: "feed",
	        picture: postImg,
	        link: "http://app.xcapade.co/",
	        name: $scope.spot.Name,
	        message:'My scene photo',    
	        caption: 'Posting a scene Photo from xcapade',
	        description: $scope.spot.Description
	    })
		.then(function(success) {
			$ionicLoading.hide();
			$ionicPopup.alert({
				title: 'Congratulation',
				template: "The scene photo published on Facebook Wall successfully.",
				okText: 'OK'
			});
		}, function (error) {
			console.log(JSON.stringify(error));
			$ionicLoading.hide();

			if(error.errorCode != "4201"){
				$ionicPopup.alert({
					title: 'Connection Error',
					template: "Publishing on Facebook failed. Please check your network again.",
					okText: 'OK'
				});
			}			

		});
	}

	$scope.onGetToRevealSecret = function(){
		//----------------------
		$ionicPopup.alert({
			template: "Please get closer to reveal the secret.<br>",
			okText: 'OK'
		});
	}
	$scope.onPayToRevealSecret = function(state){
		var confirmPopup = $ionicPopup.confirm({
			template: "Do you want to reveal the secret really?",
		});
		confirmPopup.then(function(res) {
			if(res) {
				$ionicLoading.show({
					template: 'Please wait...'
				});

				if(state == "pay"){
					SpotService.revealSecret($scope.user.ID, $scope.spot.Coin, $scope.spot.ID).then(function(data) {
						$ionicLoading.hide();	
						if(!data.success) {
							$ionicPopup.alert({
								template: "You don't have enough coins to reveal the secret. Please purchase more coins.",
								okText: 'OK'
							});
						}
						else{
							$scope.spot.IsFree = 2;

							var confirmPopup = $ionicPopup.confirm({
								template: "Purchased successfully. Please click 'Ok' to continue.",
							});

							confirmPopup.then(function(res) {
								if(res) {									
									playSceneInit();
								}
							});

						}		
					});
				}
				else{
					$ionicLoading.hide();
					playSceneInit();
				}				
			}
		});
		
		// Please add your code for AR Playback --- mode:"single"
		var playSceneInit = function(){
			var success = function(message) {
	        console.log("AR was resolved successfully.");
	        SpotService.getFoundSpots().then(function(data) {
						$scope.foundspots = data;
						console.log("found spots", data);
						localStorageService.set("foundspots", data);
						$scope.mapInitialize();
					});

	    }
		  var failure = function() {
		    $ionicPopup.alert({
					title: 'Error',
					template: "Can't connect to the camera.",
					okText: 'OK'
				});
		  }

		  var spot = [$scope.user.ID, $scope.spot.ID, $scope.spot.Name, $scope.spot.Puzzle, $scope.spot.Puzzle_Hint, $scope.spot.Puzzle_Answer];
		  cordova.AR.playback(spot, success, failure);
		}
	}
	// $scope.$on("toGoHomePage", function () {
	// 	$state.go("main.home");
	// });
	var updateCurrentPosition = function() {
		var pos = LocationService.getCurrentPosition();
		$scope.selfMarker.setPosition(new google.maps.LatLng(pos.latitude, pos.longitude));
		$scope.calculateDistance();
		$scope.$apply();
	}

	var attachevent = function(index) {
		google.maps.event.addListener($scope.spots[index].marker, 'click', function() {
			var icon = "images/red_spot.png";
			if(prevSel >= 0) {
				$scope.spots[prevSel].infowindow.close();
				if($scope.spots[prevSel].Found)
					icon = "images/red_spot_finish.png";
				$scope.spots[prevSel].marker.setIcon(icon);
			}

			icon = "images/red_spot_select.png";
			if($scope.spots[index].Found)
				icon = "images/red_spot_select_finish.png";
			$scope.spot = $scope.spots[index];
			$scope.spots[index].infowindow.open(map, $scope.spots[index].marker);
			$scope.spots[index].marker.setIcon(icon);
			prevSel = index;

			$scope.sceneImageClass_loading = true;
			if($scope.spot.ID != null){
				$scope.getSceneImage();
			}
			else{
				$scope.sceneImageClass_noImage = true;
				$scope.sceneImageClass_loading = false;
			}

			$scope.calculateDistance();
			$scope.$apply();

		});
	}

	$scope.mapInitialize = function() {
		//console.log("SingleSpot Initialize");
		prevSel = selected;
		var pos = LocationService.getCurrentPosition();
		var myLatlng = new google.maps.LatLng(pos.latitude, pos.longitude);
		if(selected >= 0) {
			$scope.spot = $scope.spots[selected];			
			pos = {latitude: $scope.spot.Latitude, longitude: $scope.spot.Longitude};
		}
		pos = new google.maps.LatLng(pos.latitude, pos.longitude);

		var mapOptions = {
			center: pos,
			zoom: 17,
			mapTypeId: google.maps.MapTypeId.ROADMAP,
			mapTypeControl: false,
			"styles": map_style
		};
		var map = new google.maps.Map(document.getElementById("map"), mapOptions);		
		$scope.map = map;
		// add marker to current position
		var marker = new google.maps.Marker({
			position: myLatlng,
			map: $scope.map,
			icon: "images/me_map.png"
			//icon: "https://maps.google.com/mapfiles/kml/shapes/parking_lot_maps.png"//"img/QRPoint.png"
		});
		marker.setAnimation(google.maps.Animation.BOUNCE);
		$scope.selfMarker = marker;

		//	start update location timer
		clearInterval(setIntervalUpdate);
		setIntervalUpdate = setInterval(updateCurrentPosition, 1200);

		// Create the DIV to hold the control and
		// call the CurrentLocationControl() constructor passing
		// in this DIV.
		var curLocControlDiv = document.createElement('div');
		var curLocControl = new CurrentLocationControl(curLocControlDiv, map, myLatlng);

		curLocControlDiv.index = 1;
		map.controls[google.maps.ControlPosition.TOP_RIGHT].push(curLocControlDiv);

		//	Show Spots
		//	Add Markers
		for(var i = 0; i < $scope.spots.length; i++) {
			var spot = $scope.spots[i];
			var icon = "images/red_spot.png";
			if(i == selected)	icon = "images/red_spot_select.png";

			//	check if this spot is found
			var j;
			$scope.spots[i].Found = false;
			for(j = 0; j < $scope.foundspots.length; j++) {
				if($scope.spots[i].ID == $scope.foundspots[j].Spot) {
					$scope.spots[i].Found = true;
					icon = "images/red_spot_finish.png";
					break;
				}
			}

			var marker = new google.maps.Marker({
				position: new google.maps.LatLng(parseFloat(spot.Latitude), parseFloat(spot.Longitude)),
				map: $scope.map,
				title: spot.Name,
				icon: icon
				//icon: {
			    //   url: icon,
			    //    scaledSize: new google.maps.Size(13, 13) // pixels
			    //}
			});
			var infowindow = new google.maps.InfoWindow({
				content: '<span style="color: #000;">' + $scope.spots[i].Description + '</span>'
			});
			$scope.spots[i].marker = marker;
			$scope.spots[i].infowindow = infowindow;
			attachevent(i);

			if($scope.spots[i].Type == 0)	$scope.spots[i].Access = "Conditional";
			else $scope.spots[i].Access = "Free Access";
		}
	}

	$scope.Initialize = function() {
		$scope.mapInitialize();
		
		if(selected >= 0) {
			$scope.spot = $scope.spots[selected];
			if($scope.spot.ID != null){
				$scope.getSceneImage();
			}
			else{
				$scope.sceneImageClass_noImage = true;
				$scope.sceneImageClass_loading = false;
			}		
			$scope.calculateDistance();
			$scope.$apply();
		}
	}
	//google.maps.event.addDomListener(window, 'load', Initialize);
	if(typeof(google) == "undefined") {
		var alertPopup = $ionicPopup.alert({
			title: 'Error',
			template: "Google map object is not loaded.",
			okText: 'OK'
		});
		//alert("Google map object is not loaded.");
	}
	$timeout($scope.Initialize, 1000);
	
	$scope.showHint = function() {
		var alertPopup = $ionicPopup.alert({
			title: 'Hint',
			template: $scope.spot.Hint,
			okText: 'OK'
		});
	}

	$scope.showReport = function() {
		var reportPopup = $ionicPopup.show({
			template: '<textarea ng-model="$parent.reportstring" />',
			title: 'Report this spot',
			subTitle: 'Please input your report.',
			scope: $scope,
			buttons: [
				{
					text: '<b>Save</b>',
					type: 'button-positive',
					onTap: function(e) {
						if (!$scope.reportstring) {
							//don't allow the user to close unless he enters wifi password
							e.preventDefault();
						} else {
							return $scope.reportstring;
						}
					}
				},
				{
					text: 'Cancel'
				}
			]
		});
		reportPopup.then(function(res) {
			delete($scope.reportstring);
			if (res) {
				//	report this spot
				$ionicLoading.show({
					template: 'Please wait...'
				});

				SpotService.reportSpot($scope.spot.ID, res).then(function(data) {
					$ionicLoading.hide();

					if(!data.success) {
						var alertPopup = $ionicPopup.alert({
							title: 'Error',
							template: "Sorry, Report failed. Please try again later.",
							okText: 'OK'
						});
					}
				});
			}
		})
		.finally(function() {
			delete($scope.reportstring);
		});
	}

	$scope.calculateDistance = function() {
		var pos = LocationService.getCurrentPosition();
		var myLatlng = new google.maps.LatLng(pos.latitude, pos.longitude);
		//console.log(myLatlng, $scope.spot.marker.getPosition());

		var distance = LocationService.getDistance($scope.spot.marker.getPosition(), myLatlng);
		if(distance < 9.14){
			distance = Math.round(distance * 3.28084) + "ft";
			$scope.isRevealSecret = true;
		}
		else if(distance < 304.8) {
			distance = "899ft";
			$scope.isRevealSecret = false;
		}
		else {
			distance = Math.round(distance / 1609.344, 1) + "mi";
			$scope.isRevealSecret = false;
		}
		$scope.spot.Distance = distance;
	}

	$scope.getSceneImage = function() {
		$scope.spot.SceneImageDataList = new Array();
		$scope.spot.SceneImageURLList = new Array();
		$scope.sceneImageClass_noImage = false;

		var prefix = 'scenes/' + $scope.spot.Bucket_name + '/';
		AWSS3Service.getSceneImagesListObjects(prefix).then(function(data) {
			var re = /(?:\.([^.]+))?$/;

	    	for(i=0; i< data.Contents.length; i++){
	    		name = data.Contents[i].Key;
	    		if(re.exec(name)[1] == "jpg"){
	    			$scope.spot.SceneImageURLList.push( data.Contents[i].Key );
	    		}
	    	}

	    	for(i=0;i<$scope.spot.SceneImageURLList.length; i++){
	    		AWSS3Service.getSceneImageObject(prefix, $scope.spot.SceneImageURLList[i]).then(function(file) {
	    			$scope.sceneImageClass_loading = false;
			        $scope.spot.SceneImageDataList.push( encode(file.Body) );
		            $timeout(function() {
		            	$ionicSlideBoxDelegate.$getByHandle('scene-img-sliderbox').update();
		            }, 100);
	    		})
	    		.catch(function(err) {
	    			console.log('Could not load objects from S3');
	    			$scope.sceneImageClass_loading = false;
			        $scope.sceneImageClass_noImage = true;
	    		});
	    	}
		});
	    function encode(data){
			var str = data.reduce(function(a,b){ return a+String.fromCharCode(b) },'');
			return btoa(str).replace(/.{76}(?=.)/g,'$&\n');
		}

	}

})


