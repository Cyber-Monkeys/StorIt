package com.example.storit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ClientFragment extends Fragment {

    //variables
    GridView gridView;
    TextView sortName;

    private ArrayList<String> fileName = new ArrayList<String>();
    static int fileImage = R.drawable.background_2;
    private ArrayList<Integer> image = new ArrayList<Integer>();
    private ClientAdapter clientAdapter;

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

        clientAdapter = new ClientAdapter(getView().getContext(),fileName.toArray(new String[fileName.size()]), image.toArray(new Integer[image.size()]));
        gridView.setAdapter(clientAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Log.d("Webrtcclient", "download request started");
                ((Menu) getActivity()).downloadData(fileName.get(arg2));
            }

        });

        sortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SortBottomSheetDialog bottomSheetDialog = new SortBottomSheetDialog();
                bottomSheetDialog.show(getFragmentManager(), "SortBottomSheet");
            }
        });

    }
    public void addFile(String name) {
        fileName.add(name);
        image.add(fileImage);
    }
    public void refreshAdapter() {
        clientAdapter = new ClientAdapter(getView().getContext(), fileName.toArray(new String[fileName.size()]),image.toArray(new Integer[image.size()]));
        gridView.setAdapter(clientAdapter);

    }

}
