package acsse.csc03a3.server;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import acsse.csc03a3.Blockchain;
import acsse.csc03a3.Transaction;

/**
 * The ElectionHandler class is responsible for handling communication with a
 * client and processing commands on the server side.
 * 
 * @author Arnold Thabo Sethaba
 */
public class ElectionHandler implements Runnable {
	// Attributes
	private Socket clientConnection;
	private Blockchain<String> blockchain;
	private boolean processing;
	private boolean isLoggedIn;
	private List<String> votes;

	// Streams
	private PrintWriter printWriter;
	private BufferedReader bufferedReader;
	// private DataOutputStream dataOutputStream;
	private DataInputStream dataInputStream;
	private OutputStream outputStream;
	private InputStream inputStream;

	/**
	 * Constructs a new ElectionHandler instance for the given client socket
	 * connection.
	 * 
	 * @param clientConnection The socket connection to the client.
	 */
	public ElectionHandler(Socket clientConnection, Blockchain<String> blockchain, List<String> votes) {
		this.clientConnection = clientConnection;
		this.blockchain = blockchain;
		this.votes = votes;

		// Instantiate input and output stream
		try {
			outputStream = clientConnection.getOutputStream();
			inputStream = clientConnection.getInputStream();
			// dataOutputStream = new DataOutputStream(outputStream);
			dataInputStream = new DataInputStream(inputStream);
			printWriter = new PrintWriter(outputStream);
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Executes the command processing logic.
	 */
	@Override
	public void run() {
		System.out.println("Initiate Commands Processing... ");
		processing = true;

		try {
			while (processing) {
				// Get Request from a client
				String req = bufferedReader.readLine();
				System.out.println("Request body from client: " + req);

				// The first part in a request is command
				StringTokenizer st = new StringTokenizer(req);
				Command command = Command.fromString(st.nextToken());

				switch (command) {
					case LOGIN:
						String studentNumber = st.nextToken();
						String password = st.nextToken();

						String user = authenticateUser(studentNumber, password);

						if (user != null) {
							// Send a status code and message
							sendMessage("200 " + user);
							isLoggedIn = true;
							System.out.println("Logged in");
						} else {
							sendMessage("500 Invalid Credentials");
							isLoggedIn = false;
							System.out.println("Invalid Credentials");
						}
						break;
					case VOTE:
						if (isLoggedIn) {
							String candidate = st.nextToken();
							String voter = st.nextToken();

							// Add to votes list
							System.out.println("Before " + votes);
							votes.add(voter + " " + candidate);
							System.out.println("After " + votes);

							sendMessage("200 Vote cast successfully");
							System.out.println("Vote cast successfully");
						} else {
							sendMessage("500 Not authenticated");
							System.out.println("Not authenticated");
						}
						break;
					case REGISTER:
						if (isLoggedIn) {
							String voter = st.nextToken();
							String candidate = st.nextToken();

							// Create a new transaction
							Transaction<String> transaction = new Transaction<>(voter, "Election", candidate);

							// Add transaction to a new block and mine it
							List<Transaction<String>> transactions = new ArrayList<>();
							transactions.add(transaction);

							// print blockchain

							blockchain.registerStake("defaultNodeAddress", 100);

							// Add block to blockchain

							blockchain.addBlock(transactions);

							// Remove vote from votes list
							votes.remove(candidate + " " + voter);

							sendMessage("200 Vote Validated");
						} else {
							sendMessage("500 Not authenticated");
						}
						break;
					case RESULTS:
						if (isLoggedIn) {
							String results = blockchain.toString();

							// Count occurrences for each candidate
							int karaCount = countSenderOccurrences(results, "Kara");
							int akatsukiCount = countSenderOccurrences(results, "Akatsuki");
							int ninjaAllianceCount = countSenderOccurrences(results, "Ninja-Alliance");
							int marinesCount = countSenderOccurrences(results, "Marines");

							System.out.println(results);
							// Send results
							sendMessage("200 " + akatsukiCount + " " + karaCount + " "
									+ ninjaAllianceCount + " " + marinesCount);

						} else {
							sendMessage("500 Not authenticated");
						}
						break;
					case LOGOUT:
						if (isLoggedIn) {
							isLoggedIn = false;
							sendMessage("200 Logged out");
							dataInputStream.close();
							dataInputStream.close();

							bufferedReader.close();
							printWriter.close();
							clientConnection.close();
							processing = false;
						} else {
							sendMessage("500 Not authenticated");
						}
						break;
					case VOTE_LIST:
						if (isLoggedIn) {
							sendMessage("200 " + votes.toString());
						} else {
							sendMessage("500 Not authenticated");
						}
						break;
					case INVALID:
						// Handle invalid command
						// invalid command
						break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Finally");
		}
	}

	/**
	 * Sends a message to the client.
	 * 
	 * @param message The message to be sent.
	 */
	private void sendMessage(String message) {
		printWriter.println(message);
		printWriter.flush();
	}

	/**
	 * Authenticates a user by verifying the provided student number and password
	 * against the database.
	 * 
	 * @param studentNumber The student number of the user.
	 * @param password      The password of the user.
	 * @return true if the user is authenticated, false otherwise.
	 */
	private static String authenticateUser(String studentNumber, String password) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String user = null;

		try {
			// Establish database connection
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/university_db", "root",
					"ITS_Pass970927");

			// Prepare SQL query
			String query = "SELECT * FROM users WHERE student_number = ? AND password = ?";
			statement = connection.prepareStatement(query);
			statement.setString(1, studentNumber);
			statement.setString(2, password);

			// Execute query
			resultSet = statement.executeQuery();

			// Check if user exists
			if (resultSet.next()) {
				user = resultSet.getString("firstname") + " "
						+ resultSet.getString("lastname") + " " + resultSet.getString("usertype") + " "
						+ resultSet.getString("email") + " " + resultSet.getBoolean("has_registered") + " "
						+ resultSet.getBoolean("has_voted");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			// Close resources
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return user;
	}

	// Method to count occurrences of a specific sender in the blockchain string
	public static int countSenderOccurrences(String blockchainString, String sender) {
		// Define regex pattern to match the specified data (candidate) field
		String patternString = sender;
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(blockchainString);

		// Count matches
		int count = 0;
		while (matcher.find()) {
			count++;
		}

		return count;
	}

}
