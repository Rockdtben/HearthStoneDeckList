package com.hearthstonedecktracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.DBCard;
import com.util.AutoResizeTextView;
import com.util.CustomTypefaceSpan;

public class CardInfoActivity extends Activity {
	
	/* 
	 * A list of priorities of attributes, 
	 * meaning which attribute comes where on the page.
	 * The lower the higher up the page.
	 */
	private static final String[] ps = {"Type: ", "Class: ", "Rarity: ", "Set: ", "Crafting Cost: ", "Arcane Dust Gained: ", "Artist: ", "Race: ", "Faction: ", "Collectible", "Elite"};
	private static final List<String> priorities = new ArrayList<String>(Arrays.asList(ps));
	
	private LinearLayout mainLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LayoutInflater inflater = 
			      (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainLayout = (LinearLayout) inflater.inflate(R.layout.card_info_activity, null);
		setContentView(mainLayout);
		
		DBCard card = getCard();
		
		setOnItemClickListener();
		
		ImageView cardImageView = (ImageView) findViewById(R.id.card_info_image);
		setImage(cardImageView, card);
		
		LinearLayout attrs = (LinearLayout) findViewById(R.id.card_info_attrs);
		
		TextView title = new TextView(this);
		formatTitle(title, card);
		title.setText(card.getTitle());
		attrs.addView(title);
		
		AutoResizeTextView attrsTextView = new AutoResizeTextView(this);
		formatAttrs(attrsTextView);
		setAttrsText(attrsTextView, card);
		attrs.addView(attrsTextView);
	}
	
	/**
	 * @return - the card given in the intent
	 */
	private DBCard getCard() {
		return (DBCard) getIntent().getExtras().getParcelable("Card");
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
	
	private void setOnItemClickListener() {
		mainLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
	
	private void setImage(ImageView cardImageView, DBCard card) {
		//The DBCard itself only contains the top third of the image, so we have to load the whole thing
		AssetManager assets = getAssets();
		Bitmap b;
		try {
			b = BitmapFactory.decodeStream(assets.open(card.getImageString() + ".png"));
			cardImageView.setImageDrawable(new BitmapDrawable(getResources(), b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void formatTitle(TextView title, DBCard card) {
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		titleParams.gravity = Gravity.CENTER;
		title.setLayoutParams(titleParams);
		title.setTypeface(HearthstoneDeckTracker.Fonts.BELWE_BOLD);
		title.setTextColor(getResources().getColor(card.getRarity().colorId));
		title.setTextSize(getResources().getDimension(R.dimen.card_info_title_text_size));
	}
	
	private void formatAttrs(AutoResizeTextView attrsTextView) {
		LinearLayout.LayoutParams attrsTextParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		attrsTextParams.gravity = Gravity.LEFT;
		attrsTextView.setLayoutParams(attrsTextParams);
		int padding = (int) getResources().getDimension(R.dimen.card_info_attrs_padding);
		attrsTextView.setPadding(padding, padding, padding, padding);
		attrsTextView.setTextSize(getResources().getDimension(R.dimen.card_info_attrs_text_size));
		//the second argument of setLineSpacing is the multiplier
		attrsTextView.setLineSpacing(getResources().getDimension(R.dimen.card_info_line_spacing), 1);
	}
	
	private void setAttrsText(AutoResizeTextView attrsTextView, DBCard card) {
		String text = "";
		Map<String, String> cardAttrs = card.getAttrs();
		//Attributes are displayed in order
		List<String> cardAttrTerms = new ArrayList<String>(cardAttrs.keySet());
		Collections.sort(cardAttrTerms, new TermComparator());
		
		for (String attr : cardAttrTerms) {
			text += attr + " " + cardAttrs.get(attr) + "\n";
		}
		SpannableStringBuilder sb = new SpannableStringBuilder(text);
		//Coloring the rarity
		ForegroundColorSpan rarityColor = new ForegroundColorSpan(getResources().getColor(card.getRarity().colorId));
		String rarityString = cardAttrs.get("Rarity: ");
		sb.setSpan(
				rarityColor, 
				text.indexOf(rarityString), 
				text.indexOf(rarityString) + rarityString.length(), 
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		//Bolding the attribute keys
		for (String attr : cardAttrs.keySet()) {
			CustomTypefaceSpan belweBoldSpan = new CustomTypefaceSpan("serif", HearthstoneDeckTracker.Fonts.BELWE_BOLD);
			sb.setSpan(
					belweBoldSpan,
					text.indexOf(attr),
					text.indexOf(attr) + attr.length(),
					Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}
		attrsTextView.setText(sb);
	}
	
	private class TermComparator implements Comparator<String> {
		
		@Override
		public int compare(String a, String b) {
			int priorityA = priorities.indexOf(a) != -1 ? priorities.indexOf(a) : 100;
			int priorityB = priorities.indexOf(b) != -1 ? priorities.indexOf(b) : 100;
			return priorityA - priorityB;
		}
	}
}
