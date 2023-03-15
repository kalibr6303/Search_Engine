package searchengine.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class UserAgent {

    private final static String path = "agents.txt";
    private static String referrer;


    public static String getReferrer() {
        return referrer;
    }


    private    static List<String> readUsingScanner()
            throws IOException
    {

        List<String> agents = new ArrayList<>();
        Path paths = Paths.get(path);
        Scanner scanner = new Scanner(paths);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.matches("\\s-\\suserAgent:\\s[\\W\\w]+")) {
                String line1 = line.replaceAll("\\s-\\suserAgent:\\s", "");
                agents.add(line1);
            }
            if (line.matches("\\s-\\sreferrer:\\s[\\W\\w]+")) {
                String line2 = line.replaceAll("\\s-\\sreferrer:\\s", "");
                referrer = line2;
            }
        }
        scanner.close();

        return agents;
    }


    public static String getAgent() throws IOException {
        List<String> userAgentsList = readUsingScanner();
        int s = (int) (Math.random() * userAgentsList.size() - 1);
        return userAgentsList.get(s);

    }
}