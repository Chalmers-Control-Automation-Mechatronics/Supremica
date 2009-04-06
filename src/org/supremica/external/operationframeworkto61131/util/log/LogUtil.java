package org.supremica.external.operationframeworkto61131.util.log;
/**
 * @author LC
 *
 */
public class LogUtil {

	public static final String DEBUG = "Debug";
	public static final String INFO = "Info";
	public static final String ERROR = "Error";

	public static final String LOG_LEVEL = INFO;

	private static Logger logger = null;

	private LogUtil() {
	}

	public static LogUtil getInstance() {

		if (LOG_LEVEL == DEBUG) {

			return new DebugLogger();
		} else if (LOG_LEVEL == INFO) {

			return new InfoLogger();
		} else if (LOG_LEVEL == ERROR) {

			return new ErrorLogger();
		}

		System.out.println("Can not match log level:" + LOG_LEVEL);
		return null;
	}

	public void debug(String debug) {
	}

	public void info(String info) {
	}

	public void error(String error) {
	}

	private static void log(String logType, String log) {

		String message = logType + ": " + log;
		if (logger != null) {
			logger.log(message);

		} else {

			System.out.println(message);
		}
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		LogUtil.logger = logger;
	}

	public static class DebugLogger extends LogUtil {

		@Override
		public void debug(String debug) {
			log(DEBUG, debug);
		}

		@Override
		public void info(String info) {

			log(INFO, info);
		}

		@Override
		public void error(String error) {

			log(ERROR, error);

		}
	}

	public static class InfoLogger extends LogUtil {

		@Override
		public void info(String info) {

			log(INFO, info);
		}

		@Override
		public void error(String error) {

			log(ERROR, error);
		}

	}

	public static class ErrorLogger extends LogUtil {

		@Override
		public void error(String error) {
			log(ERROR, error);
		}

	}

}
