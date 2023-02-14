package searchengine.developer;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@RequiredArgsConstructor
public class ConnectionSql {

    public static String url = "jdbc:mysql://localhost:3306/search_engine?eseSSL=false&serverTimezone=UTC";
    public   static String user = "root";
    public static String pass = "katiaNIK22";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }


}
