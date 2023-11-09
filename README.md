

## 实战: 超市订单管理系统



为了方便表述和理解, 在编写MVC三层对应的代码时, 使用自顶向下, 但在实际的项目中, 编写代码工作往往是自底向上的.



### 搭建项目框架

![未命名文件](超市管理系统/未命名文件.png)

#### 编写数据库操作的基础公共类DB

 

- 数据库配置文件，这是一个资源文件，应该创建在maven项目的resources文件中

  ```properties
  DRIVER=com.mysql.jdbc.Driver
  URL=jdbc:mysql://localhost:3307/smbms?useUnicode=true&characterEncoding=utf-8
  USERNAME=root
  PASSWORD=
  ```

  

- 使用静态代码块实现初始化参数

  该类为数据库访问工具类，提供了获取数据库连接、执行查询和更新等常用操作的方法。

  1. 类名：DB

  2. 属性：

  - DRIVER：String类型，表示数据库驱动名。
  - URL：String类型，表示数据库连接地址。
  - USERNAME：String类型，表示连接数据库的用户名。
  - PASSWORD：String类型，表示连接数据库的密码。

  3. 方法：

  - close：用于释放数据库连接、预处理语句和结果集资源。
  - getConnection：用于获取数据库连接。
  - executeQuery：用于执行查询操作，返回一个结果集。
  - executeUpdate：用于执行更新操作，返回受影响的行数。

  4. 方法的参数：

  - conn：Connection类型，表示数据库连接。
  - pstmt：PreparedStatement类型，表示预处理语句。
  - resSet：ResultSet类型，表示结果集。
  - query：String类型，表示要执行的SQL语句。
  - params：Object[]类型，表示SQL语句中的参数值。

  在类中还定义了一个静态代码块，该代码块在类加载时会被执行。静态代码块中的主要作用是读取配置文件中的数据库连接参数，并将其赋值给类的静态属性。具体实现如下：

  - 创建Properties对象。
  - 通过类加载器加载配置文件为字节输入流。
  - 使用Properties对象的load()方法加载字节输入流中的内容。
  - 从Properties对象中获取数据库连接参数，并将其赋值给类的静态属性。

  通过这种方式，可以在不修改Java代码的情况下，更改数据库连接参数，提高了代码的灵活性和可维护性。

  ```java
  public class DB {
      private static String DRIVER;
      private static String URL;
      private static String USERNAME;
      private static String PASSWORD;
      //静态代码块， 类加载的时候执行
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
          DRIVER = properties.getProperty("DRIVER");
          URL = properties.getProperty("URL");
          USERNAME = properties.getProperty("USERNAME");
          PASSWORD = properties.getProperty("PASSWORD");
      }
  }
  ```

- 编写数据库操作的公共方法

  该类为数据库访问工具类，提供了获取数据库连接、执行查询和更新等常用操作的方法。

  1. 类名：DB

  2. 属性：

  - DRIVER：String类型，表示数据库驱动名。
  - URL：String类型，表示数据库连接地址。
  - USERNAME：String类型，表示连接数据库的用户名。
  - PASSWORD：String类型，表示连接数据库的密码。

  3. 方法：

  - close：用于释放数据库连接、预处理语句和结果集资源。
  - getConnection：用于获取数据库连接。
  - executeQuery：用于执行查询操作，返回一个结果集。
  - executeUpdate：用于执行更新操作，返回受影响的行数。

  4. 方法的参数：

  - conn：Connection类型，表示数据库连接。
  - pstmt：PreparedStatement类型，表示预处理语句。
  - resSet：ResultSet类型，表示结果集。
  - query：String类型，表示要执行的SQL语句。
  - params：Object[]类型，表示SQL语句中的参数值。

  该类还包含一个静态代码块，用于初始化数据库连接参数，但代码块的内容未给出，需要进行补充。

  *同时需要注意，该类并没有实现单例模式，每次获取数据库连接都会重新创建一个新的连接，这可能会导致性能问题。*

```java
package util;
public class DB {
    private static String DRIVER;
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

  /*
      静态代码块， 类加载的时候执行
  */
    //释放链接资源
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet resSet) throws SQLException {
        if(resSet != null){
            resSet.close();
        }
        if(pstmt != null){
            pstmt.close();
        }
        if(conn != null) {
            conn.close();
        }
    }
    //获取数据库链接
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER);
        String url = URL;
        String username = USERNAME;
        String password = PASSWORD;
        // 2.连接数据库,代表数据库
        Connection connection = DriverManager.getConnection(url, username, password);
        return  connection;
    }
    //执行查询, 返回结果集
    public static ResultSet executeQuery(Connection conn,String query,Object[] params) throws SQLException, ClassNotFoundException{
        ResultSet resultSet = null;
        if(conn != null && !query.isEmpty() && params != null){
            //预处理
            PreparedStatement statement = conn.prepareStatement(query);
            for (int i = 0; i < params.length;i++){
                //设置SQL 语句中的参数
                if(params[i] instanceof String){
                    statement.setString(i+1,params[i].toString());
                } else if(params[i] instanceof Integer){
                    statement.setInt(i+1,Integer.parseInt(params[i].toString()));
                } else {
                    statement.setObject(i+1,params[i]);
                }

            }
            //执行
            resultSet = statement.executeQuery();
        }
        return resultSet;
    }
    //执行更新， 返回影响行数
    public static int executeUpdate(Connection conn,String query,Object[] params) throws SQLException {
        int affectedRows = 0;
        if(conn != null && !query.isEmpty() && params != null){
            //预处理
            PreparedStatement statement = conn.prepareStatement(query);
            for (int i = 0; i < params.length;i++){
                //设置SQL 语句中的参数
                if(params[i] instanceof String){
                    statement.setString(i+1,params[i].toString());
                } else if(params[i] instanceof Integer){
                    statement.setInt(i+1,Integer.parseInt(params[i].toString()));
                } else {
                    statement.setObject(i+1,params[i]);
                }
            }
            //执行
             affectedRows = statement.executeUpdate();
        }
        return affectedRows;
    }
}

```

### 登录功能实现



#### 前端页面编写

前端页面由kuang提供

```jsp
<%-- header--%>
<form class="loginForm" action="${pageContext.request.contextPath}/login.do" name="actionForm" id="actionForm" method="post">
  <div class="info">${error}</div>
  <div class="inputbox">
      <label>用户名：</label>
      <input type="text" class="input-text" id="userCode" name="userCode" placeholder="请输入用户名" required/>
  </div>
  <div class="inputbox">
      <label>密码：</label>
      <input type="password" id="userPassword" name="userPassword" placeholder="请输入密码" required/>
  </div>
  <div class="subBtn">
      <input type="submit" value="登录"/>
      <input type="reset" value="重置"/>
  </div>
</form>
<%-- footer--%>
```



#### 登录处理Servlet实现

在前端form表单的action属性值我们可以看到提交的URL, 所以, 我们需要编写对应的Servlet来处理这个请求

```jsp
action="${pageContext.request.contextPath}/login.do"
```

- 创建登录处理Servlet

  以下是一个名为“Login”的Java servlet类，用于处理HTTP GET请求。该类包括对“UserService”实例的引用，该实例用于从数据库中检索用户信息。

  在“doGet”方法中，servlet从HTTP请求中检索userCode和password参数。然后它创建一个“UserServiceImpl”类的新实例（该类实现了“UserService”接口），并调用其“findByUserCode”方法，以根据给定的用户代码从数据库中检索用户对象。

  如果用户对象不为null，则servlet检查用户密码是否与给定密码匹配。如果匹配，则servlet使用用户对象设置名为“USER_SESSION”的会话属性，并将用户重定向到“/jsp/frame.jsp”。如果密码不匹配，则servlet使用错误消息设置名为“error”的会话属性，并将用户重定向回“/login.jsp”。

  *请注意，此代码片段不完整，因为它缺少有关导入，它所依赖的接口和类，以及servlet配置的信息。*

  ```java
  public class Login extends HttpServlet {
      private UserService service;
      @Override
      protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
          String userCode = req.getParameter("userCode");
          String password = req.getParameter("userPassword");
          service = new UserServiceImpl();
          User user = null;
          user = service.findByUserCode(userCode);
          if(user != null) {
              if(user.getUserPassword().equals(password)) {
                  req.getSession().setAttribute(Constant.USER_SESSION,user);
                  resp.sendRedirect("/jsp/frame.jsp");
              } else {
                  req.getSession().setAttribute("error","username or password incorrect");
                  resp.sendRedirect("/login.jsp");
              }
          }
      }
  ```

