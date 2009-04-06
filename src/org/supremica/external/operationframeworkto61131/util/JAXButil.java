package org.supremica.external.operationframeworkto61131.util;
/**
 * @author LC
 *
 */
import java.io.File;
import java.io.FileOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;

import org.supremica.external.operationframeworkto61131.util.FileUtil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;



import java.util.LinkedList;
import java.util.List;


public class JAXButil {

	private JAXBContext jc;

	private String packageName;

	// private String inputXMLFileName;

	private LogUtil log = LogUtil.getInstance();

	private Object root;

	private JAXButil(String packageName) {

		this.packageName = packageName;

		try {
			jc = JAXBContext.newInstance(new String(this.packageName));
		
			log.info("JAXB context built from " + this.packageName);

		} catch (Exception e) {

			log.error("Invalid packagename for JAXB context:" + packageName
					+ ". Exception:" + e.getMessage());
		}

	}

	public static JAXButil getInstance(String packageName) {

		return new JAXButil(packageName);
	}

	public Object getRootElementObject(String inputXMLFileName) {

		if (StringUtil.isEmpty(inputXMLFileName)) {
			log.error("Null or empty xml file name!");
			return null;
		}

		try {
			File inputfile = new File(inputXMLFileName);

			return getRootElementObject(inputfile, inputXMLFileName);

		} catch (Exception e) {

			log.error("Failed to load file:" + inputXMLFileName
					+ ". Exception:" + e.getMessage());

			return null;
		}

	}

	public Object getRootElementObject(String path, String inputXMLFileName) {

		if (StringUtil.isEmpty(path)) {
			log.error("Null or empty xml file path!");
			return null;
		}

		if (StringUtil.isEmpty(inputXMLFileName)) {
			log.error("Null or empty xml file name!");
			return null;
		}

		try {
			File inputfile = new File(path, inputXMLFileName);

			return getRootElementObject(inputfile, path + inputXMLFileName);

		} catch (Exception e) {

			log.error("Failed to load file:" + path + inputXMLFileName
					+ ". Exception:" + e.getMessage());

			return null;
		}

	}

	private Object getRootElementObject(File inputfile, String fileNameInfo) {

		if (!FileUtil.isValid(inputfile, fileNameInfo)) {

			return null;
		}

		if (jc != null) {
			try {
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				root = unmarshaller.unmarshal(inputfile);

				log.debug("Loaded " + inputfile.getName());
				return root;
			} catch (Exception e) {

				log.error("failed to return root element object from : "
						+ fileNameInfo + ". Exception:" + e.getMessage());

				return null;
			}
		} else {

			log
					.error("empty context, failed to return root element object from : "
							+ fileNameInfo);
			return null;

		}
	}

	public Boolean exportToXMLFile(String path, String outputXMLFileName) {
		if (jc != null) {
			try {
				Marshaller marshaller = jc.createMarshaller();

				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
						new Boolean(true));

				marshaller.marshal(root, new FileOutputStream(new File(path,
						outputXMLFileName)));

				log.info("XML file is successfully exported to " + path
						+ outputXMLFileName);
				return true;

			} catch (Exception e) {

				log.error("Failed to export to " + path + outputXMLFileName
						+ ". Exception:" + e.getMessage());
				return false;
			}
		} else {
			log.error("Empty context, exporting to " + path + outputXMLFileName
					+ " failied");
		}
		return false;
	}

	public List<Object> loadXMLFromPath(String path, String nameStartsWith) {

		List<Object> rootList = new LinkedList<Object>();
		File file = new File(path);
		String[] fileNames = (file).list();

		// Load all the xml files from the 'path'
		if (StringUtil.isEmpty(nameStartsWith)) {

			log.info("Loading XML files from:" + file.getAbsolutePath());

			for (int i = 0; i < fileNames.length; i++) {
				Object root = getRootElementObject(path, fileNames[i]);
				if (root != null) {
					rootList.add(root);

				} else {
					log.error("Loading of file:" + fileNames[i] + " failed");
				}
			}

		} else {
			// Load xml files whose name starts with 'nameStartsWith' from the
			// 'path'

			nameStartsWith = nameStartsWith.toUpperCase();
			log.info("Loading XML files with prefix \"" + nameStartsWith + "\" from:"
					+ file.getAbsolutePath());

			for (int i = 0; i < fileNames.length; i++) {
				if (fileNames[i].toUpperCase().startsWith(nameStartsWith)) {

					Object root = getRootElementObject(path, fileNames[i]);
					if (root != null) {
						rootList.add(root);

					} else {
						log
								.error("Loading of file:" + fileNames[i]
										+ " failed");
					}
				}
			}

		}

		return rootList;
	}

	public List<Object> loadXMLFromPath(String path) {

		String nameStartsWith = "";
		return loadXMLFromPath(path, nameStartsWith);
	}

}
