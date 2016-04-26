/*
SClientListener.java
Author: Michael Seaman

The client for the "Spoons" Final Project
V0.3: The Game now starts properly
Created the Player and RichMessage classes
*/

import java.io.IOException;
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

	private DatagramSocket sendSocket;
	private MulticastSocket receiveSocket;
	private InetSocketAddress broadcastAddress;
	private InetSocketAddress localSendAddress;
	private byte[] buf;

	public SClientListener(InetSocketAddress lsa)
	{
		java.lang.System.setProperty("java.net.preferIPv4Stack" , "true");
		buf = new byte[256];
		receiveSocket = null;
		sendSocket = null;
		try
		{
			sendSocket = new DatagramSocket();
			localSendAddress = lsa;
			broadcastAddress = new  InetSocketAddress(BCAST_ADDR, BCAST_PORT);
			receiveSocket = new MulticastSocket(broadcastAddress);
			NetworkInterface networkInterface = NetworkInterface.getByName("en0");
			receiveSocket.joinGroup(broadcastAddress, networkInterface);
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
		DatagramPacket msgPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, broadcastAddress);
		sendSocket.send(msgPacket);
	}

	public String recieveMessage() throws IOException
	{
		DatagramPacket messagePacket = new DatagramPacket(buf, buf.length);
		receiveSocket.receive(messagePacket);
		String message = new String(buf, 0, buf.length);
		//packing the message so the process message knows whether or not the user sent it
		message = (isPacketFromUser(messagePacket) ? "t" : "f") + message;
		buf = new byte[256];
		return message;
	}

	public int processMessage(String message, boolean userSent)
	{
		if(message.trim().equals("0q"))
		{
			//Returns 1 for a qq message, meaning the server is closing.
			return 2;
		}
		else if(userSent && message.trim().equals("q"))
		{
			//Indicating that the user dropped. This will terminate the program
			return 3;
		}
		else if(message.length() > 0 && message.charAt(0) == '0')
		{
			//Message from the server, return 1, indicating it should be printed
			return 1;
		}
		else
		{
			//Message not to be displayed
			return 0;
		}
	}

	public boolean isPacketFromUser(DatagramPacket packet)
	{
		return ((InetSocketAddress) packet.getSocketAddress()).equals(localSendAddress);
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