var exec = require('cordova/exec');
var cordova = require('cordova');

(function() {
    function setupGamepadPlugin() {

        // Check if the getGamepads function already exists. If so, just return (and if it uses a prefix, get rid of it)
        navigator.getGamepads = navigator.getGamepads || navigator.webkitGetGamepads || navigator.webkitGamepads;

        if (navigator.getGamepads) return;

        /*
        This data has been added because in Construct2 (www.scirra.com) the data taken from the native gamepads is being modified 
        adding some attributes to it. Cordova is returning a new data structure in everycall (that is just how JS to Native bindings work)
        so the following array (gamepads) will contain up to date copies of the native gamepad structures.
        As this functionality adds some extra overhead (copies per call), you can disable it by just setting USE_MERGE to false.
        */
        var USE_MERGE = true;
        var gamepads = [];

        // Creates a type lookup table. The idea for this code has been taken from the PlayCanvas game engine source code (https://github.com/playcanvas/engine)
        var typeLookup = (function () {
            var result = {};
            var index;
            var names = ['Array', 'Object', 'Function', 'Date', 'RegExp', 'Float32Array'];

            for(index = 0; index < names.length; ++index) {
                result['[object ' + names[index] + ']'] = names[index].toLowerCase();
            }

            return result;
        })();

        // An imporoved version of typeof. The idea for this code has been taken from the PlayCanvas game engine source code (https://github.com/playcanvas/engine)
        function type(obj) {
            if (obj === null) {
                return 'null';
            }

            var type = typeof(obj);

            if (type == 'undefined' || type == 'number' || type == 'string' || type == 'boolean') {
                return type;
            }

            return typeLookup[Object.prototype.toString.call(obj)];
        }

        // Makes a deep copy of from into target. The idea for this code has been taken from the PlayCanvas game engine source code (https://github.com/playcanvas/engine)
        function merge(target, from) {
            var prop, copy;

            for(prop in from) {
                copy = from[prop];
                if(type(copy) == 'object') {
                    target[prop] = merge((type(target[prop]) == 'object' ? target[prop] : {}), copy);
                } 
                else if(type(copy) == 'array') {
                    target[prop] = merge((type(target[prop]) == 'array' ? target[prop] : []), copy);
                } 
                else {
                    target[prop] = copy;
                }
            }

            return target;
        }

        // The HTML5 gamepad API calling to the native Cordova plugin
        navigator.getGamepads = function() {
            var i, j;
            var nativeGamepads;
            var nativeGamepad;
            var gamepad;

            // This is the Cordova success callback.
            function success(nativeGamepads) {
                // If the merge flag is active, update the gamepads array in the JS side using the nativeGamepads array from the native side.
                if (USE_MERGE) {
                    if (nativeGamepads.length === 0) {
                            gamepads = [];
                    }
                    else {
                        // Iterate over all the JS gamepads
                        for (i = 0; i < gamepads.length; i++) {
                            gamepad = gamepads[i];
                            nativeGamepad = null;
                            // Iterate over all the native gamepads
                            for (j = 0; nativeGamepad === null && j < nativeGamepads.length; j++) {
                                nativeGamepad = nativeGamepads[j];
                                if (!nativeGamepad || nativeGamepad.index !== gamepad.index) {
                                    nativeGamepad = null;
                                }
                            }
                            // If a native gamepad has a counterpart in the JS gamepad, merge them and mark the native gamepad as found
                            if (nativeGamepad !== null) {
                                gamepad = merge(gamepad, nativeGamepad);
                                nativeGamepad.found = true;
                            }
                            // If there is no conterpart, remove the JS gamepad
                            else {
                                gamepads.splice(i, 1);
                                i--;
                            }
                        }
                        // All the native gamepads that were not found must be added to the JS gamepads array
                        for (i = 0; i < nativeGamepads.length; i++) {
                            nativeGamepad = nativeGamepads[i];
                            if (!nativeGamepad.found) {
                                gamepad = merge({}, nativeGamepad);
                                gamepads.push(gamepad);
                            }
                        }
                    }
                    return gamepads;
                }
                // As the merge flag was not active, the native gamepads are returned.
                else {
                    gamepads = nativeGamepads;
                }
            }

            function error(errorMessage) {
                console.error("Error while a call to getGamepads: " + errorMessage);
            }

            cordova.exec(success, error, "Gamepad", "getGamepads", []);

            return gamepads;
        };

        // Just in case the app was compatible with the prefixed gamepad API, make all of them point to the same function.
        navigator.webkitGetGamepads = navigator.getGamepads;
        navigator.webkitGamepads = navigator.getGamepads;

        // Create a data structure called gamepad inside cordova to hold to the gamepad connected and disconnected JS callbacks.
        if (typeof(cordova.gamepad) === "undefined") {
            cordova.gamepad = {
                listeners: {
                    connected: [],
                    disconnected: []
                }
            };
        }

        // If this function is called, it means the native side has detected a new gamepad so all the JS listeners need to be notified.
        function gamepadConnectedSuccessCallback(e) {
            for (var i = 0; i < cordova.gamepad.listeners.connected.length; i++) {
                cordova.gamepad.listeners.connected[i].apply(this, Array.prototype.slice.call(arguments));
            }
        }

        // If this function is called, it means the native side has detected a gamepad disconnection so all the JS listeners need to be notified.
        function gamepadDisconnectedSuccessCallback(e) {
            for (var i = 0; i < cordova.gamepad.listeners.disconnected.length; i++) {
                cordova.gamepad.listeners.disconnected[i].apply(this, Array.prototype.slice.call(arguments));
            }
        }

        // A function to handle possible errors.
        function gamepadEventError(errorMessage) {
            console.error("Error on the native Cordova gamepad plugin in relation with gamepad connected/disconnected callbacks.");
        }

        // Removes JS callbacks/listeners
        function removeCallback(container, callback) {
            for (var i = 0; i < container.length; i++) {
                if (container[i] === callback) {
                    container.splice(i, 1);
                    i--;
                }
            }
        }

        // Register gamepadConnected and gamepadDisconnected callbacks in the native side.
        // The native plugins will hold on to those callbacks and use them whenever a gamepad is connected/disconnected.
        cordova.exec(gamepadConnectedSuccessCallback, gamepadEventError, "Gamepad", "setGamepadConnectedCallback", []);
        cordova.exec(gamepadDisconnectedSuccessCallback, gamepadEventError, "Gamepad", "setGamepadDisconnectedCallback", []);

        // Modify the window add and remove event listeners to be able to listen and correctly register gamepad related
        // event listeners. In order to continue providing the same window old event registering functionality, the old
        // listeners are stored in the cordova.gamepad structure.
        // This functions just register/unregister listeners in the cordova.gamepad.listeners corresponding arrays.
        // If the event is not related to gamepads, the old functions are called.

        cordova.gamepad.oldWindowAddEventListener = cordova.gamepad.oldWindowAddEventListener || window.addEventListener;
        window.addEventListener = function(eventName, callback) {
            if (eventName === 'gamepadconnected') {
                cordova.gamepad.listeners.connected.push(callback);
            }
            else if (eventName === 'gamepaddisconnected') {
                cordova.gamepad.listeners.disconnected.push(callback);
            }
            else {
                return cordova.gamepad.oldWindowAddEventListener.apply(this, Array.prototype.slice.call(arguments));
            }
        };

        cordova.gamepad.oldWindowRemoveEventListener = cordova.gamepad.oldWindowRemoveEventListener || window.removeEventListener;
        window.removeEventListener = function(eventName, callback) {
            if (eventName === 'gamepadconnected') {
                removeCallback(cordova.gamepad.listeners.connected, callback);
            } 
            else if (eventName === 'gamepaddisconnected') {
                removeCallback(cordova.gamepad.listeners.disconnected, callback);
            }
            else {
                return cordova.gamepad.oldWindowRemoveEventListener.apply(this, Array.prototype.slice.call(arguments));
            }
        };   
    }

    // Wait for Cordova to be ready in order to start the plugin
    document.addEventListener("deviceready", setupGamepadPlugin);
})();
