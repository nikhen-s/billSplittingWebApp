package billSplitting

import ujson.*
import scala.util.{Failure, Success, Try}

import cs214.webapp.wires.*
import cs214.webapp.exceptions.DecodingException
import cs214.webapp.{AppWire, WireFormat, UserId}

object BillSplittingWire extends AppWire[BillSplittingEvent, BillSplittingView]:
  import BillSplittingEvent.*
  import BillSplittingView.*

  override object eventFormat extends WireFormat[BillSplittingEvent]:
    //encode event and decode event
    override def encode(t: BillSplittingEvent): Value =
      t match
        case Add(itemName, price) => 
          ujson.Obj(
            "event" -> ujson.Str("Add"),
            "billItemName" -> ujson.Str(itemName),
            "billItemPrice" -> ujson.Num(price)
          )
        case Choose(itemName, price) => 
          ujson.Obj(
            "event" -> ujson.Str("Choose"),
            "billItemName" -> ujson.Str(itemName),
            "billItemPrice" -> ujson.Num(price)
          )
        case FinishAdding => 
          ujson.Obj(
            "event" -> ujson.Str("FinishAdding")
          )
        case FinishChoosing => 
          ujson.Obj(
            "event" -> ujson.Str("FinishChoosing")
          )
      
    override def decode(json: Value): Try[BillSplittingEvent] =
      Try{
        val wholeObj = json.obj
        wholeObj("event").str match
          case "Add" => BillSplittingEvent.Add(wholeObj("billItemName").str, wholeObj("billItemPrice").num)
          case "Choose" => BillSplittingEvent.Choose(wholeObj("billItemName").str, wholeObj("billItemPrice").num)
          case "FinishAdding" => BillSplittingEvent.FinishAdding
          case "FinishChoosing" => BillSplittingEvent.FinishChoosing
          //case _ => java.lang.IllegalArgumentException(f"Unexpected Value")
      }

  override object viewFormat extends WireFormat[BillSplittingView]:
    //encode view and decode view
    def encode(t: BillSplittingView): Value =
      t match
        //server should just update the 
        case Adding(billItemToPriceMap, finishedAddingStatus) =>
          val bilItems = ujson.Arr(billItemToPriceMap.keySet.map(billItem => ujson.Str(billItem)).toArray*)
          val prices = ujson.Arr(billItemToPriceMap.values.map(price => ujson.Num(price)).toArray*)
          val billItemToPriceString = ujson.Arr(bilItems, prices)
          ujson.Obj(
            "view" -> "Adding",
            "finishedAddingStatus" -> ujson.Bool(finishedAddingStatus),
            "billItemToPriceMap" -> billItemToPriceString
          )
        case Choosing(billItemToPriceMap, billItemToUsersMap, userChoosingStatusMap) => 
          //ig for map, i just convert it to an array first?!
          val bilItems = ujson.Arr(billItemToPriceMap.keySet.map(billItem => ujson.Str(billItem)).toArray*)
          val prices = ujson.Arr(billItemToPriceMap.values.map(price => ujson.Num(price)).toArray*)
          val billItemToPriceString = ujson.Arr(bilItems, prices)
          val billItemsWithUser = ujson.Arr(billItemToUsersMap.keySet.map(itemName => ujson.Str(itemName)).toArray*)
          val users = 
            ujson.Arr(
              billItemToUsersMap.values.map(listOfUsers => 
              ujson.Arr(listOfUsers.map(eachUser => ujson.Str(eachUser)).toArray*)).toArray*)
          val billItemToUsersString = ujson.Arr(billItemsWithUser, users)
          //i didn't serialize users in userChoosingStatus Map bcs users will be the same as before
          val choosingStatusUsers = ujson.Arr(userChoosingStatusMap.keySet.map(userId => ujson.Str(userId)).toArray*)
          val choosingStatus = ujson.Arr(userChoosingStatusMap.values.map(status => ujson.Bool(status)).toArray*)
          val userChoosingStatusString = ujson.Arr(choosingStatusUsers, choosingStatus)
          ujson.Obj(
            "view" -> "Choosing",
            "billItemToPriceMap" -> billItemToPriceString,
            "billItemToUsersMap" -> billItemToUsersString,
            "userChoosingStatusMap" -> userChoosingStatusString
          )
        case FinishedChoosing(userAndPaymentMap) =>
          val users = ujson.Arr(userAndPaymentMap.keySet.map(userId => ujson.Str(userId)).toArray*)
          val payments = ujson.Arr(userAndPaymentMap.values.map(eachPayment => ujson.Num(eachPayment)).toArray*)
          val userAndPaymentString = ujson.Arr(users, payments)
          ujson.Obj(
            "view" -> "FinishedChoosing",
            "userAndPaymentMap" -> userAndPaymentString
          )

    def decode(json: Value): Try[BillSplittingView] =
      Try{
        val wholeObj = json.obj
        wholeObj("view").str match
          case "FinishedChoosing" => 
            val users = wholeObj("userAndPaymentMap").arr.apply(0).arr.map(ujsonStrUser => ujsonStrUser.str).toList
            val prices = wholeObj("userAndPaymentMap").arr.apply(1).arr.map(ujsonNumPrice => ujsonNumPrice.num).toList
            val userAndPaymentMap : Map[UserId, Double] = 
              users.zip(prices).toMap
            FinishedChoosing(userAndPaymentMap)
          case "Adding" =>
            val billItems = 
              wholeObj("billItemToPriceMap").arr.apply(0).arr.map(ujsonStrBillitem => ujsonStrBillitem.str).toList
            val prices = 
              wholeObj("billItemToPriceMap").arr.apply(1).arr.map(ujsonNumPrice => ujsonNumPrice.num).toList
            val billItemToPriceMap : Map[String, Double] = 
              billItems.zip(prices).toMap
            val finishedAddingStatus = wholeObj("finishedAddingStatus").bool
            Adding(billItemToPriceMap, finishedAddingStatus)
          case "Choosing" =>
            val billItems = 
              wholeObj("billItemToPriceMap").arr.apply(0).arr.map(ujsonStrBillitem => ujsonStrBillitem.str).toList
            val prices = 
              wholeObj("billItemToPriceMap").arr.apply(1).arr.map(ujsonNumPrice => ujsonNumPrice.num).toList
            val billItemToPriceMap : Map[String, Double] = 
              billItems.zip(prices).toMap

            val billItemsBasedOnUser = wholeObj("billItemToUsersMap").arr.apply(0).arr.map(ujsonStrBillItem => ujsonStrBillItem.str).toList
            val usersBasedOnBillItem = wholeObj("billItemToUsersMap").arr.apply(1).arr.map(eachListOfUsers => eachListOfUsers.arr.map(user => user.str).toList).toList
            val billItemToUsersMap : Map[UserId, List[String]] = 
              billItemsBasedOnUser.zip(usersBasedOnBillItem).toMap
            val choosingStatusUsers = wholeObj("userChoosingStatusMap").arr.apply(0).arr.map(ujsonStrUser => ujsonStrUser.str).toList
            val choosingStatus = wholeObj("userChoosingStatusMap").arr.apply(1).arr.map(ujsonChoosingStatusBool => ujsonChoosingStatusBool.bool).toList
            val userChoosingStatusMap: Map[UserId, Boolean] = 
              choosingStatusUsers.zip(choosingStatus).toMap
            Choosing(billItemToPriceMap, billItemToUsersMap, userChoosingStatusMap)
      }
