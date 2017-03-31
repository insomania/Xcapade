
myApp.controller('CoinShopCtrl', function($scope, $state, $ionicViewService, localStorageService, $ionicLoading, $ionicPopup, IAPService) {
	$scope.user = localStorageService.get("user") || {};
	$scope.selectedType = "";
	
	$scope.toBack = function() {
		$state.go("main.home");
	}
	$scope.checkMembership = function() {
		if($scope.user.Membership == 1) {
			return false;
		}
		else {
			return true;
		}
	}
	$scope.updateUserCoins = function(type) {
		IAPService.registerPurchase($scope.user.ID, type).then(function(data) {
  		$ionicLoading.hide();
  		if(type == "goldenMember") {
    		var alertPopup = $ionicPopup.alert({
	        title: 'Purchase was successful!',
	        template: 'Great! You got 100 coins with Golden Member. This will be expired in a month.'
	      });
	      $scope.user.Membership = 1;
    	}
    	else {
    		var alertPopup = $ionicPopup.alert({
	        title: 'Purchase was successful!',
	        template: 'Great! You got new coins to play more.'
	      });
    	}
  	}).
		catch(function(error) {
			$ionicLoading.hide();
			console.log(err);
      $ionicPopup.alert({
        title: 'Error',
        template: 'Could not connect to server.\n'
      });
		});
	}
	$scope.buyCoins = function(type){
		var productId = "";
		$scope.selectedType = type;
		if(type == "goldenMember") {
			productId = IAPProductId0;
		}
		else if(type == "coins20") {
			productId = IAPProductId1;
		}
		else if(type == "coin50") {
			productId = IAPProductId2;
		}
		else if(type == "coin80") {
			productId = IAPProductId3;
		}
		else {
			$ionicPopup.alert({
				title: 'Purchase Error',
				template: "Purchase type wasn't choosen. Please click buy button correctly.",
				okText: 'OK'
			});
			return;
		}

		$ionicLoading.show({ template: spinner + 'Purchasing...' });
		inAppPurchase
	    .buy(productId)
	    .then(function (data) {
	      console.log(JSON.stringify(data));
	      console.log('consuming transactionId: ' + data.transactionId);
	      return inAppPurchase.consume(data.type, data.receipt, data.signature);
	    })
	    .then(function () {
	    	// Connecting to Web server
	    	$scope.updateUserCoins($scope.selectedType);
	    })
	    .catch(function (err) {
	      $ionicLoading.hide();
	      console.log(err);
	      $ionicPopup.alert({
	        title: 'Purchase error',
	        template: 'Please check your internet and retry.'
	      });
	    });

	}
})