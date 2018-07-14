package services

import javax.inject.{Inject, Singleton}

import model.{CreditCard, CreditCardRequest}
import model.CreditCard._
import model.cscards.{Card, CardSearchRequest}
import model.scoredcards.ScoredCardsRequest
import play.api.Configuration
import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.concurrent.duration._
import scala.math.pow

/** Service error */
sealed trait ServiceError
/** Request to the partner service has timed out */
case object PartnerTimeoutError extends ServiceError
/** Other error produced by the partner service */
case object PartnerError extends ServiceError

/** Parent trait for credit cards services */
trait CreditCardsService {
  /**
    * Recommend credit cards based on the specified parameters
    */
  def recommendCreditCards(creditCardRequest:CreditCardRequest):Future[Either[ServiceError, List[CreditCard]]]
}
/** Default [[services.CreditCardsService]] implementation */
@Singleton
class DefaultCreditCardsService @Inject()(config:Configuration, ws:WSClient)(implicit ec:ExecutionContext) extends CreditCardsService {
  /**
    * Recommend credit cards based on eligibility determined by partner systems
    */
  def recommendCreditCards(creditCardRequest:CreditCardRequest):Future[Either[ServiceError, List[CreditCard]]] = {
    val requestTimeout = 10.seconds
    val cardSearchRequest = ws.url(s"${config.get[String]("csCardsBaseUrl")}/v1/cards").withRequestTimeout(requestTimeout)
      .post(toJson(CardSearchRequest(
        s"${creditCardRequest.firstname} ${creditCardRequest.lastname}", creditCardRequest.dob, creditCardRequest.`credit-score`)))
    val scoredCardsRequest = ws.url(s"${config.get[String]("scoredCardsBaseUrl")}/v2/creditcards").withRequestTimeout(requestTimeout)
      .post(toJson(ScoredCardsRequest(
        creditCardRequest.firstname, creditCardRequest.lastname, creditCardRequest.dob, creditCardRequest.`credit-score`,
        creditCardRequest.`employment-status`, creditCardRequest.salary)))
    (for {
      cardSearchResponse <- cardSearchRequest
      scoredCardsResponse <- scoredCardsRequest
    } yield {
      val round = (d:Double) => BigDecimal(d).setScale(3, BigDecimal.RoundingMode.HALF_DOWN).toDouble
      val calculateScore = (eligibilityPercentageRate:Double, apr:Double) => eligibilityPercentageRate*pow(1/apr, 2)
      // merge credit information from the two partner services and calculate a score for each card
      val recommendedCards = (cardSearchResponse.json.as[List[Card]].map { csCard=>
        CreditCard("CSCards", csCard.cardName, csCard.url, csCard.apr, csCard.features.getOrElse(List.empty[String]),
          round(calculateScore(csCard.eligibility*10, csCard.apr)))
      } ++ scoredCardsResponse.json.as[List[model.scoredcards.CreditCard]].map { scoredCard=>
        CreditCard("ScoredCards", scoredCard.card, scoredCard.`apply-url`, scoredCard.`annual-percentage-rate`,
          scoredCard.attributes++scoredCard.`introductory-offers`,
          round(calculateScore(scoredCard.`approval-rating`*100, scoredCard.`annual-percentage-rate`)))
      }).sorted(creditCardOrderingHighestScoreFirst)
      Right(recommendedCards)
    }) recover {
      case e:TimeoutException =>
        Logger.error(e.getMessage)
        Left(PartnerTimeoutError)
      case e:Exception =>
        Logger.error(e.getMessage)
        Left(PartnerError)
    }
  }
}
