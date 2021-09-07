/**
  * Entry point/executor. Not covered with testing. Used for side effect code.
  */

package com.github.jordenk

import Validator._

object Main extends App {
  val parsed = processCommandLineArgs(args.toSeq)
  println(parsed.mkString(", "))
}

