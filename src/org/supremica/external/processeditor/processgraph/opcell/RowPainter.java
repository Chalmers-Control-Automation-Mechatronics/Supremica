package org.supremica.external.processeditor.processgraph.opcell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;


/**
 * Draws a row of attributes.
 */
public class RowPainter extends JPanel implements MouseListener,
						  KeyListener

{
    private static final long serialVersionUID = 1L;

	private final DefaultListModel<AttributePanel> mod = new DefaultListModel<AttributePanel>();
    private final JList<AttributePanel> list = new JList<AttributePanel>(mod);
    @SuppressWarnings("unused")
	private final AttributeCellRenderer renderer = null;

    private AttributeListener myListener = null;

    private final Activity operand;

    /**
     * Creates a new instance of the class that is associated with the specified activity object.
     *
     * @param a the activity object this instance will be associated with
     */
    public RowPainter(final Activity a)
    {
	setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
	setBackground(new Color(0,0,0,0));

	operand = a;
	Object[] attributes;
	try {
	    attributes = operand.getProperties().getAttribute().toArray();
	}catch(final Exception ex) {
	    attributes = new Object[0];
	}

	for(int i = 0; i < attributes.length; i++)
	    {
		Boolean up; Boolean down;
		try{
		    up = new Boolean(((Attribute)attributes[i]).getUpperIndicator().isIndicatorValue());
		}catch(final Exception ex) {
		    up = null;
		}
		try{
		    down = new Boolean(((Attribute)attributes[i]).getLowerIndicator().isIndicatorValue());
		}catch(final Exception ex) {
		    down = null;
		}
		final AttributePanel tmpPanel = new AttributePanel(((Attribute)attributes[i]).getAttributeValue(), ((Attribute)attributes[i]).getType(), up, down);
		tmpPanel.setOpaque(true);
		try {
		    if(!((Attribute)attributes[i]).isInvisible()) {
			mod.addElement(tmpPanel);
		    }
		}catch(final Exception ex) {};
	    }

	final AttributeCellRenderer renderer = new AttributeCellRenderer(this);
	renderer.setPreferredSize(new Dimension(AttributePanel.sizeX, AttributePanel.sizeY));
	list.setCellRenderer(renderer);


	add(list);

	list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	list.setVisibleRowCount(1);

	list.addMouseListener(this);
	list.addKeyListener(this);

	setSize(new Dimension(mod.getSize()*AttributePanel.sizeX, AttributePanel.sizeY));

    }
    /**
     * Updates the size of this attribute row.
     */
    public void update() {
	list.setFixedCellWidth(AttributePanel.sizeX);
	list.setFixedCellHeight(AttributePanel.sizeY);
	setSize(new Dimension(mod.getSize()*AttributePanel.sizeX, AttributePanel.sizeY));
    }
    /**
     * Clears the selection attribute.
     */
    public void clearSelectedAttributes() {
	list.clearSelection();
    }
    /**
     * Adds the specified attribute listener to recieve attribute events.
     *
     * @param l the attribute listener
     */
    public void addListener(final AttributeListener l) {
	myListener = l;
    }
    /**
     * Sets the color of the attribute types of this attribute row.
     *
     * @param uAtt the unique attribute types
     * @param uAttC the attribute type's corresponding color
     */
    public void setAttributeTypeColor(final String[] uAtt, final Color[] uAttC) {
	final Object[] attributes = mod.toArray();
	for(int i = 0; i < attributes.length; i++) {
	    int index = -1;
	    for(int j = 0; j < uAtt.length; j++) {
		if(((AttributePanel)attributes[i]).type.equals(uAtt[j])) {
		    index = j;
		    break;
		}
	    }
	    if(index != -1) {
		((AttributePanel)attributes[i]).setAttributeColor(uAttC[index]);
	    }else {
		((AttributePanel)attributes[i]).setAttributeColor(Color.white);
	    }
	}
    }
    /**
     * Sets attributes of the specified type
     * visible or invisible in this attribute row.
     *
     * @param type specifies concerned attribute type
     * @param visible if <code>true</code> the attribute is set visible,
     * otherwise <code>false</code>
     */
    public void setAttributeTypeVisible(final String type, final boolean visible) {
	if(operand.getProperties() != null) {
	    final Object[] attributes = operand.getProperties().getAttribute().toArray();
	    for(int j = 0; j < attributes.length; j++) {
		if((attributes[j] instanceof Attribute)&&
		   (type.equals(((Attribute)attributes[j]).getType()))) {
		    ((Attribute)attributes[j]).setInvisible(!visible);
		    operand.getProperties().getAttribute().add(j, (Attribute)attributes[j]);
		}
	    }
	}
    }
    /**
     * Invoked when a mouse button has been clicked on this attribute row.
     * <p>
     * This method is intentionally left empty.
     */
    public void mouseClicked(final MouseEvent e){}
    /**
     * Invoked when the mouse enters this attribute row.
     * <p>
     * This method is intentionally left empty.
     */
    public void mouseEntered(final MouseEvent e){}
    /**
     * Invoked when the mouse exits this attribute row.
     * <p>
     * this method is intentionally left empty.
     */
    public void mouseExited(final MouseEvent e){}
    /**
     * Invoked when a mouse button has been pressed on this attribute row.
     * <p>
     * Selects the pressed attribute panel in this attribute row.
     */
    public void mousePressed(final MouseEvent e){
	//DEBUG
	//System.out.println("RowPainter.mousePressed()");
	//END DEBUG
	if(myListener != null) {
	    myListener.removeSelection();
	    myListener.setList(list);
	}
    }
    /**
     * Invoked when a mouse button has been released on this attribute row.
     * <p>
     * This method is intentionally left empty.
     */
    public void mouseReleased(final MouseEvent e){
    }
    /**
     * Invoked when a key is pressed.
     * <p>
     * If it is the <code>Del</code> key the selected attribute panel will
     * become invisible.
     */
    public void keyPressed(final KeyEvent e)
    {
	final Object[] sel =  list.getSelectedValuesList().toArray();

	if(e.getKeyCode()==KeyEvent.VK_DELETE && sel.length > 0)
	    {
		for(int i = 0; i < sel.length; i++)
		    {
			final Object[] attributes = operand.getProperties().getAttribute().toArray();
			for(int j = 0; j < attributes.length; j++) {
			    if((attributes[j] instanceof Attribute)&&
			       (((AttributePanel)sel[i]).type.equals(((Attribute)attributes[j]).getType()))&&
			       (((AttributePanel)sel[i]).value.equals(((Attribute)attributes[j]).getAttributeValue()))) {
				((Attribute)attributes[j]).setInvisible(true);
				operand.getProperties().getAttribute().add(j, (Attribute)attributes[j]);
			    }
			}
		    }
		if(myListener !=  null) {
		    myListener.rebuild();
		}
	    }
    }
    /**
     * Invoked when a key is released.
     * <p>
     * This method is intentionally left empty.
     */
    public void keyReleased(final KeyEvent e){
    }
    /**
     * Invoked when a key is typed.
     * <p>
     * This method is intentionally left empty.
     */
    public void keyTyped(final KeyEvent e){
    }
}

