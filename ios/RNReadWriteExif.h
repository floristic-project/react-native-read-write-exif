
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

/*
 * A native module is just an Objective-C class that implements the
 * RCTBridgeModule protocol.
 */

@interface RNReadWriteExif : NSObject <RCTBridgeModule>

@end
