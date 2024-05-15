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

    // Main scene UI
    @FXML
    private Button btnRegister, btnVote, btnLogin, btnLogout, btnResults, btnVoteSubmit, btnRegSubmit, btnHome;
    @FXML
    private Label lblStdNo, lblPass, lblHeading1, lblHeading2, lblResults;
    @FXML
    private TextField txtStdNo, txtPass, txtStdNumber, txtName, txtSurname, txtEmail;
    @FXML
    private Pane loginPane, homePane, registerPane, votingPane, resultsPane;
    @FXML
    private RadioButton option1, option2, option3, option4;

    //////////////////////////////////////////////////////////
    @FXML
    private GridPane voteGrid, resultGrid;

    private List<String> votes;

    //////////////////////////////////////////////////////////////////////////////

    /**
     * Initializes the controller.
     * 
     * @param url The location used to resolve relative paths for the root object,
     *            or null if the location is not known.
     * @param rb  The resources used to localize the root object, or null if the
     *            root object was not localized.
     */
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

    // Main Scene Controls

    // Login Button
    @FXML
    private void onClickLogin(ActionEvent event) {
        clientConnection.connect("localhost", 8080);
        String studentNumber = txtStdNo.getText();
        String password = txtPass.getText();
        String req = "LOGIN " + studentNumber + " " + password;
        clientConnection.sendCommand(req);

        String res = clientConnection.readResponse();
        System.out.println(res);

        String[] userInfo = res != null ? res.split(" ") : null;
        System.out.println(userInfo);
        String statusCode = userInfo != null ? userInfo[0] : "";
        System.out.println(statusCode);
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
            System.out.println("Bolena banna !!! Invalid login credentials");

        }
    }

    // Register Button
    @FXML
    private void onClickRegister(ActionEvent event) {
        getVoteList();
        redirectPage(Page.REGISTRATION);
    }

    // Vote Button
    @FXML
    private void onClickVote(ActionEvent event) {
        redirectPage(Page.VOTING);
    }

    // Results Button
    @FXML
    private void onClickResults(ActionEvent event) {
        redirectPage(Page.RESULTS);

        // Get the results
        String req = "RESULTS";
        clientConnection.sendCommand(req);
        String res = clientConnection.readResponse();
        System.out.println(res);

        // Display the results
        // tHE RETURN LOOK LIKE THIS200 1 2 0 0

        // Get the status code
        String[] returned = res.split(" ");

        // If the status code is not 200, return
        if (!"200".equals(returned[0])) {
            return;
        }

        // Get the results
        int akatsukiVotes = Integer.parseInt(returned[1]);
        int karaVotes = Integer.parseInt(returned[2]);
        int ninjaAllianceVotes = Integer.parseInt(returned[3]);
        int marinesVotes = Integer.parseInt(returned[4]);
        int totalVotes = akatsukiVotes + karaVotes + ninjaAllianceVotes + marinesVotes;

        // Populate the grid
        resultGrid.getChildren().clear();
        resultGrid.add(new Label(" Party"), 0, 0);
        resultGrid.add(new Label(" Votes"), 1, 0);
        resultGrid.add(new Label(" Akatsuki"), 0, 1);
        resultGrid.add(new Label(" Kara"), 0, 2);
        resultGrid.add(new Label(" Ninja Alliance"), 0, 3);
        resultGrid.add(new Label(" Marines"), 0, 4);

        resultGrid.add(new Label(" " + akatsukiVotes), 1, 1);
        resultGrid.add(new Label(" " + karaVotes), 1, 2);
        resultGrid.add(new Label(" " + ninjaAllianceVotes), 1, 3);
        resultGrid.add(new Label(" " + marinesVotes), 1, 4);

        lblResults.setText("Total Votes: " + totalVotes);
    }

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

        System.out.println(partySelected);
    }

    private void selected(boolean selected1, boolean selected2, boolean selected3, boolean selected4) {
        option1.setSelected(selected1);
        option2.setSelected(selected2);
        option3.setSelected(selected3);
        option4.setSelected(selected4);
    }

    // Register TO Vote Controls
    @FXML
    private void onClickRegisterSubmit(ActionEvent event) {
        String studentNumber = txtStdNumber.getText();
        String name = txtName.getText();
        String surname = txtSurname.getText();
        String email = txtEmail.getText();

        String req = "REGISTER " + studentNumber + " " + name + " " + surname + " " + email;
        clientConnection.sendCommand(req);
        redirectPage(Page.HOME);
    }

    // Vote Controls
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

    // Validation Functions
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
            voteGrid.setPrefHeight(i * 35 + 70);
        }

        // Populate the grid
        populateGrid();
    }

    private void populateGrid() {
        // Clear existing content in the grid
        voteGrid.getChildren().clear();

        // Add header labels
        Label candidateLabel = new Label("Candidate");
        Label voterLabel = new Label("Voter");
        Label validateLabel = new Label("Validate");

        voteGrid.add(voterLabel, 0, 0);
        voteGrid.add(candidateLabel, 1, 0);
        voteGrid.add(validateLabel, 2, 0);

        // Add votes
        for (int i = 0; i < votes.size(); i++) {
            String vote = votes.get(i);
            String parts[] = vote.split(" ");

            System.out.println("VOter " + parts[1]);
            System.out.println("Owner  " + user.getStudentNumber());

            // Add candidate and voter labels
            Label candidate = new Label(parts[0]);
            Label voter = new Label(parts[1]);

            voteGrid.add(candidate, 0, i + 1);
            voteGrid.add(voter, 1, i + 1);

            // Add validate button
            Button validateButton = new Button("Validate");
            validateButton.setOnAction(event -> onValidate(vote));

            voteGrid.add(validateButton, 2, i + 1);
        }
    }

    private void onValidate(String vote) {
        // Validate the vote and send confirmation to the server
        String req = "REGISTER " + vote;
        clientConnection.sendCommand(req);
        String res = clientConnection.readResponse();
        System.out.println(res);

        // Remove the vote from the list
        votes.remove(vote);
        populateGrid();
    }

    // Utility functions

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
        registerPane.setVisible(page == Page.REGISTRATION);
        votingPane.setVisible(page == Page.VOTING);
        resultsPane.setVisible(page == Page.RESULTS);

        // Set the heading based on the page
        switch (page) {
            case HOME:
                lblHeading1.setText("SRC ELECTION");
                lblHeading2.setText("Vote for your student representative");
                break;
            case REGISTRATION:
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
    HOME, REGISTRATION, VOTING, RESULTS
}
