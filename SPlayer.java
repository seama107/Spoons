/*
SPlayer.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.6 Game mechanics implemented: Game finishing naturally, spoon logic
Moved away from ASCII
Resized gameboard window
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
	public boolean firstSpoonTaker;

	public SPlayer()
	{
		playerSocket = null;
		playerName = null;
		hand = new SDeck();
		drawPile = new SDeck();
		nextCard = new SCard();
		hasSpoon = false;
		firstSpoonTaker = false;
	}

	public SPlayer(InetSocketAddress s, String n)
	{
		playerSocket = s;
		playerName = n;
		hand = new SDeck();
		drawPile = new SDeck();
		nextCard = new SCard();
		hasSpoon = false;
		firstSpoonTaker = false;
	}

	public boolean equals(SPlayer other)
	{
		return (playerName.equals(other.playerName) && playerSocket.equals(other.playerSocket));
	}


}