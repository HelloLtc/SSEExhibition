# SSE Exhibition

This repo show the functions of some SSE schemes.

## Functions

1. setup: 文件上传,加密,写 sql(放 indices);
2. search: single/multiple;
3. download: 搜到的文件进行解密,下载;
4. 支持多 scheme, 在使用之前进行选择;
5. 加密算法改`国密`;
6. 加密去重算法.

## Tech Stack

**Mode**:
前后端分离, `JSON` 传数据

- FrontEnd
- BackEnd
  - Java version: 8(1.8)
  - MySQL version: 8.0.1+
  - Tomcat version: TODO
  - Servlet
  - Java-Maven

## File Structure

For security:

.gitignore 添加如下代码, 忽略含有`secretConfig`关键字的文件:

```shell
/**/*secretConfig*.*
```

大部分结构由 `IDEA` 自动生成;

```javascript
SSEExhibition/
├── README.md                                       //  project introduction
├── android/                                        //  removed(we do not use android codes in clusion)
├── SQL/                                            //  database init,update.etc
├── lib                                             //  3rd party jars
│   ├── lucene-analyzers-common-8.2.0.jar
│   └── lucene-core-8.2.0.jar
├── src
│   ├── main
│   │   ├── resources                               //  customize properties
│   │   │   ├── config.properties                   //  Normal configuration like path,SQL.etc
│   │   │   └── secretConfig.properties             //  password or more
│   │   └── java
│   │      └── org
│   │           ├── crypto                          //  clusion origin codes
│   │           │   └── sse
│   │           │       ├── CryptoPrimitives.java   //  Crypto Usual functions
│   │           │       └── *.java
│   │           └── ruiyun                          //  ruiyun.lab functions
│   │               ├── crypto
│   │               │   └── Util                    //  Some reuse functions
│   │               │       └── DB.etc
│   │               └── Schemes*N
│   │                   └── *.java                  //  customize code
│   └── test
│       └── java
│           └── org
│               ├── crypto                          //  clusion origin tests
│               │   └── sse
│               │       └── Test*.java              //  auto
│               └── ruiyun                          //  customize test
│                   └── Schemes*N
│                       └── Test*.java
├── web                                             //  Front-End Codes
│   ├── assets                                      //  Art and logic
│   ├── WEB-INF
│   │  └── web.xml                                  //  configure like route.etc
│   └── testfolder                                  //  Because of the `war` package
├── target/                                         //  IDEA auto generated
├── docs/                                           //  documents maybe latter config
├── testfolder/                                     //  Clusion test: test folder for encrypt/decrypt
├── salt/                                           //  Clusion generated: salt
├── test.db                                         //  Clusion generated: db file
├── logs.txt                                        //  Clusion generated: running log
├── SSEExhibition.iml
└── pom.xml                                         //  maven config
```

## Privacy

关于隐私相关的配置, 均带上`secretConfig`文件名(具体请查看`.gitignore`配置), commit 和 push 之前用
`git status`查看已上传的内容.

## Copyright

This repo refer repo [Clusion](https://github.com/encryptedsystems/Clusion).

**Initial commit codes all from `org.Clusion.sse`**, for development, we changed the package name.

We do not consider this repo to `release` to public users or `commercial` usage.

And this may change in late future.
