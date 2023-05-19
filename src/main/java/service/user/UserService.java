package service.user;

import Bean.User;

import java.util.List;

public interface UserService {
    User findByUserCode(String userCode) ;
    User findById(int id) ;
    boolean updateUser(Integer id, String key, Object value);
    int getUserCount(String userName,int UserRole);
    List<User> getUserlist(String userName, int userRole,int currPage, int pageSize);
    boolean addUser(User user);
    boolean delUser(int id);

}
