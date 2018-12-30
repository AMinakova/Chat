package edu.hm.dako.chat.auditlog;

import edu.hm.dako.chat.common.AuditLogPDU;
import edu.hm.dako.chat.common.PduType;
import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ConnectionTimeoutException;
import edu.hm.dako.chat.connection.EndOfFileException;

/**
 * Worker-Thread zur serverseitigen Bedienung einer Session mit Chat Server als Client. Bei dieser
 * Implementierung wird nur einen Worker-Thread erzeugt.
 *
 * @author Minakova (auf Basis von der Arbeit von Peter Mandl)
 */

public class AuditLogWorkerThreadImpl extends Thread {

  private Connection connection;
  private boolean finished = false;
  private AuditLogServerImpl auditLogServer;
  private AuditLogStatistics auditLogStatistics;

  public AuditLogWorkerThreadImpl(Connection connection,
      AuditLogServerImpl auditLogServer, AuditLogStatistics auditLogStatistics) {
    this.connection = connection;
    this.auditLogServer = auditLogServer;
    this.auditLogStatistics = auditLogStatistics;
  }

  @Override
  public void run() {
    System.out.println(
        "AuditLogWorker-Thread erzeugt, Threadname: " + Thread.currentThread().getName());
    while (!finished && !Thread.currentThread().isInterrupted()) {
      try {
        // Warte auf naechste Nachricht des Clients und fuehre
        // entsprechende Aktion aus
        handleIncomingMessage();
      } catch (Exception e) {
        System.out.println("Exception waehrend der Nachrichtenverarbeitung");
      }
    }
    System.out.println(Thread.currentThread().getName() + " beendet sich");
    closeConnection();
  }

  protected void handleIncomingMessage() throws Exception {
    // Warten auf naechste Nachricht
    AuditLogPDU auditLogPDU = null;

    // Nach einer Minute wird geprueft, ob Client noch eingeloggt ist
    final int RECEIVE_TIMEOUT = 1200000;

    try {
      // Nachricht empfangen
      auditLogPDU = (AuditLogPDU) connection.receive(RECEIVE_TIMEOUT);

    } catch (ConnectionTimeoutException e) {

      // Wartezeit beim Empfang abgelaufen, pruefen, ob der Client
      // ueberhaupt noch etwas sendet
      System.out.println(
          "Timeout beim Empfangen, " + RECEIVE_TIMEOUT + " ms ohne Nachricht vom Client");
      return;

    } catch (EndOfFileException e) {
      System.out.println("End of File beim Empfang, vermutlich Verbindungsabbau des Partners.");
      finished = true;

      return;

    } catch (java.net.SocketException e) {
      System.out.println("Verbindungsabbruch beim Empfang der naechsten Nachricht vom Client.");
      finished = true;
      return;

    } catch (Exception e) {
      System.out.println("Empfang einer Nachricht fehlgeschlagen.");
      finished = true;
      return;
    }

    // Empfangene Nachricht protokolieren
    try {
      auditLogStatistics.writeAuditLogStatistics(auditLogPDU);
    } catch (Exception e) {
      System.out.println("Exception bei der Nachrichtenverarbeitung");
    }

    // Shutdown Nachricht bearbeiten
    if (auditLogPDU.getPduType() == PduType.SHUTDOWN_MESSAGE) {
      finished = true;
      this.auditLogServer.shutdown();
    }
  }

  private void closeConnection() {
    try {
      connection.close();
    } catch (Exception e) {
      System.out.println("Exception bei close");
    }
  }
}
