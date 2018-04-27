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

class Hand
{
	List<Card> hand=new ArrayList<Card>();
	public void addCard(Card c)
	{
		hand.add(c);
	}
	public void displayHand()
	{
		int i;
		for(i=0;i<hand.size();i++)
		{
			System.out.print(i+". "+hand.get(i).getValue()+" ");
			System.out.println(hand.get(i).getColor()+" ");
		}
	}
	public Card playCard(int i)
	{
		return hand.get(i);
	}
	public void removeCardFromHand(int i)
	{
		hand.remove(i);
	}
}

class UnoPlayer
{
	public static void main(String args[])throws IOException,ClassNotFoundException
	{
		Socket s=new Socket("localhost",1233);
		Hand h=new Hand();
		String name,tempCardValue,tempCardColor,tempName,currentColor;
		int ncards,choice,drawFactor,color,i,menu=0;
		Card tempCard,playedCard;
		boolean finished=false;
		ObjectOutputStream oos=new ObjectOutputStream(s.getOutputStream());
		System.out.println("Enter name: ");
		Scanner sc=new Scanner(System.in);
		name=sc.nextLine();
		oos.writeObject(name);
		oos.flush();
		System.out.println("Waiting for Players to Connect...");
		ObjectInputStream ois=new ObjectInputStream(s.getInputStream());
		System.out.println(ois.readObject());
		ncards=(int)ois.readObject();
		System.out.println("Dealing the cards....");
		for(i=0;i<ncards;i++)
		{
			h.addCard((Card)ois.readObject());
		}
		h.displayHand();
		while(!finished) 
		{
			menu=0;
			tempCard=(Card)ois.readObject();
			tempCardValue=tempCard.getValue();
			tempCardColor=tempCard.getColor();
			System.out.println("Top of the Pile is: "+tempCardValue+" "+tempCardColor);
			drawFactor=(int)ois.readObject();
			currentColor=(String)ois.readObject();
			System.out.println("Draw Factor is: "+drawFactor);
			System.out.println("Color is: "+currentColor);
			tempName=(String)ois.readObject();
			if(!tempName.equals(name))
			{
				System.out.println("It's "+tempName+" 's turn...");
			}
			else
			{
				System.out.println("It's your turn...");
					
					while(true)
					{
						h.displayHand();
						if(menu==0)
						{
							System.out.println("Select which card to play or 100 for drawing the card.");
							choice=sc.nextInt();
							System.out.println(choice);
						}
						else
						{
							System.out.println("Select which card to play or 200 for pass.");
							choice=sc.nextInt();
							System.out.println(choice);
						}
						if(choice==100)
						{
							oos.writeObject("DRAWCARD");
							oos.flush();
							for(i=0;i<drawFactor;i++)
							{
								h.addCard((Card)ois.readObject());
							}
							if(drawFactor>1)
								break;
							else
							{
								menu=1;
							}
							continue;
						}
						else if(choice==200)
						{
							oos.writeObject("PASS");
							oos.flush();
							break;
						}
						else
						{
							oos.writeObject("PLAYCARD");
							oos.flush();
							System.out.println("PLAYCARD");
							playedCard=h.playCard(choice);
							oos.writeObject(h.playCard(choice));
							oos.flush();
							if(h.playCard(choice).getColor().equals("ANY"))
							{
								System.out.println("Choose Color: 1. RED 2. GREEN 3. YELLOW 4. BLUE");
								color=sc.nextInt();
								switch(color)
								{
									case 1:oos.writeObject("RED");oos.flush();break;
									case 2:oos.writeObject("GREEN");oos.flush();break;
									case 3:oos.writeObject("YELLOW");oos.flush();break;
									case 4:oos.writeObject("BLUE");oos.flush();break;
									default: System.out.println("Invalid Option.");
								}
							}
							String temp=(String)ois.readObject();
							if(temp.equals("POSITIVE"))
							{
								h.removeCardFromHand(choice);
								if(h.hand.size()==0)
								{
									System.out.println("You Win.");
									finished=true;
									oos.writeObject("WIN");
									oos.flush();
									break;
								}
								else
								{
									oos.writeObject("NOTWIN");
									oos.flush();
								}
							}
							else if(temp.equals("NEGATIVE"))
							{
								System.out.println("Invalid Move.");
								continue;
							}
							if(playedCard.getValue().equals("WILD"))
							{
								continue; 
							}
						}
						break;
					}
					
			}
		}
			
			



			/*else if(ois.readObject().equals("TURN"))
			{
				System.out.println("it's your turn now....");
				tempCard=(Card)ois.readObject();
				tempCardValue=tempCard.getValue();
				tempCardColor=tempCard.getColor();
				System.out.println("This is uppermost card: "tempCardValue+" "+tempCardColor);
				h.displayHand();
				System.out.println("Select which card to play or 100 for drawing the card.");
				choice=sc.nextInt();
				if(choice==100)
				{
					oos.writeObject("DRAWCARD");
					oos.flush();
					if(tempCardValue.equals("DRAW2"))
					{
						h.addCard(ois.readObject());
						h.addCard(ois.readObject());
					}
					else if(tempCardValue.equals("DRAW4"))
					{
						h.addCard(ois.readObject());
						h.addCard(ois.readObject());
						h.addCard(ois.readObject());
						h.addCard(ois.readObject());
					}
					else 
					{
						h.addCard(ois.readObject());
					}
				}
				else
				{
					oos.writeObject("PLAYCARD");
					oos.flush();
					oos.writeObject(h.playCard(choice));
					oos.flush();
				}
			}
		}
		while(true){}
		/*while(true)
		{
			Card temp=(Card)din.readObject();
			System.out.println(temp.getValue()+" "+temp.getColor());
			if(temp.getValue().equals("SKIP"))
			{
				System.out.println("Your turn was skipped.");
			}
			else if(temp.getValue().equals("DRAW2"))
			{
				dout.writeObject(new Card("ANY","DRAW2"));
				h.addCard((Card)din.readObject());
				h.addCard((Card)din.readObject());
			}
			else if(temp.getValue().equals("DRAW4"))
			{
				dout.writeObject(new Card("ANY","DRAW4"));
				h.addCard((Card)din.readObject());
				h.addCard((Card)din.readObject());
				h.addCard((Card)din.readObject());
				h.addCard((Card)din.readObject());
			}
			else
			{
				System.out.println("Enter your choice or 10 for draw");
				int choice=sc.nextInt();
				if(choice!=10)
				{
					Card temp2=h.play(choice);
					dout.writeObject(temp2);
				}
				else
				{
					dout.writeObject(new Card("ANY","DRAW"));
					h.addCard((Card)din.readObject());
				}
				if(din.readObject().equals("POS"))
				{
					h.remove(choice);
				}
			}

		}
		/*System.out.print("You: ");
		String str="",str2="";
		while(!str.equals("stop"))
		{
			str=br.readLine();
			dout.writeUTF(str);
			dout.flush();
			str2=din.readUTF();
			System.out.println("Server says: "+str2);
			System.out.print("You: ");
		}*/
		//System.out.println("Connection Terminated");
		//dout.close();
		//s.close();
	}
}



