angular.module('Capade.filters', [])

.filter('sanitize', function($sce) {
	return function(htmlCode) {
		return $sce.trustAsHtml(htmlCode);
	}
})
