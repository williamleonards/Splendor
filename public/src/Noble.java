import java.util.*;

public class Noble {
	final Map<GemType, Integer> requirements;
	final int pts;
	public Noble(Map<GemType, Integer> requirements, int points) {
		this.requirements = requirements;
		this.pts = points;
	}
}
