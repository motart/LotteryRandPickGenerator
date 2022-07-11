import MegaMillions.MegaMillions;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Driver {
    public static void main(String[] args) throws Exception {
        MegaMillions megaMillions = new MegaMillions();
        megaMillions.runReport();
        // megaMillions.writeCSVs();
        megaMillions.printRandomPicks(0.2f, 40);
    }
}
