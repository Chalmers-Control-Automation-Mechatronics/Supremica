package org.supremica.external.shoefactory.plantBuilder;

// import org.jgrafchart.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import com.nwoods.jgo.*;
// import org.jgrafchart.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
// import org.jgrafchart.Transitions.*;
// import org.jgrafchart.Actions.*;
import java.util.*;

public class JGrafbuilder
{
//	private GrafchartStorage topGrafcharts = new GrafchartStorage();

	public JGrafbuilder()
	{
/*
		Basic2GC bla = new Basic2GC();
		GCDocument doc = new GCDocument();
		doc.setName("ShoeFactory");
		doc.setPaperColor(new Color(0xFF, 0xFF, 0xDD));

		final GCView view = new GCView(doc);
		//JFrame frame = new JFrame(doc.getName());
		final JInternalFrame frame = new JInternalFrame(doc.getName(),true,true,true);
		frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		frame.setSize(600, 600);
		frame.show();

		bla.getDesktop().add(frame);

		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(view);

		GCStepInitial GCI = new GCStepInitial(new Point(100,30),"Start");
		GCStep GC = new GCStep(new Point(100,160),"Steg1");

		GCTransition GCT1 = new GCTransition(new Point(100,110),"Trans1");
		GCTransition GCT2 = new GCTransition(new Point(100,240),"Trans2");

		BooleanVariable var1 = new BooleanVariable(new Point(50,400));
		IntegerVariable var2 = new IntegerVariable(new Point(150,400));
		StringVariable var3 = new StringVariable(new Point(250,400));

		//GCI.showActionBlock();
		GC.showActionBlock();
		GCT1.setLabelText("1");
		GCT2.setLabelText("1");

		GCT1.addPrecedingStep(GCI);
		GCT1.addSucceedingStep(GC);
		GCI.addSucceedingTransition(GCT1);
		GC.addPrecedingTransition(GCT1);
		GC.addSucceedingTransition(GCT2);
		GCI.addPrecedingTransition(GCT2);
		GCT2.addPrecedingStep(GC);
		GCT2.addSucceedingStep(GCI);

		view.newLink(GCI.getOutPort(), GCT1.getInPort());
		view.newLink(GCT1.getOutPort(), GC.getInPort());
		view.newLink(GC.getOutPort(), GCT2.getInPort());
		view.newLink(GCT2.getOutPort(), GCI.getInPort());

		doc.addObjectAtTail(GCI);
		doc.addObjectAtTail(GCT1);
		doc.addObjectAtTail(GC);
		doc.addObjectAtTail(GCT2);
		doc.addObjectAtTail(var1);
		doc.addObjectAtTail(var2);
		doc.addObjectAtTail(var3);

		topGrafcharts.add(doc);
		doc.setSpeed(300);

		org.jgrafchart.Transitions.SimpleNode n;
	//---------------Kompilering av transitions-----------------------
		bla.parser.ReInit(new StringReader(GCT1.getLabelText()));
		try
		{
			n = bla.parser.Start();
			GCT1.node = n;
		}
		catch (Throwable ex)
		{
			System.out.println("Oops");
		}

		bla.parser.ReInit(new StringReader(GCT2.getLabelText()));
		try
		{
			n = bla.parser.Start();
			GCT2.node = n;
		}
		catch (Throwable ex)
		{
			System.out.println("Oops");
		}
	//----------------------------------------------------------------

		org.jgrafchart.Actions.SimpleNode n1;
	//---------------Kompilering av steps-----------------------
		//bla.actionParser.ReInit(new StringReader(GC.myActionLabel.getText()));
		try
		{
			n1 = bla.actionParser.Statement();
			GCI.node = n1;
		}
		catch (Exception ex)
		{
			System.out.println("Oops");
		}

		//bla.actionParser.ReInit(new StringReader(GC.myActionLabel.getText()));
		try
		{
			n1 = bla.actionParser.Statement();
			GC.node = n1;
		}
		catch (Exception ex)
		{
			System.out.println("Oops");
		}

		GCI.node.compile(topGrafcharts.getStorage());
		GC.node.compile(topGrafcharts.getStorage());

	//----------------------------------------------------------------
		view.compiledOnce = true;

		//nollställer variabler
		var1.setStoredBoolAction(false);
		var2.setStoredIntAction(0);
		var3.setStoredStringAction("hej");

		//starta simulering
		GCI.activate();
		view.start();
		//bla.updateActions();


		//GC.myActionLabel.setText("S i=0;"); funkar bara om public myActionLabel i GCStep.java
		//var1.myName.setText("test");funkar bara om public myName i InternalVariable.java
*/
	}
}
