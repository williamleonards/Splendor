import java.util.*;

public class CardDeck {
	//////TO BE FILLED
	static final Card[] defaultLow = new Card[] {
			new Card(new int[] {1,0,0,0,0}, 5, GemType.Brown, Tier.Low),
			new Card(new int[] {0,1,0,0,0}, 5, GemType.Brown, Tier.Low),
			new Card(new int[] {0,0,1,0,0}, 5, GemType.Brown, Tier.Low),
			new Card(new int[] {0,0,0,1,0}, 5, GemType.Brown, Tier.Low),
			new Card(new int[] {0,0,0,0,1}, 5, GemType.Brown, Tier.Low)}; 
	static final Card[] defaultMid = new Card[] {
			new Card(new int[] {1,0,0,0,0}, 5, GemType.Brown, Tier.Mid),
			new Card(new int[] {0,1,0,0,0}, 5, GemType.Brown, Tier.Mid),
			new Card(new int[] {0,0,1,0,0}, 5, GemType.Brown, Tier.Mid),
			new Card(new int[] {0,0,0,1,0}, 5, GemType.Brown, Tier.Mid),
			new Card(new int[] {0,0,0,0,1}, 5, GemType.Brown, Tier.Mid)};
	static final Card[] defaultHigh = new Card[] {
			new Card(new int[] {1,0,0,0,0}, 5, GemType.Brown, Tier.High),
			new Card(new int[] {0,1,0,0,0}, 5, GemType.Brown, Tier.High),
			new Card(new int[] {0,0,1,0,0}, 5, GemType.Brown, Tier.High),
			new Card(new int[] {0,0,0,1,0}, 5, GemType.Brown, Tier.High),
			new Card(new int[] {0,0,0,0,1}, 5, GemType.Brown, Tier.High)};
	
	LinkedList<Card> cards;
	public HashSet<Card> display;
	
	public CardDeck(Tier tier) {
		this.cards = new LinkedList<>(); 
		if (tier == Tier.Low) {
			for (int i = 0; i < defaultLow.length; i++) {
				cards.add(defaultLow[i]);
			}
		} else if (tier == Tier.Mid) {
			for (int i = 0; i < defaultMid.length; i++) {
				cards.add(defaultMid[i]);
			}
		} else {
			for (int i = 0; i < defaultHigh.length; i++) {
				cards.add(defaultHigh[i]);
			}
		}
		Collections.shuffle(this.cards);
		display = new HashSet<>();
		for (int i = 0; i < 4; i++) {
			display.add(draw());
		}
	}
	public boolean isEmpty() {
		return cards.isEmpty();
	}
	public Card draw() {
		if (isEmpty()) {
			System.out.println("Out of cards, returning null");
			return null;
		}
		return cards.poll();
	}
	public boolean take(Card c) {
		if (!display.contains(c)) {
			System.out.println("Card not on display!");
			return false;
		}
		display.remove(c);
		if (!isEmpty()) {
			display.add(draw());
		}
		return true;
	}
}

// CUSTOM DECK CONSTRUCTOR
//public CardDeck(LinkedList<Card> cards) {
//this.cards = cards;
//Collections.shuffle(this.cards);
//}