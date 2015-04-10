package com.oufyp.bestpricehk;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.HashMap;

public class AdvSearchFragment extends Fragment {
    public static final String TAG = AdvSearchFragment.class.getSimpleName();
    private EditText keywordET;
    private Spinner typeSpinner;
    private Spinner storeSpinner;
    private EditText startPriceET;
    private EditText endPriceET;
    private Button searchBtn;

    public AdvSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adv_search, container, false);
         String[] types = {getString(R.string.all), getString(R.string.baby_care), getString(R.string.beer), getString(R.string.beverages),
                getString(R.string.biscuits), getString(R.string.bread), getString(R.string.cakes),
                getString(R.string.dairy), getString(R.string.household), getString(R.string.milk_powder),
                getString(R.string.noddles), getString(R.string.oil), getString(R.string.rice),
                getString(R.string.snacks), getString(R.string.wine)
        };
         String[] stores = {getString(R.string.all), getString(R.string.parknshop), getString(R.string.wellcome),
                getString(R.string.jusco), getString(R.string.market_place)
        };
        keywordET = (EditText) view.findViewById(R.id.keyword);
        typeSpinner = (Spinner) view.findViewById(R.id.type);
        storeSpinner = (Spinner) view.findViewById(R.id.store);
        startPriceET = (EditText) view.findViewById(R.id.start_price);
        endPriceET = (EditText) view.findViewById(R.id.end_price);
        ArrayAdapter<String> typeDataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, types);
        typeDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeDataAdapter);
        ArrayAdapter<String> storeDataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stores);
        storeDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storeSpinner.setAdapter(storeDataAdapter);
        searchBtn = (Button) view.findViewById(R.id.action_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                advSearch();
            }
        });
        return view;
    }

    public void advSearch() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra("TAG", TAG);
        HashMap<String, String> params = new HashMap<>();
        params.put("tag", "login");
        //URL space to %20 (GET method)
        params.put("keyword", keywordET.getText().toString().equals("") ? "Any" : keywordET.getText().toString().replace(" ", "%20"));
        params.put("type", typeSpinner.getSelectedItem().toString());
        params.put("store", storeSpinner.getSelectedItem().toString());
        params.put("startPrice", startPriceET.getText().toString().equals("") ? "Any" : startPriceET.getText().toString());
        params.put("endPrice", endPriceET.getText().toString().equals("") ? "Any" : endPriceET.getText().toString());
        intent.putExtra("params", params);
        startActivity(intent);
    }
}
