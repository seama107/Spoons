/*
SRichMessage.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.5: Game mechanics implemented: Swapping, drawing, passing, and spoon taking
Interface for client implemented
extended card encryption
SGameBoard.java created
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