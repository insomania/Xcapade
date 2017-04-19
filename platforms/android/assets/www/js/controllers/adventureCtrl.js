
myApp.controller('AdventureCtrl', function($scope,$state,$ionicLoading,$ionicModal,$ionicPopup,$cordovaBarcodeScanner,$timeout,$ionicSideMenuDelegate,$compile,localStorageService,SpotService,RouteService,LocationService) {
	$scope.routes = localStorageService.get("routes") || [];
	$scope.mode = localStorageService.get("mode");
	$scope.entered = false;
	$scope.isRevealSecret = false;
	$scope.foundspots = localStorageService.get("foundspots") || [];

	var selectedroute = localStorageService.get("current_route");

	var prevSel = -1;

	$scope.toggleMenu = function() {
		$ionicSideMenuDelegate.toggleLeft();
	}
	$scope.onShare = function() {
		//----------------------
	}
	$scope.onGetToRevealSecret = function(){
		//----------------------
		$ionicPopup.alert({
			template: "Please get closer to reveal the secret.<br>",
			okText: 'OK'
		});
	}
	$scope.onPayToRevealSecret = function(){
		$ionicLoading.show({
			template: spinner + 'Please wait...'
		});

	
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

	$scope.Initialize = function() {
		var selectedspot = 0;
		prevSel = selectedspot;

		var pos = LocationService.getCurrentPosition();
		var myLatlng = new google.maps.LatLng(parseFloat(pos.latitude), parseFloat(pos.longitude));
		if(selectedroute >= 0) {
			$scope.route = $scope.routes[selectedroute];
			$scope.spot = $scope.routes[selectedroute].spots[0];

			pos = {latitude: $scope.spot.Latitude, longitude: $scope.spot.Longitude};
		}
		pos = new google.maps.LatLng(parseFloat(pos.latitude), parseFloat(pos.longitude));

		var mapOptions = {
			center: pos,
			zoom: 17,
			mapTypeId: google.maps.MapTypeId.ROADMAP,
			mapTypeControl: false,
			"styles": map_style
		};
		var map = new google.maps.Map(document.getElementById("map"), mapOptions);

		//	add marker to current position
		var marker = new google.maps.Marker({
			position: myLatlng,
			map: map,
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
		
		$scope.map = map;

		//	analyse order
//		console.log($scope.routes[selectedroute]);
		//

		//	Show Spots
		//	Add Markers
		for(var i = 0; i < $scope.routes[selectedroute].spots.length; i++) {
			var spot = $scope.routes[selectedroute].spots[i];
			var finished = (spot.Found) ? "_finish" : "";
			var icon = "images/red_spot" + finished + ".png";
			if(i == selectedspot)	icon = "images/red_spot_select" + finished + ".png";
			//if(spot.OrderNumber > $scope.routes[selectedroute].OrderNumber)
			//	icon = "http://xcapade.co/Orienteering/img/QRpoint_disabled.png";

			var marker = new google.maps.Marker({
				position: new google.maps.LatLng(parseFloat(spot.Latitude), parseFloat(spot.Longitude)),
				map: $scope.map,
				title: spot.Name,
				icon: icon
			});
			if(spot.OrderNumber > $scope.routes[selectedroute].OrderNumber)
				marker.setVisible(false);
			var infowindow = new google.maps.InfoWindow({
				content: '<span style="color: #000;">' + spot.Description + '</span>'
			});

			var attachevent = function(route, spot) {
				google.maps.event.addListener($scope.routes[route].spots[spot].marker, 'click', function() {
					if(prevSel >= 0) {
						var finished = ($scope.routes[route].spots[prevSel].Found) ? "_finish" : "";
						$scope.routes[route].spots[prevSel].infowindow.close();

						if($scope.routes[route].spots[prevSel].OrderNumber > $scope.routes[selectedroute].OrderNumber) {
							//$scope.routes[route].spots[prevSel].marker.setIcon("http://xcapade.co/Orienteering/img/QRpoint_disabled.png");
							$scope.routes[route].spots[prevSel].marker.setVisible(false);
						}
						else {
							$scope.routes[route].spots[prevSel].marker.setVisible(true);
							$scope.routes[route].spots[prevSel].marker.setIcon("images/red_spot" + finished + ".png");
						}
					}

					$scope.spot = $scope.routes[route].spots[spot];
					var finished = ($scope.spot.Found) ? "_finish" : "";
					$scope.routes[route].spots[spot].infowindow.open(map, $scope.routes[route].spots[spot].marker);
					$scope.routes[route].spots[spot].marker.setIcon("images/red_spot_select" + finished + ".png");
					prevSel = spot;

					$scope.calculateDistance();

					$scope.$apply();
				});
			}
			
			$scope.routes[selectedroute].spots[i].marker = marker;
			$scope.routes[selectedroute].spots[i].infowindow = infowindow;
			attachevent(selectedroute, i);

			if($scope.routes[selectedroute].spots[i].Type == 0)	$scope.routes[selectedroute].spots[i].Access = "Conditional";
			else $scope.routes[selectedroute].spots[i].Access = "Free Access";

			//	check if this spot is found
			/*var j;
			$scope.routes[selectedroute].spots[i].Found = false;
			for(j = 0; j < $scope.foundspots.length; j++) {
				if($scope.routes[selectedroute].spots[i].ID == $scope.foundspots[j].Spot) {
					$scope.routes[selectedroute].spots[i].Found = true;
					nFoundCount++;
					break;
				}
			}*/
		}
		$scope.spot = $scope.routes[selectedroute].spots[selectedspot];
		$scope.calculateDistance();
	}
	if(typeof(google) == "undefined") {
		var alertPopup = $ionicPopup.alert({
			title: 'Error',
			template: "Google map object is not loaded.",
			okText: 'OK'
		});
		//alert("Google map object is not loaded.");
	}
	//google.maps.event.addDomListener(window, 'load', Initialize);
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
			}]
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

	$scope.onEnter = function() {
		$scope.entered = true;
	}

	$scope.calculateDistance = function() {
		var pos = LocationService.getCurrentPosition();
		var myLatlng = new google.maps.LatLng(pos.latitude, pos.longitude);
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
})
