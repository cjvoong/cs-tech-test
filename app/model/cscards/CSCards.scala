package model.cscards

import play.api.libs.json.{Format, Json}

/** Represents the request payload to the CSCards service */
case class CardSearchRequest(fullName:String, dateOfBirth:String, creditScore:Int)

object CardSearchRequest {
  implicit val cardSearchRequestFormat:Format[CardSearchRequest] = Json.format[CardSearchRequest]
}

/** Represents the response payload from the CSCards service */
case class Card(cardName:String, url:String, apr:Double, eligibility:Double, features:Option[List[String]])

object Card {
  implicit val cardFormat:Format[Card] = Json.format[Card]
}
