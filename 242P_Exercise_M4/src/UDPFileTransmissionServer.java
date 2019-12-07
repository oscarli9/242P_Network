import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPFileTransmissionServer {
    private static final Logger logger = Logger.getLogger(UDPFileTransmissionServer.class.getCanonicalName());

    private final File rootDirectory;
    private final int port;

    private UDPFileTransmissionServer(File rootDirectory, int port) throws IOException {
        if(!rootDirectory.isDirectory()) {
            throw new IOException(rootDirectory + " does not exist as a directory");
        }
        this.rootDirectory = rootDirectory;
        this.port = port;
    }

    private void start() throws IOException {
        logger.info("Accepting connections on port " + port);
        logger.info("Document Root: " + rootDirectory.getCanonicalPath());

        Runnable task = new RequestProcessor(port, rootDirectory);
        Thread t = new Thread(task);
        t.start();
    }

    public static void main(String[] args) {
        File root;
        try {
            root = new File(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("No directory's provided");
            return;
        }

        int port = 60018;

        try {
            UDPFileTransmissionServer server = new UDPFileTransmissionServer(root, port);
            server.start();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Server could not start", ex);
        }
    }
}
