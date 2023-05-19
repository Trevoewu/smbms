package dao.bill;

import Bean.Bill;
import org.junit.Test;
import util.DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BillDaoImpl implements BillDao {
    @Override
    public int addBill(Connection conn, Bill bill) {
        int affectedRows = 0;
        if(conn != null) {
            try {
                String sql = "insert into smbms_bill (billCode ,productName," +
                        "productUnit, productCount, totalPrice ,isPayment, createdBy,creationDate,providerId)" +
                        " values(?,?,?,?,?,?,?,?,?)";
                Object[] params = {
                        bill.getBillCode(),bill.getProductName(),
                        bill.getProductUnit(),bill.getProductCount(),bill.getTotalPrice(),
                        bill.getIsPayment(),bill.getCreatedBy(),bill.getCreationDate()
                        ,bill.getProviderId()
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
    public Bill findById(Connection conn, Integer id) {
        Bill bill = new Bill();
        String sql = " SELECT * FROM smbms_bill b,smbms_provider p,smbms_user u WHERE b.providerId = p.id AND b.createdBy = u.id AND b.id = ?";
        System.out.println("sql---->"+sql);
        Object[] args = {id};
        try {
            ResultSet rs = DB.executeQuery(conn, sql, args);
            while (rs.next()) {
                bill.setId(rs.getInt("id"));
                bill.setBillCode(rs.getString("billCode"));
                bill.setProductName(rs.getString("productName"));
                bill.setProductDesc(rs.getString("productDesc"));
                bill.setProductUnit(rs.getString("productUnit"));
                bill.setProductCount(rs.getBigDecimal("productCount"));
                bill.setTotalPrice(rs.getBigDecimal("totalPrice"));
                bill.setIsPayment(rs.getInt("isPayment"));
                bill.setCreatedBy(rs.getInt("createdBy"));
                bill.setCreationDate(rs.getDate("creationDate"));
                bill.setModifyBy(rs.getInt("modifyBy"));
                bill.setModifyDate(rs.getDate("modifyDate"));
                bill.setProviderId(rs.getInt("providerId"));
                bill.setProviderName(rs.getString("proName"));
                bill.setCreatedName(rs.getString("userName"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return bill;
    }

    @Override
    public int delBill(Connection conn, Integer id) {
        int affected = 0;
        String sql = "DELETE FROM smbms_bill WHERE id = ?";
        System.out.println("sql-->"+sql.toString());
        Object[] args = {id};
        try {
            conn.setAutoCommit(false);
            affected = DB.executeUpdate(conn, sql, args);
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
    public int getBillCount(Connection conn, String productName, int proId) {
        int count = 0;
        if(conn != null) {
            StringBuffer sql = new StringBuffer();
            ArrayList<Object> params = new ArrayList<>();
            sql.append("SELECT count(1) as COUNT from smbms_bill b, smbms_provider p where b.providerId = p.id ");
            if(proId > 0){
                sql.append(" and providerId = ? ");
                params.add(+proId);
            }
            if(productName != null && !productName.equals("")){
                sql.append(" and proName like ? ");
                params.add('%'+productName+'%');
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

    @Override
    public int updateBill(Connection conn, Integer id, String key, Object value) {
        int affected = 0;
        StringBuffer sql = new StringBuffer();
        sql.append("update smbms_bill set "+key+" = ? where id = ? ");
        System.out.println("sql---->"+sql.toString());
        Object[] params = {value,id};
        try {
            affected = DB.executeUpdate(conn, sql.toString(), params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return affected;
    }

    @Override
    public Bill getBillById(Connection conn, int id) {
        Object[] params = {id};
        ArrayList<Bill> billList = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        Bill bill = new Bill();
        if(conn != null) {
            try {
                sql.append("select * from smbms_bill b where id = ?");
                ResultSet rs = DB.executeQuery(conn, sql.toString(), params);
                while (rs.next()) {
                    bill.setId(rs.getInt("id"));
                    bill.setBillCode(rs.getString("billCode"));
                    bill.setProductName(rs.getString("productName"));
                    bill.setProductDesc(rs.getString("productDesc"));
                    bill.setProductUnit(rs.getString("productUnit"));
                    bill.setProductCount(rs.getBigDecimal("productCount"));
                    bill.setTotalPrice(rs.getBigDecimal("totalPrice"));
                    bill.setIsPayment(rs.getInt("isPayment"));
                    bill.setCreatedBy(rs.getInt("createdBy"));
                    bill.setCreationDate(rs.getDate("creationDate"));
                    bill.setModifyBy(rs.getInt("modifyBy"));
                    bill.setModifyDate(rs.getDate("modifyDate"));
                    bill.setProviderId(rs.getInt("providerId"));
                    bill.setProviderName(rs.getString("productName"));
                }
                DB.close(null,null,rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return  bill;
    }

    @Override
    public List<Bill> getBillList(Connection conn, String productName, int proId,int currPage, int pageSize,int isPayment) {
        ArrayList<Object> params = new ArrayList<>();
        ArrayList<Bill> billList = new ArrayList<>();
        if(conn != null) {
            StringBuffer sql = new StringBuffer();
            sql.append("select * from smbms_bill b, smbms_provider p where b.providerId = p.id ");
            if(productName != null || !productName.equals("")) {
                sql.append(" and productName like ? ");
                params.add('%'+productName+'%');
            }
            if(proId > 0){
                sql.append(" and providerId = ? ");
                params.add(proId);
            }
            if(isPayment > 0){
                sql.append(" and isPayment = ?");
                params.add(isPayment);
            }
            sql.append(" ORDER BY b.creationDate DESC LIMIT ?,? ");//在sql最后追加一个排序和分页
            currPage = (currPage-1)*pageSize;//减一的原因就是MYSQL分页的index从0开始
            params.add(currPage);//从哪一个下标开始
            params.add(pageSize);//从currentPageNo连续取几个
            System.out.println("sql--->"+sql.toString());
            try {
                ResultSet rs = DB.executeQuery(conn, sql.toString(), params.toArray());
                while (rs.next()) {
                    Bill bill = new Bill();
                    bill.setId(rs.getInt("id"));
                    bill.setBillCode(rs.getString("billCode"));
                    bill.setProductName(rs.getString("productName"));
                    bill.setProductDesc(rs.getString("productDesc"));
                    bill.setProductUnit(rs.getString("productUnit"));
                    bill.setProductCount(rs.getBigDecimal("productCount"));
                    bill.setTotalPrice(rs.getBigDecimal("totalPrice"));
                    bill.setIsPayment(rs.getInt("isPayment"));
                    bill.setCreatedBy(rs.getInt("createdBy"));
                    bill.setCreationDate(rs.getDate("creationDate"));
                    bill.setModifyBy(rs.getInt("modifyBy"));
                    bill.setModifyDate(rs.getDate("modifyDate"));
                    bill.setProviderId(rs.getInt("providerId"));
                    bill.setProviderName(rs.getString("productName"));
                    billList.add(bill);
                    System.out.println(bill.toString());
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return billList;
    }
    @Test
    public void test() throws SQLException, ClassNotFoundException {
        BillDaoImpl impl = new BillDaoImpl();
//        List<Bill> billList = impl.getBillList(DB.getConnection(), "", -1,1,5);
        int count = impl.getBillCount(DB.getConnection(), "", -1);
        System.out.println(count);
    }
}
