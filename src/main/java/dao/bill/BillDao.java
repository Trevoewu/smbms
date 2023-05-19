package dao.bill;

import Bean.Bill;

import java.sql.Connection;
import java.util.List;

public interface BillDao {
    List<Bill> getBillList(Connection conn,String productName,int proId,int currPage, int pageSize,int isPayment);
    int getBillCount(Connection conn,String productName,int proId);
    Bill getBillById(Connection conn,int id);
    int updateBill(Connection conn,Integer id, String key, Object value);
    Bill findById(Connection conn,Integer id);
    int delBill(Connection conn,Integer id);
    int addBill(Connection conn,Bill bill);
}
