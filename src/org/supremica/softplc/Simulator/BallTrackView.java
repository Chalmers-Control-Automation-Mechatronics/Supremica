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
	private final String imageFile = "backgroundtext.gif";
	private Image bgImage = Toolkit.getDefaultToolkit().getImage(imageFile);

	/**Maximum number of balls allowed*/
	final int maxNrOfBalls = 20;

	/**size of window*/
	private int size;

	/**This BallTrackView must be aware of a RouteController
	 * to be able to add balls*/
	private RouteController rController;

	// private int nrOfBalls = 0;
	private java.util.List balls;    // keep track of the balls
	public JButton insSmallBall, insLargeBall, delBall;
	public CheckboxGroup cbg = new CheckboxGroup();

	/**Menu initialization*/
	JMenuBar jMenuBar1 = new JMenuBar();
	JMenu jMenuFile = new JMenu();
	JMenuItem jMenuFileExit = new JMenuItem();
	JMenu jMenuImage = new JMenu();
	JMenuItem jMenuImageImageWithLabels = new JMenuItem();
	JMenuItem jMenuImageImageWithoutLabels = new JMenuItem();

	/**Constructor BallTrackView initialises a view for a ball track
	 * @param framesize the heigth of the window
	 */
	public BallTrackView(int framesize, RouteController rcont)
	{
		/**Menu initialization*/
		JMenuBar jMenuBar1 = new JMenuBar();
		JMenu jMenuFile = new JMenu();
		JMenuItem jMenuFileExit = new JMenuItem();
		JMenu jMenuImage = new JMenu();
		JMenuItem jMenuImageImageWithLabels = new JMenuItem();
		JMenuItem jMenuImageImageWithoutLabels = new JMenuItem();


		rController = rcont;
		size = framesize;

		setSize(size, size + 30);
		setTitle("Ball Track");    // something better than Ball Track??
		setVisible(true);

		Container contentPane = getContentPane();

		contentPane.setLayout(null);

		insSmallBall = new JButton("Add Small Ball");

		insSmallBall.setFont(new Font("Times", Font.BOLD, 10));
		insSmallBall.setBackground(Color.white);
		contentPane.add(insSmallBall);

		insLargeBall = new JButton("Add Large Ball");

		insLargeBall.setFont(new Font("Times", Font.BOLD, 10));
		insLargeBall.setBackground(Color.white);
		contentPane.add(insLargeBall);

		delBall = new JButton("Remove Ball");

		delBall.setFont(new Font("Times", Font.BOLD, 10));
		delBall.setBackground(Color.white);
		contentPane.add(delBall);

		Insets insets = contentPane.getInsets();

		insSmallBall.setBounds(0 + insets.left, 480 + insets.top, 100, 40);
		insLargeBall.setBounds(100 + insets.left, 480 + insets.top, 100, 40);
		delBall.setBounds(200 + insets.left, 480 + insets.top, 100, 40);
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

		//cbg.setBackground(Color.white);
		//cbg.setEnabled(true);
		//contentPane.add(new JRadioButton("Backgroundimage without input/output names",true));
		//contentPane.add(new Checkbox("Backgroundimage without input/output names", cbg, true));
		//contentPane.add(new Checkbox("Backgroundimage with input/output names", cbg, false));

		/* menu */
		jMenuFile.setText("File");
		jMenuFileExit.setText("Exit");
		jMenuFileExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jMenuFileExit_actionPerformed(e);
			}
		});

