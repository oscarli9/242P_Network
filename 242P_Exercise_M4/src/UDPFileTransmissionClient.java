import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class UDPFileTransmissionClient {
    private static final String HOST_NAME = "localhost";
    private static final int PORT = 60018;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            InetAddress server = InetAddress.getByName(HOST_NAME);
            SocketAddress address = new InetSocketAddress(server, PORT);
            socket.connect(address);

            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(arg).append(" ");
            }
            byte[] commandBytes = builder.toString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket outGoing = new DatagramPacket(commandBytes, commandBytes.length, address);
            socket.send(outGoing);

            if (args.length > 0) {
                if (args[0].equals("index")) {
                    byte[] resultBytes = new byte[8192];
                    DatagramPacket dp = new DatagramPacket(resultBytes, resultBytes.length);
                    try {
                        socket.receive(dp);
                        String result = new String(dp.getData(), 0, dp.getLength(), StandardCharsets.UTF_8);
                        System.out.print(result);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (args[0].equals("get")) {
                    byte[] resultBytes = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(resultBytes, resultBytes.length);
                    try {
                        socket.receive(dp);
                        String result = new String(dp.getData(), 0, dp.getLength(), StandardCharsets.UTF_8);
                        System.out.println(result);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    while (true) {
                        try {
                            byte[] piece = new byte[8192];
                            DatagramPacket receivePacket = new DatagramPacket(piece, piece.length);
                            socket.receive(receivePacket);
                            String line = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                            if (line.equals("isEnd")) break;
                            System.out.print(line);

                            socket.send(new DatagramPacket(new byte[1], 1));
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
