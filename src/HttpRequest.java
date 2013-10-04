/**
 * HttpRequest - HTTP request container and parser
 *
 * $Id: HttpRequest.java,v 1.2 2003/11/26 18:11:53 kangasha Exp $
 *
 * HttpRequest was modified for completion with enhancements as
 * an assignment for the course Computer Networks and Security
 *
 * Modifier's details:
 *      Benjamin Martin
 *      s2846492
 *      benjamin.martin2@griffithuni.edu.au
 *
 */

import java.io.*;

public class HttpRequest {
    /**
     * Helper variables
     */
    final static String CRLF = "\r\n";
    final static int HTTP_PORT = 80;

    /**
     * Store the request parameters
     */
    String method;
    String URL;
    String version;
    String headers = "";

    /**
     * Server and port
     */
    private String host;
    private int port;

    /**
     * Creates a HttpRequest object by reading the required information
     * from a BufferedReader object.
     *
     * It returns nothing.
     * It takes a BufferedReader.
     */
    public HttpRequest(BufferedReader from) throws Exception {
        /* First line of the passed BufferedReader is read to
         * obtain the requests method, URL, and version. */
        String firstLine = "";
        try {
            firstLine = from.readLine();
        } catch (IOException e) {
            System.out.println("Error occurred when reading first request line: " + e);
            return;
        }

        String[] tmp = firstLine.split(" ");
        method = tmp[0];
        URL = tmp[1];
        version = tmp[2];

        /* Only GET request methods have been implemented */
        if (!method.equals("GET")) {
            throw new NotGETException(firstLine);
        }

        /* The remaining lines of the request header must be read
         * from the passed BufferedReader object */
        try {
            String line = from.readLine();
            while (line.length() != 0) {
                headers += line + CRLF;
                /* We need to find host header to know which server to
		         * contact in case the request URL is not complete. */
                if (line.startsWith("Host:")) {
                    tmp = line.split(" ");
                    if (tmp[1].indexOf(':') > 0) {
                        String[] tmp2 = tmp[1].split(":");
                        host = tmp2[0];
                        port = Integer.parseInt(tmp2[1]);
                    } else {
                        host = tmp[1];
                        port = HTTP_PORT;
                    }
                }
                line = from.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error occurred when reading request headers: " + e);
            return;
        }
    }

    /**
     * Allows external sources read the requests intended host
     *
     * It returns a String.
     * It takes nothing.
     */
    public String getHost() {
        return host;
    }

    /**
     * Allows external sources read the requests servers port
     *
     * It returns a int.
     * It takes nothing.
     */
    public int getPort() {
        return port;
    }

    /**
     * Allows external sources read the specified URL
     *
     * It returns a String.
     * It takes nothing.
     */
    public String getURL() {
        return URL;
    }

    /**
     * Convert request into a string for easy re-sending.
     *
     * It returns a String.
     * It takes nothing.
     */
    public String toString() {
        String req = "";

        req = method + " " + URL + " " + version + CRLF;
        req += headers;
	    /* This proxy does not support persistent connections */
        req += "Connection: close" + CRLF;
        req += CRLF;

        return req;
    }

    /**
     * Convert request into a string for easy re-sending along with
     * the addition of extra header lines.
     *
     * It returns a String.
     * It takes a String containing an eTag.
     */
    public String toConditionalRequest(String eTag, String lastMod) {
        String req = "";

        req = method + " " + URL + " " + version + CRLF;
        req += headers;

        /* The cached response shows how the conditional request should
         * validate the represented file. If an ETag is supplied it is to
         * be validated using an ETag. If a Last-Modified value is
         * supplied validated using that. If both values exist then
         * both values should be used for validating. */
        if (eTag != "") {
            req += "If-None-Match: " + eTag + CRLF;
        }
        if (lastMod != "") {
            req += "If-Modified-Since: " + lastMod + CRLF;
        }

        req += CRLF;

        return req;
    }
}