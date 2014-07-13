#cordova-plugin-gamepad

A Cordova plugin to handle the HTML5 gamepad API for iOS and Android. This plugin inject the `navigator.getGamepads` function into the system along with the `gamepadconnected` and `gamepaddisconnected` events into the window event management making any Cordova application to be able to use the underlying game controllers in the system with the same standard API. If the gamepad API is already available (the underlying Cordova webview already supports it) no modifications are made.

##How to use it

###Setup your Cordova project and install the plugin

This plugin has been published in the Cordova Plugin Registry (Plugman) so adding it to any Cordova project version 3.0 and above can be done using the Cordova Command Line Interface (CLI).

1. Install the Cordova CLI: https://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html
2. Create a Cordova project and add the desired platforms (remember that this plugins works for iOS and Android for now only): https://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html
3. Install the plugin with the following command line:

	`cordova plugin add com.judax.cordova.plugin.gamepad`

###Modifications in your application

Not many modifications are needed in order to be able use the . Indeed only the inclusion of the "Gamepad.js" file is needed (and even this requirement might be removed in the future by using Cordova plugin modules). 

	...
	<head>
		<script src="Gamepad.js"></script>
	...

IMPORTANT: The `navigator.getGamepads` function won't be available until the Cordova `deviceready` event is fired, so make sure that your application does not try to call it before.

4. Build/run your project.

##Future improvements

* Add support for Gamepad on Android bellow 2.3. There might be some problems to do this as a brief overlook to the needs state that access to the main activity/view might be needed. More information at: http://developer.android.com/training/game-controllers/compatibility.html

* Improve the documentation of (and maybe even refactor) the native source code for both iOS and Android.

* Add the `GamepadButton` structure as the final W3C Gamepad specification states (http://www.w3.org/TR/gamepad/).

##Additional references

* The HTML5 Gamepad specification: http://www.w3.org/TR/gamepad/

