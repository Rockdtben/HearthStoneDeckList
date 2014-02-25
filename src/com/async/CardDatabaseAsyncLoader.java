package com.async;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.db.CardDatabase;
import com.db.DBCard;
import com.hearthstonedecklist.LoadScreenActivity;

public class CardDatabaseAsyncLoader extends AsyncTask<Context, Integer, List<DBCard>> {

	private LoadScreenActivity loadScreen;
	
	public void init(LoadScreenActivity loadScreen) {
		this.loadScreen = loadScreen;
	}
	
	@Override
	protected List<DBCard> doInBackground(Context... contexts) {
		CardDatabase db = new CardDatabase(contexts[0]);
		List<DBCard> allCards = db.makeDBCards();
		db.close();
		return allCards;
	}
	
	@Override
	public void onPostExecute(List<DBCard> allCards) {
		loadScreen.finishLoadScreen(allCards);
	}

}
