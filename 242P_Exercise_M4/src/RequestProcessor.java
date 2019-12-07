import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestProcessor implements Runnable {
    private final int port;
    private final int bufferSize;
    private final File rootDirectory;
    private volatile boolean find = false;
    private final Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());

    RequestProcessor(int port, int bufferSize, File rootDirectory) {
        this.port = port;
        this.bufferSize = bufferSize;

        if (rootDirectory == null) {
            throw new NullPointerException("rootDirectory does not exist");
        } else if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
        }
        this.rootDirectory = rootDirectory;
    }

    RequestProcessor(int port, File rootDirectory) { this(port, 8192, rootDirectory); }

    @Override
    public void run() {
        byte[] buffer = new byte[bufferSize];
        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (true) {
                DatagramPacket inComing = new DatagramPacket(buffer, buffer.length);
                socket.receive(inComing);

                String command = new String(inComing.getData(), 0, inComing.getLength(), StandardCharsets.UTF_8);

                logger.info(inComing.getSocketAddress() + " " + command);
                String[] tokens = command.split("\\s+");
                String method = tokens[0];
                String fileName = null;
                if (tokens.length > 1) fileName = tokens[1];

                File[] files = rootDirectory.listFiles();
                if (method.equals("index")) {
                    StringBuilder builder = new StringBuilder();
                    if (files == null) {
                        String alert = "rootDirectory is empty\r\n";
                        byte[] alertBytes = alert.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket outGoing = new DatagramPacket(alertBytes, alertBytes.length, inComing.getSocketAddress());
                        socket.send(outGoing);
                    } else {
                        for (File file : files) {
                            if (file.getName().endsWith("_Store")) continue;
                            builder.append(file.getName()).append("\r\n");
                        }
                        byte[] resultBytes = builder.toString().getBytes(StandardCharsets.UTF_8);
                        DatagramPacket outGoing = new DatagramPacket(resultBytes, resultBytes.length, inComing.getSocketAddress());
                        socket.send(outGoing);
                    }
                } else if (method.equals("get") && fileName != null) {
                    if (files != null) {
                        for (File file : files) {
                            if (fileName.equals(file.getName())) {
                                find = true;
                                String result = "ok\r\n";
                                byte[] resultBytes = result.getBytes(StandardCharsets.UTF_8);
                                DatagramPacket dp = new DatagramPacket(resultBytes, resultBytes.length, inComing.getSocketAddress());
                                socket.send(dp);

                                BufferedReader reader = new BufferedReader(new FileReader(file));
                                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                                    String message = line + "\r\n";
                                    byte[] piece = message.getBytes(StandardCharsets.UTF_8);
                                    DatagramPacket sendPacket = new DatagramPacket(piece, piece.length, inComing.getSocketAddress());
                                    socket.send(sendPacket);

                                    while (true) {
                                        try {
                                            byte[] ack = new byte[1];
                                            DatagramPacket receivePacket = new DatagramPacket(ack, ack.length);
                                            socket.receive(receivePacket);
                                            break;
                                        } catch (IOException ex) {
                                            socket.send(sendPacket);
                                        }
                                    }
                                }
                                String endFlag = "isEnd";
                                byte[] endBytes = endFlag.getBytes(StandardCharsets.UTF_8);
                                DatagramPacket endPacket = new DatagramPacket(endBytes, endBytes.length, inComing.getSocketAddress());
                                socket.send(endPacket);
                            }
                        }
                    }
                    if (!find) {
                        String result = "error\r\n";
                        byte[] resultBytes = result.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket dp = new DatagramPacket(resultBytes, resultBytes.length, inComing.getSocketAddress());
                        socket.send(dp);
                    }
                }
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }
}
