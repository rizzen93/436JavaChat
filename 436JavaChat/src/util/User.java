package util;

import java.io.DataOutputStream;
import java.net.Socket;

public class User 
{

	private String username;
	private int userID;
	private DataOutputStream dout;
	private Socket sock;
	
	public User(Socket sock, DataOutputStream dout, int ID)
	{
		this.sock = sock;
		this.dout = dout;
		this.userID = ID;
		this.username = "client#" + this.userID;
	}
	
	public String getUsername()
	{
		return this.username;
	}
	
	public Socket getSocket()
	{
		return this.sock;
	}
	
	public DataOutputStream getDout()
	{
		return this.dout;
	}
	
	public int getUserID()
	{
		return this.userID;
	}
}
