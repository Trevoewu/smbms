package servlet.bill;

import Bean.Bill;
import Bean.Provider;
import Bean.User;
import com.alibaba.fastjson.JSONArray;
import dao.bill.BillDaoImpl;
import service.Pro.ProService;
import service.Pro.ProServiceImpl;
import service.bill.BillService;
import service.bill.BillServiceIml;
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

public class BillDo extends HttpServlet {
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
        if("query".equals(method)){
            query(req, resp);
        }
        if("getproviderlist".equals(method)){
            getProList(req,resp);
        }
        if("modifysave".equals(method)){
            modifySave(req, resp);
        }
        if("modify".equals(method)){
            modify(req, resp);
        }
        if("view".equals(method)){
            view(req, resp);
        }
        if("delbill".equals(method)){
            delBill(req, resp);
        } if("add".equals(method)){
            addUser(req, resp);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    protected void query(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1. 从前端获取用户输入的参数 queryname -> userName，queryUserRole -> userRole,pageIndex
        String queryProviderId = req.getParameter("queryProviderId");
        String queryProductName = req.getParameter("queryProductName");
        String queryIsPayment = req.getParameter("queryIsPayment");
        String pageIndex = req.getParameter("pageIndex");
        // 检验参数的合法性
        if( queryProviderId == null || queryProviderId.equals("")) {
            queryProviderId = "-1";
        }
        if(queryProductName == null || queryProductName.equals("")){
            queryProductName = "";
        }
        if(pageIndex == null || pageIndex.equals("")){
            pageIndex = "1";
        } if(queryIsPayment == null || queryIsPayment.equals("")){
            queryIsPayment = "0";
        }
        //3. 准备前端需要的参数： queryProviderId，queryProductName ,providerList,billList
        List<Bill> billList;
        List<Provider> providerList;
        // 以及下列参数： totalPageCount（总页数），totalCount（查询到的用户数），currentPageNo（当前页）
        // 这些参数的获取需要先调用userService.getUserCount查询用户数， 然后调用PageSupport提供分页支持
        BillService billService = new BillServiceIml();
        ProService proService  = new ProServiceImpl();
        int totalCount = billService.getBillCount(queryProductName, Integer.parseInt(queryProviderId));
        PageSupport pageSupport = new PageSupport();
        pageSupport.setPageSize(PAGE_SIZE);
        pageSupport.setTotalCount(totalCount);
        pageSupport.setTotalPageCountByRs();
        pageSupport.setCurrentPageNo(Integer.parseInt(pageIndex));
        int totalPageCount = pageSupport.getTotalPageCount();
////        // 查询用户
        billList = billService.getBillList(queryProductName,Integer.parseInt(queryProviderId) ,Integer.parseInt(pageIndex), PAGE_SIZE,Integer.parseInt(queryIsPayment));
        List<Provider> proList = proService.getProList(null, null, Integer.parseInt(pageIndex), PAGE_SIZE);
////        //2. 根据参数返回用户表和角色表
        req.setAttribute("billList",billList);
        req.setAttribute("providerList",proList);
////        //3. 返回用户需要的参数
        req.setAttribute("queryProviderId",queryProviderId);
        req.setAttribute("queryProductName",queryProductName);
        req.setAttribute("totalPageCount", totalPageCount);
        req.setAttribute("totalCount",totalCount);
        req.setAttribute("currentPageNo",pageIndex);
        req.setAttribute("queryIsPayment",queryIsPayment);
////        //4. 转发到jsp/providerlist.jsp， 要使用转发而不是重定向， 便于携带参数
        req.getRequestDispatcher("/jsp/billlist.jsp").forward(req,resp);
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
        String billId = req.getParameter("billid");
        BillServiceIml billServiceIml = new BillServiceIml();
        Bill bill = billServiceIml.getBllById(Integer.parseInt(billId));
        req.setAttribute("bill", bill);
        req.getRequestDispatcher("/jsp/billmodify.jsp").forward(req,resp);
    }
    protected void modifySave(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{
        User o = (User) req.getSession().getAttribute(Constant.USER_SESSION);
        String uid = req.getParameter("id");
        String billCode = req.getParameter("billCode");
        String productName = req.getParameter("productName");
        String productUnit = req.getParameter("productUnit");
        String productCount = req.getParameter("productCount");
        String totalPrice = req.getParameter("totalPrice");
        String providerId = req.getParameter("providerId");
        String isPayment = req.getParameter("isPayment");
        Map<String, Object> map = new HashMap<>();
        map.put("billCode",billCode);
        map.put("productName",productName);
        map.put("productUnit",productUnit);
        DecimalFormat df = new DecimalFormat("0.00");
        df.setMaximumFractionDigits(2);
        map.put("productCount",df.format(Float.parseFloat(productCount)));
        map.put("totalPrice",df.format(Float.parseFloat(totalPrice)));
        map.put("isPayment",Integer.parseInt(isPayment));
        map.put("providerId",Integer.parseInt(providerId));
        map.put("modifyBy",o.getId());
        map.put("modifyDate",new Date());
        BillService service = new BillServiceIml();
        for (String key : map.keySet()) {
            boolean res = service.updateBill(Integer.parseInt(uid), key, map.get(key));
        }
        resp.sendRedirect(req.getContextPath()+"/jsp/bill.do?method=query");
    }
    protected void view(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("billid");
        System.out.println("id--->"+id);
        BillService service = new BillServiceIml();
        Bill bill = service.findBillById(Integer.parseInt(id));
        req.setAttribute("bill",bill);
        System.out.println(bill.toString());
        req.getRequestDispatcher("/jsp/billview.jsp").forward(req, resp);
    }
    protected void delBill(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String billId = req.getParameter("billid");
        HashMap<String, String> hashMap = new HashMap<>(1);
        if(billId == null || billId.equals("")){
            hashMap.put("delResult","false");
        } else {
            BillServiceIml serviceIml = new BillServiceIml();
            boolean flag = serviceIml.delBillById(Integer.parseInt(billId));
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
    protected void addUser(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
        String billCode = request.getParameter("billCode");
        String productName = request.getParameter("productName");
        String productUnit = request.getParameter("productUnit");
        String productCount = request.getParameter("productCount");
        String totalPrice = request.getParameter("totalPrice");
        String providerId = request.getParameter("providerId");
        String isPayment = request.getParameter("isPayment");
        User user = (User) request.getSession().getAttribute(Constant.USER_SESSION);
        Bill bill = new Bill(
                billCode,productName,productUnit,
                new BigDecimal(productCount),new BigDecimal(totalPrice),Integer.parseInt(isPayment),
                Integer.parseInt(providerId),user.getId(),new Date()
        );
        BillServiceIml serviceIml = new BillServiceIml();
        boolean res = serviceIml.addBill(bill);
        if(res) {
            response.sendRedirect("/jsp/bill.do?method=query");
        } else {
            request.setAttribute("error", "添加失败");
            request.getRequestDispatcher("/jsp/billadd.jsp");
        }


    }
}
