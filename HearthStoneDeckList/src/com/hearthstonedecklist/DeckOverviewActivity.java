package com.hearthstonedecklist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.async.CardRowAsyncLoader;
import com.db.DBCard;
import com.db.DBDeck;
import com.util.CardRowAdapter;

/**
 * This is the overview of the deck, which allows the user to either edit the deck,
 * or go in game mode, in which the user can keep track of which cards they have drawn
 * during a HearthStone match.
 *
 */
public class DeckOverviewActivity extends Activity {

	private DBDeck deck;
	private List<DBCard> deckCards;
	private ListView listview;
	private CardRowAdapter adapter;
	private CardRowAsyncLoader prevCardRowLoader;
	private boolean isGameMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		deck = getDeck();
		deckCards = getCards(deck);
		initUI();
		updateList();
		isGameMode = false;
		setOnItemClickListener();
	}

	private void initUI() {
		setContentView(R.layout.deck_overview_activity);
		listview = (ListView) findViewById(R.id.deck_overview_card_list);

		/*
		 * Params:
		 * - Context
		 * - The layout of each row
		 * - Whether the rows should display a button to add cards to a deck
		 */
		adapter = new CardRowAdapter(
				this, 
				R.layout.card_list_row_layout,
				false);
		listview.setAdapter(adapter);
	}

	@SuppressWarnings("unchecked")
	private void updateList() {
		if (prevCardRowLoader != null) {
			prevCardRowLoader.stop();
		}
		CardRowAsyncLoader cardRowLoader = new CardRowAsyncLoader();
		cardRowLoader.init(adapter, deck);
		cardRowLoader.execute(deckCards);
		prevCardRowLoader = cardRowLoader;
	}

	public void addCard(View v) {
		Intent intent = new Intent(getBaseContext(), CardListActivity.class);
		intent.putExtra("Deck", deck);
		startActivity(intent);
	}

	/**
	 * Switches the mode of the activity, which means that instead of getting 
	 * information when you press a listitem, the amount of that card decrements by one,
	 * and if there are 0 left, it is greyed out.
	 * @param v - The button that calls this method
	 */
	public void switchMode(View v) {
		isGameMode = !isGameMode;
		//If we're no longer in game mode, reset the list
		if (!isGameMode) {
			updateList();
		}
	}

	/**
	 * If the activity is in game mode, tapping the cards will decrease the amount by one,
	 * and if the amount is zero, set the background to grey.
	 * If the activity is in edit deck mode, tapping the cards will show information about the
	 * card in a CardInfoActivity.
	 */
	public void setOnItemClickListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
				if (isGameMode) {
					
					Map<String, Object> cardRow = adapter.getItem(position);
					adapter.remove(cardRow);
					int amount = (Integer) cardRow.get("Amount");
					if (amount <= 1) {
						cardRow.put("Amount", 0);
					} else {
						cardRow.put("Amount", amount - 1);
					}
					adapter.insert(cardRow, position);
					adapter.notifyDataSetChanged();
					
				} else {
					DBCard c = deckCards.get(position);
					Intent intent = new Intent(getBaseContext(), CardInfoActivity.class);
					intent.putExtra("Card", c);
					startActivity(intent);
				}

			}
		});

	}

	/**
	 * Get the deck for which to make the list, from the intent
	 * @return - The DBDeck in the intent
	 */
	private DBDeck getDeck() {
		return (DBDeck) getIntent().getExtras().getParcelable("Deck");
	}

	/**
	 * Gets all the cards of a given deck, which only has the card id's
	 * @param deck - The deck for which to get the cards
	 * @return - All the cards belonging to the given deck
	 */
	private List<DBCard> getCards(DBDeck deck) {
		List<DBCard> allCards = ((MyApp) getApplication()).getAllCards();
		List<DBCard> deckCards = new ArrayList<DBCard>();
		for (int cardId : deck.getDeck().keySet()) {
			for (DBCard c : allCards) {
				if (c.getId() == cardId) {
					deckCards.add(c);
				}
			}
		}
		return deckCards;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateList();
	}
}
