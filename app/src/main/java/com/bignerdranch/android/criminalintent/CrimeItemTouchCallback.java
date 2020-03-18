package com.bignerdranch.android.criminalintent;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CrimeItemTouchCallback extends ItemTouchHelper.Callback {
    private final CrimeListFragment.ItemTouchStatus mItemTouchStatus;

    public CrimeItemTouchCallback(CrimeListFragment.ItemTouchStatus itemTouchStatus) {
        mItemTouchStatus = itemTouchStatus;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // 上下拖动
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        // 向左滑动
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // 交换在数据源中相应数据源的位置
        return mItemTouchStatus.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // 从数据源中移除相应的数据
        mItemTouchStatus.onItemRemove(viewHolder.getAdapterPosition());
    }
}
