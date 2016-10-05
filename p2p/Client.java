//-------------//
// Client.java //
//-------------//

package p2p;

import java.net.*;
import java.io.*;

/*
* Usage: Client <filename>
* Lookup a file across one or more server(s), and bring it here.
* List of server IP addresses should be in local "server.in" text file
* (one IP per line)
*/
public class Client {

  public static void main(String[] args) throws Exception {

    String ip;
    //Change port corresponding to your team
    int port=1234;
    boolean found = false;

    // Loop on servers list (obtained from local "servers.lst" file)
    BufferedReader hosts = new BufferedReader(new FileReader("servers.lst"));
    while(! found && (ip = hosts.readLine()) != null) {

      Socket s = new Socket(ip, port);
      PrintWriter srv = new PrintWriter(s.getOutputStream(), true);

      // Ask for requested file, max depth (calls to further servers) = 3
      srv.println(2 + "\n" + args[0]);

      // Bring back the file, if any
      File f = new File("." + File.separator + args[0]);
      FileOutputStream out = new FileOutputStream(f);
      found = (Server.copyStream(s.getInputStream(), out, true) > 0);
      out.close();
      if(! found) f.delete();

    }
    hosts.close();
  }
}
