package org.supremica.external.operationframeworkto61131.util;
/**
 * @author LC
 *
 */
import java.lang.reflect.Method;

import org.plcopen.xml.tc6.Project;
import org.supremica.external.operationframeworkto61131.controlinfo.EquipmentStateLookUp;
import org.supremica.external.operationframeworkto61131.layout.common.Connection;
import org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn;
import org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointOut;
import org.supremica.external.operationframeworkto61131.layout.common.Position;
import org.supremica.external.operationframeworkto61131.layout.ladder.Coil;
import org.supremica.external.operationframeworkto61131.layout.ladder.Contact;
import org.supremica.external.operationframeworkto61131.layout.ladder.LeftPowerRail;
import org.supremica.external.operationframeworkto61131.layout.ladder.RightPowerRail;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;




import java.util.List;
import java.util.LinkedList;

public class DebugUtil {
	public static org.supremica.external.operationframeworkto61131.util.log.LogUtil log = org.supremica.external.operationframeworkto61131.util.log.LogUtil
			.getInstance();
	

	public List<Object> listOfEOP;

	public List<Object> listOfCOP;

	public List<Object> listOfInterlock;

	public org.supremica.manufacturingtables.xsd.fid.FunctionBlocks functionBlocks;

	public org.supremica.manufacturingtables.xsd.virtualResourcesV3.VirtualResources virtualResource;
	

	private org.supremica.manufacturingtables.xsd.cc.CycleStartConditions cycleStartConditions;

	private org.plcopen.xml.tc6.Project plcopenProject;

	public DebugUtil() {

		String path = "./";
		String configFileName = "config.xml";
		Constant.initialize(path, configFileName);
		
		// A reference of the project object,
		// plcopenProject = plcOpenObjectFactory.createProject();
		log = LogUtil.getInstance();
		// Create the PLCopen xml schema object tree.
		JAXButil JC = JAXButil.getInstance(Constant.PLC_OPEN_TC6);

		plcopenProject = (org.plcopen.xml.tc6.Project) JC.getRootElementObject(
				Constant.XML_FILE_PATH, Constant.PLCOPEN_IN_PUT_FILE);

		listOfCOP = JAXButil.getInstance(Constant.COP_XML_CONTEXT)
				.loadXMLFromPath(Constant.XML_FILE_PATH, Constant.COP_PREFIX);

		listOfEOP = JAXButil.getInstance(Constant.EOP_XML_CONTEXT)
				.loadXMLFromPath(Constant.XML_FILE_PATH, Constant.EOP_PREFIX);

		functionBlocks = (org.supremica.manufacturingtables.xsd.fid.FunctionBlocks) JAXButil
				.getInstance(Constant.FID_XML_CONTEXT).getRootElementObject(
						Constant.XML_FILE_PATH, Constant.FID_XML_FILE_NAME);

		virtualResource = (org.supremica.manufacturingtables.xsd.virtualResourcesV3.VirtualResources) JAXButil
				.getInstance(Constant.VIRTUAL_RESOURCES_XML_CONTEXT)
				.getRootElementObject(Constant.XML_FILE_PATH,
						Constant.VIRTUAL_RESOURCES_XML_FILE_NAME);

		listOfInterlock = JAXButil.getInstance(Constant.INTERLOCK_XML_CONTEXT)
				.loadXMLFromPath(Constant.XML_FILE_PATH,
						Constant.INTERLOCK_PREFIX);
		
		
		cycleStartConditions = (org.supremica.manufacturingtables.xsd.cc.CycleStartConditions)JAXButil.getInstance(Constant.CC_XML_CONTEXT)
		.getRootElementObject(Constant.XML_FILE_PATH,
				Constant.CC_XML_FILE_NAME);


		// functionBlocks
		// =(convertor.xsd.fid.FunctionBlocks)JAXButil.getInstance(Constant.FID_XML_CONTEXT)
		// .getRootElementObject(Constant.XML_FILE_PATH,
		// Constant.FID_XML_FILE_NAME);

		EquipmentStateLookUp equipmentStateLookUp = null;

		try {

			equipmentStateLookUp = (EquipmentStateLookUp) Class.forName(
					Constant.EQUIPMENT_STATE_LOOK_UP_IMPLEMENT).newInstance();
			//			 
		} catch (Exception e) {

			log
					.error("Can not build equipment state look up implement from class:"
							+ Constant.EQUIPMENT_STATE_LOOK_UP_IMPLEMENT);

			return;

		}

	}

