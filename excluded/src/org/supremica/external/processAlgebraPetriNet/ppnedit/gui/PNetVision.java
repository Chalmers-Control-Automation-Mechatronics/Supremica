package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.filechooser.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.BaseCell;



public class PNetVision
					extends JFrame 
								implements ToolBarListener{

    private int maxX, maxY;
 
    private JMenuBar jmb = new JMenuBar();
    
    private ToolBar toolbar = new ToolBar();
    private PetriToolBar petritoolbar = new PetriToolBar();
    private PetriProToolBar petriprotoolbar = new PetriProToolBar();
    
    private GraphContainer table = new GraphContainer();

    public  PNetVision() {
		//SETTINGS FOR THIS FRAME
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        maxX = screenSize.width - 100;
        maxY = screenSize.height - 100;
	
        setSize((int)(maxX/1.6), (int)(maxY/1.6));
        setLocation((screenSize.width - getSize().width)/2 ,
        			(screenSize.height - getSize().height)/2);
        
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//ADD MENUBAR TO THIS FRAME	
		this.setJMenuBar(jmb);
		//ToolBar	
		toolbar.addToolBarListener(this);
        
        petritoolbar.addToolBarListener(this);
        petritoolbar.setOrientation(1);  //VERTICAL = 1
        
		//File Menu
		jmb.add(table.getFileMenu());
		
		//Edit Menu
		jmb.add(table.getEditMenu());
		
		//Options Menu
		jmb.add(table.getOptionsMenu());

		//Windows Menu
		jmb.add(table.getWindowsMenu());
		
		//Help Menu
		JMenu jmHelp = new JMenu("Help");
		jmb.add(jmHelp);
		
		//LAYOUT
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(toolbar,BorderLayout.NORTH);
		//getContentPane().add(petritoolbar,BorderLayout.WEST);
		getContentPane().add(table, BorderLayout.CENTER);
       
		this.setVisible(true);	

		newSheet();
    }
	
    public void repaint() {	
		//sfc.repaint(resources);
    }
    
    // -------------- ToolbarListener -----------------------
    public void newSheet() {
		table.add(new PNetGraphFrame("Sheet"));	
    }    
    public void openFile() {
		table.open();
    }
    public void saveFile() {
		table.save();
    }
    public void newResource() {
		table.newResource();
    }
    public void newOperation() {
		table.newOperation();
    } 
    public void newEdge() {
    
    }
    public void delete() {
        table.delete();
    }
    public void insert(BaseCell cell) {
        table.insert(cell);
    }
    // -------------- End ToolbarListener -----------------------
}
