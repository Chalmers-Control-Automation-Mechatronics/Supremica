package org.supremica.external.shoeFactory.plantBuilder;

import org.jgrafchart.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import com.nwoods.jgo.*;
import org.jgrafchart.GCStep;
import org.jgrafchart.GCTransition;
import org.jgrafchart.DigitalIn;
import org.jgrafchart.DigitalOut;
import org.jgrafchart.DigitalOut0;
import org.jgrafchart.DigitalOut1;
import org.jgrafchart.AppAction;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.jgrafchart.Transitions.*;
import org.jgrafchart.Actions.*;
import java.util.*;

public class JGrafbuilder
{

	final JInternalFrame frame;
	GrafchartStorage topGrafcharts = new GrafchartStorage();

	public	JGrafbuilder()
	{

		Basic2GC bla = new 	Basic2GC();

		GCDocument doc = new GCDocument();
		String t = "JGrafchart" + Integer.toString(1);

		doc.setName(t);
		topGrafcharts.add(doc);

		final GCView view = new GCView(doc);
	 	frame = new JInternalFrame(doc.getName(), true, true, true, true);

		GCStepInitial GCI =new GCStepInitial(new Point(200,30),"Start");
		GCStep GC =new GCStep(new Point(200,250),"Steg1");
		GCTransition GCT =new GCTransition(new Point(200,120),"Trans1");
		GCTransition GCT2 =new GCTransition(new Point(300,120),"Trans41");

		GCI.showActionBlock();
		GC.showActionBlock();
		GCT.setLabelText("1");
		GCT2.setLabelText("1");

		GCT.addPrecedingStep(GCI);
		GCT.addSucceedingStep(GC);
		GCI.addSucceedingTransition(GCT);
		GC.addPrecedingTransition(GCT);
		GC.addSucceedingTransition(GCT2);
		GCI.addPrecedingTransition(GCT2);
		GCT2.addPrecedingStep(GC);
		GCT2.addSucceedingStep(GCI);

		view.newLink(GCI.getOutPort(), GCT.getInPort());
		view.newLink(GCT.getOutPort(), GC.getInPort());
		view.newLink(GC.getOutPort(), GCT2.getInPort());
		view.newLink(GCT2.getOutPort(), GCI.getInPort());

		doc.addObjectAtTail(GCI);
		doc.addObjectAtTail(GCT);
		doc.addObjectAtTail(GC);
		doc.addObjectAtTail(GCT2);

		view.initialize(bla, frame);
		frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		frame.setSize(400, 600);

		bla.getDesktop().add(frame);
		frame.show();

		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(view);
		view.initializeDragDropHandling();


		ArrayList symbolList = topGrafcharts.getStorage();
		//GCT.testAndFire();

		//bla.compileDocument(doc, symbolList);
		//view.start();
	}
}