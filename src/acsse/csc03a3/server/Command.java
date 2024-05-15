package acsse.csc03a3.server;

/**
 * The Command enumeration represents the commands that can be sent from the
 * client to the server.
 * 
 * @author Arnold Thabo Sethaba
 */
public enum Command {
    LOGIN,
    VOTE,
    RESULTS,
    VALIDATE,
    LOGOUT,
    VOTE_LIST,
    INVALID;

    /**
     * Converts a string to its corresponding Command enum value.
     * 
     * @param str The string representation of the command.
     * @return The Command enum value corresponding to the string, or INVALID if the
     *         string does not match any command.
     */
    public static Command fromString(String str) {
        try {
            return valueOf(str);
        } catch (IllegalArgumentException e) {
            return INVALID;
        }
    }
}
