package com.kazimierak.kacper.fancygallerylikephotopicker.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.kazimierak.kacper.fancygallerylikephotopicker.R;
import com.kazimierak.kacper.fancygallerylikephotopicker.lists.GalleryRecyclerAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
 * This fragment responsible for showing recycler view.
 */
public class LoadPhotoFragment extends Fragment implements GalleryRecyclerAdapter.OnItemClickListener {

    /**
     * Maximal number of offscreen views to be held by recycler adapter (2 full rows by default)
     */
    private static final int MAX_RECYCLED_PHOTO_COUNT = 16;

    /**
     * Number of columns presented by recycler view
     */
    private static final int GALLERY_COLS = 4;

    private OnPhotoLoaded mCallback;
    private GalleryRecyclerAdapter rAdapter;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback = new ActionModeCallback();

    /**
     * Relative layout overlaying recyclerview that shows spinning progress bar
     */
    private RelativeLayout progressBarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*
            Inflate layout
         */
        RelativeLayout mRelativeLayout = (RelativeLayout)
                inflater.inflate(R.layout.fragment_load_photo, container, false);

        /*
           Horizontal scroll view that shows recent photos
         */
        RecyclerView galleryView = (RecyclerView) mRelativeLayout.findViewById(R.id.galleryView);
        galleryView.setHasFixedSize(true);
        /*
            LinearLayoutManager for RecyclerView
         */
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(getActivity().getApplicationContext(), GALLERY_COLS);

        /*
            Initialize recycler view adapter
         */
        rAdapter = new GalleryRecyclerAdapter(getActivity());

        progressBarView = (RelativeLayout) mRelativeLayout.findViewById(R.id.loadOverlay);
        hideProgressBar();

        /*
            Initializing gallery preview
         */
        rAdapter.setOnItemClickListener(this);
        galleryView.setAdapter(rAdapter);
        galleryView.getRecycledViewPool().setMaxRecycledViews(rAdapter.getItemViewType(0),
                MAX_RECYCLED_PHOTO_COUNT);
        galleryView.setLayoutManager(layoutManager);
        prepareGallery();

