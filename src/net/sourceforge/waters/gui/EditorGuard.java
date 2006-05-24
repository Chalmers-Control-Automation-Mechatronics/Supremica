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
	
	public EditorGuard(String guard) {
		super();
		this.setText(guard.toString());
		this.setEditable(false);
		this.setForeground(EditorColor.GUARDCOLOR);
		this.setOpaque(false);
	}
	
	public String toString() {
		return super.toString();
	}
}
