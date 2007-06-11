package net.sourceforge.waters.subject.module.builder;

import groovy.util.BuilderSupport
import net.sourceforge.waters.model.base.ProxyTools
import net.sourceforge.waters.subject.module.VariableSubject
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
import org.supremica.gui.ide.IDE
import org.supremica.gui.InterfaceManager
import org.supremica.automata.IO.ProjectBuildFromWaters
import org.supremica.automata.Project
import org.supremica.automata.algorithms.AutomataVerifier
import net.sourceforge.waters.model.marshaller.DocumentManager

class Util {
	private static final factory = ModuleSubjectFactory.instance
	private static final parser = new ExpressionParser(factory.instance, CompilerOperatorTable.instance)

	public static void assertGeneratedModuleEqualsManual(ModuleProxy generatedModule, ModuleProxy manualModule) {
		def generated, manual
		assert ProxyTools.isEqualListByContents(generated = new ArrayList(generatedModule.eventDeclList).sort{it.name}, manual = new ArrayList(manualModule.eventDeclList).sort{it.name}), "\ngenerated:$generated\nmanual   :$manual\n"
		def generatedComponents = ((generatedModule.componentList.grep(SimpleComponentProxy.class) + generatedModule.componentList.grep(ForeachComponentProxy.class).body)).sort{it.name}
		def manualComponents = ((manualModule.componentList.grep(SimpleComponentProxy.class) + manualModule.componentList.grep(ForeachComponentProxy.class).body)).sort{it.name}
		assert (generated = generatedModule.componentList.name.sort()) == (manual = manualModule.componentList.name.sort()), "\nComponent names\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.collect{it.nodes.name}) == (manual = manualComponents.graph.collect{it.nodes.name}), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.kind) == (manual = manualComponents.kind), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.nodes.collect{[it.name, it.propositions.eventList.name]}) == (manual = manualComponents.graph.nodes.collect{[it.name, it.propositions.eventList.name]}), "\nstate marking\ngenerated:$generated\nmanual   :$manual\n"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.nodes, manual = manualComponents.graph.nodes), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.collect{it.edges.collect{it.labelBlock.eventList*.toString().sort()}}) == (manual = manualComponents.graph.collect{it.edges.collect{it.labelBlock.eventList*.toString().sort()}}), "\nlabelBlock\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.collect{it.edges.source.toString()}) == (manual = manualComponents.graph.collect{it.edges.source.toString()}), "\nSource node\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.collect{it.edges.target.toString()}) == (manual = manualComponents.graph.collect{it.edges.target.toString()}), "\nTarget node\ngenerated:$generated\nmanual   :$manual\n"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.guardActionBlock.findAll{it}.guards, manual = manualComponents.graph.edges.guardActionBlock.findAll{it}.guards), "\nGuards\ngenerated:${generated}\nmanual   :${manual}\n"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.guardActionBlock.findAll{it}.actions, manual = manualComponents.graph.edges.guardActionBlock.findAll{it}.actions), "\nActions\ngenerated:$generated\nmanual   :$manual\n"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.guardActionBlock, manual = manualComponents.graph.edges.guardActionBlock), "\ngenerated:$generated\nmanual   :$manual\n"
		//assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges, manual = manualComponents.graph.edges), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedComponents.graph.deterministic) == (manual = manualComponents.graph.deterministic), "\ngenerated:$generated\nmanual   :$manual\n"
		//assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph, manual = manualComponents.graph), "\ngenerated:$generated\nmanual   :$manual\n"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.variables.sort{it.name}, manual = manualComponents.variables.sort{it.name}), "\ngenerated:$generated\nmanual   :$manual\n"
		//assert ProxyTools.isEqualListByContents(generated = generatedModule.componentList.findAll{true}.sort{it.name}, manual = manualModule.componentList.findAll{true}.sort{it.name}), "\ngenerated:${generated}\nmanual   :${manual}\n"
		assert ProxyTools.isEqualListByContents(generated = new ArrayList(generatedModule.eventAliasList.expression.eventList).sort{it.name}, manual = new ArrayList(manualModule.eventAliasList.expression.eventList).sort{it.name}), "\ngenerated:$generated\nmanual   :$manual\n"
		assert (generated = generatedModule.eventAliasList.name.sort()) == (manual = manualModule.eventAliasList.name.sort()), "\ngenerated:$generated\nmanual   :$manual\n"
		assert generatedModule.name == manualModule.name, "\ngenerated:${generatedModule.name}\nmanual:   ${manualModule.name}\n"
//		assert generatedModule.equalsByContents(manualModule)
	}
	
	public static void saveModuleToFile(ModuleProxy module, String filename = "./${module.name}.${WmodFileFilter.WMOD}") {
    	saveModuleToFile(module, [filename] as File)
	}
	
	public static void saveModuleToFile(ModuleProxy module, File file) {
		def marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.instance)
    	marshaller.marshal(module, file)
	}
	
	private static IDE ide
	
	public static void openInSupremica(ModuleProxy module) {
		InterfaceManager.instance.initLookAndFeel();
		if (!ide) ide = new IDE()
		ide.visible = true
		ide.installContainer(module)
	}
	public static boolean verifyNonblocking(ModuleProxy module) {
		def supremicaProjBuilderFromWatersModule = new ProjectBuildFromWaters(new DocumentManager());
        Project supremicaProject = supremicaProjBuilderFromWatersModule.build(module);
       	AutomataVerifier.verifyMonolithicNonblocking(supremicaProject)
	}
}