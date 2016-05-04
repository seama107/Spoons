/*
SServer.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.5: Game mechanics implemented: Swapping, drawing, passing, and spoon taking
Interface for client implemented
extended card encryption
SGameBoard.java created
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.NumberFormatException;
import java.lang.ArrayIndexOutOfBoundsException;

public class SServer
{
	final static String BCAST_ADDR = "224.0.0.7";
	final static int BCAST_PORT = 7777;
	final static int WAIT_TIME = 1;
	final static int NUM_PLAYERS = 3;

	private DatagramSocket sendSocket;
	private MulticastSocket receiveSocket;
	private InetSocketAddress broadcastSocketAddress;
	private InetAddress broadcastAddress;
	private byte[] buf;
	private ArrayList<SPlayer> playerList;
	private boolean[] spoonArray;

	public SServer()
	{
		java.lang.System.setProperty("java.net.preferIPv4Stack" , "true");
		buf = new byte[256];
		receiveSocket = null;
		sendSocket = null;
		playerList = new ArrayList<SPlayer>();
		try
		{
			broadcastSocketAddress = new InetSocketAddress(BCAST_ADDR, BCAST_PORT);
			broadcastAddress = InetAddress.getByName(BCAST_ADDR);
			sendSocket = new DatagramSocket();

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

	public static void main(String[] args)  throws Exception
	{
		SServer ss = new SServer();
		ss.sendMessage("Hello!");
		ss.waitForconnections(NUM_PLAYERS);
		ss.sendMessage("Game Starting.");
		ss.gameMode(NUM_PLAYERS);
		ss.shutDown();
	}

	public void waitForconnections(int maxPlayers) throws IOException
	{
		/* Waits for a 'hello' message, aka one that starts with 'h' 
			and is from a client that hasn't been heard from before.
			When recieved, the client is added to the list and an ack
			message is sent back in the form "i<username><playernumber(in hex)>"
		*/

		while(playerList.size() < maxPlayers)
		{
			SRichMessage richMessage = recieveRichMessage();
			boolean isHelloMessage = false;
			boolean notInList = false;
			String username = "";
			InetSocketAddress fromAddress = null;
			isHelloMessage = (richMessage.message.length() > 0 && richMessage.message.charAt(0) == 'h');
			if(isHelloMessage)
			{
				username = richMessage.message.substring(1);
				fromAddress = richMessage.fromAddress;

				notInList = !(playerList.contains(new SPlayer(fromAddress, username)));
			}

			if(isHelloMessage && notInList)
			{
				playerList.add(new SPlayer(fromAddress, username));
				sendIData(username + Integer.toHexString(playerList.size() - 1));
				sendMessage(username + " has connected. Wating for " + (maxPlayers - playerList.size()) + " more players.");
			}
		}
	}

	public void gameMode(int num_players) throws Exception
	{
		initializeGame(num_players);
		boolean gameStillInProgress = true;
		while(gameStillInProgress)
		{
			SRichMessage toBeProcessed = recieveRichMessage();
			switch(processClientGameMessage(toBeProcessed))
			{
				default:
					break;
			}
		}


	}

	public void showAllPlayersTheirEncryptedHands() throws IOException
	{
		for (SPlayer p: playerList)
		{
			showPlayerTheirEncryptedHand(p);
		}
	}

	public void showPlayerTheirEncryptedHand(SPlayer p) throws IOException
	{
		//J♦X♥A♥K♦;K♥;2;tftf
		String data = "h" + p.hand.encrypt() + ";";
		data += (p.nextCard.equals(new SCard()) ? "" : p.nextCard.encrypt()) + ";";
		data += p.drawPile.size() + ";";
		for (boolean b: spoonArray)
		{
			data += b ? "t" : "f";
		}

		sendDataTo(p, data);
	}

	public void initializeGame(int num_players) throws IOException
	{
		spoonArray = new boolean[num_players - 1];
		Arrays.fill(spoonArray, true);
		SDeck initialPile = new SDeck(52);
		initialPile.shuffle();
		for(int i = 0; i < playerList.size(); ++i)
		{
			playerList.get(i).hand.addAll(initialPile.subList(0,4));
			initialPile.removeRange(0,4);
		}
		playerList.get(0).drawPile.addAll(initialPile);


		for(int i = 0; i < playerList.size(); ++i)
		{
			System.out.println("Player " + playerList.get(i).playerName + " has:");
			System.out.println(playerList.get(i).hand.toString());
			System.out.println("Draw: " + playerList.get(i).drawPile.toString());
			for (String line: playerList.get(i).hand.toStringInline()) 
			{
				System.out.println(line);
			}
		}
		showAllPlayersTheirEncryptedHands();
	}


	public int processClientGameMessage(SRichMessage gameMessage) throws IOException
	{
		/* 
		Processes client messages
		returns 1 on success
		returns 0 on client quitting message
		returns -1 for formatting errors in incoming messages
		return -2 for not having applicable codes
		returns -3 for nonexistent recieves
		*/

		if(gameMessage.message.length() < 2)
		{
			System.out.println("Recieved message too short.");
			return -1;
		}
		else if(gameMessage.message.charAt(0) != 'c')
		{
			System.out.println("Recieved message missing client code tag 'c'.");
			return -1;
		}

		SPlayer messageSender = null;
		try
		{
			messageSender = getPlayerWithSocket(gameMessage.fromAddress);
		}
		catch(Exception e)
		{
			System.out.println("Just recieved message from non-existant player");
			System.out.println(gameMessage.fromAddress);
			System.out.println("MESSAGE: " + gameMessage.message);
			return -3;
		}

		switch(gameMessage.message.charAt(1))
		{
			case 'd':
				//player is drawing a card
				playerDraw(messageSender);
				return 1;
			case 'p':
				//player is passing the card they drew
				playerPass(messageSender, true);
				return 1;
			case 'q':
				//player is quitting
				playerQuitting(messageSender);
				return 0;

			default:
				break;
		}

		if(gameMessage.message.length() < 3)
		{
			System.out.println("Recieved client message that wasn't pass, draw, and was too short");
			return -1;
		}

		int messageValue = 0;

		try
		{
			messageValue = Integer.parseInt(gameMessage.message.substring(2));
		}
		catch(NumberFormatException e)
		{
			System.out.println("Recieved client message that wasn't pass, draw, and couldn't cast as int.");
			return -1;
		}


		switch(gameMessage.message.charAt(1))
		{
			case 's':
				//player is swapping cards
				playerCardSwap(messageSender, messageValue);
				return 1;
			case 't':
				//player is taking a spoon
				playerTakeSpoon(messageSender, messageValue);
				return 1;
			default:
				break;
		}
		return -2;
	}

	public void playerDraw(SPlayer sender) throws IOException
	{
		if(sender.drawPile.size() > 0)
		{
			playerPass(sender, false);
			sender.nextCard = sender.drawPile.get(0);
			sender.drawPile.remove(0);
			showPlayerTheirEncryptedHand(sender);
		}
		else
		{
			sendPrivateMessageTo(sender, "There is nothing in your pile to draw.");
		}
	}

	public void playerPass(SPlayer sender, boolean updateScreenForPlayer) throws IOException
	{
		if(sender.nextCard.equals(new SCard()))
		{
			return;
		}
		SPlayer nextPlayer = getNextPlayer(sender);
		nextPlayer.drawPile.add(sender.nextCard);
		sender.nextCard = new SCard();
		if(updateScreenForPlayer)
			showPlayerTheirEncryptedHand(sender);
		showPlayerTheirEncryptedHand(nextPlayer);
	}

	public void playerCardSwap(SPlayer sender, int cardToSwapPosition) throws IOException
	{
		if(sender.nextCard.equals(new SCard()))
		{
			sendPrivateMessageTo(sender, "You don't have a card to swap.");
			return;
		}
		if(cardToSwapPosition > 4 || cardToSwapPosition < 1)
		{
			sendPrivateMessageTo(sender, "Swap with a card in your hand from 1 to 4. Ex: 's3'");
			return;
		}
		SCard temp = new SCard(sender.hand.get(cardToSwapPosition - 1));
		sender.hand.set(cardToSwapPosition - 1, sender.nextCard);
		sender.nextCard = temp;
		showPlayerTheirEncryptedHand(sender);
		return;
	}

	public void playerTakeSpoon(SPlayer sender, int spoonPosition) throws IOException
	{
		boolean successfulGrab = true;
		try
		{
			successfulGrab = spoonArray[spoonPosition - 1];

		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			successfulGrab = false;
		}

		if(successfulGrab)
		{
			spoonArray[spoonPosition - 1] = false;
			sender.hasSpoon = true;
			showAllPlayersTheirEncryptedHands();
			sendPrivateMessageTo(sender, "You got the spoon!");
		}
		else
		{
			sendPrivateMessageTo(sender, "That spoon is unavailable.");
		}
		return;
	}

	public void playerQuitting(SPlayer sender)
	{
		//placeholder for now
		System.out.println(sender.playerName + " is quitting. placeholder text.");
		return;
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

	public void sendIData(String message) throws IOException
	{
		sendRaw("i" + message);
	}

	public void sendMessage(String message) throws IOException
	{
		System.out.println(message);
		sendRaw("0" + message);
	}

	public void sendRaw(String message) throws IOException
	{
		//System.out.println(message);
		DatagramPacket msgPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, broadcastSocketAddress);
		sendSocket.send(msgPacket);
	}

	public void sendPrivateMessageTo(SPlayer p, String message) throws IOException
	{
		int playerNumber = playerList.indexOf(p);
		sendRaw("p" + Integer.toHexString(playerNumber) + message);
	}

	public void sendDataTo(SPlayer p, String message) throws IOException
	{
		int playerNumber = playerList.indexOf(p);
		sendRaw("d" + Integer.toHexString(playerNumber) + message);
	}

	public SPlayer getNextPlayer(SPlayer current)
	{
		return playerList.get( (playerList.indexOf(current) + 1) % playerList.size());
	}

	public SPlayer getPlayerWithSocket(InetSocketAddress sockAdd) throws Exception
	{
		for (SPlayer p: playerList)
		{
			if(p.playerSocket.equals(sockAdd))
			{
				return p;
			}
		}
		throw new Exception("No such Player.");
	}

	public String recieveMessage() throws IOException
	{
		buf = new byte[256];
		DatagramPacket messagePacket = new DatagramPacket(buf, buf.length);
		String message = "";
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

}