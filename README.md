
# react-native-read-write-exif

## Getting started

`$ npm install react-native-read-write-exif --save`

### Mostly automatic installation

`$ react-native link react-native-read-write-exif`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-read-write-exif` and add `RNReadWriteExif.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNReadWriteExif.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReadWriteExifPackage;` to the imports at the top of the file
  - Add `new RNReadWriteExifPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-read-write-exif'
  	project(':react-native-read-write-exif').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-read-write-exif/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-read-write-exif')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNReadWriteExif.sln` in `node_modules/react-native-read-write-exif/windows/RNReadWriteExif.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Read.Write.Exif.RNReadWriteExif;` to the usings at the top of the file
  - Add `new RNReadWriteExifPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNReadWriteExif from 'react-native-read-write-exif';

/*
 * copy exif from src to dest
 */

// callback version
RNReadWriteExif.copyExif(srcUri, destUri, (error) => {...}, (succeeded) => {...});

// promise version
try {
	const succeeded = await RNReadWriteExif.copyExifPromise(srcUri, destUri);
	console.log(succeeded);
} catch (error) {
	console.log(error);
}

/*
 * read exif date
 */
 
 // callback version
RNReadWriteExif.readExifDate(uri, (error) => {...}, (date) => {...});

// promise version
try {
	const date = await RNReadWriteExif.readExifDatePromise(uri);
	console.log(date);
} catch (error) {
	console.log(error);
}

/*
 * read exif geo data (GPS)
 */
 
 // callback version
RNReadWriteExif.readExifLatLon(uri, (error) => {...}, (Object) => {...});

// promise version
try {
	const { lat, lon } = await RNReadWriteExif.readExifLatLonPromise(uri);
	console.log(lat);
	console.log(lon);
} catch (error) {
	console.log(error);
}

RNReadWriteExif;
```
  