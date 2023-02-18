package searchengine.developer;

import org.springframework.stereotype.Component;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Component
public class ConnectionSql {

    private static final String fileName = "application.yaml";

    public static Connection getConnection() throws SQLException, IOException {
        List<String> configConnections = readUsingFileReader(fileName);
        return DriverManager.getConnection(configConnections.get(0),
                configConnections.get(1),
                configConnections.get(2));
    }

    public static List<String> readUsingFileReader(String fileName)
            throws IOException {
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        List<String> config = new ArrayList<>();


        while ((line = br.readLine()) != null) {
            if (line.matches("(\\s+username:\\s[^а-я]+)|" +
                    "(\\s+password:\\s[^а-я]+)|" +
                    "(\\s+url:\\s[^а-я]+)")) {
                config.add(line);
            }
        }
        br.close();
        fr.close();
        return  config;
    }
}
