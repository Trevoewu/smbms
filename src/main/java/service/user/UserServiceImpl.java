package service.user;

import Bean.User;
import dao.user.UserDao;
import dao.user.UserDaoImpl;
import util.DB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService {
    private UserDao dao;

    @Override
    public boolean delUser(int id) {
        dao = new UserDaoImpl();
        int affectedRows = 0;
        Connection conn = null;
        try {
            conn = DB.getConnection();
            //开启事务
            conn.setAutoCommit(false);
            affectedRows = dao.delUser(conn, id);
            // 提交事务
            conn.commit();
            DB.close(conn,null,null);
        } catch (SQLException e) {
            try {
                //异常回滚事务
                conn.commit();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if(affectedRows > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean addUser(User user) {
        dao = new UserDaoImpl();
        boolean flag = false;
        Connection conn = null;
        try {
            conn = DB.getConnection();
            //开启事务
            conn.setAutoCommit(false);
            int i = dao.addUser(conn, user);
            //提交事务
            conn.commit();
            if(i > 0) {
                flag = true;
            }
        } catch (SQLException e) {
            try {
                //回滚事务
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return flag;
    }

    @Override
    public List<User> getUserlist(String userName, int userRole,int currPage, int pageSize) {
        List<User> usersList = new ArrayList<>();
        try {
            Connection conn = DB.getConnection();
            dao = new UserDaoImpl();
            usersList = dao.getUserList(conn, userName, userRole,currPage, pageSize);
            DB.close(conn,null,null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return usersList;
    }

    @Override
    public User findById(int id) {
        dao = new UserDaoImpl();
        User user = dao.findById(id);
        return user;
    }

    @Override
    public User findByUserCode(String userCode){
        dao = new UserDaoImpl();
        User user = dao.findByUserCode(userCode);
        return  user;
    }
    /*
        更新User表信息
        @param id 需要修改的用户id
        @param key 需要修改的对象
        @param value 新值
        @return boolean true 表示更新成功， 否则更新失败
     */
    @Override
    public boolean updateUser(Integer id, String key, Object value)  {
        dao = new UserDaoImpl();
        int i = 0;
        i = dao.updateUser(id, key, value);
       return i > 0 ? true : false;
    }

    @Override
    public int getUserCount(String userName, int UserRole) {
        int count = 0;
        Connection conn = null;
        try {
            conn = DB.getConnection();
            dao = new UserDaoImpl();
            count = dao.getUserCount(conn, userName, UserRole);
            DB.close(conn,null,null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

}
