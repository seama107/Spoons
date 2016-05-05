/*
SCard.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.5: Game mechanics implemented: Swapping, drawing, passing, and spoon taking
Interface for client implemented
extended card encryption
SGameBoard.java created
*/

import java.util.ArrayList;

public class SCard
{
	public String suit;
	public String rankString;
	public String rankRepresentation;
	public String symbol;
	public int cardNumber;

	public SCard()
	{
		//calls the number constructor, ace of clubs
		this(52);
	}

	public SCard(int cn)
	{
		this(cardWithNumber(cn));
	}

	public SCard(SCard other)
	{
		suit = other.suit;
		rankString = other.rankString;
		rankRepresentation = other.rankRepresentation;
		symbol = other.symbol;
		cardNumber = other.cardNumber;
	}

	public SCard(String rr, String s)
	{
		this(cardWithRRandSym(rr, s));
	}

	public SCard(String su, String rs, String rr, String sy, int cn)
	{
		suit = su;
		rankString = rs;
		rankRepresentation = rr;
		symbol = sy;
		cardNumber = cn;
	}

	public String encrypt()
	{
		return (rankRepresentation.length() > 1 ? "X" : rankRepresentation) + symbol;
	}

	public static SCard blankCard()
	{
		return cardWithNumber(52);
	}

	public static SCard decrypt(String rr, String s)
	{
		//just an easier name to remember than cardWithRRandSym()
		return SCard.cardWithRRandSym(rr, s);
	}

	public boolean equals(SCard other)
	{
		return cardNumber == other.cardNumber;
	}

	public String toString()
	{
		if(cardNumber < 52)
		{
			return rankString + " of " + suit;
		}
		else
		{
			return "Blank Card";
		}
	}

	public ArrayList<String> toPrettyString()
	{
		ArrayList<String> lines = new ArrayList<String>();

		if(cardNumber >= 52)
		{
			//for a blank card
			lines.add("┌─────────┐");
			for(int i = 0; i < 7; ++i)
			{
				lines.add("│░░░░░░░░░│");
			}
			lines.add("└─────────┘");
		}
		else
		{
			//not a blank card
			lines.add("┌─────────┐");
	        lines.add(String.format("│%2s       │", rankRepresentation));
	        lines.add("│         │");
	        lines.add("│         │");
	        lines.add(String.format("│    %1s    │", symbol));
	        lines.add("│         │");
	        lines.add("│         │");
	        lines.add(String.format("│       %2s│", rankRepresentation));
	        lines.add("└─────────┘");
	    }
        return lines;
	}

	public static SCard cardWithRRandSym(String rr, String s)
	{
		//Stands for
		//"Card with RankRepresentation and Symbol"
		//used to decrypt messages sent over the socket

		//figuring out the rank
		int cardNumber = 0;
		switch(rr)
		{
			case "A":
				break;
			case "X":
				cardNumber += 9;
				break;
			case "J":
				cardNumber += 10;
				break;
			case "Q":
				cardNumber += 11;
				break;
			case "K":
				cardNumber += 12;
				break;
			case "B":
				break;
			default:
				cardNumber = Integer.parseInt(rr) - 1;
				break;
		}

		//figuring out the suit
		switch(s)
		{
			case "♣":
				break;
			case "♦":
				cardNumber += 13;
				break;
			case "♥":
				cardNumber += 26;
				break;
			case "♠":
				cardNumber += 39;
				break;
			default:
				cardNumber = 52;
				break;
		}
		return new SCard(cardNumber);
	}

	public static SCard cardWithNumber(int cardNumber)
	{
		String suit;
		String rankString;
		String rankRepresentation;
		String symbol;
		switch (cardNumber % 13)
		{
			case 0:
				rankString =  "Ace";
				rankRepresentation = "A";
				break;
			case 10:
				rankString =  "Jack";
				rankRepresentation = "J";
				break;
			case 11:
				rankString = "Queen";
				rankRepresentation = "Q";
				break;
			case 12:
				rankString = "King";
				rankRepresentation = "K";
				break;
			default:
				rankRepresentation = rankString = "" + (1 + cardNumber % 13);
				break;

		}
		switch (cardNumber / 13)
		{
			case 0:
				suit = "Clubs";
				symbol = "♣";
				break;
			case 1:
				suit = "Diamonds";
				symbol = "♦";
				break;
			case 2:
				suit = "Hearts";
				symbol = "♥";
				break;
			case 3:
				suit = "Spades";
				symbol = "♠";
				break;
			default:
				rankString = "Blank";
				rankRepresentation = "B";
				suit = "Blank";
				symbol = "b";
		}
		return new SCard(suit, rankString, rankRepresentation, symbol, cardNumber);

	}


}
