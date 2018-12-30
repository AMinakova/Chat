package edu.hm.dako.chat.auditlog;

import edu.hm.dako.chat.common.AuditLogPDU;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;

/**
 * Implementierung der Protokolierung des AuditlogServers
 *
 * @author Minakova
 */

public class AuditLogStatistics {

  private AuditLogPDU auditLogPDU;
  private File file;
  private String fileName;

  //Zaelt die Events, welche AuditLogServer empfängt
  private int counter;

  /**
   * Kostruktor, welcher File beim ersten Start von AuditLogServer erzeugt
   */
  public AuditLogStatistics(String filename) {
    this.fileName = filename;
    this.file = new File(filename);
    counter = 0;
    try {
      boolean exist = file.createNewFile();
      if (!exist) {
        System.out.println("Datei " + fileName + " existierte bereits");
      } else {
        System.out.println("Datei " + fileName + " erfolgreich angelegt");
      }
    } catch (IOException e) {
      System.out.println("Fehler beim Öffnen von der Datei " + fileName);
    }
  }

  /**
   * Öffnet File und schriebt neue Einträge in speziell angegebenem Form unten auf
   */
  public void writeAuditLogStatistics(AuditLogPDU auditLogPDU) {
    try {
      FileWriter fstream = new FileWriter(fileName, true);
      BufferedWriter out = new BufferedWriter(fstream);

      StringBuilder sb = new StringBuilder();
      Formatter formatter = new Formatter();

      sb.append(formatter.format(
          "%s | %s | %s | %s | %s | %s | %s | %s %n", ++counter, auditLogPDU.getTime(),
          auditLogPDU.getServerThreadName(),
          auditLogPDU.getUserName(), auditLogPDU.getClientThreadName(), auditLogPDU.getPduType(),
          auditLogPDU.getSequenceNumber(), auditLogPDU.getMessage()));

      out.append(sb);
      formatter.close();
      out.flush();
      out.close();

    } catch (Exception e) {
      System.out.println(
          "Fehler beim Schreiben des Auswertungssatzes in Datei " + fileName + e.getMessage());
    }
  }
}
