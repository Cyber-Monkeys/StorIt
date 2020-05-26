package com.example.storit;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortBottomSheetDialog extends BottomSheetDialogFragment {

    LinearLayout sortName, sortNameReverse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_sort, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sortName = getView().findViewById(R.id.sortName);
        sortNameReverse = getView().findViewById(R.id.sortNameReverse);

        sortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Menu)getActivity()).clientFragment.sortFile();
                SortBottomSheetDialog.this.dismiss(); //close dialog
            }
        });

        sortNameReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Menu)getActivity()).clientFragment.sortFileReverse();
                SortBottomSheetDialog.this.dismiss(); //close dialog
            }
        });
    }
}
