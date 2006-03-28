/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain However, it is freely
 * available without fee for education, research, and non-profit purposes. By
 * obtaining copies of this and other files that comprise the Supremica
 * software, you, the Licensee, agree to abide by the following conditions and
 * understandings with respect to the copyrighted software:
 *
 * The software is copyrighted in the name of Supremica, and ownership of the
 * software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its documentation for
 * education, research, and non-profit purposes is hereby granted to Licensee,
 * provided that the copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all such copies, and
 * that no charge be made for such copies. Any entity desiring permission to
 * incorporate this software into commercial products or to use it for
 * commercial purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org Supremica, Haradsgatan 26A 431 42
 * Molndal SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are available.
 *
 * Licensee may not use the name, logo, or any other symbol of Supremica nor the
 * names of any of its employees nor any adaptation thereof in advertising or
 * publicity pertaining to the software without specific prior written approval
 * of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE SUITABILITY OF THE
 * SOFTWARE FOR ANY PURPOSE. IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED
 * WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages suffered by Licensee from
 * the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
/**
 * @author Goran Cengic
 */
package org.supremica.gui.fbruntime;

import org.supremica.properties.Config;
import org.supremica.log.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import com.nwoods.jgo.*;


public class MainFrame extends JFrame {

	static
	{
		Config.LOG_TO_CONSOLE.set(false);
		Config.LOG_TO_GUI.set(true);
	}

	private Logger logger = LoggerFactory.createLogger(MainFrame.class);
	private LogDisplay theLogDisplay = LogDisplay.getInstance();

    private JMenuBar theMenuBar;
    private JMenu fileMenu;
	private JMenuItem fileExit;
    private JToolBar theToolBar;
    private JButton newButton;
    //private JTextPane statusLine;
	private JSplitPane horizontalSplit;
	private JSplitPane verticalSplit;


    public MainFrame() {
        initComponents();
		setVisible(true);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		logger.info("Blah");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {

		// Frame settings
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("FBRuntime");
        setName("mainFrame");
		setPreferredSize(new Dimension(800,600));


		// MenuBar and menus
        theMenuBar = new JMenuBar();
        fileMenu = new JMenu("File");
		fileExit = new JMenuItem("Exit");
		fileMenu.add(fileExit);

        theMenuBar.add(fileMenu);
        setJMenuBar(theMenuBar);


		// ToolBar
        theToolBar = new JToolBar();
        newButton = new JButton();
        newButton.setText("New");
        theToolBar.add(newButton);
        getContentPane().add(theToolBar, BorderLayout.NORTH);


		// StatusLine
        //statusLine = new JTextPane();
        //statusLine.setBackground(UIManager.getDefaults().getColor("Button.background"));
        //statusLine.setBorder(new BevelBorder(BevelBorder.LOWERED));
        //statusLine.setText("Status Bar");
        //getContentPane().add(statusLine, BorderLayout.SOUTH);


		// Split Panes
		horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		verticalSplit.setTopComponent(horizontalSplit);
		verticalSplit.setBottomComponent(theLogDisplay.getComponent());
        getContentPane().add(verticalSplit, BorderLayout.CENTER);

        pack();
    }


    public static void main(String args[])
	{
		new MainFrame();
    }

}