- 映射 servlet

  这段代码是一个 servlet 配置的 XML 部分，它用于将请求的 URL 映射到特定的 servlet 上。这段配置指定了一个名为 "Login" 的 servlet，使用了完整的类名 `servlet.Login`，并将它映射到 URL "/login.do" 上。

  这意味着，当用户在浏览器中访问 "/login.do" 时，容器将调用 `servlet.Login` 的 service() 方法来处理该请求。映射 servlet 的操作是 web 应用的一部分，它在 web.xml 文件中进行配置。

  ```xml
  <servlet>
    <servlet-name>Login</servlet-name>
    <servlet-class>servlet.Login</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>Login</servlet-name>
    <url-pattern>/login.do</url-pattern>
  </servlet-mapping>
  ```

- 编写Service层代码

  该代码是一个 Java 类 `UserServiceImpl`，实现了一个接口 `UserService`。该类包含了两个方法：

  1. `findByUserCode(String userCode)`：查找数据库中对应用户的信息，该方法返回一个 `User` 对象。
  2. `updateUser(Integer id, String key, String value)`：更新数据库中用户的信息，该方法返回一个 `boolean` 类型的值，表示更新是否成功。

  在这个类中，私有成员变量 `dao` 是一个 `UserDao` 类型的变量，通过该变量调用 `UserDaoImpl` 类中的方法来访问数据库。在 `findByUserCode` 和 `updateUser` 方法中，都需要先通过 `dao` 实例化一个 `UserDaoImpl` 对象来调用相应的方法。

  ```java
  public class UserServiceImpl implements UserService {
      private UserDao dao;
      @Override
      public User findByUserCode(String userCode){
          dao = new UserDaoImpl();
          User user = dao.findByUserCode(userCode);
          return  user;
      }
      @Override
      public boolean updateUser(Integer id, String key, String value)  {
          dao = new UserDaoImpl();
          int i = 0;
          i = dao.updateUser(id, key, value);
         return i > 0 ? true : false;
      }
  }
  
  ```

- DAO层代码

  这段代码是一个Dao层的实现类，用于和数据库交互，提供了对User表的查询和更新方法。下面是代码的分析：

  - `findByUserCode(String userCode)` 方法：根据传入的用户名参数 `userCode` 查询对应的用户记录，并返回一个User对象。该方法使用了数据库连接池技术，在执行完查询后会关闭连接。方法中首先初始化参数，然后执行SQL语句查询结果集，遍历结果集将数据封装到User对象中，最后关闭连接和结果集，返回User对象。
  - `updateUser(Integer id, String key,String value)` 方法：根据传入的参数更新User表中的数据。该方法也使用了数据库连接池技术，在执行完更新后会关闭连接。方法中首先判断传入的参数是否为空，然后执行SQL语句更新数据，最后关闭连接，返回更新结果的影响行数。

  ```java
  public class UserDaoImpl implements UserDao{
      private User user;
      private Connection conn;
      private String sql;
      @Override
      public User findByUserCode(String userCode){
          //2. 初始化参数
          Object[] params = {userCode};
          ResultSet set;
          sql = "SELECT * FROM smbms_user Where userCode = ?";
          try {
              conn = DB.getConnection();
              set = DB.executeQuery(conn, sql,params);
              if(set != null){
                  user = new User();
                  while (set.next()) {
                      user.setUserPassword(set.getString("userPassword"));
                      //一大串赋值代码...
                      user.setWorkPicPath("workPicPath");
                  }
              } else {
                  return null;
              }
              //关闭链接和结果集
              DB.close(conn,null,set);
          } catch (SQLException e) {
              throw new RuntimeException(e);
          } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
          }
          return user;
      }
      @Override
      public int updateUser(Integer id, String key,String value) {
          int affectedRow = 0;
          if(id != null&& value != null && key != null){
              Connection conn = null;
              sql = "UPDATE smbms_user SET "+key+" = ? WHERE id = ? ";
              Object[] params = {value,id};
              try {
                  conn = DB.getConnection();
                  affectedRow = DB.executeUpdate(conn, sql, params);
                  DB.close(conn,null,null);
              } catch (SQLException e) {
                  throw new RuntimeException(e);
              } catch (ClassNotFoundException e) {
                  throw new RuntimeException(e);
              }
          }
          return affectedRow;
      }
  ```



### 密码修改实现



#### 前端页面

**JSP部分: 页面显示**

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/jsp/common/head.jsp" %>
<div class="right">
    <div class="location">
        <strong>你现在所在的位置是:</strong>
        <span>密码修改页面</span>
    </div>
    <div class="providerAdd">
        <form id="userForm" name="userForm" method="post" action="${pageContext.request.contextPath }/jsp/user.do">
            <input type="hidden" name="method" value="savepwd"/>
            <!--div的class 为error是验证错误，ok是验证成功-->
            <div class="info">${message}</div>
            <div class="">
                <label for="oldPassword">旧密码：</label>
                <input type="password" name="oldpassword" id="oldpassword" value="">
                <font color="red"></font>
            </div>
            <div>
                <label for="newPassword">新密码：</label>
                <input type="password" name="newpassword" id="newpassword" value="">
                <font color="red"></font>
            </div>
            <div>
                <label for="newPassword">确认新密码：</label>
                <input type="password" name="rnewpassword" id="rnewpassword" value="">
                <font color="red"></font>
            </div>
            <div class="providerAddBtn">
                <!--<a href="#">保存</a>-->
                <input type="button" name="save" id="save" value="保存" class="input-button">
            </div>
        </form>
    </div>
</div>
</section>
<%@include file="/jsp/common/foot.jsp" %>
<script type="text/javascript" src="${pageContext.request.contextPath }/statics/js/pwdmodify.js"></script>
```



这是一个用 JSP（JavaServer Pages）编写的网页代码，用于修改密码。页面包括一个表单，用户可以在其中输入旧密码、新密码和确认新密码。表单使用POST方法提交到服务器，并使用“/jsp/user.do” URL。 

页面包括一个消息区域，用于显示任何错误消息。页面还包括指向名为“pwdmodify.js”的 JavaScript 文件的链接。这个文件应该包含表单的客户端验证逻辑。

页面使用 JSP 表达式语言（EL）将动态内容插入到 HTML 中。例如，页面使用表达式 "${pageContext.request.contextPath}/statics/js/pwdmodify.js" 插入一个链接到 CSS 文件。"${pageContext.request.contextPath}" 表达式计算为 web 应用程序的上下文路径，用于构造 CSS 文件的完整 URL。 



**JavaScript 表单的客户端验证逻辑**

```js
var oldpassword = null;
var newpassword = null;
var rnewpassword = null;
var saveBtn = null;

