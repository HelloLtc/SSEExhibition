<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%@ page import="org.crypto.sse.IEX2Lev" %>
<%--
  Created by IntelliJ IDEA.
  User: Ltc
  Date: 2020/1/11
  Time: 23:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script src="assets/main.js"></script>
<script src="jslib/jquery-3.4.1.min.js"></script>
<script>
    $(document).ready(function () {
        let plainFiles = null;
        let btnUploadPlain = $('#btn-upload-file');
        let btnLevEncrypt = $('#btn-levencrypt-file');
        let btnZmfEncrypt = $('#btn-zmfencrypt-file');
        let btnEliminatePlain = $('#btn-eliminate-file');
        let btnLevSearch = $('#btn-levsearch-file');
        let btnZMFSearch = $('#btn-zmfsearch-file');
        $('#input-file').change(function () {
            plainFiles = this.files;
            console.log("input plainFile change.");
            // console.log(plainFiles);
        });

        $(btnUploadPlain).on('click', function () {
            if (!plainFiles) {
                alert('请添加需要上传文件');
                return false;
            }
            let formData = new FormData();
            for (let i = 0; i < plainFiles.length; i += 1) {
                let file = plainFiles[i];
                formData.append(file.name, file);
            }
            $.ajax({
                url: './UploadServlet',
                method: 'POST',
                cache: false,
                contentType: false,
                processData: false,
                data: formData,
                success: function (data) {
                    if (data.status === true) {
                        alert(`文件: ${data.fList}上传成功.`);
                    } else {
                        alert('文件上传失败: ' + data.msg);
                    }
                    console.log('Plain Files upload success');
                    //  ReInit
                    plainFiles = null;
                },
                fail: function (err) {
                    alert('文件上传失败: ');
                    console.log('plain file(s) upload failed: ' + err);
                }
            })
        });

        $(btnLevEncrypt).on('click', function (){
            let val = document.login.genkey.value;
            if(val === null||val=== ""){
                alert("请输入密码");
                return 0;
            }
            $.ajax({
                url: './GenKeyServlet',
                method: 'POST',
                async: false,    //或false,是否异步
                data: {
                    genkey:val
                },
                success: function (data) {
                    SK0 = data.key0;
                    SK1 = data.key1;
                },
                fail: function (err) {
                    alert('密钥生成失败 ');
                }
            });
            $.ajax({
                url: './IEX2levServlet',
                method: 'POST',
                async: true,    //或false,是否异步
                data: {
                    method:'add',listSK0:SK0,listSK1:SK1
                },
                timeout: 5000,    //超时时间
                dataType: 'json',    //返回的数据格式：json/xml/html/script/jsonp/text
                success: function (data) {
                    if (data.status === true) {
                        alert('文件加密成功');
                    } else {
                        alert('文件加密失败');
                    }
                },
                fail: function (err) {
                    alert('文件加密失败 ');
                }
            })
        });

        $(btnEliminatePlain).on('click', function (){
            $.ajax({
                url: './EliFileServlet',
                type: 'POST', //GET
                async: true,    //或false,是否异步
                data: {
                    //想要传输过去的数据 key：value，另一个页面通过 key接收value的值
                },
                timeout: 5000,    //超时时间
                dataType: 'json',    //返回的数据格式：json/xml/html/script/jsonp/text
                success:function(data){//data是成功后，接收的返回值
                    if (data.filestatus === true) {
                        alert(data.filestatus+'文件删除成功');
                    } else {
                        alert(data.filestatus+'文件删除失败');
                    }
                }
            })
        });

        $(btnLevSearch).on('click', function (){
            let val = document.login.genkey.value;
            let levval = document.login.levsearch.value;
            if(val === null||val=== ""){
                alert("请输入密码");
                return 0;
            }
            if(levval === null||levval === ""){
                alert("请输入查询条件");
                return 0;
            }
            $.ajax({
                url: './GenKeyServlet',
                method: 'POST',
                async: false,    //或false,是否异步
                data: {
                    genkey:val
                },
                success: function (data) {
                    SK0 = data.key0;
                    SK1 = data.key1;
                },
                fail: function (err) {
                    alert('密钥生成失败 ');
                }
            });
            $.ajax({
                url: './IEX2levServlet',
                type: 'POST', //GET
                async: false,    //或false,是否异步
                data: {
                    method:'search',listSK0:SK0,listSK1:SK1,search:levval//想要传输过去的数据 key：value，另一个页面通过 key接收value的值
                },
                timeout: 5000,    //超时时间
                dataType: 'json',    //返回的数据格式：json/xml/html/script/jsonp/text
                success:function(data){//data是成功后，接收的返回值
                    if(data.status === true)
                        alert("查询结果："+data.msg);
                    else
                        alert("无结果");
                }
            })
        });

        $(btnZMFSearch).on('click', function (){

            let val = document.login.genkey.value;
            if(val === null||val=== ""){
                alert("请输入密码");
                return 0;
            }
            let zmfval = document.login.zmfsearch.value;
            if(zmfval === null||zmfval === ""){
                alert("请输入查询条件");
                return 0;
            }
            $.ajax({
                url: './GenKeyServlet',
                method: 'POST',
                async: false,    //或false,是否异步
                data: {
                    genkey:val
                },
                success: function (data) {
                    SK0 = data.key0;
                    SK1 = data.key1;
                },
                fail: function (err) {
                    alert('密钥生成失败 ');
                }
            });
            $.ajax({
                url: './IEXZMFServlet',
                type: 'POST', //GET
                async: false,    //或false,是否异步
                data: {
                    method: 'search', listSK0: SK0, listSK1: SK1, zmfsearch: zmfval//想要传输过去的数据 key：value，另一个页面通过 key接收value的值
                },
                timeout: 5000,    //超时时间
                dataType: 'json',    //返回的数据格式：json/xml/html/script/jsonp/text
                success: function (data) {//data是成功后，接收的返回值
                    if (data.zmfstatus === true)
                        alert("查询结果："+data.msg);
                    else
                        alert("无结果");
                },
                fail: function (err) {
                    alert('查找失败 ');
                }
            });
        });

        $(btnZmfEncrypt).on('click', function (){
            let val = document.login.genkey.value;
            if(val === null||val=== ""){
                alert("请输入密码");
                return 0;
            }
            $.ajax({
                url: './GenKeyServlet',
                method: 'POST',
                async: false,    //或false,是否异步
                data: {
                    genkey:val
                },
                success: function (data) {
                    SK0 = data.key0;
                    SK1 = data.key1;
                },
                fail: function (err) {
                    alert('密钥生成失败 ');
                }
            });
            $.ajax({
                url: './IEXZMFServlet',
                type: 'POST', //GET
                async: true,    //或false,是否异步
                data: {
                    method:'add',listSK0:SK0,listSK1:SK1//想要传输过去的数据 key：value，另一个页面通过 key接收value的值
                },
                timeout: 5000,    //超时时间
                dataType: 'json',    //返回的数据格式：json/xml/html/script/jsonp/text
                success:function(data){//data是成功后，接收的返回值
                    if(data.status === true)
                        alert("ZMF加密成功");
                    else
                        alert("ZMF加密失败");
                }
            })
        })
    });
