import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TxtLineNumberCounterUserInterface {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(5);
        ArrayList<Future<Integer>> futures = new ArrayList<>();
        ArrayList<String> filenames = new ArrayList<>();

        for (String filename : args) {
            TxtLineNumberCounter task = new TxtLineNumberCounter(filename);
            futures.add(pool.submit(task));
            filenames.add(filename);
        }

        pool.shutdown();

        try {
            for (int i = 0; i < futures.size(); i++) {
                int number = futures.get(i).get();
                if (number < 0) continue;
                String result = filenames.get(i) + ": " + String.valueOf(number);
                System.out.println(result);
            }
        } catch (InterruptedException | ExecutionException ex) {
            System.out.println("Exception encountered: " + ex);
            ex.printStackTrace();
        }
    }
}
