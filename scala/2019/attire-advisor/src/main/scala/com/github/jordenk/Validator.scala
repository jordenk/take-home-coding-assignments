/**
  * Wrapper object for utility functions.
  */

package com.github.jordenk

import scala.annotation.tailrec

object Validator {

  // Constants
  val ColdCommandMap: Map[String, ClothingResponsePreReqs] = Map(
    "1" -> ClothingResponsePreReqs("boots", Set("3", "6", "8")),
    "2" -> ClothingResponsePreReqs("hat", Set("4", "8")),
    "3" -> ClothingResponsePreReqs("socks", Set("8")),
    "4" -> ClothingResponsePreReqs("shirt", Set("8")),
    "5" -> ClothingResponsePreReqs("jacket", Set("4", "8")),
    "6" -> ClothingResponsePreReqs("pants", Set("8")),
    "7" -> ClothingResponsePreReqs("leaving house", Set("1", "2", "3", "4", "5", "6", "8")),
    "8" -> ClothingResponsePreReqs("Removing PJs", Set()),
  )

  val HotCommandMap: Map[String, ClothingResponsePreReqs] = Map(
    "1" -> ClothingResponsePreReqs("sandals", Set("6", "8")),
    "2" -> ClothingResponsePreReqs("sunglasses", Set("4", "8")),
    "4" -> ClothingResponsePreReqs("shirt", Set("8")),
    "6" -> ClothingResponsePreReqs("shorts", Set("8")),
    "7" -> ClothingResponsePreReqs("leaving house", Set("1", "2", "4", "6", "8")),
    "8" -> ClothingResponsePreReqs("Removing PJs", Set()),
  )

  val TemperatureCommandMap: Map[String, Map[String, ClothingResponsePreReqs]] = Map(
    "COLD" -> ColdCommandMap,
    "HOT" -> HotCommandMap
  )

  type ValidationError = String

  /**
    * Validates the first input argument; it must be an accepted temperature.
    *
    * @param inputArgs Parsed arguments of the form Seq(Temperature, Commands*)
    * @param validTemps Accepted temperature list.
    * @return
    */
  def validateAndProcessTemperature(inputArgs: Seq[String], validTemps: Set[String]): Either[ValidationError, (String, Seq[String])] = {
    inputArgs match {
      case Seq() => Left(s"Cannot operate on an empty list of arguments.")
      case head +: tail => if (validTemps.contains(head)) Right((head, tail)) else Left(s"First argument must be one of $validTemps")
    }
  }

  /**
    * Applies business logic to a single clothing decision.
    *
    * @param previous Previously processed commands.
    * @param remaining Remaining commands to be processed.
    * @param commandMap Map of commands
    * @return
    */
  def validateAndProcessClothingChoice(previous: Seq[String], remaining: Seq[String], commandMap: Map[String, ClothingResponsePreReqs]): Either[ValidationError, (Seq[String], Seq[String])] = {
    if (remaining.isEmpty) return Left(s"Cannot operate on an empty list of commands.")

    val currentCommand = remaining.head

    // If an invalid command is issued, respond with “fail” and stop processing commands
    val preReqs = commandMap.get(currentCommand) match {
      case Some(v) => v.preReqs
      case None => return Left(s"Invalid command. $currentCommand is not in ${commandMap.keySet}")
    }

    // Only 1 piece of each type of clothing may be put on
    if (previous.contains(currentCommand)) return Left(s"Cannot repeat command $currentCommand")

    // Check if prerequisites are satisfied. i.e. Can't put on x before y. The terminal command will have all other steps as a prerequisite.
    // If the terminal command is issued too early or there are commands after the terminal command, then a ValidationError is returned.
    if (preReqs.size != preReqs.count(req => previous.contains(req))) return Left(s"Cannot execute command $currentCommand before $preReqs")

    Right(previous :+ currentCommand, remaining.drop(1))
  }

  /**
    * Iterate through the list of commands applying business logic based on the commandMap. Terminates on ValidationError or when all
    * commands have successfully processed.
    *
    * @param previous Previously processed commands.
    * @param remaining Remaining commands to be processed.
    * @param commandMap Map of commands
    * @return
    */
  @tailrec
  def processCommandSequence(previous: Seq[String], remaining: Seq[String], commandMap: Map[String, ClothingResponsePreReqs]): (Seq[String], Seq[String]) = {
    validateAndProcessClothingChoice(previous, remaining, commandMap) match {
      case Left(_) => (previous :+ "fail", Seq())
      case Right((p, Seq())) => (p, Seq())
      case Right((p, r)) => processCommandSequence(p, r, commandMap)
    }
  }

  /**
    * Entry point function. This function calls the other functions in this file. If valid inputs are supplied, this function
    * uses the temperature dependent commandMap to apply business logic. Outcomes are collected and returned as a Seq for downstream use.
    *
    * @param args User supplied arguments
    * @param tempCommandMap Map of temperature to commandMap.
    * @return
    */
  def processCommandLineArgs(args: Seq[String], tempCommandMap: Map[String, Map[String, ClothingResponsePreReqs]] = TemperatureCommandMap): Seq[String] = {
    val strippedInputArgs = args.map(_.stripSuffix(","))

    // Valid Temperature Check
    val (temp, commands) = validateAndProcessTemperature(strippedInputArgs, tempCommandMap.keySet) match {
      case Left(_) => return Seq("fail")
      case Right(pair) => pair
    }

    val tcm = TemperatureCommandMap.getOrElse(temp, Map())

    // Roll through to end and check
    val (processCommands, _) = processCommandSequence(Seq(), commands, tcm)

    processCommands.map(pc => tcm.get(pc) match {
      case Some(crpr) => crpr.response
      case _ => pc
    })
  }
}

case class ClothingResponsePreReqs(response: String, preReqs: Set[String])
