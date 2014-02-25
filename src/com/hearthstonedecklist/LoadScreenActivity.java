package com.hearthstonedecklist;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.async.CardDatabaseAsyncLoader;
import com.db.DBCard;

public class LoadScreenActivity extends Activity {
	
	private ProgressDialog progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		progress = new ProgressDialog(this);
		progress.show();
		progress.setMessage("Loading cards...");
		
		CardDatabaseAsyncLoader cardDBLoader = new CardDatabaseAsyncLoader();
		cardDBLoader.init(this);
		cardDBLoader.execute(this);
	}
	
	public void finishLoadScreen(List<DBCard> allCards) {
		((MyApp) this.getApplication()).setAllCards(allCards);
		if (progress.isShowing()) {
			progress.dismiss();
		}
		Intent myIntent = new Intent(this, CardListActivity.class);
        startActivity(myIntent);
        finish();
	}
}
