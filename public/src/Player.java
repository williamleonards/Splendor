import java.util.*;

public class Player {
	final String name;
	int pts;
	Map<GemType, Integer> cardGems;
	Map<GemType, Integer> tokens;
	HashSet<Card> reserved;
	HashSet<Card> owned;
	
	public Player(String name) {
		this.name = name;
		pts = 0;
		cardGems = new HashMap<>();
		tokens = new HashMap<>();
		reserved = new HashSet<>();
		owned = new HashSet<>();
		
		for (GemType color : GemType.values()) {
			cardGems.put(color, 0);
			tokens.put(color, 0);
		}
	}
}