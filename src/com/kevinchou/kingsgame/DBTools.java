package com.kevinchou.kingsgame;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
This class is used to create a SQL database to store/edit rules
 */

public class DBTools extends SQLiteOpenHelper {

    // Get context to get to Strings resource
    private Context context;

    // Constructor
    public DBTools(Context applicationContext) {
        super(applicationContext, "rules.db", null, 1);
        this.context = applicationContext;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Create database when DBTools is created
        String query = "CREATE TABLE rules ( rank TEXT, ruleTitle TEXT, ruleDescription)";
        database.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS rules";
        database.execSQL(query);
        onCreate(database);
    }

    // Update a rule. Input (rank, ruleTitle, ruleDescription).
    public int updateRule(HashMap<String, String> queryValues) {

        // Get current database
        SQLiteDatabase database = this.getWritableDatabase();

        // put input into 'values' variable
        ContentValues values = new ContentValues();
        values.put("rank", queryValues.get("rank"));
        values.put("ruleTitle", queryValues.get("ruleTitle"));
        values.put("ruleDescription", queryValues.get("ruleDescription"));

        return database.update("rules", values, "rank" + " = ?", new String[]{queryValues.get("rank")});
    }

    // Input a rank and returns rules for that rank
    public HashMap<String, String> getRuleInfo(String rank) {

        // Sets correct rank for '10'
        if (rank.equals("T"))
            rank = "10";

        // Get database
        SQLiteDatabase database = this.getReadableDatabase();

        // Query to get row from database corresponding to input rank
        String selectQuery = "SELECT * FROM rules WHERE rank='" + rank + "'";

        Cursor cursor = database.rawQuery(selectQuery, null);

        HashMap<String, String> ruleMap = new HashMap<String, String>();

        if (cursor.moveToFirst()) {
            do {
                ruleMap.put("rank", cursor.getString(0));
                ruleMap.put("ruleTitle", cursor.getString(1));
                ruleMap.put("ruleDescription", cursor.getString(2));
            } while (cursor.moveToNext());
        }

        return ruleMap;
    }


    public ArrayList<HashMap<String, String>> getAllRules() {

        ArrayList<HashMap<String, String>> ruleArrayList = new ArrayList<HashMap<String, String>>();

        String selectQuery = "SELECT * FROM rules";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                HashMap<String, String> ruleMap = new HashMap<String, String>();

                ruleMap.put("rank", cursor.getString(0));
                ruleMap.put("ruleTitle", cursor.getString(1));
                ruleMap.put("ruleDescription", cursor.getString(2));


                ruleArrayList.add(ruleMap);

            } while (cursor.moveToNext());

        }
        return ruleArrayList;
    }

    public void setDefaultRules() {
        String[] rankArray = context.getResources().getStringArray(R.array.card_rank_array);
        String[] ruleTitleArray = context.getResources().getStringArray(R.array.rules_title_array);
        String[] ruleDescriptionArray = context.getResources().getStringArray(R.array.rules_description_array);

        String insertQuery = "INSERT INTO rules ('rank', 'ruleTitle', 'ruleDescription') VALUES ";
        String valuesQuery = "";
        for (int i = 0; i < rankArray.length; i++) {
            valuesQuery = valuesQuery + "('" + rankArray[i] + "', '" + ruleTitleArray[i] + "', '" + ruleDescriptionArray[i] + "')";
            if (i < rankArray.length - 1) {
                valuesQuery += ", ";
            }
        }

        SQLiteDatabase database = this.getWritableDatabase();
        database.delete("rules", null, null);
        database.execSQL(insertQuery + valuesQuery);
        database.close();
    }

}


