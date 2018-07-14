import model.cscards.Card
import model.scoredcards.{CreditCard => SCCreditCard}
import model.{CreditCardRequest, FULL_TIME}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.json.Json.toJson
import play.api.mvc.Results._
import play.api.routing.sird._
import play.api.test._
import play.core.server.Server
import services.DefaultCreditCardsService

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class CreditCardsServiceTest extends PlaySpec {

  def url(port:Int) = s"http://localhost:$port"
  def config(port:Int):Configuration = Configuration("csCardsBaseUrl"->url(port), "scoredCardsBaseUrl"->url(port))

  "CreditCardsService" should {

    "credit cards are sorted by score in descending order" in {

      val creditCardRequest = CreditCardRequest("John", "Smith", "1991/04/18", 500, FULL_TIME, 28000)

      val csCardSuperSaver = Card("SuperSaver Card", "http://www.example.com/apply", 21.4, 6.3, None)
      val csCardSuperSpender = Card("SuperSpender Card", "http://www.example.com/apply", 19.2, 5.0, Some(List("interest free purchases for 6 months")))
      val scCardBuilder = SCCreditCard("ScoreCardBuilder", "http://www.example.com/apply", 19.4, 0.8, List("Supports ApplePay"), List("interest free purchases for 1 month"))

      val result = Server.withRouterFromComponents() { components =>
        import components.{defaultActionBuilder => Action}
      {
        case POST(p"/v1/cards") => Action {
          Ok(toJson(List(csCardSuperSaver, csCardSuperSpender)))
        }
        case POST(p"/v2/creditcards") => Action {
          Ok(toJson(List(scCardBuilder)))
        }
      }
      } { implicit port =>
        WsTestClient.withClient { client =>
          Await.result(new DefaultCreditCardsService(config(port.value), client).recommendCreditCards(creditCardRequest), 10.seconds)
        }
      }
      result must be ('right)
      val cards = result.right.get
      cards must have size 3
      cards.head.name mustEqual scCardBuilder.card
      cards(1).name mustEqual csCardSuperSaver.cardName
      cards(2).name mustEqual csCardSuperSpender.cardName
      cards.head.`card-score` must be >= cards(1).`card-score`
      cards(1).`card-score` must be >= cards(2).`card-score`
    }

    "service error is returned if partner service request fails" in {

      val creditCardRequest = CreditCardRequest("John", "Smith", "1991/04/18", 500, FULL_TIME, 28000)

      val scCardBuilder = SCCreditCard("ScoreCardBuilder", "http://www.example.com/apply", 19.4, 0.8, List("Supports ApplePay"), List("interest free purchases for 1 month"))

      val result = Server.withRouterFromComponents() { components =>
        import components.{defaultActionBuilder => Action}
      {
        case POST(p"/v1/cards") => Action {
          BadRequest
        }
        case POST(p"/v2/creditcards") => Action {
          Ok(toJson(List(scCardBuilder)))
        }
      }
      } { implicit port =>
        WsTestClient.withClient { client =>
          Await.result(new DefaultCreditCardsService(config(port.value), client).recommendCreditCards(creditCardRequest), 10.seconds)
        }
      }
      result must be ('left)
    }
  }
}