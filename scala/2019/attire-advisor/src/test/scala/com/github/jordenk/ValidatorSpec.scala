package com.github.jordenk

import org.scalatest._
import Validator._

class ValidatorSpec extends FlatSpec with Matchers {
  private val testValidTemps = Set("COLD", "HOT")

  "validateTemperatureType" should "return a sequence when temperature is valid" in {
    testValidTemps.foreach(temp => {
      validateAndProcessTemperature(Seq(temp, "1", "2"), testValidTemps) shouldEqual Right(temp, Seq("1", "2"))
    })
  }

  it should "return a ValidationError when temperature is invalid" in {
    validateAndProcessTemperature(Seq("invalidTemp", "1"), testValidTemps) shouldEqual Left(s"First argument must be one of $testValidTemps")
  }

  it should "return a ValidationError when it receives an empty argument seq" in {
    validateAndProcessTemperature(Seq(), testValidTemps) shouldEqual Left("Cannot operate on an empty list of arguments.")
  }

  private val testCommandMap = Map(
    "1" -> ClothingResponsePreReqs("sandals", Set("6", "8")),
    "2" -> ClothingResponsePreReqs("sunglasses", Set("4", "8")),
    "4" -> ClothingResponsePreReqs("shirt", Set("8")),
    "6" -> ClothingResponsePreReqs("shorts", Set("8")),
    "7" -> ClothingResponsePreReqs("leaving house", Set("1", "2", "4", "6", "8")),
    "8" -> ClothingResponsePreReqs("Removing PJs", Set()),
  )

  "validateClothingChoice" should "process a value with no prerequisites" in {
    validateAndProcessClothingChoice(Seq(), Seq("8"), testCommandMap) shouldEqual Right(Seq("8"), Seq())
  }

  it should "process a value with prerequisites if the prerequisites have been satisfied" in {
    validateAndProcessClothingChoice(Seq("6", "8"), Seq("1"), testCommandMap) shouldEqual Right(Seq("6", "8", "1"), Seq())
  }

  it should "return a ValidationError when commands are invalid" in {
    validateAndProcessClothingChoice(Seq(), Seq("invalidCommand"), testCommandMap) shouldEqual Left(s"Invalid command. invalidCommand is not in ${testCommandMap.keySet}")
  }

  it should "return a ValidationError when duplicate commands are called" in {
    validateAndProcessClothingChoice(Seq("6", "8"), Seq("8"), testCommandMap) shouldEqual Left("Cannot repeat command 8")
  }

  it should "return a ValidationError when a command is called before prerequisites have been satisfied" in {
    val command = "7"
    validateAndProcessClothingChoice(Seq("6", "8", "1"), Seq(command), testCommandMap) shouldEqual Left(s"Cannot execute command $command before ${testCommandMap(command).preReqs}")
  }

  it should "return a ValidationError when it receives an empty remaining seq" in {
    validateAndProcessClothingChoice(Seq("8"), Seq(), testCommandMap) shouldEqual Left("Cannot operate on an empty list of commands.")
  }

  "processCommandSequence" should "iterate through a valid sequence without failing" in {
    val validSeq = Seq("8", "6", "4", "2", "1", "7")
    processCommandSequence(Seq(), validSeq, testCommandMap) shouldEqual ((validSeq, Seq()))
  }

  it should "return a seq with fail appended when ValidationErrors are encountered" in {
    // Invalid command
    processCommandSequence(Seq(), Seq("invalidCommand"), testCommandMap) shouldEqual ((Seq("fail"), Seq()))
    // Duplicate command
    processCommandSequence(Seq("6", "8"), Seq("8"), testCommandMap) shouldEqual ((Seq("6", "8", "fail"), Seq()))
    // Unfulfilled prerequisites
    processCommandSequence(Seq("6", "8", "1"), Seq("7"), testCommandMap) shouldEqual ((Seq("6", "8", "1", "fail"), Seq()))
  }

  private val testTempCommandMap = Map("HOT" -> testCommandMap)
  "processCommandLineArgs" should "return a sequence without 'fail' when commands are supplied in the correct order" in {
    processCommandLineArgs(Seq("HOT", "8,", "6,", "4,", "2,", "1,", "7,"), testTempCommandMap) shouldEqual Seq("Removing PJs", "shorts", "shirt", "sunglasses", "sandals", "leaving house")
  }

  it should "return 'fail' as soon as the first ValidationError is encountered" in {
    processCommandLineArgs(Seq("HOT", "8,", "6,", "4,", "7,", "1,"), testTempCommandMap) shouldEqual Seq("Removing PJs", "shorts", "shirt", "fail")
  }
}
