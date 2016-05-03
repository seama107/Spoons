/*
SClient.java
Author: Michael Seaman

The client for the "Spoons" Final Project
V0.5: Game mechanics implemented: Swapping, drawing, passing, and spoon taking
Interface for client implemented
extended card encryption
SGameBoard.java created
*/

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.NetworkInterface;

import java.util.Scanner;


public class SClient
{

	final static String BCAST_ADDR = "224.0.0.7";
	final static int BCAST_PORT = 7777;
	final static int WAIT_TIME = 10;

	private DatagramSocket sendSocket;
	private MulticastSocket receiveSocket;
	private InetSocketAddress broadcastAddress;
	private InetSocketAddress localSendAddress;
	private byte[] buf;

	private String username;

	public SClient()
	{
		this("default");
	}

	public SClient(String un)
	{
		username = un;
		java.lang.System.setProperty("java.net.preferIPv4Stack" , "true");
		buf = new byte[256];
		receiveSocket = null;
		sendSocket = null;
		try
		{
			sendSocket = new DatagramSocket();
			localSendAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), sendSocket.getLocalPort());
			broadcastAddress = new  InetSocketAddress(BCAST_ADDR, BCAST_PORT);
			InetAddress broadcastAddressNotSocket = InetAddress.getByName(BCAST_ADDR);
			//receiveSocket = new MulticastSocket(broadcastAddress);
			receiveSocket = new MulticastSocket(BCAST_PORT);
			//NetworkInterface networkInterface = NetworkInterface.getByName("en0");
			//receiveSocket.joinGroup(broadcastAddress, networkInterface);
			receiveSocket.joinGroup(broadcastAddressNotSocket);

			SClientListener listener = new SClientListener(localSendAddress);
			Thread listenerThread = new Thread(listener);
			listenerThread.start();

			//Connecting with server
			sendRaw("h" + username);
			String rcvMessage = recieveNextIMessage().trim();
			if(!(rcvMessage.substring(0, rcvMessage.length() -1).equals("i" + username)))
			{
				throw new IOException("Could not connect with Server.");
			}
			System.out.println("Connected.");

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args)  throws Exception
	{
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Welcome to Spoons Online!");
		System.out.println("Enter the the name you want to connect with:");
		String username = keyboard.nextLine();
		SClient sc = new SClient(username);
		sc.loopForUserInput();
		System.out.println("Client closing down.");
		sc.shutDown();
	}

	public void loopForUserInput() throws IOException
	{
		Scanner keyboard = new Scanner(System.in);
		String userInput;
		while(true)
		{
			userInput = keyboard.nextLine();
			sendMessage(userInput);
			if(userInput.toLowerCase().equals("q"))
			{
				break;
			}
		}

	}

	public void sendRaw(String message) throws IOException
	{
		DatagramPacket msgPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, broadcastAddress);
		sendSocket.send(msgPacket);
	}

	public void sendMessage(String message) throws IOException
	{
		sendRaw("c" + message);
	}

	/*public void sendCommand(String message) throws IOException
	{
		sendRaw("c" + message);
	}*/

	public String recieveMessage() throws IOException, SocketTimeoutException
	{
		DatagramPacket messagePacket = new DatagramPacket(buf, buf.length);
		receiveSocket.receive(messagePacket);
		String message = new String(buf, 0, buf.length);
		buf = new byte[256];
		return message;
	}

	public String recieveNextIMessage() throws IOException, SocketException
	{
		receiveSocket.setSoTimeout(WAIT_TIME);
		String message = "";
		try
		{
			do
			{
				message = recieveMessage();
			} while(!(message.length() > 0 && message.charAt(0) == 'i'));
		}
		catch(SocketTimeoutException e)
		{
			System.out.println("SocketTimeoutException");
			e.printStackTrace();
		}
		if(message.equals(""))
		{
			throw new IOException("Connection could not be established with server.");
		}
		receiveSocket.setSoTimeout(0);
		return message;
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

	public String getUsername()
	{
		return username;
	}

}