package edu.hm.dako.chat.server;

import edu.hm.dako.chat.auditlog.AuditLogServerImpl;
import edu.hm.dako.chat.connection.AuditConnectionDecorator;
import edu.hm.dako.chat.tcp.TcpConnectionFactory;
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
      case TCPExtendedImplementation:

        try {
          TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
              receiveBufferSize);
          //TODO später host adress to RemoteServerAdress
          Connection auditLogServerConnection = implType == ImplementationType.TCPExtendedImplementation
              ? new TcpConnectionFactory().connectToServer(null, 60000, 0, sendBufferSize, receiveBufferSize)
              : null;
          return new SimpleChatServerImpl(Executors.newCachedThreadPool(),
              getDecoratedServerSocket(tcpServerSocket, auditLogServerConnection), serverGuiInterface);
        } catch (Exception e) {
          throw new Exception(e);
        }

      case TCPAuditLogImplementation:

        try {
          TcpServerSocket tcpServerSocket = new TcpServerSocket(serverPort, sendBufferSize,
              receiveBufferSize);
          return new AuditLogServerImpl(tcpServerSocket, Executors.newCachedThreadPool());
        } catch (Exception e) {
          throw new Exception(e);
        }

		default:
			System.out.println("Dezeit nur TCP implementiert!");
			throw new RuntimeException("Unknown type: " + implType);
		}
	}


	private static ServerSocketInterface getDecoratedServerSocket(
			ServerSocketInterface serverSocket, Connection auditLogServerConnection) {
		return new DecoratingServerSocket(serverSocket, auditLogServerConnection);
	}

	/**
	 * Dekoriert Server-Socket mit Logging-Funktionalitaet
	 * 
	 * @author mandl
	 *
	 */
	private static class DecoratingServerSocket implements ServerSocketInterface {

		private final ServerSocketInterface wrappedServerSocket;
		private final Connection auditLogServerConnection;

		DecoratingServerSocket(ServerSocketInterface wrappedServerSocket, Connection auditLogServerConnection) {
			this.wrappedServerSocket = wrappedServerSocket;
			this.auditLogServerConnection = auditLogServerConnection;
		}

		@Override
		public Connection accept() throws Exception {
			Connection loggingDecorated = new LoggingConnectionDecorator(wrappedServerSocket.accept());
			return auditLogServerConnection == null
          ? loggingDecorated
          : new AuditConnectionDecorator(loggingDecorated, auditLogServerConnection);
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
