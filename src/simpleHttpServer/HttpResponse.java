package simpleHttpServer;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
	final private String version = "HTTP/1.1";
	private int responseCode;
	private Map<String, String> headers = new LinkedHashMap<String, String>();
	private byte[] body;
	private String responseDescription;

	public void addGeneralHeaders() {
		headers.put("Date", new Date().toString());
		headers.put("Server", "Java NIO");
	//	headers.put("Connection", "close");
	}

	public void setBody(byte[] byteBuffer) {
		body = byteBuffer;
	}

	public byte[] getByteBufferResponseWithoutBody() {
		StringBuilder response = new StringBuilder();
		response.append(version + " " + responseCode + " " + responseDescription + "\r\n");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			response.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
		}
		response.append("\r\n");
		try {
			return Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(response.toString().toCharArray()))
					.array();
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public byte[] getByteBufferBody() {
		if (body == null)
			return new byte[0];
		return body;
	}

	public String getVersion() {
		return version;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode, String responseDescription) {
		this.responseCode = responseCode;
		this.responseDescription = responseDescription;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(String key, String value) {
		headers.put(key, value);
	}

}
