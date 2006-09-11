package net.sourceforge.waters.mbt.translator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.regex.*;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.AbstractProductDESProxyVisitor;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

public class ToModelJUnit extends AbstractModuleProxyVisitor {

	private String fPath;

	private String packagePath;

	private String mFSMname;

	private String[] mNodes;

	private String mIniNode;

	private String[] mEvents;

	private String[] mEdges;

	protected DocumentManager mDocumentManager;

	protected JAXBModuleMarshaller mMarshaller;

	protected SimpleComponentProxy mSimpleComponentProxy;

	protected GraphProxy mGraphProxy;

	/**
	 * The output where the Java ModelJUnit class is written.
	 * 
	 */
	protected PrintWriter mWriter;

	// public void ToModelJUnit(String fPath, String packagePath) {
	// this.fPath = fPath;
	// this.packagePath = packagePath;
	// }

	public ToModelJUnit() {
	}

	// #########################################################################
	// # Creating a Document Manager
	protected DocumentManager getDocumentManager() throws JAXBException,
			SAXException {
		if (mDocumentManager == null) {
			final ModuleProxyFactory factory = ModuleElementFactory
					.getInstance();
			final OperatorTable optable = CompilerOperatorTable.getInstance();
			mMarshaller = new JAXBModuleMarshaller(factory, optable);
			mDocumentManager = new DocumentManager();
			mDocumentManager.registerMarshaller(mMarshaller);
			mDocumentManager.registerUnmarshaller(mMarshaller);
		}
		return mDocumentManager;
	}

	@Override
	public Object visitModuleProxy(final ModuleProxy proxy)
			throws VisitorException {
		return proxy;
	}

	protected void setSimpleComponentProxy(URL url) throws JAXBException,
			SAXException, WatersUnmarshalException, IOException,
			VisitorException {
		final DocumentManager manager = getDocumentManager();
		final ModuleProxy moduleproxy = (ModuleProxy) manager.load(url);
		moduleproxy.acceptVisitor(this);

		// Get the Component
		mSimpleComponentProxy = (SimpleComponentProxy) moduleproxy
				.getComponentList().get(3);

	}

	protected void setGraphProxy() throws JAXBException, SAXException,
			WatersUnmarshalException, IOException, VisitorException {

		mGraphProxy = mSimpleComponentProxy.getGraph();

	}

	public void setFSMname() {

		mFSMname = mSimpleComponentProxy.getName();

	}

	public String getFSMname() {

		return mFSMname;

	}

	public void setNodes() {
		Object[] oNodes;
		oNodes = mGraphProxy.getNodes().toArray();
		mNodes = new String[oNodes.length];
		for (int i = 0; i < oNodes.length; i++) {
			mNodes[i] = oNodes[i].toString();
		}
	}

	public void setIniNode() {

		if (mNodes != null) {

			for (int i = 0; i < mNodes.length; i++) {
				String s = mNodes[i];
				// System.out.println(s);
				Pattern p = Pattern.compile("^initial");
				Matcher m = p.matcher(s);

				if (m.find()) {

					// String[] result = p.split(s);
					// StringBuffer sb = new StringBuffer(20);
					mIniNode = m.replaceAll("").trim();
				} else {

					// Initial not found error

				}
			}

		}

	}

	public String getIniNode() {

		return mIniNode;
	}

	public void setEvents() {
		Object[] oEvents;
		oEvents = mGraphProxy.getNodes().toArray();
		mEvents = new String[oEvents.length];
		for (int i = 0; i < oEvents.length; i++) {
			mEvents[i] = oEvents[i].toString();
		}
	}

	public void setEdges() {
		Object[] oEdges;
		oEdges = mGraphProxy.getEdges().toArray();
		mEdges = new String[oEdges.length];
		for (int i = 0; i < oEdges.length; i++) {
			mEdges[i] = oEdges[i].toString();
		}
	}

