package edu.hm.dako.chat.auditlog;

import edu.hm.dako.chat.common.PduType;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuditLogPDU implements Serializable {

  private PduType pduType;
  private String userName;
  private String clientThreadName;
  private String serverThreadName;
  private String message;
  private String time;
  private String sequenceNumber;

  public AuditLogPDU(PduType pduType) {
    this(pduType, "", "", "", "", "");
  }

  public AuditLogPDU(PduType pduType, String userName, String clientThreadName,
      String serverThreadName, String sequenceNumber, String message) {
    this.pduType = pduType;
    this.userName = userName;
    this.clientThreadName = clientThreadName;
    this.serverThreadName = serverThreadName;
    this.sequenceNumber = sequenceNumber == "0" ? "" : sequenceNumber;
    this.message = message == null ? "" : message;
    this.time = getTimestamp();
  }

  public PduType getPduType() {
    return pduType;
  }

  public String getUserName() {
    return userName;
  }

  public String getClientThreadName() {
    return clientThreadName;
  }

  public String getTime() {
    return time;
  }

  public String getServerThreadName() {
    return serverThreadName;
  }

  public String getMessage() {
    return message;
  }

  public String getSequenceNumber() {
    return sequenceNumber;
  }

  private String getTimestamp() {
    SimpleDateFormat nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    return nowDate.format(new Date());
  }
}
