package billSplitting

import scala.collection.immutable.Queue
import scala.util.{Failure, Success, Try}

import billSplitting.* 
import cs214.webapp.*
import cs214.webapp.exceptions.*
import cs214.webapp.server.WebServer
import cs214.webapp.messages.Action

object BillSplittingStateMachine extends cs214.webapp.StateMachine[BillSplittingEvent, BillSplittingState, BillSplittingView]:

  val name: String = "BillSplitting"
  val wire = BillSplittingWire

  override def init(clients: Seq[UserId]): BillSplittingState =

    val billItemToPriceMap = Map[String, Double]()
    val billItemToUsersMap = Map[String, List[UserId]]()
    val userStatusMap = clients.map(userId => (userId -> false)).toMap
    Bill(billItemToPriceMap, billItemToUsersMap, clients, userStatusMap, false)

  // Failures in the Try must hold instances of AppException
  // (from Exceptions.scala under lib/shared/)
  override def transition(state: BillSplittingState)(uid: UserId, event: BillSplittingEvent): Try[Seq[Action[BillSplittingState]]] =
    Try{
    event match
      case BillSplittingEvent.Add(itemName, price) => 
        val newBillItemToPriceMap = state.billItemToPriceMap + (itemName -> price)
        val newBillItemToUsersMap = state.billItemToUsersMap + (itemName -> List())
        val newState = Bill(newBillItemToPriceMap, newBillItemToUsersMap, state.users, state.userChoosingStatusMap, false)
        Seq(messages.Action.Render(newState))

      case BillSplittingEvent.Choose(itemName, price) =>
        if state.finishedAddingStatus == false then Seq(messages.Action.Alert("Click finish to finish adding!"))
        else if state.billItemToPriceMap.isEmpty then Seq(messages.Action.Alert("Add at least 1 item and price to the bill!"))
        val newBillItemToPriceMap = state.billItemToPriceMap
        val newBillItemToUsersMap = state.billItemToUsersMap.updated(itemName, uid:: state.billItemToUsersMap.get(itemName).head)

        val newState = Bill(newBillItemToPriceMap, newBillItemToUsersMap, state.users, state.userChoosingStatusMap, state.finishedAddingStatus)
        Seq(messages.Action.Render(newState))
      
      case BillSplittingEvent.FinishAdding =>
        if state.billItemToPriceMap.isEmpty then Seq(messages.Action.Alert("Add at least 1 item and price to the bill!"))
        val newState = Bill(state.billItemToPriceMap, state.billItemToUsersMap, state.users, state.userChoosingStatusMap, true)
        Seq(messages.Action.Render(newState))
      
      case BillSplittingEvent.FinishChoosing =>
        val newUserChoosingStatusMap = state.userChoosingStatusMap.updated(uid, true)
        val newState = Bill(state.billItemToPriceMap, state.billItemToUsersMap, state.users, newUserChoosingStatusMap, state.finishedAddingStatus)
        Seq(messages.Action.Render(newState))
      }
    

  override def project(state: BillSplittingState)(uid: UserId): BillSplittingView =
    val finishedChoosingStatus = state.userChoosingStatusMap.forall((userId, finishedStatus)=>finishedStatus == true)
    val finishedAddingStatus = state.finishedAddingStatus
    if finishedAddingStatus && state.userChoosingStatusMap.get(uid).head == true then
      if finishedChoosingStatus == true then
        //convert the userToBillItem to Bill Item to User then select 
        val userToPaymentMap = 
          state.users.map(
            user =>
              user ->
              state.billItemToUsersMap.map(
                (itemName, listOfUsers) => 
                  if listOfUsers.contains(user) then (state.billItemToPriceMap.get(itemName).head / listOfUsers.length)
                  else 0.0
              ).sum
          ).toMap
        BillSplittingView.FinishedChoosing(userToPaymentMap)
      else
        BillSplittingView.FinishedChoosing(Map[UserId,Double]())
    else if finishedAddingStatus == true then 
      BillSplittingView.Choosing(state.billItemToPriceMap, state.billItemToUsersMap, state.userChoosingStatusMap)
    else
      BillSplittingView.Adding(state.billItemToPriceMap, state.finishedAddingStatus) //this is the first state

// Server registration magic
class register:
  WebServer.register(BillSplittingStateMachine)
