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
  private Date timestamp;

  public AuditLogPDU(PduType pduType) {
    this.pduType = pduType;
    this.timestamp = new Date();
  }

  public AuditLogPDU(PduType pduType, String userName, String clientThreadName,
      String serverThreadName, String message) {
    this.pduType = pduType;
    this.userName = userName;
    this.clientThreadName = clientThreadName;
    this.serverThreadName = serverThreadName;
    this.message = message;
    this.timestamp = new Date();
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

  public String getServerThreadName() {
    return serverThreadName;
  }

  public String getMessage() {
    return message;
  }

  public String getTimestamp() {
    SimpleDateFormat nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//dd/MM/yyyy
    String timestamp = nowDate.format(this.timestamp);
    return timestamp;
  }
}
