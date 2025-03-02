package ulisse.entities

/** The technology used by the train or by railway */
trait Technology:
  /** Returns name of technology */
  def name: String

  /** Returns max speed value */
  def maxSpeed: Int

  /** Check if technology is compatible with other technology. */
  def isCompatible(other: Technology): Boolean = maxSpeed <= other.maxSpeed

/** Factory for [[Technology]] instances. */
object Technology:
  /** Creates new technology type given `name` of technology and `maxSpeed` value supported. */
  def apply(name: String, maxSpeed: Int): Technology =
    TechnologyImpl(name, maxSpeed)

  /** Defines ordering for [[Technology]] */
  given Ordering[Technology] with
    def compare(x: Technology, y: Technology): Int = x.maxSpeed - y.maxSpeed

  private case class TechnologyImpl(name: String, maxSpeed: Int)
      extends Technology