$(function(){
   oldpassword = $("#oldpassword");
   newpassword = $("#newpassword");
   rnewpassword = $("#rnewpassword");
   saveBtn = $("#save");

   oldpassword.next().html("*");
   newpassword.next().html("*");
   rnewpassword.next().html("*");

   oldpassword.on("blur",function(){
      $.ajax({
         type:"GET",
         url:path+"/jsp/user.do",
         data:{method:"pwdmodify",oldpassword:oldpassword.val()},
         dataType:"json",
         success:function(data){
            if(data.result == "true"){//旧密码正确
               validateTip(oldpassword.next(),{"color":"green"},imgYes,true);
            }else if(data.result == "false"){//旧密码输入不正确
               validateTip(oldpassword.next(),{"color":"red"},imgNo + " 原密码输入不正确",false);
            }else if(data.result == "sessionerror"){//当前用户session过期，请重新登录
               validateTip(oldpassword.next(),{"color":"red"},imgNo + " 当前用户session过期，请重新登录",false);
            }else if(data.result == "error"){//旧密码输入为空
               validateTip(oldpassword.next(),{"color":"red"},imgNo + " 请输入旧密码",false);
            }
         },
         error:function(data){
            //请求出错
            validateTip(oldpassword.next(),{"color":"red"},imgNo + " 请求错误",false);
         }
      });


   }).on("focus",function(){
      validateTip(oldpassword.next(),{"color":"#666666"},"* 请输入原密码",false);
   });

   newpassword.on("focus",function(){
      validateTip(newpassword.next(),{"color":"#666666"},"* 密码长度必须是大于6小于20",false);
   }).on("blur",function(){
      if(newpassword.val() != null && newpassword.val().length > 5
         && newpassword.val().length < 20 ){
         validateTip(newpassword.next(),{"color":"green"},imgYes,true);
      }else{
         validateTip(newpassword.next(),{"color":"red"},imgNo + " 密码输入不符合规范，请重新输入",false);
      }
   });


   rnewpassword.on("focus",function(){
      validateTip(rnewpassword.next(),{"color":"#666666"},"* 请输入与上面一致的密码",false);
   }).on("blur",function(){
      if(rnewpassword.val() != null && rnewpassword.val().length > 5
         && rnewpassword.val().length < 20 && newpassword.val() == rnewpassword.val()){
         validateTip(rnewpassword.next(),{"color":"green"},imgYes,true);
      }else{
         validateTip(rnewpassword.next(),{"color":"red"},imgNo + " 两次密码输入不一致，请重新输入",false);
      }
   });


   saveBtn.on("click",function(){
      oldpassword.blur();
      newpassword.blur();
      rnewpassword.blur();
      if(
         oldpassword.attr("validateStatus") == "true" &&
          newpassword.attr("validateStatus") == "true"
         && rnewpassword.attr("validateStatus") == "true"){
         if(confirm("确定要修改密码？")){
            $("#userForm").submit();
         }
      }

   });
});
```

这是一个 JavaScript 代码段，它使用 jQuery 库为一个包含密码修改表单的网页添加了客户端验证逻辑。代码使用 AJAX 技术从服务器获取 JSON 格式的响应数据，以验证用户输入的旧密码是否正确，并在页面上显示相关提示信息。

代码中首先定义了四个变量，分别是旧密码、新密码、确认新密码和提交按钮的 jQuery 对象。然后使用 $() 函数将代码包装在一个回调函数中，确保代码在文档加载完成后才执行。回调函数内部做了以下工作：

1. 通过选择器获取页面上的旧密码输入框、新密码输入框、确认新密码输入框和提交按钮，并将它们的 jQuery 对象赋值给前面定义的四个变量。

2. 为旧密码输入框绑定 blur 事件处理函数，当输入框失去焦点时，使用 $.ajax() 函数向服务器发送 GET 请求，验证用户输入的旧密码是否正确，并根据服务器返回的响应数据显示相应的提示信息。在成功回调函数中，根据 data.result 的值判断旧密码是否正确，并通过 validateTip() 函数显示相应的提示信息。在失败回调函数中，显示请求出错的提示信息。

3. 为旧密码输入框绑定 focus 事件处理函数，当输入框获得焦点时，显示输入提示信息。

4. 为新密码输入框绑定 focus 和 blur 事件处理函数，当输入框获得焦点时，显示输入提示信息；当输入框失去焦点时，判断用户输入的新密码是否符合规范，如果符合则显示正确提示，否则显示错误提示。

5. 为确认新密码输入框绑定 focus 和 blur 事件处理函数，当输入框获得焦点时，显示输入提示信息；当输入框失去焦点时，判断用户输入的确认新密码是否与新密码一致，如果一致则显示正确提示，否则显示错误提示。

6. 为提交按钮绑定 click 事件处理函数，当按钮被点击时，先让旧密码、新密码和确认新密码输入框失去焦点，然后判断它们的验证状态是否都为 true，如果是，则显示确认修改密码的提示框，如果用户点击确定，则提交表单，将新密码保存到服务器上。



#### 编写处理Servlet



```java
public class UserDo extends HttpServlet {
    private UserService service;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getParameter("method");
        if("savepwd".equals(method)){
            savePwd(req, resp);
        } else if("pwdmodify".equals(method)){
            pwdModify(req, resp);
        }
    }
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
}
```

这段代码是一个Java Web应用程序中的Servlet，用于处理用户密码修改操作。

在doGet()方法中，根据请求参数"method"的值进行不同的操作。如果"method"的值是"savepwd"，则调用savePwd()方法，保存新密码；如果"method"的值是"pwdmodify"，则调用pwdModify()方法，用于检查旧密码是否正确。

在savePwd()方法中，首先从Session中获取当前用户的信息，然后获取新密码，并使用UserService接口的实现类UserServiceImpl来更新用户密码。如果更新成功，将信息存储在请求属性中，并移除Session中的用户信息，最后将请求转发到密码修改页面。

在pwdModify()方法中，首先获取当前用户的信息，然后获取旧密码。如果旧密码不为空，则将输入的旧密码与Session中存储的旧密码进行比较。如果输入的旧密码与Session中的旧密码相同，则返回前端JSON数据，表示旧密码正确；否则返回JSON数据，表示旧密码不正确。

**需要注意的是，在返回JSON数据之前，需要设置响应的Content-Type为"application/json"。**



##### 映射Servlet

```xml
  <servlet>
    <servlet-name>userDo</servlet-name>
    <servlet-class>servlet.UserDo</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>userDo</servlet-name>
    <url-pattern>/jsp/user.do</url-pattern>
  </servlet-mapping>
