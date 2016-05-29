package com.kazimierak.kacper.fancygallerylikephotopicker.lists;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.kazimierak.kacper.fancygallerylikephotopicker.R;
import com.kazimierak.kacper.fancygallerylikephotopicker.Utils.BitmapLoaderTask;
import com.kazimierak.kacper.fancygallerylikephotopicker.views.SquareCardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * Custom RecyclerAdapter, that converts list of URI to images, to RecyclerView
 * ItemHolders representing each photo.
 * Supports onClick and onLongTouch.
 * Supports choice of multiple items
 *
 */

public class GalleryRecyclerAdapter extends RecyclerView.Adapter<GalleryRecyclerAdapter.ItemHolder>{

    /*
     * List of image paths
     */
    private List<String> filePaths;

    /*
     * LayoutInflater from RecyclerView
     */
    private LayoutInflater layoutInflater;

    private OnItemClickListener onItemClickListener;

    /*
     * BooleanArray indicating which elements were chosen in multiple choice mode
     */
    private SparseBooleanArray selectedItems;
    private Context context;

    public GalleryRecyclerAdapter(Context context){
        layoutInflater = LayoutInflater.from(context);
        filePaths = new ArrayList<>();
        selectedItems = new SparseBooleanArray();
        this.context = context;
    }

    /*
     * Called when ViewHolder (in this case ItemHolder) is created
     */
    @Override
    public GalleryRecyclerAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SquareCardView itemCardView =
                (SquareCardView)layoutInflater.inflate(R.layout.cardview, parent, false);
        return new ItemHolder(itemCardView, this);
    }

    /*
     * Called before ItemHolder is attached to adapter (after creating, or recycling it when
     * became invisible.
     */
    @Override
    public void onBindViewHolder(GalleryRecyclerAdapter.ItemHolder holder, int position) {
        // Get file path of attached photo from list and set it to itemHolder
        String targetPath = filePaths.get(position);
        holder.setItemPath(targetPath);
        // set setLongClickable to true to enable multiple choice mode
        holder.itemView.setLongClickable(true);

        // load bitmaps asynchronously
        BitmapLoaderTask loaderTask = new BitmapLoaderTask(holder.imageView, 120);
        loaderTask.loadBitmap(context, targetPath, holder.imageView,
                BitmapFactory.decodeResource(context.getResources(), R.drawable.loadplaceholder));
        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onViewRecycled(GalleryRecyclerAdapter.ItemHolder holder) {
        super.onViewRecycled(holder);
        holder.imageView.setImageDrawable(null);
    }

    /**
     * Remove item at given position from adapter.
     * @param position position of item to be removed
     */
    public void removeItemFromAdapter (int position) {
        filePaths.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Remove multiple items from the adapter,
     * @param positions list of item position to be removed
     */
    public void removeItemsFromAdapter ( List<Integer> positions ) {
        // reverse sort the list
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        /*
         * Split the list into ranges, not all the items will be indicated as removed, therefore
         * only items after ones removed will be recreated. This improves performance.
         */
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                removeItemFromAdapter(positions.get(0));
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)){
                    ++count;
                }

                if (count == 1) {
                    removeItemFromAdapter(positions.get(0));
                } else {
                    removeRange(positions.get(count - 1), count);
                }

                for (int i=0; i<count; i++){
                    positions.remove(0);
                }
            }
        }
    }

    /*
     * Removes items from given range from the adapter
     */
    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            filePaths.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    /**
     * Indicates if the item at position position is selected
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    /**
     * Indicates the list of selected items
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    /**
     * Count the selected items
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        List<Integer> selection = getSelectedItems();
        selectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    public String getItemPath(int position) {
        return filePaths.get(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() { return filePaths.size(); }

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener(){ return onItemClickListener; }

    /**
     * Interface of callbacks when item is selected, or multiple choice mode is triggered
     */
    public interface OnItemClickListener{
        void onItemClick(ItemHolder item, int position);
        boolean onItemLongClicked(int position);
    }

    /**
     * Adds new image path at specified position
     * @param location position of item to be added
     * @param path path to the image
     */
    public void add(int location, String path){
        filePaths.add(location, path);
        notifyItemInserted(location);
    }

    /*
     * Inner class representing each children in recycler view
     */
    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener{

        private GalleryRecyclerAdapter parent;
        private ImageView imageView;
        private String itemPath;
        private View selectedOverlay;

        public ItemHolder(SquareCardView cardView, GalleryRecyclerAdapter parent) {
            super(cardView);

            this.parent = parent;
            imageView = (ImageView) cardView.findViewById(R.id.item_image);
            selectedOverlay = cardView.findViewById(R.id.selectedOverlay);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemPath(String itemUri){ this.itemPath = itemUri; }

        public String getItemUri(){
            return this.itemPath;
        }

        @Override
        public void onClick(View v) {
            final OnItemClickListener listener = parent.getOnItemClickListener();
            if ( listener != null){
                listener.onItemClick(this, getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final OnItemClickListener listener = parent.getOnItemClickListener();
            return listener != null && listener.onItemLongClicked(getAdapterPosition());

        }
    }
}
