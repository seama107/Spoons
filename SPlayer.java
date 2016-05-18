/*
SPlayer.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V1.0 Full game implemented
Added option to keep server online
Resized gameboard window (again)
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
		return playerSocket.equals(other.playerSocket);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null)
		{
			return false;
		}
		if(!(SPlayer.class.isAssignableFrom(obj.getClass())))
		{
			return false;
		}
		SPlayer other = (SPlayer) obj;
		return playerSocket.equals(other.playerSocket);
	}


}