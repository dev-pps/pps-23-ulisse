package ulisse.entities.train

/** The technology used by the train that define max speed bound of train
  */
trait Technology:
  /** @return
    *   Name of technology
    */
  def name: String

  /** @return
    *   Max speed value
    */
  def maxSpeed: Int

  /** @return
    *   train acceleration
    */
  def acceleration: Double

  /** @return
    *   train deceleration
    */
  def deceleration: Double

/** Factory for [[train.model.Trains.TechnologyType]] instances.
  */
object Technology:
  /** Creates new technology type,
    *
    * @param name
    *   Name of technology
    * @param maxSpeed
    *   Max speed reachable by train with this technology
    * @return
    *   [[Technology]] instance.
    */
  def apply(name: String, maxSpeed: Int, acceleration: Double, deceleration: Double): Technology =
    TechnologyImpl(name, maxSpeed, acceleration, deceleration)

  private case class TechnologyImpl(name: String, maxSpeed: Int, acceleration: Double, deceleration: Double)
      extends Technology
