import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class FileTransmissionServer {
    private static final int PORT = 60020;

    public static void main(String[] args) {
        String path = args[0];
        File directory = new File(path);
        boolean find = false;

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                try (Socket connection = server.accept()) {
                    connection.setSoTimeout(3000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    OutputStream out = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

                    try {
                        if (directory.isDirectory()) {
                            File[] files = directory.listFiles();
                            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                                if (line.startsWith("index")) {
                                    StringBuilder builder = new StringBuilder();
                                    if (files == null) {
                                        writer.write("The directory is now empty!");
                                        writer.flush();
                                    } else {
                                        for (File file : files) {
                                            if (file.getName().endsWith("_Store")) continue;
                                            builder.append(file.getName());
                                            builder.append("\r\n");
                                        }
                                        writer.write(builder.toString());
                                        writer.flush();
                                    }
                                } else if (line.startsWith("get ")) {
                                    if (files == null) {
                                        writer.write("The directory is now empty!");
                                        writer.flush();
                                    } else {
                                        for (File file : files) {
                                            writer.flush();
                                            if (file.getName().equals(line.substring(4, line.length()-1))) {
                                                find = true;
                                                writer.write("ok\r\n");
                                                writer.flush();
                                                FileReader fileReader = new FileReader(file);
                                                BufferedReader bufReader = new BufferedReader(fileReader);
                                                StringBuilder builder = new StringBuilder();
                                                for (String theLine = bufReader.readLine(); theLine != null; theLine = bufReader.readLine()) {
                                                    builder.append(theLine).append("\r\n");
                                                }
                                                writer.write(builder.toString());
                                                writer.flush();
                                            }
                                        }
                                        if (!find) {
                                            writer.write("error\r\n");
                                            writer.flush();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                    } finally {
                        connection.close();
                    }

                }
            }
        } catch (IOException ex) {
            System.out.println("Could not start server");
        }
    }
}
