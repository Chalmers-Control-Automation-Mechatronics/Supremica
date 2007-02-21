package org.supremica.external.sag.examples
import org.supremica.external.sag.SagBuilder
import org.supremica.external.sag.automaton.AutomatonGenerator
import net.sourceforge.waters.model.compiler.CompilerOperatorTable
import net.sourceforge.waters.model.expr.OperatorTable
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller
import net.sourceforge.waters.model.marshaller.ProxyMarshaller
import net.sourceforge.waters.model.marshaller.WatersMarshalException
import net.sourceforge.waters.subject.module.ModuleSubjectFactory
import net.sourceforge.waters.gui.*
import org.supremica.gui.ide.IDE
import org.supremica.gui.InterfaceManager

SagBuilder builder = new SagBuilder();

builder.project(name:'LiftSystem') {
	controlSignal(name:'up')
	controlSignal(name:'out')
	graph(name:'Ball', maxNrOfObjects:3) {
		sensor(name:'ballDown')
		sensor(name:'ballUp')
		onewayZone(front:'ballDown', frontEntryCondition:'liftDown', outsideSystemBoundry:true)
		twowayZone(back:'ballDown', front:'ballUp', capacity:1,
		           forwardCondition:'up', backwardCondition:'!up',
		           backExitCondition:'!liftUp', backEntryCondition:'!liftUp',
		           frontExitCondition:'!liftDown', frontEntryCondition:'!liftDown')
		onewayZone(back:'ballUp', backExitCondition:'out', outsideSystemBoundry:true)
	}
	graph(name:'Lift', maxNrOfObjects:1) {
		sensor(name:'liftDown', initiallyActivated:true)
		sensor(name:'liftUp')
		twowayZone(back:'liftDown', front:'liftUp', forwardCondition:'up', backwardCondition:'!up')
	}
}

def module = AutomatonGenerator.instance.generate(builder.project, false)

def saveToFile = { filename ->
	def marshaller = new JAXBModuleMarshaller(ModuleSubjectFactory.instance, CompilerOperatorTable.instance)
	marshaller.marshal(module, new File(filename))
}
def launchIDE = {
	InterfaceManager.instance.initLookAndFeel();
	IDE ide = new IDE()
	ide.visible = true
	ide.installContainer(module)
//ide.openFiles([automataFile, extendedAutomataFile])
}
saveToFile("${module.name}.${WmodFileFilter.WMOD}")
launchIDE()
