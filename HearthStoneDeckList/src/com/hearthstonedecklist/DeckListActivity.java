package com.hearthstonedecklist;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.async.DeckAsyncLoader;
import com.db.CardDatabase;
import com.db.DBDeck;
import com.util.DeckRowAdapter;

public class DeckListActivity extends Activity {
	
	private List<DBDeck> allDecks;
	private ListView listview;
	private DeckRowAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initUI();
		getAllDecks();
	}
	
	private void initUI() {
		setContentView(R.layout.deck_list_activity);
		listview = (ListView) findViewById(R.id.deck_list);
		//Clicking on a list item brings you to the overview for that deck.
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
				DBDeck deck = allDecks.get(position);
				Intent intent = new Intent(getBaseContext(), DeckOverviewActivity.class);
				intent.putExtra("Deck", deck);
				startActivity(intent);
			}
		});
		//Adding a button to create a new deck to the bottom of the list of decks
		View footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.deck_list_footer, null, false);
		listview.addFooterView(footerView);
		
		/*
		 * Params:
		 * - Context
		 * - The layout of each row
		 * - This activity, for the callback function for the delete deck button
		 */
		adapter = new DeckRowAdapter(
				this, 
				R.layout.card_list_row_layout,
				this);
		listview.setAdapter(adapter);
	}
	
	private void getAllDecks() {
		DeckAsyncLoader deckLoader = new DeckAsyncLoader();
		deckLoader.init(this, adapter, this);
		deckLoader.execute((Void) null);
	}
	
	public void setAllDecks(List<DBDeck> allDecks) {
		this.allDecks = allDecks;
		updateList();
	}
	
	/**
	 * Method called by the 'deck_list_create_deck_button'
	 * @param v - The view calling it
	 */
	public void createNewDeck(View v) {
		Intent intent = new Intent(getBaseContext(), CreateDeckHeroActivity.class);
		startActivity(intent);
	}
	
	private void updateList() {
		adapter.setDecks(allDecks);
	}
	
	/**
	 * Method called by the 'deck_list_row_delete_deck_button'
	 * @param position - The position of the row where the delete button is located
	 */
	public void deleteDeck(int position) {
		DBDeck deck = allDecks.remove(position);
		CardDatabase db = new CardDatabase(this);
		db.deleteDeck(deck);
		db.close();
		updateList();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getAllDecks();
	}
	
	/**
	 * Called by the menubar_card_list_button to go to the CardListActivity
	 * @param v - The view that calls this method
	 */
	public void goToCardList(View v) {
		Intent intent = new Intent(getBaseContext(), CardListActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Called by the menubar_deck_list_button to go to the DeckListActivity
	 * @param v - The view that calls this method
	 */
	public void goToDeckList(View v) {
		Intent intent = new Intent(getBaseContext(), DeckListActivity.class);
		startActivity(intent);
	}
}
