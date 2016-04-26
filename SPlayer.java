/*
SPlayer.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.3: The Game now starts properly
Created the Player and RichMessage classes
*/

import java.net.InetSocketAddress;


public class SPlayer
{

	public InetSocketAddress playerSocket;
	public String playerName;

	public SPlayer(InetSocketAddress s, String n)
	{
		playerSocket = s;
		playerName = n;
	}

	public boolean equals(SPlayer other)
	{
		return (playerName.equals(other.playerName) && playerSocket.equals(other.playerSocket));
	}


}