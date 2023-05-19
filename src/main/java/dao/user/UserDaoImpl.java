package dao.user;

import Bean.User;
import org.junit.Test;
import util.DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserDaoImpl implements UserDao{
    private User user;
    private Connection conn;
    private String sql;
    @Test
    public void testAddUser() throws SQLException, ParseException, ClassNotFoundException {
        UserDaoImpl dao = new UserDaoImpl();
        User user1 = findById(53);
        System.out.println(user1.toString());
    }

    @Override
    public int delUser(Connection conn, int id) {
        int affectedRow = 0;
        if(conn != null){
            try {
                String sql = "Delete from smbms_user where id = ?";
                Object[] params = {id};
                affectedRow = DB.executeUpdate(conn, sql, params);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return affectedRow;
    }

    @Override
    public int addUser(Connection conn, User user) {
        int affectedRows = 0;
        if(conn != null) {
            try {
                String sql = "insert into smbms_user (userCode ,userName, userPassword," +
                        "gender, birthday, phone ,address, userRole,createdBy,creationDate)" +
                        " values(?,?,?,?,?,?,?,?,?,?)";
                Object[] params = {
                        user.getUserCode(),user.getUserName(),user.getUserPassword(),
                        user.getGender(),user.getBirthday(),user.getPhone(),
                        user.getAddress(),user.getUserRole(),user.getCreatedBy(),user.getCreationDate()
                };
                for (int i = 0; i < params.length; i++){
                    System.out.println( "param: "+params[i].toString());
                }
                    affectedRows = DB.executeUpdate(conn,sql,params);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        return affectedRows;
    }

    @Override
    public User findById(int id) {
        //2. 初始化参数
        Object[] params = {id};
        ResultSet set;
        sql = "SELECT * FROM smbms_user,smbms_role Where smbms_user.userRole = smbms_role.id and smbms_user.id = ?";
        try {
            conn = DB.getConnection();
            set = DB.executeQuery(conn, sql,params);
            if(set != null){
                user = new User();
                while (set.next()) {
                    user.setUserRoleName(set.getString("roleName"));
                    user.setUserPassword(set.getString("userPassword"));
                    user.setId(set.getInt("id"));
                    user.setUserCode(set.getString("userCode"));
                    user.setUserName(set.getString("userName"));
                    user.setAddress(set.getString("address"));
                    user.setBirthday((Date) set.getObject("birthday"));
                    user.setGender(set.getInt("gender"));
                    user.setPhone(set.getString("phone"));
                    user.setIdPicPath(set.getString("idPicPath"));
                    user.setUserRole(set.getInt("userRole"));
                    user.setCreatedBy(set.getInt("createdBy"));
                    user.setCreationDate( set.getDate("creationDate"));
                    user.setModifyBy(set.getInt("modifyBy"));
                    user.setModifyDate(set.getDate("modifyDate"));
                    user.setWorkPicPath("workPicPath");
                    System.out.println(user.toString());
                }
            } else {
                return null;
            }
            //关闭链接和结果集
            DB.close(conn,null,set);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    @Override
    public User findByUserCode(String userCode){

        //2. 初始化参数
        Object[] params = {userCode};
        ResultSet set;
        sql = "SELECT * FROM smbms_user Where userCode = ?";
        try {
            conn = DB.getConnection();
            set = DB.executeQuery(conn, sql,params);
            if(set != null){
                user = new User();
                while (set.next()) {
                    user.setUserPassword(set.getString("userPassword"));
                    user.setId(set.getInt("id"));
                    user.setUserCode(set.getString("userCode"));
                    user.setUserName(set.getString("userName"));
                    user.setAddress(set.getString("address"));
                    user.setBirthday((Date) set.getObject("birthday"));
                    user.setGender(set.getInt("gender"));
                    user.setPhone(set.getString("phone"));
                    user.setIdPicPath(set.getString("idPicPath"));
                    user.setUserRole(set.getInt("userRole"));
                    user.setCreatedBy(set.getInt("createdBy"));
                    user.setCreationDate( set.getDate("creationDate"));
                    user.setModifyBy(set.getInt("modifyBy"));
                    user.setModifyDate(set.getDate("modifyDate"));
                    user.setWorkPicPath("workPicPath");
                    System.out.println(user.toString());
                }
            } else {
                return null;
            }
            //关闭链接和结果集
            DB.close(conn,null,set);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return user;
    }
/*
    根据条件查询用户数
    @param conn database connection
    @param userName 查询限定条件
    @param userRole 用户角色(查询限定条件)
    return count
 */
    @Override
    public int getUserCount(Connection conn, String userName, int userRole) {
        int count = 0;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT count(1) as count from smbms_user s, smbms_role r where s.userRole = r.id" );
        ArrayList<Object> list = new ArrayList<Object>();
        ResultSet rs =  null;
        if(userName != null){
            sql.append(" and userName like ? ");
            list.add("%"+userName+"%");
        }
        if(userRole > 0){
            sql.append(" and userRole like ? ");
            list.add("%"+userRole+"%");
        }
        System.out.println("sql----> "+sql.toString());
        try {
            rs = DB.executeQuery(conn, sql.toString(), list.toArray());
            while (rs.next()) {
                count = rs.getInt("count");
            }
            DB.close(null,null,rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return count;
    }
    /*
        更新User表信息
        @param id 需要修改的用户id
        @param key 需要修改的对象
        @param value 新值
        @return INT affectedRow 表示Update结果的影响行数， 大于0表示成功
     */
    @Override
    public int updateUser(Integer id, String key, Object value) {
        int affectedRow = 0;
        if(id != null&& value != null && key != null){
            Connection conn = null;
            sql = "UPDATE smbms_user SET "+key+" = ? WHERE id = ? ";
            Object[] params = {value,id};
            try {
                conn = DB.getConnection();
                affectedRow = DB.executeUpdate(conn, sql, params);
                DB.close(conn,null,null);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return affectedRow;

    }
/*
    根据用户名和角色查询用户
    @param conn db connection
    @param userName user name
    @param userRole user role
    return ArrayList<User> userList
 */
    @Override
    public List<User> getUserList(Connection conn, String userName, int userRole,int currPage, int pageSize) {
        List<User> userList = new ArrayList<>();
        if(conn != null) {
            //准备要使用的对象
            ArrayList<Object> params = new ArrayList<>();
            ResultSet rs = null;
            StringBuffer sql = new StringBuffer();
            try {
                //编写sql
                sql.append("SELECT * from smbms_user u ,smbms_role r Where u.userRole = r.id ");
                //如果用户名不为空， 表示有用户名查询要求，追加限定条件
                if(userName != null){
                    sql.append(" AND userName like ? ");
                    params.add("%"+userName+"%");
                }
                //同上...
                if(userRole != 0){
                    sql.append(" and userRole = ?");
                    params.add(userRole);
                }
                sql.append(" ORDER BY u.creationDate DESC LIMIT ?,? ");//在sql最后追加一个排序和分页
                //5
                //1     5
                //2     10
                //3     15
                currPage = (currPage-1)*pageSize;//减一的原因就是MYSQL分页的index从0开始
                params.add(currPage);//从哪一个下标开始
                params.add(pageSize);//从currentPageNo连续取几个
                System.out.println("sql--->"+sql.toString());
                //执行sql， 返回结果集, ⚠️需要把sql转化为字符串， params 转化为数组
                rs = DB.executeQuery(conn,sql.toString(),params.toArray());
                if(rs != null){
                    while (rs.next()){
                        User user = new User();
                        user.setUserPassword(rs.getString("userPassword"));
                        user.setId(rs.getInt("id"));
                        user.setUserCode(rs.getString("userCode"));
                        user.setUserName(rs.getString("userName"));
                        user.setAddress(rs.getString("address"));
                        user.setBirthday((Date) rs.getObject("birthday"));
                        user.setGender(rs.getInt("gender"));
                        user.setPhone(rs.getString("phone"));
                        user.setIdPicPath(rs.getString("idPicPath"));
                        user.setUserRole(rs.getInt("userRole"));
                        user.setCreatedBy(rs.getInt("createdBy"));
                        user.setCreationDate( rs.getDate("creationDate"));
                        user.setModifyBy(rs.getInt("modifyBy"));
                        user.setModifyDate(rs.getDate("modifyDate"));
                        user.setUserRoleName(rs.getString("roleName"));
                        userList.add(user);
//                        System.out.println(user.toString());
                    }
                }
                DB.close(null,null,rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
        return userList;
    }
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        UserDaoImpl userDao = new UserDaoImpl();
        List<User> userList = userDao.getUserList(DB.getConnection(), null, 0, 1, 5);
        for (User user : userList) {
            System.out.println(user.toString());
        }
    }
}
