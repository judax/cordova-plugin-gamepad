#cordova-plugin-gamepad

A Cordova plugin to handle the HTML5 gamepad API for iOS and Android. This plugin injects the `navigator.getGamepads` function into the system along with the `gamepadconnected` and `gamepaddisconnected` events into the window event management making any Cordova application to be able to use the underlying game controllers in the system with the same standard API. If the gamepad API is already available (the underlying Cordova webview already supports it) no modifications are made.

##How to use it

###Setup your Cordova project and install the plugin

This plugin has been published in the Cordova Plugin Registry (Plugman) so adding it to any Cordova project version 3.0 and above can be done using the Cordova Command Line Interface (CLI).

1. Install the Cordova CLI: https://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html
2. Create a Cordova project and add the desired platforms (remember that this plugin works for iOS and Android for now only): https://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html
3. Install the plugin with the following command line:

	`cordova plugin add com.judax.cordova.plugin.gamepad`

###Modifications in your application

No modifications are needed in your app in order to be able use this plugin. Of course, you need to use the HTML5 Gamepad API as it is shown in the specification (http://www.w3.org/TR/gamepad/).

NOTE: The current version is not 100% compliant with the latest version of the W3C Gamepad API spec. It is based on a previous spec version that did not include the GamepadButton interface, so the recommendation is to use the following conditional when querying for gamepad button data:

```javascript
	if (typeof gamepad.buttons[j]["value"] !== "undefined")
		value = gamepad.buttons[j]["value"];
	else
		value = gamepad.buttons[j];
```

##Future improvements

* Add support for Gamepad on Android bellow 2.3. There might be some problems to do this as a brief overlook to the needs state that access to the main activity/view might be needed. More information at: http://developer.android.com/training/game-controllers/compatibility.html

* Improve the documentation of (and maybe even refactor) the native source code for both iOS and Android.

* Add the `GamepadButton` structure as the final W3C Gamepad specification states (http://www.w3.org/TR/gamepad/).

##Additional references

* The HTML5 Gamepad specification: http://www.w3.org/TR/gamepad/

