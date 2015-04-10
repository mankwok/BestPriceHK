package com.oufyp.bestpricehk;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oufyp.bestpricehk.app.UserFunctions;
import com.oufyp.bestpricehk.database.DatabaseHandler;

import java.util.HashMap;


public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        UserFunctions uf = new UserFunctions();
        DatabaseHandler db = new DatabaseHandler(getActivity());
        TextView tv = (TextView) view.findViewById(R.id.welcome_message);
        if(uf.isUserLoggedIn(getActivity())) {

            HashMap<String,String> user = db.getUserDetails();
            tv.setText(getString(R.string.message_welcome,user.get("name")));
        }
        else{
            tv.setText(getString(R.string.message_welcome,"guest"));
        }
        // Inflate the layout for this fragment
        return view;
    }


}
