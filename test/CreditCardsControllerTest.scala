import controllers.CreditCardsController
import model.{CreditCard, CreditCardRequest, FULL_TIME}
import org.scalatestplus.play._
import play.api.http.HeaderNames
import play.api.libs.json.Json.toJson
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import services.{CreditCardsService, PartnerTimeoutError}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreditCardsControllerTest extends PlaySpec {

  "CreditCardsController" should {

    val creditCardRequest = CreditCardRequest("John", "Smith", "1991/04/18", 500, FULL_TIME, 28000)
    def fakeRequest(creditCardRequest:CreditCardRequest) =
      FakeRequest(POST, "/creditcards", FakeHeaders(Seq(HeaderNames.HOST -> "localhost")), creditCardRequest)

    "return a valid result with action" in {
      val creditCardsResponse = List(CreditCard("CSCards", "SuperSaverCard", "http://www.example.com/apply", 21.4, List.empty[String], 0.137))
      val creditCardsService:CreditCardsService = (_:CreditCardRequest) => Future.successful(Right(creditCardsResponse))
      val controller = new CreditCardsController(stubControllerComponents(), creditCardsService)
      val result = controller.creditCards(fakeRequest(creditCardRequest))
      status(result) must equal(OK)
      contentAsJson(result) must equal(toJson(creditCardsResponse))
    }

    "return an error result" in {
      val creditCardsService:CreditCardsService = (_:CreditCardRequest) => Future.successful(Left(PartnerTimeoutError))
      val controller = new CreditCardsController(stubControllerComponents(), creditCardsService)
      val result = controller.creditCards(fakeRequest(creditCardRequest))
      status(result) must equal(REQUEST_TIMEOUT)
    }
  }
}
