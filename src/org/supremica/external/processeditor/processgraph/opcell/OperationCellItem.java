package org.supremica.external.processeditor.processgraph.opcell;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.supremica.external.processeditor.xml.Converter;
import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;


/**
 * Represents the operation cell.
 */
public class OperationCellItem extends JPanel implements MouseListener,
							 							 AttributeListener

{
	private static final long serialVersionUID = 1L;

	public static int mainTextMargin = 20;
    public static int exPanelSizeX = 10;

    AttributeListener myListener = null;

    private ExtensionCell extensionCell;
    private boolean extended;

    public JPanel mainPanel, exPanel;
    @SuppressWarnings("unused")
	private JLabel opName,namePlace;

    private final JLabel expand = new JLabel(">>");

    @SuppressWarnings("unused")
    private JLabel attName;
    private Object complexFunction = null;

    private final ObjectFactory objectFactory = new ObjectFactory();

    /**
     * Creates a new instance of the class.
     */
    public OperationCellItem( )
    {
    	setLayout(null);

    	drawExtended();
    }
    /**
     * Creates a new instance of the class that will be associated with
     * the specified object.
     * <p>
     * Adds the specified attribute listener to recieve attribute events.
     *
     * @param o the object this cell will be associated with
     * @param l the attribute listener
     */
    public OperationCellItem(final Object o, final AttributeListener l) {
    	myListener = l;
    	setLayout(null);
    	complexFunction = o;
    	drawExtended();
    	boolean unextend = false;

    	if(complexFunction instanceof Activity) {
    		try {
    			unextend = ((Activity)complexFunction).getProperties().isUnextended();
    		}catch(final Exception ex) {}
    	}else if(complexFunction instanceof Relation) {
    		try {
    			unextend = ((Relation)complexFunction).getAlgebraic().isUnextended();
    		}catch(final Exception ex) {}
    	}

    	if(unextend) {
    		drawCollapsed();
    	}
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
     * Draws this operation cell in extended mode
     */
    public void drawExtended()
    {
    	//DEBUG
    	//System.out.println("OperationCellItem.drawExtended()");
    	//END DEBUG
    	extended = true;
    	if(extensionCell != null){
    		remove(extensionCell);
    	}
    	if(mainPanel != null){
    		remove(mainPanel);
    	}
    	extensionCell = new ExtensionCell(complexFunction, myListener);
    	createMain();
    	add(mainPanel);
    	extensionCell.setLocation(mainPanel.getSize().width,0);
    	add(extensionCell);

    	setSize((mainPanel.getSize().width + extensionCell.getSize().width),
    			extensionCell.getSize().height);

    	//RE-LABELING OF BUTTON
    	expand.setText("<<");
    	exPanel.setBackground(Color.gray);
    	repaint();

    	if(myListener != null) {
    		myListener.attributeChanged();
    		myListener.repaint();
    	}
    	repaint();
    }

    /**
     * Draws this operation cell in collapsed mode.
     */
    public void drawCollapsed()
    {
    	extended = false;
    	if(extensionCell != null){
    		remove(extensionCell);
    	}
    	if(mainPanel != null){
    		remove(mainPanel);
    	}

    	createMain();
    	add(mainPanel);

    	setSize(mainPanel.getSize().width, mainPanel.getSize().height);

    	//RE-LABELING OF BUTTON
    	expand.setText(">>");
    	exPanel.setBackground(Color.lightGray);
    	//repaint();
    	if(myListener != null) {
    		myListener.attributeChanged();
    		myListener.repaint();
    	}
    	repaint();
    }
    /**
     * Draws the main part of this operation cell.
     */
    public void createMain() {
    	mainPanel = new JPanel();
    	mainPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
    	mainPanel.setLayout(null);
    	mainPanel.add(opName = new JLabel());

    	if(complexFunction != null) {
    		opName.setText(Converter.convertToString(complexFunction));
    	}else {
    		opName.setText("No Activity");
    	}

    	opName.setFont(new Font("Serif", Font.BOLD, 12));

    	exPanel = new JPanel();
    	exPanel.add(expand);

    	expand.setFont(new Font("Serif", Font.BOLD, 12));
    	mainPanel.add(exPanel);

    	exPanel.setBackground(Color.lightGray);
    	exPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
    	exPanel.addMouseListener(this);

    	if(extended) {
    		mainPanel.setSize(new Dimension(opName.getPreferredSize().width+mainTextMargin*2+exPanel.getSize().width,
					    Math.max(extensionCell.getSize().height, AttributePanel.sizeY)));
    	}else {
    		mainPanel.setSize(new Dimension(opName.getPreferredSize().width+mainTextMargin*2+exPanel.getSize().width,
					    AttributePanel.sizeY));
    	}

    	exPanel.setBounds(mainPanel.getSize().width-exPanelSizeX, 0,
    			exPanelSizeX, mainPanel.getSize().height);
    	opName.setBounds((mainPanel.getSize().width-opName.getPreferredSize().width)/2, (mainPanel.getSize().height-opName.getPreferredSize().height)/2, opName.getPreferredSize().width,
			 opName.getPreferredSize().height);
    }
    /**
     * Updates the size of this operation cell.
     */
    public void update() {
    	//DEBUG
    	//System.out.println("OperationCellItem.update()");
    	//END DEBUG
    	extensionCell.update();
    	if(extended) {
    		setSize((mainPanel.getSize().width + extensionCell.getSize().width),
    				extensionCell.getSize().height);
    	}else {
    		setSize(mainPanel.getSize().width, mainPanel.getSize().height);
    	}
    }
    /**
     * Sets whether or not this operation cell should be extended or not.
     *
     * @param set if <code>true</code> sets this operation cell extended,
     * otherwise <code>false</code>
     */
    public void setExtended(final boolean set) {
	//DEBUG
	//System.out.println("OperationCellItem.setExtended(): "+set);
	//END DEBUG
	if(set) {
	    if(complexFunction instanceof Activity) {
		try {
		    ((Activity)complexFunction).getProperties().setUnextended(false);
		}catch(final Exception ex) {
		    try {
			((Activity)complexFunction).
			    setProperties(objectFactory.createProperties());
			((Activity)complexFunction).
			    getProperties().setUnextended(false);
		    }catch(final Exception ex2) {}
		}
	    }else if(complexFunction instanceof Relation) {
	    	try {
	    		((Relation)complexFunction).getAlgebraic().
	    		setUnextended(false);
	    	}catch(final Exception ex) {
	    		try {
	    			((Relation)complexFunction).
			    		setAlgebraic(objectFactory.createAlgebraic());
	    			((Relation)complexFunction).getAlgebraic().
			    		setUnextended(false);
	    		}catch(final Exception ex2) {}
	    	}
	    }
	}else {
	    if(complexFunction instanceof Activity) {
		try {
		    ((Activity)complexFunction).
			getProperties().setUnextended(true);
		}catch(final Exception ex) {
		    try {
			((Activity)complexFunction).
			    setProperties(objectFactory.createProperties());
			((Activity)complexFunction).
			    getProperties().setUnextended(true);
		    }catch(final Exception ex2) {}
		}
	    }else if(complexFunction instanceof Relation) {
		try {
		    ((Relation)complexFunction).
			getAlgebraic().setUnextended(true);
		}catch(final Exception ex) {
		    try {
			((Relation)complexFunction).
			    setAlgebraic(objectFactory.createAlgebraic());
			((Relation)complexFunction).
			    getAlgebraic().setUnextended(true);
		    }catch(final Exception ex2) {}
		}
	    }
	}
	removeSelection();
	rebuild();
    }
    /**
     * This method has intentionally left empty.
     */
    public void attributeChanged() {}
    /**
     * Removes selection from this cell's graph container.
     */
    public void removeSelection() {
	if(myListener != null) {
	    myListener.removeSelection();
	}
    }
    /**
     * Rebuilds the contents of this celll's graph container.
     */
    public void rebuild() {
	if(myListener != null) {
	    myListener.rebuild();
	}
    }
    /**
     * This method has intenionally left empty.
     */
    public void upPack() {}
    /**
     * Sets the list.
     *
     * @param list the concerned list
     */
    public void setList(final JList<AttributePanel> list) {
	if(myListener != null) {
	    myListener.setList(list);
	}
    }
    /**
     * Sets the attribute typ color of this operation cell.
     */
    public void setAttributeTypeColor() {
	if(myListener != null) {
	    myListener.setAttributeTypeColor();
	}
    }
    /**
     * Sets the color of the attribute types of this cell.
     *
     * @param uAtt the unique attribute types
     * @param uAttC the attribute type's corresponding color
     */
    public void setAttributeTypeColor(final String[] uAtt, final Color[] uAttC) {
    	extensionCell.setAttributeTypeColor(uAtt, uAttC);
    }
    /**
     * Sets attributes of the specified type
     * visible or invisible of this cell.
     *
     * @param type specifies concerned attribute type
     * @param visible if <code>true</code> the attribute is set visible,
     * otherwise <code>false</code>
     */
    public void setAttributeTypeVisible(final String type, final boolean visible) {
    	extensionCell.setAttributeTypeVisible(type, visible);
    }
    /**
     * Invoked when the mouse button has been clicked on this cell.
     */
    public void mouseClicked(final MouseEvent e)
    {
	//DEBUG
	//System.out.println("OperationCellItem.mouseClicked()");
	//END DEBUG
	if(expand.getText() == ">>")
	    {
		setExtended(true);
	    }
	else if(expand.getText() == "<<")
	    {
		setExtended(false);
	    }
    }
    /**
     * Invoked when the mouse enters this cell.
     * <p>
     * This method has intentionally left empty.
     */
    public void mouseEntered(final MouseEvent e){}
    /**
     * Invoked when the mouse exits this cell.
     * <p>
     * This method has intentionally left empty.
     */
    public void mouseExited(final MouseEvent e) {}
    /**
     * Invoked when a mouse butotn has been pressed on this cell.
     * <p>
     * This method has intentionally left empty.
     */
    public void mousePressed(final MouseEvent e) {}
    /**
     * Invoked when mouse button has been released on this cell.
     * <p>
     * This method has intentionally left empty.
     */
    public void mouseReleased(final MouseEvent e) {}

    /**
     * Represents the extension panel which is a part include all attributes.
     * <p>
     * The extension panel is a subpart of the operation cell.
     */
    class ExtensionCell extends JPanel
    {
        private static final long serialVersionUID = 1L;

        RowPainter[] rows = new RowPainter[0];
	/**
	 * Creates a new instance of this class that will be associated with
	 * the specified object.
	 * <p>
	 * Adds the specified attribute listener to recieve attribute events.
	 *
	 * @param expression the object this instance will be associated with
	 * @param l the attribute listener
	 */
    public ExtensionCell(final Object expression, final AttributeListener l)
	{
	    setBorder(new BevelBorder(BevelBorder.RAISED));
	    setLayout(null);
	    rows = new RowPainter[Converter.numOfActivities(expression)];
	    int tmpWidth = 0;
	    int tmpHeight = 0;
	    int i = 0;
	    while(i < rows.length)
		{
		    rows[i] = new RowPainter(Converter.
					     getActivityAt(expression,i));
		    rows[i].addListener(l);
		    rows[i].setBounds(0, tmpHeight,
				      rows[i].getSize().width,
				      rows[i].getSize().height);
		    if(rows[i].getSize().width > tmpWidth) {
			tmpWidth = rows[i].getSize().width;
		    }
		    if((tmpHeight + rows[i].getSize().height) > tmpHeight) {
			tmpHeight += rows[i].getSize().height;
		    }
		    add(rows[i]);

		    i++;
		}
	    setSize(tmpWidth, tmpHeight);
	}
	/**
	 * Updates the size of this extension panel.
	 */
	public void update() {
	    //DEBUG
	    //System.out.println("OperationCellItem.ExtensionCell.update()");
	    //END DEBUG
	    int tmpWidth = 0;
	    int tmpHeight = 0;
	    for(int i = 0; i < rows.length; i++) {
		rows[i].update();
		if(i == 0) {
		    tmpWidth = rows[0].getSize().width;
		    tmpHeight = rows[0].getSize().height;
		}else {
		    if(rows[i].getSize().width > tmpWidth) {
			tmpWidth = rows[i].getSize().width;
		    }
		    if((tmpHeight+rows[i].getSize().height) > tmpHeight) {
			tmpHeight += rows[i].getSize().height;
		    }
		}
	    }
	    setSize(tmpWidth, tmpHeight);
	}
	/**
	 * Sets the color of the attribute types of this extension panel.
	 *
	 * @param uAtt the unique attribute types
	 * @param uAttC the attribute type's corresponding color
	 */
	public void setAttributeTypeColor(final String[] uAtt, final Color[] uAttC) {
	    //DEBUG
	    //System.out.println("OperationCellItem.ExtensionCell.setUniqueAttributes()");
	    //END DEBUG
	    for(int i = 0; i < rows.length; i++) {
		rows[i].setAttributeTypeColor(uAtt, uAttC);
	    }
	}
	/**
	 * Sets attributes of the specified type
	 * visible or invisible in this extension panel.
	 *
	 * @param type specifies concerned attribute type
	 * @param visible if <code>true</code> the attribute is set visible,
	 * otherwise <code>false</code>
	 */
	public void setAttributeTypeVisible(final String type, final boolean visible) {
	    for(int i = 0; i < rows.length; i++) {
		rows[i].setAttributeTypeVisible(type, visible);
	    }
	}
    }
}



