
/*
 *  Copyright © Northwoods Software Corporation, 1998-2001. All Rights
 *  Reserved.
 *
 *  Restricted Rights: Use, duplication, or disclosure by the U.S.
 *  Government is subject to restrictions as set forth in subparagraph
 *  (c) (1) (ii) of DFARS 252.227-7013, or in FAR 52.227-19, or in FAR
 *  52.227-14 Alt. III, as applicable.
 *
 */
package org.jgrafchart;

import java.awt.*;
import javax.swing.*;
import com.nwoods.jgo.JGoText;
import java.awt.event.*;

public class TextPropsDialog
	extends JDialog
{
	JPanel panel1 = new JPanel();
	javax.swing.JButton OKButton = new javax.swing.JButton();
	javax.swing.JButton CancelButton = new javax.swing.JButton();
	javax.swing.JLabel label1 = new javax.swing.JLabel();
	javax.swing.JTextField heightField = new javax.swing.JTextField();
	javax.swing.JTextField xField = new javax.swing.JTextField();
	javax.swing.JLabel label2 = new javax.swing.JLabel();
	javax.swing.JTextField yField = new javax.swing.JTextField();
	javax.swing.JLabel label3 = new javax.swing.JLabel();
	javax.swing.JCheckBox visibleBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox selectableBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox resizableBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox draggableBox = new javax.swing.JCheckBox();
	javax.swing.JLabel label4 = new javax.swing.JLabel();
	javax.swing.JCheckBox editableBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox boldBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox italicBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox underlineBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox strikeBox = new javax.swing.JCheckBox();
	javax.swing.JTextField textField = new javax.swing.JTextField();
	javax.swing.JLabel label5 = new javax.swing.JLabel();
	javax.swing.JTextField faceNameField = new javax.swing.JTextField();
	javax.swing.ButtonGroup alignGroup = new javax.swing.ButtonGroup();
	javax.swing.JRadioButton alignLeftRadio = new javax.swing.JRadioButton();
	javax.swing.JRadioButton alignCenterRadio = new javax.swing.JRadioButton();
	javax.swing.JRadioButton alignRightRadio = new javax.swing.JRadioButton();
	javax.swing.JCheckBox multilineBox = new javax.swing.JCheckBox();
	javax.swing.JLabel label6 = new javax.swing.JLabel();
	javax.swing.JTextField fontSizeField = new javax.swing.JTextField();
	javax.swing.JButton textColorButton = new javax.swing.JButton();
	javax.swing.JButton backgroundColorButton = new javax.swing.JButton();
	javax.swing.JCheckBox transparentBox = new javax.swing.JCheckBox();
	javax.swing.JTextArea textArea = new javax.swing.JTextArea();
	javax.swing.JScrollPane textAreaScroll = new javax.swing.JScrollPane(textArea);
	javax.swing.JLabel classNameLabel = new javax.swing.JLabel();
	javax.swing.JCheckBox editSingle = new javax.swing.JCheckBox();
	javax.swing.JCheckBox selectBack = new javax.swing.JCheckBox();
	javax.swing.JCheckBox twoDScale = new javax.swing.JCheckBox();
	javax.swing.JCheckBox clipping = new javax.swing.JCheckBox();
	javax.swing.JCheckBox autoResize = new javax.swing.JCheckBox();
	BorderLayout borderLayout1 = new BorderLayout();
	Color myTextColor;
	Color myBkColor;
	public JGoText myObject;

	public TextPropsDialog(Frame frame, String title, boolean modal, JGoText obj)
	{
		super(frame, title, modal);

		try
		{
			jbInit();

			myObject = obj;

			UpdateDialog();
			pack();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public TextPropsDialog()
	{
		this(null, "", false, null);
	}

	void jbInit()
		throws Exception
	{
		panel1.setLayout(null);
		OKButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				OKButton_actionPerformed(e);
			}
		});
		CancelButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				CancelButton_actionPerformed(e);
			}
		});
		textColorButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				textColorButton_actionPerformed(e);
			}
		});
		backgroundColorButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				backgroundColorButton_actionPerformed(e);
			}
		});
		OKButton.addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				OKButton_keyPressed(e);
			}
		});
		CancelButton.addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				CancelButton_keyPressed(e);
			}
		});
		panel1.setMinimumSize(new Dimension(545, 310));
		panel1.setPreferredSize(new Dimension(545, 310));
		getContentPane().add(panel1);
		setTitle("Text Properties");
		OKButton.setText("OK");
		panel1.add(OKButton);
		OKButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		OKButton.setBounds(new Rectangle(168, 272, 79, 22));
		CancelButton.setText("Cancel");
		panel1.add(CancelButton);
		CancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		CancelButton.setBounds(new Rectangle(276, 272, 79, 22));

		// classNameLabel.setText("class name");
		// classNameLabel.setBounds(new Rectangle(8, 4, 389, 24));
		// panel1.add(classNameLabel);
		label1.setText("Height:");
		label1.setHorizontalAlignment(JLabel.RIGHT);
		label1.setBounds(new Rectangle(24, 36, 48, 24));
		panel1.add(label1);
		heightField.setEditable(false);
		heightField.setBounds(new Rectangle(84, 36, 36, 24));
		panel1.add(heightField);
		label2.setText("x:");
		label2.setHorizontalAlignment(JLabel.RIGHT);
		label2.setBounds(new Rectangle(24, 60, 48, 24));
		panel1.add(label2);
		xField.setBounds(new Rectangle(84, 60, 36, 24));
		panel1.add(xField);
		label3.setText("y:");
		label3.setHorizontalAlignment(JLabel.RIGHT);
		label3.setBounds(new Rectangle(24, 84, 48, 24));
		panel1.add(label3);
		yField.setBounds(new Rectangle(84, 84, 36, 24));
		panel1.add(yField);
		label6.setText("Font Size:");
		label6.setHorizontalAlignment(JLabel.RIGHT);
		label6.setBounds(new Rectangle(12, 108, 64, 24));
		panel1.add(label6);
		fontSizeField.setBounds(new Rectangle(84, 108, 36, 24));
		panel1.add(fontSizeField);
		label4.setText("Text:");
		label4.setHorizontalAlignment(JLabel.RIGHT);
		label4.setBounds(new Rectangle(132, 36, 40, 24));
		panel1.add(label4);
		textField.setBounds(new Rectangle(180, 36, 324, 24));
		panel1.add(textField);
		textAreaScroll.setBounds(new Rectangle(180, 36, 209, 67));
		panel1.add(textAreaScroll);
		label5.setText("Face:");
		label5.setHorizontalAlignment(JLabel.RIGHT);
		label5.setBounds(new Rectangle(136, 108, 36, 24));
		panel1.add(label5);
		faceNameField.setBounds(new Rectangle(180, 108, 324, 24));
		panel1.add(faceNameField);
		visibleBox.setText("Visible");
		visibleBox.setBounds(new Rectangle(24, 144, 96, 24));
		panel1.add(visibleBox);
		selectableBox.setText("Selectable");
		selectableBox.setBounds(new Rectangle(24, 168, 96, 24));
		panel1.add(selectableBox);
		resizableBox.setText("Resizable");
		resizableBox.setBounds(new Rectangle(24, 192, 96, 24));
		panel1.add(resizableBox);
		draggableBox.setText("Draggable");
		draggableBox.setBounds(new Rectangle(24, 216, 96, 24));
		panel1.add(draggableBox);
		twoDScale.setText("2D Scale");
		twoDScale.setBounds(new Rectangle(24, 240, 90, 24));
		panel1.add(twoDScale);
		autoResize.setText("AutoResize");
		autoResize.setBounds(new Rectangle(132, 144, 90, 24));
		panel1.add(autoResize);
		multilineBox.setText("Multiline");
		multilineBox.setBounds(new Rectangle(132, 168, 84, 24));
		panel1.add(multilineBox);
		clipping.setText("Clipping");
		clipping.setBounds(new Rectangle(132, 192, 90, 24));
		panel1.add(clipping);
		editableBox.setText("Editable");
		editableBox.setBounds(new Rectangle(132, 216, 84, 24));
		panel1.add(editableBox);
		editSingle.setText("Edit on Single Click");
		panel1.add(editSingle);
		editSingle.setBounds(new Rectangle(132, 240, 130, 24));
		boldBox.setText("Bold");
		boldBox.setBounds(new Rectangle(228, 144, 84, 24));
		panel1.add(boldBox);
		italicBox.setText("Italic");
		italicBox.setBounds(new Rectangle(228, 168, 84, 24));
		panel1.add(italicBox);
		underlineBox.setText("Underline");
		underlineBox.setBounds(new Rectangle(228, 192, 84, 24));
		panel1.add(underlineBox);
		strikeBox.setText("Strike");
		strikeBox.setBounds(new Rectangle(228, 216, 84, 24));
		panel1.add(strikeBox);
		alignLeftRadio.setText("Align Left");
		alignGroup.add(alignLeftRadio);
		alignLeftRadio.setBounds(new Rectangle(324, 144, 84, 24));
		panel1.add(alignLeftRadio);
		alignCenterRadio.setText("Center");
		alignGroup.add(alignCenterRadio);
		alignCenterRadio.setBounds(new Rectangle(324, 168, 84, 24));
		panel1.add(alignCenterRadio);
		alignRightRadio.setText("Align Right");
		alignGroup.add(alignRightRadio);
		alignRightRadio.setBounds(new Rectangle(324, 192, 84, 24));
		panel1.add(alignRightRadio);
		textColorButton.setText("Text Color...");
		textColorButton.setBackground(java.awt.Color.lightGray);
		textColorButton.setBounds(new Rectangle(420, 136, 117, 24));
		panel1.add(textColorButton);
		backgroundColorButton.setText("Background...");
		backgroundColorButton.setBackground(java.awt.Color.lightGray);
		backgroundColorButton.setBounds(new Rectangle(420, 166, 117, 24));
		panel1.add(backgroundColorButton);
		transparentBox.setText("Transparent");
		transparentBox.setBounds(new Rectangle(420, 192, 96, 24));
		panel1.add(transparentBox);
		selectBack.setText("Select Background");
		panel1.add(selectBack);
		selectBack.setBounds(new Rectangle(420, 216, 130, 24));
	}

	void UpdateDialog()
	{
		if (myObject == null)
		{
			return;
		}

		// classNameLabel.setText(myObject.getClass().getName());
		Rectangle rect = myObject.getBoundingRect();

		heightField.setText(String.valueOf(rect.height));

		Point pt = myObject.getLocation();    // dependent on alignment

		xField.setText(String.valueOf(pt.x));
		yField.setText(String.valueOf(pt.y));
		fontSizeField.setText(String.valueOf(myObject.getFontSize()));
		visibleBox.setSelected(myObject.isVisible());
		selectableBox.setSelected(myObject.isSelectable());
		resizableBox.setSelected(myObject.isResizable());
		draggableBox.setSelected(myObject.isDraggable());
		editableBox.setSelected(myObject.isEditable());
		boldBox.setSelected(myObject.isBold());
		italicBox.setSelected(myObject.isItalic());
		underlineBox.setSelected(myObject.isUnderline());
		strikeBox.setSelected(myObject.isStrikeThrough());
		textField.setText(myObject.getText());
		textArea.setText(myObject.getText());

		if (myObject.isMultiline())
		{
			textField.setVisible(false);
			textAreaScroll.setVisible(true);
		}
		else
		{
			textField.setVisible(true);
			textAreaScroll.setVisible(false);
		}

		multilineBox.setSelected(myObject.isMultiline());
		faceNameField.setText(myObject.getFaceName());

		int align = myObject.getAlignment();

		if (align == JGoText.ALIGN_LEFT)
		{
			alignLeftRadio.setSelected(true);
		}
		else if (align == JGoText.ALIGN_RIGHT)
		{
			alignRightRadio.setSelected(true);
		}
		else
		{
			alignCenterRadio.setSelected(true);
		}

		myTextColor = myObject.getTextColor();
		myBkColor = myObject.getBkColor();

		transparentBox.setSelected(myObject.isTransparent());
		editSingle.setSelected(myObject.isEditOnSingleClick());
		twoDScale.setSelected(myObject.is2DScale());
		clipping.setSelected(myObject.isClipping());
		autoResize.setSelected(myObject.isAutoResize());
		selectBack.setSelected(myObject.isSelectBackground());
	}

	void UpdateControl()
	{
		if (myObject == null)
		{
			return;
		}

		Point newpt = new Point(Integer.parseInt(xField.getText()), Integer.parseInt(yField.getText()));

		myObject.setLocation(newpt);    // do this before we change the alignment
		myObject.setFontSize(Integer.parseInt(fontSizeField.getText()));
		myObject.setVisible(visibleBox.isSelected());
		myObject.setSelectable(selectableBox.isSelected());
		myObject.setResizable(resizableBox.isSelected());
		myObject.setDraggable(draggableBox.isSelected());
		myObject.setEditable(editableBox.isSelected());
		myObject.setBold(boldBox.isSelected());
		myObject.setItalic(italicBox.isSelected());
		myObject.setUnderline(underlineBox.isSelected());
		myObject.setStrikeThrough(strikeBox.isSelected());

		if (myObject.isMultiline())
		{
			myObject.setText(textArea.getText());
		}
		else
		{
			myObject.setText(textField.getText());
		}

		myObject.setMultiline(multilineBox.isSelected());
		myObject.setFaceName(faceNameField.getText());

		int align;

		if (alignLeftRadio.isSelected())
		{
			align = JGoText.ALIGN_LEFT;
		}
		else if (alignRightRadio.isSelected())
		{
			align = JGoText.ALIGN_RIGHT;
		}
		else
		{
			align = JGoText.ALIGN_CENTER;
		}

		myObject.setAlignment(align);
		myObject.setTextColor(myTextColor);
		myObject.setBkColor(myBkColor);
		myObject.setTransparent(transparentBox.isSelected());
		myObject.setEditOnSingleClick(editSingle.isSelected());
		myObject.set2DScale(twoDScale.isSelected());
		myObject.setClipping(clipping.isSelected());
		myObject.setAutoResize(autoResize.isSelected());
		myObject.setSelectBackground(selectBack.isSelected());
	}

	public void addNotify()
	{

		// Record the size of the window prior to calling parents addNotify.
		Dimension d = getSize();

		super.addNotify();

		if (fComponentsAdjusted)
		{
			return;
		}

		// Adjust components according to the insets
		Insets insets = getInsets();

		setSize(insets.left + insets.right + d.width, insets.top + insets.bottom + d.height);

		Component components[] = getComponents();

		for (int i = 0; i < components.length; i++)
		{
			Point p = components[i].getLocation();

			p.translate(insets.left, insets.top);
			components[i].setLocation(p);
		}

		fComponentsAdjusted = true;
	}

	// Used for addNotify check.
	boolean fComponentsAdjusted = false;

	/**
	 * Shows or hides the component depending on the boolean flag b.
	 * @param b  if true, show the component; otherwise, hide the component.
	 * @see javax.swing.JComponent#isVisible
	 */
	public void setVisible(boolean b)
	{
		if (b)
		{
			Rectangle bounds = getParent().getBounds();
			Rectangle abounds = getBounds();

			setLocation(bounds.x + (bounds.width - abounds.width) / 2, bounds.y + (bounds.height - abounds.height) / 2);
		}

		super.setVisible(b);
	}

	void OKButton_actionPerformed(ActionEvent e)
	{
		OnOK();
	}

	void OnOK()
	{
		try
		{
			UpdateControl();
			this.dispose();    // Free system resources
		}
		catch (Exception e) {}
	}

	void CancelButton_actionPerformed(ActionEvent e)
	{
		OnCancel();
	}

	void OnCancel()
	{
		try
		{
			this.dispose();    // Free system resources
		}
		catch (Exception e) {}
	}

	void textColorButton_actionPerformed(ActionEvent e)
	{
		Color newcolor = JColorChooser.showDialog(this, "Foreground Color", myTextColor);

		if (newcolor != null)
		{
			myTextColor = newcolor;
		}
	}

	void backgroundColorButton_actionPerformed(ActionEvent e)
	{
		Color newcolor = JColorChooser.showDialog(this, "Foreground Color", myBkColor);

		if (newcolor != null)
		{
			myBkColor = newcolor;
		}
	}

	void OKButton_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
		{
			OnOK();
		}
		else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE)
		{
			OnCancel();
		}
	}

	void CancelButton_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
		{
			OnCancel();
		}
		else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE)
		{
			OnCancel();
		}
	}
}
