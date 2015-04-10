package com.oufyp.bestpricehk;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;

import com.oufyp.bestpricehk.adapter.TypeGridAdapter;


public class BrowseType extends Activity {
    public final static String PRODUCT_TYPE = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_type);
        final String[] types = {getString(R.string.baby_care), getString(R.string.beer), getString(R.string.beverages),
                getString(R.string.biscuits), getString(R.string.bread), getString(R.string.cakes),
                getString(R.string.dairy), getString(R.string.household), getString(R.string.milk_powder),
                getString(R.string.noddles), getString(R.string.oil), getString(R.string.rice),
                getString(R.string.snacks), getString(R.string.wine)
        };
         int[] icons = {R.drawable.ic_baby, R.drawable.ic_beer, R.drawable.ic_beverage, R.drawable.ic_biscuit,
                R.drawable.ic_bread, R.drawable.ic_cakes, R.drawable.ic_dairy, R.drawable.ic_household,
                R.drawable.ic_powder, R.drawable.ic_noddles, R.drawable.ic_oil, R.drawable.ic_rice,
                R.drawable.ic_snack, R.drawable.ic_wine,
        };
        TypeGridAdapter adapter = new TypeGridAdapter(this, types, icons);
        GridView grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String type = types[position];
                Intent intent = new Intent(view.getContext(), DisplayProducts.class);
                intent.putExtra(PRODUCT_TYPE, type);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activities, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
}
