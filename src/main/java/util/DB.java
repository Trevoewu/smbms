package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DB {
    private static String DRIVER;
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    //静态代码块， 类加载的时候执行
    static {
        //读取配置文件
        //1、创建properties对象
        Properties properties = new Properties();
        //2、通过类加载器加载资源文件为字节输入流
        InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DRIVER = properties.getProperty("DRIVER");
        URL = properties.getProperty("URL");
        USERNAME = properties.getProperty("USERNAME");
        PASSWORD = properties.getProperty("PASSWORD");
    }
    //释放链接资源
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet resSet) throws SQLException {
        if(resSet != null){
            resSet.close();
        }
        if(pstmt != null){
            pstmt.close();
        }
        if(conn != null) {
            conn.close();
        }
    }
    //获取数据库链接
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER);
        String url = URL;
        String username = USERNAME;
        String password = PASSWORD;
        // 2.连接数据库,代表数据库
        Connection connection = DriverManager.getConnection(url, username, password);
        return  connection;
    }
    //执行查询, 返回结果集
    public static ResultSet executeQuery(Connection conn,String query,Object[] params) throws SQLException, ClassNotFoundException{
        ResultSet resultSet = null;
        if(conn != null && !query.isEmpty() && params != null){
            //预处理
            PreparedStatement statement = conn.prepareStatement(query);
            for (int i = 0; i < params.length;i++){
                //设置SQL 语句中的参数
                if(params[i] instanceof String){
                    statement.setString(i+1,params[i].toString());
                } else if(params[i] instanceof Integer){
                    statement.setInt(i+1,Integer.parseInt(params[i].toString()));
                } else {
                    statement.setObject(i+1,params[i]);
                }

            }
            //执行
            resultSet = statement.executeQuery();
        }
        return resultSet;
    }
    //执行更新， 返回影响行数
    public static int executeUpdate(Connection conn,String query,Object[] params) throws SQLException {
        int affectedRows = 0;
        if(conn != null && !query.isEmpty() && params != null){
            //预处理
            PreparedStatement statement = conn.prepareStatement(query);
            for (int i = 0; i < params.length;i++){
                //设置SQL 语句中的参数
                if(params[i] instanceof String){
                    statement.setString(i+1,params[i].toString());
                } else if(params[i] instanceof Integer){
                    statement.setInt(i+1,Integer.parseInt(params[i].toString()));
                } else {
                    statement.setObject(i+1,params[i]);
                }
            }
            //执行
             affectedRows = statement.executeUpdate();
        }
        return affectedRows;
    }
}
