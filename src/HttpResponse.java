/**
 * HttpResponse - Handle HTTP replies
 *
 * $Id: HttpResponse.java,v 1.2 2003/11/26 18:12:42 kangasha Exp $
 *
 * HttpResponse was modified for completion with enhancements as
 * an assignment for the course Computer Networks and Security
 *
 * Modifier's details:
 *      Benjamin Martin
 *      s2846492
 *      benjamin.martin2@griffithuni.edu.au
 *
 */

import java.io.*;

public class HttpResponse {
    final static String CRLF = "\r\n";

    /**
     * How big is the buffer used for reading the object
     */
    final static int BUF_SIZE = 8192;

    /**
     * Maximum size of objects that this proxy can handle. For the
     * moment set to 100 KB. You can adjust this as needed.
     */
    final static int MAX_OBJECT_SIZE = 100000;

    /**
     * Reply status and headers
     */
    int status;
    String version;
    String statusLine = "";
    String eTag = "";
    String modified = "";
    String headers = "";
    /* Body of reply */
    byte[] body = new byte[MAX_OBJECT_SIZE];

    /**
     * Creates a HttpResponse object by reading all information contained
     * within the passed DataInputStream
     *
     * It returns nothing.
     * It takes DataInputStream.
     */
    public HttpResponse(DataInputStream fromServer) {
        /* Length of the object */
        int length = -1;
        boolean gotStatusLine = false;

	    /* Response headers must be read in and stored */
        try {
            String line = fromServer.readLine();
            while (line.length() != 0) {
                /* Store the first read line as the status line of the response
                 * and split the line into its protocol version and status code */
                if (!gotStatusLine) {
                    statusLine = line;
                    gotStatusLine = true;

                    String[] tmp = line.split(" ");
                    version = tmp[0];
                    status = Integer.parseInt(tmp[1]);
                } else {
                    headers += line + CRLF;
                }

                /* If the current line being read contains the content length
                 * save the value as a response data member */
                if (line.startsWith("Content-Length:") || line.startsWith("Content-length:")) {
                    String[] tmp = line.split(" ");
                    length = Integer.parseInt(tmp[1]);
                }

                /* If the current line being read contains an ETag
                 * save the value as a response data member */
                if (line.startsWith("ETag:")) {
                    String[] tmp = line.split(" ");
                    eTag = tmp[1];
                }

                /* If the current line being read contains the last time it was modified
                 * save the value as a response data member */
                if (line.startsWith("Last-Modified:") && line.length() > 15) {
                    modified = line.substring(15);
                }

                line = fromServer.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error reading headers from server: " + e);
            return;
        }

        /* All data stored within the body of the response is read and stored */
        try {
            int bytesRead = 0;
            byte buf[] = new byte[BUF_SIZE];
            boolean loop = false;

	        /* If we didn't get Content-Length header, just loop until
	         * the connection is closed. */
            if (length == -1) {
                loop = true;
            }

	        /* Read the body in chunks of BUF_SIZE and copy the chunk
	         * into body. Usually replies come back in smaller chunks
	         * than BUF_SIZE. The while-loop ends when either we have
	         * read Content-Length bytes or when the connection is
	         * closed (when there is no Connection-Length in the
	         * response. */
            while (bytesRead < length || loop) {
		        /* Read it in as binary data */
                int res = fromServer.read(buf, 0, BUF_SIZE);
                if (res == -1) {
                    break;
                }
		        /* Copy the bytes into body. Make sure we don't exceed
		         * the maximum object size. */
                for (int i = 0; i < res && (i + bytesRead) < MAX_OBJECT_SIZE; i++) {
		            body[bytesRead + i] = buf[i];
                }
                bytesRead += res;
            }
        } catch (IOException e) {
            System.out.println("Error reading response body: " + e);
            return;
        }

    }

    /**
     * Allows external sources read the responses ETag
     *
     * It returns a String.
     * It takes nothing.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Allows external sources read the last time the file, that the response
     * represents, was modified
     *
     * It returns a String.
     * It takes nothing.
     */
    public String getModified() {
        return modified;
    }

    /**
     * Convert response into a string for easy re-sending. Only
     * converts the response headers, body is not converted to a
     * string.
     *
     * It returns a String.
     * It takes nothing.
     */
    public String toString() {
        String res = "";

        res = statusLine + CRLF;
        res += headers;
        res += CRLF;

        return res;
    }
}