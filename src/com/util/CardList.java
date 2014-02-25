package com.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.db.DBCard;
import com.db.DBCard.Hero;
import com.db.DBCard.Rarity;
import com.hearthstonedecklist.MyApp;

public class CardList {

	public enum Sort {
		TITLE,
		RARITY,
		COST
	};

	private MyApp app;
	private List<DBCard> selectedCards;
	private Sort currentSort;
	private String currentSearch;
	private List<Rarity> currentRarities;
	private List<Hero> currentHeroes;

	public CardList(MyApp app) {
		this.app = app;
		selectedCards = app.getAllCards();
		//Sort by rarity initially
		currentSort = Sort.RARITY;
		//Set the current search to nothing
		currentSearch = "";
		//Set current rarities to all rarities
		currentRarities = new ArrayList<Rarity>(Arrays.asList(Rarity.values()));
		//Set current heroes to all heroes, including neutral
		currentHeroes = new ArrayList<Hero>(Arrays.asList(Hero.values()));
	}

	private void updateList() {
		selectedCards = app.getAllCards();
		search();
		filterRarity();
		filterHeroes();
		sort();
	}

	private void search() {
		List<DBCard> searchedCards = new ArrayList<DBCard>();
		for (DBCard c : selectedCards) {
			//Either the title contains the search string, or the attrs contain it
			if (c.getTitle().toLowerCase(Locale.US).contains(currentSearch.toLowerCase(Locale.US)) ||
					StringUtils.join(c.getAttrs().values(), " ").toLowerCase(Locale.US).contains(currentSearch.toLowerCase(Locale.US))) {
				searchedCards.add(c);
			}
		}
		selectedCards = searchedCards;
	}

	private void filterRarity() {
		List<DBCard> filteredCards = new ArrayList<DBCard>();
		for (DBCard c : selectedCards) {
			if (currentRarities.contains(c.getRarity())) {
				filteredCards.add(c);
			}
		}
		selectedCards = filteredCards;
	}

	private void filterHeroes() {
		List<DBCard> filteredCards = new ArrayList<DBCard>();
		for (DBCard c : selectedCards) {
			if (currentHeroes.contains(c.getHero())) {
				filteredCards.add(c);
			}
		}
		selectedCards = filteredCards;
	}

	private void sort() {
		switch (currentSort) {
		case TITLE:
			Collections.sort(selectedCards, getTitleComparator());
			break;
		case RARITY:
			Collections.sort(selectedCards, getRarityComparator());
			break;
		case COST:
			Collections.sort(selectedCards, getCostComparator());
			break;
		default:
			break;
		}
	}

	/**
	 * @return - A comparator that compares cards on the title
	 */
	private static Comparator<DBCard> getTitleComparator() {
		return new Comparator<DBCard>() {
			@Override
			public int compare(DBCard c1, DBCard c2) {
				return c1.getTitle().compareTo(c2.getTitle());
			}
		};
	}

	/**
	 * @return - A comparator that compares cards on the cost, secondary on rarity, tertiary on title
	 */
	private static Comparator<DBCard> getCostComparator() {
		return new Comparator<DBCard>() {
			@Override
			public int compare(DBCard c1, DBCard c2) {
				int costDifference = c1.getCost() - c2.getCost();
				if (costDifference == 0) {
					return c1.getTitle().compareTo(c2.getTitle());
				}
				return costDifference;
			}
		};
	}

	/**
	 * @return - A comparator that compares cards on the rarity, secondary on title
	 */
	private static Comparator<DBCard> getRarityComparator() {
		return new Comparator<DBCard>() {
			@Override
			public int compare(DBCard c1, DBCard c2) {
				int difference = c1.getRarity().rarityCompareTo(c2.getRarity());
				if (difference == 0) {
					return c1.getTitle().compareTo(c2.getTitle());
				}
				return difference;
			}
		};
	}

	public List<DBCard> getSelectedCards() {
		return selectedCards;
	}

	public void setCurrentSort(Sort s) {
		this.currentSort = s;
		updateList();
	}

	public void setCurrentSearch(String s) {
		this.currentSearch = s;
		updateList();
	}

	public void setCurrentRarities(List<Rarity> rarities) {
		this.currentRarities = rarities;
		updateList();
	}

	public void setCurrentHeroes(List<Hero> heroes) {
		this.currentHeroes = heroes;
		updateList();
	}
}
