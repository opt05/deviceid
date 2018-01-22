package com.cwlarson.deviceid.dialog;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.util.ItemClickHandler;
import com.cwlarson.deviceid.database.BottomSheetViewModel;
import com.cwlarson.deviceid.databinding.BottomSheetBinding;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.databinding.ItemType;
import com.cwlarson.deviceid.util.SystemUtils;

/**
 * A BottomSheetFragment with fixes for tablets
 * Includes: has a max screen width it will occupy & start fully expanded upward
 */
public class ItemClickDialog extends BottomSheetDialogFragment {
    private BottomSheetBinding binding;

    public static ItemClickDialog newInstance(String itemTitle, ItemType itemType) {
        Bundle bundle = new Bundle();
        bundle.putString("title",itemTitle);
        bundle.putString("type",itemType.name());
        ItemClickDialog dialog = new ItemClickDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.bottom_sheet, null, false);
        BottomSheetViewModel mModel = ViewModelProviders.of(this).get(BottomSheetViewModel.class);
        String title = (getArguments()!=null)?getArguments().getString("title",""):"";
        ItemType type = (getArguments()!=null)?
            ItemType.valueOf(getArguments().getString("type", ItemType.NONE.name())):ItemType.NONE;
        mModel.getItem(title,type).observe(this, new Observer<Item>() {
            @Override
            public void onChanged(@Nullable Item item) {
                if(item==null || getActivity()==null) return;
                binding.setItem(item);
                binding.setHandler(new ItemClickHandler(getActivity(), item));
            }
        });
        dialog.setContentView(binding.getRoot());
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View)binding.getRoot().getParent()).getLayoutParams();
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
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if(getDialog().getWindow()!=null)
                    getDialog().getWindow().setLayout(
                        SystemUtils.calculateBottomSheetMaxWidth(inflater.getContext()),
                        ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
