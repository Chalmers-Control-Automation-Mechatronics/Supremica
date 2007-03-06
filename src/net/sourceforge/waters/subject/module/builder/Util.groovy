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

class Util {
	private static final factory = ModuleSubjectFactory.instance
	private static final parser = new ExpressionParser(factory.instance, CompilerOperatorTable.instance)

	public static assertGeneratedModuleEqualsManual(ModuleProxy generatedModule, ModuleProxy manualModule) {
		def generated, manual
		assert ProxyTools.isEqualListByContents(generated = generatedModule.eventDeclList, manual = manualModule.eventDeclList), "\ngenerated:$generated\nmanual   :$manual"
		def generatedComponents = generatedModule.componentList.grep(SimpleComponentProxy.class) + generatedModule.componentList.grep(ForeachComponentProxy.class).body 
		def manualComponents = manualModule.componentList.grep(SimpleComponentProxy.class) + manualModule.componentList.grep(ForeachComponentProxy.class).body 
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.nodes, manual = manualComponents.graph.nodes), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.labelBlock, manual = manualComponents.graph.edges.labelBlock), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.source, manual = manualComponents.graph.edges.source), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.target, manual = manualComponents.graph.edges.target), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.guardActionBlock.findAll{it}.guards, manual = manualComponents.graph.edges.guardActionBlock.findAll{it}.guards), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.guardActionBlock.findAll{it}.actions, manual = manualComponents.graph.edges.guardActionBlock.findAll{it}.actions), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges.guardActionBlock, manual = manualComponents.graph.edges.guardActionBlock), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph.edges, manual = manualComponents.graph.edges), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.graph, manual = manualComponents.graph), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedComponents.variables, manual = manualComponents.variables), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedModule.componentList, manual = manualModule.componentList), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedModule.eventAliasList.expression.eventList, manual = manualModule.eventAliasList.expression.eventList), "\ngenerated:$generated\nmanual   :$manual"
		assert ProxyTools.isEqualListByContents(generated = generatedModule.eventAliasList, manual = manualModule.eventAliasList), "\ngenerated:$generated\nmanual   :$manual"
		assert generatedModule.equalsByContents(manualModule)
	}
	
	public static saveModuleToFile(ModuleProxy module, String filename = "./${module.name}.${WmodFileFilter.WMOD}") {
    	saveModuleToFile(module, [filename] as File)
	}
	
	public static saveModuleToFile(ModuleProxy module, File file) {
		def marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.instance)
    	marshaller.marshal(module, file)
	}
	
	private static IDE ide
	
	public static openModuleInSupremica(ModuleProxy module) {
			InterfaceManager.instance.initLookAndFeel();
			if (!ide) ide = new IDE()
			ide.visible = true
			ide.installContainer(module)
	}

}