```



### 用户管理底层实现



### 分页实现

1. 导入分页支持

   ```java
   package util;
   public class PageSupport {
       //当前页码-来自于用户输入， 1表示第一页， 用于sql做limit查询 limit startPage, length
       private int currentPageNo = 1;
       //总数量（表）
       private int totalCount = 0;
       //页面容量,每一页展示多少数据
       private int pageSize = 0;
       //总页数: totalCount/pageSize（+1）
       private int totalPageCount = 1;
     
       public int getCurrentPageNo() {
           return currentPageNo;
       }
   
       public void setCurrentPageNo(int currentPageNo) {
           if(currentPageNo > 0){
               this.currentPageNo = currentPageNo;
           }
       }
   
       public int getTotalCount() {
           return totalCount;
       }
   
       public void setTotalCount(int totalCount) {
           if(totalCount > 0){
               this.totalCount = totalCount;
               //设置总页数
               this.setTotalPageCountByRs();
           }
       }
       public int getPageSize() {
           return pageSize;
       }
   
       public void setPageSize(int pageSize) {
           if(pageSize > 0){
               this.pageSize = pageSize;
           }
       }
   
       public int getTotalPageCount() {
           return totalPageCount;
       }
   
       public void setTotalPageCount(int totalPageCount) {
           this.totalPageCount = totalPageCount;
       }
   
       public void setTotalPageCountByRs(){
           if(this.totalCount % this.pageSize == 0){
               this.totalPageCount = this.totalCount / this.pageSize;
           }else if(this.totalCount % this.pageSize > 0){
               this.totalPageCount = this.totalCount / this.pageSize + 1;
           }else{
               this.totalPageCount = 0;
           }
       }
   }
   ```

   这段代码是一个用于分页的工具类 `PageSupport`。它包含了用于处理分页逻辑的属性和方法。

   让我们逐个分析这些属性和方法：

   1. `currentPageNo`（当前页码）: 保存当前页的页码，默认为1（表示第一页）。用户可以通过调用 `setCurrentPageNo()` 方法来设置当前页码。

   2. `totalCount`（总数量）: 表示总的数据量。用户可以通过调用 `setTotalCount()` 方法来设置总数量。

   3. `pageSize`（页面容量）: 每一页显示的数据数量。用户可以通过调用 `setPageSize()` 方法来设置页面容量。

   4. `totalPageCount`（总页数）: 表示总的页数，根据总数量和页面容量计算得出。用户可以通过调用 `getTotalPageCount()` 方法来获取总页数。

   5. `setTotalPageCountByRs()` 方法: 根据总数量和页面容量计算总页数，并将结果设置给 `totalPageCount` 属性。

   此外，对于一些属性的设置，代码中进行了一些限制条件的判断：

   - `currentPageNo` 和 `pageSize` 必须大于0，否则将不进行设置。

   - `totalCount` 必须大于0，否则将不进行设置，并且会将 `totalPageCount` 设置为0。

   这个工具类主要用于在分页查询中计算总页数和进行当前页码的设置。

- 前端

  **Header下用户管理二级菜单**

  ```jsp
  <li><a href="${pageContext.request.contextPath }/jsp/user.do?method=query">用户管理</a></li>
  ```

  

  点击这个菜单, 跳转到`/jsp/user.do?method=query`这个请求,` user.do`是对应的处理请求的Servlet, 问号后面是通过请求传递的参数, 为`method = query, Servlet`可以根据这个参数对请求的不同类型做对应的处理, 实现Servlet的复用. 

  

  **用户管理界面:** 

  ```jsp
      <div class="search">
          <form method="get" action="${pageContext.request.contextPath }/jsp/user.do">
              <input name="method" value="query" class="input-text" type="hidden">
              <span>用户名：</span>
              <input name="queryname" class="input-text" type="text" value="${queryUserName }">
  
              <span>用户角色：</span>
              <select name="queryUserRole">
                  <c:if test="${roleList != null }">
                      <option value="0">--请选择--</option>
                      <c:forEach var="role" items="${roleList}">
                          <option <c:if test="${role.id == queryUserRole }">selected="selected"</c:if>
                                  value="${role.id}">${role.roleName}</option>
                      </c:forEach>
                  </c:if>
              </select>
              <input type="hidden" name="pageIndex" value="1"/>
              <input value="查 询" type="submit" id="searchbutton">
          </form>
      </div>
      <!--用户-->
  /*
  	用户的mapping代码
  */
      <input type="hidden" id="totalPageCount" value="${totalPageCount}"/>
      <c:import url="rollpage.jsp">
          <c:param name="totalCount" value="${totalCount}"/>
          <c:param name="currentPageNo" value="${currentPageNo}"/>
          <c:param name="totalPageCount" value="${totalPageCount}"/>
      </c:import>
  </div>
  </section>
  ```

这段代码展示了一个搜索表单和与分页相关的部分。

1. `<div class="search">` 标签: 定义一个搜索的区域。
2. `<form>` 标签: 定义了一个表单，使用 GET 方法提交到 `${pageContext.request.contextPath}/jsp/user.do`。
3. `<input>` 标签: 设置了一个隐藏字段 `method`，值为 `"query"`，用于指定查询的方法。
4. `<input>` 标签: 创建了一个文本输入框，名称为 `queryname`，值为 `${queryUserName}`，显示了之前查询的用户名。
5. `<select>` 标签: 创建了一个下拉选择框，名称为 `queryUserRole`。使用了 JSTL 标签 `<c:if>` 进行条件判断，如果 `roleList` 不为 null，将会显示选项。
6. `<option>` 标签: 设置了一个默认选项 `<option value="0">--请选择--</option>`，然后使用 `<c:forEach>` 遍历 `roleList` 中的角色，并将角色的名称和 ID 设置为选项的值。使用 `<c:if>` 判断当前角色是否与 `queryUserRole` 相等，如果相等，则设置该选项为选中状态。
7. `<input>` 标签: 设置了一个隐藏字段 `pageIndex`，值为 1
8. `<input>` 标签: 设置了一个隐藏字段 `totalPageCount`，值为 `${totalPageCount}`。
9. `<c:import>` 标签: 导入了一个名为 `rollpage.jsp` 的页面，并传递了三个参数 `totalCount`、`currentPageNo` 和 `totalPageCount` 的值。

这段代码展示了一个搜索表单，用户可以输入用户名和选择用户角色进行查询。同时，还包含了与分页相关的部分，包括当前页码、总页数和总数量的显示。

我们观察这段代码, 这个form表单的一部分代码, 分别展示了提交的目的地 `${pageContext.request.contextPath }/jsp/user.do`  以及 一个隐藏域, 包含一个熟悉 `method` 值为 `query`, 是不是, 很熟悉, 没错, 这个Servlet和Header菜单进入管理用户界面所发起的请求是一样的, 也就是说, 是一个Servlet的相同处理过程. 为什么会这样呢?  这是因为查询和展示用户信息(未筛选时) 本质是一样的. 

```jsp
    <form method="get" action="${pageContext.request.contextPath }/jsp/user.do">
        <input name="method" value="query" class="input-text" type="hidden">
