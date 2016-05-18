# spoons


## Disclaimer:

Enjoy playing Spoons, now in version 1.0
You shouldn't need to, but in the case of compile errors
that look like unrecogizable characters, try compiling by
specifying the UTF-8 encoding:

'javac -encoding "UTF-8" *.java'


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
