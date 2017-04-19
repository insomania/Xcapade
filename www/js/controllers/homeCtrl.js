
myApp.controller('HomeCtrl', function($scope,$state,$ionicLoading,$ionicModal,$ionicPopup,$cordovaFacebook,$timeout,$ionicSideMenuDelegate,$compile,$window,LocationService,SpotService,RouteService,UserService,localStorageService) {
	$scope.user = localStorageService.get("user") || {};
	
	//$scope.mode = localStorageService.get("mode") || "";
	//$scope.modalShown = false;
	$scope.modalShown = true;
	$scope.toggleModal = function() {
		$scope.modalShown = !$scope.modalShown;
		$scope.addAnimationClass = "updown-slider-animation";
	};

	// $scope.$on("toGoHomePage", function () {
	// 	$scope.modalShown = !$scope.modalShown;
	// });

	$scope.toggleMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}

	$scope.$on("reloadSpotData", function (event, args) {
		$scope.Initialize();
	});

	$scope.Initialize = function() {
		var loginState = localStorageService.get("login_auth");

		if(loginState == "loggedIn"){
			$scope.auth = "1";
		}
		else{			
			$scope.auth = "0";
			localStorageService.set("login_auth", "loggedIn");
		}

		var pos = LocationService.getCurrentPosition();
		var myLatlng = new google.maps.LatLng(pos.latitude, pos.longitude);

		var mapOptions = {
			backgroundColor: "#eeeeee",
			center: myLatlng,
			zoom: 15,
			//mapTypeId: google.maps.MapTypeId.ROADMAP,
			//disableDefaultUI: true,
			mapTypeControl: false,
	    //mapTypeControlOptions: {
   		//	mapTypeIds: [google.maps.MapTypeId.ROADMAP, MY_MAPTYPE_ID]
 			//},
			mapTypeId: MY_MAPTYPE_ID,
			"styles": map_style
		};
		var map = new google.maps.Map(document.getElementById("map"), mapOptions);
		var curLocControlDiv = document.createElement('div');
		var curLocControl = new CurrentLocationControl(curLocControlDiv, map, myLatlng);

		curLocControlDiv.index = 1;
		map.controls[google.maps.ControlPosition.TOP_RIGHT].push(curLocControlDiv);

		$scope.map = map;

		$scope.centerOnMe();
	}

	if(typeof(google) == "undefined") {
		var alertPopup = $ionicPopup.alert({
			title: 'Error',
			template: "Google map object is not loaded.",
			okText: 'OK'
		});
	}

	$timeout($scope.Initialize, 1000);

	var updateCurrentPosition = function() {
		var pos = LocationService.getCurrentPosition();
		$scope.selfMarker.setPosition(new google.maps.LatLng(pos.latitude, pos.longitude));
		$scope.calculateDistance();
		$scope.$apply();
	}

	$scope.centerOnMe = function() {
		var pos = LocationService.getCurrentPosition();
		$scope.curLoc = pos;
		$scope.map.setCenter(new google.maps.LatLng(pos.latitude, pos.longitude));

		//	add marker to current position
		var marker = new google.maps.Marker({
			position: new google.maps.LatLng(parseFloat(pos.latitude), parseFloat(pos.longitude)),
			map: $scope.map,
			icon: "images/me_map.png"
		});
		marker.setAnimation(google.maps.Animation.BOUNCE);
		$scope.selfMarker = marker;

		//	start update location timer
		clearInterval(setIntervalUpdate);
		setIntervalUpdate = setInterval(updateCurrentPosition, 1200);

		//	Read necessary information based on current location
		//	get spots from server slightly

		console.log("current user: ", $scope.user);
		SpotService.getSpots(-1, $scope.curLoc, $scope.auth, $scope.user).then(function(data) {
			localStorageService.set("spots", data);
			console.log("spots", data);

			//	limit to qr spots
			$scope.spots = [];
			for(var i = 0; i < data.length; i++) {
				if(data[i].Type == 0)
					$scope.spots.push(data[i]);
			}

			SpotService.getFoundSpots().then(function(data) {
				$scope.foundspots = data;
				console.log("found spots", data);
				localStorageService.set("foundspots", data);

				updateMap();				
				//	get routes from server slightly
				/*
				RouteService.getRoutes($scope.curLoc).then(function(data) {

					$scope.routes = [];
					for(var i = 0; i < data.length; i++) {
						if(data[i].Spot == -1) {
							data[i].spots = [];
							$scope.routes.push(data[i]);

							//	remove empty route
							if($scope.routes.length > 1) {
								if($scope.routes[$scope.routes.length - 2].spots.length == 0) {
									//console.log($scope.routes, $scope.routes.length);
									$scope.routes.pop($scope.routes.length - 2);
									//console.log($scope.routes);
								}
								else {
									//	check found for the last route
									var spots = $scope.routes[$scope.routes.length - 2].spots;
									$scope.routes[$scope.routes.length - 2].Found = 0;
									for(var j = 0; j < spots.length; j++) {
										var k;
										for(k = 0; k < $scope.foundspots.length; k++) {
											if(spots[j].Spot == $scope.foundspots[k].Spot) {
												break;
											}
										}
										if(k < $scope.foundspots.length) {
											//	This is found spot
											$scope.routes[$scope.routes.length - 2].spots[j].Found = true;
											$scope.routes[$scope.routes.length - 2].Found++;
										}
									}
								}
							}
						}
						else {
							$scope.routes[$scope.routes.length - 1].spots.push(data[i]);
						}
					}

					//	Process last element
					if($scope.routes.length > 0 && $scope.routes[$scope.routes.length - 1].spots.length == 0) {
						//console.log($scope.routes);
						$scope.routes.pop($scope.routes.length - 1);
						//console.log($scope.routes);
					}
					else {
						//	check found for the last route
						var spots = $scope.routes[$scope.routes.length - 1].spots;
						var foundcount = 0;
						$scope.routes[$scope.routes.length - 1].Found = 0;
						for(var j = 0; j < spots.length; j++) {
							var k;
							for(k = 0; k < $scope.foundspots.length; k++) {
								if(spots[j].Spot == $scope.foundspots[k].Spot) {
									break;
								}
							}
							if(k < $scope.foundspots.length) {
								//	This is found spot
								$scope.routes[$scope.routes.length - 1].spots[j].Found = true;
								$scope.routes[$scope.routes.length - 1].Found++;
							}
						}
					}
					//

					//	analyse order
					for(var i = 0; i < $scope.routes.length; i++) {
						var spots = $scope.routes[i].spots;
						if(spots.length == 0) {
							continue;
						}
						$scope.routes[i].OrderNumber = spots[0].OrderNumber;
						if(spots[0].Found) {
							for(var j = 1; j < spots.length; j++) {
								$scope.routes[i].OrderNumber = spots[j].OrderNumber;
								if(!spots[j].Found) {
									break;
								}
							}
						}
					}
					//

					localStorageService.set("routes", $scope.routes);

					$scope.foundroutescount = 0;
					for(var i = 0; i < $scope.routes.length; i++) {
						if($scope.routes[i].Found == $scope.routes[i].spots.length)
							$scope.foundroutescount++;
					}

					// updateMap();
				})
				.catch(function(err) {
					console.log(err);
				});
				*/
			})
			.catch(function(err) {
				console.log(err);
			});
		})
		.catch(function(err) {
			console.log(err);
		});

		$ionicLoading.hide();
	}	

	$scope.friendStyle = "selected";
	$scope.overallStyle = "";
	$scope.cur = "friends";

	$scope.onBtnFriendClick = function() {
		$scope.cur = "friends";
		document.activeElement.blur();
		$scope.friendStyle = "selected";
		$scope.overallStyle = "";
	}

	$scope.onBtnOverallClick = function() {
		$scope.cur = "overall";
		document.activeElement.blur();
		$scope.friendStyle = "";
		$scope.overallStyle = "selected";
	}

	var clearSelection = function(index, icon) {
		if(selectedMode == "single" && prevSel >= 0) {
			var icon = "images/red_spot.png";

			if($scope.spots[prevSel].Found)
				icon = "images/red_spot_finish.png";
			$scope.spots[prevSel].marker.setIcon(icon);
			$scope.spots[prevSel].infowindow.close();
		}

		//	highlight selected route
		if(selectedMode == "route" && prevSel >= 0) {
			var icon = "images/red_spot.png";

			if($scope.routes[prevSel].Found == $scope.routes[index].spots.length)
				icon = "images/red_spot_finish.png";
			//for(var k = 0; k < $scope.routes[prevSel].spots.length; k++) {
			$scope.routes[prevSel].marker.setIcon(icon);
			$scope.routes[prevSel].infowindow.close();
			//}
		}
	}

	var prevSel = -1;
	var selectedMode = "single";
	var updateMap = function() {
		
		removeMarkers();

		// Add Marker
		var data = $scope.spots;
		console.log("yellow point: " + data.length);
		for(var i = 0; i < data.length; i++) {
			var spot = data[i];

			var icon = "images/red_spot.png";

			//	check if this spot is found
			var j;
			for(j = 0; j < $scope.foundspots.length; j++) {
				if(spot.ID == $scope.foundspots[j].Spot)
					break;
			}
			if(j < $scope.foundspots.length) {
				//	found
				$scope.spots[i].Found = true;
				spot.Found = true;
				icon = "images/red_spot_finish.png";
			}

			var marker = new google.maps.Marker({
				position: new google.maps.LatLng(parseFloat(spot.Latitude), parseFloat(spot.Longitude)),
				map: $scope.map,
				title: spot.Name,
				//icon: {
		    //    url: icon,
		    //    scaledSize: new google.maps.Size(13, 13) // pixels
		    //}
				icon: icon
			});

			var attachEvent = function(index) {
				google.maps.event.addListener($scope.spots[index].marker, 'click', function() {
					$scope.spots[index].infowindow.open($scope.map, $scope.spots[index].marker);
					var icon = "images/red_spot.png";

					clearSelection(index);

					selectedMode = "single";

					icon = "images/red_spot_select.png";
					if($scope.spots[index].Found)
						icon = "images/red_spot_select_finish.png";
					$scope.spots[index].marker.setIcon(icon);
					prevSel = index;

					if($scope.spot == $scope.spots[index]) {
						localStorageService.set("current_spot", index);
						$state.go("main.singlespot");
					}
					else {
						$scope.route = "";
						$scope.spot = $scope.spots[index];
						$scope.calculateDistance();
						$scope.$apply();
					}
				});
			}

			$scope.spots[i].marker = marker;
			$scope.spots[i].infowindow = new google.maps.InfoWindow({
				content: '<span style="color: #000;">' + $scope.spots[i].Description + '</span>'
			});
			attachEvent(i);
		}

		//-------------------------
		//- How to disable Route Points------------------ 
		//-------------------------
		
		/*	
		data = $scope.routes;
		console.log("red point: " + data.length);
		for(var i = 0; i < $scope.routes.length; i++) {
			var route = $scope.routes[i];
			var icon = "images/red_spot.png";
			if(route.Found == route.spots.length)
				icon = "images/red_spot_finish.png";
			var marker = new google.maps.Marker({
				position: new google.maps.LatLng(parseFloat(route.Latitude), parseFloat(route.Longitude)),
				map: $scope.map,
				title: route.Name,
				icon: icon
			});

			var attachEvent = function(route) {
				google.maps.event.addListener($scope.routes[route].marker, 'click', function() {
					$scope.routes[route].infowindow.open($scope.map, $scope.routes[route].marker);

					var icon = "images/red_spot.png";
					
					clearSelection(route);

					selectedMode = "route";

					icon = "images/route.png";
					if($scope.routes[route].Found == $scope.routes[route].spots.length)
						icon = "images/route_finish.png";
					else if($scope.routes[route].Found > 0)
						icon = "images/route_transit.png";
					$scope.routes[route].marker.setIcon(icon);

					if(prevSel == route) {
						localStorageService.set("current_route", route);
						$state.go("main.adventure");
					}
					else {
						$scope.spot = ""; //add by km
						$scope.route = $scope.routes[route];
						$scope.calculateDistance();

						$scope.$apply();
					}
					prevSel = route;
				});
			}
			
			$scope.routes[i].marker = marker;
			$scope.routes[i].infowindow = new google.maps.InfoWindow({
				content: '<span style="color:#000">' + $scope.routes[i].Description + '</span>'
			});
			attachEvent(i);
		}
		*/

	}

	var removeMarkers = function() {
		for(var i = 0; i < $scope.spots.length; i++) {
			if(typeof($scope.spots[i].marker) != "undefined" && $scope.spots[i].marker) {
				$scope.spots[i].marker.setMap(null);
				delete($scope.spots[i].marker);
			}
		}

		for(var i = 0; i < $scope.routes.length; i++) {
			for(var j = 0; j < $scope.routes[i].spots.length; j++) {
				if(typeof($scope.routes[i].spots[j].marker) != "undefined" && $scope.routes[i].spots[j].marker) {
					$scope.routes[i].spots[j].marker.setMap(null);
					delete($scope.routes[i].spots[j].marker);
				}
			}
		}
	}

	//	Get Spots
	//	get spots from local storage first
	var spots = localStorageService.get("spots") || [];
	//	limit to qr spots
	$scope.spots = [];
	for(var i = 0; i < spots.length; i++) {
		if(spots[i].Type == 0)
			$scope.spots.push(spots[i]);
	}

	//	Get Routes
	$scope.routes = localStorageService.get("routes") || [];

	//	Get Friends
	$scope.friends = localStorageService.get("friends") || [];

	//	Get Found spots
	$scope.foundspots = localStorageService.get("foundspots") || [];

	//	
	$scope.onStart = function() {
		if(selectedMode == "single") {
			localStorageService.set("current_spot", prevSel);
			$state.go("main.singlespot");
		}
		else if(selectedMode == "route") {
			localStorageService.set("current_route", prevSel);
			$state.go("main.adventure");
		}
	}

	//	calculate distance
	$scope.calculateDistance = function() {
		var pos = LocationService.getCurrentPosition();
		var myLatlng = new google.maps.LatLng(pos.latitude, pos.longitude);

		if($scope.spot == undefined){
			return;
		}

		if(selectedMode == "single") {
			var distance = LocationService.getDistance($scope.spot.marker.getPosition(), myLatlng);
			if(distance < 9.14){
				distance = Math.round(distance * 3.28084) + "ft";
			}
			else if(distance < 304.8) {
				distance = "899ft";
			}
			else {
				distance = Math.round(distance / 1609.344, 1) + "mi";
			}
			$scope.spot.Distance = distance;
		}
		else if(selectedMode == "route") {
			var distance = LocationService.getDistance($scope.route.marker.getPosition(), myLatlng);
			if(distance < 9.14){
				distance = Math.round(distance * 3.28084) + "ft";
			}
			else if(distance < 304.8) {
				distance = "899ft";
			}
			else {
				distance = Math.round(distance / 1609.344, 1) + "mi";
			}
			$scope.route.Distance = distance;
		}
	}
})


