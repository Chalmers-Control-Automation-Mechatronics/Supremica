package net.sourceforge.waters.mbt.translator;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.module.BinaryExpression;

public class ToModelJUnit extends AbstractModuleProxyVisitor {

	private URL wmodFileURL;

	int index;

	private String filePath;

	private String packagePath;

	private String mFSMname;

	private String mFullJavafilename;

	private String mFullJavafilename_main;

	private String mIniNode;

	private String mSUTname;

	private StringBuffer javaString;

	private StringBuffer javaString_Main;

	private StringBuffer javaString_Main_Complied;

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

	public ToModelJUnit(String s, int i, String fp, String SUTname)
			throws WatersUnmarshalException, VisitorException, JAXBException,
			SAXException, IOException {

		try {
			wmodFileURL = new File(s).toURL();
			// System.out.println("url=" + wmodFileURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		index = i;
		filePath = fp;
		mSUTname = SUTname;
		setSimpleComponentGraph();
		setIniNode();
		setFSMname();
		createJava();
		createFSM_Main();
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

	public void setSimpleComponentGraph() throws JAXBException, SAXException,
			WatersUnmarshalException, IOException, VisitorException {
		final DocumentManager manager = getDocumentManager();
		final ModuleProxy moduleproxy = (ModuleProxy) manager.load(wmodFileURL);
		moduleproxy.acceptVisitor(this);

		// Get the Component
		mSimpleComponentProxy = (SimpleComponentProxy) moduleproxy
				.getComponentList().get(index);

		mGraphProxy = mSimpleComponentProxy.getGraph();

	}

	public void setFSMname() {

		mFSMname = mSimpleComponentProxy.getName().toUpperCase() + "_FSM";

	}

	public String getFSMname() {

		return mFSMname;

	}

	public void setIniNode() {

		for (NodeProxy np : mGraphProxy.getNodes()) {
			if (np instanceof SimpleNodeProxy) {
				SimpleNodeProxy snp = (SimpleNodeProxy) np;
				if (snp.isInitial()) {
					mIniNode = snp.getName();
				}
			}

		}

	}

	public String getIniNode() {

		return mIniNode;
	}

	public void createJava() {

		javaString = new StringBuffer();
		String begin;
		String end;
		String event;
		String guard;
		String action;

		LabelBlockProxy lb = null;
		List<Proxy> el = null;

		EdgesHashMap eHM = new EdgesHashMap();
		Vector<String> vEvents = new Vector<String>();
		LinkedList EdgesList;
		EdgeNode EdgeN;

		// get the Variable list from the SimpleComponent that we are
		// translating
		List<VariableProxy> vpl = mSimpleComponentProxy.getVariables();

		for (EdgeProxy e : mGraphProxy.getEdges()) {

			begin = e.getSource().getName();
			end = e.getTarget().getName();

			lb = e.getLabelBlock();
			el = (List<Proxy>) lb.getEventList();

			if (!(e.getGuardActionBlock() == null)
					&& (e.getGuardActionBlock().getGuards().size() > 0)) {
				guard = e.getGuardActionBlock().getGuards().get(0).toString();
				if (e.getGuardActionBlock().getActions().size() > 0) {
					action = e.getGuardActionBlock().getActions().get(0)
							.toString();
				} else {

					action = null;
				}

			} else {

				guard = null;
				action = null;
			}

			// System.out.println(el);
			// System.out.println(begin);
			// System.out.println(end);
			// System.out.println(guard);
			// System.out.println(action);
			// System.out.println("--------------");

			for (Proxy p : el) {

				event = p.toString();

				eHM.add(event, begin, end, guard, action);
				// Some event may not use, so use vEvents to store the event had
				// been used.
				if (vEvents.indexOf(event) < 0) {

					vEvents.add(event);
				}
			}

		}

		// Head

		// javaString.append("package ").append(packagePath).append(";\n\n");
		javaString.append("import net.sourceforge.czt.modeljunit.*;\n\n");
		javaString.append("import java.lang.reflect.Method;\n\n");
		javaString
				.append("public class " + mFSMname + " implements FsmModel\n");
		javaString.append("{\n\n");
		javaString.append(" private String state;\n\n");

		// When the Variable list is not null, create the variable.
		if (!vpl.equals(null)) {
			for (VariableProxy vp : vpl) {
				String type = null;
				if (vp.getType() instanceof BinaryExpressionProxy) {
					type = "int";
				} else if (vp.getType() instanceof SimpleIdentifierProxy) {
					type = "Boolean";
				} else if (vp.getType() instanceof EnumSetExpressionProxy) {
					type = "enum";
				} else {
					// Error,,type not found.

				}

				javaString.append(" private " + type + " " + vp.getName() + "="
						+ vp.getInitialValue() + " ;\n\n");
			}
		}

		if (!(mSUTname==null)) {
			javaString.append(" private Class c ;\n\n");
			javaString.append(" private " + mSUTname + " SUT = new "
					+ mSUTname + "();\n\n");
		}

		javaString.append(" public " + mFSMname + "() \n");
		javaString.append(" {\n");
		javaString.append("  state = \"" + mIniNode + "\";\n");

		// When mSUTname is defined. Load the class.
		if (!(mSUTname==null)) {
			javaString.append("  try{\n");
			javaString.append("   c = Class.forName(\"" + mSUTname
					+ "\");\n");
			javaString.append("  }catch(Exception e){}\n");

		}

		javaString.append(" }\n\n");
		javaString.append(" public String getState() \n");
		javaString.append(" { return state; } \n\n");

		javaString.append(" public void reset(boolean testing) \n");
		javaString.append(" { state = \"" + mIniNode + "\";\n");

		//
		if (!vpl.equals(null)) {
			for (VariableProxy vp : vpl) {
				javaString.append("   " + vp.getName() + "="
						+ vp.getInitialValue() + " ;\n");

			}
		}
		javaString.append(" } \n\n");

		// Body
		for (int i = 0; i < vEvents.size(); i++) {

			event = vEvents.elementAt(i).toString();
			EdgesList = (LinkedList) eHM.get(event);

			// Event use only one time
			if (EdgesList.size() == 1) {

				EdgesList.get(0);

				EdgeN = (EdgeNode) EdgesList.get(0);
				begin = EdgeN.getBegin();
				end = EdgeN.getEnd();
				guard = EdgeN.getGuard();
				action = EdgeN.getAction();
				if (guard == null) {
					guard = "true";
				}

				javaString.append(" public boolean " + event
						+ "Guard() { return (state.equals(\"" + begin
						+ "\") && " + guard + ") ; }\n");
				javaString.append(" public @Action void " + event + "()\n");
				javaString.append(" {\n");
				javaString.append("  System.out.println(\"" + event
						+ ": \" + state + \" --> " + end + "\");\n");
				javaString.append("  state = \"" + end + "\";\n");

				if (action == null) {
					// Edge does not have action,do nothing.
				} else {
					// Edge have actoin
					javaString.append("  System.out.println(\"Action: "
							+ action + " \");\n");
					javaString.append("  " + action + " ;\n");
				}
				if (!(mSUTname==null)) {

					javaString.append("  for (Method m : c.getMethods()){\n");
					javaString.append("   if(m.getName().equals(\"" + event
							+ "\") && "
							+ "m.getParameterTypes().length == 0){\n");
					javaString.append("      SUT." + event + "();\n");
					javaString.append("   }\n");
					javaString.append("  }\n");
				}
				
				javaString.append(" }\n\n");

				// Same event use more than one time.
			} else if (EdgesList.size() > 1) {

				// Build the first one for "guard" return.
				EdgeN = (EdgeNode) EdgesList.get(0);
				begin = EdgeN.getBegin();
				guard = EdgeN.getGuard();
				if (guard == null) {
					guard = "true";
				}

				javaString.append(" public boolean " + event
						+ "Guard() { return (state.equals(\"" + begin
						+ "\") && " + guard + ")");
				for (int j = 1; j < EdgesList.size(); j++) {
					EdgeN = (EdgeNode) EdgesList.get(j);
					begin = EdgeN.getBegin();
					guard = EdgeN.getGuard();
					if (guard == null) {

						guard = "true";
					}
					javaString.append(" || (state.equals(\"" + begin + "\") && "
							+ guard + ")");

				}
				javaString.append("; }\n");

				// Finish build "guard", begin build the action
				javaString.append(" public @Action void " + event + "()\n");
				javaString.append(" {\n");
				for (int k = 0; k < EdgesList.size(); k++) {
					EdgeN = (EdgeNode) EdgesList.get(k);
					begin = EdgeN.getBegin();
					end = EdgeN.getEnd();
					action = EdgeN.getAction();
					guard = EdgeN.getGuard();
					if (guard == null) {
						guard = "true";
					}
					// When edge does not have any action
					if (action == null) {
						if (k == 0) {
							javaString.append("    if( (state.equals(\""
									+ begin + "\") && " + guard + ") ){\n");
						} else if (k > 0) {

							javaString.append("    else if( (state.equals(\""
									+ begin + "\") && " + guard + ") ){\n");
						}
						javaString.append("      System.out.println(\"" + event
								+ ": \" + state + \" --> " + end + "\");\n");
						javaString.append("      state = \"" + end + "\";\n");

						
					} else {// When edge has action
						if (k == 0) {

							javaString.append("    if( (state.equals(\""
									+ begin + "\") && " + guard + ") ){\n");

						} else if (k > 0) {
							javaString.append("    else if( (state.equals(\""
									+ begin + "\") && " + guard + ") ){\n");

						}
						javaString.append("      System.out.println(\"" + event
								+ ": \" + state + \" --> " + end + "\");\n");
						javaString.append("      state = \"" + end + "\";\n");
						javaString.append("      System.out.println(\"Action: "
								+ action + " \");\n");
						javaString.append("      " + action + " ;\n");
					//	
					}
					if (!(mSUTname==null)) {

						javaString.append("      for (Method m : c.getMethods()){\n");
						javaString.append("          if (m.getName().equals(\"" + event
								+ "\") && "
								+ "m.getParameterTypes().length == 0){\n");
						javaString.append("             SUT." + event + "();\n");
						javaString.append("          }\n");
						javaString.append("      }\n");
						
					}
					javaString.append("    }\n");
				}
				javaString.append(" }\n\n");
			}

		}
		javaString.append("}\n");
		System.out.print(javaString);
	}

	public void createFSM_Main() {

		javaString_Main = new StringBuffer();
		javaString_Main.append("import net.sourceforge.czt.modeljunit.*;\n\n");

		javaString_Main.append("public class " + mFSMname + "_Main {\n");
		javaString_Main.append(" public static void main(String[] args) {\n\n");
		javaString_Main.append("  String fsmName = \"" + mFSMname + "\";\n");

		javaString_Main.append("  try {\n");
		// javaString_Main.append(" System.out.println(\"Starting Main\");\n");
		javaString_Main.append("  	Class fsmClass = Class.forName(fsmName);\n");
		javaString_Main
				.append("  	FsmModel fsm = (FsmModel) fsmClass.newInstance();\n");
		javaString_Main
				.append("  	ModelTestCase model = new ModelTestCase(fsm);\n");
		javaString_Main.append("  	model.randomWalk(40);\n");
		javaString_Main.append("  } catch (ClassNotFoundException e) {\n");
		javaString_Main
				.append("  	System.err.println(\"Cannot load class \" + fsmName);\n");
		javaString_Main.append("  	e.printStackTrace();\n");
		javaString_Main.append("  } catch (InstantiationException e) {\n");
		javaString_Main
				.append("  	System.err.println(\"Cannot instantiate FSM class\");\n");
		javaString_Main.append("  	e.printStackTrace();\n");
		javaString_Main.append("  } catch (IllegalAccessException e) {\n");
		javaString_Main.append("  			e.printStackTrace();\n");
		javaString_Main.append("  }\n");
		javaString_Main.append(" }\n");

		javaString_Main.append("}\n");

		// System.out.println(javaString_Main);
	}

	public void writeFile() {

		FileWriter os1 = null;
		mFullJavafilename = filePath + mFSMname + ".java";
		try {
			os1 = new FileWriter(mFullJavafilename);
			// os.write(javaString.toString() +
			// System.getProperty("line.separator"));
			os1.write(javaString.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				os1.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		FileWriter os2 = null;
		mFullJavafilename_main = filePath + mFSMname + "_Main.java";
		try {
			os2 = new FileWriter(mFullJavafilename_main);
			// os.write(javaString.toString() +
			// System.getProperty("line.separator"));
			os2.write(javaString_Main.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				os2.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public StringBuffer getjavaString() {

		return javaString;
	}

	public void compileFile() {
		try {

			Process process1 = Runtime.getRuntime().exec(
					"javac " + mFullJavafilename);

			// process1.waitFor();

			InputStream is1 = process1.getInputStream();

			InputStreamReader isr1 = new InputStreamReader(is1);

			BufferedReader br1 = new BufferedReader(isr1);

			String line1 = "";

			while ((line1 = br1.readLine()) != null) {

				System.out.println(line1);

			}

			br1.close();

			isr1.close();

			is1.close();

			System.out.println(process1.exitValue());
			if (process1.exitValue() == 0) {

				Process process2 = Runtime.getRuntime().exec(
						"javac " + mFullJavafilename_main);

				// process2.waitFor();

				InputStream is2 = process2.getInputStream();

				InputStreamReader isr2 = new InputStreamReader(is2);

				BufferedReader br2 = new BufferedReader(isr2);

				String line2 = "";

				while ((line2 = br2.readLine()) != null) {

					System.out.println(line2);

				}

				br2.close();

				isr2.close();

				is2.close();

				System.out.println(process2.exitValue());

				if (process2.exitValue() == 0) {

					Process process3 = Runtime.getRuntime().exec(
							"java -classpath " + filePath
									+ ";e:\\modeljunit.jar " + mFSMname
									+ "_Main");

					// process3.waitFor();

					InputStream is3 = process3.getInputStream();

					InputStreamReader isr3 = new InputStreamReader(is3);

					BufferedReader br3 = new BufferedReader(isr3);

					javaString_Main_Complied = new StringBuffer();

					String line3 = "";

					while ((line3 = br3.readLine()) != null) {

						// System.out.println(line3 + "\n");
						javaString_Main_Complied.append(line3 + "\n\n");
					}

					br3.close();

					isr3.close();

					is3.close();
					System.out.println(process3.exitValue());
					System.out.println(javaString_Main_Complied);
				}

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public StringBuffer getjavaStringmaincomplied() {

		return javaString_Main_Complied;
	}

	public static void main(String args[]) throws Exception {
		// if (args.length <= 0) {
		// System.err.println("Args: module.wmod");
		// } else {
		// System.out.println("url=" + new File(args[0]).toURL());
		String s = "e:/1.wmod";
		String s1 = "e:/";
//		ToModelJUnit translator = new ToModelJUnit(s, 13, s1, "CALC");
		ToModelJUnit translator = new ToModelJUnit(s, 6, s1, null);
		translator.writeFile();
		// translator.compileFile();

	}

}
