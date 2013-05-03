package org.supremica.external.processeditor.processgraph.opcell;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * Attribute renderer.
 */
class AttributeCellRenderer extends AttributePanel implements ListCellRenderer<AttributePanel>
{
	private static final long serialVersionUID = 1L;
	protected RowPainter myContainer = null;

	public AttributeCellRenderer(final RowPainter rp)
    {
	myContainer = rp;
	setOpaque(true);
	setLayout(null);
    }

    /**
     * Creates a new instance of this class.
     */
    public Component getListCellRendererComponent(
						  final JList<? extends AttributePanel> list,
						  final AttributePanel value,
						  final int index,
						  final boolean isSelected,
						  final boolean cellHasFocus)
    {
	//DEBUG
	//System.out.println("AttributeCellRenderer.getListCellRendererComponent()");
	//END DEBUG
	this.setValue(((AttributePanel)value).nameLabel.getText());
	this.setUpperIndicator(((AttributePanel)value).getUpperIndicator());
	this.setLowerIndicator(((AttributePanel)value).getLowerIndicator());
	this.setAttributeSize();
        if(isSelected) {
	    this.setBackground(Color.blue);
            this.nameLabel.setBackground(list.getSelectionBackground());
            this.nameLabel.setForeground(list.getSelectionForeground());
        }else {
	    this.setBackground(((AttributePanel)value).getAttributeColor());
            this.nameLabel.setBackground(list.getBackground());
            this.nameLabel.setForeground(list.getForeground());
        }
        return this;
    }
}
