package org.supremica.softplc.Simulator;

import java.util.*;
import javax.swing.*;
import java.lang.*;

/**
 * @author Anders Röding
 * @author Henrik Staberg
 * @author Andreas Herner
 */
public class RouteController
	implements Runnable
{
	private static final int nrOfLegs = 14;
	public static final Movement[] movements =
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
		new Movement(new int[]{ 431, 266 }, new int[]{ 431, 124 }, 25),
		/* movement[5]  = */
		new Movement(new int[]{ 431, 124 }, new int[]{ 431, 62 }, 30),
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
		new Movement(new int[]{ 48, 150 }, new int[]{ 48, 176 }, 10),
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

    // inputs to simulator [18] -- 2 signals are not used ([8],[9])
    private boolean[] inSignals = new boolean[18];
    
    // outputs from simulator [27] -- 4 signals are not used ([8],[9],[18],[19])
    private boolean[] outSignals = new boolean[27];

    //used when buttons are pressed on the simulator (manual start, auto start etc) 
    public void setOutSignals(int signal, boolean value)
    {
	outSignals[signal] = value;
    }

	// keeps track of which balls are in each leg
	private List[] legBallList = new List[nrOfLegs];

	/**takes a step
	 */

	//parts is the vertical or horisontal differences
	// range 0 - movement[2].parts
	private int matlyftLevel = 0;
	private int matlyftLength = -getVert(2);
	// range 0 - (movement[4].parts + movement[5].parts)
	private int hissLevel = 0;
	private int hissLength = -(getVert(4) + getVert(5));
	private int hissVan1Length = -getVert(4);
	// range 0 - (movement[10].parts || movements[11].parts)
	private int armLevel = 0;
	private int armVertLength = getVert(10);
	private int armVertVan1Length = getVert(11);
	// range 0 - movement[8].parts (or movement[9].parts)
	private int armAngle = 0;
	private int armHoriLength = -getHori(9);

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

	private BTSim signals;
	private static final int framesize = 500;
	BallTrackView view = new BallTrackView(framesize, this);

	public void addSmallBall()
	{
		Ball ball = new Ball(1);

		ball.moveInit(movements[12].startPos, movements[12].endPos, movements[12].move);
		legBallList[12].add(ball);
	}

	public void addLargeBall()
	{
		Ball ball = new Ball(2);

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
	public RouteController(boolean[] outs, BTSim sigs)
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
	 * It simply makes the simulator run
	 */
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
     * from the array of all insignals
     */
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
		if (hissLevel < hissLength)
		    {
			hissLevel = hissLevel + 2;
		    }
	    }
	else
	    {
		if (inSignals[5])
		    {
			if (hissLevel < hissVan1Length)
			    {
				hissLevel = hissLevel + 2;
			    }
			else if (hissLevel > hissVan1Length)
			    {
				hissLevel = hissLevel - 2;
			    }
		    }
		else
		    {
			if (hissLevel > 0)
			    {
				hissLevel = hissLevel - 2;
			    }
		    }
	    }
	
	/* ArmLevel */
	
	if (armAngle == 0)
	    {
		//  UppArmVån1 && UppArmVån2
		if (inSignals[13] && inSignals[14])
		    {
			if (armLevel < armVertLength)
			    {
				armLevel = armLevel + 2;
			    }
		    }
		//       UppArmVån1
		else if (inSignals[13])
		    {
			if (armLevel < armVertVan1Length)
			    {
				armLevel = armLevel + 2;
			    }
			else if (armLevel > armVertVan1Length)
			    {
				armLevel = armLevel - 2;
			    }
		    }
		else if (armLevel > 0)
		    {
			armLevel = armLevel - 2;
		    }
	    }
	
	/* ArmAngle */
	
	// VridArmHöger
	if (armLevel == armVertVan1Length)
	    {
		//VridArmHöger && armAngle == armHoriLength && UppArmVån1 && !UppArmVån2
		if (inSignals[15] && armAngle == armHoriLength &&
		    inSignals[13] && !inSignals[14])
		    {
				//null, nothing has to be done
		    }
		//VridArmHöger && UppArmVån1 && !UppArmVån2 && armAngle < armHoriLength
		else if (inSignals[15] && inSignals[13] &&
			 !inSignals[14] && armAngle < armHoriLength)
		    {
			armAngle = armAngle + 2;
		    }
		else if (armAngle > 0)
		    {
			armAngle = armAngle - 2;
		    }
	    }
	else if (armLevel == armVertLength)
	    {
		if (inSignals[15] && armAngle == armHoriLength)//more signals
		    {
				//null, nothing has to be done
		    }
			else if (inSignals[15] && inSignals[14] &&
				 armAngle < armHoriLength)
			    {
				armAngle = armAngle + 2;
			    }
		else if (armAngle > 0)
		    {
			armAngle = armAngle - 2;
			}
	    }
	
	//System.out.println("MatlyftLevel     : " + matlyftLevel);
	//System.out.println("HissLevel        : " + hissLevel);
	//System.out.println("ArmLevel         : " + armLevel);
	//System.out.println("ArmAngle         : " + armAngle);
	//System.out.println("");
	
	
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
	
	// leg 9
	if (!legBallList[9].isEmpty())
	{
	    Ball b = (Ball) legBallList[9].get(0);
	    
	    //!Sug  => Sug error
	    if (!inSignals[16])
	    {
		//Längst ner samt ingen sug => flytta till nästa leg
		if (armLevel == 0 && move_11_12())
		{
		    b.moveInit(movements[12].startPos, movements[12].endPos, movements[12].move);
		    b.setVisible(false);
		}
		else if (armAngle == armHoriLength)
		    {}//Not yeat possible to deliver back the ball!
		else
		{
		    Exception e = new Exception("Ball dropped (11)");
		    System.err.println(e);
		    JOptionPane.showMessageDialog(null,"Ball dropped (11)");
		    System.exit(-1);
		    //throw new Exception("Ball dropped (11)");
		}
	    }
	    
	    int x = movements[9].endPos[0]  + armAngle;
	    int y = movements[11].endPos[1] - armLevel;
	    if (b.getRadius() == Ball.SMALL_BALL)
		y = movements[11].endPos[1] - armLevel - 5;
	    
	    b.setCoord(x, y);
	}
	
	// leg 7
	if (!legBallList[7].isEmpty())
	{
	    moveBallsInLeg(7);
	    
	    Ball b = (Ball) legBallList[7].get(0);
	    
	    if (b.finishedLeg() && move_7_9())
		{
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
	    //UtVån 1 eller Hissen på väg upp, nära UtVån2 samt att UtVån2
	    if (inSignals[7] || ((inSignals[5] && inSignals[6]) && inSignals[11] && (hissLevel == (hissLength-7))) && hissLevel != hissLength)
	    {
		Exception e = new Exception("Error in handling of UtVån1 UtVån2 (5)");
		System.err.println(e);
		JOptionPane.showMessageDialog(null,"Error in handling of UtVån1 UtVån2 (5)");
		System.exit(-1);
		//throw new Exception("Error in handling of UtVån1 UtVån2 (5)");
	    }

	    //System.out.println("I leg 5");
	    Ball b = (Ball) legBallList[5].get(0);
	    int  x = movements[4].startPos[0];
	    int  y = movements[4].startPos[1] - hissLevel;

	    b.setCoord(x, y);

	    if (hissLevel == hissLength && move_5_7())
	    {
		b.moveInit(movements[7].startPos, movements[7].endPos, movements[7].move);
	    }
	}
	
	//leg4
	if (!legBallList[4].isEmpty())
	{
	    Ball b = (Ball) legBallList[4].get(0);
	    int x = movements[4].startPos[0];
	    int y = movements[4].startPos[1] - hissLevel;
	    
	    b.setCoord(x, y);
	    
	    //Elevator below first Level
	    if (hissLevel < hissVan1Length)
	    {
	        //hissen nära UtVån1 när UtVån satt
		if (inSignals[7] && (inSignals[5] && hissLevel > (hissVan1Length - 7)))
		    {
			Exception e = new Exception("Error in handling of UtVån1 (4)");
			System.err.println(e);
			JOptionPane.showMessageDialog(null,"Error in handling of UtVån1 (4)");
			System.exit(-1);
			//throw new Exception("Error in handling of UtVån1 (4)");
		    }
	    }
	    //Elevator on first Level
	    else if (hissLevel == hissVan1Length)
	    {
		if (move_4_6())
		    b.moveInit(movements[6].startPos, movements[6].endPos, movements[6].move);
	    }
	    //Elevator over first but below second Level
	    else if (hissLevel < hissLength)
	    {
		//UtVån 1 eller Hissen på väg upp, nära UtVån2 samt att UtVån2 satt
		if (inSignals[7] || ((inSignals[5] && inSignals[6]) && inSignals[11] && (hissLevel == (hissLength-7))) && hissLevel != hissLength)
		{
		    Exception e = new Exception("Error in handling of UtVån1 UtVån2 (5)");
		    System.err.println(e);
		    JOptionPane.showMessageDialog(null,"Error in handling of UtVån1 UtVån2 (5)");
		    System.exit(-1);
		    //throw new Exception("Error in handling of UtVån1 UtVån2 (5)");
		}
	    }
	    //Elevator in second Level
	    else if (hissLevel == hissLength)
	    {
		if (move_5_7())
		    b.moveInit(movements[7].startPos, movements[7].endPos, movements[7].move);
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

    /** moveBallsInLeg is the method that for each
     *  ball calls the method move in the class Ball.
     *  @param index is the current leg.
     */
    private void moveBallsInLeg(int index)
    {
	if ((index != 6) && (index != 7))
	{
	    for (Iterator i = legBallList[index].iterator(); i.hasNext(); )
	    {
		Ball b = (Ball) i.next();
		
		b.move();
	    }
	}
	//We have to check if LyftVån1 is set
	else if (index == 6)
	{
	    for (Iterator i = legBallList[index].iterator(); i.hasNext(); )
	    {
		Ball b = (Ball) i.next();
		//LyftVån1
		if (inSignals[10])
		{
		    if (b.getPosition()[0] > 246)
			b.move();
		}
		else
		    b.move();
	    }
	}
	//We have to check if LyftVån2 is set
	else if (index == 7)
	{
	    for (Iterator i = legBallList[index].iterator(); i.hasNext(); )
	    {
		Ball b = (Ball) i.next();
		//LyftVån2
		if (inSignals[12])
		{
		    if (b.getPosition()[0] > 245)
			b.move();
		}
		else
		    b.move();
	    }
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
	if (outSignals[1])
	{
	    if (inSignals[2])
	    {
		legBallList[2].add(((LinkedList) legBallList[1]).removeFirst());

		return true;
	    }
	}

	if (matlyftLevel != 0)
	{
	    Exception e = new Exception("Elevator not down (1-2)");
	    System.err.println(e);
	    JOptionPane.showMessageDialog(null,"Elevator not down (1-2)");
	    System.exit(-1);
	    //throw new Exception("Elevator not down (1-2)");
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
		Exception e = new Exception("Error using Mät (2)");
		System.err.println(e);
		JOptionPane.showMessageDialog(null,"Error using Mät (2)");
		System.exit(-1);
		//throw new Exception("Error using Mät (2)");
	    }

	    legBallList[3].add(((LinkedList) legBallList[2]).removeFirst());
	    
	    return true;
	}
	
	return false;
    }

    private boolean move_3_4()
	throws Exception
    {
	if (outSignals[7] && legBallList[4].isEmpty())
	{
	    if (inSignals[5])
	    {
		legBallList[4].add(((LinkedList) legBallList[3]).removeFirst());

		return true;
	    }
	}

	if (hissLevel != 0)
	{
	    Exception e = new Exception("Elevator not down (3-4)");
	    System.err.println(e);
	    JOptionPane.showMessageDialog(null,"Elevator not down (3-4)");
	    System.exit(-1);
	    //throw new Exception("Elevator not down (3-4)");
	}

	return false;
    }

    private boolean move_4_6()
    {
	// HissVån1 & KulaVån1 & UtVån1
	if (outSignals[11] && outSignals[12] && inSignals[7])
	{
	    legBallList[6].add(((LinkedList) legBallList[4]).removeFirst());

	    return true;
	}

	return false;
    }

    private boolean move_5_7()
    {
	// HissVån2 & KulaVån2 & UtVån2
	if (outSignals[14] && outSignals[15] && inSignals[11])
	{
	    legBallList[7].add(((LinkedList) legBallList[4]).removeFirst());
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
	if (outSignals[20] && inSignals[16] && inSignals[10])
	{
	    if (outSignals[22])
	    {
		Exception e = new Exception("Never two balls in the arm (6)");
		System.err.println(e);
		JOptionPane.showMessageDialog(null,"Never two balls in the arm (6)");
		System.exit(-1);
		//throw new Exception("Never two balls in the arm (6)");
	    }

	    legBallList[9].add(((LinkedList) legBallList[6]).removeFirst());

	    return true;
	}

	return false;
    }

    private boolean move_7_9()
	throws Exception
    {
	// ArmVån2 && Sug && LyftVån2
	if (outSignals[21] && inSignals[16] && inSignals[12])
	{
	    if (outSignals[22])
	    {
		Exception e = new Exception("Never two balls in the arm (7)");
		System.err.println(e);
		JOptionPane.showMessageDialog(null,"Never two balls in the arm (7)");
		System.exit(-1);
		//throw new Exception("Never two balls in the arm (7)");
	    }

	    legBallList[9].add(((LinkedList) legBallList[7]).removeFirst());

	    return true;
	}

	return false;
    }

    private boolean move_11_12()
    {
	// !Sug
	if (!inSignals[16])
	{
	    legBallList[12].add(((LinkedList) legBallList[9]).removeFirst());

	    return true;
	}

	return false;
    }

    private boolean move_12_13()
    {
	legBallList[13].add(((LinkedList) legBallList[12]).removeFirst());
	
	return true;
    }

    private void setOutSignals()
    {
	outSignals[0] = !legBallList[0].isEmpty();         // KulaPortvakt
	outSignals[1] = matlyftLevel == 0;                // MätlyftNere
	if (!legBallList[1].isEmpty() && outSignals[1])  // KulaMätlyft
	    outSignals[2] =	((Ball) ((LinkedList) legBallList[1]).getFirst()).finishedLeg();
	else
	    outSignals[2] =	(outSignals[1] && !legBallList[2].isEmpty());
	outSignals[3] = matlyftLevel == movements[2].parts;            // MätlyftUppe
	outSignals[4] = outSignals[3] &&!legBallList[2].isEmpty();    // KulaMätstation
	outSignals[5] = inSignals[4] && outSignals[4] && ((Ball) (((LinkedList) legBallList[2]).getFirst())).getRadius() == Ball.BIG_BALL;    // StorKula
	outSignals[6] = inSignals[4] && outSignals[4] && ((Ball) (((LinkedList) legBallList[2]).getFirst())).getRadius() == Ball.SMALL_BALL; // LitenKula
	outSignals[7] = hissLevel == 0;                    // HissNere
	outSignals[8] = false;  //Not Used
	outSignals[9] = false; //Not Used
	if (!legBallList[3].isEmpty() && outSignals[7])   // KulaHiss
	    outSignals[10] = ((Ball) ((LinkedList) legBallList[3]).getFirst()).finishedLeg();
	else
	    outSignals[10] = (outSignals[7] && !legBallList[4].isEmpty());
	outSignals[11] = hissLevel == hissVan1Length;                // HissVån1
	outSignals[12] = outSignals[11] &&!legBallList[4].isEmpty();// KulaVån1
	outSignals[13] = !legBallList[6].isEmpty() && ((Ball) ((LinkedList) legBallList[6]).getFirst()).finishedLeg();   // PlockaVån1
	outSignals[14] = hissLevel == hissLength;                     // HissVån2
	outSignals[15] = outSignals[14] &&!legBallList[4].isEmpty(); // KulaVån2
	outSignals[16] = !legBallList[7].isEmpty() && ((Ball) ((LinkedList) legBallList[7]).getFirst()).finishedLeg();// PlockaVån2
	outSignals[17] = (armLevel == 0) && (armAngle == 0);       // ArmHemma
	outSignals[18] = false;
	outSignals[19] = false;
	outSignals[20] = (armLevel == armVertVan1Length) && (armAngle == armHoriLength);    // ArmVån1
	outSignals[21] = (armLevel == armVertLength) && (armAngle == armHoriLength);      // ArmVån2
	outSignals[22] = inSignals[16] &&!(legBallList[9].isEmpty()); // KulaFast
	outSignals[23] = false;                                      // AutoStart
	outSignals[24] = false;                                     // ManuellStart
	outSignals[25] = false;                                    // NödStopp
	outSignals[26] = false;                                   // LarmKvittering
    }
}
