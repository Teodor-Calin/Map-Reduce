import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapTaskResult {

    public String fileName;
    public Map<Integer, Integer> wordLengthsMap;
    public ArrayList<String> longestWords;

    public MapTaskResult(String fileName, Map<Integer, Integer> wordLengthsMap, ArrayList<String> longestWords) {
        this.fileName = fileName;
        this.wordLengthsMap = wordLengthsMap;
        this.longestWords = longestWords;
    }

}
