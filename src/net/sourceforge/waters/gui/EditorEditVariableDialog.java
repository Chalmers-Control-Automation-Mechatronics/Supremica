package net.sourceforge.waters.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.BooleanConstantSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.VariableSubject;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;

public class EditorEditVariableDialog extends JDialog
implements ActionListener{
	private static final int FIELDWIDTH = 100;
	private static final int DEFAULTLOWERINT = 0;
	private static final int DEFAULTUPPERINT = 1;
	private static final int DEFAULTINITIALINT = 0;
	private static final boolean DEFAULTBOOLEANINITIAL = false;
	JComboBox typeSelector;
	JLabel lName, lType;
	JTextField name, rangeLower, rangeUpper, integerInitial, integerMarked;
	JComboBox booleanInitial, booleanMarked;
	JButton cancelButton, okButton;
	JSeparator separator;
	JPanel integerPanel, booleanPanel;
	GridBagLayout layout;
	private VariableSubject mVariable;
	private ModuleSubjectFactory mFactory;
	private SimpleComponentSubject mComponent;
	private ModuleTree mTree;

	public EditorEditVariableDialog(VariableSubject variable, SimpleComponentSubject component,
			ModuleTree tree) {
		setModal(true);
		setLocationRelativeTo(null);

		mVariable = variable;
		mComponent = component;
		mTree = tree;

		mFactory = ModuleSubjectFactory.getInstance();
		GridBagConstraints con = new GridBagConstraints();

		//setup layout manager
		layout = new GridBagLayout();
		this.setLayout(layout);

		lName = new JLabel("Name:");
		con.insets = new Insets(5,5,5,5);
		con.gridx = 1;
		con.gridy = 1;
		con.weightx = 0;
		con.weighty = 1;
		layout.setConstraints(lName, con);
		add(lName);

		name = new JTextField();
		name.setEditable(true);
		name.setEnabled(true);
		//name.setPreferredSize(new Dimension(FIELDWIDTH, name.getHeight()));
		con.fill = GridBagConstraints.HORIZONTAL;
		con.gridx = 2;
		con.weightx = 1;
		layout.setConstraints(name, con);
		add(name);

		lType = new JLabel("Type:");
		con.gridx = 1;
		con.gridy = 2;
		con.weightx = 0;
		layout.setConstraints(lType, con);
		add(lType);

		String[] types = {"boolean", "integer"};
		typeSelector = new JComboBox(types);
		con.gridx = 2;
		con.weightx = 1;
		layout.setConstraints(typeSelector, con);
		add(typeSelector);
		typeSelector.addActionListener(this);

		separator = new JSeparator(JSeparator.HORIZONTAL);
		con.gridx = 1;
		con.gridy = 3;
		con.gridwidth = 2;
		layout.setConstraints(separator, con);
		add(separator);

		//setup editor panel for integer variables
		createIntegerPanel();
		con.gridx = 1;
		con.gridwidth = 2;
		con.gridy = 4;
		con.insets = new Insets(2,2,2,2);
		layout.setConstraints(integerPanel, con);
		integerPanel.setEnabled(true);
		add(integerPanel);

		//setup editor panel for boolean variables
		createBooleanPanel();
		layout.setConstraints(booleanPanel, con);
		add(booleanPanel);

		if(variable == null) {
			setTitle("New variable");
			//modify dialog according to variable type
			selectTypePanel();
		} else {
			setTitle("Edit variable");
			//modify dialog according to variable type
			if(variable.getType() instanceof BinaryExpressionProxy) {
				//integer variable
				typeSelector.setSelectedItem("integer");
			} else if(variable.getType() instanceof SimpleIdentifierProxy) {
				//boolean variable
				typeSelector.setSelectedItem("boolean");
			} else {
				System.err.println("EditorEditVariableDialog: illegal variable type");
			}
			selectTypePanel();
			name.setText(variable.getName());
		}

		//Button panel
		FlowLayout buttonLayout = new FlowLayout(FlowLayout.RIGHT, 5, 5);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(buttonLayout);

		con.gridx = 2;
		con.gridy = 5;
		con.weightx = 0;
		con.weighty = 0;
		con.anchor = GridBagConstraints.EAST;
		con.insets = new Insets(5,5,5,5);
		layout.setConstraints(buttonPanel, con);
		buttonPanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		this.add(buttonPanel);

		//ok button
		okButton = new JButton();
		okButton.setText("Ok");
		buttonPanel.add(okButton);
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		this.getRootPane().setDefaultButton(okButton);

		//cancel button
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		this.add(cancelButton);
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);

		pack();
		this.setResizable(false);
		setVisible(true);
	}

	private void selectTypePanel() {
		//modify dialog according to variable type
		if(typeSelector.getSelectedItem().equals("integer")) {
			integerPanel.setVisible(true);
			booleanPanel.setVisible(false);
		} else if(typeSelector.getSelectedItem().equals("boolean")) {
			integerPanel.setVisible(false);
			booleanPanel.setVisible(true);
		}
		pack();
	}

	private void createBooleanPanel() {
		booleanPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		booleanPanel.setLayout(layout);
		GridBagConstraints con = new GridBagConstraints();

		JLabel lInitial, lMarked;

		lInitial = new JLabel("Initial value:");
		lMarked = new JLabel("Marked value:");

		con.insets = new Insets(3,3,3,3);
		con.gridx = 1;
		con.gridy = 1;
		con.anchor = GridBagConstraints.WEST;
		layout.setConstraints(lInitial, con);
		booleanPanel.add(lInitial);

		con.gridy = 2;
		layout.setConstraints(lMarked, con);
		booleanPanel.add(lMarked);

		final String[] booleanValues = {"true", "false"};
		final String[] booleanValuesExt = {"none", "true", "false"};
		con.gridx = 2;
		con.gridy = 1;
		con.weightx = 1;
		con.fill = GridBagConstraints.HORIZONTAL;
		booleanInitial = new JComboBox(booleanValues);
		layout.setConstraints(booleanInitial, con);
		booleanPanel.add(booleanInitial);

		con.gridy = 2;
		booleanMarked = new JComboBox(booleanValuesExt);
		layout.setConstraints(booleanMarked, con);
		booleanPanel.add(booleanMarked);

		if(mVariable != null && mVariable.getType() instanceof SimpleIdentifierProxy) {
			//set initial value
			if(((BooleanConstantSubject) mVariable.getInitialValue()).isValue()) {
				booleanInitial.setSelectedItem("true");
			} else {
				booleanInitial.setSelectedItem("false");
			}
			//set marked value
			if(mVariable.getMarkedValue() != null) {
				if(((BooleanConstantSubject) mVariable.getMarkedValue()).isValue()) {
					booleanMarked.setSelectedItem("true");
				} else {
					booleanMarked.setSelectedItem("false");
				}
			} else {
				booleanMarked.setSelectedItem("none");
			}
		} else {
			booleanInitial.setSelectedItem(Boolean.toString(DEFAULTBOOLEANINITIAL));
		}
	}

	private void createIntegerPanel() {
		integerPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		integerPanel.setLayout(layout);
		GridBagConstraints con = new GridBagConstraints();

		JLabel lRange, lInitial, lMarked;

		lRange = new JLabel("Range:");
		lInitial = new JLabel("Initial value:");
		lMarked = new JLabel("Marked value:");

		con.insets = new Insets(3,3,3,3);
		con.anchor = GridBagConstraints.WEST;
		con.gridx = 1;
		con.gridy = 1;
		layout.setConstraints(lRange, con);
		integerPanel.add(lRange);

		con.gridy = 2;
		layout.setConstraints(lInitial, con);
		integerPanel.add(lInitial);

		con.gridy = 3;
		layout.setConstraints(lMarked, con);
		integerPanel.add(lMarked);

		con.anchor = GridBagConstraints.CENTER;
		con.gridx = 2;
		con.gridy = 1;
		con.weightx = 1;
		con.fill = GridBagConstraints.HORIZONTAL;
		rangeLower = new JTextField();
		rangeLower.setEditable(true);
		rangeLower.setEnabled(true);
		layout.setConstraints(rangeLower, con);
		integerPanel.add(rangeLower);

		con.gridx = 4;
		rangeUpper = new JTextField();
		rangeUpper.setEnabled(true);
		rangeUpper.setEditable(true);
		layout.setConstraints(rangeUpper, con);
		integerPanel.add(rangeUpper);

		con.fill = GridBagConstraints.NONE;
		con.gridx = 3;
		con.weightx = 0;
		JLabel to = new JLabel("-");
		layout.setConstraints(to, con);
		integerPanel.add(to);

		con.gridy = 2;
		con.gridx = 2;
		con.fill = GridBagConstraints.HORIZONTAL;
		integerInitial = new JTextField();
		layout.setConstraints(integerInitial, con);
		integerPanel.add(integerInitial);

		con.gridy = 3;
		integerMarked = new JTextField();
		layout.setConstraints(integerMarked, con);
		integerPanel.add(integerMarked);

		if(mVariable != null && mVariable.getType() instanceof BinaryExpressionProxy) {
			//set range values
			BinaryExpressionProxy range = (BinaryExpressionProxy) mVariable.getType();
			rangeLower.setText(Integer.toString(
					((IntConstantProxy) range.getLeft()).getValue()));
			rangeUpper.setText(Integer.toString(
					((IntConstantProxy) range.getRight()).getValue()));

			//set initial value
			integerInitial.setText(Integer.toString(
					((IntConstantProxy) mVariable.getInitialValue()).getValue()));

			//set marked value
			if(mVariable.getMarkedValue() != null) {
				integerMarked.setText(Integer.toString(
						((IntConstantProxy) mVariable.getMarkedValue()).getValue()));

			}
		} else {
			//enter default values
			//set range values
			rangeLower.setText(Integer.toString(DEFAULTLOWERINT));
			rangeUpper.setText(Integer.toString(DEFAULTUPPERINT));

			//set initial value
			integerInitial.setText(Integer.toString(DEFAULTINITIALINT));

		}
	}

	public static void showDialog(VariableSubject variable, SimpleComponentSubject component,
			ModuleTree tree) {
		new EditorEditVariableDialog(variable, component, tree);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("OK")) {
			//do some validity checking
			if (name == null || name.getText() == null || name.getText().trim().equals(""))
			{
				JOptionPane.showMessageDialog(this,
						"Variable name missing",
						"Input error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			int intLower=0, intUpper=0, intInitial=0, intMarked=0;
			if(typeSelector.getSelectedItem().equals("integer")) {
				try {
					intLower = new Integer(rangeLower.getText());
					intUpper = new Integer(rangeUpper.getText());
				}
				catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(this,
							"Error: Non-numerical range value",
							"Input error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					intInitial = new Integer(integerInitial.getText());
				}
				catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(this,
							"Error: Non-numerical initial value",
							"Input error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(!integerMarked.getText().trim().equals("")) {
					try {
						intMarked = new Integer(integerMarked.getText().trim());
					}
					catch(NumberFormatException ex) {
						JOptionPane.showMessageDialog(this,
								"Error: Non-numerical marked value",
								"Input error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(intMarked < intLower || intMarked > intUpper) {
						JOptionPane.showMessageDialog(this,
								"Error: Marked value outside specified range",
								"Input error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				if(intInitial < intLower || intInitial > intUpper) {
					JOptionPane.showMessageDialog(this,
							"Error: Initial value outside specified range",
							"Input error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			SimpleExpressionSubject type = typeSelector.getSelectedItem().equals("integer")
			? mFactory.createBinaryExpressionProxy(
					CompilerOperatorTable.getInstance().getBinaryOperator(".."),
					mFactory.createIntConstantProxy(intLower),
					mFactory.createIntConstantProxy(intUpper))
					: mFactory.createSimpleIdentifierProxy("boolean");

		    SimpleExpressionSubject initial, marked;
		    if(type instanceof BinaryExpressionProxy) {
		    	initial = mFactory.createIntConstantProxy(intInitial);
		    	if(!integerMarked.getText().trim().equals("")) {
		    		marked = mFactory.createIntConstantProxy(intMarked);
		    	} else {
		    		marked = null;
		    	}
		    } else if(type instanceof SimpleIdentifierSubject) {
		    	initial = mFactory.createBooleanConstantProxy(booleanInitial.getSelectedItem().equals("true"));
		    	if(!booleanMarked.getSelectedItem().equals("none")) {
		    		marked = mFactory.createBooleanConstantProxy(booleanMarked.getSelectedItem().equals("true"));
		    	} else {
		    		marked = null;
		    	}
		    } else {
		    	System.err.println("EditorEditVariableDialog: Unsupported variable type");
		    	return;
		    }

		    if(mVariable == null) {
		    	//create new variable
		    	// Check that a variable with the given name does not already exists
		    	for (VariableSubject currVariable : mComponent.getVariablesModifiable())
		    	{
					if (currVariable.getName().equalsIgnoreCase(name.getText()))
					{
						JOptionPane.showMessageDialog(this,
								"Variable " + name.getText() + " already exists",
								"Input error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

		    	mVariable = new VariableSubject(name.getText(), type, initial, marked);
		    	mComponent.getVariablesModifiable().add(mVariable);
		    	mTree.addVariable(mVariable);
		    } else {
		    	//edit existing variable
		    	mVariable.setName(name.getText());
		    	mVariable.setType(type);
		    	mVariable.setInitialValue(initial);
		    	mVariable.setMarkedValue(marked);
		    	mTree.updateSelectedNode();
		    }
		    dispose();
		}
		if(e.getActionCommand().equals("Cancel")) {
			dispose();
		}
		if(e.getSource() == typeSelector) {
			selectTypePanel();
		}
	}
}
