package com.hearthstonedecktracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

import com.async.CardRowAsyncLoader;
import com.db.CardDatabase;
import com.db.DBCard;
import com.db.DBCard.Hero;
import com.db.DBDeck;
import com.util.CardList;
import com.util.CardList.Sort;
import com.util.CardRowAdapter;

/**
 * Implements OnItemSelectedListener is for drop-down menu of the choice of hero filters
 * @author Soerian
 *
 */
@SuppressWarnings("unchecked")
public class CardListActivity extends Activity implements OnItemSelectedListener {

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
		cardlist = new CardList((HearthstoneDeckTracker) getApplication());
		setContentView(R.layout.card_list_activity);
		//If we're adding cards to a deck
		if (getIntent().hasExtra("Deck")) {
			addCardMode = true;
			deck = (DBDeck) getIntent().getExtras().getParcelable("Deck");
			List<Hero> heroes = new ArrayList<Hero>();
			heroes.add(deck.getHero());
			heroes.add(Hero.NEUTRAL);
			cardlist.setCurrentHeroes(heroes);
		} else {
			addCardMode = false;
			
			//Populate heroes spinner, heroes can only be selected when not adding cards to a deck
			Spinner spinner = (Spinner) findViewById(R.id.card_list_heroes_spinner);
			spinner.setVisibility(View.VISIBLE);
			ArrayAdapter<CharSequence> heroesAdapter = ArrayAdapter.createFromResource(
					this,
			        R.array.card_list_hero_options_array, 
			        android.R.layout.simple_spinner_item);
			heroesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(heroesAdapter);
			spinner.setOnItemSelectedListener(this);
		}
		
		
		//Tapping on a card reveals information about that card in a CardInfoActivity
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
		
		adapter = new CardRowAdapter(
				this, 
				R.layout.card_list_row_layout,
				addCardMode,
				false,
				showCardTitle());
		listview.setAdapter(adapter);
		
		setKeyboardListener();
		
		cardlist.setCurrentSort(Sort.RARITY);
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
	 * Filters the list of cards depending on the selected rarity filter toggle buttons
	 */
	private void setRarities() {
		List<DBCard.Rarity> rarities = new ArrayList<DBCard.Rarity>();

		ToggleButton free = (ToggleButton) findViewById(R.id.card_list_filter_button_rarity_free);
		ToggleButton common = (ToggleButton) findViewById(R.id.card_list_filter_button_rarity_common);
		ToggleButton rare = (ToggleButton) findViewById(R.id.card_list_filter_button_rarity_rare);
		ToggleButton epic = (ToggleButton) findViewById(R.id.card_list_filter_button_rarity_epic);
		ToggleButton legendary = (ToggleButton) findViewById(R.id.card_list_filter_button_rarity_legendary);

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
	
	public void setHeroes(List<Hero> heroes) {
		cardlist.setCurrentHeroes(heroes);
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
	 * Decides whether there is room to show the title of the cards
	 * @return - Whether to show the card titles
	 */
	private boolean showCardTitle() {
		boolean isTablet = getResources().getBoolean(R.bool.isTablet);
		int orientation = getResources().getConfiguration().orientation;
		return isTablet || orientation != Configuration.ORIENTATION_PORTRAIT || !addCardMode;
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

	//Spinner interface requirements
	/**
	 * Depending on what hero is selected, filters the list
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		//'All' is selected
		if (position == 0) {
			cardlist.setCurrentHeroes(new ArrayList<Hero>(Arrays.asList(Hero.values())));
		} else {
			ArrayList<Hero> heroes = new ArrayList<Hero>();
			heroes.add(DBCard.Hero.values()[position - 1]);
			setHeroes(heroes);
		}
		updateList();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}
}
