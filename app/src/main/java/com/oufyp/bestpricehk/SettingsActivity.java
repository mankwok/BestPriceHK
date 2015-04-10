package com.oufyp.bestpricehk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.SearchRecentSuggestions;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference myPref = findPreference("clear_history");
        final Context mContext = this;
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.clear_history).setMessage(R.string.message_clear_history)
                        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(mContext,
                                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                                suggestions.clearHistory();
                            }
                        })
                        .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and return it
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });
    }

}
