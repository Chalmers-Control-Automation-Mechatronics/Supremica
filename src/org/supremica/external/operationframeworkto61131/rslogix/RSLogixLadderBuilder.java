package org.supremica.external.operationframeworkto61131.rslogix;
/**
 * @author LC
 *
 */
import java.util.LinkedList;
import java.util.List;

import org.plcopen.xml.tc6.Project.Types.Pous.Pou;
import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.FileUtil;
import org.supremica.external.operationframeworkto61131.util.JAXButil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;


public class RSLogixLadderBuilder {

	private org.plcopen.xml.tc6.Project plcopenProject;
	private String headerFile = ".\\RSLogix5000\\header.txt";
	private String footerFile = ".\\RSLogix5000\\footer.txt";


//	The location of input PLCopen xml file
	private String manualInputFile = ".\\RSLogix5000\\ladderTest.xml";
	
//	The location of output RSLogix 5000 L5K text file
	private String outputFile = ".\\RSLogix5000\\test.L5K";
	
	
	
//
//	private String manualInputFile = "C:\\Documents and Settings\\HAHA\\Desktop\\ladderTest2.xml";
//	private String outputFile = "C:\\Documents and Settings\\HAHA\\Desktop\\test.L5K";
	// private String manualInputFile =
	// "C:\\Project100\\MyDocuments\\Desktop\\ladderTest2.xml";
	// private String outputFile =
	// "C:\\Project100\\MyDocuments\\Desktop\\test.L5K";

	private static LogUtil log = LogUtil.getInstance();

	private final static String ILPrefix = "IL";

	FileUtil file = new FileUtil(outputFile);

	public void toRSLogix() {
		String path = "./";
		String configFileName = "config.xml";
		Constant.initialize(path, configFileName);

		org.plcopen.xml.tc6.Project plcopenProject = null;

		// Create the PLCopen xml schema object tree.
		JAXButil JC = JAXButil.getInstance(Constant.PLC_OPEN_TC6);

		if (this.manualInputFile != null && !this.manualInputFile.isEmpty()) {

			plcopenProject = (org.plcopen.xml.tc6.Project) JC
					.getRootElementObject(manualInputFile);
		}

		List<Pou> ILPouList = getILPouList(plcopenProject);

		StringBuffer buf = new StringBuffer();

		HeaderNFooter headerNFooter = new HeaderNFooter(headerFile, footerFile);

		buf.append(headerNFooter.getHeader());

		Program program = new Program(ILPouList);

		program.setName("TestProgram");
		program.setMain(ILPouList.get(0).getName());
		buf.append(program.getText(1));

		Task task = new Task();
		task.setName("Maintask_test");
		task.addProgram(program);

		buf.append(task.getText(1));

		buf.append(headerNFooter.getFooter());

		file.writeFile(buf.toString(), false);

	}

	public void toRSLogix2() {
		String path = "./";
		String configFileName = "config.xml";
		Constant.initialize(path, configFileName);

		org.plcopen.xml.tc6.Project plcopenProject = null;

		// Create the PLCopen xml schema object tree.
		JAXButil JC = JAXButil.getInstance(Constant.PLC_OPEN_TC6);

		if (this.manualInputFile != null && !this.manualInputFile.isEmpty()) {

			plcopenProject = (org.plcopen.xml.tc6.Project) JC
					.getRootElementObject(manualInputFile);
		}

		List<Pou> ILPouList = getILPouList(plcopenProject);

		StringBuffer buf = new StringBuffer();
		Program program = new Program(ILPouList);
		program.setName("TestProgram");
		program.setMain(ILPouList.get(0).getName());
		buf.append(program.getText_test(1));

		log.info(buf.toString());

	}

	private List<Pou> getILPouList(org.plcopen.xml.tc6.Project plcopenProject) {

		List<Pou> ILPouList = new LinkedList<Pou>();

		String pouName = "IL_Test";
		if (plcopenProject != null) {
			for (Pou iPou : plcopenProject.getTypes().getPous().getPou()) {

				// if (iPou.getName().startsWith(ILPrefix)) {

				if (iPou.getName().equals(pouName)) {

					ILPouList.add(iPou);

				}
			}
		}

		return ILPouList;

	}

	public static void main(String args[]) {

		RSLogixLadderBuilder logixLadderBuilder = new RSLogixLadderBuilder();

		logixLadderBuilder.toRSLogix();
	}

}
