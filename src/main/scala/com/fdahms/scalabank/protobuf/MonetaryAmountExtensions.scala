package com.fdahms.scalabank.protobuf

object MonetaryAmountExtensions {
  implicit class ExtendedMonetaryAmount( val monetaryAmount: MonetaryAmount) {
    def getBigDecimal: BigDecimal = {
      BigDecimal(monetaryAmount.getCentAmount) / 1e2
    }
  }

  implicit class ExtendedMonetaryAmountBuilder( val monetaryAmounBuilder: MonetaryAmount.Builder) {
    def setAmount(bigDecimal: BigDecimal): MonetaryAmount.Builder = {
      monetaryAmounBuilder.setCentAmount((bigDecimal * 1e2).toLong)
    }

    def setAmount(string: String): MonetaryAmount.Builder = {
      setAmount(BigDecimal(string))
    }
  }
}