package com.cwlarson.deviceid.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cwlarson.deviceid.BR;
import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;

public class ItemClickDialog extends BottomSheetDialogFragment {
    public ItemClickDialog() {
        //Empty
    }

    public static ItemClickDialog newInstance(Item item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("item",item);
        ItemClickDialog dialog = new ItemClickDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        ViewDataBinding viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.bottom_sheet, null, false);
        viewDataBinding.setVariable(BR.item,getArguments().getParcelable("item"));
        viewDataBinding.setVariable(BR.act,getActivity());
        dialog.setContentView(viewDataBinding.getRoot());
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View)viewDataBinding.getRoot().getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if(behavior!=null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED);
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {

                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                int width = getContext().getResources().getDimensionPixelSize(R.dimen.bottom_sheet_width);
                if(getDialog().getWindow()!=null) getDialog().getWindow().setLayout(width > 0 ? width : ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
