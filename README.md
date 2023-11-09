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
