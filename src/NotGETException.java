/**
 * NotGETException.java - Basic exception class for requests not
 * of the GET type.
 *
 * Creator's details:
 *      Benjamin Martin
 *      s2846492
 *      benjamin.martin2@griffithuni.edu.au
 *
 */

public class NotGETException extends Exception {
    NotGETException(String msg) {
        super(msg);
    }
}