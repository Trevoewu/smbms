package dao.Pro;

import Bean.Provider;

import java.sql.Connection;
import java.util.List;

public interface ProDao {
    public List<Provider> getProList(Connection conn,String ProCode,String ProName,int currPage, int pageSize);
    int getProCount(Connection conn,String ProCode,String ProName);
    int delPro(Connection conn,int id);
    int addPro(Connection conn, Provider pro);
    Provider getProById(Connection conn,int id);
    int updatePro(Connection conn,String key,Object value,int id);
}
