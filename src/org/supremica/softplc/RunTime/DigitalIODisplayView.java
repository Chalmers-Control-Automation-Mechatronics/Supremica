package org.supremica.softplc.RunTime;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Title:
 * Description: Draws the interface
 * @author  Niclas Hamp
 * @version 1.0
 */
public class DigitalIODisplayView
	extends JFrame
{
	private static JCheckBox[] cbSignalsIn;
	private static JCheckBox[] cbSignalsOut;
	private static DigitalIODriver driver;
	private static short nr_Of_Signals_In;
	private static short nr_Of_Signals_Out;

	/**Construct the frame*/
	public DigitalIODisplayView(String dynClass)
		throws Exception
	{
	    int i;
	    JPanel buttonPanel;
	    JPanel signalPanelIn;
	    JPanel signalPanelOut;
	    JPanel signalPanel;
	    JPanel descriptions;
	    JPanel south;
	    JScrollPane signalPanelScroller;
	    Container contentPane;
	    GridLayout gridLayout = new GridLayout();
	    JLabel descUt = new JLabel(" Out               ");
	    JLabel descIn = new JLabel("       In ");
	    String[] inputDescriptions;
	    String[] outputDescriptions;
	    GridBagLayout gridbag = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    JButton updateButton = new JButton("Update");
	    Class IOClass;
	    
	    /**Menu initialization*/
	    JMenuBar jMenuBar1 = new JMenuBar();
	    JMenu jMenuFile = new JMenu();
	    JMenuItem jMenuFileExit = new JMenuItem();
	    JMenu jMenuHelp = new JMenu();
	    JMenuItem jMenuHelpAbout = new JMenuItem();
	    
	    buttonPanel    = new JPanel();
	    signalPanelIn  = new JPanel();
	    signalPanelOut = new JPanel();
	    signalPanel    = new JPanel();
	    descriptions   = new JPanel();
	    south          = new JPanel();
	    contentPane    = this.getContentPane();
	    signalPanelScroller = new JScrollPane(signalPanel);
	    
	    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	    
	    try
		{
		    IOClass = Class.forName(dynClass);
		    driver = (DigitalIODriver) IOClass.newInstance();
		    nr_Of_Signals_In = driver.getNrOfSignalsIn();
		    nr_Of_Signals_Out = driver.getNrOfSignalsOut();
		}
	    catch (Exception e)
		{
		    System.err.println("3: " + e);
			System.exit(-1);
		}
	    
	    cbSignalsIn = new JCheckBox[nr_Of_Signals_In];
	    cbSignalsOut = new JCheckBox[nr_Of_Signals_Out];
	    inputDescriptions = new String[nr_Of_Signals_In];
	    outputDescriptions = new String[nr_Of_Signals_Out];
	    
	    if (driver.hasInputDescriptions())
		{
		    for (i = 0; i < nr_Of_Signals_In; i++)
			{
			    inputDescriptions[i] = new String();
			}
		    
			inputDescriptions = driver.getInputDescriptions();
		}
	    else
		{
		    for (i = 0; i < nr_Of_Signals_In; i++)
			{
			    inputDescriptions[i] = new String();
			    inputDescriptions[i] = Integer.toString(i);
			}
		}
	    
	    if (driver.hasOutputDescriptions())
		{
		    for (i = 0; i < nr_Of_Signals_Out; i++)
			{
				outputDescriptions[i] = new String();
			}
		    
		    outputDescriptions = driver.getOutputDescriptions();
		}
	    else
		{
		    for (i = 0; i < nr_Of_Signals_In; i++)
			{
			    outputDescriptions[i] = new String();
			    outputDescriptions[i] = Integer.toString(i);
			}
		}
	    
	    //buttonPanel.setLayout(new BorderLayout());
	    signalPanelIn.setLayout(new BoxLayout(signalPanelIn, BoxLayout.Y_AXIS));
	    signalPanelOut.setLayout(new BoxLayout(signalPanelOut, BoxLayout.Y_AXIS));
	    south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
	    signalPanel.setLayout(new BorderLayout());
	    descriptions.setLayout(new BorderLayout());
	    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
	    signalPanelIn.setBackground(Color.white);
	    signalPanelOut.setBackground(Color.white);
	    signalPanel.setBackground(Color.white);
	    descriptions.setBackground(Color.white);
	    
	    for (i = 0; i < nr_Of_Signals_In; i++)
		{
		    cbSignalsIn[i] = new JCheckBox();
		    
		    cbSignalsIn[i].setText(inputDescriptions[i]);
		    cbSignalsIn[i].setBackground(Color.white);
		    cbSignalsIn[i].setEnabled(false);
		    signalPanelIn.add(cbSignalsIn[i], null);
		}
	    
	    for (i = 0; i < nr_Of_Signals_Out; i++)
		{
		    cbSignalsOut[i] = new JCheckBox();
			
		    cbSignalsOut[i].setText(outputDescriptions[i]);
		    cbSignalsOut[i].setBackground(Color.white);
		    signalPanelOut.add(cbSignalsOut[i], null);
		}
	    
	    //buttonPanel.add(updateButton,BorderLayout.CENTER);
	    signalPanel.add(signalPanelIn, BorderLayout.WEST);
	    signalPanel.add(signalPanelOut, BorderLayout.EAST);
	    signalPanelScroller.setAlignmentX(LEFT_ALIGNMENT);
	    descriptions.setAlignmentX(LEFT_ALIGNMENT);
	    descIn.setFont(new Font("Arial", Font.PLAIN, 18));
	    descUt.setFont(new Font("Arial", Font.PLAIN, 18));
	    descriptions.add(descIn, BorderLayout.WEST);
	    descriptions.add(descUt, BorderLayout.EAST);
	    south.add(descriptions);
	    south.add(signalPanelScroller);
	    contentPane.add(updateButton);
	    contentPane.add(south);
		
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
	    jMenuHelp.setText("Help");
	    jMenuHelpAbout.setText("About");
	    jMenuHelpAbout.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
			jMenuHelpAbout_actionPerformed(e);
		    }
		});
	    updateButton.addMouseListener(new java.awt.event.MouseAdapter()
		{
		    public void mouseClicked(MouseEvent e)
		    {
			updateButton_mouseClicked(e);
		    }
		});
	    jMenuFile.add(jMenuFileExit);
	    jMenuHelp.add(jMenuHelpAbout);
	    jMenuBar1.add(jMenuFile);
	    jMenuBar1.add(jMenuHelp);
	    this.getContentPane().setBackground(Color.white);
	    this.setSize(new Dimension(320, 630));
	    this.setTitle("Display");
	    this.setJMenuBar(jMenuBar1);
	}
    
    /**File | Exit action performed*/
    public void jMenuFileExit_actionPerformed(ActionEvent e)
    {
	System.exit(0);
    }
    
    /**Help | About action performed*/
    public void jMenuHelpAbout_actionPerformed(ActionEvent e) {}
    
    /**Overridden so we can exit when window is closed*/
    protected void processWindowEvent(WindowEvent e)
    {
	super.processWindowEvent(e);
	
	if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
		    jMenuFileExit_actionPerformed(null);
		}
    }
    
	void updateButton_mouseClicked(MouseEvent e)
    {
	boolean[] signalsIn = new boolean[nr_Of_Signals_Out];
	boolean[] signalsOut = new boolean[nr_Of_Signals_In];
	int i;
	
	// set signals
	for (i = 0; i < cbSignalsOut.length; i++)
	    {
		signalsOut[i] = cbSignalsOut[i].isSelected();
	    }
	
	try
	    {
		driver.setSignalArray(signalsOut);
	    }
	catch (Exception exc)
	    {
		System.err.println(exc);
	    }
	
	// get signals
	try
	    {
		driver.getSignalArray(signalsIn);
	    }
	catch (Exception exc)
	    {
			System.err.println(exc);
	    }
	
	for (i = 0; i < signalsIn.length; i++)
	    {
		cbSignalsIn[i].setSelected(signalsIn[i]);
		}
    }
}
