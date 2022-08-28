package ca.camosun.ICS226;

import java.net.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

/*
* Server class - Controller for Java Socket server instance.
*/
public class Server
{
  boolean DEBUG = true; // Enable and disable debugging prints on server
  final int KEY_SIZE = 8; // constant for key size in messages
  final int MAX_INCOMMING_MESSAGE = 171; // max message size allowed, adjusted to meet to include key, command, and next key

  final String PUT_CMD = "PUT"; // Put command
  final String GET_CMD = "GET"; // Get command

  private ServerSocket sock; // class socket instantiated in constructor.
  private static HashMap<String, String> messages = new HashMap<>(); // Message list of key, value pairs.

  /**
  * Server(int port) - constructor for Server class
  * @param port - int port value that the socket will listen to for connections
  * initiates ServerSocket with @param port
  */
  public Server(int port)
  {
    try
    {
      this.sock = new ServerSocket(port);
    } 
    catch (IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      if(this.sock == null)
      {
        System.err.println("Error, No socket instantiated!");
        System.exit(-1);
      }
    }
  }

  /**
   * getConnections() - called typically in while loop to get multiple connections
   * This function calls a new thread to manage messages from the client.
   */
  public void getConnections()
  {
    try
    {
      Socket client = sock.accept();
      process_message pm = new process_message(client);
      pm.start();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /* Class of Server extending Thread to avoid blocking functions with multi-client */
  class process_message extends Thread
  {
    Socket client; // class instance of accepted client connection.

    /**
     * process_message(Socket client) - class constructor
     * @param client - a Socket passed from getConnections()
     */
    public process_message(Socket client)
    {
      this.client = client;
    }
    /**
     * @Override run() - Thread class method, executes code in function when start() is called.
     * Reads incomming message from client, tests for valid format and runs respective method
     * to handle the message and sends back a response and closes connection.
     */
    @Override
    public void run() {
      try(
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);                   
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
      )
      {
        String s = in.readLine();

        if(s == null || s.length() > MAX_INCOMMING_MESSAGE) // null received or message over length limit
        {
          out.println("NO");
          client.close();
          return;
        }

        if(DEBUG) System.out.println(s);

        String reply = "\n"; // GET: this is the message, PUT: this is the success code

        if(s.startsWith(PUT_CMD))
        {
          reply = put_message(s);
        }
        else if(s.startsWith(GET_CMD))
        {
          reply = get_message(s);
        }
        else
        {
          reply = "NO";
        }

        out.println(reply);
        out.flush();
        client.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    /**
     * put_message(String text) - Put command processing
     * @param text - message received with PUT message head.
     * tests message if it has PUT -> 8 - alphanumeric characters -> more then 1 of any character | not case-sensitive.
     * @return OK or NO message
     */
    String put_message(String text)
    {
      Pattern rPattern = Pattern.compile("(?:PUT)([\\dA-Z]{8})(.*)$", Pattern.CASE_INSENSITIVE);
      Matcher m = rPattern.matcher(text);
      String key = "";
      String message = "";

      if(m.find())
      {
        key = m.group(1);
        message = m.group(2);
      }

      if(key.length() == 8)
      {
        if(message.length() > 0)
        {
          messages.put(key, message);
        }
      }

      if(messages.containsKey(key))
      {
        return "OK";
      }
      System.out.println(text);
      return "NO";
    }

    /**
     * get_message(String text) - Get command processing
     * @param text - message received with GET message head.
     * tests message if it has GET -> 8 - alphanumeric characters -> more then 1 of any character | not case-sensitive.
     * @return message assigned to key or \n if no message found with key.
     */
    String get_message(String text)
    {
      Pattern rPattern = Pattern.compile("(?:GET)([\\dA-Z]{8})$", Pattern.CASE_INSENSITIVE);
      Matcher m = rPattern.matcher(text);
      String key = "";
      
      if(m.find())
      {
        key = m.group(1);
      }

      String message = messages.get(key);
      if(message != null)
      {
        return message + "\n";
      }
      else
      {
        return "\n";
      }
    }
  }

}