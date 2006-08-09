package net.sourceforge.waters.mbt.translator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import net.sourceforge.waters.model.base.DocumentProxy;
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
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

public class ToModelJUnit extends AbstractProductDESProxyVisitor {

	protected DocumentManager mDocumentManager;
	protected JAXBModuleMarshaller mMarshaller;
	
	/** The output where the Java ModelJUnit class is written.
	 * 
	 */
    protected PrintWriter mWriter;
    
	public ToModelJUnit() {
		// TODO Auto-generated constructor stub
	}
	
	//#########################################################################
	//# Creating a Document Manager
	protected DocumentManager getDocumentManager()
	throws JAXBException, SAXException
	{
		if (mDocumentManager == null) {
		    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
		    final OperatorTable optable = CompilerOperatorTable.getInstance();
		    mMarshaller = new JAXBModuleMarshaller(factory, optable);
			mDocumentManager = new DocumentManager();
			mDocumentManager.registerMarshaller(mMarshaller);
			mDocumentManager.registerUnmarshaller(mMarshaller);
		}
		return mDocumentManager;
	} 

	public void translate(URL url)
	throws JAXBException, SAXException, WatersUnmarshalException, IOException
	{
		    System.out.println("url="+url);

		    mWriter = new PrintWriter(System.out);
		    final DocumentManager manager = getDocumentManager();
		    System.out.println("manager="+manager);
		    final DocumentProxy proxy1 = manager.load(url);
		    System.out.println("loaded "+proxy1);
		    // proxy1.acceptVisitor(this);
	}
	
	@Override
	public Object visitDocumentProxy(final DocumentProxy proxy)
	throws VisitorException
	{
		this.mWriter.println("visiting " + proxy);
		//proxy.get????().acceptVisitor(this);
		return proxy;
	}
	
	@Override
	public Object visitAutomatonProxy(final AutomatonProxy proxy)
    throws VisitorException
    {
		Set<StateProxy> states = proxy.getStates();
		for (StateProxy st : states)
			st.acceptVisitor(this);
		return proxy;
    }

	public static void main(String args[])
	throws Exception
	{
		if (args.length <= 0)
		{
			System.err.println("Args:  module.wmod");
		}
		else
		{
			ToModelJUnit translator = new ToModelJUnit();
			translator.translate(new File(args[0]).toURL());
		}
	}
	
}
