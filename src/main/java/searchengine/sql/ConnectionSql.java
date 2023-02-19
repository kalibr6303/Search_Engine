package searchengine.sql;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@RequiredArgsConstructor
@Component
public class ConnectionSql {

    public static final String url = "jdbc:mysql://localhost:3306/search_engine?eseSSL=false&serverTimezone=UTC";
    public   static final String user = "root";
    public static final String pass = "katiaNIK18";



    public static Connection getConnection() throws SQLException {



        return DriverManager.getConnection(url, user, pass);
    }
}