package net.sourceforge.waters.mbt.translator;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.marshaller.DocumentManager;

import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class ToModelJUnitTest extends TestCase {
	
	
	 public ToModelJUnitTest(String name) {
	        super(name);
	    }

	    public static void main(String args[]) {
	        junit.textui.TestRunner.run(ToModelJUnitTest.class);
	    }

	public void testVisitModuleProxy() {
//		fail("Not yet implemented"); // TODO
		
	}

	public void testGetDocumentManager() throws JAXBException, SAXException {
//		fail("Not yet implemented"); // TODO
		
		ToModelJUnit tmj = new ToModelJUnit();
        assert( tmj!=null );
        assertNotNull(tmj.getDocumentManager());
        assertTrue(tmj.getDocumentManager() instanceof DocumentManager) ;
	}

	public void testTranslate() {
//		fail("Not yet implemented"); // TODO
	}

}
