package net.sourceforge.waters.mbt.translator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.regex.*;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

public class ToModelJUnit extends AbstractModuleProxyVisitor {

	private String fPath;

	private String packagePath;

	private String mFSMname;

	private String mIniNode;

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
				.getComponentList().get(2);

	}

	protected void setGraphProxy() throws JAXBException, SAXException,
			WatersUnmarshalException, IOException, VisitorException {

		mGraphProxy = mSimpleComponentProxy.getGraph();

	}

	public void setFSMname() {

		mFSMname = mSimpleComponentProxy.getName().toUpperCase() + "_";

	}

	public String getFSMname() {

		return mFSMname;

	}

	// public void setNodes() {
	// Object[] oNodes;
	// oNodes = mGraphProxy.getNodes().toArray();
	// mNodes = new String[oNodes.length];
	// for (int i = 0; i < oNodes.length; i++) {
	// mNodes[i] = oNodes[i].toString();
	// }
	// }
	public void setIniNode(String s) {

		mIniNode = s;
	}

	public String getIniNode() {

		return mIniNode;
	}

	protected void createJava() {

		String begin;
		String end;
		String event;

		LabelBlockProxy lb = null;
		List<Proxy> el = null;

		EdgesHashMap eHM = new EdgesHashMap();
		Vector<String> vEvents = new Vector<String>();
		LinkedList EdgesList;
		EdgeNode EdgeN;

		// e.getGuardActionBlock().getGuard() etc. ... }

		for (EdgeProxy e : mGraphProxy.getEdges()) {
			begin = e.getSource().toString();

			Pattern p1 = Pattern.compile("^initial");
			Matcher m1 = p1.matcher(begin);

			if (m1.find()) {
				mIniNode = m1.replaceAll("").trim();
				begin = m1.replaceAll("").trim();
			} else {

				// Initial not found error

			}

			end = e.getTarget().toString();

			Matcher m2 = p1.matcher(end);

			if (m2.find()) {

				end = m2.replaceAll("").trim();
			}

			lb = e.getLabelBlock();
			el = (List<Proxy>) lb.getEventList();
			for (Proxy p : el) {

				event = p.toString();

				eHM.add(event, begin, end);
				if (vEvents.indexOf(event) < 0) {

					vEvents.add(event);
				}
			}

		}

		// Head
		StringBuffer javaString = new StringBuffer();
//		javaString.append("package ").append(packagePath).append(";\n\n");
		javaString.append("import net.sourceforge.czt.modeljunit.*;\n\n");

		javaString.append("public class " + mFSMname + "FSM implements FsmModel\n");
		javaString.append("{\n\n");
		javaString.append(" private String state;\n\n");
		javaString.append(" public " + mFSMname + "FSM() \n");
		javaString.append(" { state = \"" + mIniNode + "\"; } \n\n");
		javaString.append(" public String getState() \n");
		javaString.append(" { return state; } \n\n");

		javaString.append(" public void reset(boolean testing) \n");
		javaString.append(" { state = \"" + mIniNode + "\";} \n\n");

		// Body
		for (int i = 0; i < vEvents.size(); i++) {

			event = vEvents.elementAt(i).toString();

			EdgesList = (LinkedList) eHM.get(event);
			if (EdgesList.size() == 1) {

				EdgesList.get(0);

				EdgeN = (EdgeNode) EdgesList.get(0);
				begin = EdgeN.getBegin();
				end = EdgeN.getEnd();
				javaString.append(" public boolean " + event
						+ "Guard() { return state.equals(\"" + begin
						+ "\"); }\n");
				javaString.append(" public @Action void " + event + "()\n");
				javaString.append(" {\n");
				javaString.append("  System.out.println(\"" + event
						+ ": \" + state + \" --> " + end + "\");\n");
				javaString.append("  state = \"" + end + "\";\n");
				javaString.append(" }\n\n");

			} else if (EdgesList.size() > 1) {

				EdgeN = (EdgeNode) EdgesList.get(0);
				begin = EdgeN.getBegin();
				javaString.append(" public boolean " + event
						+ "Guard() { return state.equals(\"" + begin + "\")");
				for (int j = 1; j < EdgesList.size(); j++) {
					EdgeN = (EdgeNode) EdgesList.get(j);
					begin = EdgeN.getBegin();
					javaString.append(" || state.equals(\"" + begin + "\")");

				}
				javaString.append("; }\n");

				javaString.append(" public @Action void " + event + "()\n");
				javaString.append(" {\n");

				for (int k = 0; k < EdgesList.size(); k++) {
					EdgeN = (EdgeNode) EdgesList.get(k);
					begin = EdgeN.getBegin();
					end = EdgeN.getEnd();
					javaString.append("  if ( state.equals(\"" + begin + "\")){\n");
					javaString.append("    System.out.println(\"" + event
							+ ": \" + state + \" --> " + end + "\");\n");
					javaString.append("    state = \"" + end + "\";\n");
					javaString.append("  }\n\n");
				}
				javaString.append(" }\n\n");
			}

		}
		javaString.append("}\n");
		System.out.print(javaString);
	}

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
			System.out.println("--------------------------");
			translator.createJava();
		}
	}

}
