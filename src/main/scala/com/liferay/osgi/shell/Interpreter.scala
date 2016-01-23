package com.liferay.osgi.shell

import com.liferay._
import com.liferay.portal.service.{ServiceContext, UserLocalService}
import com.liferay.portal.model.User
import scala.util.parsing.combinator._

object Interpreter extends LiffeyParser with Levenshtein{
	//TODO: maybe check for similar commands in case of error?
	//TODO: Create proper usage message
	def usage=s"""
			|
			|Usage examples:
			|	${usageExamples.foldLeft("")((s,acc)=>s"$s$acc \r\t")}
			|	<there will be more implemented soon>
		""".stripMargin

	val usageExamples=List(
		"liffey help",
		"liffey user(s) create -username=<my_desired_username> -password=<my_password> -email=<my_email>",
		"liffey create user -username=<my_desired_username> -password=<my_password> -email=<my_email>",
		"liffey user delete -username=<my_desired_username>",
		"liffey delete users -email=<my_email>",
		"liffey users list",
		"liffey list users # order doesn't matter here",
		"liffey user count",
		"liffey user update -email=<email_by_which_to_search> -password=<new_password> -email=<new_email>",
		"liffey user update -id=<id_to_search> -email=<new_email> #the first option is always used to find the entity",
		"liffey user update -username=<search_username> -username=<new_name> #searches only by id,email, or username"
	)

	def nocoments(s:String)=s.split("#")(0)
	def cleanstring(s:String)=nocoments(s.replaceAll("=.*?\\s"," ").replaceAll("[)(]",""))
	def samplePaterns= usageExamples.map(cleanstring)
	def closestExamples(s:String)={
		//usageExamples.map(nocoments).zip(samplePaterns.map{x=>distance(s,x.take(18))})
		usageExamples.map(nocoments).zip(samplePaterns.map{x=>distance(s.split("-")(0).take(18),x.split("-")(0))})
	}

	def execute(uservice:UserLocalService,params:Array[String])= {
		 parse(pLiffey, params.map(_+" ").mkString) match {
					 case Success(matched,_) => {
						 println(matched)
						 implicit val usersservice:UserLocalService=uservice
						 matched match{
							 case CreateUser(opts)=> UserHelper.createUser(opts)
							 case DeleteUser(opts)=> UserHelper.deleteUser(opts)
							 case UpdateUser(opts)=> UserHelper.editUser(opts)
							 case ShowUser(opts)=> UserHelper.showUser(opts)
							 case ListUsers=> UserHelper.listUsers
							 case CountUsers=> UserHelper.countUsers
							 case HelpCommand=> println(usage)
							 case _=>"UNIMPLEMENTED OPTION (please report the bug)"
						 }
					 }
					 case Failure(msg,_) =>{
						 println("FAILURE: " + msg)
						 println("input= " + params.map(_+" ").mkString )
						 val closest=closestExamples(params.map(_+" ").mkString).sortBy(_._2).take(3)
						 val candidates=closest.filter(_._2<15).map(_._1)
						 val similarlist=if(candidates.size>0) candidates else closest.take(1)
						 println(s"""did you mean something similar to these?
								|
								|	${similarlist.foldLeft("")((s,acc)=>s"$s$acc \r\t")}
								|
								|	(type "liffey help" for more info)
								|""".stripMargin
						 )
					 }
					 case Error(msg,_) => println("ERROR: " + msg + usage)
		 }
	}
}

object Main extends App {
  println("This jar file is supposed to be deployed as an OSGi bundle")
  println("Please, install it as an OSGi module or it will not work properly")
  Interpreter.usage
}

