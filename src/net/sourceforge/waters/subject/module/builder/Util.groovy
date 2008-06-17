package net.sourceforge.waters.subject.module.builder;

import groovy.util.BuilderSupport
import net.sourceforge.waters.model.base.ProxyTools
import net.sourceforge.waters.subject.module.VariableComponentSubject
import net.sourceforge.waters.model.module.*
import net.sourceforge.waters.subject.module.*
import net.sourceforge.waters.gui.*
import net.sourceforge.waters.model.compiler.CompilerOperatorTable
import net.sourceforge.waters.model.expr.OperatorTable
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller
import net.sourceforge.waters.model.marshaller.ProxyMarshaller
import net.sourceforge.waters.model.marshaller.WatersMarshalException
import net.sourceforge.waters.model.module.ModuleProxy
import net.sourceforge.waters.model.module.ModuleProxyFactory
import net.sourceforge.waters.subject.module.ModuleSubjectFactory
import net.sourceforge.waters.model.expr.ExpressionParser
import net.sourceforge.waters.xsd.base.ComponentKind
import net.sourceforge.waters.xsd.base.EventKind
import net.sourceforge.waters.subject.module.EdgeSubject
import net.sourceforge.waters.model.expr.Operator
import org.supremica.gui.ide.ModuleContainer
import org.supremica.gui.ide.IDE
import org.supremica.gui.InterfaceManager
import org.supremica.automata.IO.ProjectBuildFromWaters
import org.supremica.automata.Project
import org.supremica.automata.algorithms.AutomataVerifier
import net.sourceforge.waters.model.marshaller.DocumentManager
import org.supremica.gui.SupremicaLoggerFactory


class Util {
		
	public static void assertGeneratedModuleEqualsManual(ModuleProxy generatedModule, ModuleProxy manualModule) {
		def generated, manual
		assert ProxyTools.isEqualListByContents(generated = new ArrayList(generatedModule.eventDeclList).sort{it.name}, manual = new ArrayList(manualModule.eventDeclList).sort{it.name}), "\ngenerated:$generated\nmanual   :$manual\n"
		def generatedComponents = ((generatedModule.componentList.grep(SimpleComponentProxy.class) + generatedModule.componentList.grep(ForeachComponentProxy.class).body)).sort{it.name}
		def manualComponents = ((manualModule.componentList.grep(SimpleComponentProxy.class) + manualModule.componentList.grep(ForeachComponentProxy.class).body)).sort{it.name}
		assert (generated = generatedModule.componentList.name.sort()) == (manual = manualModule.componentList.name.sort()), "\nComponent names\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.collect{it.nodes.name}) == (manual = manualComponents.graph.collect{it.nodes.name}), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.kind) == (manual = manualComponents.kind), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.nodes.collect{[it.name, it.propositions.eventList.name]}) == (manual = manualComponents.graph.nodes.collect{[it.name, it.propositions.eventList.name]}), "\nstate marking\ngenerated:$generated\nmanual   :$manual\n"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.nodes.flatten(), manual = manualComponents.graph.nodes.flatten()), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.collect{it.edges.collect{it.labelBlock.eventList*.toString().sort()}}) == (manual = manualComponents.graph.collect{it.edges.collect{it.labelBlock.eventList*.toString().sort()}}), "\nlabelBlock\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.collect{it.edges.source.toString()}) == (manual = manualComponents.graph.collect{it.edges.source.toString()}), "\nSource node\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.collect{it.edges.target.toString()}) == (manual = manualComponents.graph.collect{it.edges.target.toString()}), "\nTarget node\ngenerated:$generated\nmanual   :$manual\n"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.guardActionBlock.flatten().findAll{it}.guards.flatten(), manual = manualComponents.graph.edges.guardActionBlock.flatten().findAll{it}.guards.flatten()), "\nGuards\ngenerated:${generated}\nmanual   :${manual}\n"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.guardActionBlock.flatten().findAll{it}.actions.flatten(), manual = manualComponents.graph.edges.guardActionBlock.flatten().findAll{it}.actions.flatten()), "\nActions\ngenerated:$generated\nmanual   :$manual\n"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.guardActionBlock.flatten(), manual = manualComponents.graph.edges.guardActionBlock.flatten()), "\ngenerated:$generated\nmanual   :$manual\n"
		//assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges, manual = manualComponents.graph.edges), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.deterministic) == (manual = manualComponents.graph.deterministic), "\ngenerated:$generated\nmanual   :$manual\n"
		//assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph, manual = manualComponents.graph), "\ngenerated:$generated\nmanual   :$manual\n"
		assert ProxyTools.isEqualListByContents(generated = generatedModule.componentList.grep(VariableComponentProxy.class).sort{it.name}, manual = manualModule.componentList.grep(VariableComponentProxy.class).sort{it.name}), "\ngenerated:$generated\nmanual   :$manual\n"
		//assert ProxyTools.isEqualListByContents(generated = generatedModule.componentList.findAll{true}.sort{it.name}, manual = manualModule.componentList.findAll{true}.sort{it.name}), "\ngenerated:${generated}\nmanual   :${manual}\n"
		assert ProxyTools.isEqualListByContents(generated = new ArrayList(generatedModule.eventAliasList.expression.eventList).sort{it.name}, manual = new ArrayList(manualModule.eventAliasList.expression.eventList).sort{it.name}), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedModule.eventAliasList.name.sort()) == (manual = manualModule.eventAliasList.name.sort()), "\ngenerated:$generated\nmanual   :$manual\n"
		assert generatedModule.name == manualModule.name, "\ngenerated:${generatedModule.name}\nmanual:   ${manualModule.name}\n"
//		assert generatedModule.equalsByContents(manualModule)
	}

	private static final marshaller = new JAXBModuleMarshaller(ModuleSubjectFactory.instance, CompilerOperatorTable.instance)

 	public static void saveModuleToFile(ModuleProxy module) {
 		saveModuleToFile module, "./${module.name}${marshaller.defaultExtension}"
	}
 
	public static void saveModuleToFile(ModuleProxy module, String filename) {
		saveModuleToFile module, new File(filename)
	}
	
	public static void saveModuleToFile(ModuleProxy module, File file) {
		marshaller.marshal module, file
	}
	
	private static final IDE ide
	
	public static void openInSupremica(ModuleProxy module) {
		InterfaceManager.instance.initLookAndFeel();
        SupremicaLoggerFactory.initialiseSupremicaLoggerFactory();
        IDE.logger = SupremicaLoggerFactory.createLogger(IDE.class);
		if (!ide) ide = new IDE()
		ide.visible = true
		//ide.documentManager.newDocument(module)
		ModuleContainer moduleContainer = [ide, module]
//		moduleContainer.addStandardPropositions()
		ide.documentContainerManager.addContainer(new ModuleContainer(ide, module))
	}
	public static boolean verifyNonblocking(ModuleProxy module) {
		def supremicaProjBuilderFromWatersModule = new ProjectBuildFromWaters(new DocumentManager());
        Project supremicaProject = supremicaProjBuilderFromWatersModule.build(module);
       	AutomataVerifier.verifyMonolithicNonblocking(supremicaProject)
	}
}