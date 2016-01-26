package com.liferay.osgi.shell

import com.liferay._
import scala.collection.mutable
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

/**
 * Created by carlos on 19/01/16.
 */
/*
a ~ b parse a sequence made of a then b
a | b introduce an alternative parser that parses a or b
a? introduce an optional parser
a* introduce on optional and repeatable parser
a+ introduce a repeatable parser
a ~> b like ~ but ignore the left member (a)
a <~ b like ~ but ignore the right member (b)
*/
class LiffeyParser extends RegexParsers {
  def pAnything = """.*""".r ^^ { _.toString }
  def pEnd= """\s*$""".r ^^ { _.toString }

  def pLiffey = (pHelp |pUser | pRole | pVersion |pDownload | pReplace | pScheduler) <~ pEnd

  def pReplace=  "replace".r ~> pThreeArgs ^^ { l=>Replace(l(0).toString,l(1).toString,l(2).toString)}
  def pDownload=  "download".r ~> anyChars.r ^^ { x=>DownloadCommand(x)}
  def pVersion=  "liferay version".r ^^ { _=> VersionCommand }
  def pHelp=  "(help|usage)".r ^^ { _=>HelpCommand }
  def pScheduler= ((scheduler~>pList)|(pList~>scheduler)) ^^ { _ => SchedulerList}
  def pUser =  ((user~>pCreate)|(pCreate~>user)) ~> pUserOpts ^^ { CreateUser(_) }|
    ((user~>pDelete)|(pDelete~>user)) ~> pUserDeleteOpts ^^ { DeleteUser(_) }|
    ((user~>pShow)|(pShow~>user)) ~> pUserShowOpts ^^ { ShowUser(_) }|
    ((user~>pUpdate)|(pUpdate~>user))~> pUserUpdateOpts ^^{ UpdateUser(_) }|
    ((user~>pList)|(pList~>user)) ^^ { _ => ListUsers}|
    ((user~>pCount)|(pCount~>user)) ^^ { _ => CountUsers }

  val user: Regex = "users?".r

  val scheduler: Regex = "scheduler?".r

  val emptyStr: String =""
  val anyChars: String ="""\S+"""

  val password: String = "password"
  val passwordOpt: String = "-password="

  val roles: String = "roles"
  val rolesOpt: String = "-roles="

  val email: String = "email"
  val emailOpt: String = "-email="

  val username: String = "username"
  val usernameOpt: String = "-username="

  val lastname: String = "lastname"
  val lastnameOpt: String = "-lastname="

  val firstname: String = "firstname"
  val firstnameOpt: String = "-firstname="

  val id: String = "id"
  val idOpt: String = "-id="

  private val pCount: String = "count"
  private val pCreate: String = "create"
  private val pUpdate: String = "update"
  private val pDelete: String = "delete"
  private val pShow: String = "show"
  private val pList: String = "list"
  val pSearchBy: String = "searchBy-"

  def pPassword=(passwordOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst (passwordOpt,emptyStr);((password,r))}}
  def pId =(idOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst (idOpt,emptyStr); ((id,r))}}
  def pUserName =(usernameOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst(usernameOpt,emptyStr);((username,r))}}
  def pFirstName=(firstnameOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst(firstnameOpt,emptyStr);((firstname,r))}}
  def pLastName=(lastnameOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst(lastnameOpt,emptyStr);((lastname,r))}}
  def pEmail=(emailOpt+anyChars).r ^^{x=>{val r=x.toString replaceFirst(emailOpt,emptyStr);((email,r))}}
  def pRoles: Parser[Tuple2[String,Any]]=(rolesOpt + anyChars).r ^^
    {x=>{val r=x.toString replaceFirst(rolesOpt,emptyStr);((roles,r))}}


  def pUserOpt: Parser[(String, Any)] =(pPassword|pUserName|pFirstName|pLastName|pEmail|pRoles)
  def pUserOpts:Parser[Map[String,Any]]= pUserOpt.* ^^ { _.toMap }
  def pUserShowOpt: Parser[(String, Any)] = ( pUserName | pId | pEmail )
  def pUserShowOpts:Parser[Map[String,Any]]= pUserShowOpt.* ^^ { _.toMap }
  def pUserDeleteOpts=pUserShowOpts

  def pUserSearchOpt(m:mutable.Map[String, Any])=(pUserName|pId|pEmail) ^^ {x=>m+=((pSearchBy+x._1,x._2))}
  def pUserEditOpts(m:mutable.Map[String, Any]):Parser[Map[String,Any]]= pUserOpt.+ ^^ { _.toMap ++ m }
  def pUserUpdateOpts: Parser[Map[String, Any]] = { //TODO: create generic function????
    var cntx=mutable.Map[String,Any]()
    pUserSearchOpt(cntx) ~> pUserEditOpts(cntx)
  }

  def pListParam(l:mutable.ListBuffer[String])= anyChars.r ^^ {l+=_.toString}
  def pThreeArgs: Parser[List[Any]] = {
    var paramsCtx=mutable.ListBuffer[String]()
    pListParam(paramsCtx) ~> pListParam(paramsCtx) ~> pListParam(paramsCtx) ^^ {_.toList}
  }

  def pRole = "role" ~> (pCreate|pDelete|pList|pCount) ~ pAnything ^^ { case c ~ a => role(c,a) }
  def role(command:String,rest:String):String={
    command match{
        //TODO: really implement these methods
      case `pCreate`=>s"creating role ${rest}"
      case `pDelete`=>s"deleting role ${rest}"
      case `pList`=>s"listing roles ${rest}"
      case `pCount`=>s"counting users ${rest}"
      case _=>Interpreter.usage
    }
  }
}
