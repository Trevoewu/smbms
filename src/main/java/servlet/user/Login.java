package servlet.user;

import Bean.User;
import service.user.UserService;
import service.user.UserServiceImpl;
import util.Constant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Login extends HttpServlet {
    private UserService service;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userCode = req.getParameter("userCode");
        String password = req.getParameter("userPassword");
        service = new UserServiceImpl();
        User user = null;
        user = service.findByUserCode(userCode);
        System.out.println("----->:"+user.getUserPassword());
        if(user != null && user.getUserPassword() != null) {
            if(user.getUserPassword().equals(password)) {
//                password correct
//                store in session
                req.getSession().setAttribute(Constant.USER_SESSION,user);
                resp.sendRedirect("/jsp/frame.jsp");
            } else {
                req.getSession().setAttribute("error","username or password incorrect");
                resp.sendRedirect("/login.jsp");
            }
        } else {
            req.getSession().setAttribute("error","用户不存在");
            resp.sendRedirect("/login.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
