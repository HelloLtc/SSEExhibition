# SSE Exhibition

This repo show the functions of some SSE schemes.

## Functions

1. setup: 文件上传,加密,写 sql(放 indices);
2. search: single/multiple;
3. download: 搜到的文件进行解密,下载;
4. 支持多 scheme, 在使用之前进行选择;
5. 加密算法改`国密`.

## Tech Stack

**Mode**:
前后端分离, `JSON` 传数据

- FrontEnd
- BackEnd
  - MySQL version: TODO
  - Tomcat version: TODO
  - Servlet
  - Java version: 10+
  - Java-Maven

## File Structure

For security
.gitignore 添加如下代码, 忽略含有`secretConfig`关键字的文件:

```shell
/**/*secretConfig*.*
```

大部分结构由 IDEA 自动生成;

```JavaScript
/
|-lib
|-SQL
|-src/
  |- main/
    |-java/
      |-Utils/
        |-DB/                   //  sql manipulate
        |-CryptoPrimitives.java //  密码原语(常见函数)
      |-Schemes*N
    |-resources/
      |-secretConfig.properties // 私密内容, 如数据库账号密码,相关地址; ATTENTION: gitignore应该添加
      |-config.properties   // 常用配置如path等.
  |- test/java
    |-Schemes*N
|-target     //  已编译文件
|-web/       //  前端部分
  |-files/   //  文件上传/下载暂存区;名字可根据path.properties设置
  |-assets/  //  web资源文件
  |-WEB-INF/
    |-web.xml//  route configure
|-pom.xml    //  maven config
|-docs       //  文档
|-README.md  //  project introduction
```

Ref: [Clusion](https://github.com/encryptedsystems/Clusion)
