import java.net.*;
import java.io.*;
import java.util.*;

class Card implements Serializable
{
	String value;
	String color;
	public Card(String value,String color)
	{
		this.value=value;
		this.color=color;
	}
	public void setValue(String value)
	{
		this.value=value;
	}
	public void setColor(String color)
	{
		this.color=color;
	}
	public String getValue()
	{
		return value;
	}
	public String getColor()
	{
		return color;
	}
}

class Deck
{
	Stack<Card> deck=new Stack<Card>();
	String Colors[]={"RED","BLUE","YELLOW","GREEN"};
	String Values[]={"0","1","2","3","4","5","6","7","8","9","SKIP","REVERSE","DRAW2"};

	public void createDeck()
	{
		for(String color:Colors)
		{
			for(String value:Values)
			{
				deck.push(new Card(value,color));
			}
		}
		deck.push(new Card("DRAW4","ANY"));
		deck.push(new Card("DRAW4","ANY"));
		deck.push(new Card("WILD","ANY"));
		deck.push(new Card("WILD","ANY"));
	}

	public void shuffle(int n)
	{
		int i,j,k;
		Random r=new Random();
		for(k=0;k<n;k++)
		{
			for(i=deck.size()-1;i>=0;i--)
			{
				j=r.nextInt(i+1);
				swap(i,j);
			}
		}
	}

	public void swap(int i,int j)
	{
		Card temp;
		temp=deck.get(i);
		deck.set(i,deck.get(j));
		deck.set(j,temp);
	}

	public Card drawCard()
	{
		return deck.pop();
	}

}

class Pile
{
	Stack<Card> pile=new Stack<Card>();

	public void addToPile(Card c)
	{
		pile.push(c);
	}

	public Card getUpperCard()
	{
		return pile.peek();
	}

	public void shuffle(int n)
	{
		int i,j,k;
		Random r=new Random();
		for(k=0;k<n;k++)
		{
			for(i=pile.size()-1;i>=0;i--)
			{
				j=r.nextInt(i+1);
				swap(i,j);
			}
		}
	}

	public void swap(int i,int j)
	{
		Card temp=pile.get(i);
		pile.set(i,pile.get(j));
		pile.set(j,temp);
	}
}

class Player
{
	String name;
	Socket s;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	public Player(ServerSocket ss) throws IOException
	{
		s=ss.accept();
		oos=new ObjectOutputStream(s.getOutputStream());
		oos.flush();
		ois=new ObjectInputStream(s.getInputStream());
		
	}
	public void setName(String name)
	{
		this.name=name;
	}
	public String getName()
	{
		return name;
	}
	public Socket getSocket()
	{
		return s;
	}
	public ObjectInputStream getInputStream()
	{
		return ois;
	}
	public ObjectOutputStream getOutputStream()
	{
		return oos;
	}
}


class UnoGameServer
{
	static List<Player> players=new ArrayList<Player>();
	
	public static int getTurn(int dir,int turn,int n)
	{
		if(dir==0)
		{
			if(turn==n-1)
				turn=0;
			else
				turn=turn+1;
		}
		else
		{
			if(turn==0)
				turn=n-1;
			else
				turn=turn-1;
		}
		return turn;
	}

	public static void pileToDeck(Pile p,Deck d)
	{
		int i;
		Card c=p.pile.pop();
		for(i=0;i<p.pile.size();i++)
		{
			d.deck.push(p.pile.pop());
		}
		p.pile.push(c);
	}

