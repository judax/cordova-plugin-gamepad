#import <Foundation/Foundation.h>

#import <Cordova/CDVPlugin.h>

@interface CordovaPluginGamepad : CDVPlugin
{
@private
    NSMutableArray* gamepads;
    NSMutableDictionary* argument;
    double initialTimeMillis;
    CDVInvokedUrlCommand* gamepadConnectedCommand;
    CDVInvokedUrlCommand* gamepadDisconnectedCommand;
}

-(CDVPlugin*)initWithWebView:(UIWebView*)theWebView;
-(void)dealloc;
-(void)pluginInitialize;
-(void)dispose;

@end