```



- DAO层

  `int getUserCount()`

  ```java
  public int getUserCount(Connection conn, String userName, int userRole) {
      int count = 0;
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT count(1) as count from smbms_user s, smbms_role r where s.userRole = r.id" );
      ArrayList<Object> list = new ArrayList<Object>();
      ResultSet rs =  null;
      if(userName != null){
          sql.append(" and userName like ? ");
          list.add("%"+userName+"%");
      }
      if(userRole > 0){
          sql.append(" and userRole like ? ");
          list.add("%"+userRole+"%");
      }
      System.out.println("sql----> "+sql.toString());
      try {
          rs = DB.executeQuery(conn, sql.toString(), list.toArray());
          while (rs.next()) {
              count = rs.getInt("count");
          }
          DB.close(null,null,rs);
      } catch (SQLException e) {
          throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
      }
      return count;
  }
  ```

  这段代码是一个方法 `getUserCount`，用于查询符合条件的用户数量。

  让我们逐行解释这段代码：

  1. `int getUserCount(Connection conn, String userName, int userRole)`: 这是一个方法声明，它接受一个数据库连接对象 `conn`、一个用户名 `userName` 和一个用户角色 `userRole` 作为参数，并返回一个整数表示符合条件的用户数量。

  2. `int count = 0;`: 初始化一个计数器变量 `count`，用于存储查询结果的数量。

  3. `StringBuffer sql = new StringBuffer();`: 创建一个 `StringBuffer` 对象 `sql`，用于构建 SQL 查询语句。

  4. `sql.append("SELECT count(1) as count from smbms_user s, smbms_role r where s.userRole = r.id");`: 将基本的查询语句添加到 `sql` 对象中，查询用户表 `smbms_user` 和角色表 `smbms_role`，并通过 `userRole` 字段进行关联。

  5. `ArrayList<Object> list = new ArrayList<Object>();`: 创建一个 `ArrayList` 对象 `list`，用于存储查询条件的参数值。

  6. `ResultSet rs =  null;`: 初始化一个 `ResultSet` 对象 `rs`，用于存储查询结果集。

  7. `if(userName != null) { ... }`: 如果 `userName` 不为 null，则将一个条件子句添加到 `sql` 对象中，使用 `LIKE` 运算符进行模糊匹配，并将匹配参数添加到 `list` 中。

  8. `if(userRole > 0) { ... }`: 如果 `userRole` 大于 0，则将另一个条件子句添加到 `sql` 对象中，同样使用 `LIKE` 运算符进行匹配，并将匹配参数添加到 `list` 中。

  9. `System.out.println("sql----> "+sql.toString());`: 打印输出最终生成的 SQL 查询语句，方便调试和查看。

  10. `rs = DB.executeQuery(conn, sql.toString(), list.toArray());`: 执行 SQL 查询，并将结果存储在 `rs` 中。`DB.executeQuery()` 是一个执行查询语句的方法，接受数据库连接对象、SQL 查询语句和参数值数组作为参数。

  11. `while (rs.next()) { ... }`: 遍历查询结果集，将每行的计数值存储到 `count` 变量中。

  12. `DB.close(null,null,rs);`: 关闭结果集、语句和连接对象，释放资源。`DB.close()` 是一个关闭数据库资源的方法，接受三个参数：结果集、语句和连接对象。

  13. `return count;`: 返回符合条件的用户数量。

  该方法的作用是查询数据库中符合给定用户名和用户角色条件的用户数量，并将结果返回。

  ==getUserList==

  ```java
  public List<User> getUserList(Connection conn, String userName, int userRole,int currPage, int pageSize) {
          List<User> userList = new ArrayList<>();
          if(conn != null) {
              //准备要使用的对象
              ArrayList<Object> params = new ArrayList<>();
              ResultSet rs = null;
              StringBuffer sql = new StringBuffer();
              try {
                  //编写sql
                  sql.append("SELECT * from smbms_user u ,smbms_role r Where u.userRole = r.id ");
                  //如果用户名不为空， 表示有用户名查询要求，追加限定条件
                  if(userName != null){
                      sql.append(" AND userName like ? ");
                      params.add("%"+userName+"%");
                  }
                  //同上...
                  if(userRole != 0){
                      sql.append(" and userRole = ?");
                      params.add(userRole);
                  }
                  sql.append(" ORDER BY u.creationDate DESC LIMIT ?,? ");//在sql最后追加一个排序和分页
                  //5
                  //1     5
                  //2     10
                  //3     15
                  currPage = (currPage-1)*pageSize;//减一的原因就是MYSQL分页的index从0开始
                  params.add(currPage);//从哪一个下标开始
                  params.add(pageSize);//从currentPageNo连续取几个
                  System.out.println("sql--->"+sql.toString());
                  //执行sql， 返回结果集, ⚠️需要把sql转化为字符串， params 转化为数组
                  rs = DB.executeQuery(conn,sql.toString(),params.toArray());
                  if(rs != null){
                      while (rs.next()){
                          User user = new User();
                          user.setUserPassword(rs.getString("userPassword"));
                          user.setId(rs.getInt("id"));
                          user.setUserCode(rs.getString("userCode"));
                          user.setUserName(rs.getString("userName"));
                          user.setAddress(rs.getString("address"));
                          user.setBirthday((Date) rs.getObject("birthday"));
                          user.setGender(rs.getInt("gender"));
                          user.setPhone(rs.getString("phone"));
                          user.setIdPicPath(rs.getString("idPicPath"));
                          user.setUserRole(rs.getInt("userRole"));
                          user.setCreatedBy(rs.getInt("createdBy"));
                          user.setCreationDate( rs.getDate("creationDate"));
                          user.setModifyBy(rs.getInt("modifyBy"));
                          user.setModifyDate(rs.getDate("modifyDate"));
                          user.setUserRoleName(rs.getString("roleName"));
                          userList.add(user);
  //                        System.out.println(user.toString());
                      }
                  }
                  DB.close(null,null,rs);
              } catch (SQLException e) {
                  throw new RuntimeException(e);
              } catch (ClassNotFoundException e) {
                  throw new RuntimeException(e);
              }
          } else {
              return null;
          }
          return userList;
      }
  ```

这段代码是一个方法 `getUserList`，用于根据给定的用户名、用户角色、当前页码和页面大小获取符合条件的用户列表。

让我们逐行解释这段代码：

1. `public List<User> getUserList(Connection conn, String userName, int userRole, int currPage, int pageSize)`: 这是一个方法声明，它接受一个数据库连接对象 `conn`、一个用户名 `userName`、一个用户角色 `userRole`、当前页码 `currPage` 和页面大小 `pageSize` 作为参数，并返回一个 `List<User>` 类型的用户列表。

2. `List<User> userList = new ArrayList<>();`: 创建一个 `ArrayList` 对象 `userList`，用于存储用户列表。

3. `if(conn != null) { ... } else { return null; }`: 检查数据库连接对象是否为 null，如果为 null，则直接返回 null。

4. `ArrayList<Object> params = new ArrayList<>();`: 创建一个 `ArrayList` 对象 `params`，用于存储查询条件的参数值。

5. `ResultSet rs = null;`: 初始化一个 `ResultSet` 对象 `rs`，用于存储查询结果集。

6. `StringBuffer sql = new StringBuffer();`: 创建一个 `StringBuffer` 对象 `sql`，用于构建 SQL 查询语句。

7. `sql.append("SELECT * from smbms_user u ,smbms_role r Where u.userRole = r.id ");`: 将基本的查询语句添加到 `sql` 对象中，查询用户表 `smbms_user`，和角色表`smbms_role `, 这里进行了多表的联合查询, 是为了给user(Bean)的RoleNa m e赋值, 以便前端使用. 

8. `if(userName != null) { ... }`: 如果 `userName` 不为 null，则将一个条件子句添加到 `sql` 对象中，使用 `LIKE` 运算符进行模糊匹配，并将匹配参数添加到 `params` 中。

9. `if(userRole != 0) { ... }`: 如果 `userRole` 不为 0，则将另一个条件子句添加到 `sql` 对象中，限定用户角色，并将角色参数添加到 `params` 中。

10. `sql.append(" ORDER BY u.creationDate DESC LIMIT ?,? ");`: 在查询语句的末尾追加一个排序和分页的部分，按照 `creationDate` 字段降序排列，并使用 `LIMIT` 限制查询结果的起始位置和数量。

11. `currPage = (currPage-1)*pageSize;`: 计算实际的起始位置，根据当前页码和页面大小，减一是因为 MYSQL 分页的索引从 0 开始。

12. `params.add(currPage);`: 将起始位置参数添加到 `params` 中。

13. `params.add(pageSize);`: 将页面大小参数添加到 `params` 中。

14. `System.out.println("sql--->"+sql.toString());`: 打印输出最终生成的 SQL 查询语句，方便调试和查看。

15. `rs = DB.executeQuery(conn, sql.toString(), params.toArray());`: 执行 SQL 查询，并将结果存储在 `rs` 中。`

​	==getRoleList==

```java
public List<Role> getRolelist(Connection conn, int id) {
    //prevent roleList
    List<Role> roleList = new ArrayList<>();
    if(conn != null){
        try {
            ResultSet rs = null;
            List<Object> params = new ArrayList<Object>();
            StringBuffer sql = new StringBuffer();
            //edit sql expression
            sql.append("SELECT * FROM smbms_role ");
            //输入0表查询全表， 输入>0 为sql拼接条件， 同时准备参数列表
            if(id > 0){
                sql.append(" WHERE id = ? ");
                params.add(id);
            }
            System.out.println("sql---->"+sql.toString());
            rs = DB.executeQuery(conn, sql.toString(), params.toArray());
            if(rs != null){
                while (rs.next()) {
                    //赋值
                    Role role = new Role();
                    role.setId(rs.getInt("id"));
                    role.setRoleCode(rs.getString("roleCode"));
                    role.setRoleName(rs.getString("roleName"));
                    role.setCreatedBy(rs.getInt("createdBy"));
                    role.setModifyDate(rs.getDate("creationDate"));
                    role.setCreationDate(rs.getDate("creationDate"));
                    role.setModifyBy(rs.getInt("modifyBy"));
                    roleList.add(role);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }else {
        return null;
    }
    return roleList;
}
```

- Service层

  ==getUserCount==

  ```java
  public int getUserCount(String userName, int UserRole) {
          int count = 0;
          Connection conn = null;
          try {
              conn = DB.getConnection();
              dao = new UserDaoImpl();
              count = dao.getUserCount(conn, userName, UserRole);
              DB.close(conn,null,null);
          } catch (SQLException e) {
              throw new RuntimeException(e);
          } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
          }
          return count;
      }
  ```

  ==getUserlist==

  ```java
  public List<User> getUserlist(String userName, int userRole,int currPage, int pageSize) {
      List<User> usersList = new ArrayList<>();
      try {
          Connection conn = DB.getConnection();
          dao = new UserDaoImpl();
          usersList = dao.getUserList(conn, userName, userRole,currPage, pageSize);
          DB.close(conn,null,null);
      } catch (SQLException e) {
          throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
      }
      return usersList;
  }
  ```

  ==getRoleList==

  ```java
  public List<Role> getRoleList(int id) {
      List<Role> roles = new ArrayList<>();
      try {
          Connection conn = DB.getConnection();
          RoleDao dao = new RoleDaoImpl();
          roles = dao.getRolelist(conn, id);
      } catch (SQLException e) {
          throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
      }
      return roles;
  }
  ```

- Servlet层

  类加载读取配置文件： 目前需要读取的数据有：PAGE_SIZE 用于设置页面大小

  ```java
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
  ```

  

  ```java
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      String method = req.getParameter("method");
      if("savepwd".equals(method)){
          savePwd(req, resp);
      } else if("pwdmodify".equals(method)){
          pwdModify(req, resp);
      } else if ("query".equals(method)) {
          query(req, resp);
      }
  }
  ```

  这段代码的作用是根据不同的`method`值调用不同的方法处理GET请求，实现不同的功能操作。

  

具体实现: 

```java
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
```

这段代码实现了一个查询用户的功能，以下是代码实现的步骤：

1. 从前端获取用户输入的参数：userName（用户名），userRole（用户角色），pageIndex（当前页码）。

