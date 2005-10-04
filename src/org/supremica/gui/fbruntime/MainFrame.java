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

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import com.nwoods.jgo.*;


public class MainFrame extends JFrame {

    private JMenuBar theMenuBar;
    private JMenu fileMenu;
    private JToolBar theToolBar;
    private JButton newButton;
    private JTextPane statusLine;
	private JSplitPane horizontalSplit;
	private JSplitPane verticalSplit;


    /** Creates new form FBRuntimeMainFrame */
    public MainFrame() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {

		// Frame settings
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FBRuntime");
        //setFont(new java.awt.Font("Arial", 0, 12));
        setName("mainFrame");
		setPreferredSize(new Dimension(800,600));


		// MenuBar and menus
        theMenuBar = new JMenuBar();
        fileMenu = new JMenu();
        fileMenu.setText("File");
        //jMenu1.setFont(new java.awt.Font("Arial", 1, 12));
        theMenuBar.add(fileMenu);
        setJMenuBar(theMenuBar);

		
		// ToolBar
        theToolBar = new JToolBar();
        newButton = new JButton();
        newButton.setText("New");
        theToolBar.add(newButton);
        getContentPane().add(theToolBar, BorderLayout.NORTH);
		

		// StatusLine
        statusLine = new JTextPane();
        statusLine.setBackground(UIManager.getDefaults().getColor("Button.background"));
        statusLine.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusLine.setText("Status Bar");
        getContentPane().add(statusLine, BorderLayout.SOUTH);

		
		// Split Pnaes
		horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		verticalSplit.setTopComponent(horizontalSplit);
        getContentPane().add(verticalSplit, BorderLayout.CENTER);
		


        pack();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
        
}
