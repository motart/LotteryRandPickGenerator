package Common;

import java.util.List;
import java.util.Map;

public interface Game {
    String getData();
    List<Integer> getMostFrequentNumbers (float topPercent, Map<Integer,Integer> map);
    List<String> getBestRandomPicks (float topPercent, int numberOfTickets);
}
