
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
 * ADSConverter class to import ADS files. This original ADSUnmarshaller class 
 * is replace with this since the former doesn't work for most of the files! 
 * If one fixes the original class then this can be ignored.
 * 
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */

public class ADSUnmarshaller2 implements ProxyUnmarshaller<ModuleProxy>{
    private ModuleSubject module;
    private DocumentManager manager;

    public ADSUnmarshaller2(ModuleProxyFactory factory) {
        this.manager = null;
    }

    public ModuleProxy unmarshal(URI uri) throws WatersUnmarshalException, IOException {
        String file = uri.toURL().getFile();
        String name = file.substring(file.lastIndexOf('/')+1, file.lastIndexOf('.'));
        module = new ModuleSubject(name, uri);
        final ExtendedAutomata exAutomata = new ExtendedAutomata(module);
        ADSConverter ads = new ADSConverter(uri);
        ExtendedAutomaton exAutomaton = ads.getExtendedAutomaton();
        exAutomata.addAutomaton(exAutomaton);
        return module;
    }

    public Class<ModuleProxy> getDocumentClass() {
        return ModuleProxy.class;
    }

    public String getDefaultExtension() {
        return ".ads";
    }

    public Collection<String> getSupportedExtensions() {
        return Collections.singletonList(getDefaultExtension());
    }

    public Collection<FileFilter> getSupportedFileFilters() {
        final FileFilter filter = new StandardExtensionFileFilter("ADS files [*.ads]", getDefaultExtension());
        return Collections.singletonList(filter);
    }

    public DocumentManager getDocumentManager() {
        return manager;
    }

    public void setDocumentManager(DocumentManager manager) {
        this.manager = manager;
    }
    
}
