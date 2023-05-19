package service.bill;

import Bean.Bill;

import java.sql.Connection;
import java.util.List;

public interface BillService {
    List<Bill> getBillList( String productName, int proId, int currPage, int pageSize,int isPayment);
    int getBillCount( String productName, int proId);
    Bill getBllById(int id);
    boolean updateBill(int id, String key, Object value);
    Bill findBillById(int id);
    boolean delBillById(int id);
    boolean addBill(Bill bill);
}
