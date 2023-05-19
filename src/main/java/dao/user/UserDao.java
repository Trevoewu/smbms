package dao.user;

import Bean.User;

import java.sql.Connection;
import java.util.List;

public interface UserDao {
    User findByUserCode(String userCode);
    User findById(int id);
    int updateUser(Integer id, String key, Object value);
//    查询用户数
    int getUserCount(Connection conn,String userName, int userRole);
//    根据用户名， 用户角色, 查询用户数
    List<User> getUserList(Connection conn, String userName, int userRole, int currPage, int pageSize);
// 添加用户
    int addUser(Connection conn,User user);
    int delUser(Connection conn,int id);

}
