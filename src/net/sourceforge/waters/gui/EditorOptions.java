
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorOptions
//###########################################################################
//# $Id: EditorOptions.java,v 1.2 2005-02-18 03:09:06 knut Exp $
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
	final private EditorWindow r;
	final private JFormattedTextField textField;

	public EditorOptions(EditorWindow root)
	{
		super(root);

		r = root;

		setTitle("Options");

		JLabel Label = new JLabel("Grid");
		boolean selected = root.getControlledSurface().getShowGrid();
		JRadioButton Button = new JRadioButton(On, selected);

		Button.setActionCommand(On);
		Button.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JRadioButton b = (JRadioButton) e.getSource();

				r.getControlledSurface().setShowGrid(b.isSelected());
				r.repaint();
			}
		});

		JPanel subPanel = new JPanel();
		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		gridShow.add(Button);
		subPanel.add(Label);
		subPanel.add(Button);

		Button = new JRadioButton(Off, !selected);

		Button.setActionCommand(Off);
		gridShow.add(Button);
		subPanel.add(Button);
		mainPanel.add(subPanel);

		Label = new JLabel("Grid Size");
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

		subPanel = new JPanel();

		subPanel.add(Label);
		subPanel.add(gridSize);
		subPanel.add(textField);
		mainPanel.add(subPanel);

		Label = new JLabel("Control Points move with nodes");
		selected = root.getControlledSurface().getControlPointsMove();
		Button = new JRadioButton(On, selected);

		Button.setActionCommand(On);
		Button.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JRadioButton b = (JRadioButton) e.getSource();

				r.getControlledSurface().setControlPointsMove(b.isSelected());
			}
		});

		subPanel = new JPanel();

		subPanel.add(Label);
		subPanel.add(Button);
		controlPointsMove.add(Button);

		Button = new JRadioButton(Off, !selected);

		Button.setActionCommand(Off);
		subPanel.add(Button);
		controlPointsMove.add(Button);
		mainPanel.add(subPanel);

		Label = new JLabel("Nodes snap to grid");
		selected = root.getControlledSurface().getNodesSnap();
		Button = new JRadioButton(On, selected);

		Button.setActionCommand(On);
		Button.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JRadioButton b = (JRadioButton) e.getSource();

				r.getControlledSurface().setNodesSnap(b.isSelected());
			}
		});

		subPanel = new JPanel();

		subPanel.add(Label);
		subPanel.add(Button);
		nodesSnap.add(Button);

		Button = new JRadioButton(Off, !selected);

		Button.setActionCommand(Off);
		subPanel.add(Button);
		nodesSnap.add(Button);
		mainPanel.add(subPanel);

		subPanel = new JPanel();

		JButton button = new JButton("OK");

		button.addActionListener(new ActionListener()
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
		subPanel.add(button);
		mainPanel.add(subPanel);
		this.getContentPane().add(mainPanel);
		this.pack();
	}
}
