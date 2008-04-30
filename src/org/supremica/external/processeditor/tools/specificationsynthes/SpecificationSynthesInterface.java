package org.supremica.external.processeditor.tools.specificationsynthes;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.supremica.external.processeditor.SOCGraphContainer;
import org.supremica.external.processeditor.processgraph.resrccell.ResourceCell;


/**
 * @author millares
 *
 */
public class SpecificationSynthesInterface
						              extends
						                  JFrame 
									          implements 
									              ActionListener
{
	
	ConvertPanel cPanel;
	SOCGraphContainer graphContainer;
	
	public SpecificationSynthesInterface(){
		
		this.setExtendedState( Frame.MAXIMIZED_BOTH ); 
		this.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
		
		this.setTitle("Specification Synthes (EXPERIMENTAL)");
		
        //Add contents to the window.
        cPanel = new ConvertPanel();
        cPanel.addActionListener( this );
        cPanel.setFrame( this );
        
        getContentPane().add( cPanel );
        
        
        this.pack();
        
        //Place frame in center of screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = this.getSize();
        
        Point c = new Point(screenSize.width/2, screenSize.height/2);
        c.translate(-frameSize.width/2, -frameSize.height/2);
        this.setLocation(c);
        
        this.setVisible(false);
	}
	
	public void setGraphContainer(SOCGraphContainer graphContainer) {
		this.graphContainer = graphContainer;
	}
	
	public void setInputFileChooser(JFileChooser fc) {
    	cPanel.setInputFileChooser(fc);
    }
    
    public void setOutputFileChooser(JFileChooser fc) {
    	cPanel.setOutputFileChooser(fc);
    }
	
	/**
     *	Take care of action
     */
    public void actionPerformed(ActionEvent evt) {
    	
        String action = evt.getActionCommand();
        if(ConvertPanel.EXIT.equals(action)){
        	setVisible(false);
        }
    }
    
    public void setVisible(boolean b){
    	if(b){
    		cPanel.refreshTable();
    		addSelected();
    	}
    	super.setVisible(b);
    }
    
    private void addSelected(){
    	Object o = null;
    	int selectedcells = 0;
    	
    	//check indata
    	if(graphContainer == null){
    		return;
    	}
    	
    	//loop over all selected cells
    	selectedcells = graphContainer.getSelectedCount();
    	for(int i=0; i < selectedcells; i++){
    		o = graphContainer.getSelectedAt(i);
    		
    		//ResourceCell have ROP file
    		if(o instanceof ResourceCell){
    			if(((ResourceCell)o).getFile() != null &&
    			   ((ResourceCell)o).getFile().exists()){
    				cPanel.addFile(((ResourceCell)o).getFile());
    			}
    		}
    	}
    	
    }
}
