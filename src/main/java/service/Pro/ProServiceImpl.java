package service.Pro;

import Bean.Provider;
import dao.Pro.ProDao;
import dao.Pro.ProDaoImpl;
import util.DB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProServiceImpl implements ProService{
    private ProDao proDao;

    @Override
    public boolean delPro( int id) {
        int affected = 0;
        try {
            Connection conn = DB.getConnection();
            proDao = new ProDaoImpl();
            affected = proDao.delPro(conn,id);
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return affected > 0 ? true : false;
    }

    @Override
    public boolean addPro( Provider pro) {
        int affected = 0;
        try {
            Connection conn = DB.getConnection();
            proDao = new ProDaoImpl();
            affected = proDao.addPro(conn,pro);
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return affected > 0 ? true : false;
    }

    @Override
    public Provider getProById(int id) {
        Provider pro = new Provider();
        proDao = new ProDaoImpl();
        try {
            Connection conn = DB.getConnection();
            pro = proDao.getProById(conn,id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return pro;
    }

    @Override
    public boolean updatePro(String key, Object value, int id) {
        int affected = 0;
        try {
            Connection conn = DB.getConnection();
            proDao = new ProDaoImpl();
            affected = proDao.updatePro(conn,key,value,id);
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return affected > 0 ? true : false;
    }

    @Override
    public int getProCount(String ProCode, String ProName) {
        int count = 0;
        try {
            Connection conn = DB.getConnection();
            proDao = new ProDaoImpl();
            count = proDao.getProCount(conn, ProCode, ProName);
            DB.close(conn,null,null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return count;
    }

    @Override
    public List<Provider> getProList(String ProCode, String ProName, int currPage, int pageSize) {
        proDao  = new ProDaoImpl();
        List<Provider> proList;
        try {
            Connection conn = DB.getConnection();
            proList = proDao.getProList(conn, ProCode, ProName, currPage, pageSize);
            DB.close(conn,null,null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return proList;
    }
}
