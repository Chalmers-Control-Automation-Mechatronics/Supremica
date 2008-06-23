package org.supremica.external.processeditor.tools.copextractor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;

import org.supremica.external.processeditor.SOCGraphContainer;
import org.supremica.external.processeditor.processgraph.resrccell.ResourceCell;


/**
 * @author millares
 *
 */
public class COPExtractInterface
                              extends
						          JFrame 
						              implements 
									      ActionListener
{
	
	private static final String EXIT_BUTTON_LABEL = "Done";
	private static final String EXIT_COMMAND = "EXIT";
	
	
	private ConvertPanel cPanel = null;
	private SOCGraphContainer graphContainer = null;
	
	public COPExtractInterface(){
		
		JPanel bottom = null;
		JButton jbExit = null;
		
		this.setExtendedState( Frame.MAXIMIZED_BOTH ); 
		this.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
		
		this.setTitle( "COP Extract workbench" );
		
		//Create exit button
		jbExit = new JButton( EXIT_BUTTON_LABEL );
		jbExit.setActionCommand( EXIT_COMMAND );
		jbExit.addActionListener( this );
		
		bottom = new JPanel();
		bottom.setLayout( new BorderLayout() );
		bottom.add( jbExit, BorderLayout.EAST );
		
        //Add contents to the window.
        cPanel = new ConvertPanel();
        cPanel.setFrame( this );
        
        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( cPanel, BorderLayout.CENTER );
        getContentPane().add( bottom, BorderLayout.SOUTH );
        
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
		cPanel.setGraphContainer( graphContainer );
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
        if( EXIT_COMMAND.equals( evt.getActionCommand() ) ){
        	setVisible( false );
        }
    }
    
    public void setVisible(boolean b){
    	if( b ){
    		cPanel.refreshTable();
    		addSelected();
    	}
    	
    	super.setVisible( b );
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
    
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new COPExtractInterface();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Display the window.
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
