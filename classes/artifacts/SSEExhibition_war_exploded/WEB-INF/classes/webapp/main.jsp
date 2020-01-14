<%@ page import="java.util.Set" %><%--
  Created by IntelliJ IDEA.
  User: Ltc
  Date: 2020/1/11
  Time: 23:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>登陆页面</title>
    <script language="javascript">
        function SearchIEX2lev(){
            if(document.login.search.value===""){
                alert("查询条件不完整");
            }else{
                document.login.action="IEX2levServlet?method=search";
                document.login.submit();
            }
        }
    </script>
</head>
<body>
<%! Set<String> result=null; %>
<div align="center">
    <p align="center">IEX2lev</p>
    <form name="login" method="post">
        <table>
            <tr>
                <td>查询条件:</td>
                <td><input name="search" type="text"/></td>
            </tr>
            <tr></tr>
            <tr>
                <td colspan="2" align="center">
                    <input name="submit" type="submit" value="查询" onclick="SearchIEX2lev();"/>
                    <input name="reset" type="reset" value="重置"/>
                </td>
            </tr>
        </table>
    </form>

    <% result = (Set<String>)request.getAttribute("result");
        if(result!=null){
            out.print("<span style=\"color:red\">"+result+"</span>");
        }else {
            out.print("<span style=\"color:red\">"+"没有符合条件的文件"+"</span>");
        }
    %>
</div>
</body>
</html>
