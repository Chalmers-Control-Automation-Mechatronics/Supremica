package net.sourceforge.waters.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.*;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

public class EditorAction extends JTextField 
	implements FocusListener{

	private SimpleExpressionProxy mActionExpression;
	private ExpressionParser mParser;
	private ModuleSubjectFactory mFactory;
	private GuardActionBlockSubject mGuardActionBlock;
	private EditorGuardActionBlock mParent;
	
	public EditorAction(BinaryExpressionProxy action,
			EditorGuardActionBlock editorGuardActionBlock) {
		super(" " + action.toString());
		mParent = editorGuardActionBlock;
		mGuardActionBlock = editorGuardActionBlock.getGuardActionBlock();
		mFactory = ModuleSubjectFactory.getInstance();
		mParser = new ExpressionParser(mFactory,
				CompilerOperatorTable.getInstance());
		mActionExpression = action;
		this.addFocusListener(this);
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
			e.printStackTrace();
		}
		
		BinaryExpressionSubject action = new BinaryExpressionSubject(
				((BinaryExpressionSubject) newExpression).getOperator(),
				((BinaryExpressionSubject) newExpression).getLeft().clone(),
				((BinaryExpressionSubject) newExpression).getRight().clone());
		mGuardActionBlock.removeAction(mActionExpression);
		mGuardActionBlock.addAction(action);
		mActionExpression = newExpression;

		mParent.resizePanel();
		mParent.panel.repaint();
	}
	
	public String toString() {
		return super.toString();
	}
	public void focusGained(FocusEvent arg0) {
		//do nothing
	}

	public void focusLost(FocusEvent arg0) {
		update();
	}

	public SimpleExpressionProxy getActionExpression() {
		return mActionExpression;
	}
}
