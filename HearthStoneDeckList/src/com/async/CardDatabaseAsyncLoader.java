package com.async;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
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
		AssetManager assetManager = contexts[0].getAssets();
		List<DBCard> allCards = new ArrayList<DBCard>();

		Cursor cardCursor = db.allCardsCursor();
		int cardCursorSize = cardCursor.getCount();
		//Make DBCards, no card lists yet
		cardCursor.moveToFirst();
		while (!cardCursor.isAfterLast()) {
			DBCard card;
			try {
				card = DBCard.cursorToDBCard(cardCursor, assetManager);
				allCards.add(card);
				publishProgress(allCards.size(), cardCursorSize, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			cardCursor.moveToNext();
		}
		cardCursor.close();
		
		Cursor cardListCursor = db.allCardListItemsCursor();
		int cardListProgress = 0;
		int cardListCursorSize = cardListCursor.getCount();
		//Adding the card list items to the DBCards
		cardListCursor.moveToFirst();
		while (!cardListCursor.isAfterLast()) {
			for (DBCard card : allCards) {
				//If the id of a card equals the 'card_id' column of a row in the card list items
				if (card.getId() == cardListCursor.getInt(cardListCursor.getColumnIndex(DBCard.CARD_LIST_ITEM_ID_COLUMN))) {
					card.addAttr(cardListCursor);
					cardListProgress++;
					publishProgress(cardListProgress, cardListCursorSize, 1);
				}
			}
			cardListCursor.moveToNext();
		}
		cardListCursor.close();

		db.close();
		return allCards;
	}
	
	/**
	 * @param progress[0] - The progress so far
	 * @param progress[1] - The total work
	 * @param progress[2] - The phase of the work, for example 0 for the first phase, 1 for the second, etc.
	 */
	@Override
	protected void onProgressUpdate(Integer...progress) {
		loadScreen.setProgress(progress[0], progress[1], progress[2]);
	}

	@Override
	public void onPostExecute(List<DBCard> allCards) {
		loadScreen.finishLoadScreen(allCards);
	}

}
