import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ReduceTask implements Callable<ReduceTaskResult> {

    String fileName;
    List<Map<Integer, Integer>> mapList;
    List<List<String>> longestWordsList;
    AtomicInteger inQueue;
    ExecutorService tpe;

    public ReduceTask(String fileName, List<Map<Integer, Integer>> mapList, List<List<String>> longestWordsList, AtomicInteger inQueue, ExecutorService tpe) {
        this.fileName = fileName;
        this.mapList = mapList;
        this.longestWordsList = longestWordsList;
        this.inQueue = inQueue;
        this.tpe= tpe;
    }

    static int fib(int n)
    {
        if (n <= 1)
            return n;
        return fib(n-1) + fib(n-2);
    }

    @Override
    public ReduceTaskResult call() {

        // combinarea dictionarelor
        Map<Integer, Integer> wordLengthsMap = new HashMap<>();
        for (Map<Integer, Integer> i : mapList) {
            for (Map.Entry<Integer, Integer> entry : i.entrySet()) {
                wordLengthsMap.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        // combinarea listelor de cuvinte de lungime maxima
        int max = 0;
        List<String> longestWords = new ArrayList<>();
        for (List<String> i : longestWordsList) {
            for (String word : i) {
                if (word.length() > max) {
                    max = word.length();
                    longestWords.clear();
                    longestWords.add(word);
                } else if (word.length() == max) {
                    longestWords.add(word);
                }
            }
        }

        // calculare rang fisier
        int nrWords = 0;
        float rang = 0;

        for (Map.Entry<Integer, Integer> entry : wordLengthsMap.entrySet()) {
            nrWords += entry.getValue();
            rang += fib(entry.getKey() + 1) * entry.getValue();
        }
        rang /= nrWords;



        int left = inQueue.decrementAndGet();
        if (left == 0) {
            tpe.shutdown();
        }

        Tema2.semaphore.release();

        return new ReduceTaskResult(fileName, rang, max, longestWords.size());
    }
}
