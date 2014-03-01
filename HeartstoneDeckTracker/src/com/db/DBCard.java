package com.db;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.hearthstonedecktracker.R;

/**
 * Represents a HearthStone card
 * The image attribute is a bitmap that only contains the top part of the card
 * If you want the whole thing, open the imageString in an assetmanager.
 */
public class DBCard implements Parcelable{

	//Table names
	public static final String CARD_TABLE_NAME = "card";
	public static final String CARD_LIST_ITEM_TABLE_NAME = "card_list_item";

	//Column names for the 'card' table
	public static final String ID_COLUMN = "id";
	public static final String TITLE_COLUMN = "title";
	public static final String IMAGE_COLUMN = "image";
	public static final String ABILITIES_COLUMN = "abilities";
	public static final String FLAVOR_TEXT_COLUMN = "flavor_text";
	public static final String COST_COLUMN = "cost";

	//Column names for the 'card_list_item' table
	public static final String CARD_LIST_ITEM_ID_COLUMN = "card_id";
	public static final String TERM_COLUMN = "term";
	public static final String DEFINITION_COLUMN = "definition";

	//Different card rarities and their associated colors
	public static enum Rarity {

		FREE("Free", R.color.rarity_free, 0),
		COMMON("Common", R.color.rarity_common, 1),
		RARE("Rare", R.color.rarity_rare, 2),
		EPIC("Epic", R.color.rarity_epic, 3),
		LEGENDARY("Legendary", R.color.rarity_legendary, 4);

		public String name;
		public int colorId;
		public int order;

		Rarity(String name, int colorId, int order) {
			this.name = name;
			this.colorId = colorId;
			this.order = order;
		}

		public int rarityCompareTo(Rarity r2) {
			return this.order - r2.order;
		}

	};

	//There are so few heroes that they can be stored in code also
	public static enum Hero {

		NEUTRAL("Neutral"),
		DRUID("Druid", 1, R.drawable.druid),
		HUNTER("Hunter", 2, R.drawable.hunter),
		MAGE("Mage", 3, R.drawable.mage),
		PALADIN("Paladin", 4, R.drawable.paladin),
		PRIEST("Priest", 5, R.drawable.priest),
		ROGUE("Rogue", 6, R.drawable.rogue),
		SHAMAN("Shaman", 7, R.drawable.shaman),
		WARLOCK("Warlock", 8, R.drawable.warlock),
		WARRIOR("Warrior", 9, R.drawable.warrior);

		public String name;
		public int id;
		public int imageId;

		Hero(String name, int id, int imageId) {
			this.name = name;
			this.id = id;
			this.imageId = imageId;
		}
		
		Hero(String name) {
			this.name = name;
		}
	}

	//Attributes
	private int id;
	private String title;
	private String imageString;
	private String abilities;
	private String flavorText;
	private int cost;
	private Map<String, String> attrs;
	private Bitmap image;

	public DBCard(int id, String title, String imageString, String abilities,
			String flavorText, int cost, Bitmap image) {
		this.id = id;
		this.title = title;
		this.imageString = imageString;
		this.abilities = abilities;
		this.flavorText = flavorText;
		this.cost = cost;
		this.attrs = new HashMap<String, String>();
		this.image = image;
	}

	/**
	 * 
	 * @param c - A database cursor from running the a query that returns all columns from the 'card' table
	 * @return - A DBCard object, without the 'attrs' defined
	 * @throws IOException 
	 */
	public static DBCard cursorToDBCard(Cursor c, AssetManager assets) throws IOException {
		Bitmap bitmap = BitmapFactory.decodeStream(assets.open(c.getString(c.getColumnIndex(IMAGE_COLUMN)) + ".png"));
		//Modify the size of the image to be only the top third, so it fits in a row
		Bitmap modifiedBitmap = Bitmap.createBitmap(
				bitmap,
				0, 
				0,
				bitmap.getWidth(),
				bitmap.getHeight() / 3
				);
		bitmap.recycle();
		return new DBCard(c.getInt(c.getColumnIndex(ID_COLUMN))
				, c.getString(c.getColumnIndex(TITLE_COLUMN))
				, c.getString(c.getColumnIndex(IMAGE_COLUMN))
				, c.getString(c.getColumnIndex(ABILITIES_COLUMN))
				, c.getString(c.getColumnIndex(FLAVOR_TEXT_COLUMN))
				, c.getInt(c.getColumnIndex(COST_COLUMN))
				, modifiedBitmap);
	}

	/**
	 * Converts the parcel to a DBCard
	 * @param in - The parcel coming in
	 */
	@SuppressWarnings("unchecked")
	private DBCard(Parcel in) {
		//Have to read the data in the same order as they were written
		this.id = in.readInt();
		this.title = in.readString();
		this.imageString = in.readString();
		this.abilities = in.readString();
		this.flavorText = in.readString();
		this.cost = in.readInt();
		this.attrs = (Map<String, String>) in.readHashMap(getClass().getClassLoader());
		this.image = (Bitmap) in.readParcelable(getClass().getClassLoader());
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
	 * Converts a DBCard to a parcel
	 */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(title);
		out.writeString(imageString);
		out.writeString(abilities);
		out.writeString(flavorText);
		out.writeInt(cost);
		out.writeMap(attrs);
		out.writeParcelable(image, flags);
	}

	/** This is used to regenerate DBCard. 
	 * All Parcelables must have a CREATOR that implements these two methods
	 */
	public static final Parcelable.Creator<DBCard> CREATOR = new Parcelable.Creator<DBCard>() {
		public DBCard createFromParcel(Parcel in) {
			return new DBCard(in);
		}
		public DBCard[] newArray(int size) {
			return new DBCard[size];
		}
	};

	/**
	 * @return - The rarity of a card
	 */
	public Rarity getRarity() {
		for (Rarity r : Rarity.values()) {
			if (r.name.equals(attrs.get("Rarity: "))) {
				return r;
			}
		}
		return null;
	}
	
	/**
	 * @return - The hero that can use this card, also known as the class
	 */
	public Hero getHero() {
		if (attrs.containsKey("Class: ")) {
			for (Hero h : Hero.values()) {
				if (h.name.equals(attrs.get("Class: "))) {
					return h;
				}
			}
		}
		return Hero.NEUTRAL;
	}

	/**
	 * Adds a attribute item to the DBCard from a Cursor
	 * @param c - A database cursor from running a query that returns all columns from the 'card_list_item' table
	 */
	public void addAttr(Cursor c) {
		this.attrs.put(c.getString(c.getColumnIndex(TERM_COLUMN)), c.getString(c.getColumnIndex(DEFINITION_COLUMN)));
	}

	public final int getId() {
		return id;
	}

	public final String getTitle() {
		return title;
	}

	public final String getImageString() {
		return imageString;
	}

	public final String getAbilities() {
		return abilities;
	}

	public final String getFlavorText() {
		return flavorText;
	}

	public final int getCost() {
		return cost;
	}

	public final Map<String, String> getAttrs() {
		return attrs;
	}

	public final void setId(int id) {
		this.id = id;
	}

	public final Bitmap getImage() {
		return image;
	}
}
