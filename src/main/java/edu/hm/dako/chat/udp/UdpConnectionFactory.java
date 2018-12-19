package edu.hm.dako.chat.udp;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ConnectionFactory;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Erzeugen von UDP-Verbindungen zum Server
 * 
 * @author Ganna Minakova
 *
 */
public class UdpConnectionFactory implements ConnectionFactory {
	private static Log log = LogFactory.getLog(UdpConnectionFactory.class);

	// Maximale Anzahl an Verbindungsaufbauversuchen zum Server, die ein Client
	// unternimmt, bevor er abbricht
	private static final int MAX_CONNECTION_ATTEMPTS = 50;

	// Zaehlt die Verbindungsaufbauversuche, bis eine Verbindung vom Server
	// angenommen wird
	private long connectionTryCounter = 0;

	/**
	 * Baut eine Verbindung zum Server auf. Der Verbindungsaufbau wird mehrmals
	 * versucht.
	 */
	public Connection connectToServer(String remoteServerAddress, int serverPort,
			int localPort, int sendBufferSize, int receiveBufferSize) throws IOException {

		UdpConnection connection = null;
		boolean connected = false;

		int attempts = 0;
		while ((!connected) && (attempts < MAX_CONNECTION_ATTEMPTS)) {
			try {

				connectionTryCounter++;

				connection = new UdpConnection(
						new DatagramSocket(),
						sendBufferSize, receiveBufferSize, serverPort, remoteServerAddress);
				connected = true;

			} catch (BindException e) {

				// Lokaler Port schon verwendet
				log.error("BindException beim Verbindungsaufbau: " + e.getMessage());

			} catch (IOException e) {

				log.error("IOException beim Verbindungsaufbau: " + e.getMessage());

				// Ein wenig warten und erneut versuchen
				attempts++;
				try {
					Thread.sleep(100);
				} catch (Exception e2) {
				}

			} catch (Exception e) {
				log.error("Sonstige Exception beim Verbindungsaufbau " + e.getMessage());
			}
			if (attempts >= MAX_CONNECTION_ATTEMPTS) {
				throw new IOException();
			}
		}

		log.debug("Anzahl der Verbindungsaufbauversuche fuer die Verbindung zum Server: "
				+ connectionTryCounter);
		return connection;
	}
}
