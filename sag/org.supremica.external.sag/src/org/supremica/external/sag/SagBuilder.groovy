package org.supremica.external.sag;


class SagBuilder extends BuilderSupport {
	static void main(args) {
		def y1
		def sagBuilder = new SagBuilder();
		sagBuilder.project(name:'testProject') {
			graph(name:'testGraph', maxNrOfObjects:3) {
				y1 = sensor(name:'y1')
				sensor(name:'y2')
				sensor(name:'y3')
				zone(back:'y1', front:'y2', oneway:true)
				zone(front:'y1', oneway:false)
				zone(back:'y2', front:'y3')
			}
		}
		def project = sagBuilder.project
		assert y1.name == 'y1' && y1.graph.project == project
		assert project.sensor.name == ['y1', 'y2', 'y3']
		assert project.graph.name == ['testGraph']
		assert project.graph.node.name == ['y1', 'y2', 'y3']
		assert project.graph.zone.collect{[it.back.name,it.front.name]} == [['y1', 'y2'],['end0', 'y1'],['y2','y3']]
		assert !project.graph.zone.find{it.front.name=='y1'}.oneway
		assert project.graph.zone.find{it.back.name=='y2' && it.front.name=='y3'}.oneway
		assert project.graph.find{it.name == 'testGraph'}.maxNrOfObjects == 3;
	}

	Project project
	private String currentSensorName
	private String backSensorName
	private String frontSensorName
	
	def createNode(name){
		switch (name) {
		case 'project' : SagFactory.eINSTANCE.createProject()
		}
		null
	}

	def createNode(name, value){
		null
	}

	def createNode(name, Map attributes){
		def node = null
		Closure createZone {
			node = SagFactory.eINSTANCE.createZone()
			frontSensorName = attributes.front
			attributes.remove('front')
			backSensorName = attributes.back
			attributes.remove('back')
		}
		switch (name) {
		case 'project' :
			node = SagFactory.eINSTANCE.createProject()
			project = node
			break
		case 'sensor' :
			node = SagFactory.eINSTANCE.createSensorNode()
			currentSensorName = attributes.name
			attributes.remove('name')
			break
		case 'graph' :
			node = SagFactory.eINSTANCE.createGraph()
			break
		case 'twowayZone' :
			createZone();
			attributes.oneway = false
			break
		case 'onewayZone' :
			attributes.oneway = true
		case 'zone' :
			createZone();
			break
		}
		attributes.each {key, value ->
			node[key] = value
		}
		return node
	}

	def createNode(name, Map attributes, value){
		null
	}
	
	void setParent(parent, child){
		switch (parent) {
		case Project:
			switch (child) {
			case Graph:
				parent.graph << child
				break
			}
			break;
		case Graph:
			switch (child) {
			case SensorNode:
				parent.node << child
				child.name = currentSensorName
				currentSensorName = null
				break
			case Zone:
				parent.zone << child
				Closure createEndNode  = {
					def endNode = SagFactory.eINSTANCE.createEndNode();
					endNode.name = "end${parent.node.findAll{it instanceof EndNode}.size()}"
					endNode
				}
				child.back = backSensorName ? parent.node.find{it.name == backSensorName} : createEndNode()
				backSensorName = null
				child.front = frontSensorName ? parent.node.find{it.name == frontSensorName} : createEndNode()
				frontSensorName = null
				break
			}
			break
		}
	}

	void nodeCompleted(parent, node) {
		
	}
}