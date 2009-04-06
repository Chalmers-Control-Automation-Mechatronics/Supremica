package org.supremica.external.operationframeworkto61131.util;
/**
 * @author LC
 *
 */
import java.lang.reflect.Method;

public class ReflectionUtil {

	public Object invokeMethod(Object owner, String methodName, Object[] args)
			throws Exception {

		Class[] argsClass = new Class[args.length];
		for (int i = 0, j = args.length; i < j; i++) {
			argsClass[i] = args[i].getClass();
		}

		Method method = owner.getClass().getMethod(methodName, argsClass);
		return method.invoke(owner, args);
	}

	// public Object invokeMethod(Object owner, String methodName, Object[]
	// args) {
	// Class[] argsClass = new Class[args.length];
	// try {
	//
	//			
	// for (int i = 0, j = args.length; i < j; i++) {
	// argsClass[i] = args[i].getClass();
	// }
	//
	// Method method = owner.getClass().getMethod(methodName, argsClass);
	// return method.invoke(owner, args);
	// } catch (Exception e) {
	// System.out.println("methodName:" + methodName);
	// System.out.println("methodName:" + argsClass[0]);
	// e.printStackTrace();
	// return null;
	// }
	// }

	public Object invokeMethod(Object owner, String methodName, Object arg)
			throws Exception {

		Object[] args = new Object[1];
		args[0] = arg;

		return invokeMethod(owner, methodName, args);

	}

	public Object invokeMethod(Object owner, String methodName)
			throws Exception {

		return invokeMethod(owner, methodName, new Object[0]);

	}

	public Method getMethod(Object owner, String methodName, Object[] args)
			throws Exception {
		//
		// Class[] argsClass = new Class[args.length];
		// for (int i = 0, j = args.length; i < j; i++) {
		// argsClass[i] = args[i].getClass();
		// }

		Method[] methods = owner.getClass().getMethods();

		for (int i = 0; i < methods.length; i++) {

			if (methods[i].getName().equals(methodName)) {
				return methods[i];
			}
		}

		return null;

	}

	public Boolean hasMethod(Object owner, String methodName, Object[] args)
			throws Exception {

		Method met = getMethod(owner, methodName, args);

		if (met != null) {
			return true;
		} else {
			return false;
		}

	}

	public Boolean hasMethod(Object owner, String methodName) throws Exception {

		return hasMethod(owner, methodName, new Object[0]);
	}

	public void printMethods(Object owner) {

		Method[] methods = owner.getClass().getMethods();

		for (int i = 0; i < methods.length; i++) {

			System.out.println("method " + i + " " + methods[i].getName());

		}

	}
}
