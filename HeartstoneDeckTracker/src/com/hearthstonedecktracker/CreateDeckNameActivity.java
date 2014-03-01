package com.hearthstonedecktracker;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.db.CardDatabase;
import com.db.DBCard.Hero;
import com.db.DBDeck;

public class CreateDeckNameActivity extends Activity {
	
	private Hero hero;
	private String name;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hero = getHero();
		initUI();
		setKeyboardListener();
	}
	
	private void initUI() {
		setContentView(R.layout.create_deck_name_activity);
	}
	
	private Hero getHero() {
		return (Hero) getIntent().getExtras().getSerializable("Hero");
	}
	
	/**
	 * @param v - The button that calls this method
	 */
	public void confirmFromButton(View v) {
		EditText nameTextBox = (EditText) findViewById(R.id.create_deck_name_textfield);
		name = nameTextBox.getText().toString();
		confirm();
	}
	
	/**
	 * Confirms the name of the new deck, inserts it into the database,
	 * and starts the deck overview activity
	 */
	@SuppressLint("UseSparseArrays")
	public void confirm() {
		DBDeck deck = new DBDeck(new HashMap<Integer, Integer>(), name, hero, -1);
		CardDatabase db = new CardDatabase(this);
		int deckId = db.writeDeck(deck);
		db.close();
		deck.setId(deckId);
		Intent intent = new Intent(getBaseContext(), DeckOverviewActivity.class);
		intent.putExtra("Deck", deck);
		startActivity(intent);
	}
	
	/**
	 * Hides the keyboard when enter is pressed, confirms the name
	 */
	private void setKeyboardListener() {
		final EditText nameTextBox = (EditText) findViewById(R.id.create_deck_name_textfield);
		nameTextBox.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(nameTextBox.getApplicationWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					name = nameTextBox.getText().toString();
					confirm();
					return true;
				}
				return false;
			}
		});
	}
}
