package dao;

import java.sql.*;

public abstract class DaoTemplate {
    private Connection conn;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    public void queryDB() throws SQLException, ClassNotFoundException {
        //1. connect to database
        conn = connection();
        //2. operate on database
        resultSet = query(conn);
        //3. close
        close(conn,preparedStatement,resultSet);
    }
    //concrete Methods 具体方法链接数据库
    private Connection connection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        String url="jdbc:mysql://localhost:3307/smbms?useUnicode=true&characterEncoding=utf-8";
        String username = "root";
        String password = "";
        Connection connection = DriverManager.getConnection(url, username, password);
        return  connection;
    }
    //钩子方法
    private ResultSet query(Connection conn) throws SQLException {
        return null;
    }
    //具体方法关闭数据库
    private void close(Connection conn, PreparedStatement preparedStatement, ResultSet resultSet) throws SQLException {
        if(resultSet != null){
            resultSet.close();
        }
        if(preparedStatement != null){
            preparedStatement.close();
        }
        if(resultSet != null){
            resultSet.close();
        }
    }
}
