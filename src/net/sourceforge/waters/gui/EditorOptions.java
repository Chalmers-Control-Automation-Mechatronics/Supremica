
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorOptions
//###########################################################################
//# $Id: EditorOptions.java,v 1.5 2006-07-20 02:28:37 robi Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import net.sourceforge.waters.gui.renderer.EdgeProxyShape;

public class EditorOptions
	extends JDialog
{
	static final int GS_MIN = 4;
	static final int GS_MAX = 100;
	final private ButtonGroup gridShow = new ButtonGroup();
	final private JSlider gridSize;
	final private ButtonGroup controlPointsMove = new ButtonGroup();
	final private ButtonGroup nodesSnap = new ButtonGroup();
	final private String On = "On";
	final private String Off = "Off";
	final private EditorWindowInterface r;
	final private JFormattedTextField textField;

	public EditorOptions(EditorWindowInterface root)
	{
		super(root.getFrame());

		r = root;

		setTitle("Options");

		// Main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// GRID ON OR OFF
		JPanel subPanel = new JPanel();
		JLabel label = new JLabel("Grid");
		boolean selected = root.getControlledSurface().getShowGrid();
		JRadioButton button = new JRadioButton(On, selected);
		button.setActionCommand(On);
		button.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JRadioButton b = (JRadioButton) e.getSource();

				r.getControlledSurface().setShowGrid(b.isSelected());
				r.repaint();
			}
		});
		gridShow.add(button);
		subPanel.add(label);
		subPanel.add(button);
		button = new JRadioButton(Off, !selected);
		button.setActionCommand(Off);
		gridShow.add(button);
		subPanel.add(button);
		mainPanel.add(subPanel);

		// GRID SIZE
		subPanel = new JPanel();
		label = new JLabel("Grid Size");
		gridSize = new JSlider(GS_MIN, GS_MAX, root.getControlledSurface().getGridSize());
		gridSize.setMajorTickSpacing(4);
		gridSize.setMinorTickSpacing(4);
		java.text.NumberFormat numberFormat = java.text.NumberFormat.getIntegerInstance();
		NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setMinimum(new Integer(GS_MIN));
		formatter.setMaximum(new Integer(GS_MAX));
		textField = new JFormattedTextField(formatter);
		textField.setValue(new Integer(gridSize.getValue()));
		textField.setColumns(3);    //get some space
		textField.getActionMap().put("check", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!textField.isEditValid())
				{    //The text is invalid.
					Toolkit.getDefaultToolkit().beep();
					textField.selectAll();
				}
				else
				{
					try
					{    //The text is valid,
						textField.commitEdit();    //so use it.
					}
					catch (java.text.ParseException exc) {}
				}
			}
		});
		textField.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				if ("value".equals(e.getPropertyName()))
				{
					Number value = (Number) e.getNewValue();

					if ((gridSize != null) && (value != null))
					{
						gridSize.setValue(value.intValue());
						r.repaint();
					}
				}
			}
		});
		gridSize.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				gridSize.setValue((int) (Math.round((double) (gridSize.getValue()) / 4) * 4));
				r.getControlledSurface().setGridSize(gridSize.getValue());
				r.repaint();

				if (!gridSize.getValueIsAdjusting())
				{
					textField.setValue(new Integer(gridSize.getValue()));
				}
				else
				{    //value is adjusting; just set the text
					textField.setText(String.valueOf(gridSize.getValue()));
				}
			}
		});
		subPanel.add(label);
		subPanel.add(gridSize);
		subPanel.add(textField);
		mainPanel.add(subPanel);

		// CONTROL POINTS
		subPanel = new JPanel();
		label = new JLabel("Control Points move with nodes");
		selected = root.getControlledSurface().getControlPointsMove();
		button = new JRadioButton(On, selected);
		button.setActionCommand(On);
		button.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JRadioButton b = (JRadioButton) e.getSource();

				r.getControlledSurface().setControlPointsMove(b.isSelected());
			}
		});
		subPanel.add(label);
		subPanel.add(button);
		controlPointsMove.add(button);
		button = new JRadioButton(Off, !selected);
		button.setActionCommand(Off);
		subPanel.add(button);
		controlPointsMove.add(button);
		mainPanel.add(subPanel);

		// NODES SNAP
		subPanel = new JPanel();
		label = new JLabel("Nodes snap to grid");
		selected = root.getControlledSurface().getNodesSnap();
		button = new JRadioButton(On, selected);
		button.setActionCommand(On);
		button.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JRadioButton b = (JRadioButton) e.getSource();

				r.getControlledSurface().setNodesSnap(b.isSelected());
			}
		});
		subPanel.add(label);
		subPanel.add(button);
		nodesSnap.add(button);
		button = new JRadioButton(Off, !selected);
		button.setActionCommand(Off);
		subPanel.add(button);
		nodesSnap.add(button);
		mainPanel.add(subPanel);

		// EDGE ARROWS
		JPanel edgeArrowPanel = new JPanel();
		JLabel edgeArrowLabel = new JLabel("Edge arrow position");
		ButtonGroup edgeArrowButtons = new ButtonGroup();
		JRadioButton inTheMiddle = new JRadioButton("In the middle", !EdgeProxyShape.getArrowAtEnd());
		JRadioButton atTheEnd = new JRadioButton("At the end", EdgeProxyShape.getArrowAtEnd());
		edgeArrowButtons.add(inTheMiddle);
		edgeArrowButtons.add(atTheEnd);
		edgeArrowPanel.add(edgeArrowLabel);
		edgeArrowPanel.add(inTheMiddle);
		edgeArrowPanel.add(atTheEnd);
		mainPanel.add(edgeArrowPanel);
		inTheMiddle.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JRadioButton b = (JRadioButton) e.getSource();
				
				EdgeProxyShape.setArrowAtEnd(!b.isSelected());
				r.repaint();
			}
		});

		// OK-BUTTON
		subPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String command = gridShow.getSelection().getActionCommand();

				r.getControlledSurface().setShowGrid(command == On);

				command = controlPointsMove.getSelection().getActionCommand();

				r.getControlledSurface().setControlPointsMove(command == On);
				r.getControlledSurface().setGridSize(gridSize.getValue());
				setVisible(false);
			}
		});
		subPanel.add(okButton);
		mainPanel.add(subPanel);
		this.getContentPane().add(mainPanel);
		this.pack();
	}
}
