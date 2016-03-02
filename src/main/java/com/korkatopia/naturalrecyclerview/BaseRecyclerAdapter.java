package com.korkatopia.naturalrecyclerview;


import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

/**
 * Created by Iliya Gogolev on 10/6/15.
 */
public abstract class BaseRecyclerAdapter<T extends BaseObservable> extends RecyclerView.Adapter<BaseRecyclerAdapter.ViewBindingHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * Contains the list of items that represent the data of this RecyclerView.Adapter.
     * The this list is referred to as "the array" in the documentation.
     */
    protected final ObservableArrayList<T> mItems;

    /**
     * Lock used to modify the content of {@link #mItems}. Any write operation
     * performed on the array should be synchronized on this lock.
     */
    private final Object mLock = new Object();

    protected OnItemClickListener mItemClickListener;

    public BaseRecyclerAdapter() {
        mItems = new ObservableArrayList<>();
    }

    public BaseRecyclerAdapter(@NonNull ObservableArrayList<T> items) {
        mItems = items;
    }

    protected abstract int getViewType(BaseObservable item);

    protected abstract int getLayoutIdByViewType(int viewType);

    protected abstract void onBindViewHolderByViewType(int viewType, ViewBindingHolder holder, BaseObservable item);

    /**
     * Returns the data used by this adapter.
     * If registering a listener on changes to the array is required
     * it can be done here.
     *
     * @return The list of items in the array.
     */
    public final ObservableArrayList<T> getItems() {
        return mItems;
    }

    /**
     * Adds the specified item at the end of the array.
     *
     * @param item The item to add at the end of the array.
     */
    public void add(T item) {
        synchronized (mLock) {
            mItems.add(item);
            notifyItemChanged(mItems.size() - 1);
        }
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param items The Collection to add at the end of the array.
     */
    public void addAll(Collection<T> items) {
        synchronized (mLock) {
            int position = mItems.size() - 1;
            mItems.addAll(items);
            notifyItemRangeChanged(position, items.size());
        }
    }

    /**
     * Removes the specified item from the array.
     *
     * @param item The item to remove.
     */
    public void remove(T item) {
        synchronized (mLock) {
            final int position = getPosition(item);
            mItems.remove(item);
            notifyItemChanged(position);
        }
    }

    /**
     * Removes all items in the array.
     */
    public void clear() {
        synchronized (mLock) {
            mItems.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * Returns the item at the specified position in the array.
     */
    public T getItem(int position) {
        return mItems.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        return mItems.indexOf(item);
    }

    /**
     * Registers a click listener notifying when an item in the {@link RecyclerView} has
     * been clicked.
     *
     * @param listener The listener to call when an item is clicked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public void onBindViewHolder(ViewBindingHolder holder, int position) {
        final BaseObservable item = mItems.get(position);
        onBindViewHolderByViewType(getViewType(item), holder, item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        final BaseObservable observable = mItems.get(position);
        return getViewType(observable);
    }

    @Override
    public ViewBindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflateView(parent, getLayoutIdByViewType(viewType));
        return new ViewBindingHolder(view, mItemClickListener);
    }

    /**
     * Convenience method for inflating a layout into a ViewGroup.
     *
     * @param parent   The target ViewGroup
     * @param resource The layout resource ID to inflate.
     * @return The inflated view.
     */
    private View inflateView(ViewGroup parent, int resource) {
        return LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
    }


    /**
     * Uses {@link ViewDataBinding} to create a view-holder
     */
    public static class ViewBindingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ViewDataBinding binding;
        private final OnItemClickListener mOnItemClickListener;

        public ViewBindingHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            itemView.setOnClickListener(this);
            binding = DataBindingUtil.bind(itemView);

            mOnItemClickListener = listener;
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, getLayoutPosition());
            }
        }
    }
}