//  		jMenuImage.setText("Image");
//  		jMenuImageImageWithLabels.setText("Change to Image with Labels");
//  		jMenuImageImageWithLabels.addActionListener(new ActionListener()
//  		{
//  			public void actionPerformed(ActionEvent e)
//  			{
//  				jMenuFileImageWithLabels_actionPerformed(e);
//  			}
//  		});
//  		jMenuImageImageWithoutLabels.setText("Change to Image without Labels");
//  		jMenuImageImageWithoutLabels.addActionListener(new ActionListener()
//  		{
//  			public void actionPerformed(ActionEvent e)
//  			{
//  				jMenuFileImageWithoutLabels_actionPerformed(e);
//  			}
//  		});

		jMenuFile.add(jMenuFileExit);
		//jMenuImage.add(jMenuImageWithLabels);
		//jMenuImage.add(jMenuImageWithoutLabels);
		jMenuBar1.add(jMenuFile);
		//jMenuBar1.add(jMenuImage);
		this.setJMenuBar(jMenuBar1);
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

	/**File | Exit action performed*/
	public void jMenuFileExit_actionPerformed(ActionEvent e)
	{
		System.exit(0);
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

	/*public void update()
	{
		paint();
	}*/

	/**Paint paints all balls and the background
	 * @param g the graphics pen
	 */
	public void paint(Graphics gr)
	{
		Image im = createImage(size, (size +30));
		Graphics g = im.getGraphics();


		// get the balls to be painted
		balls = rController.getAllBalls();

		// make sure no balls collide
		collisionAvoidanceHandle();

		// paint background image
		g.drawImage(bgImage, 0, 15, this);
		insSmallBall.repaint();
		insLargeBall.repaint();
		delBall.repaint();

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
		//paintTestArm(g);


		// paint the balls
		for (Iterator i = balls.iterator(); i.hasNext(); )
		{
			Ball b = (Ball) i.next();

			b.paint(g);
		}

		gr.drawImage(im, 0, 0, this);
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
	private int lengthVisible = 24;

	private void paintHiss(Graphics g)
	{
		upperY  = 58;
		middleY = 129;
		lowerY  = 265;
		centerX = 431;
		stepInLower = 25;
		stepInUpper = 30;
		level = rController.hissLevel();

		if (level < 26)
  		{
  			partLength = (level * ( (lowerY - middleY) / stepInLower));
  			partLengthInt = new Float(partLength).intValue();
			partLengthInt = partLengthInt + (level/2);
			yPos = (middleY + ( (lowerY - middleY) - partLengthInt) );

			//paint part allways visible
			g.fillRect( (centerX - radiusX), lowerY,
						(2 * radiusX), lengthVisible);

			//paint part depending of level of the lift
			g.fillRect( (centerX - radiusX), (yPos + topHeight),
						(2 * radiusX), partLengthInt);
  			g.fillRect( (centerX - radiusTopX), yPos,
  						(2 * radiusTopX), topHeight);
  		}
  		else
  		{
			partLength = ((level - stepInLower) * ((middleY-upperY) / stepInUpper) );
			partLengthInt = new Float(partLength).intValue();
			partLengthInt = partLengthInt + ( (level-stepInLower)/3 );
			yPos = (upperY + ( (middleY - upperY) - partLengthInt) );

			//paint part allways visible when lift in upper part
  			g.fillRect( (centerX - radiusX), middleY,
  						(2 * radiusX), (lowerY - middleY + lengthVisible) );

			//paint part depending on the level of the lift
  			g.fillRect( (centerX - radiusX), (yPos + topHeight),
  						(2 * radiusX),partLengthInt);
  			g.fillRect( (centerX - radiusTopX), yPos,
  						(2 * radiusTopX), topHeight);
  		}
	}

	public void paintUrVan1(Graphics g)
	{
		upperX      = 433;	    //X value of right side of bar
		upperY      = 121;	   //Y value of the upper side of the bar
		width       = 5;	  //width of the bar
		shortLength = 10;	 //length of the bar when signal is not set
		longLength  = 19;	//length of the bar when signal is set

		if (rController.getInSignal(7))
			g.fillRect(upperX,upperY,
						longLength,width);
		else
			g.fillRect( (upperX + (longLength - shortLength)),upperY,
						shortLength, width);
	}

    public void paintUrVan2(Graphics g)
	{
		upperX      = 433;	    //X value of right side of cylinder
		upperY      = 52;	   //Y value of the upper side of the bar
		width       = 5;	  //width of the bar
		shortLength = 10;	 //length of the bar when signal is not set
		longLength  = 19;	//length of the bar when signal is set

		if (rController.getInSignal(9))
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

		if (rController.getInSignal(8))
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

		if (rController.getInSignal(10))
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

	private int parts_11 = 0;           //Steps in leg 11
	private int parts_10 = 0;          //Steps in leg 10
	private int parts_9 = 0;          //Steps in leg 9
	private int parts_8 = 0;         //Steps in leg 8

	private int xLeftPos = 40;         //Start possition to the right for Arm
	private int xCenterPos = 145;     //Horisontal Center possition for Arm
	private int xRightPos = 238;     //End possition to the left for Arm
	private int xDiff1 = (xCenterPos - xLeftPos);
	private int xDiff2 = (xRightPos - xCenterPos);
	private int xDiff3 = (xRightPos - xLeftPos);

	private int yTopPos = 52;         //Top possition for Arm. Left upper corner
	private int yCenterPos = 124;    //Y possition for Arm on leg8
	private int yDownPos = 151;     //Y possistion for the lowest level
	private int yDiff1 = (yDownPos - yCenterPos);
	private int yDiff2 = (yCenterPos - yTopPos);


	private boolean notOnMiddle = true;
	private boolean notOnUpper  = true;
	private int proppSize = 9;


	private void paintArm(Graphics g)
	{
		lowerY      = 151;					   //Used to paint bar in the cylinder
		stepInLower = 10;					  //Not very good, should
		stepInUpper = 25;					 //use method
		level = rController.armLevel();     //Arm vertical
		angel = rController.armAngle();    //Arm horisontal

		if (level == 0)
		{
			g.fillRect(xLeftPos,yDownPos,
						(xDiff1 + radiusX), (2 * radiusX));
			g.fillRect(xLeftPos, (yDownPos + 2 * radiusX),
						(2 * radiusTopX), proppSize);
		}
		else if (level == 10)
		{
			if (angel == 0)
			{
				g.fillRect(xLeftPos,yCenterPos,
							xDiff1, (2 * radiusX));
				g.fillRect(xLeftPos, (yCenterPos + 2 * radiusX),
							(2 * radiusTopX), proppSize);
				g.fillRect( (xCenterPos - radiusX), yCenterPos,
							(2 * radiusX), (yDiff1 + 2 * radiusX) );
			}
			else if (angel == 35)
			{
				g.fillRect(xCenterPos,yCenterPos,
							xDiff2, (2 * radiusX));
				g.fillRect( (xRightPos - (2 * radiusTopX)), yCenterPos + (2 * radiusX),
							(2 * radiusTopX), proppSize);
				g.fillRect( (xCenterPos - radiusX), yCenterPos,
							(2 * radiusX), (yDiff1 + 2 * radiusX) );
			}
			else
			{
				parts_8 = rController.getParts(8);
				if (angel < 18)
				{
					angel = new Float(angel * (xDiff3 / parts_8)).intValue();
					g.fillRect( (xLeftPos + angel),yCenterPos,
								(xDiff1 - angel), (2 * radiusX) );
					g.fillRect( (xLeftPos + angel), (yCenterPos + (2 * radiusX)),
								(2 * radiusTopX), proppSize);
					g.fillRect( (xCenterPos - radiusX), yCenterPos,
								(2 * radiusX), (yDiff1 + 2 * radiusX) );
				}
				else
				{
					angel = new Float(angel * (xDiff3 / parts_8)).intValue();
					g.fillRect(xCenterPos, yCenterPos,
								(angel - new Float(16*(xDiff3/parts_8)).intValue()), (2 * radiusX));
					g.fillRect( (xCenterPos + (angel - new Float(19*(xDiff3/parts_8)).intValue())), (yCenterPos + (2 * radiusX)),
								(2 * radiusTopX), proppSize);
					g.fillRect( (xCenterPos - radiusX), yCenterPos,
								(2 * radiusX), (yDiff1 + 2 * radiusX) );
				}
			}
		}
		else if (level == 35)
		{
			if (angel == 0)
			{
				g.fillRect(xLeftPos,yTopPos,
							xDiff1, (2 * radiusX));
				g.fillRect(xLeftPos, (yTopPos + 2 * radiusX),
							(2 * radiusTopX), proppSize);
				g.fillRect( (xCenterPos - radiusX), yTopPos,
							(2 * radiusX), (yDiff1 + yDiff2 + (2 * radiusX)) );
			}
			else if (angel == 35)
			{
				g.fillRect(xCenterPos,yTopPos,
							xDiff2, (2 * radiusX));
				g.fillRect( (xRightPos - (2 * radiusTopX)), yTopPos + (2 * radiusX),
							(2 * radiusTopX), proppSize);
				g.fillRect( (xCenterPos - radiusX), yTopPos,
							(2 * radiusX), (yDiff1 + yDiff2 + (2 * radiusX)) );
			}
			else
			{
				parts_9 = rController.getParts(9);
				if (angel < 18)
				{
					angel = new Float(angel * (xDiff3 / parts_9)).intValue();
					g.fillRect( (xLeftPos + angel),yTopPos,
								(xDiff1 - angel), (2 * radiusX) );
					g.fillRect( (xLeftPos + angel), (yTopPos + (2 * radiusX)),
								(2 * radiusTopX), proppSize);
					g.fillRect( (xCenterPos - radiusX), yTopPos,
								(2 * radiusX), (yDiff1 +yDiff2 + (2 * radiusX)) );
				}
				else
				{
					angel = new Float(angel * (xDiff3 / parts_9)).intValue();
					g.fillRect(xCenterPos, yTopPos,
								(angel - new Float(16*(xDiff3/parts_9)).intValue()), (2 * radiusX));
					g.fillRect( (xCenterPos + (angel - new Float(19*(xDiff3/parts_9)).intValue())), (yTopPos + (2 * radiusX)),
								(2 * radiusTopX), proppSize);
					g.fillRect( (xCenterPos - radiusX), yTopPos,
								(2 * radiusX), (yDiff1 +yDiff2 + (2 * radiusX)) );
				}
			}
		}
		else
		{
			parts_10 = rController.getParts(10);
			parts_11 = rController.getParts(11);
			level = new Float(level * ( (yDiff1 + yDiff2) / (parts_10 + parts_11))).intValue();
			level = level + (level / 3);

			g.fillRect(xLeftPos, (yTopPos + ((yDiff1 + yDiff2) - level)),
						xDiff1, (2 * radiusX) );
			g.fillRect(xLeftPos, (yTopPos + ((yDiff1 + yDiff2) - level) + (2 * radiusX)),
						(2 * radiusTopX), proppSize );
			g.fillRect( (xCenterPos - radiusX), (yTopPos + ((yDiff1 + yDiff2) - level)),
						(2 * radiusX), (level + (2 * radiusX)) );
		}
	}

	public void paintTestArm(Graphics g)
	{
		g.fillRect(xLeftPos,yTopPos,
					xDiff1, (2 * radiusX));
		g.fillRect(xLeftPos, (yTopPos + 2 * radiusX),
					(2 * radiusTopX), proppSize);

		g.fillRect(xCenterPos,yTopPos,
					xDiff2, (2 * radiusX) );
		g.fillRect( (xRightPos - (2 * radiusTopX)), (yTopPos + (2 * radiusX)),
					(2 * radiusTopX), proppSize);

		g.fillRect(xLeftPos,yCenterPos,
					xDiff1, (2 * radiusX));
		g.fillRect(xLeftPos, (yCenterPos + 2 * radiusX),
					(2 * radiusTopX), proppSize);

		g.fillRect(xCenterPos,yCenterPos,
					xDiff2, (2 * radiusX));
		g.fillRect( (xRightPos - (2 * radiusTopX)), yCenterPos + (2 * radiusX),
					(2 * radiusTopX), proppSize);

		g.fillRect(xLeftPos,yDownPos,
					xDiff1, (2 * radiusX));
		g.fillRect(xLeftPos, (yDownPos + 2 * radiusX),
					(2 * radiusTopX), proppSize);
	}


	public void showMessage(String mess)
	{
		JOptionPane.showMessageDialog(null,mess);
	}
}
