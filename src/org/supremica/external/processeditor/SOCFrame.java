package org.supremica.external.processeditor;

import java.awt.*;
import javax.swing.*;

/**
 * SOCFrame is the SOC program main frame. 
 *
 * @author    Mikael Kjellgren <kjelle@etek.chalmers.se>
 *
 * @version   0.1
 */
public class SOCFrame extends JFrame  {       
    
 	private static final long serialVersionUID = 1L;
 	
	@SuppressWarnings("unused")
	private JMenuBar jmb;   
    private SOCGraphContainer table;   
    
    /**
    * Creates a new <code>SOCFrame</code> visible by default.
    *
    * <p>
    * Contain <code>SOCGraphContainer</code> with a empty <i>sheet</i>.
    *
    * @since   0.1       
    *
    */
    public  SOCFrame() {
    	jmb = new JMenuBar();
    	table = new SOCGraphContainer();
    	
    	
    	setSize(700,500);
    	this.setExtendedState(Frame.MAXIMIZED_BOTH); 
    	this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	this.setTitle("Sequence of Operation Chart");
	
    	
	
    	this.setIconImage(Toolkit.getDefaultToolkit().
			  getImage(SOCFrame.class.getClass().
				   getResource("/icons/processeditor/icon.gif")));	

    	this.setJMenuBar(table.getMenuBar());		
	       
    	getContentPane().setLayout(new BorderLayout());	
    	getContentPane().add(table.getToolBar(),BorderLayout.NORTH);   	       
    	getContentPane().add(table, BorderLayout.CENTER);      

    	this.setVisible(true);	

    	table.add(new SOCGraphFrame("Sheet"));              
    }
    
    public SOCGraphContainer getGraphContainer(){
    	return table;
    }
}
