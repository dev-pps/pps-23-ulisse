package ulisse.entities.station

import ulisse.entities.simulation.Environments.EnvironmentElement
import ulisse.entities.train.Trains.Train

trait StationEnvironmentElement extends Station with EnvironmentElement:
  val tracks: List[Track]
  def firstAvailableTrack: Option[Track] = tracks.find(_.train.isEmpty)
  def updateTrack(track: Track, train: Option[Train]): StationEnvironmentElement

object StationEnvironmentElement:
  def createStationEnvironmentElement(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(station, Track.generateSequentialTracks(station.numberOfTracks))

  private final case class StationEnvironmentElementImpl(station: Station, tracks: List[Track])
      extends StationEnvironmentElement:
    export station.*

    def updateTrack(track: Track, train: Option[Train]): StationEnvironmentElement =
      copy(tracks = tracks.map(t => if t == track then t.withTrain(train) else t))
