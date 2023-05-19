package servlet.user;

import Bean.Role;
import Bean.User;
import com.alibaba.fastjson.JSONArray;
import service.role.RoleServiceImpl;
import service.user.UserService;
import service.user.UserServiceImpl;
import util.Constant;
import util.DB;
import util.PageSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserDo extends HttpServlet {
    private UserService service;
private static int PAGE_SIZE;
/*
    类加载读取配置文件： 目前需要读取的数据有：
    PAGE_SIZE 用于设置页面大小

 */
static {
    //读取配置文件
    //1、创建properties对象
    Properties properties = new Properties();
    //2、通过类加载器加载资源文件为字节输入流
    InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties");
    try {
        properties.load(in);
    } catch (IOException e) {
        e.printStackTrace();
    }
    PAGE_SIZE = Integer.parseInt(properties.getProperty("PAGE_SIZE"));
}
/*
    一个复用的Servlet更具请求传递的方法(method)属性值的不同
    进行不同的处理
    method：savepwd -> 表示保存用户密码， 用于用户修改密码时修改数据库的值
    method: pwdmodify -> 修改密码请求， ajax利用这个请求来验证密码的合法性（是否有旧密码相同）
    method: query -> 查询请求，前端需要对数据进行展示时发起这个请求, 主要为用户列表的展示
 */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getParameter("method");
        if("savepwd".equals(method)){
            savePwd(req, resp);
        } else if("pwdmodify".equals(method)){
            pwdModify(req, resp);
        } else if ("query".equals(method)) {
            query(req, resp);
        } else if ("add".equals(method)) {
            addUser(req, resp);
        } else if ("getrolelist".equals(method)) {
            getRoleList(req, resp);
        } else if ("ucexist".equals(method)) {
            //ajax后台验证--userCode是否已存在
            //		//user.do?method=ucexist&userCode=**
            isUserCodeExit(req, resp);
        } else if ("view".equals(method)) {
            view(req, resp);
        } else if ("deluser".equals(method)) {
            delUser(req, resp);
        } else if ("modifyexe".equals(method)) {
            modifyExe(req, resp);
        } else if ("modify".equals(method)) {
            modify(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
    /*
    保存新密码
    提取方法，实现Servlet复用
    result: 转发到当前目录(jsp｜Servlet）
     */
    protected void savePwd(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //拿到USer Session , 用于得到用户id
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute(Constant.USER_SESSION);
        String newPwd = req.getParameter("newpassword");
        System.out.println("用户输入的：new password: "+newPwd);
        System.out.println("Session password: "+user.getUserPassword()+", id: "+ user.getId());
        Integer id = user.getId();
        boolean flag;
        //method 值为  savepwd, 表示用户执行修改密码操作
        if(newPwd != null){
            service = new UserServiceImpl();
            flag = service.updateUser(id, "userPassword", newPwd);
            //修改成功
            if(flag){
                System.out.println("修改成功");
                req.setAttribute("message","密码修改成功， 请重新登录");
                //  移除Session
                session.removeAttribute(Constant.USER_SESSION);
            } else {
                System.out.println("密码修改失败");
                req.setAttribute("message","密码修改失败");
            }
        } else {
            req.setAttribute("message","新密码格式错误");
        }
        //转发到当前页面, 不用重定向而使用转发是为了传统参数（attribute) 通知前端密码修改结果（by attribute message）
        req.getRequestDispatcher("/jsp/pwdmodify.jsp").forward(req,resp);
    }
    /*
    由ajax发起的密码查询请求， 用户进行密码修改时触发
    放回前端json， ajax用于比对旧密码正确性,
     */
    protected void pwdModify(HttpServletRequest req, HttpServletResponse resp){
        HttpSession session = req.getSession();
        Object user = session.getAttribute(Constant.USER_SESSION);
        Map<String, String> map = new HashMap<String, String>();
        if(user != null){
            String password = ((User) user).getUserPassword();
            String inputPwd = req.getParameter("oldpassword");
            if(inputPwd != null && inputPwd.length() > 0){
                //旧密码正确
                if(inputPwd.equals(password)){
                    map.put("result","true");
                } else {
                    //旧密码不正确
                    map.put("result","false");
                }
            } else {
                //密码输入为空
                map.put("result","error");
            }
        } else {
            //session timeout
            map.put("result","sessionerror");
        }
        //返回前端json数据
        resp.setContentType("application/json");
        try {
            PrintWriter writer = resp.getWriter();
            writer.write(JSONArray.toJSONString(map));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    protected void query(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1. 从前端获取用户输入的参数 queryname -> userName，queryUserRole -> userRole,pageIndex
        String userName = req.getParameter("queryname");
        String userRole = req.getParameter("queryUserRole");
        String pageIndex = req.getParameter("pageIndex");
        // 检验参数的合法性
        if( userName == null){
            userName ="";
        }
        if(userRole == null || userRole.equals("")){
            userRole = "0";
        }
        if(pageIndex == null || pageIndex.equals("")){
            pageIndex = "1";
        }
        //3. 准备前端需要的参数： queryUserName，queryUserRole ,roleList，userList,
        String queryUserName = userName;
        String queryUserRole = userRole;
        List<Role> roleList;
        List<User> userList;
        // 以及下列参数： totalPageCount（总页数），totalCount（查询到的用户数），currentPageNo（当前页）
        // 这些参数的获取需要先调用userService.getUserCount查询用户数， 然后调用PageSupport提供分页支持
        UserServiceImpl userService = new UserServiceImpl();
        RoleServiceImpl roleService = new RoleServiceImpl();
        int totalCount = userService.getUserCount(userName, Integer.parseInt(userRole));
        PageSupport pageSupport = new PageSupport();
        pageSupport.setPageSize(PAGE_SIZE);
        pageSupport.setTotalCount(totalCount);
        pageSupport.setTotalPageCountByRs();
        pageSupport.setCurrentPageNo(Integer.parseInt(pageIndex));
        int totalPageCount = pageSupport.getTotalPageCount();
        // 查询用户和角色
        userList = userService.getUserlist(userName, Integer.parseInt(userRole), Integer.parseInt(pageIndex), PAGE_SIZE);
        roleList = roleService.getRoleList(0);
        //2. 根据参数返回用户表和角色表
        req.setAttribute("roleList",roleList);
        req.setAttribute("userList",userList);
        //3. 返回用户需要的参数
        req.setAttribute("queryUserName",queryUserName);
        req.setAttribute("queryUserRole",queryUserRole);
        req.setAttribute("totalPageCount", totalPageCount);
        req.setAttribute("totalCount",totalCount);
        req.setAttribute("currentPageNo",pageIndex);
        //4. 转发到jsp/userlist.jsp， 要使用转发而不是重定向， 便于携带参数
        req.getRequestDispatcher("userlist.jsp").forward(req,resp);
    }
    protected void addUser(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 从前端获取参数: userCode userName password gender birthday phone address userRole
        String userCode = req.getParameter("userCode");
        String userName = req.getParameter("userName");
        String password = req.getParameter("userPassword");
        String gender = req.getParameter("gender");
        String birthday = req.getParameter("birthday");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String userRole = req.getParameter("userRole");
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // 2. 封装数据
        User user = new User(userCode, userName, password, Integer.parseInt(gender),
                date, phone, address,Integer.parseInt(userRole));
        User o = (User) req.getSession().getAttribute(Constant.USER_SESSION);
        user.setCreatedBy(o.getId());
        user.setCreationDate(new Date());
        //3.
        service = new UserServiceImpl();
        System.out.println(user.toString());
        boolean res = service.addUser(user);
        if(res) {
            resp.sendRedirect(req.getContextPath()+"/jsp/user.do?method=query");
        } else {
            req.getRequestDispatcher("user.add").forward(req,resp);
        }

    }
/*
    ajax 发起的查询角色请求
    返回前端json数据， 用于ajax获取用户添加页面的角色名
 */
    protected void getRoleList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RoleServiceImpl roleService = new RoleServiceImpl();
        List<Role> roleList = roleService.getRoleList(0);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        writer.write(JSONArray.toJSONString(roleList));
        writer.flush();
        writer.close();
    }
    protected void isUserCodeExit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        //1. 从前端获取参数
        String userCode = req.getParameter("userCode");
        System.out.println("userCode-->"+userCode);
        //2. 准备前端需要的数据， 数据类型json, 属性值userCode: exit
        Map<String, String> map = new HashMap<String, String>();
        if(userCode == null || userCode.length() == 0 || userCode.equals("")){
            map.put("userCode", "null");
        } else {
            service = new UserServiceImpl();
            User user = service.findByUserCode(userCode);
            if (user != null && user.getUserCode() != null && user.getUserCode().equals(userCode)) {
                map.put("userCode", "exist");
            } else {
                map.put("userCode","not exist");
            }
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        writer.write(JSONArray.toJSONString(map));
        writer.flush();
        writer.close();
    }
    protected void view(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String uid = req.getParameter("uid");
        service = new UserServiceImpl();
        User user = service.findById(Integer.parseInt(uid));
        req.setAttribute("user", user);
        req.getRequestDispatcher(req.getContextPath()+"/jsp/userview.jsp").forward(req, resp);
    }
    protected void delUser(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        //1. 获取用户角色， 如果用户不是系统管理员， 则没有删除用户的权限
        User o = (User) req.getSession().getAttribute(Constant.USER_SESSION);
        Map<String, String> map = new HashMap<>();
        if(o.getUserRole() == 1){
            String uid = req.getParameter("uid");
            service = new UserServiceImpl();
            boolean res = service.delUser(Integer.parseInt(uid));
            if(res){
                map.put("delResult", "true");
            } else {
                map.put("delResult", "notexist");
            }
        } else {
            map.put("delResult","false");
        }
        //封装json数据， 并写到前端
        PrintWriter writer = resp.getWriter();
        writer.write(JSONArray.toJSONString(map));
        writer.flush();

    }
    protected void modify(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException{
        String uid = req.getParameter("uid");

        service = new UserServiceImpl();
        User user = service.findById(Integer.parseInt(uid));
        req.setAttribute("user", user);
        req.getRequestDispatcher(req.getContextPath()+"/jsp/usermodify.jsp").forward(req, resp);
    }
    protected void modifyExe(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        // 1. 从前端获取参数: userCode userName password gender birthday phone address userRole
        Map<String, Object> map = new HashMap<>();
        String uid = req.getParameter("uid");
        map.put("id",uid);
        map.put("userName",req.getParameter("userName"));
        map.put("gender",Integer.parseInt(req.getParameter("gender")));
        map.put("phone",req.getParameter("phone"));
        map.put("address",req.getParameter("address"));
        map.put("userRole",Integer.parseInt(req.getParameter("userRole")));

        String birthday = req.getParameter("birthday");
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
            map.put("birthday",date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // 2. 封装数据
        User o = (User) req.getSession().getAttribute(Constant.USER_SESSION);
        map.put("modifyBy",o.getId());
        Date modifyDate = new Date();
        map.put("modifyDate",new Date());
        //3. 调用service
        service = new UserServiceImpl();
        for (String key : map.keySet()) {
            boolean res = service.updateUser(Integer.parseInt(uid), key, map.get(key));
        }
        resp.sendRedirect(req.getContextPath()+"/jsp/user.do?method=query");
    }

    public static void main(String[] args) {
        System.out.println(new Date().toString());
    }
}

