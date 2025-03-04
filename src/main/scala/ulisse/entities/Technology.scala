package ulisse.entities

/** The technology used by the train or by railway */
trait Technology:
  /** @return
    *   Name of technology
    */
  def name: String

  /** @return
    *   Max speed value
    */
  def maxSpeed: Int

  /** Check if technology is compatible with other technology. */
  def isCompatible(other: Technology): Boolean = maxSpeed <= other.maxSpeed

/** Factory for [[Technology]] instances. */
object Technology:
  /** Creates new technology type,
    *
    * @param name
    *   Name of technology
    * @param maxSpeed
    *   Max speed value supported or provided by technology.
    * @return
    *   [[Technology]] instance.
    */
  def apply(name: String, maxSpeed: Int): Technology =
    TechnologyImpl(name, maxSpeed)

  private case class TechnologyImpl(name: String, maxSpeed: Int)
      extends Technology
