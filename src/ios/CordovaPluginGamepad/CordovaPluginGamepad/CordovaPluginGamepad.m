#import "CordovaPluginGamepad.h"

#import <GameController/GCController.h>

#import <UIKit/UIKit.h>

#define NUMBER_OF_BUTTONS 17
#define NUMBER_OF_AXES 4

#define ID @"id"
#define INDEX @"index"
#define CONNECTED @"connected"
#define TIME_STAMP @"timestamp"
#define MAPPING @"mapping"
#define AXES @"axes"
#define BUTTONS @"buttons"

#define GAMEPAD @"gamepad"

#define GAMEPAD_CONNECTED @"gamepadconnected"
#define GAMEPAD_DISCONNECTED @"gamepaddisconnected"

#define BUTTON_0_INDEX 0
#define BUTTON_1_INDEX 1
#define BUTTON_2_INDEX 2
#define BUTTON_3_INDEX 3
#define BUTTON_LEFT_BUMPER_INDEX 4
#define BUTTON_RIGHT_BUMPER_INDEX 5

#define BUTTON_LEFT_TRIGGER_INDEX 6
#define BUTTON_RIGHT_TRIGGER_INDEX 7

//		KEY_CODE_TO_BUTTON_INDEX_MAP.put(SELECT/BACK, 8);
//		KEY_CODE_TO_BUTTON_INDEX_MAP.put(START/FORWARD, 9);

#define BUTTON_LEFT_JOYSTICK_INDEX 10
#define BUTTON_RIGHT_JOYSTICK_INDEX 11
#define BUTTON_DPAD_UP_INDEX 12
#define BUTTON_DPAD_DOWN_INDEX 13
#define BUTTON_DPAD_LEFT_INDEX 14
#define BUTTON_DPAD_RIGHT_INDEX 15

//		KEY_CODE_TO_BUTTON_INDEX_MAP.put(82, 16); // BUTTON_MENU

#define AXIS_LEFT_JOYSTICK_X_INDEX 0
#define AXIS_LEFT_JOYSTICK_Y_INDEX 1
#define AXIS_RIGHT_JOYSTICK_X_INDEX 2
#define AXIS_RIGHT_JOYSTICK_Y_INDEX 3

//		KEY_CODES_TO_JUST_PROCESS_ACTION_DOWN.add(82);

@implementation CordovaPluginGamepad

-(NSMutableDictionary*)findGamepadByPlayerIndex:(NSInteger)playerIndex
{
    NSMutableDictionary* gamepad = nil;
    for (NSInteger i = 0; gamepad == nil && i < gamepads.count; i++)
    {
        gamepad = gamepads[i];
        NSInteger gamepadPlayerIndex = [(NSNumber*)[gamepad objectForKey:INDEX] integerValue];
        gamepad = gamepadPlayerIndex == playerIndex ? gamepad : nil;
    }
    return gamepad;
}

