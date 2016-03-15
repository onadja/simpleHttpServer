package simpleHttpServer;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class ResponseCreator implements Runnable {
	private HttpServer server;
	private BlockingQueue<HttpRequest> queue;

	public BlockingQueue<HttpRequest> getQueue() {
		return queue;
	}

	private Thread thread;

	public Thread getThread() {
		return thread;
	}

	public ResponseCreator(BlockingQueue<HttpRequest> queue, HttpServer server) {
		this.queue = queue;
		this.server = server;
		thread = Thread.currentThread();
	}

	public void run() {
		while (true) {

			try {
				HttpRequest httpRequest = queue.take();
				server.replyToClient(createHttpResponse(httpRequest), httpRequest.getSelector(),
						httpRequest.getSocketChannel());
			} catch (InterruptedException e) {
				thread.interrupt();
				return;
			}
		}
	}

	private HttpResponse createHttpResponse(HttpRequest httpRequest) {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.addGeneralHeaders();
		if (!httpRequest.isValid()) {
			httpResponse.setResponseCode(StatusCodes.BAD_REQUEST, StatusCodes.statusCodes.get(StatusCodes.BAD_REQUEST));
			return httpResponse;
		}
		if (!httpRequest.getMethod().equals("GET")) {
			httpResponse.setResponseCode(StatusCodes.METHOD_NOT_ALLOWED,
					StatusCodes.statusCodes.get(StatusCodes.METHOD_NOT_ALLOWED));
			return httpResponse;
		}
		try {

			String etag = httpRequest.getHeaders().get("if-none-match");
			FileData fileData = null;
			if (!server.isCached() || etag == null) {
				fileData = new FileData(server.getRoot() + httpRequest.getUri().getPath());
			}
			if (etag != null) {
				fileData = server.getCache().get(etag);
			}

			if (fileData != null && fileData.fileIsFound()) {
				String charset = httpRequest.getHeaders().get("accept-charset");
				if ((charset == null
						&& Arrays.asList(FileType.Text.getExtensions()).contains(fileData.getFileExtension()))

						|| (charset != null && fileData.getFileExtension().equals(FileType.Text)
								&& !EncodeType.isSupported(charset))) {
					httpResponse.setResponseCode(StatusCodes.BAD_REQUEST,
							StatusCodes.statusCodes.get(StatusCodes.BAD_REQUEST));
					return httpResponse;
				}
				if (charset == null && (Arrays.asList(FileType.Image.getExtensions())
						.contains(fileData.getFileExtension())
						|| Arrays.asList(FileType.Application.getExtensions()).contains(fileData.getFileExtension()))) {
					httpResponse.setResponseCode(StatusCodes.OK, StatusCodes.statusCodes.get(StatusCodes.OK));
					httpResponse.setBody(fileData.getfileContentOther().array());
					httpResponse.setHeaders("Content-Type",
							FileType.getContentTypeByExtension(fileData.getFileExtension()).getName());
					if (etag == null && server.isCached()) {
						server.getCache().put(fileData.getEtag(), fileData);
						httpResponse.setHeaders("ETag", fileData.getEtag());
					}
					return httpResponse;
				}
				httpResponse.setResponseCode(StatusCodes.OK, StatusCodes.statusCodes.get(StatusCodes.OK));
				httpResponse.setBody(fileData.getFileContentWithCharset(charset).array());
				if (etag == null && server.isCached()) {
					server.getCache().put(fileData.getEtag(), fileData);
					httpResponse.setHeaders("ETag", fileData.getEtag());
				}
				return httpResponse;
			} else {
				httpResponse.setResponseCode(StatusCodes.FILE_NOT_FOUND,
						StatusCodes.statusCodes.get(StatusCodes.FILE_NOT_FOUND));
				return httpResponse;
			}

		} catch (Exception ex) {
			System.err.print(ex.toString());
			return null;
		}
	}

}
