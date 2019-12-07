import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class FileTransmissionClient {
    private static final String HOST_NAME = "localhost";
    private static final int PORT = 60020;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST_NAME, PORT)) {
            socket.setSoTimeout(10000);
            OutputStream out = socket.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(arg).append(" ");
            }
            writer.write(builder.toString() + "\r\n");
            writer.flush();

            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                System.out.println(line);
            }

        } catch (IOException ex) {
            System.out.println("Could not connect to server");
        }
    }
}
