/**
 * ProxyThread.java - Thread class for handling a client socket
 *
 * Creator's details:
 *      Benjamin Martin
 *      s2846492
 *      benjamin.martin2@griffithuni.edu.au
 *
 */

import java.net.*;
import java.io.*;

public class ProxyThread implements Runnable {
    private final Socket mClient;

    /**
     * Create the ProxyThread for the clients connection
     */
    public ProxyThread(Socket c) {
        mClient = c;
    }

    /**
     * Processes an accepted client socket connection
     */
    public void run() {
        Socket server = null;
        HttpRequest request = null;
        HttpResponse response = null;
        String cacheStatus = "Valid Entry Found";

        /* Process request. If there are any exceptions, then simply
         * return and end this request. This unfortunately means the
         * mClient will hang for a while, until it timeouts. */

	    /* Read request */
        if ((request = readRequest()) == null) {
            return;
        }

        /* Check proxy cache for a valid response to request */
        /* If a valid response is not found within the proxy cache
         * send the request to the specific server. */
        if ((response = ProxyCache.getCacheResponse(request)) == null) {
            /* Send request to server */
            if ((server = sendRequest(request)) == null) {
                return;
            }

            /* Read response from server */
            response = readResponse(server);

            /* Cache new request and response */
            ProxyCache.addCacheResponse(request, response);

            cacheStatus = "No Entry Found";
        }

        /* Forward servers response to mClient */
        sendResponse(response);

        System.out.println("URL is: " + request.getURL() + "\n" + "Host to contact is: " + request.getHost() + " at port " + request.getPort() + "\n" + "Cache status: " + cacheStatus + "\n");
    }

    /**
     * Reads a HTTP request from the clients socket
     */
    private HttpRequest readRequest() {
        HttpRequest request = null;

        try {
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
            request = new HttpRequest(fromClient);
        } catch (IOException e) {
            System.out.println("Error reading request from client: " + e);
        } catch (Exception e2) {
            if (e2.getMessage() != null && e2.getMessage().equals("!GET")) {
                System.out.println("400 Bad Request: Method not GET");
            } else {
                System.out.println("400 Bad Request: " + e2);
            }
        }

        return request;
    }

    /**
     * Opens a socket to a requested server and sends the request through
     */
    private Socket sendRequest(HttpRequest request) {
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

    /**
     * Attempts to read a response from the given server socket
     */
    private HttpResponse readResponse(Socket server) {
        HttpResponse response = null;

        try {
            DataInputStream fromServer = new DataInputStream(server.getInputStream());
            response = new HttpResponse(fromServer);
            server.close();
        } catch (IOException e) {
            System.out.println("Error reading server response: " + e);
        }

        return response;
    }

    /**
     * Forwards a passed response to the given client socket
     */
    private void sendResponse(HttpResponse response) {
        try {
            DataOutputStream toClient = new DataOutputStream(mClient.getOutputStream());

            /* Write response to mClient. First headers, then body */
            toClient.writeBytes(response.toString());
            toClient.write(response.body);

            mClient.close();
        } catch (IOException e) {
            System.out.println("Error writing response to client: " + e);
        }
    }
}
