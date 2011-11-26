package server;

import java.io.DataInputStream;
import java.io.IOException;

import util.User;
import util.Chatroom;

public class ServerThread extends Thread 
{

	private Server server;
	private User user;
	
	public ServerThread(Server server, User user)
	{
		this.server = server;
		this.user = user;
		
		this.start();
	}
	
	public void run()
	{
		try
		{
			// setup inputstream from user
			DataInputStream din = new DataInputStream(this.user.getSocket().getInputStream());
			
			// get messages from the user
			while(true)
			{
				String message = din.readUTF();
				//System.out.println("Received: " + message);
				this.processMessage(message);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				//System.out.println("removing connection to: " + this.user.toString());
				this.server.removeConnection(this.user);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * processes the message the server received from the client
	 * @param msg
	 */
	public void processMessage(String msg)
	{
		String[] m = msg.split("---");
		
		if(m.length > 2)
		{
			// chatting
			Chatroom c = this.server.getChatroomByName(m[1]);
			this.server.sendTextToChatroom(c, m[2]);
		}
		else
		{
			// joining a chatroom
			if(m[0].equals("join"))
			{
				// create the new room
				this.server.createNewChatroom(m[1]);
				
				// grab it
				Chatroom c = this.server.getChatroomByName(m[1]);
				
				// add the user to it
				c.addUserToChatroom(this.user);
				
				// and get it's chathistory to display to the user
				this.server.sendChatroomHistoryToClient(c, this.user);
			}
			else if(m[0].equals("leave"))
			{
				// get the room to leave
				Chatroom c = this.server.getChatroomByName(m[1]);
				
				// remove the user from the room
				c.removeUserFromChatroom(this.user);
				
				// send the user the lobby history, since it's the default room when you leave a chatroom
				this.server.sendChatroomHistoryToClient(this.server.getChatroomByName("lobby"), this.user);
			}
			else if(m[0].equals("history"))
			{
				// get the chatroom we want the history for
				Chatroom c = this.server.getChatroomByName(m[1]);
				
				// send it to the user
				this.server.sendChatroomHistoryToClient(c, this.user);
			}
		}
		
	}
}
