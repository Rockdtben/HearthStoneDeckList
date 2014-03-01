package com.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.db.DBCard.Hero;
import com.db.DBDeck;
import com.hearthstonedecktracker.DeckListActivity;
import com.hearthstonedecktracker.HearthstoneDeckTracker;
import com.hearthstonedecktracker.R;

/**
 * Formats a deck into a row of a ListView
 */
public class DeckRowAdapter extends ArrayAdapter<Map<String, Object>> {

	private Context context;
	private DeckListActivity deckListActivity;
	
	/**
	 * @param context - The current context
	 * @param resource - The resource ID for a layout file containing a TextView to use when instantiating views
	 * @param deckListActivity - The activity to call when the delete button is pressed
	 */
	public DeckRowAdapter(Context context, int resource, DeckListActivity deckListActivity) {
		super(context, resource);
		this.context = context;
		this.deckListActivity = deckListActivity;
	}

	/**
	 * From: http://stackoverflow.com/questions/10161064/how-to-use-imageloader-class-in-simpleadapter-to-show-imageview-from-imageurl
	 */
	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.deck_list_row_layout, null);
        }

		HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);
		
		((ImageView) convertView.findViewById(R.id.deck_list_row_hero_image)).setImageResource(((Hero) data.get("Hero")).imageId);

        TextView deckName = (TextView) convertView.findViewById(R.id.deck_list_row_deck_name);
        deckName.setTypeface(HearthstoneDeckTracker.Fonts.BELWE_BOLD);
        deckName.setText((String) data.get("Deck name"));
        
        ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.deck_list_row_delete_button);
        deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deckListActivity.deleteDeck(position);
			}
        });
        
        TextView nrOfCards = (TextView) convertView.findViewById(R.id.deck_list_row_number_of_cards);
        nrOfCards.setTypeface(HearthstoneDeckTracker.Fonts.BELWE_BOLD);
        nrOfCards.setText(((String) data.get("Number of Cards")) + "/30");
        
        return convertView;
    }
	
	/**
	 * Creates a map of values to be inserted into a deck row from a DBDeck
	 * @param d - The deck to be inserted into the row
	 * @return - The map of values representing the deck
	 */
	public Map<String, Object> createCardRow(DBDeck d) {
		Map<String, Object> cardRow = new HashMap<String, Object>();
		cardRow.put("Hero", d.getHero());
		cardRow.put("Deck name", d.getName());
		int nrOfCards = 0;
		for (int amount : d.getDeck().values()) {
			nrOfCards += amount;
		}
		cardRow.put("Number of Cards", Integer.toString(nrOfCards));
		return cardRow;
	}
	
	/**
	 * Sets the contents of this adapter to a list of decks
	 * @param decks - The decks to be inserted
	 */
	public void setDecks(List<DBDeck> decks) {
		List<Map<String, Object>> cardRows = new ArrayList<Map<String, Object>>();
		for (DBDeck d : decks) {
			cardRows.add(createCardRow(d));
		}
		clear();
		addAll(cardRows);
		notifyDataSetChanged();
	}
	
}
