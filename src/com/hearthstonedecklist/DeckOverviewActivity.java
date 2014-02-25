package com.hearthstonedecklist;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.async.CardRowAsyncLoader;
import com.db.DBCard;
import com.db.DBDeck;
import com.util.CardRowAdapter;

public class DeckOverviewActivity extends Activity{

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

	public void setOnItemClickListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
				if (isGameMode) {
					TextView amountTextView = (TextView) view.findViewById(R.id.card_list_row_amount);
					int amount = Integer.parseInt(((String) amountTextView.getText()));
					if (amount <= 1) {
						amountTextView.setText("0");
						view.setBackgroundResource(R.color.card_list_disabled_color);
					} else {
						amountTextView.setText(Integer.toString(amount - 1));
					}
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
