package com.async;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;

import com.db.CardDatabase;
import com.db.DBDeck;
import com.hearthstonedecktracker.DeckListActivity;
import com.util.DeckRowAdapter;

/**
 * Loads all decks from the database, inserts them into the given adapter, 
 * and returns a list of all the decks
 * Need to call init() before executing
 */
public class DeckAsyncLoader extends AsyncTask<Void, Map<String, Object>, List<DBDeck>>{

	private Context context;
	private boolean isStopped;
	private DeckRowAdapter adapter;
	private DeckListActivity createDeckActivity;
	
	/**
	 * @param context - The current context
	 * @param adapter - The adapter in which to insert the decks
	 * @param createDeckActivity - The activity to call when the decks are loaded
	 */
	public void init(Context context, DeckRowAdapter adapter, DeckListActivity createDeckActivity) {
		this.context = context;
		this.adapter = adapter;
		this.createDeckActivity = createDeckActivity;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<DBDeck> doInBackground(Void... unusued) {
		isStopped = false;
		CardDatabase db = new CardDatabase(context);
		List<DBDeck> allDecks = db.makeDecks();
		db.close();
		for (DBDeck d : allDecks) {
			Map<String, Object> deckRow = adapter.createCardRow(d);
			publishProgress(deckRow);
		}
		return allDecks;
	}
	
	/**
	 * Inserts the row into the adapter and updates it
	 */
	@Override
	protected void onProgressUpdate(Map<String, Object>...deckRow) {
		if (!isStopped) {
			adapter.add(deckRow[0]);
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Updates the adapter one more time,
	 * calls the executeNext method of 
	 */
	@Override
	protected void onPostExecute(List<DBDeck> allDecks) {
		adapter.notifyDataSetChanged();
		createDeckActivity.setAllDecks(allDecks);
	}
	
}
