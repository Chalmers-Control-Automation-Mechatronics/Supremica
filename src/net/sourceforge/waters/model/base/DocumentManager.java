//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   DocumentManager
//###########################################################################
//# $Id: DocumentManager.java,v 1.2 2005-05-08 00:27:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * <P>A helper class to access and store the contents of documents obtained
 * from XML files.</P>
 *
 * <P>A document manager is a cache that can parse the contents of multiple
 * documents from XML files, and provide easy access to them by their file
 * names. The object structures loaded are called <I>documents</I> and all
 * implement the {@link DocumentProxy} interface. Once loaded, they are
 * stored in the cache, so they can be quickly retrieved when needed a
 * second time.</P>
 *
 * <P>After creation of a document manager, it must be provided with the
 * appropriate proxy marshaller for each class of documents to be
 * handled. For example:</P>
 * <PRE>
 *   final DocumentManager manager = new {@link #DocumentManager()};
 *   final {@link ProxyMarshaller} m1 = new {@link net.sourceforge.waters.model.des.ProductDESMarshaller ProductDESMarshaller}();
 *   final {@link ProxyMarshaller} m2 = new {@link net.sourceforge.waters.model.module.ModuleMarshaller ModuleMarshaller}();
 *   manager.{@link #register(ProxyMarshaller) register}(m1);
 *   manager.{@link #register(ProxyMarshaller) register}(m2);
 *   final {@link File} file = new File("myfile.wmod");
 *   final {@link net.sourceforge.waters.model.module.ModuleProxy} module = ({@link net.sourceforge.waters.model.module.ModuleProxy}) manager.{@link #load(File) load}(file);
 * </PRE>
 *
 * @author Robi Malik
 */

public class DocumentManager {

  //#########################################################################
  //# Constructors
  /**
   * Create a new document manager.
   * Initially, the document manager will not support marshalling or
   * unmarshalling of any file types. To be useful, some {@link
   * ProxyMarshaller} objects need to be registered using the {@link
   * #register(ProxyMarshaller) register()} method.
   */
  public DocumentManager()
  {
    mClassMap = new HashMap(4);
    mExtensionMap = new HashMap(4);
    mDocumentCache = new HashMap(32);
  }


  //#########################################################################
  //# Accessing Documents
  /**
   * Load a document.
   * This methods loads a document with a given name and location,
   * automatically computing the file extension from the expected class.
   * @param  path       The directory containing the file to be loaded.
   * @param  name       The name of the file without extension.
   *                    The extension is determined automatically using
   *                    a {@link ProxyMarshaller}.
   * @param  clazz      The desired class of the document to be loaded.
   *                    This is used to identify an appropriate
   *                    {@link ProxyMarshaller}.
   * @return The loaded document. This may just a cached copy,
   *         or it may actually be retrieved by reading an external file.
   * @throws JAXBException to indicate that the process of reading or
   *                    parsing an XML file has failed.
   * @throws ModelException to indicate that a parsed XML structure
   *                    could not be converted to Waters objects due to
   *                    serious semantic inconsistencies.
   * @throws IllegalArgumentException to indicate that no registered
   *                    {@link ProxyMarshaller} for the given class was found.
   */
  public DocumentProxy load
    (final File path, final String name, final Class clazz)
    throws Exception
  {
    final ProxyMarshaller marshaller = findProxyMarshaller(clazz);
    final String extname = name + marshaller.getDefaultExtension();
    final File filename = new File(path, extname);
    final Object cached = mDocumentCache.get(filename);
    if (cached != null) {
      return (DocumentProxy) cached;
    }
    final DocumentProxy loaded = marshaller.unmarshal(filename);
    mDocumentCache.put(filename, loaded);
    return loaded;    
  }

  /**
   * Load a document.
   * This methods loads a document from a given file name, guessing the
   * expected class from the file name extension.
   * @param  filename   The complete path name identifying the file to
   *                    be loaded.
   * @return The loaded document. This may just a cached copy,
   *         or it may actually be retrieved by reading an external file.
   * @throws JAXBException to indicate that the process of reading or
   *                  parsing an XML file has failed.
   * @throws ModelException to indicate that a parsed XML structure
   *                  could not be converted to Waters objects due to
   *                  serious semantic inconsistencies.
   */
   public DocumentProxy load(final File filename)
    throws Exception
  {
    final Object cached = mDocumentCache.get(filename);
    if (cached != null) {
      return (DocumentProxy) cached;
    }
    final String stringname = filename.toString();
    final int dotpos = stringname.lastIndexOf(".");
    final String extension = dotpos >= 0 ? stringname.substring(dotpos) : "";
    final ProxyMarshaller marshaller =
      (ProxyMarshaller) mExtensionMap.get(extension);
    if (marshaller == null) {
      throw new BadFileTypeException(filename);
    }
    final DocumentProxy loaded = marshaller.unmarshal(filename);
    mDocumentCache.put(filename, loaded);
    return loaded;
  }


  //#########################################################################
  //# Registering Marshallers
  /**
   * Registers a proxy marshaller.
   * This method adds the given marshaller object with this document
   * manager, so future calls to the {@link #load(File) load()} method can
   * use it to load documents of the type supported by it.
   * @param  marshaller The proxy marshaller to be registered.
   */
  public void register(final ProxyMarshaller marshaller)
  {
    final Collection classes = marshaller.getMarshalledClasses();
    final Iterator citer = classes.iterator();
    while (citer.hasNext()) {
      final Class clazz = (Class) citer.next();
      mClassMap.put(clazz, marshaller);
    }
    final Collection extensions = marshaller.getSupportedExtensions();
    final Iterator eiter = extensions.iterator();
    while (eiter.hasNext()) {
      final String extension = (String) eiter.next();
      mExtensionMap.put(extension, marshaller);
    }
  }


  //#########################################################################
  //# Finding Marshallers
  /**
   * Gets the marshaller used to marshal objects of a given class.
   * @param  clazz      The class of objects to be looked up.
   * @return The {@link ProxyMarshaller} used by this document manager
   *         to handle objects of the given class.
   * @throws IllegalArgumentException to indicate that no registered
   *                    {@link ProxyMarshaller} for the given class was found.
   */
  public ProxyMarshaller findProxyMarshaller(final Class clazz)
  {
    final ProxyMarshaller marshaller = (ProxyMarshaller) mClassMap.get(clazz);
    if (marshaller != null) {
      return marshaller;
    } else {
      throw new IllegalArgumentException
        ("Unsupported document class " + clazz.getName() + "!");
    }
  }


  //#########################################################################
  //# Data Members
  private final Map mClassMap;
  private final Map mExtensionMap;
  private final Map mDocumentCache;

}