	public static void main(String args[])throws IOException,ClassNotFoundException
	{
		ServerSocket ss=new ServerSocket(1233);
		Scanner sc=new Scanner(System.in);
		String name,tempCardValue,color;
		Player tempPlayer,currentPlayer;
		Card tempCard,upperCard;
		int i,j,nplayers,ncards,turn=0,dir=0,drawFactor=1;
		Deck d=new Deck();
		Pile p=new Pile();
		boolean finished=false;
		System.out.println("Enter The Number of Players (Max 4)");
		nplayers=sc.nextInt();
		System.out.println("Waiting for the Game Server to be ready...");
		for(i=0;i<nplayers;i++)
		{
			players.add(new Player(ss));
			name=(String)players.get(i).getInputStream().readObject();
			tempPlayer=players.get(i);
			tempPlayer.setName(name);
			System.out.println(tempPlayer.getName()+" Connected.");
		}
		for(i=0;i<nplayers;i++)
		{
			players.get(i).getOutputStream().writeObject("All the players are ready!");
			players.get(i).getOutputStream().flush();
		}
		System.out.println("Players are ready!");
		d.createDeck();
		d.shuffle(5);
		System.out.println("Enter number of cards (Max 8)");
		ncards=sc.nextInt();
		for(i=0;i<nplayers;i++)
		{
			players.get(i).getOutputStream().writeObject(ncards);
			players.get(i).getOutputStream().flush();
		}
		System.out.println("Dealing the cards to the players....");
		//Dealing Cards
		for(i=0;i<ncards;i++)
		{
			for(j=0;j<nplayers;j++)
			{
				players.get(j).getOutputStream().writeObject(d.drawCard());
				players.get(j).getOutputStream().flush();
			}
		}
		while(true)
		{
			System.out.println("Drawing the first card...");
			tempCard=d.drawCard();
			tempCardValue=tempCard.getValue();
			color=tempCard.getColor();
			if(tempCardValue.equals("DRAW2") || tempCardValue.equals("DRAW4") || tempCardValue.equals("WILD")
				|| tempCardValue.equals("REVERSE") || tempCardValue.equals("SKIP"))
			{
				System.out.println(tempCardValue);
				p.addToPile(tempCard);
				System.out.println("Drawing card again...");
			}
			else
				break;
		}	
		p.addToPile(tempCard);
		while(!finished)
		{
			if(d.deck.size()<5)
			{
				pileToDeck(p,d);
				d.shuffle(5);
			}
			tempCard=p.getUpperCard();
			tempCardValue=tempCard.getValue();
			System.out.println("Pile Card is: "+tempCardValue+" "+tempCard.getColor());
			if(tempCardValue.equals("REVERSE"))
			{
				if(dir==0)
				{
					dir=1;
				}
				else
				{
					dir=0;
				}
			}
			turn=getTurn(dir,turn,nplayers);
			currentPlayer=players.get(turn);
			upperCard=p.getUpperCard();
			if(!upperCard.getColor().equals("ANY"))
			{
				color=upperCard.getColor();
			}
			for(i=0;i<nplayers;i++)
			{
				players.get(i).getOutputStream().writeObject(upperCard);
				players.get(i).getOutputStream().flush();
			}
			for(i=0;i<nplayers;i++)
			{
				players.get(i).getOutputStream().writeObject(drawFactor);
				players.get(i).getOutputStream().flush();
			}
			for(i=0;i<nplayers;i++)
			{
				players.get(i).getOutputStream().writeObject(color);
				players.get(i).getOutputStream().flush();
			}
			for(i=0;i<nplayers;i++)
			{
				players.get(i).getOutputStream().writeObject(currentPlayer.getName());
				players.get(i).getOutputStream().flush();
			}
				while(true)
				{
					//System.out.println("PLAYCARD00");
					String temp=(String)currentPlayer.getInputStream().readObject();
					if(temp.equals("DRAWCARD"))
					{
						for(i=0;i<drawFactor;i++)
						{
							currentPlayer.getOutputStream().writeObject(d.drawCard());
							currentPlayer.getOutputStream().flush();
						}
						if(drawFactor>1)
						{
							drawFactor=1;
							break;
						}
						drawFactor=1;
						continue;
					}
					else if(temp.equals("PLAYCARD"))
					{
						//GAME LOGIC
						System.out.println("PLAYCARD");
						tempCard=(Card)currentPlayer.getInputStream().readObject();
						System.out.println(currentPlayer.getName()+" wants to play: "+tempCard.getValue()+" "+tempCard.getColor());
						if(upperCard.getValue().equals(tempCard.getValue()) || 
							color.equals(tempCard.getColor()) ||
							tempCard.getColor().equals("ANY"))
						{
							if(drawFactor>1)
							{

								if(upperCard.getValue().equals("DRAW4"))
								{
									if(tempCard.getValue().equals("DRAW2") || tempCard.getValue().equals("DRAW4"))
									{

									}
								}
								else if(upperCard.getValue().equals("DRAW2"))
								{
									if(tempCard.getValue().equals("DRAW2") || tempCard.getValue().equals("DRAW4"))
									{
										
									}
								}
								else
								{
									currentPlayer.getOutputStream().writeObject("NEGATIVE");
									currentPlayer.getOutputStream().flush();
									continue;
								}
							}
							if(tempCard.getColor().equals("ANY"))
							{
								color=(String)currentPlayer.getInputStream().readObject();
							}
							if(drawFactor==1)
							{
								switch(tempCard.getValue())
								{
									case "DRAW2":drawFactor=2;break;
									case "DRAW4":drawFactor=4;break;
								}
							}
							else
							{
								switch(tempCard.getValue())
								{
									case "DRAW2":drawFactor+=2;break;
									case "DRAW4":drawFactor+=4;break;
								}
							}
							currentPlayer.getOutputStream().writeObject("POSITIVE");
							currentPlayer.getOutputStream().flush();
							temp=(String)currentPlayer.getInputStream().readObject();
							if(tempCard.getValue().equals("WILD"))
							{
								continue;
							}
							p.addToPile(tempCard);
							if(tempCard.getValue().equals("SKIP"))
							{
								System.out.println("Skip: "+currentPlayer.getName());
								if(dir==0)
								{
									if(turn==nplayers-1)
										turn=0;
									else
										turn=turn+1;
								}
								else
								{
									if(turn==0)
										turn=nplayers-1;
									else
										turn=turn-1;
								}
								break;
							}
							if(temp.equals("WIN"))
							{
								finished=true;
								break;
							}
							
						}
						else
						{
							currentPlayer.getOutputStream().writeObject("NEGATIVE");
							currentPlayer.getOutputStream().flush();
							continue;
						}
					}
					else if(temp.equals("PASS"))
					{
						System.out.println("PASS");
						break;
					}
					break;
				}

			tempCard=null;
		}
			

			/*//sendToClient(p,d,currentPlayer);
			if(currentPlayer.getInputStream().readObject().equals("DRAWCARD"))
			{
				tempCard=p.getUpperCard();
				if(tempCard.getValue().equals("DRAW2"))
				{
					//GIVE TWO CARDS TO THE PLAYER
					currentPlayer.getOutputStream().writeObject(d.drawCard());
					currentPlayer.getOutputStream().writeObject(d.drawCard());
				}
				else if(tempCard.getValue().equals("DRAW4"))
				{
					//GIVE FOUR CARDS TO THE PLAYER
					currentPlayer.getOutputStream().writeObject(d.drawCard());
					currentPlayer.getOutputStream().writeObject(d.drawCard());
					currentPlayer.getOutputStream().writeObject(d.drawCard());
					currentPlayer.getOutputStream().writeObject(d.drawCard());
				}
				else
				{
					//GIVE ONE CARDS TO THE PLAYER
					currentPlayer.getOutputStream().writeObject(d.drawCard());
				}
			}
			else if(currentPlayer.getInputStream().readObject().equals("PLAYCARD"))
			{
				tempCard=(Card)currentPlayer.getInputStream().readObject();
				//GAME RULES
				upperCard=p.getUpperCard();
				if(tempCard.getValue().equals(upperCard.getValue()) || tempCard.getColor().equals(upperCard.getColor())
					)
			}

		}
	}*/

	
/*
	public static void sendToClient(Pile p,Deck d,Player player)
	{
		Card c=p.getUpperCard();
		String value=c.getValue();
		String color=c.getColor();
		if(value.equals("SKIP"))
		{
			player.getOutputStream().writeObject("SKIP");
		}
		else
		{
			player.getOutputStream().writeObject("TURN");
			player.getOutputStream().writeObject(c);
		}

	}
}	
		/*int turn=0,dir=0;

		p.addToPile(d.draw());
		while(true)
		{
			turn=getTurn(dir,turn,n);
			send(players.get(turn).getOutputStream());
			Card temp=(Card)players.get(turn).getInputStream().readObject();
			if(temp.getValue().equals(p.pile.peek().getValue()) || temp.getColor().equals(p.pile.peek().getColor()) || temp.getColor().equals("ANY"))
			{
					players.get(turn).getOutputStream().writeObject("POS");
					p.addToPile(temp);0
					if(temp.getValue().equals("DRAW2") || temp.getValue().equals("DRAW4") || temp.getValue().equals("SKIP"))
					{
						if(dir==0)
							turn=turn+1;
						else
							turn=turn-1;
					}
					else if(temp.getValue().equals("REVERSE"))
					{
						if(dir==0)
							dir=1;
						else
							dir=0;
					}
			}
			else if(temp.getValue().equals("ANY"))
			{
				if(temp.getColor().equals("DRAW2"))
				{
					players.get(turn).getOutputStream().writeObject(d.draw());
					players.get(turn).getOutputStream().writeObject(d.draw());
				}
				else if(temp.getColor().equals("DRAW4"))
				{
					players.get(turn).getOutputStream().writeObject(d.draw());
					players.get(turn).getOutputStream().writeObject(d.draw());
					players.get(turn).getOutputStream().writeObject(d.draw());
					players.get(turn).getOutputStream().writeObject(d.draw());
				}
				else
				players.get(turn).getOutputStream().writeObject(d.draw());
			}
		}
		/*while(!win)
		{
			p.addToPile(d.draw());
			turn=getTurn(dir,turn);
			send(player.get(turn).getOutputStream());
			Card temp=(Card)player.get(turn).getInputStream().readObject();
			if(temp.getValue().equals(p.pile.peek().getValue()) || temp.getColor().equals(p.pile.peek().getColor()) || temp.getColor().equals("ANY"))
			{
				player.get(turn).getOutputStream().writeObject("POS");
				if(temp.getValue().equals("DRAW2"))
				{

				}
			}

		}
		/*Socket s=players.get(0).getSocket();
		DataInputStream din=new DataInputStream(s.getInputStream());
		DataOutputStream dout=new DataOutputStream(s.getOutputStream());
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		String str="",str2="";
		while(!str.equals("stop"))
		{
			str=din.readUTF();
			System.out.println("client says: "+str);
			System.out.print("You: ");
			str2=br.readLine();
			dout.writeUTF(str2);
			dout.flush();
		}
		System.out.println("Closing Connection");
		din.close();
		s.close();
		ss.close();*/

		/*public static void send(ObjectOutputStream oos) throws IOException,ClassNotFoundException
	{
		oos.writeObject(p.pile.peek());
	}

	public static int getTurn(int dir,int turn,int n)
	{
		if(dir==0)
		{
			if(turn==n-1)
			{
				turn=0;
			}
			else
			{
				turn=turn+1;
			}
			
		}
		else
		{
			if(turn==0)
			{
				turn=n-1;
			}
			else
			{
				turn=turn-1;
			}
		}
		return turn;
	}*/
	}
}