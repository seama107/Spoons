# Spoons

### Enjoy playing Spoons, now in version 1.0


## Compiling & Installation

No extra softwares are need aside from JVM and Java compiler.
All files should compile with 'javac *.java'

You shouldn't need to, but in the case of compile errors
that look like unrecogizable characters, try compiling by
specifying the UTF-8 encoding:

'javac -encoding "UTF-8" *.java'

This problem comes from the available charactersets on some systems being slightly
different.

## Playing the game

Start the server ("java SServer") and specify the options when prompted. Then each individual player
can start their own client ("java SClient") and join in the game. 

Controls will be specified upon game-start, but just for reference:

Display help menu: 'h'

Quit: 'q'

Draw card from draw pile: 'd'

Swap the card you drew with a card in your hand: 's#'
Where '#' is the integer position of the card you want to swap
Ex: 's3' swaps the drawn card with the 3rd card in your hand.

Pass a drawn card to the next person: 'p'

Take a spoon: 't#'
Where '#' is the spoon number
Ex: 't1' takes the 1st spoon if it's available.


If you don't know the rules of the game spoons, read up on them here:
http://www.grandparents.com/grandkids/activities-games-and-crafts/spoons




## Version History

V0.1:
Established communication between Server and Client

V0.2:
Created a listener Thread for the client

V0.3:
The Game now starts properly
Created the Player and RichMessage classes

V0.4: Players are handed an initial starting hand
The rest of the deck is stored on the server
Created Deck and Card classes

ASCII cards idea used from:
http://codereview.stackexchange.com/questions/82103/ascii-fication-of-playing-cards

V0.5: Game mechanics implemented: Swapping, drawing, passing, and spoon taking
Interface for client implemented
extended card encryption
SGameBoard.java created

V0.6 Game mechanics implemented: Game finishing naturally, spoon logic
Moved away from ASCII
Resized gameboard window

V1.0 Full game implemented
Added option to keep server online
Resized gameboard window (again)
