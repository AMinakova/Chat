package edu.hm.dako.chat.udp;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ConnectionTimeoutException;
import edu.hm.dako.chat.connection.EndOfFileException;
import edu.hm.dako.chat.tcp.TcpConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementierung der Nachrichtenaustausch mit UDP
 *
 * @author Minakova
 */
public class UdpConnection implements Connection {

  private static Log log = LogFactory.getLog(TcpConnection.class);

  // Verwendetes UDP-Socket
  private DatagramSocket socket;
  private final int serverPort;
  private final InetAddress serverAddress;
  private byte[] receiveBuffer = new byte[2048];

  public UdpConnection(DatagramSocket socket, int sendBufferSize, int receiveBufferSize,
      int serverPort, String serverHost)
      throws UnknownHostException {
    this.socket = socket;
    this.serverPort = serverPort;

    this.serverAddress =
        serverHost == null ? InetAddress.getLocalHost() : InetAddress.getByName(serverHost);

    log.debug(Thread.currentThread().getName()
        + ": Nachrichtenaustausch mit UDP vorbereitet, Remote-UDP-Port " + socket.getPort());

    try {
      log.debug("Standardgroesse des Empfangspuffers des Nachrichtenaustausch: "
          + socket.getReceiveBufferSize() + " Byte");
      log.debug("Standardgroesse des Sendepuffers des Nachrichtenaustausch: "
          + socket.getSendBufferSize() + " Byte");

      socket.setReceiveBufferSize(receiveBufferSize);
      socket.setSendBufferSize(sendBufferSize);

      log.debug("Eingestellte Groesse des Empfangspuffers des Nachrichtenaustausch: "
          + socket.getReceiveBufferSize() + " Byte");
      log.debug("Eingestellte Groesse des Sendepuffers des Nachrichtenaustausch: "
          + socket.getSendBufferSize() + " Byte");

    } catch (SocketException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Überschriebene Receive-Methode für UDP mit DatagramPacket
   * @param timeout
   *          Maximale Wartezeit in ms
   * @return Serealizable Nachricht
   * @throws Exception
   */
  @Override
  public Serializable receive(int timeout)
      throws Exception {

    socket.setSoTimeout(timeout);
    try {
      DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
      socket.receive(packet);
      ObjectInputStream iStream = new ObjectInputStream(
          new ByteArrayInputStream(receiveBuffer, 0, packet.getLength()));
      Serializable messageClass = (Serializable) iStream.readObject();
      iStream.close();

      return messageClass;
    } catch (java.net.SocketTimeoutException e) {
      throw new ConnectionTimeoutException(e);
    } catch (java.io.EOFException e) {
      log.debug("End of File beim Empfang");
      throw new EndOfFileException(e);
    } catch (Exception e) {
      log.debug("Vermutlich SocketException: " + e);
      throw new EndOfFileException(e);
    }
  }

  @Override
  public Serializable receive() throws Exception {

    return receive(0);
  }

  /**
   * Methode um Nachricht durch DatagrammSocket zu schicken
   * @param message
   *          Die zu sendende Nachricht.
   * @throws Exception
   */
  @Override
  public void send(Serializable message) throws Exception {

    if (socket.isClosed()) {
      log.debug("Sendeversuch, obwohl Socket geschlossen ist");
      throw new IOException();
    }

    try {
      ByteArrayOutputStream bStream = new ByteArrayOutputStream();
      ObjectOutput out = new ObjectOutputStream(bStream);
      out.writeObject(message);
      out.close();

      byte[] serializedMessage = bStream.toByteArray();
      DatagramPacket DPsend = new DatagramPacket(serializedMessage, serializedMessage.length,
          serverAddress, serverPort);
      socket.send(DPsend);

    } catch (Exception e) {
      log.debug("Exception beim Sendeversuch an " + socket.getInetAddress());
      log.debug(e.getMessage());
      throw new IOException();
    }
  }

  /**
   * Datagrammsocket schliesst sich
   * @throws IOException
   */
  @Override
  public synchronized void close() throws IOException {
    try {
      log.debug("Socket wird geschlossen, lokaler Port: "
          + socket.getLocalPort() + ", entfernter Port: " + socket.getPort());
      socket.close();
    } catch (Exception e) {
      log.debug("Exception beim Socketabbau " + socket.getInetAddress());
      log.debug(e.getMessage());
      throw new IOException();
    }
  }
}
