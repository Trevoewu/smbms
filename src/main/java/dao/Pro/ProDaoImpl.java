package dao.Pro;

import Bean.Provider;
import Bean.Provider;
import org.junit.Test;
import util.DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProDaoImpl implements ProDao{
@Test
    public void test() throws SQLException, ClassNotFoundException {
    ProDaoImpl dao = new ProDaoImpl();
    int count = dao.getProCount(DB.getConnection(), "G", "北京");
    System.out.println(count);
}

    @Override
    public int delPro(Connection conn, int id) {
    int affectedRow = 0;
    String sql = "DELETE FROM smbms_provider WHERE id = ? ";
    Object[] params={id};
        try {
            conn.setAutoCommit(false);
            affectedRow = DB.executeUpdate(conn, sql, params);
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
        return affectedRow;
    }

    @Override
    public int addPro(Connection conn, Provider pro) {
        int affectedRow = 0;
        String sql = "insert into smbms_provider(proCode,proName,proDesc,proContact,proPhone,proAddress,proFax,createdBy,creationDate)" +
                "values(?,?,?,?,?,?,?,?,?)";
        Object[] params = {pro.getProCode(),pro.getProName(),pro.getProDesc(),pro.getProContact(),pro.getProPhone(),pro.getProAddress(),
        pro.getProFax(),pro.getCreatedBy(),pro.getCreationDate()
        };
        try {
            conn.setAutoCommit(false);
            affectedRow = DB.executeUpdate(conn,sql,params);
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
        return affectedRow;
    }

    @Override
    public Provider getProById(Connection conn, int id) {
        Provider pro = new Provider();
        String sql = "Select * from smbms_provider where id = ?";
        Object[] params = {id} ;
        try {
            ResultSet rs = DB.executeQuery(conn, sql, params);
            while (rs.next()) {
                pro.setId(rs.getInt("id"));
                pro.setProName(rs.getString("proName"));
                pro.setProCode(rs.getString("proCode"));
                pro.setProDesc(rs.getString("proDesc"));
                pro.setProContact(rs.getString("proContact"));
                pro.setProPhone(rs.getString("proPhone"));
                pro.setProAddress(rs.getString("proAddress"));
                pro.setProFax(rs.getString("proFax"));
                pro.setCreatedBy(rs.getInt("createdBy"));
                pro.setCreationDate(rs.getDate("creationDate"));
                pro.setModifyBy(rs.getInt("modifyBy"));
                pro.setModifyDate(rs.getDate("modifyDate"));
            }
            DB.close(null,null,rs);
            System.out.println(pro.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return pro;
    }

    @Override
    public int updatePro(Connection conn, String key, Object value,int id) {
        int affected = 0;
        Object[] params = {value,id};
        StringBuffer sql = new StringBuffer();
        sql.append("update smbms_provider set "+key+" = ? where id = ? ");
        try {
            conn.setAutoCommit(false);
            affected  = DB.executeUpdate(conn, sql.toString(), params);
            conn.commit();

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
        return affected;
    }

    @Override
    public List<Provider> getProList(Connection conn, String ProCode, String proName,int currPage, int pageSize) {
        List<Provider> proList = new ArrayList<>();
        if(conn != null) {
            //准备要使用的对象
            ArrayList<Object> params = new ArrayList<>();
            ResultSet rs = null;
            StringBuffer sql = new StringBuffer();
            try {
                //编写sql
                sql.append("SELECT * from smbms_provider p Where 1 = 1 ");
                //如果用户名不为空， 表示有用户名查询要求，追加限定条件
                if(ProCode != null || (ProCode != null && !ProCode.equals("")) ){
                    sql.append(" AND proCode like ? ");
                    params.add("%"+ProCode+"%");
                }
                //同上...
                if(proName != null || (proName != null && !proName.equals(""))){
                    sql.append(" and proName like ?");
                    params.add('%'+proName+'%');
                }
                sql.append(" ORDER BY p.creationDate DESC LIMIT ?,? ");//在sql最后追加一个排序和分页
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
                        Provider pro = new Provider();
                        pro.setId(rs.getInt("id"));
                        pro.setProCode(rs.getString("proCode"));
                        pro.setProName(rs.getString("proName"));
                        pro.setProDesc(rs.getString("proDesc"));
                        pro.setProContact(rs.getString("proContact"));
                        pro.setProPhone(rs.getString("proPhone"));
                        pro.setProAddress(rs.getString("proAddress"));
                        pro.setProFax(rs.getString("proFax"));
                        pro.setCreatedBy(rs.getInt("createdBy"));
                        pro.setCreationDate(rs.getDate("creationDate"));
                        pro.setModifyBy(rs.getInt("modifyBy"));
                        pro.setModifyDate( rs.getDate("modifyDate"));
                        proList.add(pro);
                        System.out.println(pro.toString());
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
        return proList;
    }
    @Override
    public int getProCount(Connection conn, String ProCode, String ProName) {
    int count = 0;
        if(conn != null) {
            StringBuffer sql = new StringBuffer();
            ArrayList<Object> params = new ArrayList<>();
            sql.append("SELECT count(1) as COUNT from smbms_provider where 1 = 1 ");
            if(ProCode != null || !ProCode.equals("")){
                sql.append(" and proCode like ? ");
                params.add('%'+ProCode+'%');
            }
            if(ProName != null && !ProName.equals("")){
                sql.append(" and proName like ? ");
                params.add('%'+ProName+'%');
            }
            System.out.println(sql);
            try {
                ResultSet rs = DB.executeQuery(conn, sql.toString(), params.toArray());
                while (rs.next()) {
                    count = rs.getInt("COUNT");
                }
                DB.close(null,null,rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return count;
    }
}
