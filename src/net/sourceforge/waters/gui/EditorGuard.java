package net.sourceforge.waters.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

public class EditorGuard extends JTextPane {

	private String mGuardExpression;
	private ExpressionParser mParser;
	private ModuleSubjectFactory mFactory;
	private GuardActionBlockSubject mGuardActionBlock;
	private EditorGuardActionBlock mParent;
	
	public EditorGuard(String guard, EditorGuardActionBlock editorGuardActionBlock) {
		this.setText(guard.toString());
		this.setEditable(false);
		this.setForeground(EditorColor.GUARDCOLOR);
		this.setOpaque(false);
		mParent = editorGuardActionBlock;
		mGuardActionBlock = editorGuardActionBlock.getGuardActionBlock();;
		mFactory = ModuleSubjectFactory.getInstance();
		mParser = new ExpressionParser(mFactory,
				GuardExpressionOperatorTable.getInstance());
		update();
	}

	private void update() {
		SimpleExpressionProxy newExpression = null;
		
		if(this.getText().trim().equals("")) {
			mParent.remove(this);
			mParent.resizePanel();
			mParent.getParent().getParent().repaint();
			return;
		}
		
		//check syntax
		try {
			newExpression = mParser.parse(this.getText());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		mGuardActionBlock.setGuard(this.getText());
		mGuardExpression = this.getText();
		mParent.resizePanel();
		mParent.getParent().getParent().repaint();
	}
	
	public String toString() {
		return super.toString();
	}
}