</script>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>查询页面</title>
</head>
<body>
<div align="center">
    <form name="login" method="post">
        <table>
            <tr>
                <td>Key:</td>
                <td><input name="genkey" type="text"/></td>
            </tr>
            <tr></tr>
            <br>
            <br>
            <tr>
                <td>IEX2lev查询条件:</td>
                <td><input name="levsearch" type="text"/></td>
            </tr>
            <tr>
                <td>IEXZMF查询条件:</td>
                <td><input name="zmfsearch" type="text"/></td>
            </tr>

            <br>
            <br>
        </table>
        <div class="input-group-prepend">
            <button id="btn-levsearch-file">IEX2lev查询</button>
        </div>
        <div class="input-group-prepend">
            <button id="btn-zmfsearch-file">ZMF2lev查询</button>
        </div>
    </form>


    <div class="input-group-prepend">
        <button id="btn-upload-file">上传
        </button>
    </div>
    <div class="input-group-prepend">
        <button id="btn-levencrypt-file">IEX2lev加密
        </button>
    </div>
    <div class="input-group-prepend">
        <button id="btn-zmfencrypt-file">ZMF2lev加密
        </button>
    </div>
    <div class="input-group-prepend">
        <button id="btn-eliminate-file">清除文件内容
        </button>
    </div>
    <div class="custom-file">
        <input type="file" class="custom-file-input"
               id="input-file"
               aria-describedby="inputGroupFileAddon01"
               multiple
        >
    </div>
</div>
</body>
</html>
