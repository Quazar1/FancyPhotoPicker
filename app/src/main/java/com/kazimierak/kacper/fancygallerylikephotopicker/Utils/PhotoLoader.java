package com.kazimierak.kacper.fancygallerylikephotopicker.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


/**
 * Created by Kacper Kazimierak 2016-05-29.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Kacper Kazimierak
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 * Class handling Loading photo from device memory. Good idea is to use decodePhoto function
 * off the main IU thread, because converting larger images to bitmap can take longer time and cause
 * "Application Not Responding" error.
 */

public class PhotoLoader {

    public PhotoLoader() {}

    /**
        Method checking if bitmap decoded from given path is significantly larger than device screen.
        If width or height is at least scaleFactor * size returns true

        @param path The path to resource object
        @param screenWidth The width of device screen
        @param screenHeight The height of device screen
        @param scaleFactor The size factor which multiply screen sizes.

        @return true if bitmap is greater than screen dimensions
     */
    public boolean isPhotoGreaterThanScreen(String path, int screenWidth, int screenHeight, int scaleFactor) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // True to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        final int height = options.outHeight;
        final int width = options.outWidth;
        // TODO

        if (((height*width) > (scaleFactor*(screenWidth*screenHeight)))){
            options.inJustDecodeBounds = false;
            return true;
        } else {
            options.inJustDecodeBounds = false;
            return false;
        }
    }

    /**
     Method checking bitmap decoded from given path is larger than device screen.

     @param pathToThePicture The path to resource object
     @param screenWidth The width of device screen
     @param screenHeight The height of device screen
     @return true if bitmap is greater than screen dimensions
     */
    public boolean isPhotoGreaterThanScreen(String pathToThePicture, int screenWidth, int screenHeight) {

        return (isPhotoGreaterThanScreen(pathToThePicture, screenWidth, screenHeight, 1));
    }

    /**
     Method checking if given bitmap is significantly larger than device screen.
     If width or height is at least scaleFactor * size returns true

     @param bmp The bitmap to check size
     @param screenWidth The width of device screen
     @param screenHeight The height of device screen
     @param scaleFactor The size factor which multiply screen sizes.

     @return true if bitmap is greater than screen dimensions
     */
    public boolean isPhotoGreaterThanScreen(Bitmap bmp, int screenWidth, int screenHeight, int scaleFactor) {

        final int height = bmp.getHeight();
        final int width = bmp.getWidth();

        return (((height*width) > (scaleFactor*(screenWidth*screenHeight))));
    }

    /**
     Method checking if given bitmap is larger than device screen.

     @param bitmap The bitmap to check size
     @param screenWidth The width of device screen
     @param screenHeight The height of device screen
     @return true if bitmap is greater than screen dimensions
     */
    public boolean isPhotoGreaterThanScreen(Bitmap bitmap, int screenWidth, int screenHeight) {
        return (isPhotoGreaterThanScreen(bitmap, screenWidth, screenHeight, 1));
    }

    /**
     * Decode bitmap from given path, adjusted to device screen size
     *
     * @param imagePath Path to resource to be decoded
     * @param screenWidth The width of device screen
     * @param screenHeight The height of device screen
     *
     * @return decoded and scaled bitmap
     */
    public Bitmap decodePhoto(String imagePath, int screenWidth, int screenHeight) {
        Bitmap tempBitmap;

        if (isPhotoGreaterThanScreen(imagePath, screenWidth, screenHeight)) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            PhotoScaler scaler = new PhotoScaler();

            // True to check dimensions
            options.inJustDecodeBounds = true;

            //Just calculate dimensions
            BitmapFactory.decodeFile(imagePath,options);

            //Calc. inSampleSize
            options.inSampleSize = scaler.calculateInSampleSize(options, screenWidth, screenHeight);
            //Now decode the full bitmap
            options.inJustDecodeBounds = false;
            tempBitmap = BitmapFactory.decodeFile(imagePath, options);
        } else {
            tempBitmap = BitmapFactory.decodeFile(imagePath);
        }
        if (tempBitmap == null) throw new NullPointerException("Bitmap decoded from path is null" + imagePath);
        else return tempBitmap;
    }

    /**
     * Decode bitmap from given path, regardless of size
     *
     * @param imagePath Path to resource picture
     * @return  Decoded bitmap
     */
    public Bitmap decodePhoto(String imagePath) {
        return  BitmapFactory.decodeFile(imagePath);
    }

    /**
      * Class scaling given bitmap down (or up) to given size.
      */
    public class PhotoScaler {

        private final static String DEBUG_TAG = "PhotoScaler";
        private static final boolean LOCAL_LOG = true;

        public PhotoScaler(){
        }

        /**
         * Calculates largest sample size value that is a power of 2 and keeps both height and width
         * larger than the requested height and width.
         *
         * @param options Bitmap factory options
         * @param reqWidth Target width
         * @param reqHeight Target Height
         * @return Sample size value that is a power of 2 and keeps both height and width
         * larger than the requested height and width.
         */
        public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            //Height and width of the raw image
            final int height = options.outHeight;
            final int width = options.outWidth;
            //Default sample size = 1
            int inSampleSize = 1;
            if((height > reqHeight) || (width >reqWidth)) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }
            if (LOCAL_LOG) Log.d(DEBUG_TAG, "Sample size: " + inSampleSize);
            return inSampleSize;
        }

        /**
         * Decoded bitmap with given max longest border
         *
         * @param bmp Bitmap to resize
         * @param maxBorder Maximal value of longest border of the photo
         * @return Scaled bitmap
         */
        public Bitmap scaleDownBitmap (Bitmap bmp, int maxBorder) {
            int width = bmp.getWidth();
            int height = bmp.getHeight();

            float bitmapRatio = (float)width / (float) height;
            if (bitmapRatio > 1) {
                width = maxBorder;
                height = (int) (width / bitmapRatio);
            } else {
                height = maxBorder;
                width = (int) (height * bitmapRatio);
            }
            return Bitmap.createScaledBitmap(bmp, width, height, true);
        }

        /**
         * Scale bitmap to given width and height
         *
         * @param bmp Bitmap to resize
         * @param width Target width
         * @param height Target height
         * @return Scaled bitmap
         */
        public Bitmap scaleDownBitmap (Bitmap bmp, int width, int height) {
            return Bitmap.createScaledBitmap(bmp, width, height, true);
        }

        /**
         * Scale bitmap with given width
         *
         * @param bmp Bitmap to resize
         * @param maxWidth Target width
         * @return Scaled bitmap with retained ratio
         */
        public Bitmap scaleDownBitmapWidth (Bitmap bmp, int maxWidth) {
            int width = bmp.getWidth();
            int height = bmp.getHeight();

            float bitmapRatio = (float) maxWidth / (float)width;
            height = (int) (height * bitmapRatio);
            return Bitmap.createScaledBitmap(bmp, width, height, true);
        }

        /**
         * Scale bitmap with given height
         *
         * @param bmp Bitmap to resize
         * @param maxHeight Target height
         * @return Scaled bitmap with retained ratio
         */
        public Bitmap scaleDownBitmapHeight (Bitmap bmp, int maxHeight) {
            int width = bmp.getWidth();
            int height = bmp.getHeight();

            float bitmapRatio = (float) maxHeight / (float)height;
            width = (int) (width * bitmapRatio);
            return Bitmap.createScaledBitmap(bmp, width, height, true);
        }
    }
}
