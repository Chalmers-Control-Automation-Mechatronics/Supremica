//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   DocumentManager
//###########################################################################
//# $Id: DocumentManager.java,v 1.8 2006-09-15 09:26:13 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.filechooser.FileFilter;

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
 *   final DocumentManager manager = new {@link #DocumentManager() DocumentManager()};
 *   final {@link net.sourceforge.waters.model.module.ModuleProxyFactory ModuleProxyFactory} factory = {@link net.sourceforge.waters.plain.module.ModuleElementFactory}.{@link net.sourceforge.waters.plain.module.ModuleElementFactory#getInstance() getInstance}();
 *   final {@link net.sourceforge.waters.model.expr.OperatorTable OperatorTable} optable = {@link net.sourceforge.waters.model.compiler.CompilerOperatorTable CompilerOperatorTable}.{@link net.sourceforge.waters.model.compiler.CompilerOperatorTable#getInstance() getInstance}();
 *   final {@link JAXBMarshaller}&lt;{@link net.sourceforge.waters.model.module.ModuleProxy}&gt; marshaller = new {@link JAXBModuleMarshaller}(factory, optable);
 *   manager.{@link #registerMarshaller(ProxyMarshaller) registerMarshaller}(marshaller);
 *   manager.{@link #registerUnmarshaller(ProxyUnmarshaller) registerUnmarshaller}(marshaller);
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
   * Creates a new document manager.
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
      new HashMap<Class<? extends DocumentProxy>,
                  ProxyMarshaller<? extends DocumentProxy>>(4);
    mClassUnmarshallerMap =
      new HashMap<Class<? extends DocumentProxy>,
                  List<ProxyUnmarshaller<? extends DocumentProxy>>>(4);
    mExtensionUnmarshallerMap =
      new HashMap<String,ProxyUnmarshaller<? extends DocumentProxy>>(4);
    mFileFilters = new LinkedList<FileFilter>();
    mDocumentCache = new HashMap<URI,DocumentProxy>(32);
  }


  //#########################################################################
  //# Accessing Documents
  /**
   * Loads a document relative to a URI.
   * This methods loads a document with a given name and location,
   * automatically computing the file extension from the expected class.
   * @param  uri          The URI to be used to resolve the name.
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
   *                      {@link ProxyMarshaller} for the given class was
   *                      found.
   */
  public <DD extends DocumentProxy> DD load
    (final URI uri, final String name, final Class<DD> clazz)
    throws WatersUnmarshalException, IOException
  {
    final Collection<ProxyUnmarshaller<? extends DD>> unmarshallers =
      findProxyUnmarshallers(clazz);
    for (final ProxyUnmarshaller<? extends DD> unmarshaller : unmarshallers) {
      final String extname = name + unmarshaller.getDefaultExtension();
      final URI resolved = resolve(uri, extname);
      final DocumentProxy cached = mDocumentCache.get(resolved);
      if (cached != null) {
        return clazz.cast(cached);
      }
      final DD loaded = unmarshaller.unmarshal(resolved);
      mDocumentCache.put(resolved, loaded);
      return loaded;
    }
    throw new FileNotFoundException
      ("Can't find file for " + clazz.getName() + " named '" + name +
       "' relative to '" + uri + "'!");
  }

  /**
   * Loads a document from a URI.
   * This methods loads a document from a given file name, guessing the
   * expected class from the file name extension.
   * @param  uri          A URI specifiying the location of the document
   *                      to be retrieved.
   * @return The loaded document. This may be just a cached copy,
   *         or it may actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException to indicate that parsing the input file
   *                      has failed for some reason.
   * @throws IOException  to indicate that the input file could not be
   *                      opened or read.
   */
  public DocumentProxy load(final URI uri)
    throws WatersUnmarshalException, IOException
  {
    final DocumentProxy cached = mDocumentCache.get(uri);
    if (cached != null) {
      return cached;
    }
    final String path = uri.getRawSchemeSpecificPart();
    final int dotpos = path.lastIndexOf('.');
    final String extension = dotpos >= 0 ? path.substring(dotpos) : "";
    final String lowerext = extension.toLowerCase();
    final ProxyUnmarshaller<? extends DocumentProxy> unmarshaller =
      mExtensionUnmarshallerMap.get(lowerext);
    if (unmarshaller == null) {
      throw new BadFileTypeException(uri);
    }
    final DocumentProxy loaded = unmarshaller.unmarshal(uri);
    mDocumentCache.put(uri, loaded);
    return loaded;
  }

  /**
   * Loads a document from a file.
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
   *                      {@link ProxyMarshaller} for the given class was
   *                      found.
   */
  public <DD extends DocumentProxy> DD load
    (final File path, final String name, final Class<DD> clazz)
    throws WatersUnmarshalException, IOException
  {
    final Collection<ProxyUnmarshaller<? extends DD>> unmarshallers =
      findProxyUnmarshallers(clazz);
    for (final ProxyUnmarshaller<? extends DD> unmarshaller : unmarshallers) {
      final String extname = name + unmarshaller.getDefaultExtension();
      final File filename = new File(path, extname);
      final URI uri = filename.toURI();
      final DocumentProxy cached = mDocumentCache.get(uri);
      if (cached != null) {
        return clazz.cast(cached);
      }
      if (filename.canRead()) {
        final DD loaded = unmarshaller.unmarshal(uri);
        mDocumentCache.put(uri, loaded);
        return loaded;
      }
    }
    throw new FileNotFoundException
      ("Can't find file for " + clazz.getName() + " named '" + name +
       "' in directory " + path + "!");
  }

  /**
   * Loads a document from a URL.
   * This methods loads a document from a given file name, guessing the
   * expected class from the file name extension.
   * @param  url          A URL specifiying the location of the document
   *                      to be retrieved.
   * @return The loaded document. This may be just a cached copy,
   *         or it may actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException to indicate that parsing the input file
   *                      has failed for some reason.
   * @throws IOException  to indicate that the input file could not be
   *                      opened or read.
   */
  public DocumentProxy load(final URL url)
    throws WatersUnmarshalException, IOException
  {
    try {
      final URI uri = url.toURI();
      return load(uri);
    } catch (final URISyntaxException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  /**
   * Loads a document from a file.
   * This methods loads a document from a given file name, guessing the
   * expected class from the file name extension.
   * @param  filename     The absolute path of the file to be loaded.
   * @return The loaded document. This may be just a cached copy,
   *         or it may actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException to indicate that parsing the input file
   *                      has failed for some reason.
   * @throws IOException  to indicate that the input file could not be
   *                      opened or read.
   */
  public DocumentProxy load(final File filename)
    throws WatersUnmarshalException, IOException
  {
    final URI uri = filename.toURI();
    return load(uri);
  }

  /**
   * Saves a document to a file.
   * This methods writes a document to a given file name, using the
   * marshaller appropriate to the document's class. If the file name
   * differs from the document's current location, the document
   * manager cache will be updated to reflect the name change.
   * @param  doc          The document to be written.
   * @param  filename     The absolute path of the file to be written.
   * @throws WatersMarshalException to indicate a failure while writing the
   *                      data structures.
   * @throws IOException to indicate that the output file could not be
   *                      opened or written.
   * @throws IllegalArgumentException to indicate that no registered
   *                      {@link ProxyMarshaller} for the given class was
   *                      found.
   */
  public void saveAs(final DocumentProxy doc, final File filename)
    throws WatersMarshalException, IOException
  {
    final Class<DocumentProxy> clazz = Casting.toClass(doc.getClass());
    final ProxyMarshaller<DocumentProxy> marshaller =
      findProxyMarshaller(clazz);
    final URI newuri = filename.toURI();
    final URI olduri = doc.getLocation();
    marshaller.marshal(doc, filename);
    if (!newuri.equals(olduri)) {
      mDocumentCache.remove(olduri);
      doc.setLocation(newuri);
      mDocumentCache.put(newuri, doc);
    }
  }


  /**
   * Adds a new document to this document manager.
   * This method adds the given document to the cache maintained by the
   * document manager, under the location given by the document.
   * It does not make any attempt to save the document to disk.
   * If the document manager has already stored a document at the
   * given location, the new document will replace the existing one.
   * @param  doc          The document to be added. It should provide a
   *                      valid file location where the document can be
   *                      looked up and saved.
   */
  public void newDocument(final DocumentProxy doc)
  {
    final URI uri = doc.getLocation();
    mDocumentCache.put(uri, doc);
  }


  //#########################################################################
  //# Registering Marshallers
  /**
   * Registers a proxy marshaller.
   * @param  marshaller   The proxy marshaller to be registered.
   */
  public <DD extends DocumentProxy>
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
  public <DD extends DocumentProxy>
    void registerUnmarshaller(final ProxyUnmarshaller<DD> unmarshaller)
  {
    final Class<DD> clazz = unmarshaller.getDocumentClass();
    List<ProxyUnmarshaller<? extends DocumentProxy>> list =
      mClassUnmarshallerMap.get(clazz);
    if (list == null) {
      list = new LinkedList<ProxyUnmarshaller<? extends DocumentProxy>>();
      mClassUnmarshallerMap.put(clazz, list);
    }
    list.add(unmarshaller);
    final Collection<String> extensions =
      unmarshaller.getSupportedExtensions();
    for (String extension : extensions) {
      final String lowerext = extension.toLowerCase();
      if (mExtensionUnmarshallerMap.containsKey(lowerext)) {
        throw new IllegalArgumentException
          ("Registering a second unmarshaller for extension '" +
           extension + "'!");
      }
      mExtensionUnmarshallerMap.put(lowerext, unmarshaller);
    }
    mFileFilters.addAll(unmarshaller.getSupportedFileFilters());
    unmarshaller.setDocumentManager(this);
  }


  //#########################################################################
  //# Finding Marshallers
  /**
   * Gets the marshaller used to marshal objects of a given class.
   * If there is no marshaller for excatly the given class, this method
   * tries all superclasses and superinterfaces and returns a marshaller
   * for the first supported class or interface found.
   * @param  clazz        The class of objects to be looked up.
   * @return The {@link ProxyMarshaller} used by this document manager
   *         to handle objects of the given class.
   * @throws IllegalArgumentException to indicate that no registered
   *                      {@link ProxyMarshaller} for the given class was
   *                      found.
   */
  public <DD extends DocumentProxy>
    ProxyMarshaller<DD> findProxyMarshaller(final Class<DD> clazz)
  {
    final ProxyMarshaller marshaller = getProxyMarshaller(clazz);
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
  public <DD extends DocumentProxy>
    Collection<ProxyUnmarshaller<? extends DD>>
    findProxyUnmarshallers(final Class<DD> clazz)
  {
    final Collection<ProxyUnmarshaller<? extends DocumentProxy>>
      unmarshallers =
      mClassUnmarshallerMap.get(clazz);
    if (unmarshallers != null) {
      return Casting.toCollection(unmarshallers);
    } else {
      throw new IllegalArgumentException
        ("Unsupported document class " + clazz.getName() + "!");
    }
  }

  public Collection<FileFilter> getSupportedFileFilters()
  {
    return Collections.unmodifiableList(mFileFilters);
  }


  //#########################################################################
  //# Auxiliary Methods
  private ProxyMarshaller getProxyMarshaller(final Class clazz)
  {
    if (DocumentProxy.class.isAssignableFrom(clazz)) {
      ProxyMarshaller marshaller = mClassMarshallerMap.get(clazz);
      if (marshaller != null) {
        return marshaller;
      }
      final Class superclass = clazz.getSuperclass();
      if (superclass != null) {
        marshaller = getProxyMarshaller(superclass);
        if (marshaller != null) {
          return marshaller;
        }
      }
      final Class[] interfaces = clazz.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        marshaller = getProxyMarshaller(interfaces[i]);
        if (marshaller != null) {
          return marshaller;
        }
      }
    }
    return null;
  }

  private static URI resolve(final URI base, final String tailname)
    throws WatersUnmarshalException
  {
    if (base != null) {
      return base.resolve(tailname);
    } else {
      try {
        final URI result = new URI(tailname);
        if (result.isAbsolute()) {
          return result;
        } else {
          throw new WatersUnmarshalException
            ("Trying to load from relative location '" + tailname +
             "' without known path!");
        }
      } catch (final URISyntaxException exception) {
        throw new WatersUnmarshalException(exception);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final Map<Class<? extends DocumentProxy>,
                    ProxyMarshaller<? extends DocumentProxy>>
    mClassMarshallerMap;
  private final Map<Class<? extends DocumentProxy>,
                    List<ProxyUnmarshaller<? extends DocumentProxy>>>
    mClassUnmarshallerMap;
  private final Map<String,ProxyUnmarshaller<? extends DocumentProxy>>
    mExtensionUnmarshallerMap;
  private final List<FileFilter> mFileFilters;
  private final Map<URI,DocumentProxy> mDocumentCache;

}
