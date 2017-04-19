
myApp.controller('CoinShopCtrl', function($scope, $state, $ionicViewService, localStorageService, $ionicLoading, $ionicPopup, IAPService) {
	$scope.user = localStorageService.get("user") || {};
	$scope.selectedType = "";
	$scope.productId = "";
	
	var productIds = [IAPProductId0, IAPProductId1, IAPProductId2, IAPProductId3];
	$scope.toBack = function() {
		$state.go("main.home");
	}
	$scope.loadProducts = function () {
    $ionicLoading.show({ template: spinner + 'Loading Products...' });
    inAppPurchase
      .getProducts(productIds)
      .then(function (products) {
        $ionicLoading.hide();
        $scope.products = products;
      })
      .catch(function (err) {
        $ionicLoading.hide();
        console.log(err);
      });
  };	
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
	$scope.buyGoldenMember = function() {

		$scope.selectedType = "goldenMember";
		$ionicLoading.show({ template: spinner + 'Purchasing...' });
		inAppPurchase.subscribe(IAPProductId0)
	    .then(function () {
	    	// Connecting to Web server
	    	$scope.updateUserCoins($scope.selectedType);
	    })
	    .catch(function (err) {
	      $ionicLoading.hide();
	      console.log(err);
	      $ionicPopup.alert({
	        title: 'Purchase error',
	        template: err.message
	      });
	    });
	}
	$scope.buyCoins = function(type){		
		$scope.selectedType = type;
		if(type == "coins20") {
			$scope.productId = IAPProductId1;
		}
		else if(type == "coin50") {
			$scope.productId = IAPProductId2;
		}
		else if(type == "coin80") {
			$scope.productId = IAPProductId3;
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
		inAppPurchase.buy($scope.productId)
			.then(function (data) {
        console.log(JSON.stringify(data));
        console.log('consuming transactionId: ' + data.transactionId);
        return inAppPurchase.consume(data.productType, data.receipt, data.signature);
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
	        template: err.message
	      });
	    });
	}
	$scope.loadProducts();
})