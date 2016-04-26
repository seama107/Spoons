/*
SRichMessage.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.3: The Game now starts properly
Created the Player and RichMessage classes
*/

import java.net.InetSocketAddress;


public class SRichMessage
{

	public InetSocketAddress fromAddress;
	public String message;

	public SRichMessage(InetSocketAddress f, String m)
	{
		fromAddress = f;
		message = m;
	}

	public boolean equals(SRichMessage other)
	{
		return (fromAddress.equals(other.fromAddress) && message.equals(other.message));
	}

}