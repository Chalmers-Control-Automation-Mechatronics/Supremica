package org.supremica.external.processeditor.processgraph.resrccell;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.border.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;

/**
 * Displays the resource info window, which allow the user to edit the
 * resource information.
 */
public class ResourceCellInfoWindow extends JDialog implements ActionListener,
							       MouseListener
{    
    private int sizeX = 300;
    private int sizeY = 350;

    private String[] types = {"DOP", "COP"};

    private JPanel main = new JPanel();
    
    private JComboBox type = new JComboBox(types);
    private JTextField machine;
    private JTextField id;
    private JTextField comment;
    private JButton ok,cancel;

    private ROP r = null;
    
    public static final int APPROVE_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int ERROR_OPTION = 3;
    private int option = ERROR_OPTION;
    
    /**
     * Creates a new instance of the class
     * 
     * @param o the object that is to be edit by this info window
     */
    public ResourceCellInfoWindow(ROP o)
    {	    
	r = o;
	String title = "  Resource  ";	
	main.setBorder(new TitledBorder(title));
	main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));	
	
	
	main.add(type);
	type.setSelectedItem(r.getType());
	type.setBorder(new TitledBorder("  Type  "));	    
	type.setBackground(main.getBackground());	    
	type.addActionListener(this);
	
	main.add(machine = new JTextField(r.getMachine(),15));
	machine.setBorder(new TitledBorder("  Machine  "));	    
	machine.setBackground(main.getBackground());
	machine.addMouseListener(this);	    	    
	
	main.add(id = new JTextField(r.getId(),15));
	id.setBorder(new TitledBorder("  Id  "));		    
	id.setBackground(main.getBackground());
	id.addMouseListener(this);
	
	main.add(comment = new JTextField(15));
	try {
	    comment.setText(r.getComment());
	}catch(Exception ex) {
	    comment.setText("");
	}
	comment.setBorder(new TitledBorder("  Comment  "));		    
	comment.setBackground(main.getBackground());
	comment.addMouseListener(this);
	
	JToolBar tool = new JToolBar();
	tool.setLayout(new FlowLayout(FlowLayout.LEFT));
	tool.setFloatable(false);	    
	tool.add(ok = new JButton("OK")); 	    	    	    	    
	tool.add(cancel = new JButton("Cancel"));	    
	ok.addActionListener(this);
	cancel.addActionListener(this);
	
	main.add(tool);
	
	add(main);
	setSize(sizeX,sizeY);		    
	setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-sizeX)/2,
		     (Toolkit.getDefaultToolkit().getScreenSize().height-sizeY)/2);
	setModal(true);
    }
    /**
     * Popup the info window and returns the window state on popdown.
     *
     * @return the return state of this info window on popdown:
     * <ul>
     * <li>ResourceCellInfoWindow.APPROVE_OPTION</li>
     * <li>ResourceCellInfoWindow.CANCEL_OPTION</li>
     * <li>ResourceCellInfoWindow.DELETE_OPTION</li>
     * <li>ResourceCellInfoWIndow.ERROR_OPTION</li>
     * </ul>
     */
    public int showDialog() {
	show();
	return option;
    }
    /**
     * Is invoked when an action has occured. 
     */
    public void actionPerformed(ActionEvent e) {
	//DEBUG
	//System.out.println("ResourceCellInfoWindow.actionPerformed()");
	//END DEBUG          
	if(e.getSource() == ok){	   
	    option = APPROVE_OPTION;	    
	    if(((String)type.getSelectedItem()).equals("DOP")) {
		r.setType(ROPType.ROP);	    
	    }else if(((String)type.getSelectedItem()).equals("COP")) {
		r.setType(ROPType.COP);
	    }
	    r.setMachine(machine.getText());
	    r.setId(id.getText());
	    if(comment.getText().equals("")) {
		r.setComment(null);
	    }else {
		r.setComment(comment.getText());
	    }
	    dispose();
	}
	else if(e.getSource() == cancel){	    		
	    option = CANCEL_OPTION;
	    dispose();
	}
    } 
    /**
     * Invoked when the mouse button has been cliked on this window.
     * <p>
     * <i>The method is not in use.</i>
     */
    public void mouseClicked(MouseEvent e) {}
     /**
     * Invoked when the mouse enters this window.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void mouseEntered(MouseEvent e) {}
     /**
     * Invoked when the mouse exits this window.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void mouseExited(MouseEvent e) {}
    /**
     * Invoked when the mouse is pressed on this window.
     */
    public void mousePressed(MouseEvent e) {	    
	machine.setBackground(main.getBackground());
	id.setBackground(main.getBackground());
	comment.setBackground(main.getBackground());	    
	if(e.getSource() == machine){
	    machine.setBackground(Color.white);   
	}
	else if(e.getSource() == id){
	    id.setBackground(Color.white);
	}
	else if(e.getSource() == comment){
	    comment.setBackground(Color.white);
	}	    
    }    
    /**
     * Invoked when a mouse button has been released on this window.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void mouseReleased(MouseEvent e) {}    
}
