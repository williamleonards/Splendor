import java.util.*;

public class Card {
	final Map<GemType, Integer> requirements;
	final int pts;
	final Tier tier;
	final GemType gemType;
	
	public Card(int[] requirements, int points, GemType gemType, Tier tier) {
		this.requirements = new HashMap<>();
		this.requirements.put(GemType.Brown,	requirements[0]);
		this.requirements.put(GemType.Red,	requirements[1]);
		this.requirements.put(GemType.Green,	requirements[2]);
		this.requirements.put(GemType.Blue,	requirements[3]);
		this.requirements.put(GemType.White,	requirements[4]);
		this.pts = points;
		this.gemType = gemType;
		this.tier = tier;
	}
	
	public String toString() {
		int[] reqs = new int[5];
		reqs[0] = requirements.get(GemType.Brown);
		reqs[1] = requirements.get(GemType.Red);
		reqs[2] = requirements.get(GemType.Green);
		reqs[3] = requirements.get(GemType.Blue);
		reqs[4] = requirements.get(GemType.White);
		return Arrays.toString(reqs);
	}
}
