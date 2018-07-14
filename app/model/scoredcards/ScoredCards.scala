package model.scoredcards

import model.EmploymentStatus
import play.api.libs.json.{Format, Json}

/** Represents the request payload to the ScoredCards service */
case class ScoredCardsRequest(`first-name`:String, `last-name`:String, `date-of-birth`:String, score:Int,
                              `employment-status`:EmploymentStatus, salary:Int)

object ScoredCardsRequest {
  implicit val scoredCardsRequestFormat:Format[ScoredCardsRequest] = Json.format[ScoredCardsRequest]
}

/** Represents the response payload from the ScoredCards service */
case class CreditCard(card:String, `apply-url`:String, `annual-percentage-rate`:Double, `approval-rating`:Double,
                      attributes:List[String], `introductory-offers`:List[String])

object CreditCard {
  implicit val creditCardFormat:Format[CreditCard] = Json.format[CreditCard]
}
