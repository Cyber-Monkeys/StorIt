package com.example.storit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ClientFragment extends Fragment {

    //variables
    GridView gridView;
    TextView sortName;

    String[] fileName = {"Hello","Hello","Hello","Hello","Hello","Hello","Hello","Hello","Hello","Hello","Hello",};

    int[] image = {R.drawable.background_2,R.drawable.background_2,R.drawable.background_2,R.drawable.background_2,R.drawable.background_2,
            R.drawable.background_2,R.drawable.background_2,R.drawable.background_2,R.drawable.background_2,R.drawable.background_2,
            R.drawable.background_2};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sortName = getView().findViewById(R.id.sortName);
        gridView = getView().findViewById(R.id.gridView);

        ClientAdapter clientAdapter = new ClientAdapter(getView().getContext(),fileName,image);
        gridView.setAdapter(clientAdapter);

        sortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SortBottomSheetDialog bottomSheetDialog = new SortBottomSheetDialog();
                bottomSheetDialog.show(getFragmentManager(), "SortBottomSheet");
            }
        });

    }

}
