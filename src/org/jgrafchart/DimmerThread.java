package org.jgrafchart;



import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import java.awt.print.*;

import javax.swing.*;
import javax.swing.border.*;

import java.util.*;

import com.nwoods.jgo.*;


public class DimmerThread
	extends Thread
{

	JGoObject step;

	public DimmerThread(JGoObject s)
	{

		super();

		step = s;
	}

	public DimmerThread()
	{
		super();
	}

	public void run()
	{

		JGoEllipse token;

		if (step instanceof GCStep)
		{
			GCStep gcStep = (GCStep) step;

			token = gcStep.myToken;
		}
		else
		{
			if (step instanceof MacroStep)
			{
				MacroStep mStep = (MacroStep) step;

				token = mStep.myToken;
			}
			else
			{
				if (step instanceof ProcedureStep)
				{
					ProcedureStep pStep = (ProcedureStep) step;

					token = pStep.myToken;
				}
				else
				{
					token = new JGoEllipse();
				}
			}
		}

		try
		{
			sleep(((GCDocument) step.getDocument()).threadSpeed * ((GCDocument) step.getDocument()).dimTicks);
		}
		catch (InterruptedException e) {}

		// step.myToken.setPen(JGoPen.Null);
		if (token.getBrush() == JGoBrush.lightGray)
		{
			token.setBrush(JGoBrush.Null);
		}
	}
}
