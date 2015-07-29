package com.igumnov.scala.example

import java.util.Calendar
import com.igumnov.scala._
import com.igumnov.scala.webserver.User

object SiteServer {
  def main(args: Array[String]) {
    ORM.connectionPool("org.h2.Driver", "jdbc:h2:mem:test", "SA", "", 1, 3)
    ORM.applyDDL("sql")
    WebServer.setPoolSize(5,10)
    WebServer.init("localhost", 8989)

    WebServer.loginService((name) => {
      val user = ORM.findOne[ExampleUser](name)
      if (user.isDefined) {
        Option(new User(user.get.userName, user.get.userPassword, Array[String]("user_role")))
      } else {
        Option(null)
      }
    })

    WebServer.securityPages("/login", "/login?error=1", "/logout")
    WebServer.addRestrictRule("/*", Array("user_role"))
    WebServer.addAllowRule("/static/*")
    WebServer.addClassPathHandler("/static", "META-INF/resources/webjars")
    WebServer.addAllowRule("/js/*")
    WebServer.addStaticContentHandler("/js", "javascript")
    val user = new ExampleUser


    WebServer.locale(Map("en" -> "locale/messages_en.properties"),  (rq,rs)=>{
      "en"
    })


    WebServer.addController("/", (rq, rs,model) => {
      model += "time" -> Calendar.getInstance.getTime
      "index"
    })


    WebServer.addController("/login", (rq, rs,model) => {
      "login"
    })

    WebServer.templates("pages",0)

    WebServer.addRestController[ExampleUser]("/rest/user", (rq, rs, obj) => {
      rq.getMethod match {
        case "GET" => {
          ORM.findAll[ExampleUser]()
        }
        case "POST" => {
          val user = obj.get
          user.userPassword = WebServer.crypthPassword(user.userName, user.userPassword)
          ORM.insert(obj.get)
        }
        case "DELETE" => {
          val user = ORM.findOne[ExampleUser](rq.getParameter("userName"))
          if(user.get.userName == "demo") throw new Exception("You cant delete demo user")
          ORM.delete(user.get)
          user.get
        }

      }
    })

    WebServer.addRestErrorHandler((rq, rs, e) => {
      object Error{
        var message:String =_
      }
      Error.message = e.getMessage
      Error
    })


    val users = ORM.findAll[ExampleUser]

    if(users.size==0) {
      val user = new ExampleUser
      user.userName="demo"
      user.userPassword=WebServer.crypthPassword(user.userName, "demo")
      ORM.insert(user)
    }

    WebServer.start

    }
}