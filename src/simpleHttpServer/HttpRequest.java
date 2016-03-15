package simpleHttpServer;

import java.net.URI;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

	private SocketChannel socketChannel;
	private Selector selector;
	private String request;
	private String method;
	private URI uri;
	private Map<String, String> headers;
	private boolean isValid;

	public HttpRequest(SocketChannel socketChannel, Selector selector, String request, Path root) {
		this.socketChannel = socketChannel;
		this.selector = selector;
		this.request = request;
		isValid = true;
		parseRequest(request, root);
	}

	private void parseRequest(String request, Path root) {
		try {
			String[] lines = request.split("\r\n");
			String[] startLineElements = lines[0].split("[ ]+");
			method = startLineElements[0].toUpperCase();
			uri = URI.create(startLineElements[1]);
			headers = new HashMap<String, String>();
			for (int i = 2; i < lines.length; ++i) {
				int idx = lines[i].indexOf(':');
				if (idx < 0) {
					break;
				}
				headers.put(lines[i].substring(0, idx).toLowerCase(), lines[i].substring(idx + 1).trim());
			}
		} catch (Exception ex) {
			isValid = false;
		}
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public Selector getSelector() {
		return selector;
	}

	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	public String getMessage() {
		return request;
	}

	public void setMessage(String message) {
		this.request = message;
	}

	public boolean isValid() {
		return isValid;
	}
}