-(void)setupControllerToBeRead:(GCController*)controller
{
    // First check if the controller supports the extended gamepad profile (it includes the basic).
    if (controller.extendedGamepad)
    {
        controller.extendedGamepad.valueChangedHandler = ^(GCExtendedGamepad* data, GCControllerElement* element)
        {
            NSMutableDictionary* gamepad = [self findGamepadByPlayerIndex:data.controller.playerIndex];
            if (gamepad != nil)
            {
                // The common data with the basic profile gamepad data
                NSMutableArray* buttons = [gamepad objectForKey:BUTTONS];
                NSMutableArray* axes = [gamepad objectForKey:AXES];
                if (element == data.buttonA)
                {
                    buttons[BUTTON_0_INDEX] = [NSNumber numberWithFloat:data.buttonA.value];
                }
                else if (element == data.buttonB)
                {
                    buttons[BUTTON_1_INDEX] = [NSNumber numberWithFloat:data.buttonB.value];
                }
                else if (element == data.buttonX)
                {
                    buttons[BUTTON_2_INDEX] = [NSNumber numberWithFloat:data.buttonX.value];
                }
                else if (element == data.buttonY)
                {
                    buttons[BUTTON_3_INDEX] = [NSNumber numberWithFloat:data.buttonY.value];
                }
                else if (element == data.leftShoulder)
                {
                    buttons[BUTTON_LEFT_BUMPER_INDEX] = [NSNumber numberWithFloat:data.leftShoulder.value];
                }
                else if (element == data.rightShoulder)
                {
                    buttons[BUTTON_RIGHT_BUMPER_INDEX] = [NSNumber numberWithFloat:data.rightShoulder.value];
                }
                else if (element == data.dpad)
                {
                    buttons[BUTTON_DPAD_UP_INDEX] = [NSNumber numberWithFloat:data.dpad.up.value];
                    buttons[BUTTON_DPAD_DOWN_INDEX] = [NSNumber numberWithFloat:data.dpad.down.value];
                    buttons[BUTTON_DPAD_LEFT_INDEX] = [NSNumber numberWithFloat:data.dpad.left.value];
                    buttons[BUTTON_DPAD_RIGHT_INDEX] = [NSNumber numberWithFloat:data.dpad.right.value];
                }
                // Specific data to the extended profile gamepad data
                else if (element == data.leftTrigger)
                {
                    buttons[BUTTON_LEFT_TRIGGER_INDEX] = [NSNumber numberWithFloat:data.leftTrigger.value];
                }
                else if (element == data.rightTrigger)
                {
                    buttons[BUTTON_RIGHT_TRIGGER_INDEX] = [NSNumber numberWithFloat:data.rightTrigger.value];
                }
                else if (element == data.leftThumbstick)
                {
                    axes[AXIS_LEFT_JOYSTICK_X_INDEX] = [NSNumber numberWithFloat:data.leftThumbstick.xAxis.value];
                    // Negate the value as the W3C considers up as negative 1 (-1)
                    axes[AXIS_LEFT_JOYSTICK_Y_INDEX] = [NSNumber numberWithFloat:-data.leftThumbstick.yAxis.value];
                }
                else if (element == data.rightThumbstick)
                {
                    axes[AXIS_RIGHT_JOYSTICK_X_INDEX] = [NSNumber numberWithFloat:data.rightThumbstick.xAxis.value];
                    // Negate the value as the W3C considers up as negative 1 (-1)
                    axes[AXIS_RIGHT_JOYSTICK_Y_INDEX] = [NSNumber numberWithFloat:-data.rightThumbstick.yAxis.value];
                }
                
                [gamepad setObject:[NSNumber numberWithInteger:CACurrentMediaTime() * 1000.0 - initialTimeMillis] forKey:TIME_STAMP];
            }
        };
    }
    else if (controller.gamepad)
    {
        controller.gamepad.valueChangedHandler = ^(GCGamepad* data, GCControllerElement* element)
        {
            NSMutableDictionary* gamepad = [self findGamepadByPlayerIndex:data.controller.playerIndex];
            if (gamepad != nil)
            {
                // The common data with the basic profile gamepad data
                NSMutableArray* buttons = [gamepad objectForKey:BUTTONS];
                if (element == data.buttonA)
                {
                    buttons[BUTTON_0_INDEX] = [NSNumber numberWithFloat:data.buttonA.value];
                }
                else if (element == data.buttonB)
                {
                    buttons[BUTTON_1_INDEX] = [NSNumber numberWithFloat:data.buttonB.value];
                }
                else if (element == data.buttonX)
                {
                    buttons[BUTTON_2_INDEX] = [NSNumber numberWithFloat:data.buttonX.value];
                }
                else if (element == data.buttonY)
                {
                    buttons[BUTTON_3_INDEX] = [NSNumber numberWithFloat:data.buttonY.value];
                }
                else if (element == data.leftShoulder)
                {
                    buttons[BUTTON_LEFT_BUMPER_INDEX] = [NSNumber numberWithFloat:data.leftShoulder.value];
                }
                else if (element == data.rightShoulder)
                {
                    buttons[BUTTON_RIGHT_BUMPER_INDEX] = [NSNumber numberWithFloat:data.rightShoulder.value];
                }
                else if (element == data.dpad)
                {
                    buttons[BUTTON_DPAD_UP_INDEX] = [NSNumber numberWithFloat:data.dpad.up.value];
                    buttons[BUTTON_DPAD_DOWN_INDEX] = [NSNumber numberWithFloat:data.dpad.down.value];
                    buttons[BUTTON_DPAD_LEFT_INDEX] = [NSNumber numberWithFloat:data.dpad.left.value];
                    buttons[BUTTON_DPAD_RIGHT_INDEX] = [NSNumber numberWithFloat:data.dpad.right.value];
                }
                
                [gamepad setObject:[NSNumber numberWithInteger:CACurrentMediaTime() * 1000.0 - initialTimeMillis] forKey:TIME_STAMP];
            }
        };
    }
    else
    {
        @throw [NSException exceptionWithName:@"Unknown problem" reason:@"Could not retrieve neither the extended nor the basic gamepad profiles from a game controller" userInfo:nil];
    }
    // If the controller does not support the extended profile, check for the basic profile.
}

