
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventListCell
//###########################################################################
//# $Id: SimpleExpressionCell.java,v 1.3 2005-02-18 03:09:06 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.ParseException;

public class SimpleExpressionCell
	extends JFormattedTextField
{

	//#########################################################################
	//# Constructors
	public SimpleExpressionCell()
	{
		this(SimpleExpressionProxy.TYPE_ANY);
	}

	public SimpleExpressionCell(final int mask)
	{
		this(null, mask);
	}

	public SimpleExpressionCell(final SimpleExpressionProxy expr)
	{
		this(expr, SimpleExpressionProxy.TYPE_ANY);
	}

	public SimpleExpressionCell(final SimpleExpressionProxy expr, final int mask)
	{
		mTypeMask = mask;
		mParser = new ExpressionParser();

		final DefaultFormatter formatter = new SimpleExpressionFormatter();
		final DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);

		setFormatterFactory(factory);

		final InputVerifier verifier = new SimpleExpressionVerifier();

		setInputVerifier(verifier);

		if (expr != null)
		{
			setValue(expr);
		}
	}

	//#########################################################################
	//# Local Class SimpleExpressionFormatter
	private class SimpleExpressionFormatter
		extends DefaultFormatter
	{

		//#######################################################################
		//# Constructors
		private SimpleExpressionFormatter()
		{
			setCommitsOnValidEdit(false);
			setAllowsInvalid(true);
		}

		//#######################################################################
		//# Overrides for class javax.swing.text.DefaultFormatter
		public Object stringToValue(final String input)
			throws java.text.ParseException
		{
			try
			{
				return mParser.parse(input, mTypeMask);
			}
			catch (final ParseException exception)
			{
				throw exception.getJavaException();
			}
		}
	}

	//#########################################################################
	//# Local Class SimpleExpressionVerifier
	private static class SimpleExpressionVerifier
		extends InputVerifier
	{

		//#######################################################################
		//# Overrides for class javax.swing.InputVerifier
		public boolean verify(final JComponent input)
		{
			if (input instanceof JFormattedTextField)
			{
				final JFormattedTextField textfield = (JFormattedTextField) input;

				try
				{
					textfield.commitEdit();

					return true;
				}
				catch (final java.text.ParseException exception)
				{
					final String msg = exception.getMessage();
					final String text = textfield.getText();
					final int pos = exception.getErrorOffset();
					final ErrorWindow window = new ErrorWindow(msg, text, pos);

					textfield.setCaretPosition(pos);

					return false;
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

	//#########################################################################
	//# Data Members
	private final int mTypeMask;
	private final ExpressionParser mParser;
}
