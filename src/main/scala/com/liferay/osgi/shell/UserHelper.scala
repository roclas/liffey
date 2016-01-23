package com.liferay.osgi.shell

import java.util

import com.liferay.portal.kernel.json.JSON

import scala.collection.JavaConverters._
import com.liferay.counter.service.CounterLocalServiceUtil
import com.liferay.portal.kernel.dao.orm.QueryUtil
import com.liferay.portal.model.{Group, User}
import com.liferay.portal.service.{ServiceContext, UserLocalService}

/**
 * Created by carlos on 20/01/16.
 */
object UserHelper extends EntityHelper{

  override def toMap(cc: AnyRef): Map[String, Any] ={
    val user=cc.asInstanceOf[User]
    Map(
      "id"->user.getUserId,
      "email"->user.getEmailAddress,
      "username"->user.getScreenName
    )
  }

  def getAnyOneUser(implicit usersservice:UserLocalService)={
    usersservice.getUsers(QueryUtil.ALL_POS, QueryUtil.ALL_POS).get(0)
  }


  def getUser(params:Map[String,Any])(implicit usersservice:UserLocalService): Option[User]={
    List(
      (Interpreter.id,(s:String)=>{usersservice.getUserById(java.lang.Long.parseLong(s))}),
      (Interpreter.username,(s:String)=>{usersservice.getUserByScreenName(getAnyOneUser.getCompanyId,s)}),
      (Interpreter.email,(s:String)=>{usersservice.getUserByEmailAddress(getAnyOneUser.getCompanyId,s)})
    ).map{case(k,f)=>((k,f),params.get(k))}.
      filter{case(x,None)=>false;case _=>true}.headOption match{
        case Some(((_,f:((String)=>User)),v:Some[String]))=>Some(f(v.get))
        case _ =>None
    }
  }

  def showUser(params:Map[String,Any])(implicit usersservice:UserLocalService): Unit ={
    println(getUser(params).getOrElse("Not found").toString.replaceAll(", ","\n")) //TODO: improve format??
  }

  def listUsers(implicit usersservice:UserLocalService): Unit ={
    val table: util.List[User] =usersservice.getUsers(QueryUtil.ALL_POS, QueryUtil.ALL_POS)
    printTable(table.toArray.toSeq)
  }

  def countUsers(implicit usersservice:UserLocalService): Unit ={
    val count=usersservice.getCompanyUsersCount(getAnyOneUser.getCompanyId)
    println(s"count=${count} users")
  }


  def deleteUser(params:Map[String,Any])(implicit usersservice:UserLocalService): Unit ={
      val user: Option[User] =getUser(params)
      println(s"deleting ${user.getOrElse("nothing (user not found)")}")
      user.map{x=> usersservice.deleteUser(x.getUserId) }
  }

  def editUser(params:Map[String,Any])(implicit usersservice:UserLocalService):Unit={
    params.filter{
      case(k,v)=>if(k.startsWith(Interpreter.pSearchBy)) true else false
      case _=>false
    }.map{ case(k,v)=>
      val user: Option[User] =getUser(Map((k.replaceFirst(Interpreter.pSearchBy,""),v)))
      user.foreach{u=>
        println(s"editing user ${u}")
        List(
          (Interpreter.username,(s:String)=>{u.setScreenName(s)}),
          (Interpreter.email,(s:String)=>{u.setEmailAddress(s)}),
          (Interpreter.firstname,(s:String)=>{u.setFirstName(s)}),
          (Interpreter.lastname,(s:String)=>{u.setLastName(s)})
          //TODO:Add more options!!!!!
        ).foreach{
              case((pname:String,f:((String)=>Any)))=>params.get(pname).foreach{
                x=>f(x.asInstanceOf[String])
              }
        }
        usersservice.updateUser(u)
      }
    }
 }

  def createUser(params:Map[String,Any])(implicit usersservice:UserLocalService): Unit ={
    println(s"creating actual user ${params}")

    val gpId:Long = CounterLocalServiceUtil.increment( classOf[Group].getName )
    val companyId: Long =getAnyOneUser.getCompanyId

    val serviceContext = new ServiceContext()
    val roleIds: Array[Long] = Array()

    val myuser:User = usersservice.addUser(
      usersservice.getDefaultUser(companyId).getUserId(),
      companyId,
      false,
      params.getOrElse(Interpreter.password,"test").asInstanceOf[String],
      params.getOrElse(Interpreter.password,"test").asInstanceOf[String],
      false,
      params.getOrElse(Interpreter.username,"test").asInstanceOf[String],
      params.getOrElse(Interpreter.email,"").asInstanceOf[String],
      0,//TODO: dehardcode facebookId,
      "",//TODO: dehardcode  openId,
      serviceContext.getLocale(),
      params.getOrElse(Interpreter.firstname,"test").asInstanceOf[String],
      null,
      params.getOrElse(Interpreter.lastname,"test").asInstanceOf[String],
      0,//TODO: dehardcode  prefixId,
      0,//TODO: dehardcode   suffixId,
      true,//TODO:dehardcode isMale
      1,//TODO:dehardcode bMonth,
      1,//TODO:dehardcode bDay,
      1970,//TODO:dehardcode bYear,
      "blogger",//TODO:dehardcode jobTitle
      Array[Long](),//TODO:dehardcode  groupIds,
      Array[Long](),//TODO:dehardcode  organizationIds,
      roleIds,
      Array[Long](),//TODO:dehardcode   userGroupIds,
      false,
      serviceContext
    )
    //val userId = myuser.getUserId()
  }
}
