package net.sourceforge.waters.gui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.*;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

public class EditorAction extends JTextPane {

	private SimpleExpressionProxy mActionExpression;
	private ExpressionParser mParser;
	private ModuleSubjectFactory mFactory;
	private GuardActionBlockSubject mGuardActionBlock;
	private EditorGuardActionBlock mParent;
	
	public EditorAction(BinaryExpressionProxy action,
			EditorGuardActionBlock editorGuardActionBlock) 
	{
		this.setText(action.toString());
		this.setForeground(EditorColor.ACTIONCOLOR);
		this.setEditable(false);
		mParent = editorGuardActionBlock;
		mGuardActionBlock = editorGuardActionBlock.getGuardActionBlock();
		mFactory = ModuleSubjectFactory.getInstance();
		mParser = new ExpressionParser(mFactory,
				CompilerOperatorTable.getInstance());
		mActionExpression = action;
	}
	
	public EditorAction(String action,
			EditorGuardActionBlock editorGuardActionBlock) 
	{
		this.setText(action);
		this.setForeground(EditorColor.ACTIONCOLOR);
		this.setEditable(false);
		mParent = editorGuardActionBlock;
		mGuardActionBlock = editorGuardActionBlock.getGuardActionBlock();
		mFactory = ModuleSubjectFactory.getInstance();
		mParser = new ExpressionParser(mFactory,
				CompilerOperatorTable.getInstance());
		update();
	}
	
	public EditorAction(EditorGuardActionBlock parent) 
	{
		mParent = parent;
		this.setForeground(EditorColor.ACTIONCOLOR);
		this.setEditable(false);
		mGuardActionBlock = parent.getGuardActionBlock();
		mFactory = ModuleSubjectFactory.getInstance();
		mParser = new ExpressionParser(mFactory,
				CompilerOperatorTable.getInstance());
		mActionExpression = null;
	}

	private void update() 
	{
		SimpleExpressionProxy newExpression = null;
		
		if (this.getText().trim().equals("")) {
			mParent.remove(this);
			mParent.resizePanel();
			mParent.getParent().getParent().repaint();
			return;
		}
		
		//check syntax
		try 
		{
			newExpression = mParser.parse(this.getText());

			BinaryExpressionSubject action = new BinaryExpressionSubject(
							 ((BinaryExpressionSubject) newExpression).getOperator(),
							 ((BinaryExpressionSubject) newExpression).getLeft().clone(),
							 ((BinaryExpressionSubject) newExpression).getRight().clone());
			
			List<NamedProxy> actionList = 
				mGuardActionBlock.getActionList();
			actionList.remove(mActionExpression);
			actionList.add(action);
			
			mActionExpression = newExpression;
		} 
		catch (ParseException ex) 
		{
			//JOptionPane.showMessageDialog(this, ex.getMessage(), "Syntax error", 
			//							  JOptionPane.ERROR_MESSAGE); 
			// ex.printStackTrace();
			return;
		}
		
		mParent.resizePanel();
		mParent.panel.repaint();
	}
	
	public String toString() 
	{
		return super.toString();
	}

	public SimpleExpressionProxy getActionExpression() 
	{
		return mActionExpression;
	}
}
