import java.util.*;
import java.io.*;
public class Game {
	int goal;
	boolean isFinalRound;
	String winner;
	
	ArrayList<Player> players;
	
	Map<Tier, CardDeck> decks;
	
	Map<GemType, TokenStack> tokenStacks;
	
	List<Noble> nobles;
	
	private int numTokens(GemType color, int players) {
		if (color == null) {
			System.out.println("Color cant be null");
			return 0;
		}
		if (color == GemType.Gold) return 5;
		if (players == 2) return 4;
		if (players == 3) return 5;
		if (players == 4) return 7;
		System.out.println("Invalid number of players");
		return 0;
	}
	
	public Game(ArrayList<Player> players, int goal) {
		this.players = players;
		this.goal = goal;
		this.isFinalRound = false;
		this.winner = "";
		
		decks = new HashMap<>();
		for (Tier t : Tier.values()) {
			decks.put(t, new CardDeck(t));
		}
		
		// parametrise in terms of # of players
		tokenStacks = new HashMap<>();
		for (GemType color : GemType.values()) {
			tokenStacks.put(color, new TokenStack(color, numTokens(color, players.size())));
		}
		
		NobleDeck nobleDeck = new NobleDeck();
		nobles = nobleDeck.generate(5); //
	}
	
	public void play() {
		while (!isFinalRound) {
			for (int i = 0; i < players.size(); i++) {
				System.out.println(players.get(i).name + " to move");
				
				String tokenData = "";
				for (GemType color : GemType.values()) {
					tokenData = tokenData + color + " -> " + tokenStacks.get(color).height + " ";
				}
				System.out.println(tokenData);
				for (Tier t : Tier.values()) {
					CardDeck d = decks.get(t);
					System.out.println(t + " : ");
					for (Card c : d.display) {
						System.out.println(c.toString());
					}
				}
				boolean flag = false;
				while (!flag) {
					try {
						// Use takeAction() method for AI
						flag = move(players.get(i));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						flag = false;
					}
				}
				System.out.println("_____________________");
			}
		}
		int best = 0;
		for (Player p : players) {
			best = Math.max(best, p.pts);
		}
		for (Player p : players) {
			if (p.pts == best) {
				System.out.println(p.name + " wins!");
			}
		}
	}
	
	public static Tier tierTranslator(String s) {
		if (s.equals("low")) return Tier.Low;
		if (s.equals("Mid")) return Tier.Mid;
		if (s.equals("High")) return Tier.High;
		return null;
	}
	
	public static GemType colorTranslator(String s) {
		if (s.equals("brown")) return GemType.Brown;
		if (s.equals("red")) return GemType.Red;
		if (s.equals("green")) return GemType.Green;
		if (s.equals("blue")) return GemType.Blue;
		if (s.equals("white")) return GemType.White;
		return null;
	}
	
	public Card cardSelector(int[] reqs) {
		for (Tier t : Tier.values()) {
			for (Card c: decks.get(t).display) {
				if (c.requirements.get(GemType.Brown) != reqs[0]) continue;
				if (c.requirements.get(GemType.Red) != reqs[1]) continue;
				if (c.requirements.get(GemType.Green) != reqs[2]) continue;
				if (c.requirements.get(GemType.Blue) != reqs[3]) continue;
				if (c.requirements.get(GemType.White) != reqs[4]) continue;
				return c;
			}
		}
		return null;
	}
	
	public Card cardSelector(int[] reqs, Set<Card> set) {
		for (Card c: set) {
			if (c.requirements.get(GemType.Brown) != reqs[0]) continue;
			if (c.requirements.get(GemType.Red) != reqs[1]) continue;
			if (c.requirements.get(GemType.Green) != reqs[2]) continue;
			if (c.requirements.get(GemType.Blue) != reqs[3]) continue;
			if (c.requirements.get(GemType.White) != reqs[4]) continue;
			return c;
		}
		return null;
	}
	
	/* move format: <command> <args>
	 * reserveFaceDown: <tier>
	 * reserveFaceUp: <requirements: brown red green blue white, separated by space>
	 * takeTwo: <token>
	 * takeThree: <token1> <token2> <token3>
	 * buy: <requirements>
	 * buyReserved: <requirements>
	 */
	public boolean move(Player p) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = br.readLine();
		String[] command = input.split(" ");
		
		int len = command.length;
		
		if (len < 2) {
			System.out.println("Command too short!");
			return false;
		}
		
