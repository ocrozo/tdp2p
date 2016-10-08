//-------------//
// Server.java //
//-------------//

package p2p;

import java.net.*;
import java.io.*;

public class Server implements Runnable {

    public static void main(String[] args) throws Exception {

        //Change port corresponding to your team
        int port = 1234;
        ServerSocket srv = new ServerSocket(port);

        //noinspection InfiniteLoopStatement
        while (true) {
            System.out.println("Waiting for connexions...");
            Thread t = new Thread(new Server(srv.accept()));
            t.start();
        }
        // srv.close();
    }

    // Client socket in/out streams
    private BufferedReader clientInput_;
    private OutputStream clientOutput_;

    private Server(Socket client) {
        System.out.print("Received connection from ");
        System.out.println(client.getInetAddress().getHostAddress());
        try {
            clientInput_ = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            clientOutput_ = client.getOutputStream();
        } catch (Exception ex) {
            System.out.println("An error occurred while reading client information");
            System.out.println("ex.getMessage() = " + ex.getMessage());
        }

    }

    public void run() { // Executed upon Thread's start() method call
        try {
            int level = 2;

            // Read "level" information
            // (max depth if further server calls are necessary)
            String line = clientInput_.readLine();
            if (line != null) level = Integer.parseInt(line);

            // Read the name of the requested file
            if ((line = clientInput_.readLine()) != null) {
                System.out.print("Client request for file " + line + "...");
                if (fileInServer(line)) {
                    File f = new File("." + File.separator + line);
                    copyStream(new FileInputStream(f), clientOutput_, true);
                    System.out.println(" transfer done.");
                } else if (level > 0) { // File is not here... maybe on another server ?
                    System.out.println(" file is not here, lookup further...");
                    // Lookup on other known servers (decrement depth)
                    boolean found = lookupFurther(level - 1, line, clientOutput_);
                    System.out.println(found ? "Transfer done." : "File not found.");
                }
            }
            clientInput_.close();
            clientOutput_.close();

        } catch (Exception e) {
            System.out.println("Error while looking for file");
            System.out.println(e.getMessage());
        } // ignore
    }

    /*
    * Lookup the requested file on every known server
    * Server list is in local "servers.list" text file (one IP address per line)
    */
    private static boolean lookupFurther(int level, String fname, OutputStream out)
            throws IOException {

        BufferedReader hosts;
        try {
            hosts = new BufferedReader(new FileReader("servers.lst"));
        } catch (FileNotFoundException e) {
            System.out.println("No servers.lst file, can't lookup further !");
            return false;
        }

        String ip;
        boolean found = false;
        while (!found && (ip = hosts.readLine()) != null) {
            System.out.println("trying server " + ip);
            try {
                Socket s = new Socket(ip, 1234);
                PrintWriter srv = new PrintWriter(s.getOutputStream(), true);
                srv.println(level + "\n" + fname);
                int nbytes = copyStream(s.getInputStream(), out, true);
                s.close();
                found = (nbytes > 0);
            } catch (ConnectException e) {
                System.out.println("Error while connecting to server");
                System.out.println(e.getMessage());
            } // ignore
        }
        hosts.close();
        return found;
    }

    static int copyStream(InputStream in, OutputStream out, boolean close)
            throws IOException {
        int nbytes, total = 0;
        byte[] buf = new byte[1024];
        while ((nbytes = in.read(buf)) > 0) {
            out.write(buf, 0, nbytes);
            total += nbytes;
        }
        if (close) in.close();
        return total;
    }

    /*
     * Verifies if the file should be on the server and creates
     * a new random file with the requested name
    */
    private boolean fileInServer(String fileName) {
        BufferedReader files;
        String filePattern;
        try {
            files = new BufferedReader(new FileReader("files.lst"));
            filePattern = files.readLine();
            files.close();
        } catch (FileNotFoundException e) {
            System.out.println("No files.lst file, can't lookup files in this server !");
            return false;
        } catch (IOException ioe) {
            System.out.println("Error while reading pattern file !");
            return false;
        }

        for (char c : filePattern.toCharArray()) {
            if (fileName.indexOf(c) == 0) {
                try {
                    File file = new File("." + File.separator + fileName);
                    FileWriter writer = new FileWriter(file, false);
                    writer.write(InetAddress.getLocalHost().getHostName());
                    writer.write(System.lineSeparator());
                    writer.write(InetAddress.getLocalHost().getHostAddress());
                    //writer.flush();
                    writer.close();
                    return true;
                } catch (IOException ioe) {
                    System.out.println("Error while creating file " + fileName);
                    return false;
                }
            }
        }
        return false;
    }
}
