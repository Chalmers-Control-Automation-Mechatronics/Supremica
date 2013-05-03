
package org.supremica.automata.IO;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import javax.swing.filechooser.FileFilter;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;

/**
 * TCTUnmarshaller class to import TCT binary files.
 * This class has been tested for Xptct138 DES files.
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

public class TCTUnmarshaller implements ProxyUnmarshaller<ModuleProxy>
{
    private ModuleSubject module;

    public TCTUnmarshaller(final ModuleProxyFactory factory) {
      // TODO Fix bug - must use factory to create model
      // this.factory = factory;
    }

    public ModuleProxy unmarshal(final URI uri) throws WatersUnmarshalException, IOException {
        final String file = uri.toURL().getFile();
        final String name = file.substring(file.lastIndexOf('/')+1, file.lastIndexOf('.'));
        // TODO Use factory, do not assume subject
        module = new ModuleSubject(name, uri);
        final ExtendedAutomata exAutomata = new ExtendedAutomata(module);
        final TCTConverter tct = new TCTConverter(uri);
        final ExtendedAutomaton exAutomaton = tct.getExtendedAutomaton();
        exAutomata.addAutomaton(exAutomaton);
        return module;
    }

    public Class<ModuleProxy> getDocumentClass() {
        return ModuleProxy.class;
    }

    public String getDefaultExtension() {
        return ".des";
    }

    public Collection<String> getSupportedExtensions() {
        return Collections.singletonList(getDefaultExtension());
    }

    public Collection<FileFilter> getSupportedFileFilters() {
        final FileFilter filter = new StandardExtensionFileFilter("DES files [*.des]", getDefaultExtension());
        return Collections.singletonList(filter);
    }

    public DocumentManager getDocumentManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDocumentManager(final DocumentManager manager) {
        // this.manager = manager;
    }

}
