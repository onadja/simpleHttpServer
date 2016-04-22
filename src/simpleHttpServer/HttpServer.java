package simpleHttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HttpServer implements Runnable {
	private static final long TIMEOUT = 200000000;
	private InetSocketAddress inetSocketAddress;
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	private Path root;
	private boolean isCached;
	private List<ResponseCreatorWithThread> responseCreatorsWithThread;
	private BlockingQueue<HttpRequest> queue;
	private Map<String, FileData> cache;

	private List<SocketChannel> socketChannels = new LinkedList<SocketChannel>();
	private Map<SocketChannel, List<ByteBuffer>> mapSocketToNeedWriteData = new HashMap<SocketChannel, List<ByteBuffer>>();
	private boolean isRunning;

	public boolean isCached() {
		return isCached;
	}

	public HttpServer(InetSocketAddress inetSocketAddress, Path root, boolean isCached) {
		this.inetSocketAddress = inetSocketAddress;
		this.root = root;
		cache = new HashMap<String, FileData>();
		this.isCached = isCached;
		this.queue = new LinkedBlockingQueue<HttpRequest>();
		isRunning = true;
		initializeChannel();
	}

	public Path getRoot() {
		return root;
	}

	public void refreshCache() {
		synchronized (cache) {
			cache.clear();
		}
		System.out.println("Cache is up-to-date");
	}

	private void initializeChannel() {
		try {
			selector = SelectorProvider.provider().openSelector();
			openChannel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		responseCreatorsWithThread = new ArrayList<ResponseCreatorWithThread>();
		ResponseCreator responseCreator = new ResponseCreator(queue, this);
		responseCreatorsWithThread.add(new ResponseCreatorWithThread(new Thread(responseCreator), responseCreator));
		responseCreator = new ResponseCreator(queue, this);
		responseCreatorsWithThread.add(new ResponseCreatorWithThread(new Thread(responseCreator), responseCreator));
		responseCreatorsWithThread.forEach(record -> record.getThread().start());

		while (isRunning) {
			try {
				synchronized (socketChannels) {
					for (SocketChannel socketChannel : socketChannels) {

						SelectionKey key = socketChannel.keyFor(selector);
						key.interestOps(SelectionKey.OP_WRITE);
					}
					socketChannels.clear();
				}

				selector.select(TIMEOUT);
				for (SelectionKey key : selector.selectedKeys()) {
					selector.selectedKeys().remove(key);

					if (!key.isValid()) {
						continue;
					}
					if (key.isAcceptable()) {
						accept(key);
					} else if (key.isReadable()) {
						read(key);
					} else if (key.isWritable()) {
						write(key);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		closeConnection();
	}

	private void write(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		List<ByteBuffer> queueToWrite = new ArrayList<ByteBuffer>();
		synchronized (mapSocketToNeedWriteData) {
			queueToWrite = mapSocketToNeedWriteData.get(socketChannel);
		}
		while (!queueToWrite.isEmpty()) {
			ByteBuffer buf = queueToWrite.get(0);
			int bytes = buf.limit();
			while (buf.hasRemaining() && bytes!=0) {
				try {
					bytes -= socketChannel.write(buf);;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			queueToWrite.remove(0);
		}

		if (queueToWrite.isEmpty()) {
			try {
				socketChannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				openChannel();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void read(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		readBuffer.clear();
		int numRead;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (IOException e) {
			closeSelectionKey(key);
			return;
		}

		if (numRead == -1) {
			closeSelectionKey(key);
			return;
		}
		String message = new String(readBuffer.array());
		try {
			queue.put(new HttpRequest(socketChannel, selector, message, root));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	void closeSelectionKey(SelectionKey key) {
		try {
			key.cancel();
			openChannel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void openChannel() throws IOException, ClosedChannelException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.socket().bind(inetSocketAddress);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	private void accept(SelectionKey key) {
		try (ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel()) {
			SocketChannel socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void replyToClient(HttpResponse httpResponse, Selector selector, SocketChannel socketChannel) {
		synchronized (socketChannels) {
			socketChannels.add(socketChannel);

			synchronized (mapSocketToNeedWriteData) {
				List<ByteBuffer> queue = mapSocketToNeedWriteData.get(socketChannel);
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					mapSocketToNeedWriteData.put(socketChannel, queue);
				}
				queue.add(ByteBuffer.wrap(httpResponse.getByteBufferResponseWithoutBody()));
				queue.add(ByteBuffer.wrap(httpResponse.getByteBufferBody()));
			}
		}

		selector.wakeup();

	}

	public InetSocketAddress getInetSocketAddress() {
		return inetSocketAddress;
	}

	public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
		this.inetSocketAddress = inetSocketAddress;
	}

	private void closeConnection() {
		System.out.println("Closing connection");
		if (selector != null) {
			try {
				selector.close();
				serverSocketChannel.socket().close();
				serverSocketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void exit() {
		responseCreatorsWithThread.forEach(responseCreator -> responseCreator.getThread().interrupt());
		System.out.println("Closing server");
		isRunning = false;
	}

	public Map<String, FileData> getCache() {
		return cache;
	}

}
