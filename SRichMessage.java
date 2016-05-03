/*
SRichMessage.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.4: Players are handed an initial starting hand
The rest of the deck is stored on the server
Created Deck and Card classes
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

	public String toString()
	{
		return "ADDRESS: " + fromAddress + "  MES: " + message;
	}

}