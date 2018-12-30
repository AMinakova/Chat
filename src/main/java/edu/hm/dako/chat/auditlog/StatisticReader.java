package edu.hm.dako.chat.auditlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Administratives Programm um Statistic von der Protokolierung des AuditLogServers zusamenzufassen
 *
 * @author Minakova
 */
public class StatisticReader {

  public static void main(String[] args) {
    Path path = Paths.get("AuditLogStatistics");
    int logins = 0;
    int logouts = 0;
    int messages = 0;
    int testCounter = 0;
    String[] columnsInLine;
    List<String> clientsList = new ArrayList<>();
    boolean lastPrinted = false;

    //Liest Datei
    try (BufferedReader reader = Files.newBufferedReader(path)) {
      String line;
      //Liest jede Zeile von Datei und entsprechend bearbeitet
      while ((line = reader.readLine()) != null) {
        lastPrinted = false;

        //Formatiert Datei in Columns
        columnsInLine = line.split(" \\| ");

        switch (columnsInLine[5]) {
          case "Login-Request":
            logins++;
            break;
          case "Logout-Request":
            logouts++;
            break;
          case "Chat-Message-Request":
            messages++;
            break;
        }

        //Zählt Clienten
        if (!clientsList.contains(columnsInLine[3]) && columnsInLine[3] != null
            && !columnsInLine[3].equals("")) {
          clientsList.add(columnsInLine[3]);
        }

        //Macht Zusammenfassung für jeden Test
        if (logouts == logins && logouts != 0) {
          testCounter++;
          System.out.println("Test #" + testCounter + " | Clients: " + clientsList.size() + " | Logins: "
              + logins + " | Messages: " + messages + " | Logouts: " + logouts);
          lastPrinted = true;
          logins = 0;
          logouts = 0;
          messages = 0;
          clientsList.clear();
        }
      }

      //Macht Zusammenfassung für jeden Test auch mit teilweiseverlorener Folge
      if (!lastPrinted && (logouts != 0 || logins != 0)) {
        testCounter++;
        System.out.println("Test #" + testCounter + " | Clients: " + clientsList.size() + " | Logins: "
            + logins + " | Messages: " + messages + " | Logouts: " + logouts);
      }
    } catch (IOException e) {
      System.out.println("Smth wrong" + e);
    }
  }
}
