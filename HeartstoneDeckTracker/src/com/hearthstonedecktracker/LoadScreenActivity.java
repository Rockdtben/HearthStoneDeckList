package com.hearthstonedecktracker;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.async.CardDatabaseAsyncLoader;
import com.db.DBCard;

public class LoadScreenActivity extends Activity {
	
	private ProgressDialog progressDialog;
	private String[] phases = {"Loading cards", "Loading card attributes"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(phases[0]);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.show();
		
		CardDatabaseAsyncLoader cardDBLoader = new CardDatabaseAsyncLoader();
		cardDBLoader.init(this);
		cardDBLoader.execute(this);
	}
	
	public void finishLoadScreen(List<DBCard> allCards) {
		((HearthstoneDeckTracker) this.getApplication()).setAllCards(allCards);
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		Intent myIntent = new Intent(this, DeckListActivity.class);
        startActivity(myIntent);
        finish();
	}
	
	public void setProgress(int progress, int total, int phase) {
		if (progressDialog.isShowing()) {
			progressDialog.setTitle(phases[phase]);
			progressDialog.setProgress(progress);
			progressDialog.setMax(total);
		}
	}
}
