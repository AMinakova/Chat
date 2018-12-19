package edu.hm.dako.chat.udp;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ServerSocketInterface;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

/**
 * Server-Socket Implementierung auf UDP-Basis
 * 
 * @author Ganna Minakova
 */
public class UdpServerSocket implements ServerSocketInterface {

	private DatagramSocket serverSocket;
	private int sendBufferSize;
	private int receiveBufferSize;

	/**
	 * Erzeugt ein UDP-Serversocket und bindet es an einen Port.
	 *
	 * @param port
	 *          Portnummer, die verwendet werden soll
	 * @param sendBufferSize
	 *          Groesse des Sendepuffers in Byte
	 * @param receiveBufferSize
	 *          Groesse des Empfangspuffers in Byte
	 * @exception BindException
	 *              Port schon belegt
	 * @exception IOException
	 *              I/O-Fehler bei der Dovket-Erzeugung
	 */
	public UdpServerSocket(int port, int sendBufferSize, int receiveBufferSize)
			throws IOException {

		this.sendBufferSize = sendBufferSize;
		this.receiveBufferSize = receiveBufferSize;
		try {
			serverSocket = new DatagramSocket(port);
		} catch (BindException e) {
			System.out.println(
					"Port " + port + " auf dem Rechner schon in Benutzung, Bind Exception: " + e);
			throw e;
		} catch (IOException e) {
			System.out.println("Schwerwiegender Fehler beim Anlegen eines UDP-Sockets mit Portnummer "
					+ port + ": " + e);
			throw e;
		}
	}

	@Override
	public Connection accept() throws UnknownHostException {
		return new UdpConnection(serverSocket, sendBufferSize, receiveBufferSize, 0,
        null);
	}

	@Override
	public void close() {
		System.out.println(
				"Serversocket wird geschlossen, lokaler Port: " + serverSocket.getLocalPort());
		serverSocket.close();
	}

	@Override
	public boolean isClosed() {
		return serverSocket.isClosed();
	}
}
