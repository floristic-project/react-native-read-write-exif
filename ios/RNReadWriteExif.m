
#import "RNReadWriteExif.h"
#import <React/RCTLog.h>
#import <ImageIO/ImageIO.h>

#import <CoreLocation/CoreLocation.h>
#import <Photos/PHPhotoLibrary.h>

#import <Photos/PHAssetCollectionChangeRequest.h>


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

- (dispatch_queue_t)methodQueue {
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

 - (NSDictionary *)constantsToExport {
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

RCT_EXPORT_METHOD(copyExifCallback:(NSString *)srcUri destUri:(NSString *)destUri exif:(NSDictionary *)exif
				errorCallback:(RCTResponseSenderBlock)errorCallback
				successCallback:(RCTResponseSenderBlock)successCallback) {
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

    [self copyExifFrom:srcUri to:destUri existingExif:exif]; //@WARNING MIGHT THROW EXCEPTION

    successCallback(@[srcUri, [NSNull null]]);
}

RCT_EXPORT_METHOD(copyExifPromise:(NSString *)srcUri destUri:(NSString *)destUri exif:(NSDictionary *)exif
				resolver:(RCTPromiseResolveBlock)resolve
				rejecter:(RCTPromiseRejectBlock)reject) {
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

    [self copyExifFrom:srcUri to:destUri existingExif:exif]; // @WARNING MIGHT THROW EXCEPTION

    resolve(srcUri);
}

- (void) copyExifFrom:(NSString *)src to:(NSString *)dest existingExif:(NSDictionary *)exif {

    RCTLogInfo(@"EXIF SOURCE: %@", exif);

    NSData *imageData = [NSData dataWithContentsOfFile:src];
    NSData *stubData = [NSData dataWithContentsOfFile:dest];
    // read image basic metadata
    CGImageSourceRef  cgSource ;
    cgSource = CGImageSourceCreateWithData((__bridge CFDataRef)imageData, NULL);
    CFStringRef UTI = CGImageSourceGetType(cgSource); // image mimetype
    RCTLogInfo(@"UTI: %@", UTI);
    // read destination image for further EXIF writing
    CGImageSourceRef cgStub = CGImageSourceCreateWithData((__bridge CFDataRef)stubData, NULL);


    // Writable stuff for destination image
    NSMutableData *dest_data = [NSMutableData data];
    NSMutableDictionary *new_metadata = [[NSMutableDictionary alloc] init];
    //[new_metadata addEntriesFromDictionary:exif];

    
    // a) Create TIFF dictionary
    NSMutableDictionary *tiffDictionnary = [[NSMutableDictionary alloc] init];

    // Create formatted date into TIFF dictionary
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat:@"yyyy:MM:dd HH:mm:ss"];
    //NSDate *imageDate = [dateFormat dateFromString:[tiffDic objectForKey:(NSString *)kCGImagePropertyTIFFDateTime]];
    // @TODO PUT DATE FROM ORIGINAL EXIF !!
    [tiffDictionnary setObject:[dateFormat stringFromDate:[NSDate date]] forKey:(NSString *)kCGImagePropertyTIFFDateTime];

    // Set other metadata into TIFF dictionary
    //[tiffDictionnary setValue:@"6" forKey:(NSString *)kCGImagePropertyTIFFOrientation];
    [tiffDictionnary setValue:@"Apple du cul kipu" forKey:(NSString *)kCGImagePropertyTIFFMake];
    [tiffDictionnary setValue:[[UIDevice currentDevice] model] forKey:(NSString *)kCGImagePropertyTIFFModel];

    // set TIFF dictionary in metadata dictionary
    [new_metadata setObject:tiffDictionnary forKey:(__bridge NSString *)kCGImagePropertyTIFFDictionary];
    RCTLogInfo(@"New TIFF dict: %@", tiffDictionnary);
    
    
    // a 1/2) create Exif dictionary
    NSMutableDictionary *exifDictionnary = [[NSMutableDictionary alloc] init];
    [exifDictionnary setObject:[dateFormat stringFromDate:[NSDate date]] forKey:(NSString *)kCGImagePropertyExifDateTimeOriginal];

    // set Exif dictionary in metadata dictionary
    [new_metadata setObject:exifDictionnary forKey:(__bridge NSString *)kCGImagePropertyExifDictionary];
    RCTLogInfo(@"New Exif dict: %@", exifDictionnary);

    
    // b) Create GPS dictionary
    NSMutableDictionary *gpsDictionnary = [[NSMutableDictionary alloc] init];
    [gpsDictionnary setValue:@"12.34567" forKey:(NSString *)kCGImagePropertyGPSLatitude];
    [gpsDictionnary setValue:@"76.54321" forKey:(NSString *)kCGImagePropertyGPSLongitude];
    [gpsDictionnary setValue:@"N" forKey:@"LatitudeRef"];
    [gpsDictionnary setValue:@"B" forKey:@"LongitudeRef"];

    //CLLocationDegrees *latitude = &lat;
    //CLLocationDegrees *longitude = &lon;
    //CLLocation *exifLocation = [[CLLocation alloc] initWithLatitude:*latitude longitude:*longitude];

    // set GPS dictionary in metadata dictionary
    [new_metadata setObject:gpsDictionnary forKey:(__bridge NSString *)kCGImagePropertyGPSDictionary];
    RCTLogInfo(@"New GPS dict: %@", gpsDictionnary);

    
    // c) Add other metadata into root dictionary
    [new_metadata setObject:@"6" forKey:(__bridge NSString *)kCGImagePropertyOrientation];
    // test
    [new_metadata setObject:@"Le Exif maker est un con" forKey:(__bridge NSString *)kCGImagePropertyExifMakerNote];
    [new_metadata setObject:@"Coucou les neuneus" forKey:(__bridge NSString *)kCGImagePropertyExifUserComment];
    [new_metadata setObject:@"2000-01-01 18:32:23" forKey:(__bridge NSString *)kCGImagePropertyExifDateTimeOriginal];
    RCTLogInfo(@"New GLOBAL dict: %@", new_metadata);


    // Create and write destination image
    CGImageDestinationRef destination = CGImageDestinationCreateWithData((__bridge CFMutableDataRef)dest_data,UTI,1,NULL);
    if(!destination) {
        NSLog(@"***Could not create image destination ***");
    }
    // add the image contained in the image source to the destination,
    // overidding the old metadata with our modified metadata
    CGImageDestinationAddImageFromSource(destination,cgStub,0, (__bridge CFDictionaryRef)(exif));
    //CGImageDestinationAddImageFromSource(destination,cgStub,0, (__bridge CFDictionaryRef)(new_metadata));
    BOOL success;
    success = CGImageDestinationFinalize(destination);
    RCTLogInfo(@"Finalize: %d", success);

    //overwrite destination file
    [dest_data writeToFile:dest atomically:YES];
    RCTLogInfo(@">>> Written to: %@", dest);


    /*// write metadata to dest image
    NSMutableData *dest_data = [NSMutableData data];
    CGImageDestinationRef destination = CGImageDestinationCreateWithData((__bridge CFMutableDataRef)dest_data,UTI,1,NULL);
    // inject stub image into destination image
    CGImageSourceRef cgStub = CGImageSourceCreateWithData((__bridge CFDataRef)stubData, NULL);
    CGImageDestinationAddImageFromSource(destination,cgStub,0, (__bridge CFDictionaryRef) exif);

    CGImageDestinationFinalize(destination); // @WARNING this might return false (BOOL:NO)

    //overwrite destination file
    [dest_data writeToFile:dest atomically:YES];

    // free memory
     */
	CFRelease(cgSource);
    CFRelease(cgStub);
    CFRelease(destination);
    
    
    RCTLogInfo(@"%@", @"DONE les copains");
    
    // re-read pour voir
    RCTLogInfo(@">>> Reading again from: %@", dest);
    NSData *destNewData = [NSData dataWithContentsOfFile:dest];
    CGImageSourceRef  cgNewDest ;
    cgNewDest = CGImageSourceCreateWithData((__bridge CFDataRef)destNewData, NULL);
    NSDictionary *newDestMeta = (__bridge NSDictionary *) CGImageSourceCopyPropertiesAtIndex(cgNewDest,0,NULL);
    RCTLogInfo(@"NEW DEST META: %@", newDestMeta);
    
}

- (NSDictionary *)getGPSDictionaryForLocation:(CLLocation *)location {
    
    NSMutableDictionary *gps = [NSMutableDictionary dictionary];
    
    
    
    // GPS tag version
    
    [gps setObject:@"2.2.0.0" forKey:(NSString *)kCGImagePropertyGPSVersion];
    
    
    
    // Time and date must be provided as strings, not as an NSDate object
    
    NSDate *timestamp = location.timestamp;
    
    if (timestamp) {
        
        NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
        
        [formatter setDateFormat:@"HH:mm:ss.SSSSSS"];
        
        [formatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"UTC"]];
        
        [gps setObject:[formatter stringFromDate:location.timestamp] forKey:(NSString *)kCGImagePropertyGPSTimeStamp];
        
        [formatter setDateFormat:@"yyyy:MM:dd"];
        
        [gps setObject:[formatter stringFromDate:location.timestamp] forKey:(NSString *)kCGImagePropertyGPSDateStamp];
        
    }
    
    
    
    // Latitude
    
    CGFloat latitude = (CGFloat)location.coordinate.latitude;
    
    if (latitude) {
        
        if (latitude < 0) {
            
            latitude = -latitude;
            
            [gps setObject:@"S" forKey:(NSString *)kCGImagePropertyGPSLatitudeRef];
            
        } else {
            
            [gps setObject:@"N" forKey:(NSString *)kCGImagePropertyGPSLatitudeRef];
            
        }
        
        [gps setObject:[NSNumber numberWithFloat:latitude] forKey:(NSString *)kCGImagePropertyGPSLatitude];
        
    }
    
    
    
    // Longitude
    
    CGFloat longitude = (CGFloat)location.coordinate.longitude;
    
    if (longitude) {
        
        if (longitude < 0) {
            
            longitude = -longitude;
            
            [gps setObject:@"W" forKey:(NSString *)kCGImagePropertyGPSLongitudeRef];
            
        } else {
            
            [gps setObject:@"E" forKey:(NSString *)kCGImagePropertyGPSLongitudeRef];
            
        }
        
        [gps setObject:[NSNumber numberWithFloat:longitude] forKey:(NSString *)kCGImagePropertyGPSLongitude];
        
    }
    
    
    
    // Altitude
    
    CGFloat altitude = (CGFloat)location.altitude;
    
    if (altitude) {
        
        if (!isnan(altitude)){
            
            if (altitude < 0) {
                
                altitude = -altitude;
                
                [gps setObject:@"1" forKey:(NSString *)kCGImagePropertyGPSAltitudeRef];
                
            } else {
                
                [gps setObject:@"0" forKey:(NSString *)kCGImagePropertyGPSAltitudeRef];
                
            }
            
            [gps setObject:[NSNumber numberWithFloat:altitude] forKey:(NSString *)kCGImagePropertyGPSAltitude];
            
        }
        
    }
    
    
    
    // Speed, must be converted from m/s to km/h
    
    if (location.speed) {
        
        if (location.speed >= 0){
            
            [gps setObject:@"K" forKey:(NSString *)kCGImagePropertyGPSSpeedRef];
            
            [gps setObject:[NSNumber numberWithFloat:(CGFloat)location.speed*3.6] forKey:(NSString *)kCGImagePropertyGPSSpeed];
            
        }
        
    }
    
    
    
    // Heading
    
    if (location.course) {
        
        if (location.course >= 0){
            
            [gps setObject:@"T" forKey:(NSString *)kCGImagePropertyGPSTrackRef];
            
            [gps setObject:[NSNumber numberWithFloat:(CGFloat)location.course] forKey:(NSString *)kCGImagePropertyGPSTrack];
            
        }
        
    }
    
    
    
    return gps;
    
}


@end
