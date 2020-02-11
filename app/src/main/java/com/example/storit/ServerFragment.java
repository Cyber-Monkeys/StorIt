package com.example.storit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;

import io.feeeei.circleseekbar.CircleSeekBar;

public class ServerFragment extends ListFragment {

    //Variables
    private static final String TAG = "-----------------------";
    ListView listView;
    ServerListAdapter serverListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_server, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = getView().findViewById(android.R.id.list);
        serverListAdapter = new ServerListAdapter(getActivity());
        setListAdapter(serverListAdapter);

        //add example
        serverListAdapter.add(new Server("Server1", 1000));
//        serverListAdapter.add(new Server("Server2", 1000));
        serverListAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getActivity(), ServerInformation.class));
            }
        });



    }
}
