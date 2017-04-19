# Cordova Hello World Plugin

Simple plugin that returns your string prefixed with hello.

Greeting a user with "Hello, world" is something that could be done in JavaScript. This plugin provides a simple example demonstrating how Cordova plugins work.

## Using
    
Install the plugin

    $ cd hello
    $ cordova plugin add ../path/com.metaio.ar
    

Edit `www/js/index.js` and add the following code inside `onDeviceReady`

```js

    var success = function(message) {
        alert(message);
    }

    var failure = function() {
        alert("Error calling Hello Plugin");
    }

    // Playing AR
    cordova.AR.playback("Welcome Kenji", success, failure);

    // Creating AR with a Spot
    // 0: spotName
    // 1: spotLatitude
    // 2: spotLongitude
    // 3: spotDifficulty
    // 4: spotCoin
    // 5: spotDescription
    // 6: spotHint
    // 7: spotPuzzle
    // 8: spotPuzzleHint
    // 9: spotPuzzleAnswer

    spot = ["Spot1", "50.00", "50.00", "2", "3", "spot desc", "spot hint", "spot puzzle", "spot puzzle hint", "spot puzzle answer"];
    cordova.AR.creation(spot, success, failure);

```

Install iOS or Android platform

    cordova platform add ios
    cordova platform add android
    
Run the code

    cordova run 

##########################################################
## More Info

1. In Config.xml, 
- Add <preference name="android-minSdkVersion" value="15" />
- Add <preference name="android-targetSdkVersion" value="22" />

2. In res/values/strings.xml, please add the following codes:

<string name="app_prefs_label" translatable="false">capaug_prefs</string>
<string name="app_prefs_language" translatable="false">app_lang</string>
<string name="aws_s3_bucket" translatable="false">xcapade-v1</string>

3. In res/values/dimens.xml, please add the following codes:

<dimen name="top_bar_height">48dp</dimen>
<dimen name="marker_bar_width">72dp</dimen>
<dimen name="marker_bar_height">196dp</dimen>
<dimen name="progress_bar_height">16dp</dimen>
<dimen name="activity_thick_margin">64dp</dimen>
<dimen name="activity_thin_margin">16dp</dimen>
<dimen name="activity_horizontal_margin">16dp</dimen>
<dimen name="activity_vertical_margin">16dp</dimen>

3. In res/values/colors.xml, please add the following codes:

<color name="primary_dark">#88000000</color>
<color name="dark">#ff3c1509</color>
<color name="custom_red">#FFcf2e2e</color>
<color name="custom_white">#FFf7f7f7</color>


4. On Android Studio, Go to File/New/Import Module...
- Select metaioSDK-release/build.gradle and change ":metaioSDK-release"
- Select metaiohelper-release/build.gradle and change ":metaiohelper-release"
- Please go to File/ Project Structure/android/Dependencies and click "+" button and select "Module Dependencies" and add "metaiohelper-release" and "metaioSDK-release"

5. Open Android Studio and go to build.gradle
- chnage the following codes: 

"JavaVersion" should be set to "VERSION_1_7" such as the this code.

compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_7
    targetCompatibility JavaVersion.VERSION_1_7
}

- in "dependencies", add the following code: ( if you have any error with this, don't use. )
compile 'com.android.support:support-v4:21.0.3'

/*
    dependencies {
        classpath 'com.android.tools.build:gradle:1.5.0'
    }
*/

6. in <application> tag of AndroidManifest.xml, please add the code:

tools:overrideLibrary="com.zumoko.metaiohelper"


