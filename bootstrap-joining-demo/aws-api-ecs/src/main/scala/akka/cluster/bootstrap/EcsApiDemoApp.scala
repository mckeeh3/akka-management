/*
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.cluster.bootstrap

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.discovery.awsapi.ecs.AsyncEcsSimpleServiceDiscovery
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import com.typesafe.config.ConfigFactory

object EcsApiDemoApp {

  def main(args: Array[String]): Unit = {
    val privateAddress = getPrivateAddressOrExit
    val config = ConfigFactory
      .systemProperties()
      .withFallback(
        ConfigFactory.parseString(s"""
             |akka {
             |  actor.provider = "cluster"
             |  management {
             |    cluster.bootstrap.contact-point.fallback-port = 19999
             |    http.hostname = "${privateAddress.getHostAddress}"
             |  }
             |  discovery.method = aws-api-ecs-async
             |  remote.netty.tcp.hostname = "${privateAddress.getHostAddress}"
             |}
           """.stripMargin)
      )
    val system = ActorSystem("bootstrap-joining-demo-aws-api-ecs", config)
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
  }

  private[this] def getPrivateAddressOrExit: InetAddress =
    AsyncEcsSimpleServiceDiscovery.getContainerAddress match {
      case Left(error) =>
        System.err.println(s"$error. Halting.")
        sys.exit(1)

      case Right(value) =>
        value
    }

}
