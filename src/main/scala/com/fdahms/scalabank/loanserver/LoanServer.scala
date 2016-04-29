package com.fdahms.scalabank.loanserver

import org.zeromq.{ZContext, ZMQ}
import com.fdahms.scalabank.protobuf._
import com.fdahms.scalabank.protobuf.MonetaryAmountExtensions._
import com.fdahms.scalabank.protobuf.InterestExtensions._
import com.fdahms.scalabank.authserver.AuthServer
import com.fdahms.scalabank.zmq.LazyPirate


object LoanServer {
  val port: Int = 6220

  private lazy val zmqContext = new ZContext()
  private lazy val authLP = new LazyPirate(zmqContext, s"tcp://127.0.0.1:${AuthServer.port}")

  private def checkAuth(credentials: Credentials): Boolean = {
    val request = AuthRequest.newBuilder
      .setCredentials(credentials)
      .build
    authLP.reqResp(request.toByteArray)
      .map(AuthResponse.parseFrom) match {
      case None => false
      case Some(response) => response.getAuthorized
    }
  }

  private def cleverAlgorithm(loanRequest: LoanApplicationRequest): Option[BigDecimal] = {
    // Check authorization first
    if (!checkAuth(loanRequest.getCredentials)) return None

    // Now do risk
    if (loanRequest.getUsingMoneyForFintech) return Some(BigDecimal(0.02))
    if (loanRequest.getEverDefaulted) return None
    if (loanRequest.getAmount.getBigDecimal < BigDecimal(20000)
        && loanRequest.getPromiseToGiveMoneyBack) return Some(BigDecimal(0.055))
    None
  }

  def main(args: Array[String]) {
    val socket = zmqContext.createSocket(ZMQ.REP)
    socket.bind(s"tcp://*:${port}")
    println("Loan server started - awaiting requests")

    while(true) {
      val request = LoanApplicationRequest.parseFrom(socket.recv)
      println("Got an application for an amount of " + request.getAmount.getBigDecimal)

      val responseBuilder = LoanApplicationResponse.newBuilder
      cleverAlgorithm(request) match {
        case None => responseBuilder.setApproved(false)
        case Some(interest) =>
          responseBuilder.setApproved(true)
            .setInterest(Interest.newBuilder.setInterest(interest))
      }

      socket.send(responseBuilder.build.toByteArray)
      println("Done for now")

    }
  }


}