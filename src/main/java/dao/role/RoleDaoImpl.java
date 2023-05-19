package dao.role;

import Bean.Role;
import util.DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDaoImpl implements RoleDao{

    /*
        根据id查询角色表
        @param conn database connection
        @param id Role id 1 means 系统管理员
        2 -> 经理 3->普通员工 5->测试员 7->董事长
        return Lit<Role> 角色链表
     */
    @Override
    public List<Role> getRolelist(Connection conn, int id) {
        //prevent roleList
        List<Role> roleList = new ArrayList<>();
        if(conn != null){
            try {
                ResultSet rs = null;
                List<Object> params = new ArrayList<Object>();
                StringBuffer sql = new StringBuffer();
                //edit sql expression
                sql.append("SELECT * FROM smbms_role ");
                //输入0表查询全表， 输入>0 为sql拼接条件， 同时准备参数列表
                if(id > 0){
                    sql.append(" WHERE id = ? ");
                    params.add(id);
                }
                System.out.println("sql---->"+sql.toString());
                rs = DB.executeQuery(conn, sql.toString(), params.toArray());
                if(rs != null){
                    while (rs.next()) {
                        //赋值
                        Role role = new Role();
                        role.setId(rs.getInt("id"));
                        role.setRoleCode(rs.getString("roleCode"));
                        role.setRoleName(rs.getString("roleName"));
                        role.setCreatedBy(rs.getInt("createdBy"));
                        role.setModifyDate(rs.getDate("creationDate"));
                        role.setCreationDate(rs.getDate("creationDate"));
                        role.setModifyBy(rs.getInt("modifyBy"));
                        roleList.add(role);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }else {
            return null;
        }
        return roleList;
    }
}