-(NSMutableDictionary*)createGamepadForController:(GCController*)controller
{
    NSMutableArray* buttons = [[NSMutableArray alloc] initWithCapacity:NUMBER_OF_BUTTONS];
    for (NSInteger i = 0; i < NUMBER_OF_BUTTONS; i++)
    {
        buttons[i] = [NSNumber numberWithDouble:0.0];
    }
    NSMutableArray* axes = [[NSMutableArray alloc] initWithCapacity:NUMBER_OF_AXES];
    for (NSInteger i = 0; i < NUMBER_OF_AXES; i++)
    {
        axes[i] = [NSNumber numberWithDouble:0.0];
    }
    NSMutableDictionary* gamepad = [[NSMutableDictionary alloc] init];
    
    // Get the player index from the controller. If it hasn't been set yet, assign it the last value from the gamepads array.
    NSInteger playerIndex = controller.playerIndex;
    if (playerIndex == GCControllerPlayerIndexUnset || [self findGamepadByPlayerIndex:playerIndex])
    {
        playerIndex = gamepads.count;
        // Remember to assign the player index to the controller.
        controller.playerIndex = playerIndex;
    }
    
    // Assign the basic values.
    [gamepad setObject:controller.vendorName forKey:ID];
    [gamepad setObject:[NSNumber numberWithInteger:playerIndex] forKey:INDEX];
    [gamepad setObject:[NSNumber numberWithBool:true] forKey:CONNECTED];
    [gamepad setObject:[NSNumber numberWithInteger:CACurrentMediaTime() * 1000.0 - initialTimeMillis] forKey:TIME_STAMP];
    [gamepad setObject:@"standard" forKey:MAPPING];
    [gamepad setObject:buttons forKey:BUTTONS];
    [gamepad setObject:axes forKey:AXES];
    
    [self setupControllerToBeRead:controller];
    
    // Can release the button and axis arrays as the ownership is in the NSDictionary now.
    [buttons release];
    buttons = nil;
    [axes release];
    axes = nil;
    
    controller.controllerPausedHandler = ^(GCController* controller)
    {
    };
    
    return gamepad;
}

-(void)createGamepads
{
    [self->gamepads removeAllObjects];
    
    NSArray* controllers = [GCController controllers];
    for (NSInteger i = 0; i < controllers.count; i++)
    {
        GCController* controller = controllers[i];
        NSDictionary* gamepad = [self createGamepadForController:controller];
        [gamepads addObject:gamepad];
        [gamepad release];
        gamepad = nil;
    }
}

-(void)controllerDidConnect:(NSNotification*)notification
{
    GCController* controller = notification.object;
    NSMutableDictionary* gamepad = nil;
    NSInteger i = 0;
    for (i = 0; !gamepad && i < self->gamepads.count; i++)
    {
        gamepad = self->gamepads[i];
        NSInteger playerIndex = [(NSNumber*)[gamepad objectForKey:INDEX] integerValue];
        gamepad = controller.playerIndex == playerIndex ? gamepad : nil;
    }
    
    bool found = gamepad != nil;
    
    if (!found)
    {
        // Create a gamepad from the controller
        gamepad = [self createGamepadForController:controller];
        // Add it to the array of gamepads
        [self->gamepads addObject:gamepad];
    }
    
    // Set the gamepad to be notified in the event
    [argument setObject:gamepad forKey:GAMEPAD];
    
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:argument];
    [pluginResult setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:gamepadConnectedCommand.callbackId];
    
    if (!found)
    {
        // Release the gamepad as the reference is retained in the gamepads container.
        [gamepad release];
        gamepad = nil;
    }
}

