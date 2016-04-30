package com.fdahms.scalabank.client

import com.fdahms.scalabank.zmq.LazyPirate
import com.fdahms.scalabank.protobuf._
import com.fdahms.scalabank.protobuf.InterestExtensions._
import com.fdahms.scalabank.protobuf.MonetaryAmountExtensions._
import com.fdahms.scalabank.loanserver.LoanServer
import org.zeromq.ZContext


object Client {

  private lazy val loanLP = new LazyPirate(new ZContext, s"tcp://127.0.0.1:${LoanServer.port}")

  private def performApplication(application: LoanApplicationRequest): Option[LoanApplicationResponse] = {
    val message = application.toByteArray
    val response = loanLP.reqResp(message)
    response.map(LoanApplicationResponse.parseFrom)
  }

  def main(args: Array[String]): Unit = {

    println("\nWelcome to the Scala Bank loan application client\n")

    val loanRequestBuilder = LoanApplicationRequest.newBuilder

    println("How much do you need? ")
    val amountBuilder = MonetaryAmount.newBuilder
      amountBuilder.setAmount(readLine)
    loanRequestBuilder.setAmount(amountBuilder)

    println("What do you need the money for? ")
    loanRequestBuilder.setUsingMoneyForFintech(readLine.contains("FinTech"))

    println("Where you ever unable to repay a loan? ")
    loanRequestBuilder.setEverDefaulted(readLine == "Yes")

    println("Do you promise to pay us back? ")
    loanRequestBuilder.setPromiseToGiveMoneyBack(readLine == "Yes")

    println("Your Account number: ")
    val credentialsBuilder = Credentials.newBuilder
    credentialsBuilder.setAccountId(readLine.toLong)

    println("Your password: ")
    credentialsBuilder.setPassword(readLine)
    loanRequestBuilder.setCredentials(credentialsBuilder)

    println("Let's see if we can help you with that")
    val applicationResponse = performApplication(loanRequestBuilder.build)

    applicationResponse match {
      case None => println("Could not connect to server. Its definitely not us so it must be your fault.")
      case Some(resp) if resp.getApproved() =>
        println("Lucky you!")
        println("We can offer you the loan for an interest rate of just " +
          (resp.getInterest.getBigDecimal * 1e2).toDouble
          + "%."
        )
      case Some(resp) => println("No chance my friend! Go see your local loan shark.")
    }
  }
}