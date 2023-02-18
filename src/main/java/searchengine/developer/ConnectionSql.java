package searchengine.developer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@RequiredArgsConstructor
@Component
public class ConnectionSql {

<<<<<<< HEAD
    public final static String url = "jdbc:mysql://localhost:3306/search_engine?eseSSL=false&serverTimezone=UTC";
    public  final static String user = "root";
    public final static String pass = "katiaNIK18";
=======
    public static String url = "jdbc:mysql://localhost:3306/searh?eseSSL=false&serverTimezone=UTC";
    public   static String user = "root";
    public static String pass = "katiaNIK22";
>>>>>>> 025241e972bcbb4cf9ecfb32bdd6ce75d407bb47

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }


}