-(void)controllerDidDisconnect:(NSNotification*)notification
{
    GCController* controller = notification.object;
    // Find the gamepad that corresponds to the controller.
    NSMutableDictionary* gamepad = nil;
    NSInteger i = 0;
    for (i = 0; !gamepad && i < self->gamepads.count; i++)
    {
        gamepad = self->gamepads[i];
        NSInteger playerIndex = [(NSNumber*)[gamepad objectForKey:INDEX] integerValue];
        gamepad = controller.playerIndex == playerIndex ? gamepad : nil;
    }
    
    // Remove the gamepad from the array of gamepads
    if (gamepad)
    {
        // Keep a reference to the gamepad as we need it to notify the event but we will remove it from the gamepads container.
        gamepad = [gamepad retain];
        [self->gamepads removeObjectAtIndex:i-1];
        // Set the gamepad to be notified in the event.
        [argument setObject:gamepad forKey:GAMEPAD];
        
        CDVPluginResult* pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:argument];
        [pluginResult setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:gamepadDisconnectedCommand.callbackId];
        
        [gamepad release];
        gamepad = nil;
    }
}

-(id)initWithWebView:(UIWebView*)theWebView
{
    self = [super initWithWebView:theWebView];
    if (self)
    {
        self->gamepads = [[NSMutableArray alloc] init];
        self->argument = [[NSMutableDictionary alloc] init];
    }
    return self;
}

-(void)dealloc
{
    if (self->gamepads)
    {
        [self->gamepads release];
        self->gamepads = nil;
    }
    if (self->argument)
    {
        [self->argument release];
        self->argument = nil;
    }
    if (gamepadConnectedCommand)
    {
        [gamepadDisconnectedCommand release];
        gamepadDisconnectedCommand = nil;
    }
    
    if (gamepadDisconnectedCommand)
    {
        [gamepadDisconnectedCommand release];
        gamepadDisconnectedCommand = nil;
    }
    [super dealloc];
}

-(void)pluginInitialize
{
    // This check allows iOS backwark compatibility. In devices with iOS version < 7.0, it will look like if the gamepad API did not even exist.
    if (![GCController class]) return;
    
    [self createGamepads];
    
    // Register for game controller connection/disconnection notifications
    NSNotificationCenter* center = [NSNotificationCenter defaultCenter];
    [center addObserver:self selector:@selector(controllerDidConnect:) name:GCControllerDidConnectNotification object:nil];
    [center addObserver:self selector:@selector(controllerDidDisconnect:) name:GCControllerDidDisconnectNotification object:nil];
    
    initialTimeMillis = CACurrentMediaTime() * 1000.0;
}

-(void)dispose
{
    // Remove the game controller related observers
    NSNotificationCenter* center = [NSNotificationCenter defaultCenter];
    [center removeObserver:self name:GCControllerDidConnectNotification object:nil];
    [center removeObserver:self name:GCControllerDidDisconnectNotification object:nil];
    
    // Remove all the gamepads
    [self->gamepads removeAllObjects];
    
    // Remove the gamepad argument
    [argument removeObjectForKey:GAMEPAD];
    
    if (gamepadConnectedCommand)
    {
        [gamepadDisconnectedCommand release];
        gamepadDisconnectedCommand = nil;
    }
    
    if (gamepadDisconnectedCommand)
    {
        [gamepadDisconnectedCommand release];
        gamepadDisconnectedCommand = nil;
    }
}

- (void)getGamepads:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:gamepads];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setGamepadConnectedCallback:(CDVInvokedUrlCommand*)command
{
    if (gamepadConnectedCommand)
    {
        [gamepadConnectedCommand release];
        gamepadConnectedCommand = nil;
    }
    gamepadConnectedCommand = [command retain];
}

- (void)setGamepadDisconnectedCallback:(CDVInvokedUrlCommand*)command
{
    if (gamepadDisconnectedCommand)
    {
        [gamepadDisconnectedCommand release];
        gamepadDisconnectedCommand = nil;
    }
    gamepadDisconnectedCommand = [command retain];
}

@end
