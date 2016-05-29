package com.kazimierak.kacper.fancygallerylikephotopicker.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.lang.ref.WeakReference;

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
 * Task that decodes bitmap from given file path, resize it to match CardView's size in order to
 * keep memory usage at low level.
 *
 * Decoding and resizing is done off the main UI thread and bound to ImageView using WeakReference
 * so when ImageView is recycled in RecyclerView (e.g. when view is scrolled off the screen) loading
 * is stopped.
 */

public class BitmapLoaderTask extends AsyncTask<String, Void, Bitmap> {

    /*
     * Reference to ImageView that will show loaded bitmap, weak reference does not prevent object
     * to be recycled by garbage collector, e.g. when ItemHolder in recycler view is off the screen.
     */
    private final WeakReference<ImageView> imageViewReference;
    private int size;
    private String path = "";

    public BitmapLoaderTask(ImageView imageView, int size) {
        imageViewReference = new WeakReference<>(imageView);
        this.size = size;
    }

    public void setSize(int size) { this.size = size; }

    /*
     * Task done off the main ui thread
     */
    @Override
    protected Bitmap doInBackground(String... params) {
        path = params[0];
        PhotoLoader loader = new PhotoLoader();
        return loader.decodePhoto(path, size, size);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)  {
        if (isCancelled()) {
            bitmap = null;
        }

        /*
         * Check if weak reference still exist and container was not recycled
         */
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapLoaderTask bitmapLoaderTask = getBitmapLoaderTask(imageView);

            if (this == bitmapLoaderTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * Loads bitmap off the main UI thread, using weak reference to photo container, so when
     * container is recycled loading is stopped.
     *
     * @param context Activity context
     * @param photoPath Path to the photo
     * @param imageView Photo container
     * @param placeholderBitmap Placeholder image to show when actual photo is loading
     */
    public void loadBitmap (Context context, String photoPath, ImageView imageView, Bitmap placeholderBitmap) {
        if (cancelPotentialWork(photoPath, imageView)) {
            final BitmapLoaderTask task = new BitmapLoaderTask(imageView, size);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(context.getResources(), placeholderBitmap, task);

            imageView.setImageDrawable(asyncDrawable);
            task.execute(photoPath);
        }
    }

    /**
     * Cancel current loading operation if container was recycled, or photo is already loaded
     * @param data File path to loaded photo
     * @param imageView reference to ImageView that is meant to show bitmap
     *
     * @return true if photo from given path is already loaded, or ImageView was recycled
     */
    public static boolean cancelPotentialWork (String data, ImageView imageView) {
        final BitmapLoaderTask bitmapLoaderTask = getBitmapLoaderTask(imageView);

        if (bitmapLoaderTask != null) {
            final String bitmapPath = bitmapLoaderTask.path;

            // If bitmapPath is not yet set or it differs from the new data
            if ( bitmapPath.isEmpty() || !bitmapPath.equals(data)) {
                // Cancel previous task
                bitmapLoaderTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    /*
     * Get getBitmapLoaderTask from AsyncDrawable to check if is not duplicated
     */
    private static BitmapLoaderTask getBitmapLoaderTask (ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapLoaderTask();
            }
        }
        return null;
    }

    /**     Helper class for BitmapLoaderTask, extending BitmapDrawable. Uses WeakReference to
     *   BitmapLoaderTask to allow garbage collector recycle it when is no longer needed.
     **/
    public class AsyncDrawable extends android.graphics.drawable.BitmapDrawable {

        private final WeakReference<BitmapLoaderTask> bitmapLoaderTaskWeakReference;

        public AsyncDrawable (Resources res, Bitmap bitmap, BitmapLoaderTask bitmapLoaderTask) {
            super(res, bitmap);
            bitmapLoaderTaskWeakReference = new WeakReference<>(bitmapLoaderTask);
        }

        public BitmapLoaderTask getBitmapLoaderTask() {
            return bitmapLoaderTaskWeakReference.get();
        }
    }
}
