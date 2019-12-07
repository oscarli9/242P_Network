import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTransmissionServer {
    private static final Logger logger = Logger.getLogger(FileTransmissionServer.class.getCanonicalName());
    private static final int NUM_THREAD = 20;

    private final File rootDirectory;
    private final int port;

    public FileTransmissionServer(File rootDirectory, int port) throws IOException {
        if(!rootDirectory.isDirectory()) {
            throw new IOException(rootDirectory + " does not exist as a directory");
        }
        this.rootDirectory = rootDirectory;
        this.port = port;
    }

    void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREAD);
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Document Root: " + rootDirectory.getCanonicalPath());

            while (true) {
                try {
                    Socket request = server.accept();
                    Runnable task = new RequestProcessor(rootDirectory, request);
                    pool.submit(task);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "Error accepting connection", ex);
                }
            }
        }
    }

    public static void main(String[] args) {
        File root;
        try {
            root = new File(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("No directory's provided");
            return;
        }

        int port = 60021;

        try {
            FileTransmissionServer server = new FileTransmissionServer(root, port);
            server.start();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Server could not start", ex);
        }
    }
}
