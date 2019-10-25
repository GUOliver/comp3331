import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

// lab3 a simple webserver using java

public class WebServer {
    public static void main (String[] args) throws Exception {
        
        if (args.length != 1) {
            System.out.println("Argument requirement: port number");
            return;
        }

        int port = Integer.parseInt(args[0]);
       
        // create a listenSocket listeing on the port
        ServerSocket listenSocket = new ServerSocket(port); 

        // now we have a server which is listening for connection
        System.out.println("Now listening for connection on port " + port);        

        while (true) {
            // (ii) receive a GET HTTP request from this connection. Your server should only process GET request. You may assume that only GET requests will be received.
            // create a connection socket
            Socket connectionSocket = listenSocket.accept();

            
            // You can read the content of request using InputStream opened from the client socket. 
            // It's better to use BufferedReader because browser will send multiple line. 
            // using it to read the request in http server
            InputStreamReader isr =  new InputStreamReader(connectionSocket.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            
            // HTTP response msg goes into outToClient
            DataOutputStream responseToClient = new DataOutputStream(connectionSocket.getOutputStream());

            String line = reader.readLine();
            System.out.println(line);
            // Constructs a string tokenizer for the specified string 
            // pretty handy tool here :)
            StringTokenizer token = new StringTokenizer(line);

            try {   
            
                if (token.nextToken().equals("GET")) {
                    String filename = token.nextToken();
                    if (filename.startsWith("/")) {
                        // (iii) parse the request to determine the specific file being requested.
                        filename = filename.substring(1);

                        // (iv) get the requested file from the server's file system.
                        File file = new File(filename);
                        int fileBytesNumber = (int) file.length();
                        FileInputStream fileInput = new FileInputStream(file);
                        byte[] bytesArray = new byte[fileBytesNumber];
                        
                        // read from fileInput to byte array
                        fileInput.read(bytesArray);
                        responseToClient.writeBytes("HTTP/1.1 200 OK\r\n");

                        // (v) create an HTTP response message consisting of the requested file preceded by header lines.

                        // In responses, a Content-Type header tells the client
                        // what the content type of the returned content actually is
                        if (filename.endsWith(".jpg")) {
                            responseToClient.writeBytes("Content-Type: image/jpeg\r\n");
                        } else if(filename.endsWith(".png")) {
                            responseToClient.writeBytes("Content-Type: image/png\r\n");
                        }

                        responseToClient.writeBytes("Content-Length: " + fileBytesNumber + "\r\n");
                        
                        // AN important mandatory blank line
                        responseToClient.writeBytes("\r\n");
                        
                        // send the response over the TCP connection to the requesting browser.
                        responseToClient.write(bytesArray, 0, fileBytesNumber);
                        
                        // close the socket and the 
                        fileInput.close();
                    }
                }
            
            } catch (FileNotFoundException e) {
                responseToClient.writeBytes("HTTP/1.1 404 File not found\r\n" + "Content-Type: text/html\r\n\r\n");
                responseToClient.writeBytes("\r\n");
                // display the error msg
                responseToClient.writeBytes("<html><h2>404 not found...</h2></html>\r\n");
                connectionSocket.close();
            }
        }
    }
}


