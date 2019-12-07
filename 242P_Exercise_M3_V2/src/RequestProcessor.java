import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestProcessor implements Runnable {
    private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());

    private File rootDirectory;
    private Socket connection;
    private boolean find = false;

    RequestProcessor(File rootDirectory, Socket connection) {
        if (rootDirectory == null) {
            throw new NullPointerException("rootDirectory does not exist");
        } else if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
        }
        this.rootDirectory = rootDirectory;
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            OutputStream out = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

            String command = reader.readLine();

            logger.info(connection.getRemoteSocketAddress() + " " + command);
            String[] tokens = command.split("\\s+");
            String method = tokens[0];
            String fileName = null;
            if (tokens.length > 1) fileName = tokens[1];

            File[] files = rootDirectory.listFiles();
            if (method.equals("index")) {
                StringBuilder builder = new StringBuilder();
                if (files == null) {
                    writer.write("rootDirectory is empty\r\n");
                    writer.flush();
                } else {
                    for (File file : files) {
                        if (file.getName().endsWith("_Store")) continue;
                        builder.append(file.getName()).append("\r\n");
                    }
                    writer.write(builder.toString());
                    writer.flush();
                }
            } else if (method.equals("get") && fileName != null) {
                if (files != null) {
                    for (File file : files) {
                        if (fileName.equals(file.getName())) {
                            find = true;
                            writer.write("ok\r\n");
                            writer.flush();

                            FileReader fileReader = new FileReader(file);
                            BufferedReader bufferedReader = new BufferedReader(fileReader);
                            StringBuilder builder = new StringBuilder();
                            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                                builder.append(line).append("\r\n");
                            }
                            writer.write(builder.toString());
                            writer.flush();
                        }
                    }
                }
                if (!find) {
                    writer.write("error\r\n");
                    writer.flush();
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error talking to " + connection.getRemoteSocketAddress(), ex);
        } finally {
            try {
                connection.close();
            } catch (IOException ex) {}
        }
    }
}
