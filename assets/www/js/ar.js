/*global cordova, module*/

module.exports = {
    playback: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "ar", "playback", name);
    },
    creation: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "ar", "creation", name);
    },
    preload: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "ar", "preload", [name]);
    }
};
