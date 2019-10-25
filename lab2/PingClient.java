import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.Timestamp;

/*
 * Server to process ping requests over UDP. 
 * The server sits in an infinite loop listening for incoming UDP packets. 
 * When a packet comes in, the server simply sends the encapsulated data back to the client.
 */

public class PingClient
{
   public static void main(String[] args) throws Exception
   {
      // Get command line argument.
    if (args.length != 2) {
         System.out.println("Required arguments: host and port");
         return;
      }

      // port is the port number it is listening to.
      int port = Integer.parseInt(args[1]);

      // host is the IP ad of the computer the server is running on
      InetAddress host = InetAddress.getByName(args[0]);

      // Create a datagram socket for sending UDP packets
      // through the port specified on the command line.
      DatagramSocket client_socket = new DatagramSocket();

      
      long min_delay = 10000;
      long max_delay = 0;
      long sum = 0;
      // send 10 ping request to the server
      for (int request_sent = 0; request_sent < 10; request_sent++) {
        
        // using public long getTime method of Date class
        Date now = new Date();
        long current_time = now.getTime();
      
        // get the timestamp for the current time
        Timestamp stamp = new Timestamp(current_time);

        // transfer the msg being sent to byte arraxy
        byte[] buffer = new byte[4096];
        String to_send = "PING " + request_sent + " " + stamp + "\r\n";
        buffer = to_send.getBytes();

        // create the data packet being sent (to the destination constructed by IP ad and port number)
        DatagramPacket packet_to_send = new DatagramPacket(buffer, buffer.length, host, port);

        // send the packet
        client_socket.send(packet_to_send);

        // using try catch block, if timeout, print error msg
        try {
            // using function setting the timeout
            client_socket.setSoTimeout(1000);

            // set up a UDP packet for receiving the response from the server
            byte[] buffer2 = new byte[4096];
            DatagramPacket packet_to_receive_reponse = new DatagramPacket(buffer2, 4096);
            client_socket.receive(packet_to_receive_reponse);

            now = new Date();
            long rrt = now.getTime() - current_time;

            // calculation ==============================
            if (rrt < min_delay) {
               min_delay = rrt;
            } else {
               max_delay = rrt;
            }

            sum += rrt;

            printData(packet_to_receive_reponse, rrt);

        } catch (IOException e) {
            System.out.println("Time out, packet " + request_sent);
        }
        
        System.out.printf("\n");
      }

      // print the msg of calculation
      System.out.printf("\nAverage rrt: %d, Min-delay: %d, Max-delay: %d\n", sum/10, min_delay, max_delay);
   }

   /* 
    * Print ping data to the standard output stream.
    */
   private static void printData(DatagramPacket request, long time_delay) throws Exception
   {
      // Obtain references to the packet's array of bytes.
      byte[] buf = request.getData();

      // Wrap the bytes in a byte array input stream,
      // so that you can read the data as a stream of bytes.
      ByteArrayInputStream bais = new ByteArrayInputStream(buf);

      // Wrap the byte array output stream in an input stream reader,
      // so you can read the data as a stream of characters.
      InputStreamReader isr = new InputStreamReader(bais);

      // Wrap the input stream reader in a bufferred reader,
      // so you can read the character data a line at a time.
      // (A line is a sequence of chars terminated by any combination of \r and \n.) 
      BufferedReader br = new BufferedReader(isr);

      // The message data is contained in a single line, so read this line.
      String line = br.readLine();

      // Print host address and data received from it.
      System.out.println(
         "Received from " + 
         request.getAddress().getHostAddress() + 
         ": " +
         new String(line) + " RTT: " + time_delay + " ms");
   }
}