2. 根据参数返回用户表和角色表，并将其设置为请求的属性，供jsp文件使用。

3. 准备前端需要的参数： queryUserName（查询用户名），queryUserRole（查询用户角色），roleList（角色列表），userList（用户列表）,totalPageCount（总页数），totalCount（查询到的用户数），currentPageNo（当前页）。

4. 查询用户数，并设置pageSupport的totalCount，currentPageNo，pageSize属性。

5. 查询用户和角色，并将其设置为请求的属性，供jsp文件使用。

6. 将前端需要的参数设置为请求的属性，供jsp文件使用。

7. 将请求转发到userlist.jsp文件，便于携带参数。

其中，代码中调用了UserService类和RoleService类的方法实现具体的查询操作。同时，代码中也对获取的参数进行了一些合法性的检验和设置默认值的操作。最终，将查询到的结果设置为请求的属性，通过转发的方式返回到jsp文件中供前端展示。



### 用户删除实现

在前端页面找到用户删除的按钮(锚), href是: `javascript:`, 

```jsp
<td>
    <span><a class="viewUser" href="javascript:;" userid=${user.id } username=${user.userName }><img src="${pageContext.request.contextPath }/statics/images/read.png" alt="查看" title="查看"/></a></span>
    <span><a class="modifyUser" href="javascript:;" userid=${user.id } username=${user.userName }><img src="${pageContext.request.contextPath }/statics/images/xiugai.png" alt="修改" title="修改"/></a></span>
    <span><a class="deleteUser" href="javascript:;" userid=${user.id } username=${user.userName }><img src="${pageContext.request.contextPath }/statics/images/schu.png" alt="删除" title="删除"/></a></span>
</td>
```

找到对应的javascript

```js
	$(".deleteUser").on("click",function(){
		userObj = $(this);
		changeDLGContent("你确定要删除用户【"+userObj.attr("username")+"】吗？");
		openYesOrNoDLG();
	});
```

当删除被点击后, js调用函数`userObj`, 接着我们去看这个函数

```js
function deleteUser(obj){
   $.ajax({
      type:"GET",
      url:path+"/jsp/user.do",
      data:{method:"deluser",uid:obj.attr("userid")},
      dataType:"json",
      success:function(data){
         if(data.delResult == "true"){//删除成功：移除删除行
            cancleBtn();
            obj.parents("tr").remove();
         }else if(data.delResult == "false"){//删除失败
            changeDLGContent("对不起，删除用户【"+obj.attr("username")+"】失败");
         }else if(data.delResult == "notexist"){
            changeDLGContent("对不起，用户【"+obj.attr("username")+"】不存在");
         }
      },
      error:function(data){
         changeDLGContent("对不起，删除失败");
      }
   });
}
```

这段代码是一个 JavaScript 函数，它使用 jQuery 的 `$.ajax` 方法发送一个 GET 请求到服务器。以下是对代码的分析：

1. `function deleteUser(obj)`：这是一个命名为 `deleteUser` 的函数，它接受一个参数 `obj`，表示要删除的用户对象。

2. `$.ajax`：这是 jQuery 提供的一个函数，用于发送 AJAX 请求到服务器。

3. AJAX 请求的配置选项包括：
   - `type: "GET"`：指定请求类型为 GET。
   - `url: path + "/jsp/user.do"`：指定请求的 URL 地址，其中 `path` 是一个变量或常量，表示服务器路径。
   - `data: {method: "deluser", uid: obj.attr("userid")}`：指定请求发送的数据，包括 `method` 和 `uid` 两个参数。`method` 的值为 `"deluser"`，`uid` 的值通过 `obj.attr("userid")` 获取。
   - `dataType: "json"`：指定预期的响应数据类型为 JSON。

4. `success`：这是一个成功回调函数，在服务器成功响应时被调用。它接受一个参数 `data`，表示从服务器返回的数据。函数内部根据返回的数据执行不同的操作：
   - 如果 `data.delResult` 的值为 `"true"`，表示删除成功。函数调用 `cancleBtn()` 取消按钮，并移除 `obj` 对应的父级 `<tr>` 行。
   - 如果 `data.delResult` 的值为 `"false"`，表示删除失败。函数调用 `changeDLGContent` 修改对话框的内容，显示删除失败的提示信息。
   - 如果 `data.delResult` 的值为 `"notexist"`，表示用户不存在。函数调用 `changeDLGContent` 修改对话框的内容，显示用户不存在的提示信息。

5. `error`：这是一个错误回调函数，在请求发生错误时被调用。它接受一个参数 `data`，表示错误信息。函数调用 `changeDLGContent` 修改对话框的内容，显示删除失败的提示信息。

这段代码用于通过 AJAX 请求向服务器发送删除用户的请求，并根据服务器返回的结果执行相应的操作。成功时移除对应的行，失败时显示错误提示信息。



通过这些信息,我们可以得知如何编写我们的后台程序

1. 编写对应的Servlet, 根据method值不同, 调用delUser函数
2. 编写请求处理函数delUser
3. delUser调用Service删除用户, 并返回结果
   1. 如果删除用户成功, 返回true或者受影响行数为1, 通过响应向前端返回数据delResult = true
   2. 如果Service返回false或者受影响行数为0, 向前端返回delResult = no exist, 表示删除的用户不存在. 
4. 出现异常则返回delResult = false(在这里是用户权限不够时返回会false)

```java
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
```

接下来, 我们还需要编写对应的Service层以及Dao层代码, 由于service实际上并没有做什么事, 这里只展示Dao层代码

```java
public int delUser(Connection conn, int id) {
    int affectedRow = 0;
    if(conn != null){
        try {
            String sql = "Delete from smbms_user where id = ?";
            Object[] params = {id};
            affectedRow = DB.executeUpdate(conn, sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    return affectedRow;
}
```

这段代码及其简单, 只需要写一段sql, 然后交给DB去执行就好了, DB这个类会预处理sql, 以及给占位符赋值, 最后执行sql返回结果.

### 查看用户实现

和删除用户类似, 找到对应的js请求

```js
$(".viewUser").on("click",function(){
   //将被绑定的元素（a）转换成jquery对象，可以使用jquery方法
   var obj = $(this);
   window.location.href=path+"/jsp/user.do?method=view&uid="+ obj.attr("userid");
});
```

编写Servlet

```java
protected void view(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
    String uid = req.getParameter("uid");
    service = new UserServiceImpl();
    User user = service.findById(Integer.parseInt(uid));
    req.setAttribute("user", user);
    req.getRequestDispatcher(req.getContextPath()+"/jsp/userview.jsp").forward(req, resp);
}
```

编写dao层, service层实现

```java
public User findById(int id) {
    //2. 初始化参数
    Object[] params = {id};
    ResultSet set;
    sql = "SELECT * FROM smbms_user,smbms_role Where smbms_user.userRole = smbms_role.id and smbms_user.id = ?";
    try {
        conn = DB.getConnection();
        set = DB.executeQuery(conn, sql,params);
        if(set != null){
            user = new User();
            while (set.next()) {
                user.setUserRoleName(set.getString("roleName"));
                user.setUserPassword(set.getString("userPassword"));
                user.setId(set.getInt("id"));
                user.setUserCode(set.getString("userCode"));
                user.setUserName(set.getString("userName"));
                user.setAddress(set.getString("address"));
                user.setBirthday((Date) set.getObject("birthday"));
                user.setGender(set.getInt("gender"));
                user.setPhone(set.getString("phone"));
                user.setIdPicPath(set.getString("idPicPath"));
                user.setUserRole(set.getInt("userRole"));
                user.setCreatedBy(set.getInt("createdBy"));
                user.setCreationDate( set.getDate("creationDate"));
                user.setModifyBy(set.getInt("modifyBy"));
                user.setModifyDate(set.getDate("modifyDate"));
                user.setWorkPicPath("workPicPath");
                System.out.println(user.toString());
            }
        } else {
            return null;
        }
        //关闭链接和结果集
        DB.close(conn,null,set);
    } catch (SQLException e) {
        throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
    }
    return user;
}
```



### 修改用户实现



重复重复再重复....



```js
$(".modifyUser").on("click",function(){
   var obj = $(this);
   window.location.href=path+"/jsp/user.do?method=modify&uid="+ obj.attr("userid");
});
```

