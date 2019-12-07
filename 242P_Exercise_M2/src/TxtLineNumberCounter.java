import java.io.*;
import java.util.concurrent.Callable;

public class TxtLineNumberCounter implements Callable<Integer> {
    private String filename;

    TxtLineNumberCounter(String filename) {
        this.filename = filename;
    }

    @Override
    public Integer call() {
        int lineNum = 0;
        boolean succeed = true;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            while(reader.readLine() != null) lineNum++;
        } catch (FileNotFoundException ex) {
            System.out.println(filename + " doesn't exist.");
            succeed = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if(!succeed) return -1;
        return lineNum;
    }
}
