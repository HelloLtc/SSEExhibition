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
        let btnEncryptPlain = $('#btn-encrypt-file');
        let btnEliminatePlain = $('#btn-eliminate-file');
        let btnSearchPlain = $('#btn-search-file');


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

        $(btnEncryptPlain).on('click', function (){
            let listSk0 = '<%=IEX2Lev.keyGenString(256, "123", "salt/saltSetM", 100000)%>';
            let listSk1 = '<%=IEX2Lev.keyGenString(256, "123", "salt/saltSetM", 100000)%>';
            $.ajax({
                url: './IEX2levServlet',
                method: 'POST',
                async: true,    //或false,是否异步
                data: {
                    method:'add',listSK0:listSk0,listSK1:listSk1
                },
                success: function (data) {
                    if (data.status === true) {
                        alert(`文件加密成功`);
                    } else {
                        alert('文件加密失败');
                    }
                },
                fail: function (err) {
                    alert('文件上传失败 ');
                }
            })
        });


        $(btnSearchPlain).on('click', function (){
            alert("123");
            var val = document.login.levsearch.value;
            let listSk0 = '<%=IEX2Lev.keyGenString(256, "123", "salt/saltSetM", 100000)%>';
            let listSk1 = '<%=IEX2Lev.keyGenString(256, "123", "salt/saltSetM", 100000)%>';
            $.ajax({
                url: './IEX2levServlet',
                type: 'POST', //GET
                async: true,    //或false,是否异步
                data: {
                    method:'search',listSK0:listSk0,listSK1:listSk1,search:val//想要传输过去的数据 key：value，另一个页面通过 key接收value的值
                },
                timeout: 5000,    //超时时间
                dataType: 'text',    //返回的数据格式：json/xml/html/script/jsonp/text
                success:function(data){//data是成功后，接收的返回值
                    alert(data);
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

            <br>
            <br>
        </table>
        <div class="input-group-prepend">
            <button id="btn-search-file">查询</button>
        </div>
    </form>


    <div class="input-group-prepend">
        <button id="btn-upload-file">上传
        </button>
    </div>
    <div class="input-group-prepend">
        <button id="btn-encrypt-file">加密
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

