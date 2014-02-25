package com.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.db.DBCard;
import com.db.DBCard.Rarity;
import com.hearthstonedecklist.MyApp;
import com.hearthstonedecklist.R;

/**
 * Presents DBCards nicely into a row, 
 * optionally with a button to add the card to a deck
 */
public class CardRowAdapter extends ArrayAdapter<Map<String, Object>> {

	private Context context;
	private boolean showAddCard;
	
	/**
	 * @param context - The current context
	 * @param resource - The resource ID for a layout file containing a TextView to use when instantiating views.
	 * @param showAddCard - Whether to show a button for adding the card to a deck
	 */
	public CardRowAdapter(Context context, int resource, boolean showAddCard) {
		super(context, resource);
		this.context = context;
		this.showAddCard = showAddCard;
	}

	/**
	 * Converts a view into a card row
	 * From: http://stackoverflow.com/questions/10161064/how-to-use-imageloader-class-in-simpleadapter-to-show-imageview-from-imageurl
	 */
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.card_list_row_layout, null);
        }
        
		HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);

        TextView cost = (TextView) convertView.findViewById(R.id.card_list_row_cost);
        cost.setTypeface(MyApp.Fonts.BELWE_BOLD);
        cost.setText((String) data.get("Cost"));
        
        AutoResizeTextView title = (AutoResizeTextView) convertView.findViewById(R.id.card_list_row_title);
        title.setTypeface(MyApp.Fonts.BELWE_BOLD);
        title.setText((String) data.get("Title"));
        title.setTextColor(context.getResources().getColor(((Rarity) data.get("Rarity")).colorId));
        
        if (showAddCard) {
        	ImageButton addCard = (ImageButton) convertView.findViewById(R.id.card_list_row_add_card);
        	addCard.setVisibility(View.VISIBLE);
        }
        
        if (data.containsKey("Amount")) {
        	TextView amount = (TextView) convertView.findViewById(R.id.card_list_row_amount);
        	amount.setTypeface(MyApp.Fonts.BELWE_BOLD);
        	amount.setText(Integer.toString((Integer) data.get("Amount")));
        	amount.setVisibility(View.VISIBLE);
        }
        
        ((ImageView) convertView.findViewById(R.id.card_list_row_image)).setImageDrawable((Drawable) data.get("Image"));

        return convertView;
    }
	
	/**
	 * Creates a map of values to be inserted into a card row from a DBCard
	 * @param c - The DBCard that is to be inserted into the card row
	 * @return - The map of values representing the DBCard
	 */
	public Map<String, Object> createCardRow(DBCard c) {
		Map<String, Object> cardRow = new HashMap<String, Object>();

		cardRow.put("Title", c.getTitle());
		cardRow.put("Cost", Integer.toString(c.getCost()));

		Bitmap bitmap = c.getImage();
		//Modify the size of the image to be only the top third, so it fits in a row
		Bitmap modifiedBitmap = Bitmap.createBitmap(
				bitmap,
				0, 
				0,
				bitmap.getWidth(),
				bitmap.getHeight() / 3
				);
		//TODO: Make bitmap modifying into a class, as it will need to happen more often.
		cardRow.put("Image", new BitmapDrawable(context.getResources(), modifiedBitmap));
		cardRow.put("Rarity", c.getRarity());

		return cardRow;
	}
	
	/**
	 * Creates a card row with amounts of cards
	 * @param c - The DBCard that is to be inserted into the card row
	 * @param amount - The amount of the card in the deck
	 * @return - The map of values representing the DBCard
	 */
	public Map<String, Object> createCardRow(DBCard c, int amount) {
		Map<String, Object> cardRow = createCardRow(c);
		cardRow.put("Amount", amount);
		return cardRow;
	}
	
}
