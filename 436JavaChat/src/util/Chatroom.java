package util;

import java.util.ArrayList;

/**
 * class to represent a chatroom object for the server to keep track of
 * @author ryan
 *
 */
public class Chatroom 
{

	private String chatroomName;
	private String chatHistory;
	private ArrayList<User> usersInRoom;
	private ArrayList<String> textQueue;
	
	public Chatroom(String name)
	{
		this.chatroomName = name;
		this.chatHistory = "";
		this.usersInRoom = new ArrayList<User>();
		this.textQueue = new ArrayList<String>();
	}
	
	public String getChatHistory()
	{
		return this.chatHistory;
	}
	
	public void addToHistory(String message)
	{
		this.chatHistory += message + "\n";
	}
	
	/**
	 * gets the name of the chatroom
	 * @return
	 */
	public String getChatroomName()
	{
		return this.chatroomName;
	}
	
	/**
	 * adds a user to the chatroom
	 * @param u
	 */
	public void addUserToChatroom(User u)
	{
		synchronized(this.usersInRoom)
		{
			this.usersInRoom.add(u);
		}
	}
	
	public void removeUserFromChatroom(User u)
	{
		synchronized(this.usersInRoom)
		{
			this.usersInRoom.remove(u);
		}
	}
	/**
	 * gets the list of users currently in this chatroom
	 * @return
	 */
	public ArrayList<User> getUsersInRoom()
	{
		return this.usersInRoom;
	}
	
	/**
	 * adds a message to the text queue, for viewing the next time the chatroom is displayed to the client
	 * @param m
	 */
	public void addMessageToRoomQueue(String m)
	{
		this.textQueue.add(m);
	}
	
	public String dumpMessageQueueToString()
	{
		String dump = "";
		
		for(int i = 0; i < this.textQueue.size(); i++)
		{
			dump += this.textQueue.get(i) + "\n";
		}
		
		this.clearQueue();
		return dump;
	}
	
	public void clearQueue()
	{
		this.textQueue.clear();
	}
}
