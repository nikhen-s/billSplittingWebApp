
package billSplitting

import cs214.webapp.UserId
import scala.util.{Failure, Success, Try}

/** Stores all information about the current Bill. */
type BillSplittingState = Bill 

enum BillSplittingEvent:
  /** User chooses which item they bought */
  case Add(itemName: String, price: Double)
  case FinishAdding
  case Choose(name: String, price: Double)
  case FinishChoosing

enum BillSplittingView:
  /** Adding in progress. */
  case Adding(billItemToPriceMap : Map[String, Double], finishedAddingStatus : Boolean)
  /** Choosing in progress. */
  case Choosing(billItemToPriceMap : Map[String, Double], billItemToUsersMap: Map[String, List[UserId]], userChoosingStatusMap: Map[UserId, Boolean])
  /** Everyone has finished choosing what they bought */
  case FinishedChoosing(userAndPaymentMap: Map[UserId, Double]) 


case class Bill(val billItemToPriceMap: Map[String, Double], val billItemToUsersMap: Map[String, List[UserId]], val users: Seq[UserId], val userChoosingStatusMap: Map[String, Boolean], val finishedAddingStatus : Boolean) 