        return mRelativeLayout;
    }

    /**
     * Prepare files for gallery preview
     */
    private void prepareGallery() {
        final String externalStoragePath = Environment.getExternalStorageDirectory() + "/" +
                Environment.DIRECTORY_DCIM;
        final File startingFile = new File(externalStoragePath);

        //  AsyncTask
        new AsyncTask<Void, Void, Void>() {
            List<File> readFiles = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressBar();
            }

            @Override
            protected Void doInBackground(Void... params) {
                readFiles = browseFolder(startingFile, Integer.MAX_VALUE);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                hideProgressBar();
                for (File file : readFiles) {
                    rAdapter.add(rAdapter.getItemCount(), file.getAbsolutePath());
                }
            }
        }.execute();
    }

    /**
     * Recusively browse trough all files in given directory, try to decode bounds of file to check
     * if is supported type of image, if width or height will be greater than 0, it means that it
     * can be converted to bitmap.
     *
     * @param startingDirectory File representing starting directory
     * @param maxItems          number of items
     * @return List of images in given directory and it's subdirectories
     */
    private List<File> browseFolder(File startingDirectory, int maxItems) {
        List<File> listOfFiles = new ArrayList<>();
        File[] files = startingDirectory.listFiles();

        /*
            * BitmapFactory inJustDecodeBounds outWidth and outHeight options returns width and
            * height, or -1 if some error occurred when decoding. So if we get -1 then this file
            * cannot be decoded to bitmap, so it's not supported file type.
            *
            * I did it this way to automatically recognize supported image file types, and avoid
            * messing with mimetypes
         */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        for (File file : files) {
            // If checked file is directory, go inside.
            if (file.isDirectory()) {
                for (File f : browseFolder(file, maxItems)) {
                    if (listOfFiles.size() < maxItems) {
                        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                        if (options.outWidth != -1 && options.outHeight != -1) {
                            listOfFiles.add(f);
                        }
                    } else return listOfFiles;
                }
            } else {
                if (listOfFiles.size() < maxItems) {
                    BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                    if (options.outWidth != -1 && options.outHeight != -1) {
                        listOfFiles.add(file);
                    }
                } else {
                    return listOfFiles;
                }
            }
        }
        return listOfFiles;
    }

    public interface OnPhotoLoaded {
        void imageLoadSuccess(String path);
    }

    /**
     * Recycler View on item click listener.
     * Called when item is clicked.
     *
     * @param item     Touched itemHolder
     * @param position index of item
     */
    @Override
    public void onItemClick(GalleryRecyclerAdapter.ItemHolder item, int position) {
        if (actionMode != null) {
            toggleSelection(position);
            if (rAdapter.getItemCount() < 2) {
                actionMode.invalidate();
            }
        } else {
            showProgressBar();
            rAdapter.notifyItemChanged(position);
            if (!(openEditingMode(new File(item.getItemUri()).getAbsolutePath()))) {
                Toast.makeText(getActivity().getApplicationContext(), "Cannot load the photo"
                        + " " + item.getItemUri(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (actionMode == null) {
            actionMode = getActivity().startActionMode(actionModeCallback);
        }
        toggleSelection(position);
        return true;
    }

    private boolean openEditingMode(String path) {
        mCallback.imageLoadSuccess(path);
        return true;
    }

    /**
     * Deletes file from file paths list
     *
     * @return number of files were not deleted, if returned number is 0 then all files were deleted,
     * returns -1 if list of file paths is empty, there are no files to delete.
     */
    public int delete(List<String> paths) {
        int notDeletedFiles = paths.size();
        if (!paths.isEmpty()) {
            for (String path : paths) {
                File file = new File(path);
                if(file.delete()) notDeletedFiles--;
            }
        } else notDeletedFiles = -1;
        notifyMediaScanner(paths);
        return notDeletedFiles;
    }

    /**
     * Notifies the system that media files were removed and to scan remaining files again
     */
    public void notifyMediaScanner (List<String> paths) {
        MediaScannerConnection.scanFile(getActivity(), paths.toArray(new String[paths.size()]), null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

    public void showProgressBar() {
        progressBarView.setVisibility(View.VISIBLE);

        /*
            Set new OnItemClickListener with empty methods to prevent form loading photos from adapter
            when progress bar is spinning
         */
        rAdapter.setOnItemClickListener(new GalleryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GalleryRecyclerAdapter.ItemHolder item, int position) {
            }

            @Override
            public boolean onItemLongClicked(int position) {
                return true;
            }
        });
    }

    /**
     * Hide progress bar from recycler view, and restore onclick listeners
     */
    public void hideProgressBar() {
        progressBarView.setVisibility(View.GONE);
        /*
            Set onItemClickListener to make recycler view clickable again
         */
        rAdapter.setOnItemClickListener(this);
    }

    /**
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        rAdapter.toggleSelection(position);
        int count = rAdapter.getSelectedItemCount();
        int total = rAdapter.getItemCount();

        if (count == 0) {
            actionMode.finish();
            actionMode = null;
        } else {
            String s;
            if (count == 1) s = String.valueOf(count)+" selected item of total: "+total;
            else s = String.valueOf(count)+" selected items of total: "+total;
            actionMode.setTitle(s);
            actionMode.invalidate();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        private List<String> pathsToDelete;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.menu_select_delete, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (rAdapter.getSelectedItems().size() > 1) {
                menu.findItem(R.id.pick).setVisible(false);
                return true;
            } else {
                menu.findItem(R.id.pick).setVisible(true);
                return true;
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    /*
                    Get list of file to delete from adapter
                    */
                    pathsToDelete = new ArrayList<>();
                    for (Integer pos : rAdapter.getSelectedItems()) {
                        pathsToDelete.add(rAdapter.getItemPath(pos));
                    }

                    /*
                    Get amount of files to delete
                    */
                    int initialFilesCount = pathsToDelete.size();

                    /*
                    Delete fies, and return how many file were not deleted
                    */
                    int notDeletedFilesCount = delete(pathsToDelete);

                    pathsToDelete.clear();
                    /*
                    Remove deleted files from adapter
                    */
                    rAdapter.removeItemsFromAdapter(rAdapter.getSelectedItems());

                    mode.finish();

                    if (notDeletedFilesCount == -1)
                        Toast.makeText(getActivity().getApplicationContext(),
                                "no files selected",
                                Toast.LENGTH_LONG).show();

                    else Toast.makeText(getActivity().getApplicationContext(),
                            initialFilesCount + " files deleted",
                            Toast.LENGTH_LONG).show();
                    return true;
                case R.id.pick:
                    openEditingMode(rAdapter.getItemPath(rAdapter.getSelectedItems().get(0)));
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            rAdapter.clearSelection();
            actionMode = null;
        }
    }

    /*
     * Hide progress bar when parent activity is stopped
     */
    @Override
    public void onStop() {
        super.onStop();
        /*
            Hide progress bar when fragment becomes invisible
         */
        hideProgressBar();
    }



    /*
         * For API 23 and higher, not called properly in lower APIs
         */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnPhotoLoaded) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnPhotoLoaded");
        }
    }

    /*
     * Deprecated method called in API up to 22
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnPhotoLoaded) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnPhotoLoaded");
        }
    }
}
