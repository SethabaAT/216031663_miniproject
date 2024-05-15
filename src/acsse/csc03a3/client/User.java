package acsse.csc03a3.client;

public class User {
    private String studentNumber;
    private String password;
    private String firstName;
    private String lastName;
    private String userType;
    private String email;
    private boolean hasRegisteredToVote;
    private boolean hasVoted;

    public User(String studentNumber, String password, String firstName, String lastName, String userType,
            String email, boolean hasRegisteredToVote, boolean hasVoted) {
        this.studentNumber = studentNumber;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.email = email;
    }

    // Getters
    public String getStudentNumber() {
        return studentNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUserType() {
        return userType;
    }

    public String getEmail() {
        return email;
    }

    public boolean hasRegisteredToVote() {
        return hasRegisteredToVote;
    }

    public boolean hasVoted() {
        return hasVoted;
    }

    // Setters
    public void setHasRegisteredToVote(boolean hasRegisteredToVote) {
        this.hasRegisteredToVote = hasRegisteredToVote;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }
}
