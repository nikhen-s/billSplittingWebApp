package driver

import cs214.webapp.*
import cs214.webapp.messages.Action
import billSplitting.*

class BillSplittingTest extends WebappTest[BillSplittingEvent, BillSplittingState, BillSplittingView]:
    val sm = billSplitting.BillSplittingStateMachine
    val wire = BillSplittingWire
    private val USER_IDS = Seq(UID0, UID1)
    private var currentAppId: String = _

    test("BillSplitting: Encoding and Decoding Event"):
        val encoded = wire.eventFormat.encode(BillSplittingEvent.FinishAdding)
        println(encoded)
        val decoded = wire.eventFormat.decode(encoded)
        println(decoded)
        assertEquals(1,1)
    
    test("BillSplitting: Encoding and Decoding View"):
        val encodedAdding = wire.viewFormat.encode(
            BillSplittingView.Adding(Map(("rice") -> 5.5, ("chicken") -> 10), false)
            )
        println(encodedAdding)
        val decodedAdding = wire.viewFormat.decode(encodedAdding)
        println(decodedAdding)
        
        val encodedChoosing = wire.viewFormat.encode(
            BillSplittingView.Choosing(
                Map("rice" -> 5.5, "chicken" -> 10), 
                Map("rice" -> List("user1"), "chicken" -> List("user2")), 
                Map("user1" -> true, "user2" -> true))
        )
        println(encodedChoosing)
        val decodedChoosing = wire.viewFormat.decode(encodedChoosing)
        println(decodedChoosing)
        
        val encodedFinishedChoosing = wire.viewFormat.encode(
            BillSplittingView.FinishedChoosing(
                Map("user1" -> 5.5, "user2" -> 10))
            )
            
        val decodedFinishedChoosing = wire.viewFormat.decode(encodedFinishedChoosing)
        println(encodedFinishedChoosing)
        println(decodedFinishedChoosing)

        assertEquals(1,1)


