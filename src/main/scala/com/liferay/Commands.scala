package com.liferay

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
case class ListUsers()
case class CountUsers()
case class HelpCommand()
