package edu.hm.dako.chat.auditlog;

import edu.hm.dako.chat.connection.Connection;
import edu.hm.dako.chat.connection.ServerSocketInterface;
import edu.hm.dako.chat.server.ChatServerGUI;
import edu.hm.dako.chat.server.ChatServerInterface;
import java.util.concurrent.ExecutorService;
import javafx.concurrent.Task;

/**
 * AuditLogServer-Implementierung
 *
 * @author Minakova (auf Basis von der Arbeit von Peter Mandl)
 */

public class AuditLogServerImpl implements ChatServerInterface {

  private ServerSocketInterface socket;
  private final ExecutorService executorService;
  private ChatServerGUI serverGuiInterface;
  private AuditLogStatistics auditLogStatistics;

  /**
   * Konstruktor für AuditLogServer, welche neues File für Protokolierung erzeugt
   *
   * @param socket socket
   * @param executorService executorService
   * @param serverGuiInterface Gui von AuditLogServer
   */
  public AuditLogServerImpl(ServerSocketInterface socket,
      ExecutorService executorService,
      ChatServerGUI serverGuiInterface) {
    this.socket = socket;
    this.executorService = executorService;
    this.serverGuiInterface = serverGuiInterface;
    this.auditLogStatistics = new AuditLogStatistics("AuditLogStatistics");
  }

  @Override
  public void start() {
    AuditLogServerImpl auditLogServer = this;
    Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        boolean isStarting = true;

        //Wegen UDP-Schwerigkeiten, erzeugen wir nut einen Thread
        while (!Thread.currentThread().isInterrupted() && !socket.isClosed() && isStarting) {
          isStarting = false;
          try {
            // Auf ankommende Verbindungsaufbauwuensche warten
            System.out.println(
                "AuditLogServer wartet auf Verbindungsanfragen von Clients...");

            Connection connection = socket.accept();

            // Neuen Workerthread starten
            executorService.submit(
                new AuditLogWorkerThreadImpl(connection, auditLogServer, auditLogStatistics));

          } catch (Exception e) {
            if (socket.isClosed()) {
              System.out.println("Socket wurde geschlossen");
            } else {
              System.out.println(
                  "Exception beim Entgegennehmen von Verbindungsaufbauwuenschen: " + e);
            }
          }
        }
        return null;
      }
    };

    Thread th = new Thread(task);
    th.setDaemon(true);
    th.start();
  }

  /**
   * Macht Änderungen in GUI vom AuditlogServer wegen dem Schutdown Nachricht
   */
  public void shutdown() {
    serverGuiInterface.stopServer();
  }

  @Override
  public void stop() throws Exception {
    Thread.currentThread().interrupt();
    socket.close();
    System.out.println("Listen-Socket geschlossen");
    executorService.shutdown();
    System.out.println("Threadpool freigegeben");
    System.out.println("AuditLogServer beendet sich");
  }
}
