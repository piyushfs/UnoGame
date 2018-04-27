# UnoGame

This is a command line based socket programming project developed using java.

You will need latest JDK to be installed on your machine.

Multiple players can connect to the server to play the UNO game.

Always compile and run UnoGameServer.java first and enter the number of players to be connected.

Then compile and run UnoPlayer.java as many times as number of players and start playing!

NOTE: If both the programs i.e UnoGameServer and UnoPlayer are running on same machines then no need to make any changes to the code.
If both the programs are running on different machines then:
1. Make sure the machines are connected to same network.
2. Get ip address of the machine running UnoGameServer.
3. Put this ip address on line 66 of UnoPlayer. For eg. if ip is 192.123.45.46 then line 62 will be:
Socket s=new Socket("192.123.45.46",1233);
4. If the programs are compiling successfully but for some reason cannot proceed further then check the firewall of all the machines participating.

Have Fun!

Creator: Piyush Patil
