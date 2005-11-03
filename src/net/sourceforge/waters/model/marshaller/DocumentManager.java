//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   DocumentManager
//###########################################################################
//# $Id: DocumentManager.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.unchecked.Casting;


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
 *   final {@link net.sourceforge.waters.model.module.ModuleProxyFactory ModuleProxyFactory} factory = new {@link net.sourceforge.waters.plain.module.ModuleElementFactory ModuleElementFactory}();
 *   final {@link JAXBMarshaller}&lt;{@link net.sourceforge.waters.model.module.ModuleProxy}&gt; marshaller = new {@link JAXBModuleMarshaller}();
 *   manager.{@link #registerMarshaller(ProxyMarshaller) registerMarshaller}(marshaller);
 *   manager.{@link #registerUnmarshaller(ProxyUnmarshaller) registerUnmarshaller}(marshaller);
 *   final {@link File} file = new File("myfile.wmod");
 *   final {@link net.sourceforge.waters.model.module.ModuleProxy} module = ({@link net.sourceforge.waters.model.module.ModuleProxy}) manager.{@link #load(File) load}(file);
 * </PRE>
 *
 * @author Robi Malik
 */

public class DocumentManager<D extends DocumentProxy> {

  //#########################################################################
  //# Constructors
  /**
   * Create a new document manager.
   * Initially, the document manager will not support marshalling or
   * unmarshalling of any file types. To be useful, some {@link
   * ProxyMarshaller} or {@link ProxyUnmarshaller} objects need to be
   * registered using the {@link #registerMarshaller(ProxyMarshaller)
   * registerMarshaller()} or {@link
   * #registerUnmarshaller(ProxyUnmarshaller) registerMarshaller()} methods.
   */
  public DocumentManager()
  {
    mClassMarshallerMap =
      new HashMap<Class<? extends D>,ProxyMarshaller<? extends D>>(4);
    mClassUnmarshallerMap =
      new HashMap<Class<? extends D>,List<ProxyUnmarshaller<? extends D>>>(4);
    mExtensionUnmarshallerMap =
      new HashMap<String,ProxyUnmarshaller<? extends D>>(4);
    mDocumentCache = new HashMap<File,D>(32);
  }


  //#########################################################################
  //# Accessing Documents
  /**
   * Loads a document.
   * This methods loads a document with a given name and location,
   * automatically computing the file extension from the expected class.
   * @param  path         The directory containing the file to be loaded.
   * @param  name         The name of the file without extension.
   *                      The extension is determined automatically using
   *                      a {@link ProxyMarshaller}.
   * @param  clazz        The desired class of the document to be loaded.
   *                      This is used to identify an appropriate
   *                      {@link ProxyMarshaller}.
   * @return The loaded document. This may just a cached copy,
   *         or it may actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException to indicate that parsing the input file
   *                      has failed for some reason.
   * @throws IOException  to indicate that the input file could not be
   *                      opened or read.
   * @throws IllegalArgumentException to indicate that no registered
   *                      {@link ProxyMarshaller} for the given class was found.
   */
  public <DD extends D> DD load
    (final File path, final String name, final Class<DD> clazz)
    throws WatersUnmarshalException, IOException
  {
    final Collection<ProxyUnmarshaller<? extends DD>> unmarshallers =
      findProxyUnmarshallers(clazz);
    for (final ProxyUnmarshaller<? extends DD> unmarshaller : unmarshallers) {
      final String extname = name + unmarshaller.getDefaultExtension();
      final File filename = new File(path, extname);
      final D cached = mDocumentCache.get(filename);
      if (cached != null) {
        return clazz.cast(cached);
      }
      if (filename.canRead()) {
        final DD loaded = unmarshaller.unmarshal(filename);
        mDocumentCache.put(filename, loaded);
        return loaded;
      }
    }
    throw new FileNotFoundException
      ("Can't find file for " + clazz.getName() + " named '" + name +
       "' in directory " + path + "!");
  }

  /**
   * Loads a document.
   * This methods loads a document from a given file name, guessing the
   * expected class from the file name extension.
   * @param  filename     The complete path name identifying the file to
   *                      be loaded.
   * @return The loaded document. This may just a cached copy,
   *         or it may actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException to indicate that parsing the input file
   *                      has failed for some reason.
   * @throws IOException  to indicate that the input file could not be
   *                      opened or read.
   */
   public D load(final File filename)
    throws WatersUnmarshalException, IOException
  {
    final D cached = mDocumentCache.get(filename);
    if (cached != null) {
      return cached;
    }
    final String stringname = filename.toString();
    final int dotpos = stringname.lastIndexOf(".");
    final String extension = dotpos >= 0 ? stringname.substring(dotpos) : "";
    final ProxyUnmarshaller<? extends D> unmarshaller =
      mExtensionUnmarshallerMap.get(extension);
    if (unmarshaller == null) {
      throw new BadFileTypeException(filename);
    }
    final D loaded = unmarshaller.unmarshal(filename);
    mDocumentCache.put(filename, loaded);
    return loaded;
  }


  //#########################################################################
  //# Registering Marshallers
  /**
   * Registers a proxy marshaller.
   * @param  marshaller   The proxy marshaller to be registered.
   */
  public <DD extends D>
    void registerMarshaller(final ProxyMarshaller<DD> marshaller)
  {
    final Class<DD> clazz = marshaller.getDocumentClass();
    if (mClassMarshallerMap.containsKey(clazz)) {
      throw new IllegalArgumentException
        ("Registering a second marshaller for class '" +
         clazz.getName() + "'!");
    }
    mClassMarshallerMap.put(clazz, marshaller);
  }

  /**
   * Registers a proxy unmarshaller.
   * This method registers the given unmarshaller object with this document
   * manager, so future calls to the {@link #load(File) load()} method can
   * use it to load documents of the type supported by it.
   * @param  unmarshaller The proxy unmarshaller to be registered.
   */
  public <DD extends D>
    void registerUnmarshaller(final ProxyUnmarshaller<DD> unmarshaller)
  {
    final Class<DD> clazz = unmarshaller.getDocumentClass();
    List<ProxyUnmarshaller<? extends D>> list =
      mClassUnmarshallerMap.get(clazz);
    if (list == null) {
      list = new LinkedList<ProxyUnmarshaller<? extends D>>();
      mClassUnmarshallerMap.put(clazz, list);
    }
    list.add(unmarshaller);
    final Collection<String> extensions =
      unmarshaller.getSupportedExtensions();
    for (String extension : extensions) {
      if (mExtensionUnmarshallerMap.containsKey(extension)) {
        throw new IllegalArgumentException
          ("Registering a second unmarshaller for extension '" +
           extension + "'!");
      }
      mExtensionUnmarshallerMap.put(extension, unmarshaller);
    }
  }


  //#########################################################################
  //# Finding Marshallers
  /**
   * Gets the marshaller used to marshal objects of a given class.
   * @param  clazz        The class of objects to be looked up.
   * @return The {@link ProxyMarshaller} used by this document manager
   *         to handle objects of the given class.
   * @throws IllegalArgumentException to indicate that no registered
   *                      {@link ProxyMarshaller} for the given class was
   *                      found.
   */
  public <DD extends D>
    ProxyMarshaller<DD> findProxyMarshaller(final Class<DD> clazz)
  {
    final ProxyMarshaller<? extends D> marshaller =
      mClassMarshallerMap.get(clazz);
    if (marshaller != null) {
      final Class<ProxyMarshaller<DD>> marshallerclazz =
        Casting.toClass(ProxyMarshaller.class);
      return marshallerclazz.cast(marshaller);
    } else {
      throw new IllegalArgumentException
        ("Unsupported document class " + clazz.getName() + "!");
    }
  }

  /**
   * Gets the unmarshaller used to unmarshal objects of a given class.
   * @param  clazz        The class of objects to be looked up.
   * @return A list of {@link ProxyUnmarshaller} objects used by this
   *         document manager to handle objects of the given class.
   * @throws IllegalArgumentException to indicate that no registered
   *                      {@link ProxyUnmarshaller} for the given class was
   *                      found.
   */
  public <DD extends D>
    Collection<ProxyUnmarshaller<? extends DD>>
    findProxyUnmarshallers(final Class<DD> clazz)
  {
    final Collection<ProxyUnmarshaller<? extends D>> unmarshallers =
      mClassUnmarshallerMap.get(clazz);
    if (unmarshallers != null) {
      return Casting.toCollection(unmarshallers);
    } else {
      throw new IllegalArgumentException
        ("Unsupported document class " + clazz.getName() + "!");
    }
  }


  //#########################################################################
  //# Data Members
  private final Map<Class<? extends D>,ProxyMarshaller<? extends D>>
    mClassMarshallerMap;
  private final Map<Class<? extends D>,List<ProxyUnmarshaller<? extends D>>>
    mClassUnmarshallerMap;
  private final Map<String,ProxyUnmarshaller<? extends D>>
    mExtensionUnmarshallerMap;
  private final Map<File,D> mDocumentCache;

}
