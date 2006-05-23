package net.sourceforge.waters.gui;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.*;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

public class EditorEditEdgeDialog extends JDialog
implements ActionListener, ItemListener{
	private static final int fieldHeight = 100;
	private static final int fieldWidth = 200;
	private JButton okButton, cancelButton;
	private JTextPane guardField, actionField;
	private final JLabel guardLabel = new JLabel("Guard:");
	private final JLabel actionLabel = new JLabel("Action:");
	private GridBagLayout layout;
	private ExpressionParser actionParser, guardParser;
	private ModuleSubjectFactory m = ModuleSubjectFactory.getInstance();
	private EdgeSubject edgeModel;
	
	public EditorEditEdgeDialog(EdgeSubject edge) {
		edgeModel = edge;
		actionParser = new ExpressionParser(m, CompilerOperatorTable.getInstance());
		guardParser = new ExpressionParser(m, GuardExpressionOperatorTable.getInstance());
		
		GridBagConstraints con = new GridBagConstraints();
		
		setModal(true);
		setLocationRelativeTo(null);
		this.setTitle("Edit Edge");
		
		//setup layout manager
		layout = new GridBagLayout();
		this.setLayout(layout);
		
		//Button panel
		FlowLayout buttonLayout = new FlowLayout(FlowLayout.RIGHT, 5, 5);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(buttonLayout);
		
		con.gridx = 2;
		con.gridy = 3;
		con.weightx = 0;
		con.weighty = 0;
		con.anchor = con.EAST;
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
		
		//cancel button
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		this.add(cancelButton);
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		
		//guard field
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 1;
		con.insets = new Insets(5,5,5,5);
		layout.setConstraints(guardLabel, con);
		add(guardLabel);
		
		guardField = new JTextPane();
		String guardText;
		if(edge.getGuardActionBlock() != null) {
			guardText = edge.getGuardActionBlock().getGuard();
		} else {
			guardText = "";
			edge.setGuardActionBlock(m.createGuardActionBlockProxy());
		}
		guardField.setText(guardText);
		guardField.setMargin(new Insets(5,5,5,5));
		JScrollPane scrollPaneG = new JScrollPane(guardField);
		scrollPaneG.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
		con.gridx = 2;
		con.gridy = 1;
		con.weightx = 1;
		con.weighty = 1;
		con.fill = GridBagConstraints.BOTH;
		con.insets = new Insets(5,5,5,5);
		layout.setConstraints(scrollPaneG, con);
		add(scrollPaneG);
		
		//action field
		con = new GridBagConstraints();
		con.gridx = 1;
		con.gridy = 2;
		con.insets = new Insets(5,5,5,5);
		layout.setConstraints(actionLabel, con);
		add(actionLabel);
		
		actionField = new JTextPane();
		List<BinaryExpressionProxy> actionList;
		String actionText = "";
		actionList = edge.getGuardActionBlock().getActionList();
		for(BinaryExpressionProxy action: actionList) {
			actionText = actionText + action + ";\n";
		}
		actionField.setText(actionText);
		actionField.setMargin(new Insets(5,5,5,5));
		JScrollPane scrollPaneA = new JScrollPane(actionField);
		scrollPaneA.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
		con.gridx = 2;
		con.gridy = 2;
		con.weightx = 1;
		con.weighty = 1;
		con.fill = GridBagConstraints.BOTH;
		con.insets = new Insets(5,5,5,5);
		layout.setConstraints(scrollPaneA, con);
		add(scrollPaneA);
		
		pack();
		setVisible(true);
	}
	
	public static void showDialog(EdgeSubject edge) {
		EditorEditEdgeDialog dialog = new EditorEditEdgeDialog(edge);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().equals("OK")) {
			//set guard
			try {
				guardParser.parse(guardField.getText());
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Syntax error", JDialog.DO_NOTHING_ON_CLOSE);
			}
			edgeModel.getGuardActionBlock().setGuard(guardField.getText());
			
			//set action
			String[] actions = actionField.getText().split(";");
			ListSubject<BinaryExpressionSubject> actionList = 
				new ArrayListSubject<BinaryExpressionSubject>();
			for(String action: actions) {
				BinaryExpressionSubject actionExpr;
				try {
					actionExpr = (BinaryExpressionSubject) actionParser.parse(action);
				} catch (ParseException e) {
					//e.printStackTrace();
					JOptionPane.showMessageDialog(this, e.getMessage(), "Syntax error", JDialog.DO_NOTHING_ON_CLOSE);
					actionExpr = null;
				} catch (ClassCastException e) {
					JOptionPane.showMessageDialog(this, "Incomplete action expression", "Syntax error", JDialog.DO_NOTHING_ON_CLOSE);
					actionExpr = null;
				}
				if(actionExpr != null) {
					actionList.add(actionExpr);
				}
			}
			edgeModel.getGuardActionBlock().setActionList(actionList);
			
			dispose();
		} else if (arg0.getActionCommand().equals("Cancel")) {
			dispose();
		}
	}
	
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
