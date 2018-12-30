package edu.hm.dako.chat.common;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Nachrichtenaufbau fuer Auditlog-Protokoll (empfangene Nachrichten)
 *
 * @author Ganna Minakova
 */

public class AuditLogPDU implements Serializable {

  private PduType pduType;
  private String userName;
  private String clientThreadName;
  private String serverThreadName;
  private String message;

  //Zeit von der Erzeugung vom AuditLogPDU
  private String time;
  private String sequenceNumber;

  /**
   * Konstruktor für AuditLogPdu nur mit PDU-Type als Parameter (Shutdown-Message)
   *
   * @param pduType PDU-Typen
   */
  public AuditLogPDU(PduType pduType) {
    this(pduType, "", "", "", "", "");
  }

  /**
   * Konstruktor für Nachrichten in der Kette "Client-Server-Auditlogserver"
   *
   * @param pduType PDU-Typen
   * @param userName Name des Clients, von dem ein Event initiiert wurde
   * @param clientThreadName Name des Client-Threads, der den Request absendet
   * @param serverThreadName Name des Threads, der den Request im Server empfängt
   * @param sequenceNumber Anzahl den uebertragenen Nachrichten eines Clients
   * @param message Nachrichtdaten in Textform
   */
  public AuditLogPDU(PduType pduType, String userName, String clientThreadName,
      String serverThreadName, String sequenceNumber, String message) {
    this.pduType = pduType;
    this.userName = userName;
    this.clientThreadName = clientThreadName;
    this.serverThreadName = serverThreadName;
    this.sequenceNumber = sequenceNumber == null ? "" : sequenceNumber;
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

  //Methode, die Zeitstempel in passendem Typ erzeugt
  private String getTimestamp() {
    SimpleDateFormat nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    return nowDate.format(new Date());
  }
}
