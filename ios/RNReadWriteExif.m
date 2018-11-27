
#import "RNReadWriteExif.h"
#import <React/RCTLog.h>

/*
 * Your class must also include the RCT_EXPORT_MODULE() macro. This takes an
 * optional argument that specifies the name that the module will be accessible
 * as in your JavaScript code (more on this later). If you do not specify a
 * name, the JavaScript module name will match the Objective-C class name.
 * If the Objective-C class name begins with RCT, the JavaScript module name
 * will exclude the RCT prefix.
 */

@implementation RNReadWriteExif

/*
 * The native module should not have any assumptions about what thread it is
 * being called on. React Native invokes native modules methods on a separate
 * serial GCD queue, but this is an implementation detail and might change.
 * The - (dispatch_queue_t)methodQueue method allows the native module to
 * specify which queue its methods should be run on. For example, if it needs
 * to use a main-thread-only iOS API, it should specify this via:
 * return dispatch_get_main_queue();
 *
 * Similarly, if an operation may take a long time to complete, the native
 * module should not block and can specify it's own queue to run operations on.
 *
 * The specified methodQueue will be shared by all of the methods in your
 * module. If just one of your methods is long-running (or needs to be run on
 * a different queue than the others for some reason), you can use
 * dispatch_async inside the method to perform that particular method's code
 * on another queue, without affecting the others.
 */

- (dispatch_queue_t)methodQueue
{
    // return dispatch_get_main_queue();
	return dispatch_queue_create("com.floristicreactlibrary.RNReadWriteExifQueue", DISPATCH_QUEUE_SERIAL);
}
RCT_EXPORT_MODULE() // default: "RNReadWriteExif"
// RCT_EXPORT_MODULE(RNReadWriteExif); // custom

/*
 * RCT_EXPORT_METHOD supports all standard JSON object types, such as:
 *
 *    string (NSString)
 *    number (NSInteger, float, double, CGFloat, NSNumber)
 *    boolean (BOOL, NSNumber)
 *    array (NSArray) of any types from this list
 *    object (NSDictionary) with string keys and values of any type from this list
 *    function (RCTResponseSenderBlock)
 *
 * But it also works with any type that is supported by the RCTConvert class
 * (see RCTConvert for details). The RCTConvert helper functions all accept a
 * JSON value as input and map it to a native Objective-C type or class.
 */

/*
 * A native module can export constants that are immediately available to
 * JavaScript at runtime. This is useful for communicating static data that
 * would otherwise require a round-trip through the bridge.
 */

 - (NSDictionary *)constantsToExport
 {
	 return @{
		 //@"key": @"value"
	 };
}

/*
 * Native modules also supports a special kind of argument- a callback.
 * In most cases it is used to provide the function call result to JavaScript.
 *
 * RCTResponseSenderBlock accepts only one argument - an array of parameters
 * to pass to the JavaScript callback. In this case we use Node's convention
 * to make the first parameter an error object (usually null when there is no
 * error) and the rest are the results of the function.
 *
 * A native module should invoke its callback exactly once. It's okay to store
 * the callback and invoke it later. This pattern is often used to wrap iOS
 * APIs that require delegates - see RCTAlertManager for an example. If the
 * callback is never invoked, some memory is leaked. If both onSuccess and
 * onFail callbacks are passed, you should only invoke one of them.
 *
 * If you want to pass error-like objects to JavaScript, use RCTMakeError from
 * RCTUtils.h. Right now this just passes an Error-shaped dictionary to
 * JavaScript, but we would like to automatically generate real JavaScript
 * Error objects in the future.
 *
 * Native modules can also fulfill a promise, which can simplify your code,
 * especially when using ES2016's async/await syntax. When the last parameters
 * of a bridged native method are an RCTPromiseResolveBlock and
 * RCTPromiseRejectBlock, its corresponding JS method will return a JS Promise
 * object.
 */

