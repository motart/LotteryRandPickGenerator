import MegaMillions.MegaMillions;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Driver {
    public static void main(String[] args) throws Exception {
        MegaMillions megaMillions = new MegaMillions();
        megaMillions.runReport();
        megaMillions.printRandomPicks(0.4f, 10);
    }
}
