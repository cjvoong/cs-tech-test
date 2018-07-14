package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Enumeration type for the different employment status */
sealed trait EmploymentStatus extends Product {
  val name:String = productPrefix
}
object EmploymentStatus {
  implicit val employmentStatusFormat:Format[EmploymentStatus] = new Format[EmploymentStatus] {
    def writes(employmentStatus:EmploymentStatus):JsValue = JsString(employmentStatus.name)
    def reads(jsValue:JsValue):JsResult[EmploymentStatus] = jsValue match {
      case JsString(value) =>
        value.toUpperCase match {
          case FULL_TIME.name => JsSuccess(FULL_TIME)
          case PART_TIME.name => JsSuccess(PART_TIME)
          case STUDENT.name => JsSuccess(STUDENT)
          case UNEMPLOYED.name => JsSuccess(UNEMPLOYED)
          case RETIRED.name => JsSuccess(RETIRED)
          case _ => JsError()
        }
      case _ => JsError()
    }
  }
}
case object FULL_TIME extends EmploymentStatus
case object PART_TIME extends EmploymentStatus
case object STUDENT extends EmploymentStatus
case object UNEMPLOYED extends EmploymentStatus
case object RETIRED extends EmploymentStatus

/** Represents the request payload for [[controllers.CreditCardsController.creditCards]] */
case class CreditCardRequest(firstname:String, lastname:String, dob:String, `credit-score`:Int,
                             `employment-status`:EmploymentStatus, salary:Int)

object CreditCardRequest {
  implicit val creditCardRequestReads:Reads[CreditCardRequest] = (
    (JsPath \ "firstname").read[String] and
      (JsPath \ "lastname").read[String] and
      (JsPath \ "dob").read[String](pattern(raw"[0-9]{4}/[0-9]{2}/[0-9]{2}".r)) and
      (JsPath \ "credit-score").read[Int](min(0) keepAnd max(700)) and
      (JsPath \ "employment-status").read[EmploymentStatus] and
      (JsPath \ "salary").read[Int](min(0))
    )(CreditCardRequest.apply _)
  implicit val creditCardRequestWrites:Writes[CreditCardRequest] = Json.writes[CreditCardRequest]
}
