package com.liferay

import scala.util.matching.Regex

/**
 * Created by carlos on 19/01/16.
 */

//////////////////////////
//////USER COMMANDS///////
//////////////////////////
case class CreateUser(options:Map[String,Any])
case class UpdateUser(options:Map[String,Any])
case class DeleteUser(options:Map[String,Any])
case class ShowUser(options:Map[String,Any])
case class DownloadCommand(url:String)
case class Replace(s:String,regex:String,subs:String)
case class ListUsers()
case class SchedulerList()
case class CountUsers()
case class HelpCommand()
case class VersionCommand()

