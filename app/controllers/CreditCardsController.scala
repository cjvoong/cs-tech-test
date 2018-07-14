package controllers

import javax.inject.{Inject, Singleton}

import model.CreditCardRequest
import play.api.libs.json.Json.toJson
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{CreditCardsService, PartnerError, PartnerTimeoutError}

import scala.concurrent.ExecutionContext

/**
  * Credit cards controller with a single endpoint
  */
@Singleton
class CreditCardsController @Inject()(cc:ControllerComponents, creditCardsService:CreditCardsService)
                                     (implicit ec:ExecutionContext) extends AbstractController(cc) {
  /** Recommend credit cards */
  def creditCards = Action.async(parse.json[CreditCardRequest]) { implicit request=>
    creditCardsService.recommendCreditCards(request.body).map(_.fold({
      case PartnerTimeoutError => RequestTimeout
      case PartnerError => InternalServerError
    }, {
      creditCards => Ok(toJson(creditCards)).as(JSON)
    }))
  }
}
