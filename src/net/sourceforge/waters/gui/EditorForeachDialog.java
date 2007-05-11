//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorForeachDialog
//###########################################################################
//# $Id: EditorForeachDialog.java,v 1.6 2007-05-11 02:44:46 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.*;
import java.util.Collection;
import java.util.Collections;
import javax.swing.*;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.module.ForeachComponentSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;


/**
 * <p>A dialog to help users create new foreach components in a module.</p>
 *
 * <p>This dialog creates new components when the "new" option is selected
 * in the module window.</p>
 *
 * @author Gian Perrone
 */

public class EditorForeachDialog
	extends JDialog
	implements ActionListener
{

	//#######################################################################
	//# Constructors
	public EditorForeachDialog(final ModuleWindowInterface root)
	{
		mRoot = root;
		setTitle("Foreach Component Editor");

		Box b = new Box(BoxLayout.PAGE_AXIS);
		JPanel r1 = new JPanel();
		JPanel r2 = new JPanel();
		JPanel r3 = new JPanel();
		JPanel r4 = new JPanel();
		JPanel r5 = new JPanel();

		b.add(r1);
		b.add(r2);
		b.add(r3);
		b.add(r4);
		b.add(r5);
		r1.add(new JLabel("<html><b>Foreach Component Editor</b></html>"));
		r2.add(new JLabel("Foreach "));
		r2.add(mNameInput);
		r3.add(new JLabel(" in "));
		r3.add(mRangeInput);
		r4.add(new JLabel(" where "));
		r4.add(mGuardInput);

		JButton b1 = new JButton("OK");
		JButton b2 = new JButton("Cancel");

		b1.setActionCommand("ok");
		b2.setActionCommand("cancel");
		b1.addActionListener(this);
		b2.addActionListener(this);

		r5.add(b1);
		r5.add(b2);

		setContentPane(b);
		pack();
		setLocationRelativeTo(mRoot.getRootWindow());
		setVisible(true);
		mNameInput.requestFocusInWindow();
	}

	public void actionPerformed(ActionEvent e)
	{
		String body = "";

		if ("ok".equals(e.getActionCommand())) {
			final ExpressionParser parser = mRoot.getExpressionParser();

			final String nameText = mNameInput.getText(); 
			try	{
				parser.parseSimpleIdentifier(nameText);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(exception, nameText);
				return;
			}

			final String rangeText = mRangeInput.getText();
			SimpleExpressionSubject rangeExpr = null;
			try	{
				rangeExpr = (SimpleExpressionSubject)
					parser.parse(rangeText, Operator.TYPE_RANGE);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(exception, rangeText);
				return;
			}

			final String guardText = mGuardInput.getText();
			SimpleExpressionSubject guardExpr = null;
			if (guardText.length() > 0) {
				try	{
					guardExpr = (SimpleExpressionSubject)
						parser.parse(guardText, Operator.TYPE_INT);
				} catch (final ParseException exception) {
					ErrorWindow.askRevert(exception, guardText);
					return;
				}
			}

			final Collection<Proxy> empty = Collections.emptyList();
			final ForeachComponentSubject foreach =
				new ForeachComponentSubject
				      (nameText, rangeExpr, guardExpr, empty);
			final ModuleSubject module = mRoot.getModuleSubject();
			// *** BUG *** must add to proper subtree !!!
			module.getComponentListModifiable().add(foreach);
			dispose();
		}

		if ("cancel".equals(e.getActionCommand()))
		{
			dispose();
			mNameInput.setText("");
		}
	}


	//#######################################################################
	//# Data Members
	private final JTextField mNameInput = new JTextField(10);
	private final JTextField mRangeInput = new JTextField(25);
	private final JTextField mGuardInput = new JTextField(25);
	private final ModuleWindowInterface mRoot;

}
