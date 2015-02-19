package com.cwlarson.deviceid.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.util.MyAdapter;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private Context mContext;

    public DividerItemDecoration(Context context) {

        mDivider = context.getResources().getDrawable(R.drawable.line_divider);
        mContext=context;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        int left = parent.getPaddingLeft()+(int) mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
        int right = parent.getWidth() - parent.getPaddingRight()-(int) mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            View childNext=child;
            if (i+1<childCount) childNext = parent.getChildAt(i+1);

            if (!child.getTag().equals(Integer.toString(MyAdapter.VIEW_TYPE_HEADER))&&!childNext.getTag().equals(Integer.toString(MyAdapter.VIEW_TYPE_HEADER))) {

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}