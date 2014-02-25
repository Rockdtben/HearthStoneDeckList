package com.hearthstonedecklist;

import java.util.List;

import android.app.Application;
import android.graphics.Typeface;

import com.db.DBCard;

public class MyApp extends Application{
	
	private List<DBCard> allCards;
	
	public void onCreate() {
		super.onCreate();
		//Typeface caching, prevents memory leak. See: http://stackoverflow.com/questions/8057010/listview-memory-leak
		//To apply a font to a TextView: textView.setTypeface(MyApp.Fonts.BELWE_BOLD);
		initializeTypefaces();
	}

	public static class Fonts {
		public static Typeface BELWE_BOLD;
	}

	private void initializeTypefaces() {
		Fonts.BELWE_BOLD = Typeface.createFromAsset(getAssets(), "fonts/belwe_bold.ttf");
	}
	
	public void setAllCards(List<DBCard> allCards) {
		this.allCards = allCards;
	}

	public List<DBCard> getAllCards() {
		return allCards;
	}
}
