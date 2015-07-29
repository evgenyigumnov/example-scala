package com.igumnov.scala.example

import com.igumnov.scala.orm.Id

class ExampleUser {
  @Id(autoIncremental = false)
  var userName: String = _
  var userPassword: String = _

}
