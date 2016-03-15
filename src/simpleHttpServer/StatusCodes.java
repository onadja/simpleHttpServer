package simpleHttpServer;

import java.util.HashMap;
import java.util.Map;

public class StatusCodes {

	static final int OK = 200;
	static final int FILE_NOT_FOUND = 404;
	static final int METHOD_NOT_ALLOWED = 405;
	static final int BAD_REQUEST = 400;
	static final Map<Integer, String> statusCodes;

	static {
		statusCodes = new HashMap<Integer, String>();
		statusCodes.put(OK, "OK");
		statusCodes.put(FILE_NOT_FOUND, "file not found");
		statusCodes.put(METHOD_NOT_ALLOWED, "method not allowed");
		statusCodes.put(BAD_REQUEST, "bad request");
	}

}