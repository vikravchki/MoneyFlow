package com.example.pavel.moneyflow.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

public abstract class AbstractCursorRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected boolean mDataValid;
    protected Cursor mCursor;
    protected int mRowIDColumn;

    public AbstractCursorRecyclerAdapter(Cursor c) {
        init(c);
    }

    void init(Cursor c) {
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
        setHasStableIds(true);
    }

    @Override
    public final void onBindViewHolder (VH holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        onBindViewHolder(holder, mCursor);
    }

    public abstract void onBindViewHolder(VH holder, Cursor cursor);

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount () {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId (int position) {
        if(hasStableIds() && mDataValid && mCursor != null){
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIDColumn);
            } else {
                return RecyclerView.NO_ID;
            }
        } else {
            return RecyclerView.NO_ID;
        }
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;
    }
}
