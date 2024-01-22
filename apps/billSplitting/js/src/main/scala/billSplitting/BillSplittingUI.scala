package billSplitting

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

import org.scalajs.dom
import scalatags.JsDom.all.*
import org.scalajs.dom.html.{Input, Select, TextArea}
import cs214.webapp.*
import cs214.webapp.client.*

object BillSplittingClientApp extends WSClientApp:
  def name: String = "BillSplitting"

  def init(userId: UserId, sendMessage: ujson.Value => Unit, target: dom.Element): ClientAppInstance =
    BillSplittingInstance(userId, sendMessage, target)

class BillSplittingInstance(userId: UserId, sendMessage: ujson.Value => Unit, target: dom.Element)
    extends StateMachineClientAppInstance[BillSplittingEvent, BillSplittingView](userId, sendMessage, target):
  def name: String = "BillSplitting"

  override val wire: AppWire[BillSplittingEvent, BillSplittingView] = BillSplittingWire
    
  override def render(userId: UserId, view: BillSplittingView): Frag =
    frag(
      println("rendered"),
      h2(b("BillSplitting: "), "Be the first to Add to the Bill!"),
      renderView(userId, view)
    )
  
  //private def nextEvent()
  def renderEachMenuItem(itemName : String, itemPrice : Double) : Frag = 
    div(
              span(itemName),       // Display ItemName
              span(s"$$$itemPrice") // Display Price (assuming price is in dollars)
              //input(`type` := "checkbox") // Display a checkbox
            )
  def renderNonInteractiveBill(itemNameToPriceMap: Map[String, Double]) : Frag = 
    footer(
      h3("Current Bill"),
      if itemNameToPriceMap.isEmpty then h4("Empty") else
      itemNameToPriceMap.map((itemName, itemPrice) => renderEachMenuItem(itemName, itemPrice)).toArray
      //it accepts sequences!
    )


  def renderView(userId: UserId, view: BillSplittingView): Frag = 
      println(view)
      view match
        case BillSplittingView.Adding(billItemToPriceMap, finishedAddingStatus) => 
          frag(
          h2(b("Add A New Item And Price to the Bill")),
          form(
            onsubmit := {
              () => //means that it is not immediately invoked
              sendEvent( 
              BillSplittingEvent.Add(dom.document.getElementById("item-name").asInstanceOf[Input].value,dom.document.getElementById("item-price").asInstanceOf[Input].valueAsNumber)
              )
              },
            div(
              cls := "grid-form",
              label(`for` := "item-name", "Item Name: "),
              input(
                `type` := "text",
                id := "item-name",
                placeholder := "Chicken..",
                required := true
              )
              ,
              label(`for` := "item-price", "Item Price: "),
              input(
                `type` := "number",
                step := "0.01",
                id := "item-price",
                placeholder := "5.00",
                required := true
              )
            ),
          input(`type` := "submit", value := "Add Menu Item")
          ),
          renderNonInteractiveBill(billItemToPriceMap),
          form(
            onsubmit := {
              () => //means that it is not immediately invoked
              sendEvent( 
              BillSplittingEvent.FinishAdding
              )
              },
          input(`type` := "submit", value := "Finish Adding")
          )
          )
        case BillSplittingView.Choosing(billItemToPriceMap, billItemToUsersMap, userChoosingStatusMap) => 
          frag(
            h3("Current Bill"),
            if billItemToPriceMap.isEmpty then h4("Empty") else

            billItemToPriceMap.map((itemName, itemPrice) => 
              div(
              span(itemName),       // Display ItemName
              span(s"$$$itemPrice"), // Display Price (assuming price is in dollars)
              input(`type` := "checkbox", onclick:= {() =>
                sendEvent(BillSplittingEvent.Choose(
                  itemName, itemPrice
                ))
                }) // Display a checkbox
                )).toArray,
            form(
            onsubmit := {
              () => //means that it is not immediately invoked
              
              sendEvent( 
              BillSplittingEvent.FinishChoosing
              )
              },
          input(`type` := "submit", value := "Finish Choosing")
          )
          ) 
        case BillSplittingView.FinishedChoosing(userAndPaymentMap) =>
          frag(
            if userAndPaymentMap.isEmpty then 
              p(i("Wait for others to finish choosing :)"))
            else 
              p(
                cls := "finished",
                f"Your Payment is: ${userAndPaymentMap.get(userId).head}"
              )
            
          )
        
    

// Scala.js magic to register our application from this file
@JSExportAll
object BillSplittingRegistration:
  @JSExportTopLevel("BillSplittingExport")
  val registration = WebClient.register(BillSplittingClientApp)
