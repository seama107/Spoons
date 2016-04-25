/*
SClient.java
Author: Michael Seaman

The client for the "Spoons" Final Project
V0.1: Testing for Client-Server Communication
*/

import java.io.IOException;
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

	private DatagramSocket sendSocket;
	private MulticastSocket receiveSocket;
	private InetSocketAddress broadcastAddress;
	private byte[] buf;

	public SClient()
	{
		java.lang.System.setProperty("java.net.preferIPv4Stack" , "true");
		buf = new byte[256];
		receiveSocket = null;
		sendSocket = null;
		try
		{
			sendSocket = new DatagramSocket();
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

	public static void main(String[] args)  throws Exception
	{
		SClient sc = new SClient();
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Waiting..");
		System.out.println("MESSAGE RECEIVED: " + sc.recieveMessage());
		sc.sendMessage("RECEIVED " + args[0]);
		while(true)
		{
			String userInput = keyboard.nextLine();
			if(userInput.equals("q"))
			{
				break;
			}
			else
			{
				sc.sendMessage(userInput);
			}
		}
		System.out.println("Sent Response!");
		sc.shutDown();
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
		buf = new byte[256];
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



}