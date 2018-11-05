package com.floristicreactlibrary.utils;

import android.content.Context;
import android.net.Uri;
import android.support.media.ExifInterface;
import android.util.Log;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Utils {

    public static Boolean copyExifData(File sourceFile, File destFile, List<TagInfo> excludedFields) throws Exception {
        Exception finalException;

        String tempFileName = destFile.getAbsolutePath() + ".tmp";
        File tempFile = null;
        OutputStream tempStream = null;

        try {
            tempFile = new File (tempFileName);

            TiffOutputSet sourceSet = Utils.getSanselanOutputSet(sourceFile, TiffConstants.DEFAULT_TIFF_BYTE_ORDER);
            TiffOutputSet destSet = Utils.getSanselanOutputSet(destFile, sourceSet.byteOrder);

            // If the EXIF data endianess of the source and destination files
            // differ then fail. This only happens if the source and
            // destination images were created on different devices. It's
            // technically possible to copy this data by changing the byte
            // order of the data, but handling this case is outside the scope
            // of this implementation
            if (sourceSet.byteOrder != destSet.byteOrder) {
                return false;
            }

            destSet.getOrCreateExifDirectory();

            // Go through the source directories
            List<?> sourceDirectories = sourceSet.getDirectories();
            for (int i = 0; i < sourceDirectories.size(); i++) {
                TiffOutputDirectory sourceDirectory = (TiffOutputDirectory)sourceDirectories.get(i);
                TiffOutputDirectory destinationDirectory = Utils.getOrCreateExifDirectory(destSet, sourceDirectory);

                if (destinationDirectory == null) {
                    continue; // failed to create
                }

                // Loop the fields
                List<?> sourceFields = sourceDirectory.getFields();
                for (int j = 0; j < sourceFields.size(); j++) {
                    // Get the source field
                    TiffOutputField sourceField = (TiffOutputField) sourceFields.get(j);

                    // Check exclusion list
                    if (excludedFields != null && excludedFields.contains(sourceField.tagInfo)) {
                        destinationDirectory.removeField(sourceField.tagInfo);
                        continue;
                    }

                    // Remove any existing field
                    destinationDirectory.removeField(sourceField.tagInfo);

                    // Add field
                    destinationDirectory.add(sourceField);
                }
            }

            // Save data to destination
            tempStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            new ExifRewriter().updateExifMetadataLossless(destFile, tempStream, destSet);
            tempStream.close();

            // Replace file
            if (destFile.delete()) {
                tempFile.renameTo(destFile);
            }

            return true;
        }
        catch (ImageReadException exception) {
            finalException = exception;
            exception.printStackTrace();
        }
        catch (ImageWriteException exception) {
            finalException = exception;
            exception.printStackTrace();
        }
        catch (IOException exception) {
            finalException = exception;
            exception.printStackTrace();
        }
        finally {
            if (tempStream != null) {
                try {
                    tempStream.close();
                }
                catch (IOException e) {}
            }

            if (tempFile != null) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }

        if (finalException != null) {
            throw finalException;
        }

        return false;
    }

    private static TiffOutputSet getSanselanOutputSet(File jpegImageFile, int defaultByteOrder)
            throws IOException, ImageReadException, ImageWriteException {
        TiffImageMetadata exif = null;
        TiffOutputSet outputSet = null;

        IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (jpegMetadata != null) {
            exif = jpegMetadata.getExif();

            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }

        // If JPEG file contains no EXIF metadata, create an empty set
        // of EXIF metadata. Otherwise, use existing EXIF metadata to
        // keep all other existing tags
        if (outputSet == null) {
            outputSet = new TiffOutputSet(exif == null ? defaultByteOrder : exif.contents.header.byteOrder);
        }

        return outputSet;
    }

    private static TiffOutputDirectory getOrCreateExifDirectory(TiffOutputSet outputSet, TiffOutputDirectory outputDirectory) {
        TiffOutputDirectory result = outputSet.findDirectory(outputDirectory.type);
        if (result != null) {
            return result;
        }

        result = new TiffOutputDirectory(outputDirectory.type);
        try {
            outputSet.addDirectory(result);
        }
        catch (ImageWriteException e) {
            return null;
        }

        return result;
    }

    public static String getExifDate(Context context, File file) throws IOException {
        Uri uri = Uri.fromFile(file);
        InputStream in;
        in = context.getContentResolver().openInputStream(uri);

        if (in != null) {
            ExifInterface exif = new ExifInterface(in);

            Log.e("Utils::getExifDate", "ExifInterface: ok");

            if (exif.getAttribute(ExifInterface.TAG_DATETIME) != null) {
                Log.e("Utils::getExifDate", "TAG_DATETIME: found");
                return exif.getAttribute(ExifInterface.TAG_DATETIME);
            }

            if (exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED) != null) {
                Log.e("Utils::getExifDate", "TAG_DATETIME_DIGITIZED: found");
                return exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED);
            }

            if (exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) != null) {
                Log.e("Utils::getExifDate", "TAG_DATETIME_ORIGINAL: found");
                return exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
            }

            if (exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP) != null) {
                Log.e("Utils::getExifDate", "TAG_GPS_DATESTAMP: found");
                return exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
            }

            in.close();

            Log.e("Utils::getExifDate", "DATE: missing");
        }

        return null;
    }

    public static double[] getExifLatLon(Context context, File file) throws IOException {
        Uri uri = Uri.fromFile(file);
        InputStream in;
        in = context.getContentResolver().openInputStream(uri);

        if (in != null) {
            ExifInterface exif = new ExifInterface(in);

            Log.e("Utils::getExifLatLon", "ExifInterface: ok");

            if (exif.getLatLong() != null) {
                Log.e("Utils::getExifLatLon", "getLatLong: found");
                return exif.getLatLong();
            }

            in.close();

            Log.e("Utils::getExifLatLon", "getLatLong: missing");
        }

        return null;
    }

    public static String readMetadata(File file) throws ImageReadException, IOException {
        final String NAME = "printMetadata";

        String result = "" + NAME;

        IImageMetadata sanselanMetadata = Sanselan.getMetadata(file);

        if (sanselanMetadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) sanselanMetadata;
            TiffImageMetadata tiffImageMetadata = jpegMetadata.getExif();

            result += "\n" + "meta: ";
            Log.e(NAME, "meta: ");
            result += Utils.getMetadataList(jpegMetadata.getItems());

            result += "\n" + "exif: ";
            Log.e(NAME, "exif: ");
            result += Utils.getExif(tiffImageMetadata);

            result += "\n" + "gps: ";
            Log.e(NAME, "gps: ");
            TiffImageMetadata.GPSInfo gpsInfo = tiffImageMetadata.getGPS();
            result += gpsInfo.toString();
            Log.e(" - ", gpsInfo.toString());
        }

        return result;
    }

    private static String getMetadataList(List items) {
        StringBuilder result = new StringBuilder();

        for (Object item : items) {
            if (item instanceof ImageMetadata.Item) {
                ImageMetadata.Item tiffItem = (ImageMetadata.Item) item;
                Log.e(" - ", tiffItem.getText());
                Log.e(" - ", tiffItem.toString());

                result.append("\n").append(tiffItem.getText());
                result.append("\n").append(tiffItem.toString());
            }
        }

        return result.toString();
    }

    private static String getExif(TiffImageMetadata tiffImageMetadata) throws ImageReadException {
        StringBuilder result = new StringBuilder();

        for(Object field: tiffImageMetadata.getAllFields()) {
            if(field instanceof TiffField) {
                TiffField tiffField = (TiffField)field;

                result.append(tiffField.getTagName()).append(": ").append(tiffField.getValueDescription());

                Log.e(" - ", tiffField.getTagName()+ ": " + tiffField.getValueDescription());
            }
        }

        return result.toString();
    }
}
