/**
 * ProxyCache.java - Simple caching proxy
 *
 * $Id: ProxyCache.java,v 1.3 2004/02/16 15:22:00 kangasha Exp $
 *
 * ProxyCache was modified for completion with enhancements as
 * an assignment for the course Computer Networks and Security
 *
 * Modifier's details:
 *      Benjamin Martin
 *      s2846492
 *      benjamin.martin2@griffithuni.edu.au
 *
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class ProxyCache {
    /**
     * Port for the proxy
     */
    private static int port;

    /**
     * Socket for client connections
     */
    private static ServerSocket socket;

    /**
     * Cache for the proxy
     */
    private static HashMap cache;

    /**
     * Create the ProxyCache object, socket, and the cache
     */
    public static void init(int p) {
        port = p;
        try {
            socket = new ServerSocket(port); /* Fill in */
            cache = new HashMap();
        } catch (IOException e) {
            System.out.println("Error creating socket: " + e);
            System.exit(-1);
        }
    }

    public static void handle(Socket client) {
        Socket server = null;
        HttpRequest request = null;
        HttpResponse response = null;

        /* Process request. If there are any exceptions, then simply
         * return and end this request. This unfortunately means the
         * client will hang for a while, until it timeouts. */

	    /* Read request */
        request = readRequest(client);
        if (request == null)
            return;

	    /* Send request to server */
        server = sendRequest(request);
        if (request == null)
            return;

	    /* Read response from server */
        response = readResponse(server);

        /* Forward servers response to client */
        sendResponse(response, client);
    }

    private static HttpRequest readRequest(Socket client) {
        HttpRequest request = null;

        try {
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream())); /* Fill in */
            request = new HttpRequest(fromClient); /* Fill in */
        } catch (IOException e) {
            System.out.println("Error reading request from client: " + e);
        }

        return request;
    }

    private static Socket sendRequest(HttpRequest request) {
        Socket server = null;

        try {
	        /* Open socket and write request to socket */
            server = new Socket(request.getHost(), request.getPort()); /* Fill in */
            DataOutputStream toServer = new DataOutputStream(server.getOutputStream()); /* Fill in */
            toServer.writeBytes(request.toString()); /* Fill in */
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + request.getHost());
            System.out.println(e);
        } catch (IOException e) {
            System.out.println("Error writing request to server: " + e);
        }

        return server;
    }

    private static HttpResponse readResponse(Socket server) {
        HttpResponse response = null;

        try {
            DataInputStream fromServer = new DataInputStream(server.getInputStream()); /* Fill in */
            response = new HttpResponse(fromServer);/* Fill in */
            server.close();
        } catch (IOException e) {
            System.out.println("Error writing response to client: " + e);
        }

        return response;
    }

    private static void sendResponse(HttpResponse response, Socket client) {
        try {
            DataOutputStream toClient = new DataOutputStream(client.getOutputStream()); /* Fill in */

            /* Write response to client. First headers, then body */
            toClient.writeBytes(response.toString()); /* Fill in */
            toClient.write(response.body); /* Fill in */

            client.close();
        } catch (IOException e) {
            System.out.println("Error writing response to client: " + e);
        }
    }

/* -------------------------------------------------- */


    /**
     * Read command line arguments and start proxy
     */
    public static void main(String args[]) {
        int myPort = 0;

        try {
            myPort = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Need port number as argument");
            System.exit(-1);
        } catch (NumberFormatException e) {
            System.out.println("Please give port number as integer.");
            System.exit(-1);
        }

        init(myPort);

        /** Main loop. Listen for incoming connections and spawn a new
         * thread for handling them */
        Socket client = null;

        while (true) {
            try {
                client = socket.accept(); /* Fill in */
                handle(client);
            } catch (IOException e) {
                System.out.println("Error reading request from client: " + e);
		        /* Definitely cannot continue processing this request,
		         * so skip to next iteration of while loop. */
                continue;
            }
        }

    }
}