//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventListCell
//###########################################################################
//# $Id: SimpleExpressionCell.java,v 1.4 2005-02-20 23:32:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ExpressionScanner;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.ParseException;



public class SimpleExpressionCell
	extends JFormattedTextField
	implements KeyListener
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

		final DefaultFormatter formatter = new SimpleExpressionFormatter(false);
		final DefaultFormatter nullformatter = new SimpleExpressionFormatter(true);
		final DefaultFormatterFactory factory =
			new DefaultFormatterFactory(formatter, formatter, formatter, nullformatter);

		setFormatterFactory(factory);
		setInputVerifier(mVerifier);
		addKeyListener(this);

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
		final Object value = getValue();
		final String goodtext = value.toString();
		setText(goodtext);
	}



	//#######################################################################
	//# Interface java.awt.event.KeyListener
	public void keyPressed(final KeyEvent event) 
	{
		if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			// For some reason, input is not checked automatically when
			// <enter> is pressed with invalid input. So we do it manually ...
			if (!verify()) {
				event.consume();
			}
		}
	}

	public void keyReleased(final KeyEvent event) 
	{
	}

	public void keyTyped(final KeyEvent event) 
	{
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
	//# Data Members
	private final Frame mRoot;
	private final int mTypeMask;
	private final ExpressionParser mParser;
	private final DocumentFilter mFilter;
	private final InputVerifier mVerifier;

}
