//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventParameterEditorDialog
//###########################################################################
//# $Id: SimpleParameterEditorDialog.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.expr.SimpleIdentifierProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.module.SimpleComponentType;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import java.util.Vector;

public class SimpleParameterEditorDialog extends JDialog implements ActionListener, ItemListener {
    private final JTextField name = new JTextField(16);
    private final JButton okButton = new JButton("OK");
    private ButtonGroup group = new ButtonGroup();
    private ModuleWindow root = null;
    private DefaultListModel data = null;
    private JList dataList = null;
    private JCheckBox requiredBox = null;
    private boolean isRequired = false;
    private final JTextField defaultText = new JTextField(16);

    public SimpleParameterEditorDialog(ModuleWindow root) {
	setTitle("Simple Parameter Editor");
	this.root = root;

	// TODO: Change the selection mode for the JList component (Single selection)

	// Center this element on the screen
	setModal(true);
	setLocationRelativeTo(null);

	okButton.setActionCommand("OK");
	
	JPanel contentPanel = new JPanel();
	Box b = new Box(BoxLayout.PAGE_AXIS);
	contentPanel.add(b);

	JPanel r1 = new JPanel();
	b.add(r1);
	r1.add(new JLabel("Name: "));
	r1.add(name);	

	JPanel r2 = new JPanel();
	b.add(r2);
	r2.add(requiredBox = new JCheckBox("Required?",false));
	requiredBox.addItemListener(this);

	JPanel r3 = new JPanel();
	b.add(r3);
	r3.add(new JLabel("Default Value: "));
	r3.add(defaultText);

	JRadioButton integerButton, rangeButton;

	group.add(integerButton = new JRadioButton("Integer Expression"));
	group.add(rangeButton = new JRadioButton("Range"));

	integerButton.setSelected(true);
	integerButton.setActionCommand("integer");
	rangeButton.setActionCommand("range");

	JPanel buttons = new JPanel();
	buttons.setLayout(new GridLayout(1,2));

	buttons.add(integerButton);
	buttons.add(rangeButton);

	b.add(buttons);

	JButton cancelButton = new JButton("Cancel");
	JPanel r4 = new JPanel();
	r4.add(okButton);
	okButton.setActionCommand("okbutton");
	okButton.addActionListener(this);
	r4.add(cancelButton);
	cancelButton.setActionCommand("cancelbutton");
	cancelButton.addActionListener(this);
	b.add(r4);

	setContentPane(contentPanel);
	pack();
	show();
    }
    
    public void actionPerformed(ActionEvent e) {
	if("okbutton".equals(e.getActionCommand())) {
	    ExpressionParser parser = null;
	    SimpleExpressionProxy expr = null;

	    try {
		if(name.getText().length() != 0) {
		    parser = new ExpressionParser();
		    expr = parser.parse(name.getText(), SimpleExpressionProxy.TYPE_NAME);
		    root.logEntry("Event name passed validation: " + name.getText());
		}
		else {
		    JOptionPane.showMessageDialog(this, "Invalid identifier");
		    root.logEntry("Event name was found to be invalid: " + name.getText());
		}
	    } catch(final ParseException exception) {
		ErrorWindow ew = new ErrorWindow("Parse error: " + exception.getMessage(),
						 name.getText(),
						 exception.getPosition());
		root.logEntry("ParseException in event name: " + exception.getMessage());
		return;
	    }

	    try {
		parser = new ExpressionParser();
		if(defaultText.getText().length() != 0) {
		    if(group.getSelection().getActionCommand().equals("integer")) {
			expr = parser.parse(defaultText.getText(), SimpleExpressionProxy.TYPE_INT);
		    }
		    if(group.getSelection().getActionCommand().equals("range")) {
			expr = parser.parse(defaultText.getText(), SimpleExpressionProxy.TYPE_RANGE);
		    }
		}
		else {
		    JOptionPane.showMessageDialog(this, "Parameter must have a default value!");
		    root.logEntry("Parameter must have a default value!");
		    return;
		}
	    } catch(final ParseException exception) {
		ErrorWindow ew = new ErrorWindow("Parse error: " + exception.getMessage(),
						 defaultText.getText(),
						 exception.getPosition());
		root.logEntry("ParseException in event range: " + exception.getMessage());
		return;
	    }

	    SimpleParameterProxy sp = null;

	    if(group.getSelection().getActionCommand().equals("integer")) {
		sp = new IntParameterProxy(name.getText(), null, isRequired);
		((IntParameterProxy)sp).setDefault(expr);
	    }
	    if(group.getSelection().getActionCommand().equals("range")) {
		sp = new RangeParameterProxy(name.getText(), null, isRequired);
		((RangeParameterProxy)sp).setDefault(expr);
	    }

	    root.getModuleProxy().getParameterList().add(sp);
	    root.getParameterDataList().add(root.getParameterDataList().getSize(), sp);
	    dispose();
	}
	if("cancelbutton".equals(e.getActionCommand())) {
	    dispose();
	}

    }

    public void itemStateChanged(ItemEvent e) {
	Object source = e.getItemSelectable();
	
	if (source == requiredBox) {
	    isRequired = !isRequired;
	}
    }
}


