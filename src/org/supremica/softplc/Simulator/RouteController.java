package org.supremica.softplc.Simulator;

import java.util.*;
import javax.swing.*;
import java.lang.*;

// skall byta namn till RouteController???

/**
 * @author Anders Röding
 * @author Henrik Staberg
 * @author Andreas Herner
 */
public class RouteController
	implements Runnable
{
	private static final int nrOfLegs = 14;
	private static final Movement[] movements =
	{
		/* movement[0]  = */
		new Movement(new int[]{ 98, 314 }, new int[]{ 116, 316 }, 12),
		/* movement[1]  = */
		new Movement(new int[]{ 116, 316 }, new int[]{ 285, 329 }, 50),
		/* movement[2]  = */
		new Movement(new int[]{ 285, 328 }, new int[]{ 285, 251 }, 15),
		/* movement[3]  = */
		new Movement(new int[]{ 285, 251 }, new int[]{ 435, 266 }, 25),
		/* movement[4]  = */
		new Movement(new int[]{ 431, 266 }, new int[]{ 431, 123 }, 25),
		/* movement[5]  = */
		new Movement(new int[]{ 431, 123 }, new int[]{ 431, 62 }, 30),
		/* movement[6]  = */
		new Movement(new int[]{ 431, 127 }, new int[]{ 230, 149 }, 30),
		/* movement[7]  = */
		new Movement(new int[]{ 431, 62 }, new int[]{ 230, 78 }, 30),
		/* movement[8]  = */
		new Movement(new int[]{ 230, 149 }, new int[]{ 48, 149 }, 35),
		/* movement[9]  = */
		new Movement(new int[]{ 230, 78 }, new int[]{ 48, 78 }, 35),
		/* movement[10] = */
		new Movement(new int[]{ 48, 78 }, new int[]{ 48, 176 }, 35),
		/* movement[11] = */
		new Movement(new int[]{ 48, 149 }, new int[]{ 48, 176 }, 10),
		/* movement[12] = */
		new Movement(new int[]{ 48, 176 }, new int[]{ 48, 310 }, 15),
		/* movement[13] = */
		new Movement(new int[]{ 48, 310 }, new int[]{ 98, 314 }, 25)
	};

	public int getParts(int leg)
	{
		return movements[leg].parts;
	}

	public int getHori(int leg)
    {
		return (movements[leg].endPos[0] - movements[leg].startPos[0]);
	}

	public int getVert(int leg)
	{
		return (movements[leg].endPos[1] - movements[leg].startPos[1]);
	}

	// inputs to simulator [16]
	private boolean[] inSignals = new boolean[16];

	// outputs from simulator [23]
	private boolean[] outSignals = new boolean[23];

	// keeps track of which balls are in each leg
	private List[] legBallList = new List[nrOfLegs];

	/**takes a step
	 */

	// 0 <=> down/left
	private int matlyftLevel = 0;      // range 0 - movement[2].parts
	private int hissLevel = 0;        // range 0 - (movement[4].parts + movement[5].parts)
	private int armLevel = 0;        // range 0 - movement[10].parts
	private int armAngle = 0;       // range 0 - movement[8].parts (or movement[9].parts)


	/*-----------------------------------------
	  -----------------------------------------*/
	public int matlyftLevel()
	{
		return matlyftLevel;
	}

	public int hissLevel()
	{
		return hissLevel;
	}

	public int armLevel()
	{
		return armLevel;
    }

	public int armAngle()
	{
		return armAngle;
    }

	/*-----------------------------------------
	  -----------------------------------------*/

	private BTSim signals;
	private static final int framesize = 500;
	BallTrackView view = new BallTrackView(framesize, this);

	public void addSmallBall()
	{
		Ball ball = new Ball(1);

		// b.setVisible(false); //should be false when not testing
		ball.moveInit(movements[12].startPos, movements[12].endPos, movements[12].move);
		legBallList[12].add(ball);
	}

	public void addLargeBall()
	{
		Ball ball = new Ball(2);

		// b.setVisible(false); //should be false when not testing
		ball.moveInit(movements[12].startPos, movements[12].endPos, movements[12].move);
		legBallList[12].add(ball);
	}

	public void delBall()
	{
		if (!legBallList[13].isEmpty())
		{
			Ball ball = (Ball) legBallList[13].get(0);

			if (ball.finishedLeg())
			{
				((LinkedList) legBallList[13]).removeFirst();
			}
		}
	}

	public List getAllBalls()
	{
		LinkedList list = new LinkedList();

		for (int j = 0; j < nrOfLegs; j++)
		{
			for (Iterator i = legBallList[j].iterator(); i.hasNext(); )
			{
				list.add(i.next());
			}
		}

		return list;
	}

	/**Lite kommentar kanske ;)
 *
 */
	public RouteController(boolean[] outs, BTSim sigs)    // BallTrackSimulator()
	{
		for (int i = 0; i < legBallList.length; i++)
		{
			legBallList[i] = new LinkedList();
		}

		System.arraycopy(outs, 0, outSignals, 0, outSignals.length);

		signals = sigs;

		this.addLargeBall();
	}

	/**run is the method run by the thread
		 * It simply makes the simulator run*/
	public void run()
	{
		while (true)
		{
			try
			{
				step();
			}
			catch (Exception e)
			{
				System.err.println(e + "345");
			}

			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e) {}
		}
	}

	/**getInSignal returns specified insignal
	 * from the array of all insignals*/
    public boolean getInSignal(int sig)
    {
		inSignals = signals.getInSignals();
		return inSignals[sig];
    }

	private void step()
		throws Exception
	{
		inSignals = signals.getInSignals();

		/* Set Countervalues */
		/* MatLyftlevel */

		// UppMätLyft
		if (inSignals[2])
		{
			if (matlyftLevel < movements[2].parts)
			{
				matlyftLevel++;
			}
		}
		else
		{
			if (matlyftLevel > 0)
			{
				matlyftLevel--;
			}
		}

		/* HissLevel */

		// UppHissVån1 && UppHissVån2
		if (inSignals[5] && inSignals[6])
		{
			if (hissLevel < (movements[4].parts + movements[5].parts))
			{
				hissLevel++;
			}
		}
		else
		{
			if (inSignals[5])
			{
				if (hissLevel < movements[4].parts)
				{
					hissLevel++;
				}
				else if (hissLevel > movements[4].parts)
				{
					hissLevel--;
				}
			}
			else
			{
				if (hissLevel > 0)
				{
					hissLevel--;
				}
			}
		}

		/* ArmLevel */

		// UppArmVån1 && UppArmVån2
		if (armAngle == 0)
		{
			if (inSignals[11] && inSignals[12])
			{
				if (armLevel < (movements[10].parts))
				{
					armLevel++;
				}
			}
			else if (inSignals[11])
			{
				if (armLevel < movements[11].parts)
				{
					armLevel++;
				}
				else if (armLevel > movements[11].parts)
				{
					armLevel--;
				}
			}
			else if (armLevel > 0)
			{
				armLevel--;
			}
		}

		/* ArmAngle */

		// VridArmHöger
		if (armLevel == 10)
		{
			if (inSignals[13] && armAngle == 35)
			{
				//null, nothing has to be done
			}
			else if (inSignals[13] && inSignals[11] &&
					 !inSignals[12] && armAngle < movements[8].parts)
			{
				armAngle++;
			}
			else if (armAngle > 0)
			{
				armAngle--;
			}
		}
		else if (armLevel == 35)
		{
			if (inSignals[13] && armAngle == 35)
			{
				//null, nothing has to be done
			}
			else if (inSignals[13] && inSignals[12] &&
					 armAngle < movements[9].parts)
			{
				armAngle++;
			}
			else if (armAngle > 0)
			{
				armAngle--;
			}
		}

		System.out.println("MatlyftLevel: " + matlyftLevel);
		System.out.println("HissLevel   : " + hissLevel);
		System.out.println("ArmLevel    : " + armLevel);
		System.out.println("ArmAngle    : " + armAngle);
		System.out.println("");

		// -----------------------------------

		// leg 13
		if (!legBallList[13].isEmpty())
		{
			moveBallsInLeg(13);

			Ball b = (Ball) legBallList[13].get(0);

			if (b.finishedLeg() && move_13_0())
			{
				b.moveInit(movements[0].startPos, movements[0].endPos, movements[0].move);
			}
		}

		// leg 12
		if (!legBallList[12].isEmpty())
		{
			moveBallsInLeg(12);

			Ball b = (Ball) legBallList[12].get(0);

			if (b.finishedLeg() && move_12_13())
			{
				b.moveInit(movements[13].startPos, movements[13].endPos, movements[13].move);
				b.setVisible(true);
			}
		}

		// leg 11
		if (!legBallList[11].isEmpty())
		{
			if (!inSignals[14] && ((armLevel != 0) || (armAngle != 0)))    // Sug error
			{
				throw new Exception("Ball dropped (11)");
			}

			if (inSignals[12] || ((hissLevel != movements[11].parts) && inSignals[11]))
			{
				throw new Exception("Cannot go up before leaving ball (11)");
			}

			if (armAngle != 0)
			{
				throw new Exception("Cannot turn right before leaving ball (11)");
			}

			moveBallsInLeg(11);

			Ball b = (Ball) legBallList[11].get(0);

			if (b.finishedLeg() && move_11_12())
			{
				b.moveInit(movements[12].startPos, movements[12].endPos, movements[12].move);
				b.setVisible(false);
			}
		}

		// leg 10
		if (!legBallList[10].isEmpty())
		{
			if (!inSignals[14] &&!outSignals[15])    // Sug error
			{
				throw new Exception("Ball dropped (10)");
			}

			if ((inSignals[12] && inSignals[11] && (hissLevel != movements[10].parts)) ||
				((inSignals[12] || inSignals[11]) && (hissLevel < movements[11].parts)))
			{
				throw new Exception("Cannot go up before leaving ball (10)");
			}

			if (armAngle != 0)
			{
				throw new Exception("Cannot turn right before leaving ball (10)");
			}

			moveBallsInLeg(10);

			Ball b = (Ball) legBallList[10].get(0);

			if (b.finishedLeg() && move_10_12())
			{
				b.moveInit(movements[12].startPos, movements[12].endPos, movements[12].move);
				b.setVisible(false);
			}
		}

		//  // leg 9
//  		if (!legBallList[9].isEmpty())
//  		{
//  			if (!inSignals[14])
//  			{
//  				throw new Exception("Ball dropped (9)");
//  				//Dialogruta och Exit
//  			}

//  			Ball b = (Ball) legBallList[9].get(0);

//  			if (inSignals[13] && (armAngle != movements[9].parts))
//  			{
//  				//throw new Exception("Cannot turn right before leaving ball (9)");
//  				int x = b.getPosition()[0];
//  				int y = b.getPosition()[1];
//  				distance = getHori(9)/getParts(9);
//  				b.setCoord(x, y);
//  			}

//  			//b.allowMove2(!inSignals[13]);
//  			//moveBallsInLeg(9);

//  			if ((!inSignals[11] ||!inSignals[12]) &&!b.finishedLeg())
//  			{
//  				throw new Exception("Cannot go down until arm is back to the left (9)");
//  			}

//  			if (b.finishedLeg() && move_9_10())
//  			{
//  				b.moveInit(movements[10].startPos, movements[10].endPos, movements[10].move);
//  			}
//  		}


		// leg 9
		if (!legBallList[9].isEmpty())
		{
			if (!inSignals[14])
			{
				throw new Exception("Ball dropped (9)");
			}

			if (inSignals[13] && (armAngle != movements[9].parts))
			{
				throw new Exception("Cannot turn right before leaving ball (9)");
			}
			Ball b = (Ball) legBallList[9].get(0);
			b.allowMove2(!inSignals[13]);
			moveBallsInLeg(9);

			if ((!inSignals[11] ||!inSignals[12]) &&!b.finishedLeg())
			{
				throw new Exception("Cannot go down until arm is back to the left (9)");
			}

			if (b.finishedLeg() && move_9_10())
			{
				b.moveInit(movements[10].startPos, movements[10].endPos, movements[10].move);
			}
		}

		// leg 8
		if (!legBallList[8].isEmpty())
		{
			if (!inSignals[14])
			{
				throw new Exception("Ball dropped (8)");
			}

			if (inSignals[13] && (armAngle != movements[9].parts))
			{
				throw new Exception("Cannot turn right before leaving ball (8)");
			}

			if (!inSignals[11] && (armAngle != 0))    // | inSignals[12])
			{
				throw new Exception("Cannot go down until arm is back to the left (8)");
			}
			Ball b = (Ball) legBallList[8].get(0);
			b.allowMove2(!inSignals[13]);
			moveBallsInLeg(8);

			if (b.finishedLeg() && move_8_11())
			{
				b.moveInit(movements[11].startPos, movements[11].endPos, movements[11].move);
			}
		}

		// i leg 6-7 måste vi ta hand om att Lyftarna måste ner mellan varje överflytt
		// till nästa leg
		// leg 7
		if (!legBallList[7].isEmpty())
		{
			moveBallsInLeg(7);

			Ball b = (Ball) legBallList[7].get(0);

			if (b.finishedLeg() && move_7_9())
			{
				//b.allowMove2 =
				b.moveInit(movements[9].startPos, movements[9].endPos, movements[9].move);
			}
		}

		// leg 6
		if (!legBallList[6].isEmpty())
		{
			moveBallsInLeg(6);

			Ball b = (Ball) legBallList[6].get(0);

			if (b.finishedLeg() && move_6_8())
			{
				b.moveInit(movements[8].startPos, movements[8].endPos, movements[8].move);
			}
		}

		// leg 5
		if (!legBallList[5].isEmpty())
		{
			if ((!inSignals[5] ||!inSignals[6]) && (hissLevel != 0))
			{
				throw new Exception("Cannot go down before leaving ball (5)");
			}

			if (inSignals[7] || (inSignals[9] && (hissLevel > (movements[4].parts + movements[5].parts - 7)) && (hissLevel != (movements[4].parts + movements[5].parts))))
			{
				throw new Exception("Error in handling of UtVån1 or UtVån2 (5)");
			}

			moveBallsInLeg(5);

			Ball b = (Ball) legBallList[5].get(0);

			if (b.finishedLeg() && move_5_7())
			{
				b.moveInit(movements[7].startPos, movements[7].endPos, movements[7].move);
			}
		}

		/*
		// leg 4
		if (!legBallList[4].isEmpty())
		{
			Ball b = (Ball) legBallList[2].get(0);

			//The ball will hit the UtVan1 bar
			if ( (hissLevel == (movements[4].parts - 2) && inSignals[5] && inSignals[7]) ||
				 (hissLevel > (movements[4].parts) && inSignals[7]) )
			{
				Exception e = new Exception("Error in handling of UtVan 1 (4)");
				System.err.println(e);
				JOptionPane.showMessageDialog(null,"Error in handling of  UtVan 1 (4), program will be ended!");
				System.exit(-1);
   			}
			//The ball will hit the UtVan2 bar
			else if (hissLevel == ( (movements[4].parts + movements[5].parts) - 2) &&
					 inSignals[5] && inSignals[6] && inSignals[7])
			{
				Exception e = new Exception("Error in handling of UtVan 2 (5)");
				System.err.println(e);
				JOptionPane.showMessageDialog(null,"Error in handling of  UtVan 2 (5), program will be ended!");
				System.exit(-1);
   			}
			else
			{
			int x       = b.getPosition()[0];
			int lowerY  = 265;
			int y       = lowerY - hissLevel - (new Float(hissLevel/2).intValue());

			b.setCoord(x, y);
			}
			}*/

		//leg4
		if (!legBallList[4].isEmpty())      // REMOVE when NOT used anymore!
		{
			if (!inSignals[5] && (hissLevel != 0))
			{
				throw new Exception("Cannot go down before leaving ball (4)");
			}

			if (inSignals[7] && (hissLevel > (movements[4].parts - 7)) && (hissLevel != movements[4].parts))
			{
				throw new Exception("Error in handling of UtVån1 (4)");
			}

			moveBallsInLeg(4);

			Ball b = (Ball) legBallList[4].get(0);

			if (b.finishedLeg() && move_4_6())
			{
				b.moveInit(movements[6].startPos, movements[6].endPos, movements[6].move);
			}
			else if (b.finishedLeg() && move_4_5())
			{
				b.moveInit(movements[5].startPos, movements[5].endPos, movements[5].move);
			}
		}

		// leg 3
		if (!legBallList[3].isEmpty())
		{
			moveBallsInLeg(3);

			Ball b = (Ball) legBallList[3].get(0);

			if (b.finishedLeg() && move_3_4())
			{
				b.moveInit(movements[4].startPos, movements[4].endPos, movements[4].move);
			}
		}

		// leg 2
		int lowerY = 327;

		if (!legBallList[2].isEmpty())
		{
			Ball b = (Ball) legBallList[2].get(0);

			//The ball will hit the urMatning bar
			if (matlyftLevel == (movements[2].parts - 2) && inSignals[2] && inSignals[3])
			{
				Exception e = new Exception("Error in handling of UrMätning (2)");
				System.err.println(e);
				JOptionPane.showMessageDialog(null,"Error in handling of UrMätning (2), program will be ended!");
				System.exit(-1);
				//throw new Exception("Error in handling of UrMätning (2)");
   			}
			else if (matlyftLevel == movements[2].parts && inSignals[3] && move_2_3())
			{
				b.moveInit(movements[3].startPos, movements[3].endPos, movements[3].move);
			}
			else
			{
				int x = b.getPosition()[0];

				int parts         = getParts(2);
				int vert          = getVert(2);
  				float partLength  = (matlyftLevel * (vert/parts));
  				int partLengthInt = new Float(partLength).intValue();
				int y             = lowerY + partLengthInt;
				b.setCoord(x, y);
			}
		}

		/*if (!legBallList[2].isEmpty())      REMOVE when NOT used anymore!
		{
			if (!inSignals[2] && (matlyftLevel != 0))
			{
				throw new Exception("Cannot go down before leaving ball (2)");
			}

			if (inSignals[3] && (matlyftLevel > (movements[2].parts - 7)) && (matlyftLevel != movements[2].parts))
			{
				throw new Exception("Error in handling of UrMätning (2)");
			}

			moveBallsInLeg(2);

			Ball b = (Ball) legBallList[2].get(0);

			if (b.finishedLeg() && move_2_3())
			{
				b.moveInit(movements[3].startPos, movements[3].endPos, movements[3].move);
			}
			}*/

		// leg 1
		if (!legBallList[1].isEmpty())
		{
			moveBallsInLeg(1);

			Ball b = (Ball) legBallList[1].get(0);

			if (b.finishedLeg() && move_1_2())
			{
				b.moveInit(movements[2].startPos, movements[2].endPos, movements[2].move);
			}
		}

		// leg 0
		if (!legBallList[0].isEmpty())
		{
			moveBallsInLeg(0);

			Ball b = (Ball) legBallList[0].get(0);

			if (b.finishedLeg() && move_0_1())
			{
				b.moveInit(movements[1].startPos, movements[1].endPos, movements[1].move);
			}
		}

		view.repaint();
		setOutSignals();    // set all outsignals
		signals.setOutSignals(outSignals);
	}

	// --------------------------------
	private void moveBallsInLeg(int index)
	{
		for (Iterator i = legBallList[index].iterator(); i.hasNext(); )
		{
			Ball b = (Ball) i.next();

			b.move();
		}
	}

	private boolean move_13_0()
	{
		if (inSignals[0] && legBallList[0].isEmpty())
		{
			legBallList[0].add(((LinkedList) legBallList[13]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_0_1()
	{
		if (inSignals[1])
		{
			legBallList[1].add(((LinkedList) legBallList[0]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_1_2()
		throws Exception
	{
		if (outSignals[1])// && !outSignals[2])
		{
			if (inSignals[2])
			{
				legBallList[2].add(((LinkedList) legBallList[1]).removeFirst());

				return true;
			}
		}

		if (matlyftLevel != 0)
		{
			throw new Exception("Elevator not down (1-2)");
			//JOptionPane.showMessageDialog(null,"Elevator not down (1-2)");
			//System.exit(1);
			//BallTrackView.showMessage("Fel! Elevator not down (1 2)");
		}

		return false;
	}

	private boolean move_2_3()
		throws Exception
	{
		if (outSignals[3] && outSignals[4] && inSignals[3])
		{
			if (inSignals[4])
			{
				throw new Exception("Error using Mät (2)");
			}

			legBallList[3].add(((LinkedList) legBallList[2]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_3_4()
		throws Exception
	{
		if (outSignals[7] &&!outSignals[8])
		{
			if (inSignals[5])
			{
				legBallList[4].add(((LinkedList) legBallList[3]).removeFirst());

				return true;
			}
		}

		if (hissLevel != 0)
		{
			throw new Exception("Elevator not down (3-4)");
		}

		return false;
	}

	private boolean move_4_5()
	{

		// HissVån1 && KulaVån1 && UppHissVån1 && UppHissVån2
		if (outSignals[9] && outSignals[10] && inSignals[5] && inSignals[6])
		{
			legBallList[5].add(((LinkedList) legBallList[4]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_4_6()
	{

		// HissVån1 & KulaVån1 & UtVån1
		if (outSignals[9] && outSignals[10] && inSignals[7])
		{
			legBallList[6].add(((LinkedList) legBallList[4]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_5_7()
	{

		// HissVån2 & KulaVån2 & UtVån2
		if (outSignals[12] && outSignals[13] && inSignals[9])
		{
			legBallList[7].add(((LinkedList) legBallList[5]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_6_8()
		throws Exception

	// Vad händer om armen är i rätt läge och har en
	// kula fastsugen och man försöker knuffa upp en
	// till?
	{

		// ArmVån1 && Sug && LyftVån1
		if (outSignals[16] && inSignals[14] && inSignals[8])
		{
			System.out.println("tttttttttt");

			if (outSignals[18])
			{
				throw new Exception("Never two balls in the arm (6)");
			}

			legBallList[8].add(((LinkedList) legBallList[6]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_7_9()
		throws Exception
	{

		// ArmVån2 && Sug && LyftVån2
		if (outSignals[17] && inSignals[14] && inSignals[10])
		{
			if (outSignals[18])
			{
				throw new Exception("Never two balls in the arm (7)");
			}

			legBallList[9].add(((LinkedList) legBallList[7]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_8_11()
	{

		// !ArmHöger && Sug && ! UppArmVån1 && ! UppArmVån2
		if (!inSignals[13] && inSignals[14] &&!inSignals[11] &&!inSignals[12])
		{
			legBallList[11].add(((LinkedList) legBallList[8]).removeFirst());

			return true;
		}

		// exception
		return false;
	}

	private boolean move_9_10()
	{

		// !ArmHöger && Sug && ! UppArmVån1 && ! UppArmVån2
		if (!inSignals[13] && inSignals[14] &&!inSignals[11] &&!inSignals[12])
		{
			legBallList[10].add(((LinkedList) legBallList[9]).removeFirst());

			return true;
		}

		// exception
		return false;
	}

	private boolean move_10_12()
	{

		// !Sug
		if (!inSignals[14])
		{
			legBallList[12].add(((LinkedList) legBallList[10]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_11_12()
	{

		// !Sug
		if (!inSignals[14])
		{
			legBallList[12].add(((LinkedList) legBallList[11]).removeFirst());

			return true;
		}

		return false;
	}

	private boolean move_12_13()
	{
		legBallList[13].add(((LinkedList) legBallList[12]).removeFirst());

		return true;
	}

	private void setOutSignals()    // should comments be in english???
	{
		outSignals[0] = !legBallList[0].isEmpty();                     // KulaPortvakt
		outSignals[1] = matlyftLevel == 0;                            // MätlyftNere
		outSignals[2] = !legBallList[2].isEmpty();                   // KulaMätlyft
		//outSignals[2] = (matlyftLevel == 0) && !legBallList[2].isEmpty();    // KulaMätlyft
		//outSignals[2] = (matlyftLevel == 0) && legBallList[2].isEmpty() && ((Ball) ((LinkedList) legBallList[1]).getFirst()).finishedLeg();
		outSignals[3] = matlyftLevel == movements[2].parts;            // MätlyftUppe
		outSignals[4] = outSignals[3] &&!legBallList[2].isEmpty();    // KulaMätstation
		outSignals[5] = inSignals[4] && outSignals[4] && ((Ball) (((LinkedList) legBallList[2]).getFirst())).getRadius() == Ball.BIG_BALL;    // StorKula
		outSignals[6] = inSignals[4] && outSignals[4] && ((Ball) (((LinkedList) legBallList[2]).getFirst())).getRadius() == Ball.SMALL_BALL;    // LitenKula
		outSignals[7] = hissLevel == 0;                               // HissNere
		outSignals[8] = (outSignals[7] && !legBallList[4].isEmpty());// || KulaHiss
			//(!legBallList[3].isEmpty() &&
			//((Ball) ((LinkedList) legBallList[3]).getFirst()).finishedLeg()); //KulaHiss
		outSignals[9] = hissLevel == movements[4].parts;                // HissVån1
		outSignals[10] = outSignals[9] &&!legBallList[4].isEmpty();    // KulaVån1
		outSignals[11] = !legBallList[6].isEmpty() && ((Ball) ((LinkedList) legBallList[6]).getFirst()).finishedLeg();    // PlockaVån1
		outSignals[12] = hissLevel == (movements[4].parts + movements[5].parts);    // HissVån2
		outSignals[13] = outSignals[12] &&!legBallList[5].isEmpty();               // KulaVån2
		outSignals[14] = !legBallList[7].isEmpty() && ((Ball) ((LinkedList) legBallList[7]).getFirst()).finishedLeg();    // PlockaVån2
		outSignals[15] = (armLevel == 0) && (armAngle == 0);          // ArmHemma
		outSignals[16] = (armLevel == movements[11].parts) && (armAngle == movements[9].parts);    // ArmVån1
		outSignals[17] = (armLevel == movements[10].parts) && (armAngle == movements[9].parts);    // ArmVån2
		outSignals[18] = inSignals[14] &&!(legBallList[8].isEmpty() && legBallList[9].isEmpty() && legBallList[10].isEmpty() && legBallList[11].isEmpty());    // KulaFast
		outSignals[19] = false;                                       // AutoStart
		outSignals[20] = false;                                       // ManuellStart
		outSignals[21] = false;                                       // NödStopp
		outSignals[22] = false;                                       // LarmKvittering
	}
}
