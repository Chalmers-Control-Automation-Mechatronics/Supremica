package org.supremica.external.operationframeworkto61131.util;
/**
 * @author LC
 *
 */
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.math.*;


import org.supremica.external.operationframeworkto61131.main.Constant;
import org.supremica.external.operationframeworkto61131.util.ReflectionUtil;



public class PLCopenXMLParser {

	private final static String getLocalId = "getLocalId";

	private final static String getRefLocalId = "getRefLocalId";

	private List<Object> pouElements;

	private org.supremica.external.operationframeworkto61131.util.log.LogUtil log = org.supremica.external.operationframeworkto61131.util.log.LogUtil
			.getInstance();

	// org.plcopen.xml.tc6.ObjectFactory plcopenObjectFactory = new
	// org.plcopen.xml.tc6.ObjectFactory();

	public PLCopenXMLParser(List<Object> pouElements) {

		this.pouElements = pouElements;
	}

	/*
	 * return a list of objects which are connected with the target obj. By
	 * "connected with", it means one obj in the pous has the refLocalId that
	 * equals one of the target obj's localId.
	 */
	public void getSubElementByLocalId(ArrayList<Object> subElementList,
			BigInteger localId, String formalParameter) {

		Object targetObj = this.getObjByLocalId(localId);

		try {

			for (Object obj : pouElements) {

				if (obj.equals(targetObj)) {

					continue;
				}

				ArrayList<Object> tempSubElementList = new ArrayList<Object>();
				ReflectionUtil reflectionUtil = new ReflectionUtil();

				// Get the object list that is connected to the obj
				if (reflectionUtil.hasMethod(obj, getLocalId)) {
					BigInteger tempLocalId = (BigInteger) reflectionUtil
							.invokeMethod(obj, getLocalId);

					this.getSubElementByRefLocalId(tempSubElementList,
							tempLocalId);

				}

				// Check if the object list contains the target object
				// The current obj is refering targetObj directly or indirectly
				if (tempSubElementList.contains(targetObj)
						&& !subElementList.contains(obj)) {

					// If the target object is in the object list that is
					// connected to the obj, the obj and the target object is
					// connected directly or indirectly. For direct connection ,
					// the formalParameter need to be checked to pick up system
					// connection(The only difference between user connection
					// and system
					// connection is formalParameter, user parameters are also
					// connected to
					// block, but they should be left in the pou, only varaibles
					// connected to system connection should be removed)
					if (this
							.checkFormalParameter(obj, localId, formalParameter)) {

						subElementList.add(obj);
					}

				}

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		/*
		 * When the refered target obj is a block, have to check if the founded
		 * obj has the right formalParameter This is only needed for direct
		 * connection
		 */

	}

	private Boolean checkFormalParameter(Object obj, BigInteger localId,
			String formalParameter) {

		try {

			if (getFieldInSubElement(obj, getRefLocalId, localId)) {

				if (formalParameter == null || formalParameter.isEmpty()) {
					return true;
				} else {

					if (getFieldInSubElement(obj, "getFormalParameter",
							formalParameter)) {

						return true;

					} else {

						return false;
					}
				}

			} else {

				return true;
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		return false;
	}

	/*
	 * return a list of objects which are connected to the target obj. By
	 * "connected to", it means the one obj in the pous has the id that equals
	 * the target obj's refLocalIds
	 */
	/**
	 * 
	 * @param subElementList
	 *            the object list to store the result
	 * @param refLocalId
	 *            the target object's localId/ For elements in the result list,
	 *            it's refLocalId
	 */
	public void getSubElementByRefLocalId(ArrayList<Object> subElementList,
			BigInteger refLocalId) {

		Object refObj = this.getObjByLocalId(refLocalId);

		if (refObj != null) {

			// add the refObj to subElementList
			// ignore the duplicate obj
			if (!subElementList.contains(refObj)) {
				subElementList.add(refObj);
			}

			// continue search in refObj's sub element.
			Object[] args = new Object[1];
			args[0] = subElementList;
			searchInSubElement(refObj, getRefLocalId,
					"getSubElementByRefLocalId", args, this);
		} else {

			log.error("Can not find obj localId=" + refLocalId);
		}

	}

	public List<Object> getReferingElements(BigInteger refLocalId) {

		List<Object> ReferingElementList = new LinkedList<Object>();

		Object refObj = this.getObjByLocalId(refLocalId);
		ReflectionUtil reflectionUtil = new ReflectionUtil();
		ArrayList<Object> tempList = new ArrayList<Object>();
		try {

			if (refObj != null) {

				for (Object obj : pouElements) {

					BigInteger iRefLocalId = BigInteger.ZERO;

					// reflectionUtil.printMethods(tempList);

					searchInSubElement(obj, getRefLocalId, "add",
							new Object[0], tempList);

					//
					// log
					// .info("Found refLocalId:"
					// + tempList.get(0));

					// if (iRefLocalId.equals(refLocalId)) {
					//
					// ReferingElementList.add(refObj);
					// }

				}

			} else {

				log.error("Can not find obj localId=" + refLocalId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ReferingElementList;
	}

	/**
	 * @param obj
	 *            the object to search in
	 * @param targetMethod
	 *            the target method to look for
	 * @param methodToInvoke
	 *            the method to invoke when the target method is found
	 * @param args
	 *            the args array to the method to invoke
	 * @param invoker
	 *            the object that contains the method to invoke
	 */
	public void searchInSubElement(Object obj, String targetMethod,
			String methodToInvoke, Object[] args, Object invoker) {

		// FIXME probably need the getMethods() to get Methods from super class.
		Method[] methods = obj.getClass().getDeclaredMethods();

		ReflectionUtil reflectionUtil = new ReflectionUtil();
		try {

			// if the obj A has refLocalId, call getSubElementByRefLocalId to
			// add the sub element of A to the subElementList
			if (reflectionUtil.hasMethod(obj, targetMethod)) {

				Object result = reflectionUtil.invokeMethod(obj, targetMethod);
				// log.info("got getRefLocalId:" + refLocalId);
				// getSubElementByRefLocalId(refLocalId, subElementList);

				// Copy the input args array to a new array of
				// length=args.length+1. Add the result to extra position at
				// the end
				Object[] argsAndResult = new Object[args.length + 1];

				for (int i = 0; i < args.length; i++) {

					argsAndResult[i] = args[i];

				}

				argsAndResult[args.length] = result;

				// Invoke the input method with the new args array
				reflectionUtil.invokeMethod(invoker, methodToInvoke,
						argsAndResult);

			} else {

				// if the obj does not have refLocalId, get all the sub element
				// of the input obj by invoking all
				// getter to check if the obj's subElements have refLocalId
				for (int j = 0; j < methods.length; j++) {

					Class returnType = methods[j].getReturnType();

					if (methods[j].getName().contains("get")) {

						// To accelerate the seach speed.
						// FIXME the key word "plcopen" to match in the return
						// type is hard coded, will malfunction if the package
						// name is changed. To avoid this bug, remove the return
						// type judging if-else. Then the searching speed will
						// be slowed down.
						if (returnType.getPackage().getName().contains(
								"plcopen")
								|| returnType.equals(List.class)) {

							// FIXME returnType!= ArrayList? LinkedList? It's
							// java.util.List

							Class[] paramTypes = methods[j].getParameterTypes();
							// invoke only getter() with no parameter
							if (paramTypes.length == 0) {
								//
								// log.info("Invoke method:"
								// + methods[j].getName());

								Object subElement = methods[j].invoke(obj,
										new Object[0]);

								if (subElement == null) {
									continue;
								}

								// log
								// .info("Found obj:"
								// + subElement.getClass()
								// .getSimpleName());

								if (subElement.getClass().equals(
										java.util.ArrayList.class)) {

									// log.info("getLinkedList");

									ArrayList subElementArrayList = (ArrayList) subElement;
									int subElementSubListSize = ((Integer) reflectionUtil
											.invokeMethod(subElement, "size"))
											.intValue();

									for (int i = 0; i < subElementSubListSize; i++) {

										// Object subSubElement = reflectionUtil
										// .invokeMethod(subElement, "get",i);
										Object subSubElement = subElementArrayList
												.get(i);

										searchInSubElement(subSubElement,
												targetMethod, methodToInvoke,
												args, invoker);

									}

								} else {

									searchInSubElement(subElement,
											targetMethod, methodToInvoke, args,
											invoker);
								}

							}
						}
					}
				}

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public Boolean getFieldInSubElement(Object obj, String targetMethod,
			Object targetValue) {

		// FIXME probably need the getMethods() to get Methods from super class.
		Method[] methods = obj.getClass().getDeclaredMethods();

		ReflectionUtil reflectionUtil = new ReflectionUtil();

		try {

			if (reflectionUtil.hasMethod(obj, targetMethod)) {
				log.debug("Obj type:" + obj.getClass().getSimpleName());

				Object value = reflectionUtil.invokeMethod(obj, targetMethod);
				// log.info("got getRefLocalId:" +
				// refLocalId);
				// getSubElementByRefLocalId(refLocalId,
				// subElementList);

				if (value != null) {

					log.debug("Target:" + targetValue.toString());
					log.debug("value:" + value.toString());
					if (targetValue.equals(value)) {

						return true;
					}
				}

			}

			// if the obj does not have refLocalId, get all the sub element
			// of the input obj by invoking all
			// getter to check if the obj's subElements have refLocalId
			for (int j = 0; j < methods.length; j++) {

				Class returnType = methods[j].getReturnType();

				if (methods[j].getName().contains("get")) {

					// FIXME could cause problem if package name changes

					if (returnType.getPackage().getName().contains("plcopen")
							|| returnType.equals(List.class)) {

						Class[] paramTypes = methods[j].getParameterTypes();
						// invoke only getter() with no parameter
						if (paramTypes.length == 0) {

							Object subElement = methods[j].invoke(obj,
									new Object[0]);

							if (subElement == null) {
								continue;
							}

							if (subElement.getClass().equals(
									java.util.ArrayList.class)) {

								ArrayList subElementArrayList = (ArrayList) subElement;
								int subElementSubListSize = ((Integer) reflectionUtil
										.invokeMethod(subElement, "size"))
										.intValue();

								for (int i = 0; i < subElementSubListSize; i++) {

									Object subSubElement = subElementArrayList
											.get(i);

									if (getFieldInSubElement(subSubElement,
											targetMethod, targetValue)) {

										return true;
									} else {
										continue;
									}

								}

							} else {

								if (getFieldInSubElement(subElement,
										targetMethod, targetValue)) {

									return true;
								} else {
									continue;
								}

							}

						}
					}
				}

			}
		} catch (Exception e) {

			e.printStackTrace();

		}

		return false;
	}

	public Object getObjByLocalId(BigInteger refLocalId) {

		return this.getObjByField(this.getLocalId, refLocalId);

	}

	public Object getObjByInstanceName(String instanceName) {

		return this.getObjByField("getInstanceName", instanceName);

	}

	public Object getObjByField(String method, Object value) {
		ReflectionUtil reflectionUtil = new ReflectionUtil();
		try {

			for (Object obj : pouElements) {

				// Check if the object has the method to get localId
				if (reflectionUtil.hasMethod(obj, method)) {

					Object field = reflectionUtil.invokeMethod(obj, method);

					if (field != null && field.equals(value)) {

						return obj;

					}

				}

			}

			return null;

		} catch (Exception e) {

			e.printStackTrace();
			return null;

		}

	}

	public void removeElements(List<Object> toRemove) {

		for (Object obj : toRemove) {

			this.pouElements.remove(obj);
		}

	}

	public void removeElements(Object toRemove) {

		this.pouElements.remove(toRemove);

	}

	// public void getDirectNeighbourElements

	public static void main(String[] args) {

		String path = "./";
		String configFileName = "config.xml";
		Constant.initialize(path, configFileName);

		org.supremica.external.operationframeworkto61131.util.log.LogUtil log = org.supremica.external.operationframeworkto61131.util.log.LogUtil
				.getInstance();

		String inputFileName = "fid_input001.xml";

		JAXButil JC = JAXButil.getInstance(Constant.PLC_OPEN_TC6);

		org.plcopen.xml.tc6.Project plcopenProject = (org.plcopen.xml.tc6.Project) JC
				.getRootElementObject(Constant.PLCOPEN_OUT_PUT_FILE_PATH,
						inputFileName);

		org.supremica.manufacturingtables.xsd.fid.FunctionBlocks functionBlocks = (org.supremica.manufacturingtables.xsd.fid.FunctionBlocks) JAXButil
				.getInstance(Constant.FID_XML_CONTEXT).getRootElementObject(
						Constant.XML_FILE_PATH, Constant.FID_XML_FILE_NAME);

		// String pouName = "FB_C1F1C1_Two_Position_Movement";
		// String pouName = "IL_IL_C1_41";
		// String pouName = "IL_IL_F1C1_WORK_POS";
		String pouName = "FB_C1F1C1_Two_Position_Movement";
		String fbType = "Two Position Movement";

		// String formalParameter = "output forward";
		int refLocalId = 1;

		String formalParameter = "forward started";
		org.supremica.manufacturingtables.xsd.fid.FunctionBlock fid = null;

		//		
		// for(convertor.xsd.fid.FunctionBlock
		// fb:functionBlocks.getFunctionBlock()){
		//			
		// if(fb.getType().equals(fbType)){
		//				
		//				
		// fid=fb;
		// }
		// }
		//		
		//		
		// convertor.Builder builder=new convertor.Builder();
		//		

		org.plcopen.xml.tc6.Project.Types.Pous.Pou po = null;
		for (org.plcopen.xml.tc6.Project.Types.Pous.Pou pou : plcopenProject
				.getTypes().getPous().getPou()) {

			if (pou.getName().equals(pouName)) {

				po = pou;

			}

		}

		PLCopenXMLParser parser = new PLCopenXMLParser(po.getBody().getFBD()
				.getCommentOrErrorOrConnector());

		// PLCopenXMLParser parser = new PLCopenXMLParser(po.getBody().getFBD()
		// .getCommentOrErrorOrConnector());
		// Method[] mm = parser.getClass().getDeclaredMethods();
		// for (int i = 0; i < mm.length; i++) {
		//
		// log.info(" method name:" + mm[i].getName());
		// Class[] tt = mm[i].getParameterTypes();
		//
		// for (int j = 0; j < tt.length; j++) {
		// log.info(" type:" + tt[j].toString());
		//
		// }
		// }

		// // method name:getSubElementByRefLocalId
		// Info:: type:interface java.util.List
		// Info:: type:class java.math.BigInteger
		//		

		// ArrayList<Object> subElementList = new ArrayList<Object>();
		// parser.getSubElementByRefLocalId(subElementList, BigInteger
		// .valueOf(refLocalId));

		ArrayList<Object> subElementListOut = new ArrayList<Object>();
		parser.getSubElementByLocalId(subElementListOut, BigInteger
				.valueOf(refLocalId), formalParameter);

		try {
			// for (Object obj : subElementList) {
			// ReflectionUtil reflectionUtil = new ReflectionUtil();
			// // log.info("TYpe:" + obj.getClass().getSimpleName());
			//
			// log.info("subElementList:"
			// + (BigInteger) reflectionUtil.invokeMethod(obj,
			// "getLocalId"));
			//
			// }

			for (Object obj : subElementListOut) {
				ReflectionUtil reflectionUtil = new ReflectionUtil();
				// log.info("TYpe:" + obj.getClass().getSimpleName());

				log.info("subElementListOut:"
						+ (BigInteger) reflectionUtil.invokeMethod(obj,
								"getLocalId"));

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		// List<String> aa=new LinkedList<String>();
		//		
		// aa.add("123");
		// aa.add("12e3");
		// aa.add("123ee");
		// String[] bb=new String[aa.size()];
		//		
		// aa.toArray(bb);
		//		
		//		
		// for(int i=0;i<bb.length;i++){
		//			
		// log.info("bb:"+i+","+bb[i]);
		//			
		// }

	}

}
