public class TokenStack {
	final GemType color;
	int height;
	public TokenStack(GemType color, int h) {
		this.color = color;
		this.height = h;
	}
	public boolean isGold() {
		return color == GemType.Gold;
	}
	public boolean transferTo(Player p, int amt) {
		if (height < amt) {
			System.out.println("Deck " + color + " has not enough tokens");
			return false;
		}
		height -= amt;
		p.tokens.put(color, p.tokens.getOrDefault(color, 0) + amt);
		return true;
	}
	public boolean returnFrom(Player p, int amt) {
		int num = p.tokens.getOrDefault(color, 0);
		if (num < amt) {
			System.out.println("Player " + p.name + " has not enough " + color + " tokens");
			return false;
		}
		p.tokens.put(color, num - amt);
		height += amt;
		return true;
	}
} 