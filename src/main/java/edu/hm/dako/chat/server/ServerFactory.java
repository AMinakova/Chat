package edu.hm.dako.chat.server;

import edu.hm.dako.chat.auditlog.AuditLogServerImpl;
import edu.hm.dako.chat.tcp.TcpConnectionFactory;
import edu.hm.dako.chat.udp.UdpConnectionFactory;
import edu.hm.dako.chat.udp.UdpServerSocket;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.common.ImplementationType;
import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.LoggingConnectionDecorator;
import edu.hm.dako.chat.connection.ServerSocketInterface;
import edu.hm.dako.chat.tcp.TcpServerSocket;

/**
 * Uebernimmt die Konfiguration und Erzeugung bestimmter Server-Typen. 
 * @author Peter Mandl
 */
public final class ServerFactory {
	private static Log log = LogFactory.getLog(ServerFactory.class);

	private ServerFactory() {
	}

	/**
	 * Erzeugt einen Chat-Server
	 * 
	 * @param implType
	 *          Implementierungytyp des Servers
	 * @param serverPort
	 *          Listenport
	 * @param sendBufferSize
	 *          Groesse des Sendepuffers in Byte
	 * @param receiveBufferSize
	 *          Groesse des Empfangspuffers in Byte
	 * @param serverGuiInterface
	 *          Referenz auf GUI fuer Callback
	 * @return
	 * @throws Exception
	 */
	public static ChatServerInterface getServer(ImplementationType implType, int serverPort,
			int sendBufferSize, int receiveBufferSize,
			ChatServerGuiInterface serverGuiInterface) throws Exception {
		log.debug("ChatServer (" + implType.toString() + ") wird gestartet, Serverport: "
				+ serverPort + ", Sendepuffer: " + sendBufferSize + ", Empfangspuffer: "
				+ receiveBufferSize);
		System.out.println("ChatServer (" + implType.toString()
				+ ") wird gestartet, Listen-Port: " + serverPort + ", Sendepuffer: "
				+ sendBufferSize + ", Empfangspuffer: " + receiveBufferSize);

		switch (implType) {

		  case TCPSimpleImplementation:

        try {
          TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
              receiveBufferSize);
          Connection connection = null;
          return new SimpleChatServerImpl(Executors.newCachedThreadPool(),
              getDecoratedServerSocket(tcpServerSocket), serverGuiInterface, connection);
        } catch (Exception e) {
          throw new Exception(e);
        }

        // Server als SimpleChatServer und als TCP-Client für AuditlogServer startet
			case TCPExtendedImplementation:

        try {
          TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
              receiveBufferSize);
          //TCPConnection zwischen Server und Auditlogserver erzeugt
          Connection connection = new TcpConnectionFactory()
              .connectToServer(null, 60000, 0, sendBufferSize, receiveBufferSize);
          return new SimpleChatServerImpl(Executors.newCachedThreadPool(),
              getDecoratedServerSocket(tcpServerSocket), serverGuiInterface, connection);
        } catch (Exception e) {
          throw new Exception(e);
        }

        // Server als SimpleChatServer und als UDP-Client für AuditlogServer startet
			case UDPExtendedImplementation:

				try {
					TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
							receiveBufferSize);
					//UDPConnection zwischen Server und Auditlogserver erzeugt
					Connection connection = new UdpConnectionFactory()
							.connectToServer(null, 60001, 0, sendBufferSize, receiveBufferSize);
					return new SimpleChatServerImpl(Executors.newCachedThreadPool(),
							getDecoratedServerSocket(tcpServerSocket), serverGuiInterface, connection);
				} catch (Exception e) {
					throw new Exception(e);
				}

				// AuditLogServer mit TCP-Verbindung startet
      case TCPAuditLogImplementation:

        try {
          TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
              receiveBufferSize);
          return new AuditLogServerImpl(tcpServerSocket, Executors.newCachedThreadPool(),
							(ChatServerGUI) serverGuiInterface);
        } catch (Exception e) {
          throw new Exception(e);
        }

        // AuditLogServer mit UDP-Verbindung startet
			case UDPAuditLogImplementation:

				try {
					UdpServerSocket udpServerSocket = new UdpServerSocket(serverPort, sendBufferSize,
							receiveBufferSize);
					return new AuditLogServerImpl(udpServerSocket, Executors.newCachedThreadPool(),
							(ChatServerGUI) serverGuiInterface);
				} catch (Exception e) {
					throw new Exception(e);
				}

		default:
			System.out.println("Dezeit nur TCP implementiert!");
			throw new RuntimeException("Unknown type: " + implType);
		}
	}

	private static ServerSocketInterface getDecoratedServerSocket(
			ServerSocketInterface serverSocket) {
		return new DecoratingServerSocket(serverSocket);
	}

	/**
	 * Dekoriert Server-Socket mit Logging-Funktionalitaet
	 * 
	 * @author mandl
	 *
	 */
	private static class DecoratingServerSocket implements ServerSocketInterface {

		private final ServerSocketInterface wrappedServerSocket;

		DecoratingServerSocket(ServerSocketInterface wrappedServerSocket) {
			this.wrappedServerSocket = wrappedServerSocket;
		}

		@Override
		public Connection accept() throws Exception {
			return new LoggingConnectionDecorator(wrappedServerSocket.accept());
		}

		@Override
		public void close() throws Exception {
			wrappedServerSocket.close();
		}

		@Override
		public boolean isClosed() {
			return wrappedServerSocket.isClosed();
		}
	}
}
