package ulisse.entities.station

import ulisse.entities.simulation.Environments.EnvironmentElement
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.*

trait StationEnvironmentElement extends Station with EnvironmentElement:
  val tracks: List[Track]
  def firstAvailableTrack: Option[Track] = tracks.find(_.train.isEmpty)
  def updateTrack(track: Track, train: Option[Train]): StationEnvironmentElement

object StationEnvironmentElement:
  def createStationEnvironmentElement(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(station, Track.generateSequentialTracks(station.numberOfTracks))

  extension (train: Train)
    def arriveAt(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.firstAvailableTrack.map(track => station.updateTrack(track, Some(train)))

    def leave(station: StationEnvironmentElement): Option[StationEnvironmentElement] =
      station.tracks.find(_.train.contains(train)).map(track => station.updateTrack(track, None))

    def findInStation(stations: Seq[StationEnvironmentElement]): Option[StationEnvironmentElement] =
      stations.find(_.tracks.contains(train))

  private final case class StationEnvironmentElementImpl(station: Station, tracks: List[Track])
      extends StationEnvironmentElement:
    export station.*

    def updateTrack(track: Track, train: Option[Train]): StationEnvironmentElement =
      copy(tracks = tracks.updateWhen(_ == track)(_.withTrain(train)))