RCT_EXPORT_METHOD(copyExifCallback:(NSString *)srcUri
				destUri:(NSString *)destUri
				errorCallback:(RCTResponseSenderBlock)errorCallback
				successCallback:(RCTResponseSenderBlock)successCallback)
{
	RCTLogInfo(@"copyExifCallback from %@ to %@", srcUri, destUri);

	NSFileManager *fileManager = [NSFileManager defaultManager];

	if (![fileManager fileExistsAtPath:srcUri]) {
		NSError *error = [NSError errorWithDomain:@"com.floristicreactlibrary"
									code:404
									userInfo:@"src file not found"];
		errorCallback(@[error, [NSNull null]]);
		return;
	}

	if (![fileManager fileExistsAtPath:destUri]) {
		NSError *error = [NSError errorWithDomain:@"com.floristicreactlibrary"
									code:404
									userInfo:@"dest file not found"];
		errorCallback(@[error, [NSNull null]]);
		return;
	}

    successCallback(@[[self copyExifFrom:srcUri to:destUri], [NSNull null]]);
}

RCT_EXPORT_METHOD(copyExifPromise:(NSString *)srcUri destUri:(NSString *)destUri
				resolver:(RCTPromiseResolveBlock)resolve
				rejecter:(RCTPromiseRejectBlock)reject) {
	RCTLogInfo(@"copyExifPromise from %@ to %@", srcUri, destUri);

	NSFileManager *fileManager = [NSFileManager defaultManager];

	if (![fileManager fileExistsAtPath:srcUri]) {
		NSError *error = [NSError errorWithDomain:@"com.floristicreactlibrary"
									code:404
									userInfo:@"src file not found"];
		reject(@"missing_file", @"File at srcUri not found", error);
		return;
	}

	if (![fileManager fileExistsAtPath:destUri]) {
		NSError *error = [NSError errorWithDomain:@"com.floristicreactlibrary"
									code:404
									userInfo:@"dest file not found"];
		reject(@"missing_file", @"File at destUri not found", error);
		return;
	}

    resolve([self copyExifFrom:srcUri to:destUri]);
}

- (void) copyExifFrom:(NSString *)src to:(NSString *)dest {

    // open source and dest images
    try {
        NSData *imageData = [NSData dataWithContentsOfFile:src];
        NSData *stubData = [NSData dataWithContentsOfFile:dest];
    }
    @catch (NSException *exception) {
        NSMutableDictionary * info = [NSMutableDictionary dictionary];
        [info setValue:exception.name forKey:@"ExceptionName"];
        [info setValue:exception.reason forKey:@"ExceptionReason"];
        [info setValue:exception.callStackReturnAddresses forKey:@"ExceptionCallStackReturnAddresses"];
        [info setValue:exception.callStackSymbols forKey:@"ExceptionCallStackSymbols"];
        [info setValue:exception.userInfo forKey:@"ExceptionUserInfo"];
        
        NSError *error = [[NSError alloc] initWithDomain:@"com.floristicreactlibrary"
                                                    code:404 userInfo:info];
        
        reject(@"reading_file", @"File cannot be read", error);
    }
    
    // read image basic metadata
    CGImageSourceRef  cgSource ;
    cgSource = CGImageSourceCreateWithData((__bridge CFDataRef)imageData, NULL);
    NSDictionary *metadataNew = (__bridge NSDictionary *) CGImageSourceCopyPropertiesAtIndex(cgSource,0,NULL);

    // write metadata to dest image
    CFStringRef UTI = CGImageSourceGetType(cgSource); // image mimetype
    NSMutableData *dest_data = [NSMutableData data];
    CGImageDestinationRef destination = CGImageDestinationCreateWithData((CFMutableDataRef)dest_data,UTI,1,NULL);
    // inject stub image into destination image
    CGImageSourceRef cgStub = CGImageSourceCreateWithData((__bridge CFDataRef)stubData, NULL);
    CGImageDestinationAddImageFromSource(destination,cgStub,0, (CFDictionaryRef) metadataNew);

    CGImageDestinationFinalize(destination); // @WARNING this might return false (BOOL:NO)

    //overwrite destination file
    [dest_data writeToFile:dest atomically:YES];

    // free memory
	CFRelease(cgSource);
    CFRelease(cgStub);
    CFRelease(destination);
}

@end
