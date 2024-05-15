package acsse.csc03a3.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import acsse.csc03a3.Blockchain;

/**
 * The ElectionServer class creates a server socket and accepts incoming client
 * connections.
 * 
 * @author Arnold Thabo Sethaba
 */
public class ElectionServer {
	// Attributes
	private ServerSocket electionServer;
	private boolean isRunning;
	private Blockchain<String> blockchain;
	private List<String> votes;

	/**
	 * Constructs a new ElectionServer instance that listens on the specified port.
	 * 
	 * @param port The port number on which the server listens for incoming
	 *             connections.
	 */
	public ElectionServer(int port) {
		try {
			electionServer = new ServerSocket(port);
			System.out.println("Server is running on Port: " + port);
			isRunning = true;
			blockchain = new Blockchain<String>();
			votes = new ArrayList<String>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Accepts incoming client connections and creates a new ElectionHandler
	 */
	public void acceptConnections() {
		while (isRunning) {
			System.out.println("Waiting to accept clients... ");
			try {
				Socket clientConnection = electionServer.accept();
				ElectionHandler handler = new ElectionHandler(clientConnection, blockchain, votes);
				Thread clientThread = new Thread(handler);
				clientThread.start();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	/**
	 * The main method is the entry point of the ElectionServer application.
	 * It creates an instance of ElectionServer that listens on port 8080.
	 * 
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args) {
		ElectionServer server = new ElectionServer(8080);
		server.acceptConnections();
	}

}
