package com.kevinchou.kingsgame;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.widget.Toast;

public class RulesActivity extends Activity {

	
    TextView rank;
    ListView listView;

    DBTools dbTools = new DBTools(this);


    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rules);
		// Show the Up button in the action bar.
		setupActionBar();
		
        listView = (ListView) findViewById(R.id.list);

        ArrayList<HashMap<String, String>> rulesList = dbTools.getAllRules();

        SimpleAdapter adapter = new SimpleAdapter(RulesActivity.this, rulesList, R.layout.rule_element,
                new String[]{"rank", "ruleTitle", "ruleDescription"},
                new int[]{R.id.tvRuleRank, R.id.tvRuleTitle, R.id.tvRuleDescription});


        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {

                rank = (TextView) view.findViewById(R.id.tvRuleRank);

                String rankValue = rank.getText().toString();

                popUp(rankValue);
            }
        });


        // Show message telling user to tap rule to edit
        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.change_rule), Toast.LENGTH_SHORT);
        toast.show();

	}

	 	TextView invisibleRank;
	    EditText etEditRuleTitle;
	    EditText etEditRuleDescription;
	    private void popUp(String rankIn) {

            // Adds body of popup: editable rules & description
	        LayoutInflater inflater = LayoutInflater.from(this);
	        View addView = inflater.inflate(R.layout.edit_rule, null);
	        invisibleRank = (TextView) addView.findViewById(R.id.invisibleRank);
	        etEditRuleTitle = (EditText) addView.findViewById(R.id.etEditRuleTitle);
	        etEditRuleDescription = (EditText) addView.findViewById(R.id.etEditRuleDescription);

	        // Get current rule and fill in EditTexts
	        HashMap<String, String> currentRule = dbTools.getRuleInfo(rankIn);
	        invisibleRank.setText(rankIn);
	        etEditRuleTitle.setText(currentRule.get("ruleTitle"));
	        etEditRuleDescription.setText(currentRule.get("ruleDescription"));

	        AlertDialog.Builder builder = new AlertDialog.Builder(this );
	        builder.setTitle("Editing Rules for: " + rankIn);
	        builder.setView(addView);
	        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialogInterface, int i) {
	                        // Do nothing
	                    }
	                });

	        builder.setPositiveButton("Save",
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                        HashMap<String, String> ruleMap = new HashMap<String, String>();

	                        ruleMap.put("rank", invisibleRank.getText().toString());
	                        ruleMap.put("ruleTitle", etEditRuleTitle.getText().toString());
	                        ruleMap.put("ruleDescription", etEditRuleDescription.getText().toString());

	                        dbTools.updateRule(ruleMap);

	                        onCreate(new Bundle());

	                    }
	                });

	        builder.show();
	    }


	    public void setDefaultRules() {
	        
	        String[] rankArray = getResources().getStringArray(R.array.card_rank_array);
	        String[] rulesTitleArray = getResources().getStringArray(R.array.rules_title_array);
	        String[] rulesDescriptionArray = getResources().getStringArray(R.array.rules_description_array);

	        for (int i = 0; i < rankArray.length; i++) {
	            HashMap<String, String> ruleMap = new HashMap<String, String>();

	            ruleMap.put("rank", rankArray[i]);
	            ruleMap.put("ruleTitle", rulesTitleArray[i]);
	            ruleMap.put("ruleDescription", rulesDescriptionArray[i]);

	            dbTools.updateRule(ruleMap);
	        }

	    }


	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rules, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			//NavUtils.navigateUpFromSameTask(this);
            finish();
			return true;

        case R.id.help:
            displayHelpPopUp();
            break;

        case R.id.change_to_default_rules:
            //setDefaultRules();
            dbTools.setDefaultRules();
            onCreate(new Bundle());
            break;
        default:
            break;
		}
		return super.onOptionsItemSelected(item);
	}


    public void displayHelpPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.help_title));
        builder.setMessage(getResources().getString(R.string.help_message));
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
