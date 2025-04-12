/******************* MonolithicSynthesis.java *************************/
/* Java script for doing monolithic synthesis of the in Supremica
 * currently open module.
 */
package Lupremica;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.des.AutomatonProxy;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;

import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.MarshallingTools;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.DocumentContainerManager;

import org.apache.logging.log4j.Logger;

public class MonolithicSynthesis
{
    public MonolithicSynthesis(org.supremica.gui.ide.IDE ide) // called by RunScript
    	throws ParseException, EvalException, AnalysisException
    {
        final Logger logger = ide.getTheLog();
        logger.info("Monolithic synthesis of current module");

        // Compile the module to a ProductDESProxy ...
          // Create a compiler
          DocumentManager manager = ide.getDocumentManager();
          final ProductDESProxyFactory desFactory =
            ProductDESElementFactory.getInstance();
         final ModuleSubject module =
              ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();
          final ModuleCompiler compiler =
            new ModuleCompiler(manager, desFactory, module);
          // Configure the compiler
          // To make it shorter, we only need normalisation for the simple example in the paper.
          // ... optimisation removes selfloops and redundant components
          compiler.setOptimizationEnabled(true);
          // ... normalisation is needed for this module with advanced features
          compiler.setNormalizationEnabled(true);
          // ... automaton variables are not used here, this could be turned off
          compiler.setAutomatonVariablesEnabled(true);
          // ... only report the first error even if there are several
          compiler.setMultiExceptionsEnabled(false);
          // Now compile the module
          final ProductDESProxy des = compiler.compile();

          final SupervisorSynthesizer synthesizer =
            new MonolithicSynthesizer(desFactory);
          synthesizer.setModel(des);
          synthesizer.run();
          final ProductDESResult result = synthesizer.getAnalysisResult();
          if (result.isSatisfied()) {
            final ProductDESProxy supervisor = result.getComputedProductDES();
			// factory must be ModuleSubjectFactory
			final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
			final ProductDESImporter importer = new ProductDESImporter(factory);
			final ModuleSubject supervisorModule = (ModuleSubject) importer.importModule(supervisor);
			final java.util.List<AbstractSubject> supervisorComponents =
				supervisorModule.getComponentListModifiable();
			// Add the synthesized compnents to the module
/*		    for (final AutomatonProxy aut : supervisor.getAutomata()) {
				final SimpleComponentSubject comp =
			  		(SimpleComponentSubject) importer.importComponent(aut);
				module.getComponentListModifiable().add(comp);
		    }*/
		    final java.util.Iterator<AutomatonProxy> it = supervisor.getAutomata().iterator();
		    while(it.hasNext())
		    {
				final AutomatonProxy aut = it.next();
				final SimpleComponentSubject comp =
							  		(SimpleComponentSubject) importer.importComponent(aut);
				module.getComponentListModifiable().add(comp);
			}
			logger.info("Synthesized supervisor(s) added to module");
          } else {
            logger.info("Synthesis result is empty.");
          }
    }
}