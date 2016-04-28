/*
SCard.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V0.4: Players are handed an initial starting hand
The rest of the deck is stored on the server
Created Deck and Card classes
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
		this(0);
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

	public static SCard decrypt(String rr, String s)
	{
		//just an easier name to remember than cardWithRRandSym()
		return SCard.cardWithRRandSym(rr, s);
	}

	public String toString()
	{
		return rankString + " of " + suit;
	}

	public ArrayList<String> toPrettyString()
	{
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("┌─────────┐");
        lines.add(String.format("│%2s       │", rankRepresentation));
        lines.add("│         │");
        lines.add("│         │");
        lines.add(String.format("│    %1s    │", symbol));
        lines.add("│         │");
        lines.add("│         │");
        lines.add(String.format("│       %2s│", rankRepresentation));
        lines.add("└─────────┘");
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
			default:
				cardNumber += 39;
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
			default:
				suit = "Spades";
				symbol = "♠";
				break;
		}
		return new SCard(suit, rankString, rankRepresentation, symbol, cardNumber);

	}


}