package com.liferay.osgi.shell

import java.util

import com.liferay.portal.kernel.scheduler.SchedulerEngineHelperUtil
import com.liferay.portal.kernel.scheduler.messaging.SchedulerResponse
import reflect.runtime.universe._


import scala.util.matching.Regex
import sys.process._
import java.net.URL
import java.io.File
import scala.collection.JavaConversions._

import com.liferay.counter.service.CounterLocalServiceUtil
import com.liferay.portal.kernel.dao.orm.QueryUtil
import com.liferay.portal.model.{Release, Group, User}
import com.liferay.portal.service.{ReleaseLocalServiceUtil, ServiceContext, UserLocalService}

/**
 * Created by carlos on 20/01/16.
 */
object OthersHelper extends EntityHelper{

  def replaceAndPrint(s: String, regex: String,subs:String): String ={
    val r=s.replaceAll(regex,subs)
    println(r)
    r
  }

  val downloadsDir=new File(".downloads/")
  private def assureDownloadsDirExists= if (!downloadsDir.exists()) downloadsDir.mkdir

  def printVersion = {
    val buildNumber=ReleaseLocalServiceUtil.getBuildNumberOrCreate()
    val releases =ReleaseLocalServiceUtil.getReleases(0, ReleaseLocalServiceUtil.getReleasesCount-1)
    val osgiServiceIdentifier=ReleaseLocalServiceUtil.getOSGiServiceIdentifier
    println( buildNumber )
    //println( releases )
    //println( osgiServiceIdentifier )
    buildNumber
  }

  def listScheduledTasks=printTable(SchedulerEngineHelperUtil.getScheduledJobs)

  /**
   *
   * @param url
   * @return
   * downloadJar("http://www.mySite.com/mypath/myjar.jar")
   * will be downloaded to the tomcat's directory
   */
  def downloadJar(url:String): String = {
        assureDownloadsDirExists
        val filename=s"${downloadsDir.getAbsolutePath}/${url.split("/").last}"
        println(s"downloading $filename")
        new URL(url) #> new File(filename) !!
  }

  //TODO: include in the jar
  //com.liferay.marketplace.service
  def listMarketplace = {
    //com.liferay.marketplace.service.

  }

}