```java
protected void modify(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException{
    String uid = req.getParameter("uid");

    service = new UserServiceImpl();
    User user = service.findById(Integer.parseInt(uid));
    req.setAttribute("user", user);
    req.getRequestDispatcher(req.getContextPath()+"/jsp/usermodify.jsp").forward(req, resp);
}
```

和之前不同的是, 这次修改用户指向一个新的jsp页面, 也就是修改用户的页面. 也就是说, 删除用户的请求提交以后, 我们需要把这个请求转发到`/jsp/usermodify.jsp`这个URL对应的jsp页面去, 由于这个jsp页面需要修改前的user数据, 我们需要查询对应的id,得到user数据后把数据传递给`usermodify.jsp`这个jsp页面, jsp页面需要这些数据来向用户展示修改前的数据. 下面我们去看这个jsp页面



```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/jsp/common/head.jsp"%>
<div class="right">
    <div class="location">
        <strong>你现在所在的位置是:</strong>
        <span>用户管理页面 >> 用户修改页面</span>
    </div>
    <div class="providerAdd">
        <form id="userForm" name="userForm" method="post" action="${pageContext.request.contextPath }/jsp/user.do">
            <input type="hidden" name="method" value="modifyexe">
            <input type="hidden" name="uid" value="${user.id }"/>
            <div>
                <label for="userName">用户名称：</label>
                <input type="text" name="userName" id="userName" value="${user.userName }">
                <font color="red"></font>
            </div>
            <div>
                <label >用户性别：</label>
                <select name="gender" id="gender">
                    <c:choose>
                        <c:when test="${user.gender == 1 }">
                            <option value="1" selected="selected">男</option>
                            <option value="2">女</option>
                        </c:when>
                        <c:otherwise>
                            <option value="1">男</option>
                            <option value="2" selected="selected">女</option>
                        </c:otherwise>
                    </c:choose>
                </select>
            </div>
            <div>
                <label >出生日期：</label>
                <input type="text" Class="Wdate" id="birthday" name="birthday" value="${user.birthday }"
                       readonly="readonly" onclick="WdatePicker();">
                <font color="red"></font>
            </div>

            <div>
                <label >用户电话：</label>
                <input type="text" name="phone" id="phone" value="${user.phone }">
                <font color="red"></font>
            </div>
            <div>
                <label >用户地址：</label>
                <input type="text" name="address" id="address" value="${user.address }">
            </div>
            <div>
                <label >用户角色：</label>
                <!-- 列出所有的角色分类 -->
                <input type="hidden" value="${user.userRole}" id="rid" />
                <select name="userRole" id="userRole"></select>
                <font color="red"></font>
            </div>
            <div class="providerAddBtn">
                <input type="button" name="save" id="save" value="保存" />
                <input type="button" id="back" name="back" value="返回"/>
            </div>
        </form>
    </div>
</div>
</section>
<%@include file="/jsp/common/foot.jsp" %>
<script type="text/javascript" src="${pageContext.request.contextPath }/statics/js/usermodify.js"></script>
```

先观察表单提交的目的地, `{pageContext.request.contextPath }/jsp/user.do`, 对应一个Servlet, 但是这里并没有提交的按钮, 但是我发现这里有一个`usermodify.js`的脚本文件, 定位到这个脚本文件

```js
var userName = null;
var birthday = null;
var phone = null;
var userRole = null;
var saveBtn = null;
var backBtn = null;

$(function(){
   userName = $("#userName");
   birthday = $("#birthday");
   phone = $("#phone");
   userRole = $("#userRole");
   saveBtn = $("#save");
   backBtn = $("#back");
   
   userName.next().html("*");
   birthday.next().html("*");
   phone.next().html("*");
   userRole.next().html("*");
   
   
   $.ajax({
      type:"GET",//请求类型
      url:path+"/jsp/user.do",//请求的url
      data:{method:"getrolelist"},//请求参数
      dataType:"json",//ajax接口（请求url）返回的数据类型
      success:function(data){//data：返回数据（json对象）
         if(data != null){
            var rid = $("#rid").val();
            userRole.html("");
            var options = "<option value=\"0\">请选择</option>";
            for(var i = 0; i < data.length; i++){
               if(rid != null && rid != undefined && data[i].id == rid ){
                  options += "<option selected=\"selected\" value=\""+data[i].id+"\" >"+data[i].roleName+"</option>";
               }else{
                  options += "<option value=\""+data[i].id+"\" >"+data[i].roleName+"</option>";
               }
               
            }
            userRole.html(options);
         }
      },
      error:function(data){//当访问时候，404，500 等非200的错误状态码
         validateTip(userRole.next(),{"color":"red"},imgNo+" 获取用户角色列表error",false);
      }
   });
   
   
   userName.on("focus",function(){
      validateTip(userName.next(),{"color":"#666666"},"* 用户名长度必须是大于1小于10的字符",false);
   }).on("blur",function(){
      if(userName.val() != null && userName.val().length > 1 
            && userName.val().length < 10){
         validateTip(userName.next(),{"color":"green"},imgYes,true);
      }else{
         validateTip(userName.next(),{"color":"red"},imgNo+" 用户名输入的不符合规范，请重新输入",false);
      }
      
   });
   
   birthday.on("focus",function(){
      validateTip(birthday.next(),{"color":"#666666"},"* 点击输入框，选择日期",false);
   }).on("blur",function(){
      if(birthday.val() != null && birthday.val() != ""){
         validateTip(birthday.next(),{"color":"green"},imgYes,true);
      }else{
         validateTip(birthday.next(),{"color":"red"},imgNo + " 选择的日期不正确,请重新输入",false);
      }
   });
   
   phone.on("focus",function(){
      validateTip(phone.next(),{"color":"#666666"},"* 请输入手机号",false);
   }).on("blur",function(){
      var patrn=/^(13[0-9]|15[0-9]|18[0-9])\d{8}$/;
      if(phone.val().match(patrn)){
         validateTip(phone.next(),{"color":"green"},imgYes,true);
      }else{
         validateTip(phone.next(),{"color":"red"},imgNo + " 您输入的手机号格式不正确",false);
      }
   });
   
   userRole.on("focus",function(){
      validateTip(userRole.next(),{"color":"#666666"},"* 请选择用户角色",false);
   }).on("blur",function(){
      if(userRole.val() != null && userRole.val() > 0){
         validateTip(userRole.next(),{"color":"green"},imgYes,true);
      }else{
         validateTip(userRole.next(),{"color":"red"},imgNo+" 请重新选择用户角色",false);
      }
      
   });
   
   saveBtn.on("click",function(){
      userName.blur();
      phone.blur();
      birthday.blur();
      userRole.blur();
      if(userName.attr("validateStatus") == "true" 
         && phone.attr("validateStatus") == "true"
         && birthday.attr("validateStatus") == "true"
         && userRole.attr("validateStatus") == "true"){
         if(confirm("是否确认要提交数据？")){
            $("#userForm").submit();
         }
      }
   });
   
   backBtn.on("click",function(){
      //alert("modify: "+referer);
      if(referer != undefined 
         && null != referer 
         && "" != referer
         && "null" != referer
         && referer.length > 4){
       window.location.href = referer;
      }else{
         history.back(-1);
      }
   });
});
```

**直接丢给chatGPT分析:**

