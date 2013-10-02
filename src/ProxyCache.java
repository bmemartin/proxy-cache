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
    private static HashMap<HttpRequest, HttpResponse> cache;

    /**
     * Create the ProxyCache object, socket, and the cache
     */
    public static void init(int p) {
        port = p;
        try {
            socket = new ServerSocket(port);
            cache = new HashMap<HttpRequest, HttpResponse>();
        } catch (IOException e) {
            System.out.println("Error creating socket: " + e);
            System.exit(-1);
        }
    }

    /**
     * Searches through the stored cache for valid response to given request
     */
    public static HttpResponse getCacheResponse(HttpRequest request) {
        HttpResponse response = null;

        Iterator iter = cache.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry cacheEntry = (Map.Entry)iter.next();
            HttpRequest cacheRequest = (HttpRequest) cacheEntry.getKey();
            if (request.getURL().equals(cacheRequest.getURL())) {
                response = (HttpResponse) cacheEntry.getValue();
            }
        }

        return response;
    }

    /**
     * Adds an entry to the cache using the request as a key to the response
     */
    public static void addCacheResponse(HttpRequest request, HttpResponse response) {
        cache.put(request, response);
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
                client = socket.accept();
                (new Thread(new ProxyThread(client))).start();
            } catch (IOException e) {
                System.out.println("Error accepting or handling client socket: " + e);
		        /* Definitely cannot continue processing this request,
		         * so skip to next iteration of while loop. */
                continue;
            }
        }

    }
}