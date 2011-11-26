package client;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientThread extends Thread 
{
	private DataInputStream din;
	private Client client;
	
	public ClientThread(Client client, DataInputStream din)
	{
		this.client = client;
		this.din = din;
		
		this.start();
	}
	
	public void run()
	{
		try
		{
			while(true)
			{
				String message = din.readUTF();
				//System.out.println("client received: " + message);
				this.processMessage(message);
			}
		}
		catch (IOException io)
		{
			io.printStackTrace();
		}
	}
	
	public void processMessage(String message)
	{
		String[] splitMsg = message.split("---");
		
		if(splitMsg[0].equals("chat"))
		{
			// check if the user is currently looking at the room we got the text for
			if(splitMsg[1].equals(this.client.getFocusedChatroomName()))
			{
				// if so, add it to the chatlog
				// if it isn't, it doesn't matter here, since it's added to the chatroom history by the server
				// so the history will get called up by the user when they switch to viewing the relevant chatroom
				this.client.addToChatLog(splitMsg[2] + "\n");
			}
		}
		else if(splitMsg[0].equals("history"))
		{
			// set the chatlog to display the history of the current chatroom
			this.client.setChatLogHistory(splitMsg[1]);
		}

	}
}