- 代码的核心部分是以下部分：

  ```javascript
  $.ajax({
    type: "GET",
    url: path + "/jsp/user.do",
    data: { method: "getrolelist" },
    dataType: "json",
    success: function(data) {
      if (data != null) {
        var rid = $("#rid").val();
        userRole.html("");
        var options = "<option value=\"0\">请选择</option>";
        for (var i = 0; i < data.length; i++) {
          if (rid != null && rid != undefined && data[i].id == rid) {
            options += "<option selected=\"selected\" value=\"" + data[i].id + "\" >" + data[i].roleName + "</option>";
          } else {
            options += "<option value=\"" + data[i].id + "\" >" + data[i].roleName + "</option>";
          }
        }
        userRole.html(options);
      }
    },
    error: function(data) {
      validateTip(userRole.next(), { "color": "red" }, imgNo + " 获取用户角色列表error", false);
    }
  });
  ```

  这部分代码使用了 `$.ajax` 函数发起一个 GET 请求，请求的目标 URL 是 `path + "/jsp/user.do"`。请求的数据参数中包含了一个名为 `method` 的参数，其值为 `"getrolelist"`。预期的响应数据类型为 JSON。

  在成功回调函数中，首先判断返回的数据 `data` 是否为非空。如果不为空，则继续执行后续逻辑。

  通过获取 `$("#rid").val()` 获取一个表示角色的 ID 值。

  接下来，清空 `userRole` 元素的内容，然后使用循环遍历返回的数据数组 `data`。对于每个数据项，根据其 ID 值和当前选中的角色 ID (`rid`) 进行比较。如果两者相等，说明该选项应该被选中，否则为普通选项。在循环过程中，动态生成了一段 HTML 字符串 `options`，包含了所有的选项。

  最后，将生成的选项字符串设置为 `userRole` 元素的 HTML 内容，从而在页面上显示这些选项。

  在错误回调函数中，对获取用户角色列表出错的情况进行处理，调用 `validateTip` 函数显示相应的错误提示信息。

  该部分代码的核心功能是通过 AJAX 请求获取用户角色列表，并根据返回的数据动态生成下拉选项，用于用户在表单中选择角色。

  

  所以, 我们需要一个servlet来处理这段ajax请求, 这个请求需要一个角色表

,整理前端提供的数据, 以及前端需要的数据

- 提供的数据:

  -  `name="method" value="modifyexe"`
  -  `name="uid" value="${user.id }`
  -  `name="userName" value="${user.userName }`
  -  `name="gender"`
  -  `name="birthday" value="${user.birthday }`
  -  `name="phone" value="${user.phone }`
  -  `name="address"  value="${user.address }`
  -  `name="userRole"`

- 需要的数据

  - `name="birthday" value="${user.birthday }`
  - `name="phone" value="${user.phone }`
  - `name="address"  value="${user.address }`
  - `name="uid" value="${user.id }`
  - `name="userName" value="${user.userName }`
  - 角色表

  

**下面我们来编写servlet**

1. 通过method的值对请求做不同处理, 这里有三个个请求, 分别是

   1. `method=modify` 
   2. `method = getrolelist`
   3. `metho=modidyexe`

2. `method=modify` 这个请求当用户点击修改按钮时被触发, 目的是导航到用户修改页面, 以及返回修改用户的信息

3. `method = getrolelist` 这个请求当用户跳转到用户修改页面触发, 目的是通过 AJAX 请求获取用户角色列表，并根据返回的数据动态生成下拉选项，用于用户在表单中选择角色。

4. `metho=modidyexe`, 这个请求当用户提交修改信息的时候触发, 修改数据库内容

   一个复用的Servlet, 用来响应对用户表进行操作的请求. (实际上组合查询了角色表)

   ```java
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
           //    //user.do?method=ucexist&userCode=**
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
   ```

   ​       ==getRoleList(req, resp)== 

   ```java
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
   ```

   ​        ==modify(req, resp);==

   ```java
   protected void modify(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException{
       String uid = req.getParameter("uid");
   
       service = new UserServiceImpl();
       User user = service.findById(Integer.parseInt(uid));
       req.setAttribute("user", user);
       req.getRequestDispatcher(req.getContextPath()+"/jsp/usermodify.jsp").forward(req, resp);
   }
   ```

   ​        ==modifyExe(req, resp);==

   ```java
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
   ```

   剩下的编写Service以及Dao层实现对数据库的操作, 整个功能就实现了. 这里不在叙述.

   

   ## 尾巴

   ​	到这里, 这个项目的搭建算是告一段落了. 在这个项目中, 我基本上是从零开始搭建. 说来惭愧, 现在大三下下学期已经过半, 学校的JavaEE也结课两周. 我对整个课程却没什么印象, 这当然也和我根本没去上几次课有关系, 除了实验课我基本就不去了. 这也不是我不想学, 只是觉得要是跟着老师学我会更糟. 老师上课完全不顾学生, 只将电脑的idea投屏到投影仪上便能自顾自的讲一节课. 投影仪本来亮度就不高, idea字体又小,完全不知道怎么听. 听过几次我就是在不想去了. 

   ​	加之又有考研的计划, 基本都呆在图书馆学习高数和计算机网络了, JavaEE基本上没怎么碰了, 连分组的实践也是完全丢下给小组其他同学了(小组实践能不能从大学消失啊). 小组其他几个也都是混子, 这留下我的旁边床铺的室友了, 这让我感觉实在很惭愧. 当然最后的结果也是压根没人管这个小组实践了, 直到deadline的前一天晚上, 这事才被重新提起. 当然也只能从github匆匆找一个应付交作业了. 

   ​	那么如果总结这半学期学到了什么的话, 只剩下把计算机网络算是完完整整的学完了, 不过这也是补了之前欠下的债了. 高数我已经接近一个月没有看过了, 现在也已经没什么印象了, 不知道考研要怎么办了.  我也已经做好了二战的准备了. 

   ​	写这个项目的契机, 也是学校又莫名其妙多了个课程设计, 相比学校的什么破实验实践, 我很喜欢学校的课程设计, 上次操作系统的实践设计是关于xv6的, 那次让我对操作系统的理解提升了很多, 现在我都不舍得删掉电脑里的qemu模拟器. 不过这和老师没什么关系, 老师基本上只是把课题扔在那, 我们自己独立完成. 但也同时我有了更多的时间去完成这个课程设计, 没有其他课程任务的干扰, 我可以全心全意的投入到这个课程设计中去. 这很符合我自己的学习习惯, 我更喜欢把一个课程学完了, 或者是一个阶段的任务完成了, 才去开始下一项学习计划. 相比于去课堂上听无意义的课程, 这种把时间留给学生自己的做法, 在这个大学老师基本上起不到什么帮助的破大学里, 显然是更值得的. 

   ​	说回写这个项目的契机, 也是学校的课程设计. 到这个时候所以的课程也已经结束了, 我本以为可以完完全全的开始自己的学习计划的时候, 来这么一个任务一开始我是蛮不开心的. 这次的任务是写一个图书管理系统, 狂神这个课程写了一个超市管理系统, 但是这些什么学生管理系统啊, 图书管理系统之类的, 都没区别, 随便改一改就可以. 

   ​	这次的课程设计分为了几个小组, 和其他小组不同的是, 我们小组的老师要求每个同学独立完成, 这让我很喜这个老师的做法. 而我们每天上课的内容也就是在自习室写自己的项目罢了, 除了早上八点要爬上11楼外, 我没什么抱怨的. 

   ​	接下来的学习计划, 我准备把CSAPP的内容补上, 这个从大一开始便一直计划要学的内容, 到现在也没开始, 我真是废物啊. 如果可以穿越回大一到话, 当然是带着现在的记忆穿越回去, 不然结局也是淹没在大学的无意义的课程中了. 不过带着记忆的话, 与其幻想这个不切实际的梦, 这个幻想其本质和我接下把自己缺失的补上是一样的.

   ​	即使我对自己的能力不满意, 但我也确实没什么可以抱怨的, 毕竟这就是我, 这才是我. 除了暑假的时间顾着玩了, 大学的生活我的学习步伐一直都是踏踏实实的. 我不得不说我已经够努力了, 靠着我一个人, 在一个远离家乡的城市. 这个指望不上老师的地方, 我做到现在我已经和满意了, 从大一到现在的我, 以及学会了太多太多, 完完全全成长了很多, 学到很多. 剩下的遗憾, 留下没有一个好的环境, 一个良师益友引导我. 不知道这个遗憾能不能在研究生生活中补上呢, 至少现在先考上一个好多大学, 补完这些遗憾吧. 加油啊, 自己.

   

   

   

   以下是改为图书管理系统的字段替换: 

   ```
   供应商表-> 图书
   ISBN：->provider.proCode
   书名：->provider.proName
   作者：->provider.proContact
   定价：->provider.proPhone 
   出版社->provider.proFax 
   分类：->provider.proDesc
   订单管理->借阅记录管理
   未归还 -> queryIsPayment == 1 
   已归还-> queryIsPayment == 2 
   用户：->queryProductName
   借阅记录编码->billCode
   图书 ->providerName
   金额->totalPrice
   ```

