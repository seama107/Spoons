/*
SRichMessage.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V1.0 Full game implemented
Added option to keep server online
Resized gameboard window (again)
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