	public static void printCommonLayoutObject(
			org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject commonObj) {

		log.info("==============================getLocalId:"
				+ commonObj.getLocalId());

		log.info("==============================Position:"
				+ commonObj.getPosition());

		log.info("==============================getHeight:"
				+ commonObj.getHeight());

		log.info("==============================getWidth:"
				+ commonObj.getWidth());

		int i = 0;
		for (ConnectionPointIn conIn : commonObj.getConnectionPointInList()) {

			log.info("----------------------ConIn" + i++);
			log.info("----------------------relPosition:"
					+ conIn.getRelPosition());

			printCommentConnectionIn(conIn);

		}

		int k = 0;
		for (ConnectionPointOut conOut : commonObj.getConnectionPointOutList()) {

			log.info("----------------------ConOut" + k++);
			log.info("----------------------relPosition:"
					+ conOut.getRelPosition());
			log.info("----------Expression" + conOut.getExpression());
			log
					.info("----------formalParameter:"
							+ conOut.getFormalParameter());

		}

		log.info("");
	}

	public static void printCommentConnectionIn(
			org.supremica.external.operationframeworkto61131.layout.common.CommonConnectionIn conIn) {

		int j = 0;
		for (Connection con : conIn.getConnections()) {

			log.info("----------Connection " + j++);
			log.info("----------refPosition " + conIn.getRelPosition());
			log.info("----------refId:" + con.getRefLocalId());

			for (Position position : con.getPositionList()) {

				log.info(position.toString());
			}
		}

	}

