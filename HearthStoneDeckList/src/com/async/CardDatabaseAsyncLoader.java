package com.async;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.db.CardDatabase;
import com.db.DBCard;
import com.hearthstonedecklist.LoadScreenActivity;

/**
 * Loads all the cards from the database.
 * init() must be called before executing the task.
 * Uses the LoadScreenActivity while loading.
 */
public class CardDatabaseAsyncLoader extends AsyncTask<Context, Integer, List<DBCard>> {

	private LoadScreenActivity loadScreen;
	
	/**
	 * @param loadScreen - The load screen in which to display the loading while in progresss
	 */
	public void init(LoadScreenActivity loadScreen) {
		this.loadScreen = loadScreen;
	}
	
	/**
	 * Loads all the cards from the database
	 * @param contexts - Only one parameter is needed, the current context
	 * @return - The list of all cards
	 */
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
