package com.db;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.db.DBCard.Hero;

/**
 * Represents a deck the user has made
 */
public class DBDeck implements Parcelable{

	//Table names
	public static final String DECK_TABLE_NAME = "deck";
	public static final String DECK_CARD_TABLE_NAME = "deck_card";
	public static final String HERO_TABLE_NAME = "hero";

	//Column names for the 'deck' table
	public static final String DECK_ID_COLUMN = "_id";
	public static final String DECK_NAME_COLUMN = "name";
	public static final String DECK_HERO_ID_COLUMN = "hero_id";

	//Column names for the 'deck_card' table
	public static final String DECK_CARD_CARD_ID_COLUMN = "card_id";
	public static final String DECK_CARD_DECK_ID_COLUMN = "deck_id";
	public static final String DECK_CARD_AMOUNT_COLUMN = "amount";

	//Column names for the 'hero' table
	public static final String HERO_ID_COLUMN = "id";
	public static final String HERO_NAME_COLUMN = "name";


	//Private attributes
	private Map<Integer, Integer> deck;
	private String name;
	private Hero hero;
	private int id;

	/**
	 * @param deck - The list of card id's
	 * @param name - The name of the deck
	 * @param hero - The hero used in the deck
	 */
	public DBDeck(Map<Integer, Integer> deck, String name, Hero hero, int id) {
		this.deck = deck;
		this.name = name;
		this.hero = hero;
		this.id = id;
	}

	/**
	 * If this method is used, it must be used in conjunction with the addCard method
	 * @param c - The cursor from the table 'card'
	 * @param allCards - All of the cards, accessible through MyApp
	 * @return - A DBDeck without cards
	 */
	@SuppressLint("UseSparseArrays")
	public static DBDeck cursorToDBDeck(Cursor c) {
		int heroId = c.getInt(c.getColumnIndex(DECK_HERO_ID_COLUMN));
		Hero hero = null;
		for (Hero h : Hero.values()) {
			if (h.id == heroId) {
				hero = h;
				break;
			}
		}
		return new DBDeck(
				new HashMap<Integer, Integer>(),
				c.getString(c.getColumnIndex(DECK_NAME_COLUMN)),
				hero,
				c.getInt(c.getColumnIndex(DECK_ID_COLUMN)));
	}

	
	/**
	 * Converts the parcel to a DBDeck
	 * @param in - The parcel coming in
	 */
	@SuppressWarnings("unchecked")
	public DBDeck(Parcel in) {
		//Have to read the data in the same order as they were written
		this.deck = (Map<Integer, Integer>) in.readHashMap(getClass().getClassLoader());
		this.name = in.readString();
		this.hero = (Hero) in.readSerializable();
		this.id = in.readInt();
	}
	
	/**
	 * Adds a card to the current deck, from a cursor
	 * @param c - The cursor from the table 'deck_card'
	 */
	public void addCard(Cursor c) {
		deck.put(c.getInt(c.getColumnIndex(DECK_CARD_CARD_ID_COLUMN)), 
				c.getInt(c.getColumnIndex(DECK_CARD_AMOUNT_COLUMN)));
	}
	

	/**
	 * Can ignore this, see: 
	 * http://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-android-activity-to-another-using-intents/2141166#2141166
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Converts a DBDeck to a parcel
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeMap(deck);
		dest.writeString(name);
		dest.writeSerializable(hero);
		dest.writeInt(id);
	}
	
	/** This is used to regenerate DBDeck. 
	 * All Parcelables must have a CREATOR that implements these two methods
	 */
    public static final Parcelable.Creator<DBDeck> CREATOR = new Parcelable.Creator<DBDeck>() {
        public DBDeck createFromParcel(Parcel in) {
            return new DBDeck(in);
        }
        public DBDeck[] newArray(int size) {
            return new DBDeck[size];
        }
    };

	public final Map<Integer, Integer> getDeck() {
		return deck;
	}

	public final void setDeck(Map<Integer, Integer> deck) {
		this.deck = deck;
	}
	
	public final void addCard(DBCard c) {
		if (deck.containsKey(c.getId())) {
			deck.put(c.getId(), deck.get(c.getId()) + 1);
		} else {
			deck.put(c.getId(), 1);
		}
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final Hero getHero() {
		return hero;
	}

	public final void setHero(Hero hero) {
		this.hero = hero;
	}

	public final int getId() {
		return id;
	}

	public final void setId(int id) {
		this.id = id;
	}
}
