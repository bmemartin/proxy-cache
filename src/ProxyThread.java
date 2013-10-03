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
    private final static String CRLF = "\r\n";

    private Socket mClient;

    /**
     * Create the ProxyThread for the clients connection
     *
     * It returns nothing.
     * It takes a Socket
     */
    public ProxyThread(Socket c) {
        mClient = c;
    }

    /**
     * ********************************************
     *
     * It returns nothing.
     * It takes nothing.
     */
    public void run() {
        Socket server = null;
        HttpRequest request = null, conditionRequest = null;
        HttpResponse response = null, conditionResponse = null;
        String cacheStatus = "";

	    /* Read the request. If a request is not returned then exit this request. */
        if ((request = readRequest()) == null) {
            return;
        }

        /* Check proxy cache for a valid response to request.
         * If a valid response is not found within the proxy cache
         * send the request to the specific server. */
        if ((response = ProxyCache.getCacheResponse(request)) != null) {
            /* Create a condition request for validation that the cached
             * response is still valid (up to date). */
            try {
                String reqStr = request.toConditionalRequest(response.getETag(), response.getModified());
                conditionRequest = new HttpRequest(new BufferedReader(new StringReader(reqStr)));
            } catch (Exception e) {
                System.out.println("400 Bad Request: " + e);
            }

            /* Send the condition request to the server.
             * If a server socket is not returned then exit this request. */
            if ((server = sendRequest(conditionRequest)) == null) {
                return;
            }

            /* Read the condition response from server. */
            conditionResponse = readResponse(server);

            /* If the condition response shows that the file has not been
             * modified then use the response from cache. If the file has
             * been modified use the condition response and update the
             * cache. For all other statuses exit the current request. */
            if (conditionResponse.status == 304) {
                cacheStatus = "Up-to-date entry found.";
                sendResponse(response);
            } else if (conditionResponse.status == 200) {
                cacheStatus = "Outdated entry found, updating...";
                ProxyCache.addCacheResponse(request, conditionResponse);
                sendResponse(conditionResponse);
            } else {
                System.out.println(conditionResponse.statusLine);
                return;
            }
        } else {
            /* Send the condition request to the server.
             * If a server socket is not returned then exit this request. */
            if ((server = sendRequest(request)) == null) {
                return;
            }

            /* Read the response from server. */
            response = readResponse(server);

            cacheStatus = "No entry found, saving...";
            ProxyCache.addCacheResponse(request, response);
            /* Forward servers response to mClient */
            sendResponse(response);
        }

        System.out.println("URL is: " + request.getURL() + "\n" + "Host to contact is: " + request.getHost() + " at port " + request.getPort() + "\n" + "Cache status: " + cacheStatus + "\n");
    }

    /**
     * Reads a HTTP request from the clients socket
     *
     * It returns a HttpRequest.
     * It takes nothing.
     */
    private HttpRequest readRequest() {
        HttpRequest request = null;

        try {
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
            request = new HttpRequest(fromClient);
        } catch (NotGETException notGET) {
            System.out.println("501 Not Implemented: Method not GET");
            System.out.println(notGET.getMessage() + "\n");
        } catch (Exception e) {
            System.out.println("400 Bad Request: " + e);
        }

        return request;
    }

    /**
     * Opens a socket to a requested server and sends the request through
     *
     * It returns a Socket.
     * It takes a HttpRequest.
     */
    private Socket sendRequest(HttpRequest request) {
        Socket server = null;

        try {
	        /* Open socket and write request to socket */
            server = new Socket(request.getHost(), request.getPort());
            DataOutputStream toServer = new DataOutputStream(server.getOutputStream());
            toServer.writeBytes(request.toString());
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + request.getHost());
            System.out.println(e);
        } catch (IOException e) {
            System.out.println("500 Internal Server Error: " + e);
        }

        return server;
    }

    /**
     * Attempts to read a response from the given server socket
     *
     * It returns a HttpResponse.
     * It takes a Socket.
     */
    private HttpResponse readResponse(Socket server) {
        HttpResponse response = null;

        try {
            DataInputStream fromServer = new DataInputStream(server.getInputStream());
            response = new HttpResponse(fromServer);
            server.close();
        } catch (IOException e) {
            System.out.println("500 Internal Server Error: " + e);
        }

        return response;
    }

    /**
     * Forwards a passed response to the given client socket
     *
     * It returns nothing.
     * It takes a HttpResponse.
     */
    private void sendResponse(HttpResponse response) {
        try {
            DataOutputStream toClient = new DataOutputStream(mClient.getOutputStream());

            /* Write response to mClient. First headers, then body */
            toClient.writeBytes(response.toString());
            toClient.write(response.body);

            mClient.close();
        } catch (IOException e) {
            System.out.println("500 Internal Server Error: " + e);
        }
    }
}
