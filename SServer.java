/*
SServer.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.3: The Game now starts properly
Created the Player and RichMessage classes
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;

public class SServer
{
	final static String BCAST_ADDR = "224.0.0.7";
	final static int BCAST_PORT = 7777;
	final static int WAIT_TIME = 1;

	private DatagramSocket sendSocket;
	private MulticastSocket receiveSocket;
	private InetSocketAddress broadcastAddress;
	private byte[] buf;
	private ArrayList<SPlayer> playerList;

	public SServer()
	{
		java.lang.System.setProperty("java.net.preferIPv4Stack" , "true");
		buf = new byte[256];
		receiveSocket = null;
		sendSocket = null;
		playerList = new ArrayList<SPlayer>();
		try
		{
			broadcastAddress = new InetSocketAddress(BCAST_ADDR, BCAST_PORT);
			sendSocket = new DatagramSocket();
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

	public static void main(String[] args)  throws Exception
	{
		SServer ss = new SServer();
		ss.sendMessage("Hello!");
		ss.waitForconnections(4);
		ss.sendMessage("Game Starting.");
		ss.echoMode();
		ss.shutDown();
	}

	public void sendIData(String message) throws IOException
	{
		sendRaw("i" + message);
	}

	public void sendMessage(String message) throws IOException
	{
		sendRaw("0" + message);
	}

	public void sendRaw(String message) throws IOException
	{
		DatagramPacket msgPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, broadcastAddress);
		sendSocket.send(msgPacket);
	}

	public String recieveMessage() throws IOException
	{
		buf = new byte[256];
		DatagramPacket messagePacket = new DatagramPacket(buf, buf.length);
		String message = "";
		/*boolean validPacketRecieved = false;
		while(!(validPacketRecieved))
		{
			receiveSocket.receive(messagePacket);
			message = new String(buf, 0, buf.length);
			if (message.length() > 0 && message.charAt(0) == '0')
			{
				buf = new byte[256];
				messagePacket = new DatagramPacket(buf, buf.length);
				continue;	
			}
			else
			{
				validPacketRecieved = true;
			}
		}*/
		receiveSocket.receive(messagePacket);
		message = new String(buf, 0, buf.length);
		return message;
	}

	public SRichMessage recieveRichMessage() throws IOException
	{
		//Rich messages include a from address
		buf = new byte[256];
		DatagramPacket messagePacket = new DatagramPacket(buf, buf.length);
		String message = "";
		receiveSocket.receive(messagePacket);
		message = (new String(buf, 0, buf.length)).trim();
		return new SRichMessage( (InetSocketAddress) messagePacket.getSocketAddress(), message);
	}

	public void waitForconnections(int maxPlayers) throws IOException
	{

		while(playerList.size() < maxPlayers)
		{
			SRichMessage richMessage = recieveRichMessage();
			boolean isHelloMessage = false;
			boolean notInList = false;
			String username = "";
			InetSocketAddress fromAdress = null;
			isHelloMessage = (richMessage.message.length() > 0 && richMessage.message.charAt(0) == 'h');
			if(isHelloMessage)
			{
				username = richMessage.message.substring(1);
				fromAdress = richMessage.fromAddress;

				notInList = !(playerList.contains(new SPlayer(fromAdress, username)));
			}

			if(isHelloMessage && notInList)
			{
				playerList.add(new SPlayer(fromAdress, username));
				sendIData(username);
				sendMessage(username + " has connected. Wating for " + (maxPlayers - playerList.size()) + " more players.");
				System.out.println(username + " has connected. Wating for " + (maxPlayers - playerList.size()) + " more players.");
			}
		}
	}

	public void echoMode() throws Exception
	{
		String streamInput;
		do
		{
			streamInput = recieveMessage();
			if (streamInput.length() > 0 && (streamInput.charAt(0) == '0' || streamInput.charAt(0) == 'i') )
			{
				continue;	
			}
			sendMessage(streamInput);
	
		} while(!(streamInput.trim().equals("0q")));
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