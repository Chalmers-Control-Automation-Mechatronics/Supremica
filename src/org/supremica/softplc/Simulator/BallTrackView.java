package org.supremica.softplc.Simulator;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.awt.Color.*;
import java.lang.*;

/**Class BallTrackView paints the background ball track and possibly several
 * balls in a window
 *
 * Date 15 Oct 2001
 * @author Anders Röding
 * @author Henrik Staberg
 */
public class BallTrackView
	extends JFrame

/*
 * This is the component painting all balls
 * and also the background
 * It also manages the balls such that they don't run into each other.
 */
{

    /**Background image*/
    MediaTracker bildsLaddningKontroll = new MediaTracker(this);
    private final String imageFileWithText    = "backgroundtext.gif";
    private final String imageFileWithoutText = "background.gif";
    private boolean imageWithText = true; //Because bgImage will contain this image from the beginning
    private Image bgImage = Toolkit.getDefaultToolkit().getImage(BallTrackView.class.getResource("/labprocess/" + imageFileWithText));
    private Image bgImageTemp = Toolkit.getDefaultToolkit().getImage(BallTrackView.class.getResource("/labprocess/" + imageFileWithoutText));

    /**Maximum number of balls allowed*/
    final int maxNrOfBalls = 20;
    
    /**size of window*/
    private int size;
    
    /**This BallTrackView must be aware of a RouteController
     * to be able to add balls*/
    private RouteController rController;
    
    // private int nrOfBalls = 0;
    private java.util.List balls;    // keep track of the balls
    public JButton insSmallBall, insLargeBall, delBall, changeImage, manuellStart, autoStart, nodStop, larmKvitt, exit;
    public JPanel south       = new JPanel();    //panel to collect the button panels
    public JPanel simButtons  = new JPanel();   //panel for buttons used to simulate the BallTrack
    public JPanel realButtons = new JPanel();  //panel for buttons simulating real life actions 
    public Canvas canvas      = new Canvas(); //Canvas used to paint simualtor images
        
    /**Constructor BallTrackView initialises a view for a ball track
     * @param framesize the heigth of the window
     */
    public BallTrackView(int framesize, RouteController rcont)
    {
	rController = rcont;
	size = framesize;
	
	setSize(size, size + 45);
	setTitle("Ball Track Simulator");
	setVisible(true);
	
	Container contentPane = getContentPane();
	contentPane.setLayout(new BorderLayout());
	contentPane.setBackground(Color.white);

	south.setLayout(new BorderLayout());
	south.setBackground(Color.white);

	simButtons.setLayout(new FlowLayout());
	simButtons.setBackground(Color.white);

	realButtons.setLayout(new FlowLayout());
	realButtons.setBackground(Color.white);

	canvas.setSize(500,458);
	canvas.setBackground(Color.white);
	canvas.setVisible(true);
	contentPane.add(canvas,BorderLayout.NORTH);

	bildsLaddningKontroll.addImage(bgImage, 0);
	bildsLaddningKontroll.addImage(bgImageTemp, 1);
	
	try
            {
                bildsLaddningKontroll.waitForAll();
            }
        catch(Exception e)
            {
                System.exit(-1);
            }

	//Button to insert a BALL with small radius
	insSmallBall = new JButton("Add Small Ball");
	insSmallBall.setFont(new Font("Times", Font.BOLD, 10));
	realButtons.add(insSmallBall);

	//Button to insert a BALL with large radius
	insLargeBall = new JButton("Add Large Ball");
	insLargeBall.setFont(new Font("Times", Font.BOLD, 10));
	realButtons.add(insLargeBall);
	
	//Button to delete the first ball in Leg 1
	delBall = new JButton("Remove Ball");
	delBall.setFont(new Font("Times", Font.BOLD, 10));
	realButtons.add(delBall);
	
	//Button to change the backgroud image. With or Without text
	changeImage = new JButton("Change Image");
	changeImage.setFont(new Font("Times", Font.BOLD, 10));
	realButtons.add(changeImage);

	//Button to start the simulation manually
	manuellStart = new JButton("ManuellStart");
	manuellStart.setFont(new Font("Times", Font.BOLD, 10));
	simButtons.add(manuellStart);

	//Button to start the simulation automatically
	autoStart = new JButton("AutoStart");
	autoStart.setFont(new Font("Times", Font.BOLD, 10));
	simButtons.add(autoStart);

	//Button to stop the simulation quickly
	nodStop = new JButton("NödStop");
	nodStop.setFont(new Font("Times", Font.BOLD, 10));
	simButtons.add(nodStop);

	//Button to turn of the alarm
	larmKvitt = new JButton("LarmKvittering");
	larmKvitt.setFont(new Font("Times", Font.BOLD, 10));
	simButtons.add(larmKvitt);

	//Button to exit the simulation
	exit = new JButton("Exit Program");
	exit.setFont(new Font("Times", Font.BOLD, 12));

	simButtons.setVisible(true);
	realButtons.setVisible(true);

	south.add(simButtons, BorderLayout.NORTH);
	south.add(realButtons, BorderLayout.CENTER);
	south.add(exit, BorderLayout.SOUTH);
	south.setVisible(true);

	contentPane.add(canvas,BorderLayout.NORTH);
	contentPane.add(south,BorderLayout.SOUTH);
	contentPane.setVisible(true);

	//Add mouseListener to the buttons
	insSmallBall.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mouseClicked(MouseEvent e)
	    {
		insSmallBall_mouseClicked(e);
	    }
	});
	insLargeBall.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mouseClicked(MouseEvent e)
	    {
		insLargeBall_mouseClicked(e);
	    }
	});
	delBall.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mouseClicked(MouseEvent e)
	    {
		delBall_mouseClicked(e);
	    }
	});
	changeImage.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mouseClicked(MouseEvent e)
	    {
		changeImage_mouseClicked(e);
	    }
        });
	manuellStart.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mousePressed(MouseEvent e)
	    {
		manuellStart_mousePressed(e);
	    }
	    public void mouseReleased(MouseEvent e)
	    {
		manuellStart_mouseReleased(e);
	    }

        });
	autoStart.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mousePressed(MouseEvent e)
	    {
		autoStart_mousePressed(e);
	    }
	    public void mouseReleased(MouseEvent e)
	    {
		autoStart_mouseReleased(e);
	    }

        });
	nodStop.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mousePressed(MouseEvent e)
	    {
		nodStop_mousePressed(e);
	    }
	    public void mouseReleased(MouseEvent e)
	    {
		nodStop_mouseReleased(e);
	    }

        });
	larmKvitt.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mousePressed(MouseEvent e)
	    {
		larmKvitt_mousePressed(e);
	    }
	    public void mouseReleased(MouseEvent e)
	    {
		larmKvitt_mouseReleased(e);
	    }

        });
	exit.addMouseListener(new java.awt.event.MouseAdapter()
	{
	    public void mouseClicked(MouseEvent e)
	    {
		exit_mouseClicked(e);
	    }
        });
	pack();//Paint the components (buttons) the first time
    }
    
    void insSmallBall_mouseClicked(MouseEvent e)
    {
	rController.addSmallBall();
    }
    
    void insLargeBall_mouseClicked(MouseEvent e)
    {
	rController.addLargeBall();
    }

    void delBall_mouseClicked(MouseEvent e)
    {
	rController.delBall();
    }
    
    void changeImage_mouseClicked(MouseEvent e)
    {
	if (imageWithText)
	{
	    bgImage = Toolkit.getDefaultToolkit().getImage(BallTrackView.class.getResource("/labprocess/" + imageFileWithoutText));
	    imageWithText = false;
	}
	else if (!imageWithText)
	{
	    bgImage = Toolkit.getDefaultToolkit().getImage(BallTrackView.class.getResource("/labprocess/" + imageFileWithText));
	    imageWithText = true;
	}
    }

    void manuellStart_mousePressed(MouseEvent e)
    {
	rController.setOutSignals(24, false);
    }

    void manuellStart_mouseReleased(MouseEvent e)
    {
	rController.setOutSignals(24, true);
    }

    void autoStart_mousePressed(MouseEvent e)
    {
	rController.setOutSignals(23, false);
    }

    void autoStart_mouseReleased(MouseEvent e)
    {
	rController.setOutSignals(23, true);
    }

    void nodStop_mousePressed(MouseEvent e)
    {
	
    }

    void nodStop_mouseReleased(MouseEvent e)
    {
	
    }

    void larmKvitt_mousePressed(MouseEvent e)
    {
	
    }

    void larmKvitt_mouseReleased(MouseEvent e)
    {

    }

    void exit_mouseClicked(MouseEvent e)
    {
	System.exit(0);
    }

    /**getBgImageStatus returns the status for the bgImage
     */
    public boolean getBgImageStatus()
    {
	return (bgImage != null);
    }


    /**collisionAvoidanceHandle makes sure that no ball run over another
     */
    private void collisionAvoidanceHandle()
    {
	boolean colliding;
	
	for (Iterator i = balls.iterator(); i.hasNext(); )
	{
	    Ball b = (Ball) i.next();
		
	    colliding = false;
	    
	    for (Iterator j = balls.iterator(); j.hasNext(); )
	    {
		Ball c = (Ball) j.next();
			
		if (!b.equals(c))
		{
		    if (b.collisionRisk(c))
		    {
			colliding = true;
		    }
		}
	    }

	    b.allowMove(!colliding);
	}
    }

    /**update changes the standard update used in repaint()
     * to avoid blinking
     * @param g the graphics pen
     */
    public void update(Graphics g)
    {
	paint(g);
    }

    /**Paint paints all balls and the background
     * @param g the graphics pen
     */
    public void paint(Graphics gr)
    {
	    Image im = createImage(500, 458);
	    Graphics g = im.getGraphics();
	    
	    // get the balls to be painted
	    balls = rController.getAllBalls();
	    
	    // make sure no balls collide
	    collisionAvoidanceHandle();
	    
	    // paint background image
	    g.drawImage(bgImage, 0, 0, this);
	    
	    // paint the animated lifts
	    g.setColor(Color.gray);
	    paintPortVakt(g);
	    paintMatLyft(g);
	    paintUrMatning(g);
	    paintHiss(g);
	    paintUrVan1(g);
	    paintUrVan2(g);
	    paintLyftVan1(g);
	    paintLyftVan2(g);
	    paintArm(g);
	    
	    // paint the balls
	    for (Iterator i = balls.iterator(); i.hasNext(); )
	    {
		Ball b = (Ball) i.next();
		b.paint(g);
	    }
	    
	    //paint the simulator buffer to the canvas
	    Graphics gN = canvas.getGraphics();
	    gN.drawImage(im, 0, 0, this);
	    
	    //paint the buttons and the background of the buttons
	    south.repaint();
    }


	/************************************************
	*Below just methods to paint the lifts and arms**
	************************************************/

	//Variables used when painting the lifts and the Arm.
	private int level = 0;            //Level of the lifts, vertical
	private float partLength = 0;   //The size of the step in the leg, will be converted to int
	private int partLengthInt = 0;
	private int stepInLower;	  //Number of step in lift leg
	private int stepInUpper;	 //Number of step in lift leg

	private int centerX;		     //Center of the cylinders
	private int radiusX    = 3;     //Radius of lift "bar"
	private int radiusTopX = 8;    //Radius of the top part of lift
								  //Int taken from radius of a big ball
	private int topHeight  = 2;	 //Height of carrier
	private int yPos;			//position of the lifts vertical
	private int lowerY;		   //position of the lower "station"
	private int middleY;	  //position of the middle "station"
	private int upperY;		 //position of the upper "station"
	private int upperX;

	private int width;		  //used for defining width of bars
	private int shortLength; //used for defining length of bars when
							//signal is not set
	private int longLength;//used when signal is set

	/**paintPortVakt draws the PortVakt
	 * @param g the graphics pen
	 */

	  //Variables used when painting the PortVakt
	 // taken from background picture
	//The menubar takes some space as well
	private int inPortVaktUpperX = 104;
	private int inPortVaktUpperY = 310;
	private int inPortVaktDepth  = 36;

	private int utPortVaktUpperX = 122;
	private int utPortVaktUpperY = 311;
	private int utPortVaktDepth  = 35;

	private int portLenght		 = 10; //Value taken to fit the balls
	private int portWidth		 = 2;


	private void paintPortVakt(Graphics g)
	{
		//Paint the lower part of inPortVakt
		g.fillRect(inPortVaktUpperX,inPortVaktUpperY,
					portWidth,inPortVaktDepth);
		//Paint the lower part of utPortVakt
		g.fillRect(utPortVaktUpperX,utPortVaktUpperY,
					portWidth,utPortVaktDepth);

		//Paint the Ports if signals equal to False
		if (!rController.getInSignal(0))
		{
			g.fillRect(inPortVaktUpperX, (inPortVaktUpperY - portLenght),
						portWidth, portLenght);
		}
		if (!rController.getInSignal(1))
		{
			g.fillRect(utPortVaktUpperX, (utPortVaktUpperY - portLenght),
						portWidth, portLenght);
		}
	}

	/**paintMatLyft draws the matlyft
	 * @param g the graphics pen
	 */

	//Variables for use when painting the MatLyft
	private int parts = 0;     //Steps in leg 2
	private int vert  = 0;    //vertical differance in leg 2

	private int maxHeightY = 88;       //Maximum height of lift
	private int baseHeight = 10;      //Height allways visible
	private boolean NotOnTop = true; //True when the level is != 15


	private void paintMatLyft(Graphics g)
	{
		centerX = 285;
		lowerY  = 339;    //top of the cylinder in picture
		level = rController.matlyftLevel();

		if ( (!rController.getInSignal(2)) && (level == 0) )
		{
			//paint the lower part of the lift. It's allways visible.
  			g.fillRect( (centerX - radiusX), (lowerY - baseHeight),
						(2 * radiusX), baseHeight);
			g.fillRect( (centerX - radiusTopX), (lowerY - baseHeight - topHeight),
						(2 * radiusTopX), topHeight);
			NotOnTop = true;
		}
		else
		{
			if ( (!rController.getInSignal(2)) || (NotOnTop) )
			{
				//The lift is going up or down
				parts = rController.getParts(2);
				vert  = rController.getVert(2);
  				partLength = (level * (vert/parts));
  				partLengthInt = new Float(partLength).intValue();

				//First paint the lower part always visible
				g.fillRect( (centerX - radiusX), (lowerY - baseHeight),
							(2 * radiusX), baseHeight);
				 //Paint the part that depends on the level of the lift
				//partLengthInt is negative (=> + partLengthInt)
  				g.fillRect( (centerX - radiusX), (lowerY - baseHeight + partLengthInt ),
  							(2 * radiusX), (-partLengthInt));
  				g.fillRect( (centerX - radiusTopX), (lowerY - baseHeight + partLengthInt - topHeight),
  							(2 * radiusTopX), topHeight);
  				if (level == 15)
  					NotOnTop = false;
  				else
  					NotOnTop = true;
			}
			else
			{
				   //Lift is on top, we have to compensate for misscalculation
				  //so the lift and ball fit to each other

				//First paint the lower part always visible
				g.fillRect( (centerX - radiusX), (lowerY - baseHeight),
							(2 * radiusX), baseHeight);
				//paint upper part of lift.
				g.fillRect( (centerX - radiusX), (lowerY - maxHeightY),
							(2 * radiusX), maxHeightY);
				g.fillRect( (centerX - radiusTopX), (lowerY - maxHeightY - topHeight),
  							(2 * radiusTopX), topHeight);
			}
		}
	}

	/**paintUrMatning draws the bar that hits the ball when UrMatning is true
 	 * @param g the Graphics pen
	 */

	//Variables for use when painting the UrMatning

	public void paintUrMatning(Graphics g)
	{
		upperX      = 265;	    //X value of right side of cylinder
		upperY      = 238;	   //Y value of the upper side of the bar
		width       = 5;	  //width of the bar
		shortLength = 5;	 //length of the bar when signal is not set
		longLength  = 15;	//length of the bar when signal is set


		if (rController.getInSignal(3))   //Checks if UrMatLift is true
			g.fillRect(upperX,upperY,
						longLength,width);
		else
			g.fillRect(upperX,upperY,
						shortLength,width);
	}

	/**paintHiss draws the right lift called Hiss
	 *@param g the Graphics pen
	 */

	//Variables for use when painting the Hiss
	private int lengthVisible = 23;

	private void paintHiss(Graphics g)
	{
		level   = rController.hissLevel();
		lowerY  = rController.movements[4].startPos[1];
		centerX = rController.movements[4].startPos[0];

		//paint part allways visible
		g.fillRect( (centerX - radiusX), lowerY,
					(2 * radiusX), lengthVisible);

		//paint vertical and horisontal bars
		g.fillRect( (centerX - radiusX), (lowerY - level),
					(2 * radiusX), level);

		g.fillRect( (centerX - radiusTopX), (lowerY - level),
					(2 * radiusTopX), radiusX );
	}

	//paintUrVan1 differs from paintUrVan2 because the
	//the lift reaches higher than the cylinder...


	public void paintUrVan1(Graphics g)
	{
		int ballX   = rController.movements[4].endPos[0];
		int ballY   = rController.movements[4].endPos[1];
			level   = rController.hissLevel();
		int space   = 1;
			width   = 5;
		int length0 = 7;
		int length1 = 9;
		int length2 = 12;
		int length3 = 14;

		if (!rController.getInSignal(7))
		{
			g.fillRect( (ballX + length1), (ballY - width - space),
						length1, width );
			g.fillRect( (ballX + length3), (ballY - space),
						length0, width );
		}
		else
		{
			g.fillRect( (ballX + width), (ballY - width - space),
						length1, width );
			g.fillRect( (ballX + length1), (ballY - space),
						length2, width );
		}
	}

    public void paintUrVan2(Graphics g)
	{
		upperX      = 433;	    //X value of right side of cylinder
		upperY      = 52;	   //Y value of the upper side of the bar
		width       = 5;	  //width of the bar
		shortLength = 10;	 //length of the bar when signal is not set
		longLength  = 19;	//length of the bar when signal is set

		if (rController.getInSignal(11))
			g.fillRect(upperX,upperY,
						longLength,width);
		else
			g.fillRect( (upperX + (longLength - shortLength)),upperY,
						shortLength, width);
	}

	public void paintLyftVan1(Graphics g)
	{
		upperX      = 236;	    //X value of right side of bar
		lowerY      = 160;	   //Y value of the lower side of the bar
		width       = 7;	  //width of the bar
		shortLength = 6;	 //length of the bar when signal is not set
		longLength  = 12;	//length of the bar when signal is set

		if (rController.getInSignal(10))
			g.fillRect( upperX, (lowerY - longLength),
						width, longLength);
		else
			g.fillRect( upperX, (lowerY - shortLength),
						width, shortLength);
	}

	public void paintLyftVan2(Graphics g)
	{
		upperX      = 235;
		lowerY      = 90;
		width       = 7;
		shortLength = 6;
		longLength  = 12;

		if (rController.getInSignal(12))
			g.fillRect(upperX, (lowerY - longLength),
					   width, longLength);
		else
			g.fillRect(upperX, (lowerY - shortLength),
					   width, shortLength);
	}

	/**paintArm draws the Arm on left hand side
	 *@param g the Graphics pen
	 */

	//Variables for use in painting the Arm
	private int angel = 0;  //Angel of the arm horisontal

	private int xLeftPos = 40;         //Start possition to the right for Arm
	private int xCenterPos = 145;     //Horisontal Center possition for Arm
	private int xDiff1 = (xCenterPos - xLeftPos);

	private int proppSize   = 14;
	private int proppHeight = 10;
	private int proppDepth  =  2;

	private void paintArm(Graphics g)
	{
		lowerY      = 151;
		level = rController.armLevel();     //Arm vertical
		angel = rController.armAngle();    //Arm horisontal

		//Paint the center vertical bar
		g.fillRect( (xCenterPos - radiusX), (lowerY - level),
					(2 * radiusX), (level + 6) );

		//Paint the horisontal bar and the PROPP
		if (angel >= xDiff1)
		{
			g.fillRect( xCenterPos, (lowerY - level),
						(angel - xDiff1 + proppSize/2), radiusX);

			g.fillOval( (xCenterPos + angel - xDiff1),
						(lowerY - level + proppDepth),
						proppSize, proppHeight );
		}
		else
		{
			g.fillRect( (xLeftPos + angel + proppSize/2), (lowerY - level),
						(xDiff1 - angel - proppSize/2), radiusX);

			g.fillOval( (xLeftPos + angel), (lowerY - level + proppDepth),
						proppSize, proppHeight );
		}
	}
}
