package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.Chatroom;

public class Client extends JFrame implements ListSelectionListener
{
	// gui components
	// right side of the frame
	private JButton sendTextButton;
	private JTextField chatLine;
	private JTextArea chatLog;
	
	// left side
	private JButton joinChatroomButton;
	private JButton leaveChatroomButton;
	private JList roomList;
	private DefaultListModel rooms;
	private JScrollPane scrollRoomList;
	
		// connection stuffs
	private Socket sock;
	private DataOutputStream dout;
	private DataInputStream din;
	
	private String focusedChatroom;
	
	public Client(String host, int port)
	{
		// gui
		this.initGui();
		
		try
		{
			// connect to the server
			this.sock = new Socket(host, port);
			
			// streams
			this.dout = new DataOutputStream(this.sock.getOutputStream());
			this.din = new DataInputStream(this.sock.getInputStream());
			
			System.out.println("Connected to server: " + this.sock);
			
			// handle incoming stuff from the server
			new ClientThread(this, this.din);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}
	
	/**
	 * sends a message to the server
	 * goes a little something like this:
	 * <command>---<chatroom>---<text>
	 * @param command
	 * @param chatroomName
	 * @param message
	 */
	private void sendMessageToServer(String command, String chatroomName, String message)
	{
		if((command == null) || chatroomName == null || message == null)
			return;
		
		try
		{
			if((command.equals("join")) || (command.equals("leave")) || (command.equals("history")))
			{
				this.dout.writeUTF(command + "---" + chatroomName);
			}
			else
				this.dout.writeUTF(command + "---" + chatroomName + "---" + message);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void initGui()
	{
		JPanel wholeThing = new JPanel(new BorderLayout());
		JPanel leftSidePanel = new JPanel(new BorderLayout());
		JPanel rightSidePanel = new JPanel(new BorderLayout());

		// left side of gui
		// inits
		JPanel bottomButtonPanel = new JPanel(new BorderLayout());
		this.rooms = new DefaultListModel();
		this.roomList = new JList(rooms);
		this.roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.roomList.addListSelectionListener(this);
		this.scrollRoomList = new JScrollPane(roomList);
		this.rooms.addElement("lobby");
		this.focusedChatroom = "lobby";
		this.joinChatroomButton = new JButton("Join Room");
		this.leaveChatroomButton = new JButton("Leave Room");

		// throw the left side together
		bottomButtonPanel.add(BorderLayout.WEST, this.joinChatroomButton);
		bottomButtonPanel.add(BorderLayout.EAST, this.leaveChatroomButton);
		leftSidePanel.add(BorderLayout.CENTER, this.scrollRoomList);
		leftSidePanel.add(BorderLayout.SOUTH, bottomButtonPanel);

		// right side of gui
		// inits
		JPanel textLinePanel = new JPanel(new BorderLayout());
		this.chatLine = new JTextField();
		this.chatLog = new JTextArea();
		this.chatLog.setEditable(false);
		this.chatLog.setLineWrap(true);
		this.sendTextButton = new JButton("Send Text");

		// throw the right side together
		textLinePanel.add(BorderLayout.EAST, this.sendTextButton);
		textLinePanel.add(BorderLayout.CENTER, this.chatLine);
		rightSidePanel.add(BorderLayout.SOUTH, textLinePanel);
		rightSidePanel.add(BorderLayout.CENTER, this.chatLog);

		// put em together
		wholeThing.add(BorderLayout.WEST, leftSidePanel);
		wholeThing.add(BorderLayout.CENTER, rightSidePanel);

		// mainframe stuff
		this.add(wholeThing);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(new Dimension(600, 350));
		this.setResizable(false);
		this.getRootPane().setDefaultButton(this.sendTextButton);
		this.setVisible(true);
		
		this.sendTextButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// get the text
				String message = chatLine.getText();

				// get the name of the chatroom we're sending this text to
				String name = roomList.getSelectedValue().toString();
				
				// send to server for other connected clients to see
				sendMessageToServer("chat", name, message);
				chatLine.setText("");
			}
		});

		this.joinChatroomButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// get the name of the new chatroom from the user
				String selectedRoom = JOptionPane.showInputDialog("Enter new chatroom name:");

				// tell the server we're joining a new room
				sendMessageToServer("join", selectedRoom, "join");
				focusedChatroom = selectedRoom;
				
				// gui
				rooms.addElement(selectedRoom);
				roomList.repaint();
			}
		});

		this.leaveChatroomButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// get the string
				String name = roomList.getSelectedValue().toString();

				// remove from server
				sendMessageToServer("leave", name, "leave");
				
				//focusedChatroom = "lobby";
				//sendMessageToServer("history", focusedChatroom, "");
				
				// remove from client jlist
				rooms.removeElement(name);
				chatLog.setText("");
				roomList.repaint();
			}
		});
	}

	public String getCurrentChatroomName()
	{
		return (String) this.roomList.getSelectedValue();
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) 
	{
		// get the name of the chatroom we're switching focus to
		String name = (String) this.roomList.getSelectedValue();
		
		this.sendMessageToServer("history", name, "");
		this.focusedChatroom = name;
	}
	
	public void addToChatLog(String text)
	{
		//System.out.println("appending: " + text);
		this.chatLog.append(text);
	}
	
	public void setChatLogHistory(String history)
	{
		this.chatLog.setText("");
		this.chatLog.setText(history);
	}
	
	public String getFocusedChatroomName()
	{
		return this.focusedChatroom;
	}
	
	public static void main(String[] args)
	{
		new Client("localhost", 5555);
	}

}
