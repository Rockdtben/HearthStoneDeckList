package com.async;

import java.util.List;
import java.util.Map;

import android.os.AsyncTask;

import com.db.DBCard;
import com.db.DBDeck;
import com.util.CardRowAdapter;

/**
 * Class to load card rows asynchronously.
 * Needs to be initialized by calling init()
 */
public class CardRowAsyncLoader extends AsyncTask<List<DBCard>, Map<String, Object>, Void>{

	private CardRowAdapter adapter;
	private boolean isStopped;
	private boolean deckOverviewMode;
	private DBDeck deck;


	/**
	 * @param adapter - The adapter to which the card rows are added
	 */
	public void init(CardRowAdapter adapter) {
		this.adapter = adapter;
		isStopped = false;
		deckOverviewMode = false;
	}
	
	/**
	 * If a DBDeck is given as a parameter, 
	 * it also loads the amounts of the cards into the rows
	 * @param adapter - The adapter to which the card rows are added
	 * @param deck - The deck which contains the amounts of the cards to be added
	 */
	public void init(CardRowAdapter adapter, DBDeck deck) {
		this.adapter = adapter;
		isStopped = false;
		deckOverviewMode = true;
		this.deck = deck;
	}
	
	/**
	 * Stops the CardRowAsyncLoader from loading any more cards into the list
	 */
	public void stop() {
		isStopped = true;
	}

	/**
	 * Clears the adapter, as we don't want to add more rows
	 */
	@Override
	protected void onPreExecute() {
		adapter.clear();
	}

	/**
	 * Creates a card row for each DBCard given
	 * @param cardLists - Only one parameter is needed, a List of DBCards that are to be inserted as rows
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Void doInBackground(List<DBCard>... cardLists) {
		//More efficient to check outside the loop than inside
		if (deckOverviewMode) {
			Map<Integer, Integer> deckCards = deck.getDeck();
			for (DBCard c : cardLists[0]) {
				Map<String, Object> cardRow = adapter.createCardRow(c, deckCards.get(c.getId()));
				publishProgress(cardRow);
			}
		} else {
			for (DBCard c : cardLists[0]) {
				Map<String, Object> cardRow = adapter.createCardRow(c);
				publishProgress(cardRow);
			}
		}
		return null;
	}

	/**
	 * Inserts the row into the adapter and updates it
	 */
	@Override
	protected void onProgressUpdate(Map<String, Object>...cardRow) {
		if (!isStopped) {
			adapter.add(cardRow[0]);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Updates the adapter one more time
	 */
	@Override
	protected void onPostExecute(Void unused) {
		adapter.notifyDataSetChanged();
	}

}