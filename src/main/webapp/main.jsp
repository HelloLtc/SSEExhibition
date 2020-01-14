<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %><%--
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
    <title>查询页面</title>
    <script language="javascript">
        function SearchIEXlev(){
            if(document.login.levsearch.value===""){
                alert("查询条件不完整");
            }else{
                document.login.action="IEX2levServlet?method=search";
                document.login.submit();
            }
        }
    </script>
    <script language="javascript">
        function SearchIEXZMF(){
            if(document.login.zmfsearch.value===""){
                alert("查询条件不完整");
            }else{
                document.login.action="IEXZMFServlet?method=search";
                document.login.submit();
            }
        }
    </script>
</head>
<body>
<%! Set<String> levresult=null; %>
<%! List<String> zmfresult=null;%>
<div align="center">
    <form name="login" method="post">
        <table>
            <tr>
                <td>IEX2lev查询条件:</td>
                <td><input name="levsearch" type="text"/></td>
            </tr>
            <tr></tr>
            <tr>
                <td colspan="2" align="center">
                    <input name="submit" type="submit" value="查询" onclick="SearchIEXlev();"/>
                    <input name="reset" type="reset" value="重置"/>
                </td>
            </tr>
            <br>
            <br>
            <tr>
                <td>IEXZMF查询条件:</td>
                <td><input name="zmfsearch" type="text"/></td>
            </tr>
            <tr></tr>
            <tr>
                <td colspan="2" align="center">
                    <input name="submit" type="submit" value="查询" onclick="SearchIEXZMF();"/>
                    <input name="reset" type="reset" value="重置"/>
                </td>
            </tr>
        </table>
    </form>

    <% zmfresult = (List<String>)request.getAttribute("ZmfResult");
        if(zmfresult!=null){
            out.print("<span style=\"color:red\">"+zmfresult+"</span>");
        }%>

</div>
</body>
</html>
