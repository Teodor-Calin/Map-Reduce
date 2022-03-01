import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class MapTask implements Callable<MapTaskResult> {

    String fileName;
    int offset;
    int dim;
    AtomicInteger inQueue;
    ExecutorService tpe;


    public MapTask(String fileName, int offset, int dim, AtomicInteger inQueue, ExecutorService tpe) {
        this.fileName = fileName;
        this.offset = offset;
        this.dim = dim;
        this.inQueue = inQueue;
        this.tpe = tpe;
    }

    @Override
    public MapTaskResult call() throws IOException {

        FileReader fr = null;
        try {
            fr = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader buffered_reader = new BufferedReader(fr);
        StringBuilder t;
        boolean lastByteIsLetter = false;

        // citire + verificare inceput
        if (offset == 0) {

            char[] text = new char[dim];
            try {
                buffered_reader.read(text, 0, dim);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Character.isLetter(text[dim - 1])) {
                lastByteIsLetter = true;
            }

            t = new StringBuilder(new String(text, 0, dim));

        } else {

            try {
                buffered_reader.skip(offset - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }


            char[] text = new char[dim + 1];
            try {
                buffered_reader.read(text, 0, dim + 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Character.isLetter(text[dim])) {
                lastByteIsLetter = true;
            }

            int i = 1;
            if (Character.isLetter(text[0])) {
                while(Character.isLetter(text[i]) && i < dim) {
                    i++;
                }
            }


            if (i != dim) {
                t = new StringBuilder(new String(text, i, dim + 1 - i));
            } else {
                t = new StringBuilder();
            }

        }

        // verificare sfarsit
        if (lastByteIsLetter) {
            char c = ' ';
            try {
                c = (char) buffered_reader.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

            while(Character.isLetter(c)) {
                t.append(c);
                try {
                    c = (char) buffered_reader.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // obtinere lista de cuvinte din fragment
        String[] words = t.toString().split("[^a-zA-Z0-9]+");


        // se creeaza dictionarul de lungimi-cuvinte / numar aparitii
        Map<Integer, Integer> wordLengthsMap = new HashMap<>();
        ArrayList<String> longestWords = new ArrayList<>();
        int maxWordLength = 0;

        for (String i : words) {
            if (i.length() != 0) {
                wordLengthsMap.merge(i.length(), 1, Integer::sum);

                if (i.length() > maxWordLength) {
                    maxWordLength = i.length();
                    longestWords.clear();
                    longestWords.add(i);
                } else if (i.length() == maxWordLength) {
                    longestWords.add(i);
                }
            }
        }

        buffered_reader.close();

        int left = inQueue.decrementAndGet();
        if (left == 0) {
            tpe.shutdown();
        }

        Tema2.semaphore.release();

        return(new MapTaskResult(fileName, wordLengthsMap, longestWords));

    }
}
