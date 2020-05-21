import java.util.*;
public class AI {
	class Pair<U, V> {
		U first; // number of turns required
		V second; // surplus tokens
		Pair(U x, V y) {
			first = x;
			second = y;
		}
	}
	public double turnUtility(double turns) {
		return turns;
	}
	public double prevGemUtility(double prevGems) {
		return prevGems;
	}
	// increased counted number of turns for having many take2 turns, since they are unreliable
	// 1st take2 -> 1 move, 2nd -> 1.5 moves, 3rd -> 2 moves, etc..
	public double take2Penalty(int x) {
		return x*(x-1)/4;
	}
	public Pair<Double, Integer> turnsRequired(Card c, Game g, Player p) {
		List<Pair<GemType, Integer>> requirements = new ArrayList<>();
		int turns = 0;
		int take2 = 0;
		for (GemType color : GemType.values()) {
			if (color != GemType.Gold) {
				if (g.tokenStacks.get(color).height == 0) {
					// have to reserve gold token at the moment
					turns += c.requirements.get(color);
					requirements.add(new Pair<GemType, Integer>(color, 0));
				} else {
					int required = c.requirements.get(color);
					required = Math.max(required - p.tokens.getOrDefault(color, 0) - p.cardGems.getOrDefault(color, 0), 0);
					requirements.add(new Pair<GemType, Integer>(color, required));
				}
			}
		}
			
		// card is unreachable; set high number of turns; 3 is max number of gold tokens
		if (turns > 3) return new Pair<Double, Integer>(100.0, 0);
		requirements.sort((a,b) -> b.second - a.second);
		while (requirements.get(0).second > 0) {
			Pair<GemType, Integer> color1 = requirements.get(0);
			Pair<GemType, Integer> color2 = requirements.get(1);
			Pair<GemType, Integer> color3 = requirements.get(2);
			// take two
			if (color2.second <= 0 && color1.second > 1 && g.tokenStacks.get(color1.first).height > 3) {
				color1.second -= 2;
				take2++;
				turns++;
			} else { // take three
				color1.second -= 1;
				color2.second -= 1;
				color3.second -= 1;
				turns++;
				requirements.sort((a,b) -> b.second - a.second);
			}
		}
		int surplus = 0;
		for (Pair<GemType, Integer> color : requirements) {
			surplus -= color.second;
		}
		return new Pair<Double, Integer>(turns + take2Penalty(take2), surplus);
	}
//	public boolean affordable(Card c, Player p) {
//		return false;
//	}
	public int[] cardRequirements(Card c) {
		int[] reqs = new int[5];
		reqs[0] = c.requirements.get(GemType.Brown);
		reqs[1] = c.requirements.get(GemType.Red);
		reqs[2] = c.requirements.get(GemType.Green);
		reqs[3] = c.requirements.get(GemType.Blue);
		reqs[4] = c.requirements.get(GemType.White);
		return reqs;
	}
	public int cost(Card c, Player p) {
		int[] costs = new int[] {1,2,3,4,5,7,10};
		int cost = 0;
		for (GemType color : GemType.values()) {
			// cost is counted according to the costs to get the remaining tokens of each color
			int require = Math.max(c.requirements.getOrDefault(color, 0) - p.cardGems.getOrDefault(color, 0), 0);
			for (int i = p.tokens.getOrDefault(color, 0); i < require; i++) {
				cost += costs[i];
			}
		}
		return cost;
	}
	public double cardUtility(Card c, Game g, Player p) {
		int points = c.pts;
		Pair<Double, Integer> pair = turnsRequired(c, g, p);
		double turns = pair.first;
		double noblePts = 0;
		GemType rewardGem = c.gemType;
		for (Noble n : g.nobles) {
			if (n.requirements.get(rewardGem) > p.cardGems.get(rewardGem)) {
				int left = 0;
				for (GemType color : GemType.values()) {
					if (color != GemType.Gold) {
						left += Math.max(0, n.requirements.get(color) - p.cardGems.get(color));
					}
				}
				noblePts += n.pts/(left);
			}
		}
		double tokenSurplus = pair.second / 3;
		int prevGems = p.cardGems.get(rewardGem);
		return - turnUtility(turns) + points + noblePts + tokenSurplus - prevGemUtility(prevGems);
	}
	public boolean affordCard(Card c, Game g, Player p, List<Card> considered) {
		List<Pair<GemType, Integer>> requirements = new ArrayList<>();
		for (GemType color : GemType.values()) {
			if (color != GemType.Gold) {
				int required = c.requirements.get(color);
				required = Math.max(required - p.tokens.getOrDefault(color, 0) - p.cardGems.getOrDefault(color, 0), 0);
				requirements.add(new Pair<GemType, Integer>(color, required));
			}
		}
		// sort by most required gem, then by scarcity
		requirements.sort((a,b) -> b.second - a.second != 0 ? 
				b.second - a.second : 
					(g.tokenStacks.get(a.first).height - g.tokenStacks.get(b.first).height));
		
		Pair<GemType, Integer> color1 = requirements.get(0);
		Pair<GemType, Integer> color2 = requirements.get(1);
		// take 2 where appropriate
		if (color1.second > 1 && color2.second <= 0) {
			if (g.tokenStacks.get(color1.first).height > 3) {
				System.out.println(p.name + " takes two of color " + color1);
				return g.tokenStacks.get(color1.first).transferTo(p, 2);
			}
			int numTokens = 0;
			for (GemType color : GemType.values()) {
				numTokens += p.tokens.getOrDefault(color, 0);
			}
			// if have > 8 tokens, reserve a card to get gold token
			if (numTokens > 8 && g.tokenStacks.get(GemType.Gold).height > 0) {
				if (!p.reserved.contains(c)) {
					System.out.println(p.name + " reserves card " + Arrays.toString(cardRequirements(c)));
					return g.reserveFaceUp(p, c);
				} else { // reserve the next best card
					for (int i = 1; i < considered.size(); i++) {
						Card x = considered.get(i);
						if (!p.reserved.contains(x)) {
							System.out.println(p.name + " reserves card " + Arrays.toString(cardRequirements(x)));
							return g.reserveFaceUp(p, x);
						}
					}
					// almost impossible to trigger; just reserve any card
					return g.reserveFaceDown(p, Tier.High);
				}
			}
		}
		// else, take three cards, or whatever's left
		int taken = 0;
		for (int i = 0; i < requirements.size(); i++) {
			boolean status = g.tokenStacks.get(requirements.get(i).first).transferTo(p, 1);
			if (status) {
				System.out.println(p.name + " takes token " + requirements.get(i).first);
				taken++;
			}
			if (taken == 3) return true;
		}
		if (taken == 1) {
			for (int i = 0; i < requirements.size(); i++) {
				boolean status = g.tokenStacks.get(requirements.get(i).first).transferTo(p, 1);
				if (status) {
					System.out.println(p.name + " takes token " + requirements.get(i).first);
					return true;
				}
			}
		}
		return true;
	}
	public boolean makeMove(Game g, Player p) {
		ArrayList<Card> visibleCards = new ArrayList<>();
		for (Card c : p.reserved) {
			visibleCards.add(c);
		}
		for (Tier t : Tier.values()) {
			for (Card c : g.decks.get(t).display) {
				visibleCards.add(c);
			}
		}
		ArrayList<Card> considered = new ArrayList<>();
		// consider cards with cost at most 10
		for (Card c : visibleCards) {
			if (cost(c, p) <= 10) considered.add(c);
		}
		considered.sort((a,b) -> { // can make a map of card to its utlity ?
			double A = cardUtility(a, g, p);
			double B = cardUtility(b, g, p);
			return A > B ? -1 : (A == B ? 0 : 1); // utility in descending order
		});
		// **at worst**, consider all cards
		if (considered.size() == 0) {
			considered = visibleCards;
			considered.sort((a,b) -> { // can make a map of card to its utlity ?
				double A = cardUtility(a, g, p);
				double B = cardUtility(b, g, p);
				return A > B ? -1 : (A == B ? 0 : 1); // utility in descending order
			});
		}
		Card bestCard = considered.get(0);
		
		// try buying the best card
		boolean flag = g.buy(p, bestCard);
		if (flag) {
			System.out.println(p.name + "buys the card" + Arrays.toString(cardRequirements(bestCard)));
			return true;
		}
		
		// take tokens towards that card
		return affordCard(bestCard, g, p, considered);
	}
}
