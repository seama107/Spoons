/*
SGameBoard.java
Author: Michael Seaman

The server for the "Spoons" Final Project
V1.0 Full game implemented
Added option to keep server online
Resized gameboard window (again)


Spoon art by joan stark, at:
"http://www.oocities.org/spunk1111/small.htm"

*/

import java.util.Arrays;
import java.util.ArrayList;

public class SGameBoard
{


	public SDeck hand;
	public SCard nextCard;
	public int drawPileSize;
	public boolean[] spoonsLeft;

	public SGameBoard()
	{
		hand = new SDeck();
		nextCard = new SCard();
		drawPileSize = 0;
		spoonsLeft = new boolean[6];
		Arrays.fill(spoonsLeft, true);
	}

	public SGameBoard(String gameData)
	{
		String[] gameDataSplit = gameData.split(";");

		hand = SDeck.decrypt(gameDataSplit[0]);

		nextCard = new SCard();
		if(!(gameDataSplit[1].isEmpty()))
		{
			nextCard = new SCard(gameDataSplit[1].substring(0,1), gameDataSplit[1].substring(1,2));
		}


		drawPileSize = 0;
		if(!(gameDataSplit[2].isEmpty()))
		{
			drawPileSize = Integer.parseInt(gameDataSplit[2]);
		}


		spoonsLeft = new boolean[6];
		for (int i = 0; i < gameDataSplit[3].length(); i++)
		{
			spoonsLeft[i] = gameDataSplit[3].charAt(i) == 't';
		}
	}


	public void printBoard()
	{

		ArrayList<String> handPrettyPrint = hand.toStringInline();
		ArrayList<String> nextCardPrettyPrint = nextCard.toPrettyString();
		ArrayList<String> spoon = new ArrayList<String>(Arrays.asList(" _______   .==. ","[_______>c(( X )","           '==' "));

		if(nextCard.equals(new SCard()))
		{
			nextCardPrettyPrint = new ArrayList<String>(Arrays.asList("", "", "", "", "", "", "", "", ""));
		}

		String[] output = new String[16];
		String firstLineText =  "Cards in your draw Pile: ";
		String[] secondLineText = { "Your Cards:", "Next Card:", "Spoons:"};
		output[0] = String.format("╔%76s╗", " ").replaceAll(" ", "═" );
		output[1] = String.format("║%31s%-2d%43s║", firstLineText, drawPileSize, " ");
		output[2] = String.format("║%-31s%-16s%-29s║", secondLineText[0], secondLineText[1], secondLineText[2]);

		for (int i = 0; i < 3; ++i)
		{
			boolean leftSpoon = spoonsLeft[2*i];
			boolean rightSpoon = spoonsLeft[2*i + 1];

			for (int j = 0;j < 3; ++j) 
			{
				String currentHandString = handPrettyPrint.get(3*i + j);
				String currentNextCardString = nextCardPrettyPrint.get(3*i + j);
				String currentLeftSpoonString = "";
				String currentRightSpoonString = "";
				if(leftSpoon)
				{
					currentLeftSpoonString = spoon.get(j).replaceAll("X", "" + (2*i + 1));
				}
				if(rightSpoon)
				{
					currentRightSpoonString = spoon.get(j).replaceAll("X", "" + (2*i + 2));
				}
				output[(3*i + j)+3] = String.format("%36s%10s%-16s%-16s", currentHandString, currentNextCardString,
				 currentLeftSpoonString, currentRightSpoonString);
			}

		}

		output[12] = String.format("║%17s%21s%21s%17s║", " ", "Press 'h' for help", "Press 'q' to quit", " ");
		output[13] = String.format("╠%76s╣", " ").replaceAll(" ", "═" );
		output[14] = String.format("║ Controls: %10s%15s%15s%20s     ║", "Draw: 'd'", "Pass: 'p'", "Swap: 's#'", "Take Spoon: 't#'");
		output[15] = String.format("╚%76s╝", " ").replaceAll(" ", "═" );
		for (String line :output)
		{
			System.out.println(line);
		}
	}

}
