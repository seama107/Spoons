/*
SPlayer.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.5: Game mechanics implemented: Swapping, drawing, passing, and spoon taking
Interface for client implemented
extended card encryption
SGameBoard.java created
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