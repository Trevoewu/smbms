package dao.role;

import Bean.Role;

import java.sql.Connection;
import java.util.List;

public interface RoleDao {
    //    根据角色名查询角色
    List<Role> getRolelist(Connection conn, int id);
}
