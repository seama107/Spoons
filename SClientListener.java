/*
SClientListener.java
Author: Michael Seaman

The client for the "Spoons" Final Project
V1.0 Full game implemented
Added option to keep server online
Resized gameboard window (again)
*/

import java.io.IOException;
import java.util.ArrayList;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.NetworkInterface;

import java.util.Scanner;


public class SClientListener implements Runnable
{

	final static String BCAST_ADDR = "224.0.0.7";
	final static int BCAST_PORT = 7777;
	final static int WAIT_TIME = 1;


	private int playerNumber;
	private DatagramSocket sendSocket;
	private MulticastSocket receiveSocket;
	private InetSocketAddress broadcastSocketAddress;
	private InetAddress broadcastAddress;
	private InetSocketAddress localSendAddress;
	private byte[] buf;

	public SClientListener(InetSocketAddress lsa)
	{
		playerNumber = -1;
		java.lang.System.setProperty("java.net.preferIPv4Stack" , "true");
		buf = new byte[256];
		receiveSocket = null;
		sendSocket = null;
		try
		{
			sendSocket = new DatagramSocket();
			localSendAddress = lsa;
			broadcastSocketAddress = new InetSocketAddress(BCAST_ADDR, BCAST_PORT);
			broadcastAddress = InetAddress.getByName(BCAST_ADDR);

			if(System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0)
			{

				NetworkInterface networkInterface = NetworkInterface.getByName("en0");
				receiveSocket = new MulticastSocket(broadcastSocketAddress);
				receiveSocket.joinGroup(broadcastSocketAddress, networkInterface);
			}
			else
			{
				receiveSocket = new MulticastSocket(BCAST_PORT);
				receiveSocket.joinGroup(broadcastAddress);
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void run()
	{
		String streamInput;
		try
		{
			boolean running = true;
			while(running)
			{
				streamInput = recieveMessage();
				//unpacking the message
				boolean userSent = streamInput.charAt(0) == 't';
				streamInput = streamInput.substring(1);
				switch(processMessage(streamInput, userSent))
				{
					case 0:
						break;
					case 1:
						System.out.println("SERVER: " + streamInput.substring(1));
						break;
					case 2:
						System.out.println("Server closing down. Press 'q' to quit.");
						running = false;
						break;
					case 3:
						running = false;
						break;
					case 4:
						if(playerNumber == -1)
							assignPlayerNumber(streamInput);
						break;
					case 5:
						processGameDataMessage(streamInput);
						break;
					case 6:
						System.out.println("SERVER: " + streamInput.substring(2));
						break;
					case 7:
						System.out.println("Press 'q' to quit.");
						running = false;
						break;
					default:
						break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Press 'q' to quit.");
		}
		shutDown();
	}

	public void sendMessage(String message) throws IOException
	{
		DatagramPacket msgPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, broadcastSocketAddress);
		sendSocket.send(msgPacket);
	}

	public String recieveMessage() throws IOException
	{
		DatagramPacket messagePacket = new DatagramPacket(buf, buf.length);
		receiveSocket.receive(messagePacket);
		String message = new String(buf, 0, buf.length);
		//packing the message so the process message knows whether or not the user sent it
		message = ((isPacketFromUser(messagePacket) ? "t" : "f") + message).trim();
		buf = new byte[256];
		return message;
	}

	public int processMessage(String message, boolean userSent)
	{
		if(message.equals("0q"))
		{
			//Returns 1 for a 0q message, meaning the server is closing.
			return 2;
		}
		if(message.equals("0r"))
		{
			//Returns 7 for a 0r message, meaning the server needs the clients to reconnect.
			return 7;
		}
		else if(userSent && message.equals("cq"))
		{
			//Indicating that the user dropped. This will terminate the program
			return 3;
		}
		else if(message.length() > 0 && message.charAt(0) == '0')
		{
			//Message from the server, return 1, indicating it should be printed
			return 1;
		}
		else if(message.length() > 0 && message.charAt(0) == 'i')
		{
			//acknowledgement message from the server, including what playerNumber
			//the client should look for
			return 4;
		}
		else if(message.length() > 2 && message.substring(0, 2).equals("d" + Integer.toHexString(playerNumber)))
		{
			//game data message from the server to this specific client, 
			//needs some needs further processing
			return 5;
		}
		else if(message.length() > 2 && message.substring(0, 2).equals("p" + Integer.toHexString(playerNumber)))
		{
			//private message sent from the server to the client
			return 6;
		}
		else
		{
			//Message not to be displayed
			return 0;
		}
	}

	public void processGameDataMessage(String message)
	{
		message = message.substring(2);
		switch(message.charAt(0))
		{
			case 'h':
				PrintGame(message.substring(1));
				break;
		}
	}

	public void assignPlayerNumber(String message)
	{
		//looks for the last digit of the ack message
		//from the server, and uses that as the player number (in hex)
		playerNumber = Integer.parseInt(message.substring(message.length()-1), 16);
	}

	public boolean isPacketFromUser(DatagramPacket packet)
	{
		return ((InetSocketAddress) packet.getSocketAddress()).equals(localSendAddress);
	}

	/*public void PrintDeck(String gameData)
	{
		SDeck hand = SDeck.decrypt(gameData);
		for (String line :hand.toStringInline())
		{
			System.out.println(line);	
		}
		System.out.println("Your cards.");
	}*/

	public void PrintGame(String gameData)
	{
		SGameBoard currentBoard = new SGameBoard(gameData);
		currentBoard.printBoard();
	}


	public void shutDown()
	{
		try
		{
			receiveSocket.leaveGroup(InetAddress.getByName(BCAST_ADDR));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		receiveSocket.close();
		sendSocket.close();
	}



}