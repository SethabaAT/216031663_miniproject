package acsse.csc03a3.client;

import java.io.*;
import java.net.Socket;

/**
 * The ClientConnection class manages the network connection with the server.
 * 
 * @author Arnold Thabo Sethaba
 */
public class ClientConnection {
    // Attributes
    private Socket clientSocket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private InputStreamReader inputStreamReader;

    /**
     * Establishes a connection with the server.
     * 
     * @param host The hostname of the server.
     * @param port The port number of the server.
     */
    public void connect(String host, int port) {
        try {
            clientSocket = new Socket(host, port);
            OutputStream outputStream = clientSocket.getOutputStream();
            InputStream inputStream = clientSocket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            printWriter = new PrintWriter(outputStream);

            System.out.println("Client connected to server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        try {
            if (clientSocket != null)
                clientSocket.close();
            if (printWriter != null)
                printWriter.close();
            if (bufferedReader != null)
                bufferedReader.close();
            if (inputStreamReader != null)
                inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a command to the server.
     * 
     * @param command The command to be sent.
     */
    public void sendCommand(String command) {
        if (printWriter != null) {
            printWriter.println(command);
            printWriter.flush();
        }
    }

    /**
     * Reads a response from the server.
     * 
     * @return The response received from the server.
     */
    public String readResponse() {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