		String type = command[0];
		if (type.equals("reserveFaceUp")) {
			if (len != 6) {
				System.out.println("Malformed reserve face up command");
				return false;
			}
			
			// Card characteristics
			int[] req = new int[6];
			for (int i = 1; i < 6; i++) {
				try {
					req[i-1] = Integer.parseInt(command[i]);
				} catch (NumberFormatException e) {
					System.out.println("Card characteristics need to be integers!");
					return false;
				}
			}
			return reserveFaceUp(p, cardSelector(req));
			
		} else if (type.equals("reserveFaceDown")) {
			if (len != 2) {
				System.out.println("Malformed reserve face down command");
				return false;
			}
			
			return reserveFaceDown(p, tierTranslator(command[1]));
			
		} else if (type.equals("takeTwo")) {
			if (len != 2) {
				System.out.println("Malformed take two command");
				return false;
			}
			
			return takeTwo(p, colorTranslator(command[1]));
			
		} else if (type.equals("takeThree")) {
			if (len != 4) {
				System.out.println("Malformed take three command");
				return false;
			}
			
			return takeThree(p, colorTranslator(command[1]), colorTranslator(command[2]), colorTranslator(command[3]));
			
		} else if (type.equals("buy")) {
			if (len != 6) {
				System.out.println("Malformed buy command");
				return false;
			}
			
			// Card characteristics
			int[] req = new int[6];
			for (int i = 1; i < 6; i++) {
				try {
					req[i-1] = Integer.parseInt(command[i]);
				} catch (NumberFormatException e) {
					System.out.println("Card characteristics need to be integers!");
					return false;
				}
			}
			return buy(p, cardSelector(req));
			
		} else if (type.equals("buyReserved")) {
			if (len != 6) {
				System.out.println("Malformed buy command");
				return false;
			}
			
			// Card characteristics
			int[] req = new int[6];
			for (int i = 1; i < 6; i++) {
				try {
					req[i-1] = Integer.parseInt(command[i]);
				} catch (NumberFormatException e) {
					System.out.println("Card characteristics need to be integers!");
					return false;
				}
			}
			return buy(p, cardSelector(req, p.reserved));
		}
		
