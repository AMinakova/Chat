package edu.hm.dako.chat.udp;

import java.io.IOException;
import java.net.BindException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ServerSocketInterface;

/**
 * Datagram-Socket Implementierung auf UDP-Basis
 *
 * @author Peter Mandl, E.Nicole Harmat
 */
public class UdpServerSocket implements ServerSocketInterface {

  private static Log log = LogFactory.getLog(UdpServerSocket.class);

  private static java.net.DatagramSocket datagramSocket;
  int sendBufferSize;
  //int receiveBufferSize;

  /**
   * Erzeugt ein UDP-Serversocket und bindet es an einen Port.
   *
   * @param port Portnummer, die verwendet werden soll
   * @param sendBufferSize Groesse des Sendepuffers in Byte
   * @exception BindException Port schon belegt
   * @exception IOException I/O-Fehler bei der Dovket-Erzeugung
   */
  public UdpServerSocket(int port, int sendBufferSize)
      throws BindException, IOException {

    this.sendBufferSize = sendBufferSize;
    //this.receiveBufferSize = receiveBufferSize;
    try {
      datagramSocket = new java.net.DatagramSocket(port);
    } catch (BindException e) {
      log.debug("Port " + port + " auf dem Rechner schon in Benutzung, Bind Exception: " + e);
      throw e;
    } catch (IOException e) {
      log.debug(
          "Schwerwiegender Fehler beim Anlegen eines UDP-Sockets mit Portnummer "
              + port
              + ": "
              + e);
      throw e;
    }
  }

  @Override
  public void close() throws IOException {
    log.debug("Serversocket wird geschlossen, lokaler Port: " + datagramSocket.getLocalPort());
    datagramSocket.close();
  }

  @Override
  public boolean isClosed() {
    return datagramSocket.isClosed();
  }

  @Override
  public Connection accept() throws IOException {
    return null;
  }
}
