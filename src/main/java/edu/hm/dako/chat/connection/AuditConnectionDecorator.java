package edu.hm.dako.chat.connection;

import edu.hm.dako.chat.auditlog.AuditLogPDU;
import edu.hm.dako.chat.common.ChatPDU;
import edu.hm.dako.chat.tcp.TcpConnectionFactory;
import java.io.Serializable;

public class AuditConnectionDecorator implements Connection {
  private Connection wrappedConnection;
  private Connection auditLogServerConnection;

  public AuditConnectionDecorator(Connection wrappedConnection, Connection auditLogServerConnection) throws Exception {
    System.out.println("audit log connectio decorator started");
    this.wrappedConnection = wrappedConnection;
    this.auditLogServerConnection = auditLogServerConnection;
  }

  @Override
  public Serializable receive(int timeout) throws Exception, ConnectionTimeoutException {
    return wrappedConnection.receive(timeout);
  }

  @Override
  public Serializable receive() throws Exception {
    ChatPDU chatPDU = (ChatPDU)wrappedConnection.receive();
    this.sendAuditLogPdu(chatPDU);
    return chatPDU;
  }

  @Override
  public void send(Serializable message) throws Exception {
    wrappedConnection.send(message);
  }

  @Override
  public void close() throws Exception {
    wrappedConnection.close();
  }

  private void sendAuditLogPdu(ChatPDU chatPdu) throws Exception {
    AuditLogPDU auditLogPDU = new AuditLogPDU();
    //TODO einfügen parameter für diese AuditlogPDU(,,,,,) nach Beschreibung oder setters, wie in Abstractchat client 144
    this.auditLogServerConnection.send(auditLogPDU);
  }
}