		return false;
	}
	public boolean reserveFaceDown(Player p, Tier t) {
		if (p == null) {
			System.out.println("Null player argument (reserveFaceDown)");
			return false;
		}
		if (t == null) {
			System.out.println("Null tier argument (reserveFaceDown)");
			return false;
		}
		
		// Draw a card from the deck
		CardDeck deck = decks.get(t);
		Card obtained = deck.draw();
		if (obtained == null) return false;
		
		// Add it to player's reserve cards
		p.reserved.add(obtained);
		
		// Take a gold token if present
		tokenStacks.get(GemType.Gold).transferTo(p, 1);
		
		return true;
	}
	
	public boolean reserveFaceUp(Player p, Card c) {
		if (p == null) {
			System.out.println("Null player argument (reserveFaceUp)");
			return false;
		}
		if (c == null) {
			System.out.println("Null card argument (reserveFaceUp)");
			return false;
		}
		
		// Check if the card is on display
		Tier cardTier = c.tier;
		CardDeck deck = decks.get(cardTier);
		if (!deck.display.contains(c)) {
			System.out.println("Invalid card to reserve, not in display!");
			return false;
		}
		
		// Take the card from display
		boolean x = deck.take(c);
		if (!x) return false;
		
		// Add it to player's reserve cards
		p.reserved.add(c);
		
		// Take a gold token if present* (need to see rules for max gold tokens)
		tokenStacks.get(GemType.Gold).transferTo(p, 1);
		
		return true;
	}
	
	public boolean takeTwo(Player p, GemType color) {
		if (p == null) {
			System.out.println("Null player argument (takeTwo)");
			return false;
		}
		if (color == null) {
			System.out.println("Null color argument (takeTwo)");
			return false;
		}
		
		// Check if deck can be taken from
		TokenStack stack = tokenStacks.get(color);
		if (stack.height < 4) {
			System.out.println("Can't take two tokens from stack of height " + stack.height);
			return false;
		}
		
		// Take tokens from stack
		return stack.transferTo(p, 2);
	}
	
	public boolean takeThree(Player p, GemType color1, GemType color2, GemType color3) {
		if (p == null) {
			System.out.println("Null player argument (takeThree)");
			return false;
		}
		if (color1 == null || color2 == null || color3 == null) {
			System.out.println("Null color argument (takeThree)");
			return false;
		}
		
		if (color1 == color2 || color2 == color3 || color3 == color1) {
			System.out.println("Must be three distince colors");
			return false;
		}
		
		TokenStack stack1 = tokenStacks.get(color1);
		TokenStack stack2 = tokenStacks.get(color2);
		TokenStack stack3 = tokenStacks.get(color3);
		
//		if (stack1.height < 1 || stack2.height < 1 || stack3.height < 1) return false;
		
		// Some stacks may be empty
		stack1.transferTo(p, 1);
		stack2.transferTo(p, 1);
		stack3.transferTo(p, 1);
		
		return true;
	}
	
	public boolean buyReserved(Player p, Card c) {
		if (p == null) {
			System.out.println("Null player argument (buyReserved)");
			return false;
		}
		if (c == null) {
			System.out.println("Null card argument (buyReserved)");
			return false;
		}
		
		if (!p.reserved.contains(c)) {
			System.out.println("Card not reserved");
			return false;
		}

		int nGold = p.tokens.get(GemType.Gold);
		int deficit = 0;
		HashMap<GemType, Integer> pay = new HashMap<>(); // the amt you end up paying
		HashMap<GemType, Integer> need = new HashMap<>(); // the amt you need
		HashMap<GemType, Integer> had = new HashMap<>(); // the amt you have; SAME AS P.TOKENS

		// Check if affordable
		for (GemType color : GemType.values()) {
			if (color != GemType.Gold) {
				int needed = Math.max(0, c.requirements.get(color) - p.cardGems.get(color));
				int have = p.tokens.get(color);
				deficit += Math.max(needed - have, 0);
				pay.put(color, Math.min(needed, have));
				need.put(color,  needed);
				had.put(color, have);
			}
		}
		if (deficit > nGold) {
			System.out.println("Not enough tokens!");
			for (GemType color : GemType.values()) {
				if (color != GemType.Gold) {
					System.out.println("Have " + had.get(color) + " " + color + " tokens, need " + need.get(color));
				}
			}
			System.out.print("Deficit is " + deficit + ", only have " + nGold + " gold tokens");
			return false;
		}

		// Actually pay the tokens BACK TO THE STACK
		for (GemType color : GemType.values()) {
			TokenStack stack = tokenStacks.get(color);
			if (color != GemType.Gold) {
//				p.tokens.put(color, had.get(color) - pay.get(color));
				stack.returnFrom(p, pay.get(color));
			} else {
//				p.tokens.put(color, deficit);
				stack.returnFrom(p, deficit);
			}
		}
		
		// Remove from reserved
		p.reserved.remove(c);
		
		// Give the card to the player, update player's card gems and points
		p.owned.add(c);
		p.cardGems.put(c.gemType, p.cardGems.get(c.gemType) + 1);
		p.pts += c.pts;
		
		// CHECK IF PLAYER REACHED THE GOAL
		if (p.pts >= goal) {
			// SIGNAL FINAL ROUND /////
			isFinalRound = true;
		}
		return true;
	}
	
	public boolean buy(Player p, Card c) {
		if (p == null) {
			System.out.println("Null player argument (buy)");
			return false;
		}
		if (c == null) {
			System.out.println("Null card argument (buy)");
			return false;
		}
		
		int nGold = p.tokens.get(GemType.Gold);
		int deficit = 0;
		HashMap<GemType, Integer> pay = new HashMap<>(); // the amt you end up paying
		HashMap<GemType, Integer> need = new HashMap<>(); // the amt you need
		HashMap<GemType, Integer> had = new HashMap<>(); // the amt you have; SAME AS P.TOKENS
		
		// Check if the card is on display
		Tier cardTier = c.tier;
		CardDeck deck = decks.get(cardTier);
		if (!deck.display.contains(c)) {
			System.out.println("Invalid card to purchase, not in display!");
			return false;
		}
		
		// Check if affordable
		for (GemType color : GemType.values()) {
			if (color != GemType.Gold) {
				int needed = Math.max(0, c.requirements.get(color) - p.cardGems.get(color));
				int have = p.tokens.get(color);
				deficit += Math.max(needed - have, 0);
				pay.put(color, Math.min(needed, have));
				need.put(color,  needed);
				had.put(color, have);
			}
		}
		if (deficit > nGold) {
			System.out.println("Not enough tokens!");
			for (GemType color : GemType.values()) {
				if (color != GemType.Gold) {
					System.out.println("Have " + had.get(color) + " " + color + " tokens, need " + need.get(color));
				}
			}
			System.out.print("Deficit is " + deficit + ", only have " + nGold + " gold tokens");
			return false;
		}
		
		// Actually pay the tokens BACK TO THE STACK
		for (GemType color : GemType.values()) {
			TokenStack stack = tokenStacks.get(color);
			if (color != GemType.Gold) {
//				p.tokens.put(color, had.get(color) - pay.get(color));
				stack.returnFrom(p, pay.get(color));
			} else {
//				p.tokens.put(color, deficit);
				stack.returnFrom(p, deficit);
			}
		}
		
		// Remove from display, update the display
		deck.take(c);
		
		// Give the card to the player, update player's card gems and points
		p.owned.add(c);
		p.cardGems.put(c.gemType, p.cardGems.get(c.gemType) + 1);
		p.pts += c.pts;
		
		// CHECK IF PLAYER REACHED THE GOAL
		if (p.pts >= goal) {
			// SIGNAL FINAL ROUND /////
			isFinalRound = true;
		}
		return true;
	}
	
	public static void main(String[] args) {
		ArrayList<Player> players = new ArrayList<>();
		players.add(new Player("Player 1"));
		players.add(new Player("Player 2"));
		players.add(new Player("Player 3"));
		players.add(new Player("Player 4"));
		Game game = new Game(players, 5);
		game.play();
	}
}