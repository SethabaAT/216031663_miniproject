package acsse.csc03a3.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The Controller class manages the interaction between the user interface
 * components and the application logic.
 * 
 * @author Arnold Thabo Sethaba
 */
public class Controller implements Initializable {
    // Attributes
    private ClientConnection clientConnection;
    private boolean isLoggedIn;
    private User user;
    private String partySelected;
    private List<String> votes;

    // FXML Components
    @FXML
    private Button btnValidate, btnVote, btnLogin, btnLogout, btnResults, btnVoteSubmit, btnHome;
    @FXML
    private Label lblStdNo, lblPass, lblHeading1, lblHeading2, lblResults;
    @FXML
    private TextField txtStdNo, txtPass;
    @FXML
    private Pane loginPane, homePane, validatePane, votingPane, resultsPane;
    @FXML
    private RadioButton option1, option2, option3, option4;
    @FXML
    private GridPane validateGrid, resultGrid;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isLoggedIn = false;
        clientConnection = new ClientConnection();
        redirectPage(Page.HOME);
    }

    /**
     * Handles the onClick event for the Home button.
     * 
     * @param event The event triggered by clicking the Home button.
     */
    @FXML
    private void onClickHome(ActionEvent event) {
        redirectPage(Page.HOME);
    }

    /**
     * Handles the onClick event for the Logout button.
     * 
     * @param event The event triggered by clicking the Logout button.
     */
    @FXML
    private void onClickLogout(ActionEvent event) {
        clientConnection.sendCommand("LOGOUT");
        String res = clientConnection.readResponse();
        System.out.println(res);
        String statusCode = res != null ? res.split(" ")[0] : "";

        if ("200".equals(statusCode)) {
            isLoggedIn = false;
            redirectPage(Page.HOME);
            clientConnection.disconnect();
        }
    }

    /**
     * Handles the onClick event for the Login button.
     * 
     * @param event The event triggered by clicking the Login button.
     */
    @FXML
    private void onClickLogin(ActionEvent event) {
        clientConnection.connect("localhost", 8080); // Connect to the server

        // Send login request
        String studentNumber = txtStdNo.getText();
        String password = txtPass.getText();
        String req = "LOGIN " + studentNumber + " " + password;
        clientConnection.sendCommand(req);

        // Read the response
        String res = clientConnection.readResponse();
        System.out.println(res);

        // 200 firstName lastName userType email hasRegisteredToVote hasVoted
        String[] userInfo = res != null ? res.split(" ") : null;
        System.out.println(userInfo);

        // If the status code is 200, get the user information
        String statusCode = userInfo != null ? userInfo[0] : "";
        if ("200".equals(statusCode)) {
            String firstName = userInfo[1];
            String lastName = userInfo[2];
            String userType = userInfo[3];
            String email = userInfo[4];
            boolean hasRegisteredToVote = Boolean.parseBoolean(userInfo[5]);
            boolean hasVoted = Boolean.parseBoolean(userInfo[6]);

            // Get profile information
            user = new User(studentNumber, password, firstName, lastName, userType, email, hasRegisteredToVote,
                    hasVoted);

            isLoggedIn = true;
            redirectPage(Page.HOME);
        } else {
            System.out.println("Invalid login credentials");
        }
    }

    /**
     * Handles the onClick event for the validating the votes.
     * 
     * @param event The event triggered by clicking the Validate button.
     */
    @FXML
    private void onClickValidate(ActionEvent event) {
        getVoteList();
        redirectPage(Page.VALIDATE);
    }

    /**
     * Handles the onClick event for the vote button.
     * 
     * @param event The event triggered by clicking the vote button.
     */
    @FXML
    private void onClickVote(ActionEvent event) {
        redirectPage(Page.VOTING);
    }

    /**
     * Handles the onClick event for the Results button.
     * 
     * @param event The event triggered by clicking the Results button.
     */
    @FXML
    private void onClickResults(ActionEvent event) {
        redirectPage(Page.RESULTS);

        // Send request to get the results
        String req = "RESULTS";
        clientConnection.sendCommand(req);

        // Read the response
        String res = clientConnection.readResponse();
        System.out.println(res);

        // Get the status code
        String[] returned = res.split(" ");

        // If the status code is not 200, return
        if (!"200".equals(returned[0])) {
            return;
        }

        // Get the results
        String akatsukiVotes = returned[1];
        String karaVotes = returned[2];
        String ninjaAllianceVotes = returned[3];
        String marinesVotes = returned[4];
        int totalVotes = Integer.parseInt(akatsukiVotes) + Integer.parseInt(karaVotes)
                + Integer.parseInt(ninjaAllianceVotes) + Integer.parseInt(marinesVotes);

        // Populate the grid
        populateResultGrid(akatsukiVotes, karaVotes, ninjaAllianceVotes, marinesVotes);
        lblResults.setText("Total Votes: " + totalVotes);
    }

    /**
     * Handles the onClick event for the party options.
     * 
     * @param event The event triggered by clicking the party options.
     */
    @FXML
    private void onClickOption(ActionEvent event) {
        if (event.getSource() == option1) {
            selected(true, false, false, false);
            partySelected = "Akatsuki";
        } else if (event.getSource() == option2) {
            selected(false, true, false, false);
            partySelected = "Kara";
        } else if (event.getSource() == option3) {
            selected(false, false, true, false);
            partySelected = "Ninja-Alliance";
        } else if (event.getSource() == option4) {
            selected(false, false, false, true);
            partySelected = "Marines";
        }
    }

    /**
     * Handles the onClick event for the Vote Submit button.
     * 
     * @param event The event triggered by clicking the Vote Submit button.
     */
    @FXML
    private void onClickVoteSubmit(ActionEvent event) {
        if (partySelected != null) {
            System.out.println("Voting for " + partySelected);
            String req = "VOTE " + partySelected + " " + user.getStudentNumber();
            clientConnection.sendCommand(req);
            String res = clientConnection.readResponse();
            System.out.println(res);

            // clear the selected party
            partySelected = null;
            selected(false, false, false, false);
            redirectPage(Page.HOME);
        } else {
            System.out.println("Please select a party to vote for");
        }
    }

    // Helper Functions

    /**
     * Sets the selected party option.
     * 
     * @param selected1 Slection for option 1
     * @param selected2 Selection for option 2
     * @param selected3 Selection for option 3
     * @param selected4 Selection for option 4
     */
    private void selected(boolean selected1, boolean selected2, boolean selected3, boolean selected4) {
        option1.setSelected(selected1);
        option2.setSelected(selected2);
        option3.setSelected(selected3);
        option4.setSelected(selected4);
    }

    /**
     * Gets the list of votes from the server and populates the grid.
     */
    private void getVoteList() {
        String req = "VOTE_LIST";
        clientConnection.sendCommand(req);
        String res = clientConnection.readResponse();
        votes = new ArrayList<>();

        // Get the status code
        String statusCode = res.split(" ")[0];

        // If the status code is not 200, return
        if (!"200".equals(statusCode) || res.equals("200 []")) {
            return;
        }

        // Get the vote string
        String voteString = res.substring(res.indexOf("[") + 1, res.indexOf("]"));
        String[] voteArray = voteString.split(",");

        // Iterate over the split parts
        for (int i = 0; i < voteArray.length; i++) {
            // If vote is from this user, skip it
            if (voteArray[i].contains(user.getStudentNumber())) {
                continue;
            }

            votes.add(voteArray[i].trim());
            validateGrid.setPrefHeight(i * 35 + 70);
        }

        // Populate the grid
        populatevalidateGrid();
    }

    /**
     * Populates the vote grid with the votes from the server.
     */
    private void populatevalidateGrid() {
        // Clear existing content in the grid
        validateGrid.getChildren().clear();

        // Add header labels
        Label candidateLabel = new Label("Candidate");
        Label voterLabel = new Label("Voter");
        Label validateLabel = new Label("Validate");

        validateGrid.add(voterLabel, 0, 0);
        validateGrid.add(candidateLabel, 1, 0);
        validateGrid.add(validateLabel, 2, 0);

        // Add votes
        for (int i = 0; i < votes.size(); i++) {
            String vote = votes.get(i);
            String parts[] = vote.split(" ");

            System.out.println("VOter " + parts[1]);
            System.out.println("Owner  " + user.getStudentNumber());

            // Add candidate and voter labels
            Label candidate = new Label(parts[0]);
            Label voter = new Label(parts[1]);

            validateGrid.add(candidate, 0, i + 1);
            validateGrid.add(voter, 1, i + 1);

            // Add validate button
            Button validateButton = new Button("Validate");
            validateButton.setOnAction(event -> onValidate(vote));

            validateGrid.add(validateButton, 2, i + 1);
        }
    }

    /**
     * Populates the result grid with the election results.
     * 
     * @param opt1 Option 1
     * @param opt2 Option 2
     * @param opt3 Option 3
     * @param opt4 Option 4
     */
    private void populateResultGrid(String opt1, String opt2, String opt3, String opt4) {
        // Clear existing content in the grid
        resultGrid.getChildren().clear();

        // Add header labels
        Label partyLabel = new Label("Party");
        Label votesLabel = new Label("Votes");

        resultGrid.add(partyLabel, 0, 0);
        resultGrid.add(votesLabel, 1, 0);

        // Add votes
        Label party1 = new Label("Akatsuki");
        Label party2 = new Label("Kara");
        Label party3 = new Label("Ninja-Alliance");
        Label party4 = new Label("Marines");

        resultGrid.add(party1, 0, 1);
        resultGrid.add(party2, 0, 2);
        resultGrid.add(party3, 0, 3);
        resultGrid.add(party4, 0, 4);

        Label votes1 = new Label(opt1);
        Label votes2 = new Label(opt2);
        Label votes3 = new Label(opt3);
        Label votes4 = new Label(opt4);

        resultGrid.add(votes1, 1, 1);
        resultGrid.add(votes2, 1, 2);
        resultGrid.add(votes3, 1, 3);
        resultGrid.add(votes4, 1, 4);

    }

    /**
     * Validates the vote and sends a confirmation to the server.
     * 
     * @param vote The vote to validate.
     */
    private void onValidate(String vote) {
        // Validate the vote and send confirmation to the server
        String req = "VALIDATE " + vote;
        clientConnection.sendCommand(req);
        String res = clientConnection.readResponse();
        System.out.println(res);

        // Remove the vote from the list
        System.out.println("Before " + votes);
        votes.remove(vote);
        System.out.println("After " + votes);
        populatevalidateGrid();
    }

    /**
     * Redirects to the specified page and updates the UI accordingly.
     * 
     * @param page The page to redirect to.
     */
    private void redirectPage(Page page) {
        // Mock redirection by means of setting the visibilty of components
        btnLogout.setVisible(isLoggedIn);
        btnHome.setVisible(page != Page.HOME);
        loginPane.setVisible(!isLoggedIn && page == Page.HOME);
        homePane.setVisible(isLoggedIn && page == Page.HOME);
        validatePane.setVisible(page == Page.VALIDATE);
        votingPane.setVisible(page == Page.VOTING);
        resultsPane.setVisible(page == Page.RESULTS);

        // Set the heading based on the page
        switch (page) {
            case HOME:
                lblHeading1.setText("SRC ELECTION");
                lblHeading2.setText("Vote for your student representative");
                break;
            case VALIDATE:
                lblHeading1.setText("VALIDATE VOTE");
                lblHeading2.setText("Please validate the votes below");
                break;
            case VOTING:
                lblHeading1.setText("Vote");
                lblHeading2.setText("Please select your preferred party");
                break;
            case RESULTS:
                lblHeading1.setText("Election Results");
                lblHeading2.setText("Results of the SRC election");
                break;
        }
    }
}

/**
 * The Page enumeration represents the different pages that can be displayed in
 * the application.
 */
enum Page {
    HOME, VALIDATE, VOTING, RESULTS
}
