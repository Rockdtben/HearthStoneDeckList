package com.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class CardDatabase extends SQLiteAssetHelper {

	private static final String DATABASE_NAME = "hearthstone_cards.db";
	//IMPORTANT! Every time changes are made to the database, the DATABASE_VERSION has to be incremented
	private static final int DATABASE_VERSION = 9;

	private SQLiteDatabase db;
	private Context context;

	public CardDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		setForcedUpgrade(DATABASE_VERSION); //Forces to use current version, just overwrites what was there.
		//TODO: Remove forcing of current version, because now when the database changes, the user's decks are gone.
		db = getReadableDatabase();
		this.context = context;
	}
	
	/**
	 * Writes one card that belongs to a deck to the database.
	 * Increments the count of the card by one, if it's already in the database.
	 * @param card - The card to insert
	 * @param deck - The deck it belongs to
	 */
	public void writeDeckCard(DBCard card, DBDeck deck) {
		SQLiteQueryBuilder deckCardQB = new SQLiteQueryBuilder();
		deckCardQB.setTables(DBDeck.DECK_CARD_TABLE_NAME);
		String[] deckCardArgs = {Integer.toString(deck.getId()), Integer.toString(card.getId())};
		//From the table 'deck_card', get all columns WHERE deck_id=deck.getId() AND card_id=card.getId(), no ordering, no grouping
		Cursor deckCardCursor = deckCardQB.query(
				db,
				null,
				DBDeck.DECK_CARD_DECK_ID_COLUMN + "=? AND " + DBDeck.DECK_CARD_CARD_ID_COLUMN + "=?",
				deckCardArgs,
				null,
				null,
				null);
		//If there already is a deck card like the card to be inserted, add one to the count
		if (deckCardCursor.getCount() > 0) {
			deckCardCursor.moveToFirst();
			int amount = deckCardCursor.getInt(deckCardCursor.getColumnIndex(DBDeck.DECK_CARD_AMOUNT_COLUMN));
			amount += 1;
			ContentValues values = new ContentValues();
			values.put(DBDeck.DECK_CARD_AMOUNT_COLUMN, amount);
			db.update(
					DBDeck.DECK_CARD_TABLE_NAME,
					values, 
					DBDeck.DECK_CARD_DECK_ID_COLUMN + "=? AND " + DBDeck.DECK_CARD_CARD_ID_COLUMN + "=?",
					deckCardArgs);
		} else {
			ContentValues values = new ContentValues();
			values.put(DBDeck.DECK_CARD_CARD_ID_COLUMN, card.getId());
			values.put(DBDeck.DECK_CARD_DECK_ID_COLUMN, deck.getId());
			values.put(DBDeck.DECK_CARD_AMOUNT_COLUMN, 1);
			db.insert(DBDeck.DECK_CARD_TABLE_NAME, null, values);
		}
	}

	/**
	 * Inserts a DBDeck into the database
	 * @param deck
	 * @return - The id of the inserted deck
	 */
	public int writeDeck(DBDeck deck) {
		SQLiteQueryBuilder deckQB = new SQLiteQueryBuilder();
		deckQB.setTables(DBDeck.DECK_TABLE_NAME);
		//From the table 'deck', get all columns WHERE id=deck.getId(), no ordering, no grouping
		Cursor deckCursor = deckQB.query(db, null, DBDeck.DECK_ID_COLUMN + "='" + deck.getId() + "'", null, null, null, null);

		int deckId = 0;

		//If the deck with the given name already exists, update it, else insert a new deck
		if (deckCursor.getCount() > 0) {

			deckCursor.moveToFirst();
			deckId = deckCursor.getInt(deckCursor.getColumnIndex(DBDeck.DECK_ID_COLUMN));
			String[] deckIdString = {String.valueOf(deckId)};
			ContentValues values = new ContentValues();
			values.put(DBDeck.DECK_NAME_COLUMN, deck.getName());
			values.put(DBDeck.DECK_HERO_ID_COLUMN, deck.getHero().id);
			//Update deck where id=deckId
			db.update(DBDeck.DECK_TABLE_NAME, values, DBDeck.DECK_ID_COLUMN + "=?", deckIdString);

			//Delete deck_cards where deck_id=deckId
			db.delete(DBDeck.DECK_CARD_TABLE_NAME, DBDeck.DECK_CARD_DECK_ID_COLUMN + "=?", deckIdString);

		} else {

			ContentValues values = new ContentValues();
			//Creating a new id, so don't have to specify a value
			values.putNull(DBDeck.DECK_ID_COLUMN);
			values.put(DBDeck.DECK_NAME_COLUMN, deck.getName());
			values.put(DBDeck.DECK_HERO_ID_COLUMN, deck.getHero().id);
			deckId = (int) db.insert(DBDeck.DECK_TABLE_NAME, null, values);

		}
		deckCursor.close();

		//Insert deck_cards
		Map<Integer, Integer> deckCards = deck.getDeck();
		for (Integer cardId : deckCards.keySet()) {
			ContentValues values = new ContentValues();
			values.put(DBDeck.DECK_CARD_DECK_ID_COLUMN, deckId);
			values.put(DBDeck.DECK_CARD_CARD_ID_COLUMN, cardId);
			values.put(DBDeck.DECK_CARD_AMOUNT_COLUMN, deckCards.get(cardId));
			db.insert(DBDeck.DECK_CARD_TABLE_NAME, null, values);
		}
		
		return deckId;
	}
	
	/**
	 * Deletes the given deck from the database
	 * @param deck - The deck to delete
	 */
	public void deleteDeck(DBDeck deck) {
		String[] deckIdArg = {Integer.toString(deck.getId())};
		//Delete from the table 'deck' all decks with the id of deck.getId()
		db.delete(DBDeck.DECK_TABLE_NAME, DBDeck.DECK_ID_COLUMN + "=?", deckIdArg);
		//Delete from the table 'deck_card' all deck_cards with card_id = deck.getId()
		db.delete(DBDeck.DECK_CARD_TABLE_NAME, DBDeck.DECK_CARD_DECK_ID_COLUMN + "=?", deckIdArg);
	}

	/**
	 * Gets all the decks from the database
	 * @return - All the DBDecks, in a list
	 */
	public List<DBDeck> makeDecks() {
		List<DBDeck> allDecks = new ArrayList<DBDeck>();

		SQLiteQueryBuilder deckQB = new SQLiteQueryBuilder();
		deckQB.setTables(DBDeck.DECK_TABLE_NAME);
		//From the table 'deck' get all columns, all rows, no ordering, no grouping
		Cursor deckCursor = deckQB.query(db, null, null, null, null, null, null);

		//Make DBDecks, no card lists yet
		deckCursor.moveToFirst();
		while (!deckCursor.isAfterLast()) {
			DBDeck deck = DBDeck.cursorToDBDeck(deckCursor);
			allDecks.add(deck);
			deckCursor.moveToNext();
		}
		deckCursor.close();
		
		SQLiteQueryBuilder deckCardQB = new SQLiteQueryBuilder();
		deckCardQB.setTables(DBDeck.DECK_CARD_TABLE_NAME);
		//From the table 'deck_card' get all columns, all rows, no ordering, no grouping
		Cursor deckCardCursor = deckCardQB.query(db, null, null, null, null, null, null);
		
		//If there actually are any deck_cards
		if (deckCardCursor.getCount() > 0) {
			//Add the deck cards to the decks
			deckCardCursor.moveToFirst();
			while (!deckCardCursor.isAfterLast()) {
				int deckId = deckCardCursor.getInt(deckCardCursor.getColumnIndex(DBDeck.DECK_CARD_DECK_ID_COLUMN));
				for (DBDeck deck : allDecks) {
					if (deck.getId() == deckId) {
						deck.addCard(deckCardCursor);
					}
				}
				deckCardCursor.moveToNext();
			}
		}
		deckCardCursor.close();

		return allDecks;
	}

	/**
	 * Gets all the DBCards from the database
	 * @return - All the DBCards, fully populated, in a list
	 */
	public List<DBCard> makeDBCards() {
		List<DBCard> allCards = new ArrayList<DBCard>();
		AssetManager assetManager = context.getAssets();

		SQLiteQueryBuilder cardQB = new SQLiteQueryBuilder();
		cardQB.setTables(DBCard.CARD_TABLE_NAME);
		//From the table 'card' get all columns, all rows, no ordering, no grouping
		Cursor cardCursor = cardQB.query(db, null, null, null, null, null, null);

		//Make DBCards, no card lists yet
		cardCursor.moveToFirst();
		while (!cardCursor.isAfterLast()) {
			DBCard card;
			try {
				card = DBCard.cursorToDBCard(cardCursor, assetManager);
				allCards.add(card);
			} catch (IOException e) {
				e.printStackTrace();
			}
			cardCursor.moveToNext();
		}
		cardCursor.close();


		SQLiteQueryBuilder cardListQB = new SQLiteQueryBuilder();
		cardListQB.setTables(DBCard.CARD_LIST_ITEM_TABLE_NAME);
		//From the table 'card_list_item' get all columns, all rows, no ordering, no grouping
		Cursor cardListCursor = cardListQB.query(db, null, null, null, null, null, null);

		//Adding the card list items to the DBCards
		cardListCursor.moveToFirst();
		while (!cardListCursor.isAfterLast()) {
			for (DBCard card : allCards) {
				//If the id of a card equals the 'card_id' column of a row in the card list items
				if (card.getId() == cardListCursor.getInt(cardListCursor.getColumnIndex(DBCard.CARD_LIST_ITEM_ID_COLUMN))) {
					card.addAttr(cardListCursor);
				}
			}
			cardListCursor.moveToNext();
		}
		cardListCursor.close();

		return allCards;
	}

	public void close() {
		db.close();
	}
}
