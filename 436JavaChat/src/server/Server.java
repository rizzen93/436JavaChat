package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import util.Chatroom;
import util.User;

/**
 * class to hold the innards of the server
 * deals with chatrooms + sending text to the proper ones.
 * @author ryan
 *
 */
public class Server 
{

	private ServerSocket servSock;
	private ArrayList<Chatroom> chatrooms;
	private ArrayList<User> users;
	
	public Server(int port)
	{
		try
		{
			// initialize the lists
			this.chatrooms = new ArrayList<Chatroom>();
			this.users = new ArrayList<User>();
			
			// make default chatroom
			this.createNewChatroom("lobby");
			//this.getChatroomByName("lobby").addToHistory("Welcome to the Lobby!");
			
			// start listening for client connections
			this.listen(port);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * listens for incoming client connections and deals with it
	 * @param port
	 * @throws IOException
	 */
	private void listen(int port) throws IOException
	{
		this.servSock = new ServerSocket(port);
		
		System.out.println("Now listening for connections on: " + this.servSock.toString());
		
		// listen for client connections
		while(true)
		{
			// get the next connection
			Socket sock = this.servSock.accept();
			
			System.out.println("Received Connection from: " + sock.toString());
			
			DataOutputStream dout = new DataOutputStream(sock.getOutputStream());
			
			// create a new user class to hold info about the connection, and shove them into the lobby
			User newUser = new User(sock, dout, this.getNumUsers());
			this.users.add(newUser);
			this.chatrooms.get(0).addUserToChatroom(newUser);
			
			// start listenin
			new ServerThread(this, newUser);
		}
	}
	
	/**
	 * sends specified text to every user currently connected to the chatroom
	 * @param room
	 * @param text
	 */
	public void sendTextToChatroom(Chatroom room, String text)
	{
		// add text to chatroom history
		room.addToHistory(text);
		
		// send new text to every user in the room
		for(User u: room.getUsersInRoom())
		{
			try
			{
				//System.out.println("Sending: " + text + " to: " + u.toString());
				u.getDout().writeUTF("chat---" + room.getChatroomName() + "---" + text);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * sends the chatroom's history to the specified client
	 * @param room
	 * @param u
	 */
	public void sendChatroomHistoryToClient(Chatroom room, User u)
	{
		try
		{
			String history = room.getChatHistory();
			
			u.getDout().writeUTF("history---" + history);
		}
		catch (IOException ie)
		{
			ie.printStackTrace();
		}
	}
	
	/**
	 * creates a new chatroom and adds it to the list of chatrooms for the server
	 * @param name
	 */
	public void createNewChatroom(String name)
	{
		// check if room exists already
		for(Chatroom c : this.chatrooms)
		{
			if(c.getChatroomName().equals(name))
				return;
		}
		
		// guess it doesn't, so make it
		Chatroom newRoom = new Chatroom(name);
		newRoom.addToHistory("Welcome to " + newRoom.getChatroomName() + "!");
		this.chatrooms.add(newRoom);
	}
	
	/**
	 * closes connection to the specified client
	 * @param u
	 * @throws IOException
	 */
	public void removeConnection(User u) throws IOException
	{
		u.getSocket().close();
	}
	
	/**
	 * gets the number of users connected to the server
	 * @return
	 */
	public int getNumUsers()
	{
		return this.users.size();
	}
	
	/**
	 * gets a specific chatroom object via it's String name
	 * @param name
	 * @return
	 */
	public Chatroom getChatroomByName(String name)
	{
		for(int i = 0; i < chatrooms.size(); i++)
		{
			if(this.chatrooms.get(i).getChatroomName().equals(name))
				return this.chatrooms.get(i);
		}
		
		return null;
	}
	
	// main 
	public static void main(String[] args)
	{
		// default port
		int port = 5555;
		
		new Server(port);
	}
}
