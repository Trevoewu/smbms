package service.Pro;

import Bean.Provider;

import java.sql.Connection;
import java.util.List;

public interface ProService {
    public List<Provider> getProList(String ProCode, String ProName, int currPage, int pageSize);
    int getProCount(String ProCode, String ProName);
    boolean delPro(int id);
    boolean addPro( Provider pro);
    Provider getProById(int id);
    boolean updatePro(String key,Object value,int id);
}
