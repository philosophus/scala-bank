package com.fdahms.scalabank.protobuf

object InterestExtensions {

  implicit class ExtendedInterest(interest: Interest) {
    def getBigDecimal: BigDecimal = {
      BigDecimal(interest.getPpm) / 1e7
    }

  }

  implicit class ExtendedInterestBuilder(interestBuilder: Interest.Builder) {
    def setInterest(bigDecimal: BigDecimal): Interest.Builder = {
      interestBuilder.setPpm((bigDecimal * 1e7).toInt)
    }
  }
}