	/*
	 * private static LogUtil log = LogUtil.getInstance(); private static int
	 * nTab=0; private static String tab=""; public static void
	 * showFactory(Factory factory) {
	 * 
	 * log.debug(tab+"Description:" + factory.getDescription());
	 * log.debug(tab+"Name" + factory.getName());
	 * 
	 * convertor.xsd.factoryV2.Areas areas = (convertor.xsd.factoryV2.Areas)
	 * factory .getAreas();
	 * 
	 * log.debug(tab+"Areas");
	 * 
	 * 
	 * nTab++; tab=getTabs(nTab); int i = 1; for (convertor.xsd.factoryV2.Area
	 * area : areas .getArea()) {
	 * 
	 * log.debug(tab+"-Area " + i + ":" + area.getName());
	 * log.debug(tab+"-Description:" + area.getDescription());
	 * 
	 * showCells(area.getCells()); } nTab--; tab=getTabs(nTab); }
	 * 
	 * public static void showCells( convertor.xsd.factoryV2.Cells cells) { if
	 * (cells == null) { return; }
	 * 
	 * log.debug(tab+"Cells");
	 * 
	 * nTab++; tab=getTabs(nTab);
	 * 
	 * 
	 * int i = 1; for (convertor.xsd.factoryV2.Cell cell : cells .getCell()) {
	 * 
	 * log.debug(tab+"-Cell " + i + ":" + cell.getName());
	 * log.debug(tab+"-Description:" + cell.getDescription());
	 * 
	 * showMachines(cell.getMachines()); i++; } nTab--; tab=getTabs(nTab); }
	 * 
	 * public static void showMachines( convertor.xsd.factoryV2.Machines
	 * machines) {
	 * 
	 * if (machines == null) { return; }
	 * 
	 * 
	 * 
	 * log.debug(tab+"Machines");
	 * 
	 * 
	 * nTab++; tab=getTabs(nTab); int i = 1; for
	 * (convertor.xsd.factoryV2.Machine machine : machines .getMachine()) {
	 * 
	 * log.debug(tab+"-Machine " + i + ":" + machine.getName());
	 * log.debug(tab+"-Description:" + machine.getDescription());
	 * log.debug(tab+"-MachineType: " + machine.getType().value()); //
	 * log.debug("dd:"+machine.getOwnControlSystem()); //
	 * log.debug("HasOwnSystem:" + // machine.getOwnControlSystem().value());
	 * 
	 * showEquipment(machine.getEquipment()); i++; }
	 * 
	 * nTab--; tab=getTabs(nTab); }
	 * 
	 * public static void showEquipment( convertor.xsd.factoryV2.Equipment
	 * equipment) {
	 * 
	 * if (equipment == null) { return; }
	 * 
	 * 
	 * log.debug(tab+"Equipment"); nTab++; tab=getTabs(nTab);
	 * 
	 * 
	 * int i = 1; for (convertor.xsd.factoryV2.EquipmentEntity equipmententity :
	 * equipment .getEquipmentEntity()) {
	 * 
	 * log.debug(tab+"-EquipmentEntity " + i + ":" + equipmententity.getName());
	 * log.debug(tab+"-Type:" + equipmententity.getType().value());
	 * log.debug(tab+"-Description:" + equipmententity.getDescription());
	 * 
	 * 
	 * showStates(equipmententity.getStates());
	 * showElements(equipmententity.getElements());
	 * showEquipment(equipmententity.getEquipment());
	 * 
	 * i++; }
	 * 
	 * nTab--; tab=getTabs(nTab); }
	 * 
	 * public static void showElements( convertor.xsd.factoryV2.Elements
	 * elements) { if (elements == null) { return; }
	 * 
	 * 
	 * log.debug(tab+"Elements"); nTab++; tab=getTabs(nTab); int i = 1; for
	 * (convertor.xsd.factoryV2.Element element : elements .getElement()) {
	 * 
	 * log.debug(tab+"-Element " + i + ":" + element.getName());
	 * log.debug(tab+"-Description " + i + ":" + element.getDescription()); i++; }
	 * nTab--; tab=getTabs(nTab); }
	 * 
	 * public static void showStates( convertor.xsd.factoryV2.States states) {
	 * if (states == null) { return; }
	 * 
	 * 
	 * 
	 * log.debug(tab+"States");
	 * 
	 * nTab++; tab=getTabs(nTab); int i = 1; for (convertor.xsd.factoryV2.State
	 * state : states.getState()) {
	 * 
	 * log.debug(tab+"-State " + i + ":" + state); i++; } nTab--;
	 * tab=getTabs(nTab); }
	 * 
	 * private static String getTabs(int tabs){
	 * 
	 * StringBuffer tab=new StringBuffer(" ");
	 * 
	 * for (int i=1 ; i<tabs ; i++){
	 * 
	 * tab=tab.append(" "); }
	 * 
	 * return tab.toString(); }
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String path = "./";
		String configFileName = "config.xml";

		Constant.initialize(path, configFileName);

		Coil coil = new Coil();

		coil.setLocalId(10);
		coil.setHeight(20);
		coil.setWidth(20);
		coil.setNegated();
		coil.setPosition(new Position(3, 4));

		ConnectionPointIn in = new ConnectionPointIn();
		in.setRelPosition(new Position(4, 5));

		Position route1 = new Position(1, 3);
		Position route2 = new Position(3, 4);
		Connection con = new Connection();
		con.addPosition(route1);
		con.addPosition(route2);
		con.setRefLocalId(1999);
		in.addToConnections(con);
		coil.addToConnectionPointInList(in);

		ConnectionPointOut out = new ConnectionPointOut();

		out.setFormalParameter("haha");
		out.setRelPosition(new Position(3, 33));

		coil.addToConnectionPointOutList(out);

		coil.setVaraible("19233");

		LeftPowerRail leftp = new LeftPowerRail(2008, 4);
		leftp.setPosition(4, 5);

		ConnectionPointOut conOut1 = new ConnectionPointOut(
				new Position(12, 43));
		ConnectionPointOut conOut2 = new ConnectionPointOut(
				new Position(22, 33));
		ConnectionPointOut conOut3 = new ConnectionPointOut(
				new Position(32, 23));
		ConnectionPointOut conOut4 = new ConnectionPointOut(
				new Position(42, 13));

		// leftp.addToConnectionPointOutList(conOut1);
		// leftp.addToConnectionPointOutList(conOut2);
		// leftp.addToConnectionPointOutList(conOut3);
		// leftp.addToConnectionPointOutList(conOut4);

		Connection con1 = new Connection();
		con1.setRefLocalId(33);

		List<Position> positionList = new LinkedList<Position>();

		Position p1 = new Position(33, 477);
		Position p2 = new Position(323, 7);
		positionList.add(p1);
		positionList.add(p2);

		con1.setPositionList(positionList);
		Connection con2 = new Connection();
		con2.setRefLocalId(1997);

		List<Position> positionList2 = new LinkedList<Position>();

		Position p12 = new Position(33, 477);
		Position p22 = new Position(323, 7);
		positionList2.add(p12);
		positionList2.add(p22);

		con2.setPositionList(positionList2);

		RightPowerRail rightp = new RightPowerRail(33, 4);
		rightp.setPosition(4, 5);

		List<Connection> conList = new LinkedList<Connection>();
		conList.add(con1);
		conList.add(con2);
		ConnectionPointIn conIn1 = new ConnectionPointIn(p1, conList);

		coil.addToConnectionPointInList(conIn1);

		rightp.addToConnectionPointInList(conIn1);

		Contact contact = new Contact(222);

		contact.setPosition(p2);
		contact.setConnectionPointIn(conIn1);

		// try {
		// coil2 = coil.getPLCOpenObject();
		//
		// log.info("Coil id:" + coil2.getLocalId().toString());
		// } catch (Exception e) {
		//
		// e.printStackTrace();
		// }

		org.plcopen.xml.tc6.Project plcopenProject;
		// A reference of the project object,
		// plcopenProject = plcOpenObjectFactory.createProject();

		// Create the PLCopen xml schema object tree.
		JAXButil JC = JAXButil.getInstance(Constant.PLC_OPEN_TC6);

		plcopenProject = (org.plcopen.xml.tc6.Project) JC.getRootElementObject(
				Constant.XML_FILE_PATH, Constant.PLCOPEN_IN_PUT_FILE);

		org.plcopen.xml.tc6.ObjectFactory objf = new org.plcopen.xml.tc6.ObjectFactory();

		org.plcopen.xml.tc6.Body body = objf.createBody();

		org.plcopen.xml.tc6.Body.LD ld = objf.createBodyLD();
		try {
			ld.getCommentOrErrorOrConnector().add(coil.getPLCOpenObject());
			ld.getCommentOrErrorOrConnector().add(contact.getPLCOpenObject());
			ld.getCommentOrErrorOrConnector().add(leftp.getPLCOpenObject());
			ld.getCommentOrErrorOrConnector().add(rightp.getPLCOpenObject());

		} catch (Exception e) {

			e.printStackTrace();
		}
		body.setLD(ld);

		Project.Types.Pous.Pou pou = objf.createProjectTypesPousPou();
		pou.setBody(body);

		plcopenProject.getTypes().getPous().getPou().add(pou);

		Method[] methods = org.supremica.manufacturingtables.xsd.interlock.ActuatorValue.class
				.getDeclaredMethods();

		for (int i = 0; i < methods.length; i++) {

			System.out.println("method " + i + " " + methods[i].getName());

		}

		JC.exportToXMLFile(Constant.PLCOPEN_OUT_PUT_FILE_PATH,
				Constant.PLCOPEN_OUT_PUT_FILE);

	}
}
