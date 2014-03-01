package com.hearthstonedecktracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.db.DBCard.Hero;

public class CreateDeckHeroActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUI();
	}
	
	private void initUI() {
		setContentView(R.layout.create_deck_hero_activity);
	}
	
	/**
	 * Called by the hero buttons
	 * @param v - The view that calls this method
	 */
	public void createDeckName(View v) {
		Hero hero = null;
		switch (v.getId()) {
		case R.id.create_deck_hero_druid:
			hero = Hero.DRUID;
			break;
		case R.id.create_deck_hero_hunter:
			hero = Hero.HUNTER;
			break;
		case R.id.create_deck_hero_mage:
			hero = Hero.MAGE;
			break;
		case R.id.create_deck_hero_paladin:
			hero = Hero.PALADIN;
			break;
		case R.id.create_deck_hero_priest:
			hero = Hero.PRIEST;
			break;
		case R.id.create_deck_hero_rogue:
			hero = Hero.ROGUE;
			break;
		case R.id.create_deck_hero_shaman:
			hero = Hero.SHAMAN;
			break;
		case R.id.create_deck_hero_warlock:
			hero = Hero.WARLOCK;
			break;
		case R.id.create_deck_hero_warrior:
			hero = Hero.WARRIOR;
			break;
		default:
			break;
		}
		Intent intent = new Intent(getBaseContext(), CreateDeckNameActivity.class);
		intent.putExtra("Hero", hero);
		startActivity(intent);
	}
}
