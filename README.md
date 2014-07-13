#cordova-plugin-gamepad

A Cordova plugin to handle the HTML5 gamepad API for iOS and Android.

##How to use it

###Setup yout Cordova project and install the plugin

This plugin has been published in Cordova Plugin Registry (Plugman) so you should be able to follow the Cordova Command Line instructions in order to install and use a plugin.

1. Install the Cordova Command Line: https://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html
2. Create a Cordova project and add the desired platforms (remember that this plugins works for iOS and Android for now only): https://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html
3. Install the plugin with the following command line:

	`cordova plugin add com.judax.cordova.plugin.gamepad

###Modifications in the source code of your application

Not many modifications are needed. Indeed only the inclusion of the "Gamepad.js" file is needed (and even this requirement might be removed in the future by using Cordova plugin modules). 

	...
	<head>
		<script src="Gamepad.js"></script>
	...

The `navigator.getGamepads` function won't be available until the Cordova `deviceready` event is fired, so make sure that your application does not try to call it before.

##Future improvements

* Add support for Gamepad on Android bellow 2.3. There might be some problems to do this as a brief overlook to the needs state that access to the main activity/view might be needed. More information at: http://developer.android.com/training/game-controllers/compatibility.html

* Improve the documentation of (and maybe even refactor) the native source code for both iOS and Android.

