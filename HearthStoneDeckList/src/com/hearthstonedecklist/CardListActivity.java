package com.hearthstonedecklist;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.async.CardRowAsyncLoader;
import com.db.CardDatabase;
import com.db.DBCard;
import com.db.DBCard.Hero;
import com.db.DBDeck;
import com.util.CardList;
import com.util.CardList.Sort;
import com.util.CardRowAdapter;

@SuppressWarnings("unchecked")
public class CardListActivity extends Activity {

	private CardList cardlist;
	private ListView listview;
	private CardRowAdapter adapter;
	private CardRowAsyncLoader prevCardRowLoader;
	
	private boolean addCardMode;
	private DBDeck deck;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}
	
	
	private void init() {
		cardlist = new CardList((MyApp) getApplication());
		if (getIntent().hasExtra("Deck")) {
			addCardMode = true;
			deck = (DBDeck) getIntent().getExtras().getParcelable("Deck");
			List<Hero> heroes = new ArrayList<Hero>();
			heroes.add(deck.getHero());
			heroes.add(Hero.NEUTRAL);
			cardlist.setCurrentHeroes(heroes);
		} else {
			addCardMode = false;
		}
		
		//Tapping on a card reveals information about that card in a CardInfoActivity
		setContentView(R.layout.card_list_activity);
		listview = (ListView) findViewById(R.id.card_list);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
				DBCard c = cardlist.getSelectedCards().get(position);
				Intent intent = new Intent(getBaseContext(), CardInfoActivity.class);
				intent.putExtra("Card", c);
				startActivity(intent);
			}
		});

		/*
		 * Params:
		 * - Context
		 * - The layout of each row
		 * - Whether the rows should display a button to add cards to a deck
		 */
		adapter = new CardRowAdapter(
				this, 
				R.layout.card_list_row_layout,
				addCardMode);
		listview.setAdapter(adapter);
		
		setKeyboardListener();
		
		updateList();
	}
	
	/**
	 * Sorts the list of cards on different attributes
	 * @param v - The button that calls this method
	 */
	public void setSort(View v) {
		Sort currentSort = null;
		switch (v.getId()) {
		case R.id.button_sort_title:
			currentSort = Sort.TITLE;
			break;
		case R.id.button_sort_rarity:
			currentSort = Sort.RARITY;
			break;
		case R.id.button_sort_cost:
			currentSort = Sort.COST;
			break;
		default:
			break;
		}
		cardlist.setCurrentSort(currentSort);
		updateList();
	}

	/**
	 * Filters the list of cards depending on the selected rarity filter checkboxes
	 */
	private void setRarities() {
		List<DBCard.Rarity> rarities = new ArrayList<DBCard.Rarity>();

		CheckBox free = (CheckBox) findViewById(R.id.checkbox_rarity_free);
		CheckBox common = (CheckBox) findViewById(R.id.checkbox_rarity_common);
		CheckBox rare = (CheckBox) findViewById(R.id.checkbox_rarity_rare);
		CheckBox epic = (CheckBox) findViewById(R.id.checkbox_rarity_epic);
		CheckBox legendary = (CheckBox) findViewById(R.id.checkbox_rarity_legendary);

		if (free.isChecked()) {
			rarities.add(DBCard.Rarity.FREE);
		}
		if (common.isChecked()) {
			rarities.add(DBCard.Rarity.COMMON);
		}
		if (rare.isChecked()) {
			rarities.add(DBCard.Rarity.RARE);
		}
		if (epic.isChecked()) {
			rarities.add(DBCard.Rarity.EPIC);
		}
		if (legendary.isChecked()) {
			rarities.add(DBCard.Rarity.LEGENDARY);
		}
		cardlist.setCurrentRarities(rarities);
		updateList();
	}
	
	/**
	 * Sets the current search string
	 * @param s - The string to search for
	 */
	private void setSearch(String s) {
		cardlist.setCurrentSearch(s);
		updateList();
	}
	
	/**
	 * Filters the list of cards depending on the selected rarity filter checkboxes
	 * @param v - The checkbox that calls this method
	 */
	public void setRarities(View v) {
		setRarities();
	}

	/**
	 * Inserts the selected cards from the cardlist into the views
	 */
	private void updateList() {
		if (prevCardRowLoader != null) {
			prevCardRowLoader.stop();
		}
		CardRowAsyncLoader cardRowLoader = new CardRowAsyncLoader();
		cardRowLoader.init(adapter);
		cardRowLoader.execute(cardlist.getSelectedCards());
		prevCardRowLoader = cardRowLoader;
	}
	
	/**
	 * When the add card button is clicked, this method is called
	 * @param v - The button that calls this method
	 */
	public void addCard(View v) {
		int position = listview.getPositionForView(v);
		DBCard c = cardlist.getSelectedCards().get(position);
		deck.addCard(c);
		CardDatabase db = new CardDatabase(this);
		db.writeDeckCard(c, deck);
		db.close();
	}
	
	/**
	 * Hides the keyboard when enter is pressed,
	 * and searches for the text in the searchbar
	 */
	private void setKeyboardListener() {
		final EditText searchBar = (EditText) findViewById(R.id.card_list_search);
		searchBar.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(searchBar.getApplicationWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					setSearch(searchBar.getText().toString());
					return true;
				}
				return false;
			}
		});
	}
	
	/**
	 * Open the Deck List Activity
	 */
	private void openDeckListActivity() {
		Intent intent = new Intent(getBaseContext(), DeckListActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Add creating a new deck to the action bar
	 * From: http://developer.android.com/guide/topics/ui/actionbar.html
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.card_list_action_bar, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_create_deck:
	        	openDeckListActivity();
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
