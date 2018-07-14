package model

import play.api.libs.json.{Format, Json}

/** Represents the response payload for [[controllers.CreditCardsController.creditCards]] */
case class CreditCard(provider:String, name:String, `apply-url`:String, apr:Double, features:List[String], `card-score`:Double)

object CreditCard {
  implicit val creditCardFormat:Format[CreditCard] = Json.format[CreditCard]
  val creditCardOrderingHighestScoreFirst:Ordering[CreditCard] = new Ordering[CreditCard] {
    def compare(x:CreditCard, y:CreditCard):Int = x.`card-score` compare y.`card-score`
  }.reverse
}
