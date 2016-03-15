package simpleHttpServer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.Properties;

public class RunServer {
	private static final String REFRESH_CACHE = "refreshCache";
	private final static String EXIT = "exit";

	public static void main(String[] args) {
		try {
			Properties property = new Properties();
			property.load(new FileInputStream(System.getProperty("user.dir") + "/src/resources/config.properties"));
			HttpServer httpServer = new HttpServer(
					new InetSocketAddress(property.getProperty("server.host"),
							Integer.valueOf(property.getProperty("server.port"))),
					Paths.get(property.getProperty("server.root")),
					Boolean.valueOf(property.getProperty("server.isCached")));
			Thread thread = new Thread(httpServer);
			thread.start();
			System.out.println("Server is runnig. Available commands: " + REFRESH_CACHE + " and " + EXIT);

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			String command;
			while (!(command = br.readLine()).equals(EXIT)) {
				if (command.equals(REFRESH_CACHE)) {
					httpServer.refreshCache();
				} else {
					System.out.println("Unknown command. Known commands: " + REFRESH_CACHE + " and " + EXIT);
				}
			}
			httpServer.exit();
			thread.interrupt();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
