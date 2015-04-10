package com.oufyp.bestpricehk.app;


import android.content.Context;
import com.oufyp.bestpricehk.database.DatabaseHandler;
import java.util.HashMap;

public class UserFunctions {
    public boolean isUserLoggedIn(Context context){
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        int count = db.getRowCount();
        return count > 0;
    }

    /**
     * Function to logout user
     * Reset Database
     * */
    public boolean logoutUser(Context context){
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        db.resetTables();
        return true;
    }
    public String getUserID(Context context){
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        HashMap<String, String> user = db.getUserDetails();
        return user.get("uid");
    }
    public boolean isSilverMember(Context context){
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        HashMap<String, String> user = db.getUserDetails();
        return user.get("rank").equals("1");
    }
    public boolean isGoldMember(Context context){
        DatabaseHandler db = DatabaseHandler.getInstance(context);
        HashMap<String, String> user = db.getUserDetails();
        return user.get("rank").equals("2");
    }
}
