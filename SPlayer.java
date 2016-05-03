/*
SPlayer.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.4: Players are handed an initial starting hand
The rest of the deck is stored on the server
Created Deck and Card classes
*/

import java.net.InetSocketAddress;


public class SPlayer
{

	public InetSocketAddress playerSocket;
	public String playerName;
	public SDeck hand;
	public SDeck drawPile;
	public SCard nextCard;
	public boolean hasSpoon;

	public SPlayer(InetSocketAddress s, String n)
	{
		playerSocket = s;
		playerName = n;
		hand = new SDeck();
		drawPile = new SDeck();
		nextCard = new SCard();
		hasSpoon = false;
	}

	public boolean equals(SPlayer other)
	{
		return (playerName.equals(other.playerName) && playerSocket.equals(other.playerSocket));
	}


}