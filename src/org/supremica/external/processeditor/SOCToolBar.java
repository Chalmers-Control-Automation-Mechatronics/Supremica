package org.supremica.external.processeditor;

import javax.swing.*;
import javax.swing.border.*;

import org.supremica.gui.Supremica;

import java.awt.*;
import java.awt.Font.*;
import java.awt.event.*;
import java.io.*;

/**
 * A Simple class that handles the SOC program menu icons. 
 *
 * <p>
 * Address URL for the icon images. 
 * Handles mouse event and communicate with  other objects that have 
 * implemented 
 * the <code>SOCToolbarListener</code> interface and are added with the 
 * <code>addSOCToolbarListener</code> method. 
 *
 * @author    Mikael Kjellgren <kjelle@etek.chalmers.se>
 *
 * @version   0.1
 */
public class SOCToolBar extends JToolBar implements ActionListener
						
{                      
    private Image fileNew;    
    private Image fileOpen;
    private Image fileSave;
    private Image editCut;
    private Image editCopy;
    private Image editPaste;
    private Image editDelete;    

    private JButton jbFileNew;
    private JButton jbFileOpen;
    private JButton jbFileSave;
    private JButton jbEditCut;
    private JButton jbEditCopy;
    private JButton jbEditPaste;
    private JButton jbEditDelete;	        

    private SOCToolBarListener l = null;

    /**     
     * Constructs a new toolbar.
     *
     * @author    Mikael Kjellgren <kjelle@etek.chalmers.se>
     *
     * @version   0.1
     */
    public SOCToolBar() 
    {
	fileNew = Toolkit.getDefaultToolkit().	     
	    getImage(Supremica.class.getResource("/icons/processeditor/FileNew.gif"));	       
	fileOpen = Toolkit.getDefaultToolkit().
	    getImage(Supremica.class.getResource("/icons/processeditor/FileOpen.gif"));
	fileSave = Toolkit.getDefaultToolkit().
	    getImage(Supremica.class.getResource("/icons/processeditor/FileSave.gif"));
	editCut = Toolkit.getDefaultToolkit().
	    getImage(Supremica.class.getResource("/icons/processeditor/EditCut.gif"));
	editCopy = Toolkit.getDefaultToolkit().
	    getImage(Supremica.class.getResource("/icons/processeditor/EditCopy.gif"));
	editPaste = Toolkit.getDefaultToolkit().
	    getImage(Supremica.class.getResource("/icons/processeditor/EditPaste.gif"));
	editDelete = Toolkit.getDefaultToolkit().
	    getImage(Supremica.class.getResource("/icons/processeditor/EditDelete.gif"));
	
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	Dimension buttonSpace = new Dimension(4, 1);
	
	this.add(Box.createRigidArea(buttonSpace));
	this.add(jbFileNew = new JButton(" New ",new ImageIcon(fileNew)));
	this.add(Box.createRigidArea(buttonSpace));
	
	this.add(jbFileOpen = new JButton(" Open ",new ImageIcon(fileOpen)));  	
	this.add(Box.createRigidArea(buttonSpace));
	this.add(jbFileSave = new JButton(" Save ",new ImageIcon(fileSave))); 
	this.add(Box.createRigidArea(buttonSpace));
		
	this.addSeparator();
		
	this.add(Box.createRigidArea(buttonSpace));
	this.add(jbEditCut = new JButton(" Cut ", new ImageIcon(editCut)));
	this.add(Box.createRigidArea(buttonSpace));
	this.add(jbEditCopy = new JButton(" Copy ", new ImageIcon(editCopy)));
	this.add(Box.createRigidArea(buttonSpace));
	this.add(jbEditPaste = new JButton(" Paste ", new ImageIcon(editPaste)));
	this.add(Box.createRigidArea(buttonSpace));
		
	this.addSeparator();
		
	this.add(Box.createRigidArea(buttonSpace));
	this.add(jbEditDelete = new JButton(" Delete ", new ImageIcon(editDelete)));
	this.add(Box.createRigidArea(buttonSpace));
		
	this.addSeparator();
	
	
	jbFileNew.addActionListener(this);
	jbFileOpen.addActionListener(this);
	jbFileSave.addActionListener(this);
	jbEditCut.addActionListener(this);
	jbEditCopy.addActionListener(this);
	jbEditPaste.addActionListener(this);
	jbEditDelete.addActionListener(this);       	
	
    }
    /**     
     * Adds a new listener.
     *
     * @version   0.1
     */
    public void addSOCToolBarListener(SOCToolBarListener tbl) {
	l = tbl;
    }
    /**     
     * Sets the <i>New icon</i> enabled.
     *
     * @version   0.1
     */
    public void setNewEnabled(boolean b) {
	jbFileNew.setEnabled(b);
    }
    /**     
     * Sets the <i>Open icon</i> enabled.
     *
     * @version   0.1
     */
    public void setOpenEnabled(boolean b) {
	jbFileOpen.setEnabled(b);
    }
    /**     
     * Sets the <i>Save icon</i> enabled.
     *
     * @version   0.1
     */
    public void setSaveEnabled(boolean b) {
	jbFileSave.setEnabled(b);
    }
    /**     
     * Sets the <i>Cut icon</i> enabled.
     *
     * @version   0.1
     */
    public void setCutEnabled(boolean b) {
	jbEditCut.setEnabled(b);
    }
    /**     
     * Sets the <i>Copy icon</i> enabled.
     *
     * @version   0.1
     */
    public void setCopyEnabled(boolean b) {
	jbEditCopy.setEnabled(b);
    }
    /**     
     * Sets the <i>Paste icon</i> enabled.
     *
     * @version   0.1
     */
    public void setPasteEnabled(boolean b) {
	jbEditPaste.setEnabled(b);
    }
    /**     
     * Sets the <i>Delete icon</i> enabled.
     *
     * @version   0.1
     */
    public void setDeleteEnabled(boolean b) {
	jbEditDelete.setEnabled(b);
    }
    /**     
     * Invoked when an action occurs. 
     *
     * @version   0.1
     */
    public void actionPerformed(ActionEvent e)
    {
	String itemName = e.getActionCommand();
	//----- BUTTONS ------	
	if (e.getSource()instanceof JButton)
	    {
		if(l != null) {
		    if (e.getSource() == jbFileNew)
			{			
			    l.newSheet();
			}
		    else if (e.getSource() == jbFileOpen)
			{			
			    l.open();
			}
		    else if (e.getSource() == jbFileSave)
			{			
			    l.save();
			}
		    else if (e.getSource() == jbEditCut)
			{
			    l.cut();
			}
		    else if(e.getSource() == jbEditCopy) 
			{
			    l.copy();
			}
		    else if(e.getSource() == jbEditPaste) 
			{
			    l.paste();
			}
		    else if(e.getSource() == jbEditDelete) 
			{
			    l.delete();
			}
		}
	    }
    }
}		       
