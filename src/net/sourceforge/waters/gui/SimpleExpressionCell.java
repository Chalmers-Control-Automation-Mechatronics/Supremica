//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventListCell
//###########################################################################
//# $Id: SimpleExpressionCell.java,v 1.5 2005-02-21 19:18:35 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ExpressionScanner;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.ParseException;



public class SimpleExpressionCell
	extends JFormattedTextField
{

	//#######################################################################
	//# Constructors
	public SimpleExpressionCell(final Frame root)
	{
		this(root, SimpleExpressionProxy.TYPE_ANY);
	}


	public SimpleExpressionCell(final Frame root, final int mask)
	{
		this(root, null, mask);
	}


	public SimpleExpressionCell(final Frame root,
								final SimpleExpressionProxy expr)
	{
		this(root, expr, SimpleExpressionProxy.TYPE_ANY);
	}


	public SimpleExpressionCell(final Frame root,
								final SimpleExpressionProxy expr,
								final int mask)
	{
		mRoot = root;
		mTypeMask = mask;
		mParser = new ExpressionParser();
		mFilter = new SimpleExpressionFilter();
		mVerifier = new SimpleExpressionVerifier();

		final DefaultFormatter formatter =
			new SimpleExpressionFormatter(false);
		final DefaultFormatter nullformatter =
			new SimpleExpressionFormatter(true);
		final DefaultFormatterFactory factory =
			new DefaultFormatterFactory(formatter, formatter,
										formatter, nullformatter);
		setFormatterFactory(factory);
		setInputVerifier(mVerifier);

		final Action action = new EnterAction();
		final Action[] actions = {action};
		final Keymap keymap = getKeymap();
		JTextComponent.loadKeymap(keymap, BINDINGS, actions);

		if (expr != null)
		{
			setValue(expr);
		}
	}



	//#######################################################################
	//# Input Verification
	public boolean verify()
	{
		return mVerifier.verify(this);
	}


	public void revert()
	{
		final Object oldvalue = getValue();
		setValue(oldvalue);
	}



	//#######################################################################
	//# Local Class SimpleExpressionFormatter
	private class SimpleExpressionFormatter
		extends DefaultFormatter
	{

		//###################################################################
		//# Constructors
		private SimpleExpressionFormatter(final boolean allownull)
		{
			mAllowNull = allownull;
			setCommitsOnValidEdit(false);
		}



		//###################################################################
		//# Overrides for class javax.swing.text.DefaultFormatter
		public Object stringToValue(final String input)
			throws java.text.ParseException
		{
			if (input.length() != 0) {
				try {
					return mParser.parse(input, mTypeMask);
				} catch (final ParseException exception) {
					throw exception.getJavaException();
				}
			} else if (mAllowNull) {
				return null;
			} else {
				throw new java.text.ParseException("Empty input!", 0);
			}
		}


		public String valueToString(final Object value)
		{
			if (value == null) {
				return "";
			} else {
				return value.toString();
			}
		}


		protected DocumentFilter getDocumentFilter()
		{
			return mFilter;
		}



		//###################################################################
		//# Data Members
		private final boolean mAllowNull;

	}



	//#######################################################################
	//# Local Class SimpleExpressionVerifier
	private class SimpleExpressionVerifier
		extends InputVerifier
	{

		//###################################################################
		//# Overrides for class javax.swing.InputVerifier
		public boolean verify(final JComponent input)
		{
			if (input instanceof JFormattedTextField)
			{
				final SimpleExpressionCell textfield =
					(SimpleExpressionCell) input;

				try
				{
					textfield.commitEdit();

					return true;
				}
				catch (final java.text.ParseException exception)
				{
					final String text = textfield.getText();
					if (text.length() == 0) {
						return false;
					}
					final boolean revert =
						ErrorWindow.askRevert(mRoot, exception, text);
					if (revert) {
						textfield.revert();
					} else {
						final int pos = exception.getErrorOffset();
						textfield.setCaretPosition(pos);
					}
					return revert;
				}
			}
			else
			{
				return true;
			}
		}

		public boolean shouldYieldFocus(final JComponent input)
		{
			return verify(input);
		}

	}



	//#######################################################################
	//# Local Class SimpleExpressionFilter
	private static class SimpleExpressionFilter extends DocumentFilter
	{

		//###################################################################
		//# Overrides for class javax.swing.DocumentFilter
		public void insertString(final DocumentFilter.FilterBypass bypass,
								 final int offset,
								 final String text,
								 final AttributeSet attribs)
			throws BadLocationException
		{
			final String filtered = filter(text);
			if (filtered != null) {
				super.insertString(bypass, offset, filtered, attribs);
			}
		}


		public void replace(final DocumentFilter.FilterBypass bypass,
							final int offset,
							final int length,
							final String text,
							final AttributeSet attribs)
			throws BadLocationException
		{
			final String filtered = filter(text);
			if (filtered != null) {
				super.replace(bypass, offset, length, filtered, attribs);
			}
		}



		//###################################################################
		//# Auxiliary Methods
		private String filter(final String text)
		{
			if (text == null) {
				return null;
			} else {
				final int len = text.length();
				final StringBuffer buffer = new StringBuffer(len);
				for (int i = 0; i < len; i++) {
					final char ch = text.charAt(i);
					if (ExpressionScanner.isExpressionCharacter(ch)) {
						buffer.append(ch);
					}
				}
				if (buffer.length() == 0) {
					return null;
				} else {
					return buffer.toString();
				}
			}
		}

	}



	//#######################################################################
	//# Local Class EnterAction
	/**
	 * This handles the <CODE>&lt;ENTER&gt;</CODE> key.
	 * When pressed, we need to verify the input and, if successful,
	 * fire an {@link ActionEvent} to notify any registered listeners.
	 * Other keys such as <CODE>&lt;TAB&gt;</CODE> are handled automatically
	 * as focus changes. 
	 */
	private class EnterAction extends AbstractAction
	{

		//###################################################################
		//# Constructors
		private EnterAction()
		{
			super(ACTNAME_ENTER);
		}



		//###################################################################
		//# Interface java.awt.event.ActionListener
		public void actionPerformed(final ActionEvent event)
		{
			if (verify()) {
				fireActionPerformed();
			}
		}

	}



	//#######################################################################
	//# Data Members
	private final Frame mRoot;
	private final int mTypeMask;
	private final ExpressionParser mParser;
	private final DocumentFilter mFilter;
	private final InputVerifier mVerifier;



	//#######################################################################
	//# Class Constants
	private static final String ACTNAME_ENTER = "SimpleExpressionCell.ENTER";
	private static final KeyStroke STROKE_ENTER =
		KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	private static final JTextComponent.KeyBinding BINDING_ENTER =
		new JTextComponent.KeyBinding(STROKE_ENTER, ACTNAME_ENTER);

	private static final JTextComponent.KeyBinding[] BINDINGS = {
		BINDING_ENTER
	};

}
