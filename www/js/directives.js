angular.module('Capade.directives', [])

.directive('preventDefault', function() {
	return function(scope, element, attrs) {
		angular.element(element).bind('click', function(event) {
			event.preventDefault();
			event.stopPropagation();
		});
	}
})

.directive('dialog', function() {
	return {
		restrict: 'E',
		scope: {
			show: '='
		},
		replace: true, // Replace with the template below
		transclude: true, // we want to insert custom content inside the directive
		link: function(scope, element, attrs) {
			scope.dialogStyle = {};
			scope.addAnimationClass = "updown-slider-animation";
			if (attrs.width)
				scope.dialogStyle.minWidth = attrs.width;
			if (attrs.height)
				scope.dialogStyle.height = attrs.height;
			scope.hideModal = function() {
				scope.show = false;
			};
		},
		template: "<div class='ng-modal' ng-show='show'>\
					<div class='ng-modal-overlay' ng-click='hideModal()'>\
					</div>\
					<div class='ng-modal-dialog {{addAnimationClass}}' ng-style='dialogStyle'>\
						<!-- div class='ng-modal-close' ng-click='hideModal()'>X</div -->\
						<div class='ng-modal-dialog-content' ng-transclude></div>\
					</div>\
				</div>"
	};
});
