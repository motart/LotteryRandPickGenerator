package MegaMillions;

import Common.Game;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class MegaMillions implements Game {
    private Map<Integer, Integer> firstNumberFrequency = new HashMap<>();
    private Map<Integer, Integer> secondNumberFrequency = new HashMap<>();
    private Map<Integer, Integer> thirdNumberFrequency = new HashMap<>();
    private Map<Integer, Integer> fourthNumberFrequency = new HashMap<>();
    private Map<Integer, Integer> fifthNumberFrequency = new HashMap<>();
    private Map<Integer, Integer> megaNumberFrequency = new HashMap<>();
    public static final String API_URL = "https://data.ny.gov/resource/";
    public static final String APP_TOKEN = "5xaw-6ayf";
    public static final String API_KEY = "f3c8ni9licdlqodlv1102f2wa";

    public String getData() {
        try {
            HttpResponse<String> response;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + APP_TOKEN + ".json"))
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response != null) {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {

        }
        return "";
    }

    public void runReport() throws Exception {
        String rawDataString = getData();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(rawDataString);
        Iterator<JsonNode> elements = rootNode.elements();
        while (elements.hasNext()) {
            JsonNode draw = elements.next();
            JsonNode winningNumbers = draw.get("winning_numbers");
            JsonNode megaBall = draw.get("mega_ball");
            processWinningNumbers(winningNumbers.toString().replace("\"",""));
            processMega(megaBall.toString().replace("\"",""));
        }
    }

    private void processMap(String numberString, Map<Integer,Integer> map) {
        Integer number = Integer.valueOf(numberString);
        Integer count = map.get(number);
        if (count == null) {
            map.put(number,1);
        } else {
            map.put(number, count + 1);
        }
    }

    private void processMega(String megaString) {
        processMap(megaString,megaNumberFrequency);
    }

    private void processWinningNumbers(String winningNumbers) throws Exception {
        String [] winningNumbersArray = winningNumbers.split(" ");

        for (int i = 0; i < winningNumbersArray.length; i++) {
            switch (i) {
                case 0:
                    processMap(winningNumbersArray[i],firstNumberFrequency);
                    break;
                case 1:
                    processMap(winningNumbersArray[i],secondNumberFrequency);
                    break;
                case 2:
                    processMap(winningNumbersArray[i],thirdNumberFrequency);
                    break;
                case 3:
                    processMap(winningNumbersArray[i],fourthNumberFrequency);
                    break;
                case 4:
                    processMap(winningNumbersArray[i],fifthNumberFrequency);
                    break;
                default:
                    throw new Exception("Invalid case");
            }
        }
    }

    public List<Integer> getMostFrequentNumbers(float topPercent, Map<Integer, Integer> map) {
        int maxFrequency = getMaxFrequency(map);
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer,Integer> entry : map.entrySet()) {
            if (entry.getValue() > ((float)maxFrequency * (1-topPercent))) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private int getMaxFrequency(Map<Integer, Integer> map) {
        int max = 0;
        for (Map.Entry<Integer,Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
            }
        }
        return max;
    }

    public List<String> getBestRandomPicks(float topPercent, int numberOfTickets) {
        List<String> result = new ArrayList<>();
        List<Integer> firstNumberList = getMostFrequentNumbers(topPercent,firstNumberFrequency);
        List<Integer> secondNumberList = getMostFrequentNumbers(topPercent,secondNumberFrequency);
        List<Integer> thirdNumberList = getMostFrequentNumbers(topPercent,thirdNumberFrequency);
        List<Integer> fourthNumberList = getMostFrequentNumbers(topPercent,fourthNumberFrequency);
        List<Integer> fifthNumberList = getMostFrequentNumbers(topPercent,fifthNumberFrequency);
        List<Integer> megaNumberList = getMostFrequentNumbers(topPercent,megaNumberFrequency);

        for (int i = 0; i < numberOfTickets; i++) {
            StringBuilder pick = new StringBuilder();
            pick.append(getRandomNumberFromList(firstNumberList)).append(" ")
                    .append(getRandomNumberFromList(secondNumberList)).append(" ")
                    .append(getRandomNumberFromList(thirdNumberList)).append(" ")
                    .append(getRandomNumberFromList(fourthNumberList)).append(" ")
                    .append(getRandomNumberFromList(fifthNumberList)).append(" * ")
                    .append(getRandomNumberFromList(megaNumberList));
            result.add(pick.toString());
        }
        return result;
    }

    private int getRandomNumberFromList(List<Integer> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }
    public void printRandomPicks(float topPercent, int numberOfTickets) {
        for (String draw : getBestRandomPicks(topPercent,numberOfTickets)) {
            System.out.println(draw);
        }
    }

    public void writeCSVs() throws IOException {
        writeCSV(firstNumberFrequency,"first.csv");
        writeCSV(secondNumberFrequency,"second.csv");
        writeCSV(thirdNumberFrequency,"third.csv");
        writeCSV(fourthNumberFrequency,"fourth.csv");
        writeCSV(fifthNumberFrequency,"fifth.csv");
        writeCSV(megaNumberFrequency,"mega.csv");
    }

    private void writeCSV(Map<Integer,Integer> map, String fileName) throws IOException {
        CsvSchema schema = null;
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        List<Map<String,Integer>> listOfMap = buildCSVMap(map);
        File file = new File(fileName);
        Writer writer = new FileWriter(file, true);
        if (listOfMap != null && !listOfMap.isEmpty()) {
            for (String col : listOfMap.get(0).keySet()) {
                schemaBuilder.addColumn(col);
            }
            schema = schemaBuilder.build().withLineSeparator("\r").withHeader();
        }
        CsvMapper mapper = new CsvMapper();
        mapper.writer(schema).writeValues(writer).writeAll(listOfMap);
        writer.flush();
    }

    private List<Map<String,Integer>> buildCSVMap(Map<Integer,Integer> map) {
        List<Map<String,Integer>> result = new ArrayList<>();
        for (Map.Entry<Integer,Integer> entry : map.entrySet()) {
            Map<String,Integer> element = new HashMap<>();
            element.put("number",entry.getKey());
            element.put("frequency",entry.getValue());
            result.add(element);
        }
        return result;
    }

}
