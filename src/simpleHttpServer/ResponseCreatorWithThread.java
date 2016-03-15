package simpleHttpServer;

public class ResponseCreatorWithThread {
	private Thread thread;
	private ResponseCreator responseCreator;

	public ResponseCreatorWithThread(Thread thread, ResponseCreator responseCreator) {
		this.thread = thread;
		this.responseCreator = responseCreator;
	}

	public Thread getThread() {
		return thread;
	}

	public ResponseCreator getResponseCreator() {
		return responseCreator;
	}
}