	protected void createJava() {

		// Head
		StringBuffer head = new StringBuffer();
		head.append("package ").append(packagePath).append(";\n");

		head.append("public class " + mFSMname + "FSM implements FsmModel\n");
		head.append("{\n");
		head.append("private String state;\n");
		head.append(" public FSM() \n");
		head.append(" { state = " + mIniNode + "; } \n");
		head.append(" public String getState() \n");
		head.append("{ return state; } \n");

		head.append(" public void reset(boolean testing) \n");
		head.append("{ state = " + mIniNode + ";} \n");

		// Body
		String begin;
		String end;
		String event;
		EdgesHashMap eHM = new EdgesHashMap();
		Vector<String> vEvents = new Vector<String>();
		LinkedList EdgesList;
		EdgeNode EdgeN;

		for (String s : mEdges) {

			Pattern p = Pattern.compile("[->\\{\\}\\\n\\ ]+");

			String[] result = p.split(s);
			begin = result[0];
			end = result[1];
			event = result[2];
			eHM.add(event, begin, end);
			if (vEvents.indexOf(event) < 0) {

				vEvents.add(event);
			}

		}

		for (int i = 0; i < vEvents.size(); i++) {

			event = vEvents.elementAt(i).toString();

			EdgesList = (LinkedList) eHM.get(event);
			// Iterator ii = EdgesList.iterator();
			if (EdgesList.size() == 1) {

				// Iterator ii = EdgesList.iterator();
				// while (ii.hasNext()) {

				EdgesList.get(0);

				// EdgeN = (EdgeNode) ii.next();

				EdgeN = (EdgeNode) EdgesList.get(0);
				begin = EdgeN.getBegin();
				end = EdgeN.getEnd();
				head.append("public boolean " + event
						+ "Guard() { return state == " + begin + "; }\n");
				head.append("public @Action void " + event + "()\n");
				head.append("{\n");
				head.append(" System.out.println(\"" + event + ": state --> "
						+ end + "\");\n");
				head.append(" state = " + end + ";\n");
				head.append("}\n");

				// }

			} else if (EdgesList.size() > 1) {

				EdgeN = (EdgeNode) EdgesList.get(0);
				begin = EdgeN.getBegin();
				head.append("public boolean " + event
						+ "Guard() { return state == " + begin);
				for (int j = 1; j < EdgesList.size(); j++) {
					EdgeN = (EdgeNode) EdgesList.get(j);
					begin = EdgeN.getBegin();
					head.append(" || state == " + begin);

				}
				head.append("; }\n");

				head.append("public @Action void " + event + "()\n");
				head.append("{\n");

				for (int k = 0; k < EdgesList.size(); k++) {
					EdgeN = (EdgeNode) EdgesList.get(k);
					begin = EdgeN.getBegin();
					end = EdgeN.getEnd();
					head.append("if ( state == " + begin + "){\n");
					head.append(" System.out.println(\"" + event
							+ ": state --> " + end + "\");\n");
					head.append(" state = " + end + ";\n");
					head.append("}\n");
				}
				head.append("}\n");
			}

		}
		head.append("}\n");
		// System.out.println(eHM.keySet());
		System.out.print(head);
	}

	// if (begin != null && end != null && event != null){
	// head.append("public boolean " + event
	// + "Guard() { return state == " + begin + "; }\n");
	// head.append("public @Action void " + event + "()\n");
	// head.append("{\n");
	// head.append(" System.out.println(\"" + event + ": state --> "
	// + end + "\");\n");
	// head.append(" state = " + end + ";\n");
	// head.append("}\n");
	// }
	// else{
	// //No begin, end, event found error
	// break;
	// }

	// }
	// head.append("}\n");
	// System.out.print(head);

	public static void writeFile(String message, String path, boolean append) {
		FileWriter os = null;
		try {
			os = new FileWriter(path, append);
			os.write(message + System.getProperty("line.separator"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[]) throws Exception {
		if (args.length <= 0) {
			System.err.println("Args:  module.wmod");
		} else {
			System.out.println("url=" + new File(args[0]).toURL());
			ToModelJUnit translator = new ToModelJUnit();
			translator.setSimpleComponentProxy(new File(args[0]).toURL());
			translator.setGraphProxy();
			translator.setFSMname();
			// translator.setEvents();
			translator.setNodes();
			translator.setIniNode();
			translator.setEdges();

			// System.out.println(translator.getFSMname());
			// System.out.println(translator.getIniNode());
			// System.out.println("-------");
			// System.out.println(translator.mNodes[0]);
			// System.out.println(translator.mNodes[1]);
			// System.out.println(translator.mNodes[2]);

			// System.out.println(translator.mEdges[0]);
			// System.out.println(translator.mEdges[1]);
			// System.out.println(translator.mEdges[2]);
			//			
			// translator.createHead();
			System.out.println("--------------------------");
			translator.createJava();
			// Object[] s = translator.mGraphProxy.getNodes().toArray();
			// String s1 = s[2].toString();
			// System.out.print(s1);

			// System.out.println(translator.mNodes);
			// translator.translate(new File(args[0]).toURL());
		}
	}

}
