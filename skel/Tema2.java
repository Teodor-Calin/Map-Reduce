import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema2 {

    public static Semaphore semaphore;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        if (args.length < 3) {
            System.err.println("Usage: Tema2 <workers> <in_file> <out_file>");
            return;
        }

        int nr_tasks = 0;
        int nr_workers = Integer.parseInt(args[0]);
        String in_file_path = args[1];
        String out_file_path = args[2];

        BufferedReader buffered_reader = new BufferedReader(new FileReader(in_file_path));

        int fragment_dim = Integer.parseInt(buffered_reader.readLine());
        int nr_files = Integer.parseInt(buffered_reader.readLine());

        String[] file_names = new String[nr_files];


        // calculare numar de taskuri de tip Map
        for (int i = 0; i < nr_files; i++) {
            file_names[i] = buffered_reader.readLine();
            File file = new File(file_names[i]);
            long file_dim = file.length();
            nr_tasks += file_dim / fragment_dim;
            if (file_dim % fragment_dim != 0) {
                nr_tasks ++;
            }
        }

        buffered_reader.close();


        // pornire taskuri de tip Map
        List<Future<MapTaskResult>> futureResults = new ArrayList<>();
        List<MapTaskResult> results = new ArrayList<>();
        semaphore = new Semaphore(1 - nr_tasks);
        AtomicInteger inQueue = new AtomicInteger(nr_tasks);
        ExecutorService tpe = Executors.newFixedThreadPool(nr_workers);

        for (int i = 0; i < nr_files; i++) {

            File file = new File(file_names[i]);
            long file_dim = file.length();
            int offset = 0;

            while(offset < file_dim) {
                int dim = fragment_dim;
                if (dim > file_dim - offset) {
                    dim = ((int)(file_dim - offset));
                }

                futureResults.add(tpe.submit(new MapTask(file_names[i], offset, dim, inQueue, tpe)));
                offset += fragment_dim;
            }
        }

        // semafor pentru a ne asigura ca s-au terminat toate taskurile de tip Map
        semaphore.acquire(); ///////////////////////////////////////


        for (int i = 0; i < nr_tasks; i++) {
            results.add(futureResults.get(i).get());
        }


        // pornire taskuri de tip reduce
        inQueue = new AtomicInteger(file_names.length);
        List<Future<ReduceTaskResult>> reduceFutureResults = new ArrayList<>();
        List<ReduceTaskResult> reduceResults = new ArrayList<>();
        semaphore = new Semaphore(1 - file_names.length);
        tpe = Executors.newFixedThreadPool(nr_workers);

        for (String i : file_names) {
            List<Map<Integer, Integer>> mapList = new ArrayList<>();
            List<List<String>> longestWordsList = new ArrayList<>();
            for (MapTaskResult j : results) {
                if (j.fileName.equals(i)) {
                    mapList.add(j.wordLengthsMap);
                    longestWordsList.add(j.longestWords);
                }
            }

            reduceFutureResults.add(tpe.submit(new ReduceTask(i, mapList, longestWordsList, inQueue, tpe)));
        }


        // semafor pentru a ne asigura ca s-au terminat toate taskurile de tip Reduce
        semaphore.acquire(); /////////////////////////////////////////

        for (int i = 0; i < file_names.length; i++) {
            reduceResults.add(reduceFutureResults.get(i).get());
        }

        // sortare lista de rezultate dupa rangul fisierelor
        reduceResults.sort((o1, o2) -> (int) ((o2.rang - o1.rang) * 100));

        // scrierea rezultatelor in fisierul de iesire
        File out_file = new File(out_file_path);
        FileWriter writer = new FileWriter(out_file);

        for (int i = 0; i < file_names.length; i++) {
            File f = new File(reduceResults.get(i).fileName);
            writer.write(f.getName() + ","
                    + String.format("%.2f", reduceResults.get(i).rang) + ","
                    + reduceResults.get(i).maxLength + ","
                    + reduceResults.get(i).nrMaxWords + "\n");
        }


        writer.close();
    }
}
