package com.fdahms.scalabank.authserver

import org.zeromq.{ZContext, ZMQ}
import com.fdahms.scalabank.protobuf._
import com.fdahms.scalabank.protobuf.MonetaryAmountExtensions._
import com.fdahms.scalabank.protobuf.InterestExtensions._


object AuthServer {
  val port: Int = 6221

  private lazy val zmqContext = new ZContext()

  private val accountsDB = Map(
    123L -> "Secret",
    321L -> "Very Secret"
  )

  def main(args: Array[String]) {
    val socket = zmqContext.createSocket(ZMQ.REP)
    socket.bind(s"tcp://*:${port}")
    println("Auth server started - awaiting requests")

    while (true) {
      val request = AuthRequest.parseFrom(socket.recv)
      println("Someone wants to authenticate for account " + request.getCredentials.getAccountId)

      val responseBuilder = AuthResponse.newBuilder
      accountsDB.get(request.getCredentials.getAccountId) match {
        case Some(pw) if pw == request.getCredentials.getPassword =>
          responseBuilder.setAuthorized(true)
        case _ =>
          responseBuilder.setAuthorized(false)
      }

      socket.send(responseBuilder.build.toByteArray)
      println("Keeping the world save")

    }
  }
}