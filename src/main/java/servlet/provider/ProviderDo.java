package servlet.provider;

import Bean.Bill;
import Bean.Provider;
import Bean.Role;
import Bean.User;
import com.alibaba.fastjson.JSONArray;
import service.Pro.ProService;
import service.Pro.ProServiceImpl;
import service.bill.BillService;
import service.bill.BillServiceIml;
import service.role.RoleServiceImpl;
import service.user.UserServiceImpl;
import util.Constant;
import util.DB;
import util.PageSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

public class ProviderDo extends HttpServlet {
    private static int PAGE_SIZE;
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
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getParameter("method");
        if("query".equals(method)) {
            query(req,resp);
        } else if("getproviderlist".equals(method)){
            getProList(req,resp);
        } else if("modifysave".equals(method)){
            modifySave(req, resp);
        } else if("modify".equals(method)){
            modify(req, resp);
        }else if("view".equals(method)){
            view(req, resp);
        } else if("delprovider".equals(method)){
            delPro(req, resp);
        } else if("add".equals(method)){
            addPro(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
    protected void query(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1. 从前端获取用户输入的参数 queryname -> userName，queryUserRole -> userRole,pageIndex
        String queryProCode = req.getParameter("queryProCode");
        String queryProName = req.getParameter("queryProName");
        String pageIndex = req.getParameter("pageIndex");
        // 检验参数的合法性
        if( queryProCode == null || queryProCode.equals("")) {
            queryProCode ="";
        }
        if(queryProName == null || queryProName.equals("")){
            queryProName = "";
        }
        if(pageIndex == null || pageIndex.equals("")){
            pageIndex = "1";
        }
        //3. 准备前端需要的参数： queryProCode，queryProName ,providerList
        List<Provider> providerList;
        // 以及下列参数： totalPageCount（总页数），totalCount（查询到的用户数），currentPageNo（当前页）
        // 这些参数的获取需要先调用userService.getUserCount查询用户数， 然后调用PageSupport提供分页支持
        ProService proService = new ProServiceImpl();
        int totalCount = proService.getProCount(queryProCode, queryProName);
        PageSupport pageSupport = new PageSupport();
        pageSupport.setPageSize(PAGE_SIZE);
        pageSupport.setTotalCount(totalCount);
        pageSupport.setTotalPageCountByRs();
        pageSupport.setCurrentPageNo(Integer.parseInt(pageIndex));
        int totalPageCount = pageSupport.getTotalPageCount();
//        // 查询用户
        providerList = proService.getProList(queryProCode,queryProName ,Integer.parseInt(pageIndex), PAGE_SIZE);
//        //2. 根据参数返回用户表和角色表
        req.setAttribute("providerList",providerList);
//        //3. 返回用户需要的参数
        req.setAttribute("queryProCode",queryProCode);
        req.setAttribute("queryProName",queryProName);
        req.setAttribute("totalPageCount", totalPageCount);
        req.setAttribute("totalCount",totalCount);
        req.setAttribute("currentPageNo",pageIndex);
//        //4. 转发到jsp/providerlist.jsp， 要使用转发而不是重定向， 便于携带参数
        req.getRequestDispatcher("providerlist.jsp").forward(req,resp);
    }
    protected void getProList(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{
        resp.setContentType("application/json");
        ProService proService = new ProServiceImpl();
        List<Provider> proList = proService.getProList(null, null, 1, PAGE_SIZE);
        String jsonString = JSONArray.toJSONString(proList);
        PrintWriter writer = resp.getWriter();
        writer.write(jsonString);
        writer.flush();
    }
    protected void modify(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{
        String proId = req.getParameter("proid");
        ProService service = new ProServiceImpl();
        Provider pro = service.getProById(Integer.parseInt(proId));
        req.setAttribute("provider", pro);
        req.getRequestDispatcher("/jsp/providermodify.jsp").forward(req,resp);
    }
    protected void modifySave(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{
        User o = (User) req.getSession().getAttribute(Constant.USER_SESSION);
        String uid = req.getParameter("id");
        String proCode = req.getParameter("proCode");
        String proName = req.getParameter("proName");
        String proContact = req.getParameter("proContact");
        String proPhone = req.getParameter("proPhone");
        String proAddress = req.getParameter("proAddress");
        String proFax = req.getParameter("proFax");
        String proDesc = req.getParameter("proDesc");
        Map<String, Object> map = new HashMap<>();
        map.put("proCode",proCode);
        map.put("proName",proName);
        map.put("proContact",proContact);
        map.put("proPhone",proPhone);
        map.put("proAddress",proAddress);
        map.put("proFax",proFax);
        map.put("proDesc",proDesc);
        map.put("modifyBy",o.getId());
        map.put("modifyDate",new Date());
        ProService service = new ProServiceImpl();
        for (String key : map.keySet()) {
            boolean res = service.updatePro( key, map.get(key),Integer.parseInt(uid));
        }
        resp.sendRedirect(req.getContextPath()+"/jsp/provider.do?method=query");
    }
    protected void view(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("proid");
        System.out.println("id--->"+id);
        ProService service = new ProServiceImpl();
        Provider pro = service.getProById(Integer.parseInt(id));
        req.setAttribute("provider",pro);
        System.out.println(pro.toString());
        req.getRequestDispatcher("/jsp/providerview.jsp").forward(req, resp);
    }
    protected void delPro(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String proId = req.getParameter("proid");
        HashMap<String, String> hashMap = new HashMap<>(1);
        if(proId == null || proId.equals("")){
            hashMap.put("delResult","false");
        } else {
            ProService serviceIml = new ProServiceImpl();
            boolean flag = serviceIml.delPro(Integer.parseInt(proId));
            if(flag) {
                hashMap.put("delResult","true");
            } else {
                hashMap.put("delResult","notexist");
            }
        }
        resp.setContentType("application/json");
        PrintWriter writer = resp.getWriter();
        writer.write(JSONArray.toJSONString(hashMap));
        writer.flush();
    }
    protected void addPro(HttpServletRequest rep, HttpServletResponse resp) throws IOException,ServletException {
        String proCode = rep.getParameter("proCode");
        String proName = rep.getParameter("proName");
        String proContact = rep.getParameter("proContact");
        String proPhone = rep.getParameter("proPhone");
        String proAddress = rep.getParameter("proAddress");
        String proFax = rep.getParameter("proFax");
        String proDesc = rep.getParameter("proDesc");
        User user = (User) rep.getSession().getAttribute(Constant.USER_SESSION);
        Provider pro = new Provider(
                proCode,proName,proDesc,proContact,proPhone,proAddress,proFax,user.getId(),new Date()
        );
        ProService serviceIml = new ProServiceImpl();
        boolean res = serviceIml.addPro(pro);
        if(res) {
            resp.sendRedirect("/jsp/provider.do?method=query");
        } else {
            rep.setAttribute("error", "添加失败");
            rep.getRequestDispatcher("/jsp/provideradd.jsp");
        }


    }
}
