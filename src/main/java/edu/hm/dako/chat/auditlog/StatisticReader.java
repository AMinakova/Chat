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
    int testNumber = 0;
    String name;
    String PDUtype;
    String[] columnsInLine;
    List<String> clientsList = new ArrayList<>();

    //Liest Datei
    try (BufferedReader reader = Files.newBufferedReader(path)) {
      String line;

      //Liest jede Zeile von Datei und entsprechend bearbeitet
      while ((line = reader.readLine()) != null) {

        //Formatiert Datei in Columns
        columnsInLine = line.split(" \\| ");
        PDUtype = columnsInLine[5];
        switch (PDUtype) {
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
        name = columnsInLine[3];
        if (!clientsList.contains(name) && name != null
            && !name.equals("")) {
          clientsList.add(name);
        }

        //Macht Zusammenfassung für Test
        if (PDUtype.equals("Shutdown-Message")) {
          System.out.println("Test #" + ++testNumber + " | Clients: " + clientsList.size() + " | Logins: "
              + logins + " | Messages: " + messages + " | Logouts: " + logouts);
          logins = 0;
          logouts = 0;
          messages = 0;
          clientsList.clear();
        }
      }
    } catch (IOException e) {
      System.out.println("Smth wrong" + e);
    }
  }
}
