
# react-native-read-write-exif

currently available for Android (iOS version is coming soon)

## Getting started

`$ npm install react-native-read-write-exif --save`

or

`$ npm install git+https://github.com/floristic-project/react-native-read-write-exif.git --save`

### Mostly automatic installation

`$ react-native link react-native-read-write-exif`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.floristicreactlibrary.RNReadWriteExifPackage;` to the imports at the top of the file
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

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-read-write-exif` and add `RNReadWriteExif.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNReadWriteExif.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

## Usage

```javascript
import RNReadWriteExif from 'react-native-read-write-exif';

/* ******* ******* ******* ******* ******* ******* *******
 * COPY EXIF from src to dest
 ******* ******* ******* ******* ******* ******* ******* */

// callback version
RNReadWriteExif.copyExifCallback(srcUri, destUri,
(error) => {
	console.log(error);
},
(succeeded) => {
	console.log(succeeded); // true or false
});

// promise version
try {
	const succeeded = await RNReadWriteExif.copyExifPromise(srcUri, destUri);
	console.log(succeeded); // true or false
} catch (error) {
	console.log(error);
}

/* ******* ******* ******* ******* ******* ******* *******
 * READ EXIF DATE
 ******* ******* ******* ******* ******* ******* ******* */

 // callback version
RNReadWriteExif.readExifDateCallback(uri,
(error) => {
	console.log(error);
},
(date) => {
	console.log(date); // "a string date"
});

// promise version
try {
	const date = await RNReadWriteExif.readExifDatePromise(uri);
	console.log(date); // "a string date"
} catch (error) {
	console.log(error);
}

/* ******* ******* ******* ******* ******* ******* *******
 * READ EXIF GEO DATA (GPS)
 ******* ******* ******* ******* ******* ******* ******* */

 // callback version
RNReadWriteExif.readExifLatLonCallback(uri,
(error) => {
	console.log(error);
},
(geo) => {
	console.log(geo); // { lat: 43.01, lon: 3.01 }
});

// promise version
try {
	const { lat, lon } = await RNReadWriteExif.readExifLatLonPromise(uri);
	console.log(lat); // 43.01
	console.log(lon); // 3.01
} catch (error) {
	console.log(error);
}

/* ******* ******* ******* ******* ******* ******* *******
 * READ EXIF & META
 ******* ******* ******* ******* ******* ******* ******* */

 // callback version
RNReadWriteExif.readExifMetaCallback(uri,
(error) => {
	console.log(error);
},
(result) => {
	console.log(result); // "a string with all readable data"
});

// promise version
try {
	const result = await RNReadWriteExif.readExifMetaPromise(uri);
	console.log(result); // "a string with all readable data"
} catch (error) {
	console.log(error);
}

//

RNReadWriteExif;
```
