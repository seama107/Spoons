/*
SServer.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V1.0 Full game implemented
Added option to keep server online
Resized gameboard window (again)
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
import java.util.Scanner;
import java.lang.NumberFormatException;
import java.lang.InterruptedException;
import java.lang.ArrayIndexOutOfBoundsException;

public class SServer
{
	final static String BCAST_ADDR = "224.0.0.7";
	final static int BCAST_PORT = 7777;
	final static int WAIT_TIME = 1;

	private DatagramSocket sendSocket;
	private InetSocketAddress localSendAddress;
	private MulticastSocket receiveSocket;
	private InetSocketAddress broadcastSocketAddress;
	private InetAddress broadcastAddress;
	private byte[] buf;
	private ArrayList<SPlayer> playerList;
	private boolean[] spoonArray;

	private ArrayList<String> helpString;

	public SServer()
	{
		java.lang.System.setProperty("java.net.preferIPv4Stack" , "true");
		buf = new byte[256];
		receiveSocket = null;
		sendSocket = null;
		playerList = new ArrayList<SPlayer>();
		initializeHelpString();
		try
		{
			broadcastSocketAddress = new InetSocketAddress(BCAST_ADDR, BCAST_PORT);
			broadcastAddress = InetAddress.getByName(BCAST_ADDR);
			sendSocket = new DatagramSocket();
			localSendAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), sendSocket.getLocalPort());

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
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Server started");
		System.out.print("How many players would you like to play with? ");
		String userInput = keyboard.nextLine();
		int num_players = 3;
		try
		{
			num_players = Integer.parseInt(userInput);
		}
		catch(Exception e)
		{
			;
		}

		System.out.print("Would you like the server to remain online after a game? (Y/N)   ");
		boolean remainOnline = false;
		userInput = keyboard.nextLine();
		try
		{
			remainOnline = Character.toLowerCase(userInput.charAt(0)) == 'y';
		}
		catch(Exception e)
		{
			;
		}

		System.out.println();
		System.out.println(String.format("Number of players: %5d   Remain-Online: %7s", num_players, remainOnline) );
		System.out.println();

		SServer ss = new SServer();
		ss.sendMessage("SERVER ONLINE");


		while(true)
		{
			ss.waitForconnections(num_players);
			ss.sendMessage("Game Starting.");
			ss.gameMode(num_players);
			if(remainOnline)
			{
				ss.sendMessage("");
				ss.sendMessage("If you'd like to play again, the server will still be up!");
				ss.sendMessage("Simply restart your client and reconnect.");
				ss.sendMessage("r");

				ss.clearPlayerList();
			}
			else
			{
				break;
			}
		}




		ss.sendMessage("q");
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
			boolean isQuittingMessage = false;
			isQuittingMessage = richMessage.message.equals("cq");
			isHelloMessage = (richMessage.message.length() > 0 && richMessage.message.charAt(0) == 'h');
			if(isHelloMessage)
			{
				String username = richMessage.message.substring(1);
				InetSocketAddress fromAddress = richMessage.fromAddress;
				SPlayer newPlayer = new SPlayer(fromAddress, username);

				boolean notInList = !(playerList.contains(newPlayer));
				if(notInList)
				{
					playerList.add(newPlayer);
					sendIData(username + Integer.toHexString(playerList.size() - 1));
					sendMessage(username + " has connected. Wating for " + (maxPlayers - playerList.size()) + " more players.");
				}
			}
			else if(isQuittingMessage)
			{
				InetSocketAddress fromAddress = richMessage.fromAddress;

				SPlayer leavingPlayer = new SPlayer(fromAddress, "");
				boolean inList = playerList.contains(leavingPlayer);
				if(inList)
				{
					String username = playerList.get(playerList.indexOf(leavingPlayer)).playerName;
					playerList.remove(leavingPlayer);
					sendMessage(username + " has disconnected. Wating for " + (maxPlayers - playerList.size()) + " more players.");
				}
			}

		}
	}

	public void clearPlayerList()
	{
		playerList.clear();
	}

	public void gameMode(int num_players) throws Exception
	{
		initializeGame(num_players);
		displayHelpToAllPlayers(1000);
		showAllPlayersTheirEncryptedHands();

		boolean gameStillInProgress = true;
		boolean gameExitedSafely = true;
		while(gameStillInProgress)
		{
			SRichMessage toBeProcessed = recieveRichMessage();
			switch(processClientGameMessage(toBeProcessed))
			{
				case 0:
					gameStillInProgress = false;
					gameExitedSafely = false;
					break;

				case 2:
					gameStillInProgress = false;
					break;

				default:
					break;
			}
		}
		if(gameExitedSafely)
		{
			gameWrapUp();
		}


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
	}


	public int processClientGameMessage(SRichMessage gameMessage) throws IOException, InterruptedException
	{
		/* 
		Processes client messages
		returns 2 on last spoon taken AKA Game over
		returns 1 on success
		returns 0 on client quitting message
		returns -1 for formatting errors in incoming messages
		return -2 for not having applicable codes
		returns -3 for nonexistent recieves
		*/

		if(isSRichMessageFromMe(gameMessage))
		{
			//Message was sent by server
			return 1;
		}

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
			case 'h':
				//player is asking for the help menu
				playerHelp(messageSender);
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
			sendPrivateMessageTo(messageSender, "Make sure to follow the codes listed above.");
			return -1;
		}

		int messageValue = 0;

		try
		{
			messageValue = Integer.parseInt(gameMessage.message.substring(2));
		}
		catch(NumberFormatException e)
		{
			sendPrivateMessageTo(messageSender, "Make sure to follow the codes listed above.");
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
				return playerTakeSpoon(messageSender, messageValue);
			default:
				break;
		}
		sendPrivateMessageTo(messageSender, "Make sure to follow the codes listed above.");
		return -2;
	}

	public void gameWrapUp() throws IOException, InterruptedException
	{
		sendMessage("Game Over!");
		Thread.sleep(500);
		if(atLeastOnePlayer4ofAKind())
		{
			//Game over with no cheating
			sendMessage("The first person to get 4 of a kind was ...");
			Thread.sleep(1000);
			String winnerName = playerWith4ofAKind().playerName;
			sendMessage(winnerName + "!");
			Thread.sleep(1000);
			sendMessage("The loser who didn't get a spoon was ...");
			Thread.sleep(1000);
			String loserName = playerWithoutSpoon().playerName;
			sendMessage(loserName + "!");
			Thread.sleep(1000);
		}
		else
		{
			//someone cheated and took a spoon without having 4ofAKind
			sendMessage("No one got 4 of a kind!");
			Thread.sleep(1000);
			String loserName = playerWhoDrewFirst().playerName;
			sendMessage("Looks like " + loserName + " cheated and took a spoon too early!");
			Thread.sleep(1000);
			sendMessage("Our loser is " + loserName + "!");
			Thread.sleep(1000);
		}
		sendMessage("Thanks for playing!");
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
			sendPrivateMessageTo(sender, "You have no card to pass.");
			return;
		}
		SPlayer nextPlayer = getNextPlayer(sender);
		nextPlayer.drawPile.add(sender.nextCard);
		sender.nextCard = new SCard();
		if(updateScreenForPlayer)
			showPlayerTheirEncryptedHand(sender);
		showPlayerTheirEncryptedHand(nextPlayer);
	}

	public void playerHelp(SPlayer sender) throws IOException
	{
		for (String line: helpString )
		{
			sendPrivateMessageTo(sender, line);
		}
	}

	public void displayHelpToAllPlayers(int waitTime) throws InterruptedException, IOException
	{
		sendMessage("");
		sendMessage("");
		for (String line: helpString )
		{
			sendMessage(line);
			Thread.sleep(waitTime);
		}
		sendMessage("");
		sendMessage("Get ready to see your cards!");
		sendMessage("");
		Thread.sleep(waitTime * 5);
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
		if(playerHas4ofAKind(sender))
		{
			sendPrivateMessageTo(sender, "You have 4 of a kind! Grab a spoon quick!");
		}
	}

	public int playerTakeSpoon(SPlayer sender, int spoonPosition) throws IOException
	{
		boolean successfulGrab = true;
		if(sender.hasSpoon)
		{
			sendPrivateMessageTo(sender, "You already have a spoon!");
			return 1;
		}
		boolean wouldBeFirstTaker = allSpoonsInPlay();

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
			sender.firstSpoonTaker = wouldBeFirstTaker;
			showAllPlayersTheirEncryptedHands();
			sendPrivateMessageTo(sender, "You got the spoon!");
		}
		else
		{
			sendPrivateMessageTo(sender, "That spoon is unavailable.");
		}
		if(allSpoonsTaken())
		{
			return 2;
		}

		return 1;
	}

	public void playerQuitting(SPlayer sender) throws IOException
	{
		//placeholder for now
		sendMessage(sender.playerName + " is quitting. The game will now close.");
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

	public boolean isSRichMessageFromMe(SRichMessage srm)
	{
		return srm.fromAddress.equals(localSendAddress);
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

	public SPlayer playerWithoutSpoon()
	{
		for(SPlayer p: playerList)
		{
			if(!(p.hasSpoon))
			{
				return p;
			}
		}

		return new SPlayer();
	}

	public boolean allSpoonsInPlay()
	{
		for (boolean spoon: spoonArray)
		{
			if(!spoon)
			{
				return false;
			}
		}
		return true;
	}

	public boolean allSpoonsTaken()
	{
		for (boolean spoon: spoonArray)
		{
			if(spoon)
			{
				return false;
			}
		}
		return true;
	}

	public SPlayer playerWith4ofAKind()
	{
		for(SPlayer p: playerList)
		{
			if( playerHas4ofAKind(p) )
			{
				return p;
			}
		}

		return new SPlayer();
	}

	public boolean atLeastOnePlayer4ofAKind()
	{
		for(SPlayer p: playerList)
		{
			if(playerHas4ofAKind(p))
			{
				return true;
			}
		}

		return false;
	}

	public boolean playerHas4ofAKind(SPlayer player)
	{
		String cardRankString = player.hand.get(0).rankString;
		for (SCard c: player.hand)
		{
			if(!(c.rankString.equals(cardRankString)))
			{
				return false;
			}
		}
		return true;
	}

	public SPlayer playerWhoDrewFirst()
	{
		for(SPlayer p: playerList)
		{
			if(p.firstSpoonTaker)
			{
				return p;
			}
		}

		return new SPlayer();
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

	public void initializeHelpString()
	{
		helpString = new ArrayList<String>();
		helpString.add("Welcome to the game of spoons!");
		helpString.add("The player who connected first will have cards to draw from.");
		helpString.add("You can draw from your draw pile by pressing 'd'.");
		helpString.add("When you have a card drawn, you can swap it with 's'.");
		helpString.add("Do this by typing 's' followed by the position of the card you want to switch.");
		helpString.add("Ex: 's3' swaps the drawn card with the 3rd card in your hand.");
		helpString.add("You can also pass your drawn card to the next person by pressing 'p'.");
		helpString.add("(Hint: pressing 'd' automatically passes your held card)");
		helpString.add("You can take a spoon by with 't' followed by the spoon number.");
		helpString.add("Ex: 't1' takes the 1st spoon if it's available.");
		helpString.add("The point of spoons is to have a spoon when the game finishes.");
		helpString.add("The game will finish when a single player gets 4 of a kind of any card.");
		helpString.add("At that time, they, and any other player can grab a spoon.");
		helpString.add("Smart strategy is always watching the spoon pile, to take one for yourself if they start to disapear.");
		helpString.add("If you get lost at any time, press 'h' to display this menu again.");
	}

}