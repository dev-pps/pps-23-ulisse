package ulisse

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Runner.runAll

import java.util.concurrent.LinkedBlockingQueue
import scala.jdk.CollectionConverters.*

object Runner:

  /** Processes a queue of state transformation functions and computes the sequence of states.
    *
    * This function starts with an initial state and applies each function from the provided `LinkedBlockingQueue` in
    * sequence, producing a list of intermediate states, including the initial state.
    *
    * @tparam S
    *   The type of the state being processed.
    * @param initialState
    *   The initial state of type `S` to begin the computation.
    * @param queue
    *   A `LinkedBlockingQueue` containing functions of type `S => S`, where each function represents a transformation
    *   to be applied to the state.
    */
  def runAll[S](initialState: S, queue: LinkedBlockingQueue[S => S]): List[S] =
    val elements = java.util.ArrayList[S => S]()
    queue.drainTo(elements)
    elements.asScala.toList.scanLeft(initialState)((state, event) => event(state))

class RunnerTest extends AnyWordSpec with Matchers:

  "runAll" should:
    "return just the initial state when the queue is empty" in:
      val queue = LinkedBlockingQueue[String => String]()
      runAll("", queue) shouldEqual List("")

    "correctly apply all transformations from the queue to the initial state" in:
      val queue = LinkedBlockingQueue[String => String]()
      queue.add(state => state + "A")
      queue.add(state => state + "B")
      queue.add(state => state + "C")
      runAll("", queue) shouldEqual List("", "A", "AB", "ABC")
