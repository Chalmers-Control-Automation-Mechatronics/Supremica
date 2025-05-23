//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * <P>
 * A helper class to access and store the contents of documents obtained from
 * XML files.
 * </P>
 *
 * <P>
 * A document manager is a cache that can parse the contents of multiple
 * documents from XML files, and provide easy access to them by their file
 * names. The object structures loaded are called <I>documents</I> and all
 * implement the {@link DocumentProxy} interface. Once loaded, they are stored
 * in the cache, so they can be quickly retrieved when needed a second time.
 * </P>
 *
 * <P>
 * The caching mechanism provides some protection against modification of
 * external files through third parties. Whenever a document is found in the
 * cache, it is checked whether the external file has been changed after the
 * document was loaded, and if so, it is reloaded from the modified file.
 * </P>
 *
 * <P>
 * After creation of a document manager, it must be provided with the
 * appropriate proxy marshaller for each class of documents to be handled. For
 * example:
 * </P>
 *
 * <PRE>
 *   final DocumentManager manager = new {@link #DocumentManager() DocumentManager()};
 *   final {@link net.sourceforge.waters.model.module.ModuleProxyFactory ModuleProxyFactory} factory = {@link net.sourceforge.waters.plain.module.ModuleElementFactory}.{@link net.sourceforge.waters.plain.module.ModuleElementFactory#getInstance() getInstance}();
 *   final {@link net.sourceforge.waters.model.expr.OperatorTable OperatorTable} optable = {@link net.sourceforge.waters.model.compiler.CompilerOperatorTable CompilerOperatorTable}.{@link net.sourceforge.waters.model.compiler.CompilerOperatorTable#getInstance() getInstance}();
 *   final {@link SAXMarshaller}&lt;{@link net.sourceforge.waters.model.module.ModuleProxy}&gt; marshaller = new {@link SAXModuleMarshaller}(factory, optable);
 *   manager.{@link #registerMarshaller(ProxyMarshaller) registerMarshaller}(marshaller);
 *   manager.{@link #registerUnmarshaller(ProxyUnmarshaller) registerUnmarshaller}(marshaller);
 *   final {@link File} file = new File("myfile.wmod");
 *   final {@link net.sourceforge.waters.model.module.ModuleProxy} module = ({@link net.sourceforge.waters.model.module.ModuleProxy}) manager.{@link #load(File) load}(file);
 * </PRE>
 *
 * @author Robi Malik
 */

public class DocumentManager
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new document manager. Initially, the document manager will not
   * support marshalling or unmarshalling of any file types. To be useful, some
   * {@link ProxyMarshaller} or {@link ProxyUnmarshaller} objects need to be
   * registered using the {@link #registerMarshaller(ProxyMarshaller)
   * registerMarshaller()} or {@link #registerUnmarshaller(ProxyUnmarshaller)
   * registerMarshaller()} methods.
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
    mDocumentCache = new HashMap<URI,DocumentEntry>(32);
  }


  //#########################################################################
  //# Accessing Documents
  /**
   * Loads a document relative to a URI. This methods loads a document with a
   * given name and location, automatically computing the file extension from
   * the expected class.
   * @param uri
   *          The URI to be used to resolve the name.
   * @param name
   *          The name of the file without extension. The extension is
   *          determined automatically using a {@link ProxyMarshaller}.
   * @param clazz
   *          The desired class of the document to be loaded. This is used to
   *          identify an appropriate {@link ProxyUnmarshaller}.
   * @return The loaded document. This may just be a cached copy, or it may
   *         actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException
   *           to indicate that parsing the input file has failed for some
   *           reason.
   * @throws IOException
   *           to indicate that the input file could not be opened or read.
   * @throws IllegalArgumentException
   *           to indicate that no registered {@link ProxyUnmarshaller} for
   *           the given class was found.
   */
  public <DD extends DocumentProxy> DD load(final URI uri, final String name,
      final Class<DD> clazz) throws WatersUnmarshalException, IOException
  {
    final Collection<ProxyUnmarshaller<? extends DD>> unmarshallers =
        findProxyUnmarshallers(clazz);
    for (final ProxyUnmarshaller<? extends DD> unmarshaller : unmarshallers) {
      final String extname = name + unmarshaller.getDefaultExtension();
      final URI resolved = resolve(uri, extname);
      final DocumentProxy cached = getCachedDocument(resolved);
      if (cached != null) {
        return clazz.cast(cached);
      }
      final long time = System.currentTimeMillis();
      final DD loaded = unmarshaller.unmarshal(resolved);
      final DocumentEntry entry = new DocumentEntry(resolved, loaded, time);
      mDocumentCache.put(resolved, entry);
      return loaded;
    }
    throw new FileNotFoundException("Can't find file for " + clazz.getName()
        + " named '" + name + "' relative to '" + uri + "'!");
  }

  /**
   * Loads a document from a URI. This methods loads a document from a given
   * file name, guessing the expected class from the file name extension.
   * @param uri
   *          A URI specifying the location of the document to be retrieved.
   * @return The loaded document. This may be just be a cached copy, or it may
   *         actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException
   *           to indicate that parsing the input file has failed for some
   *           reason.
   * @throws IOException
   *           to indicate that the input file could not be opened or read.
   */
  public DocumentProxy load(final URI uri) throws WatersUnmarshalException,
      IOException
  {
    final DocumentProxy cached = getCachedDocument(uri);
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
    final long time = System.currentTimeMillis();
    final DocumentProxy loaded = unmarshaller.unmarshal(uri);
    final DocumentEntry entry = new DocumentEntry(uri, loaded, time);
    mDocumentCache.put(uri, entry);
    return loaded;
  }

  /**
   * Loads a document from a file. This methods loads a document with a given
   * name and location, automatically computing the file extension from the
   * expected class.
   * @param path
   *          The directory containing the file to be loaded.
   * @param name
   *          The name of the file without extension. The extension is
   *          determined automatically using a {@link ProxyUnmarshaller}.
   * @param clazz
   *          The desired class of the document to be loaded. This is used to
   *          identify an appropriate {@link ProxyUnmarshaller}.
   * @return The loaded document. This may just be a cached copy, or it may
   *         actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException
   *           to indicate that parsing the input file has failed for some
   *           reason.
   * @throws IOException
   *           to indicate that the input file could not be opened or read.
   * @throws IllegalArgumentException
   *           to indicate that no registered {@link ProxyUnmarshaller} for
   *           the given class was found.
   */
  public <DD extends DocumentProxy> DD load(final File path, final String name,
      final Class<DD> clazz) throws WatersUnmarshalException, IOException
  {
    final Collection<ProxyUnmarshaller<? extends DD>> unmarshallers =
        findProxyUnmarshallers(clazz);
    for (final ProxyUnmarshaller<? extends DD> unmarshaller : unmarshallers) {
      final String extname = name + unmarshaller.getDefaultExtension();
      final File filename = new File(path, extname);
      final URI uri = filename.toURI();
      final DocumentProxy cached = getCachedDocument(uri);
      if (cached != null) {
        return clazz.cast(cached);
      }
      if (filename.canRead()) {
        final long time = System.currentTimeMillis();
        final DD loaded = unmarshaller.unmarshal(uri);
        final DocumentEntry entry = new DocumentEntry(uri, loaded, time);
        mDocumentCache.put(uri, entry);
        return loaded;
      }
    }
    throw new FileNotFoundException("Can't find file for " + clazz.getName()
        + " named '" + name + "' in directory " + path + "!");
  }

  /**
   * Loads a document from a URL. This methods loads a document from a given
   * file name, guessing the expected class from the file name extension.
   * @param url
   *          A URL specifying the location of the document to be retrieved.
   * @return The loaded document. This may be just be a cached copy, or it may
   *         actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException
   *           to indicate that parsing the input file has failed for some
   *           reason.
   * @throws IOException
   *           to indicate that the input file could not be opened or read.
   */
  public DocumentProxy load(final URL url) throws WatersUnmarshalException,
      IOException
  {
    try {
      final URI uri = url.toURI();
      return load(uri);
    } catch (final URISyntaxException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  /**
   * Loads a document from a file. This methods loads a document from a given
   * file name, guessing the expected class from the file name extension.
   * @param filename
   *          The absolute path of the file to be loaded.
   * @return The loaded document. This may be just be a cached copy, or it may
   *         actually be retrieved by reading an external file.
   * @throws WatersUnmarshalException
   *           to indicate that parsing the input file has failed for some
   *           reason.
   * @throws IOException
   *           to indicate that the input file could not be opened or read.
   */
  public DocumentProxy load(final File filename)
      throws WatersUnmarshalException, IOException
  {
    final URI uri = filename.toURI();
    return load(uri);
  }

  /**
   * Checks whether a cached document has been modified externally. This method
   * checks whether file for the document, given by its URI, has been modified
   * since it has last been accessed through the document manager. The check is
   * only performed for file URIs, other URIs are assumed never to change their
   * contents, so this method always returns <CODE>false</CODE> for them.
   * @throws MalformedURLException
   *           if the given URI is not a proper URI.
   * @throws IllegalArgumentException
   *           if the given URI does not represent a document currently in the
   *           cache.
   */
  public boolean hasBeenModified(final URI uri) throws MalformedURLException
  {
    final DocumentEntry entry = mDocumentCache.get(uri);
    if (uri != null) {
      return entry.hasBeenModified();
    } else {
      throw new IllegalArgumentException("URI " + uri + " not in cache!");
    }
  }

  /**
   * Checks whether a cached document has been modified externally. This method
   * checks whether the file for the document, given by its file name, has been
   * modified since it has last been accessed through the document manager.
   * @throws IllegalArgumentException
   *           if the given file name does not represent a document currently in
   *           the cache.
   */
  public boolean hasBeenModified(final File filename)
  {
    try {
      final URI uri = filename.toURI();
      return hasBeenModified(uri);
    } catch (final MalformedURLException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  /**
   * Saves a document to a file. This methods writes a document to a given file
   * name, using the marshaller appropriate to the document's class. If the file
   * name differs from the document's current location, the document manager
   * cache will be updated to reflect the name change.
   * @param doc
   *          The document to be written.
   * @param filename
   *          The absolute path of the file to be written.
   * @throws WatersMarshalException
   *           to indicate a failure while writing the data structures.
   * @throws IOException
   *           to indicate that the output file could not be opened or written.
   * @throws IllegalArgumentException
   *           to indicate that no registered {@link ProxyMarshaller} for the
   *           given class was found.
   */
  public void saveAs(final DocumentProxy doc, final File filename)
      throws WatersMarshalException, IOException
  {
    @SuppressWarnings("unchecked")
    final Class<DocumentProxy> clazz =
      (Class<DocumentProxy>) doc.getClass();
    final ProxyMarshaller<DocumentProxy> marshaller =
        findProxyMarshaller(clazz);
    final URI newuri = filename.toURI();
    final URI olduri = doc.getLocation();
    marshaller.marshal(doc, filename);
    mDocumentCache.remove(olduri);
    final long time = System.currentTimeMillis();
    final DocumentEntry entry = new DocumentEntry(newuri, doc, time);
    doc.setLocation(newuri);
    mDocumentCache.put(newuri, entry);
  }

  /**
   * Adds a new document to this document manager. This method adds the given
   * document to the cache maintained by the document manager, under the
   * location given by the document. It does not make any attempt to save the
   * document to disk. If the document manager has already stored a document at
   * the given location, the new document will replace the existing one.
   * @param doc
   *          The document to be added. It should provide a valid file location
   *          where the document can be looked up and saved.
   */
  public void newDocument(final DocumentProxy doc)
  {
    final URI uri = doc.getLocation();
    final DocumentEntry entry = new DocumentEntry(uri, doc, 0);
    mDocumentCache.put(uri, entry);
  }

  /**
   * Removes a document from the cache. This method removes any cached document
   * for the given URI, in order to save some memory.
   */
  public void remove(final URI uri)
  {
    mDocumentCache.remove(uri);
  }

  /**
   * Determines a full URI for a given document name relative to another
   * document. For example, this method can be used to locate the files for
   * {@link net.sourceforge.waters.model.module.InstanceProxy InstanceProxy}
   * items found within a module.
   * @param  context  The document containing the name to be looked up.
   * @param  base     The name of the document to be looked up.
   * @param  clazz    The expected class of the document to be looked up.
   */
  public <DD extends DocumentProxy> URI resolve(final DocumentProxy context,
      final String base, final Class<DD> clazz) throws WatersUnmarshalException
  {
    final URI uri = context.getLocation();
    final ProxyMarshaller<DD> marshaller = findProxyMarshaller(clazz);
    final String ext = marshaller.getDefaultExtension();
    return resolve(uri, base + ext);
  }


  //#########################################################################
  //# Registering Marshallers
  /**
   * Registers a proxy marshaller.
   *
   * @param marshaller
   *          The proxy marshaller to be registered.
   */
  public <DD extends DocumentProxy> void registerMarshaller(
      final ProxyMarshaller<DD> marshaller)
  {
    final Class<DD> clazz = marshaller.getDocumentClass();
    if (mClassMarshallerMap.containsKey(clazz)) {
      throw new IllegalArgumentException(
          "Registering a second marshaller for class '" + clazz.getName()
              + "'!");
    }
    mClassMarshallerMap.put(clazz, marshaller);
  }

  /**
   * Registers a proxy unmarshaller. This method registers the given
   * unmarshaller object with this document manager, so future calls to the
   * {@link #load(File) load()} method can use it to load documents of the type
   * supported by it.
   *
   * @param unmarshaller
   *          The proxy unmarshaller to be registered.
   */
  public <DD extends DocumentProxy> void registerUnmarshaller(
      final ProxyUnmarshaller<DD> unmarshaller)
  {
    final Class<DD> clazz = unmarshaller.getDocumentClass();
    List<ProxyUnmarshaller<? extends DocumentProxy>> list =
        mClassUnmarshallerMap.get(clazz);
    if (list == null) {
      list = new LinkedList<ProxyUnmarshaller<? extends DocumentProxy>>();
      mClassUnmarshallerMap.put(clazz, list);
    }
    list.add(unmarshaller);
    final Collection<String> extensions = unmarshaller.getSupportedExtensions();
    for (final String extension : extensions) {
      final String lowerext = extension.toLowerCase();
      if (mExtensionUnmarshallerMap.containsKey(lowerext)) {
        throw new IllegalArgumentException(
            "Registering a second unmarshaller for extension '" + extension
                + "'!");
      }
      mExtensionUnmarshallerMap.put(lowerext, unmarshaller);
    }
    mFileFilters.addAll(unmarshaller.getSupportedFileFilters());
  }


  //#########################################################################
  //# Finding Marshallers
  /**
   * Gets the marshaller used to marshal objects of a given class. If there is
   * no marshaller for exactly the given class, this method tries all
   * superclasses and superinterfaces and returns a marshaller for the first
   * supported class or interface found.
   * @param clazz
   *          The class of objects to be looked up.
   * @return The {@link ProxyMarshaller} used by this document manager to handle
   *         objects of the given class.
   * @throws IllegalArgumentException
   *           to indicate that no registered {@link ProxyMarshaller} for the
   *           given class was found.
   */
  public <DD extends DocumentProxy> ProxyMarshaller<DD> findProxyMarshaller
    (final Class<DD> clazz)
  {
    final ProxyMarshaller<?> marshaller = getProxyMarshaller(clazz);
    if (marshaller != null) {
      final Class<?> preclazz = ProxyMarshaller.class;
      @SuppressWarnings("unchecked")
      final Class<ProxyMarshaller<DD>> marshallerclazz =
        (Class<ProxyMarshaller<DD>>) preclazz;
      return marshallerclazz.cast(marshaller);
    } else {
      throw new IllegalArgumentException("Unsupported document class " +
                                         clazz.getName() + "!");
    }
  }

  /**
   * Searches for a marshaller that uses the given file filter as its default
   * file filter. This method can be used to choose a marshaller based on the
   * file extension selected by the user in a file dialog, provided that the
   * dialog is set up using file filters obtained from this document manager.
   * @param filter
   *           The file filter to search for.
   * @return   The first {@link ProxyMarshaller} that uses the given file filter
   *           as its default file filter, using object identity to test for
   *           equality.
   * @throws IllegalArgumentException
   *           to indicate that no registered {@link ProxyMarshaller} using
   *           the given file filter as default has been found.
   */
  public ProxyMarshaller<? extends DocumentProxy> findProxyMarshaller
    (final FileFilter filter)
  {
    for (final ProxyMarshaller<? extends DocumentProxy> marshaller :
         mClassMarshallerMap.values()) {
      if (marshaller.getDefaultFileFilter() == filter) {
        return marshaller;
      }
    }
    throw new IllegalArgumentException
      ("Can't find marshaller with default file filter '" +
       filter.getDescription() + "'!");
  }

  /**
   * Gets the unmarshaller used to unmarshal objects of a given class.
   * @param clazz
   *          The class of objects to be looked up.
   * @return A list of {@link ProxyUnmarshaller} objects used by this document
   *         manager to handle objects of the given class.
   * @throws IllegalArgumentException
   *           to indicate that no registered {@link ProxyUnmarshaller} for the
   *           given class was found.
   */
  @SuppressWarnings("unchecked")
  public <DD extends DocumentProxy> Collection<ProxyUnmarshaller<? extends DD>>
    findProxyUnmarshallers(final Class<DD> clazz)
  {
    final Collection<ProxyUnmarshaller<? extends DocumentProxy>> unmarshallers =
        mClassUnmarshallerMap.get(clazz);
    if (unmarshallers != null) {
      final Collection<?> precast = unmarshallers;
      return (Collection<ProxyUnmarshaller<? extends DD>>) precast;
    } else {
      throw new IllegalArgumentException("Unsupported document class "
          + clazz.getName() + "!");
    }
  }

  public List<FileFilter> getSupportedFileFilters()
  {
    return Collections.unmodifiableList(mFileFilters);
  }


  //#########################################################################
  //# Auxiliary Methods
  private DocumentProxy getCachedDocument(final URI uri)
      throws WatersUnmarshalException
  {
    try {
      final DocumentEntry entry = mDocumentCache.get(uri);
      if (entry == null || entry.hasBeenModified()) {
        return null;
      } else {
        return entry.getDocument();
      }
    } catch (final MalformedURLException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  private ProxyMarshaller<?> getProxyMarshaller(final Class<?> clazz)
  {
    if (DocumentProxy.class.isAssignableFrom(clazz)) {
      ProxyMarshaller<?> marshaller = mClassMarshallerMap.get(clazz);
      if (marshaller != null) {
        return marshaller;
      }
      final Class<?> superclass = clazz.getSuperclass();
      if (superclass != null) {
        marshaller = getProxyMarshaller(superclass);
        if (marshaller != null) {
          return marshaller;
        }
      }
      final Class<?>[] interfaces = clazz.getInterfaces();
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
    try {
      if (base != null) {
        final URI tailuri = new URI(null, tailname, null);
        return base.resolve(tailuri);
      } else {
        final File tailfile = new File(tailname);
        final URI result = tailfile.toURI();
        if (result.isAbsolute()) {
          return result;
        } else {
          throw new WatersUnmarshalException
            ("Trying to load from relative location '" + tailname +
             "' without known path!");
        }
      }
    } catch (final URISyntaxException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }


  //#########################################################################
  //# Inner Class DocumentEntry
  private static class DocumentEntry
  {

    //#######################################################################
    //# Constructors
    private DocumentEntry(final URI uri, final DocumentProxy doc,
        final long time)
    {
      mURI = uri;
      mDocument = doc;
      mOpeningTime = time;
    }

    //#######################################################################
    //# Simple Access
    private DocumentProxy getDocument()
    {
      return mDocument;
    }

    //#######################################################################
    //# Checking for File Modification
    private boolean hasBeenModified() throws MalformedURLException
    {
      final File file = getFile();
      if (file != null) {
        final long modtime = file.lastModified();
        return modtime > mOpeningTime;
      } else {
        return false;
      }
    }

    private File getFile() throws MalformedURLException
    {
      final URL url = mURI.toURL();
      final String proto = url.getProtocol();
      if (proto.equals("file")) {
        final String path = mURI.getPath();
        return new File(path);
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Data Members
    private final URI mURI;
    private final DocumentProxy mDocument;
    private final long mOpeningTime;

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
  private final Map<URI,DocumentEntry> mDocumentCache;

}
