/*
SDeck.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.4: Players are handed an initial starting hand
The rest of the deck is stored on the server
Created Deck and Card classes
*/

import java.util.ArrayList;
import java.util.Collections;

public class SDeck extends ArrayList<SCard>
{

	public SDeck()
	{
		super();
	}

	public SDeck(int numCards)
	{
		super();
		for (int i = 0; i < numCards; ++i)
		{
			add(new SCard(i));	
		}
	}

	public void shuffle()
	{
		Collections.shuffle(this);
	}

	public String encrypt()
	{
		String output = "";
		for (SCard c: this)
		{
			output += c.encrypt();
		}
		return output;
	}

	public static SDeck decrypt(String encryptedDeck)
	{
		SDeck output = new SDeck();
		for (int i = 0; i < (encryptedDeck.length()/2); ++i)
		{
			String cardRankRepresentation = encryptedDeck.substring(2*i, 2*i + 1);
			String cardSymbol = encryptedDeck.substring(2*i + 1, 2*i + 2);
			output.add(SCard.decrypt(cardRankRepresentation, cardSymbol));
		}
		return output;
	}

	@Override
	public void removeRange(int startIndex, int endIndex)
	{
		//this.removeRange(startIndex, endIndex);

		for (int i = startIndex; i < endIndex; ++i)
		{
			this.remove(startIndex);
		}
	}

	public ArrayList<String> toStringInline()
	{
		ArrayList<String> output = new ArrayList<String>();
		for (int i = 0;i < 9; ++i )
		{
			output.add("");	
		}

		for (SCard c: this)
		{
			ArrayList<String> lines = c.toPrettyString();
			for (int i = 0; i < 9; ++i) 
			{
				output.set(i, output.get(i) + " " + lines.get(i));
			}
		}
		return output;
	}

	@Override
	public String toString()
	{
		String output = new String();
		for( SCard c: this)
		{
			output += c.toString() + ", ";
		}
		if(size() > 0)
		{
			output = output.substring(0, output.length() - 1);
		}
		return output;
	}

	//just for testing
	public static void main(String[] args) {
		SDeck s = new SDeck(10);
		s.shuffle();
		for (String line  : s.toStringInline() ) {
			System.out.println(line);
		}
		s.removeRange(0,4);
		for (String line  : s.toStringInline() ) {
			System.out.println(line);
		}
		System.out.println(s.encrypt());
		System.out.println(SDeck.decrypt(s.encrypt()).toString());
	}






}