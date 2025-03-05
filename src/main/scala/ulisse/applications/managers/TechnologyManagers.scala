package ulisse.applications.managers

import ulisse.entities.Technology
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.utils.Errors.{BaseError, ErrorMessage, ErrorNotExist, ErrorValidation}

object TechnologyManagers:
  /** Train TechnologyManager errors that can be returned after a request. */
  sealed trait TechErrors extends BaseError
  object TechErrors:
    final case class InvalidSpeed(speed: Int) extends ErrorValidation(s"technology speed value $speed") with TechErrors
    final case class TechnologyAlreadyExists(name: String) extends ErrorMessage(s"technology $name") with TechErrors
    final case class TechnologyNotExists(name: String)     extends ErrorNotExist(s"technology $name") with TechErrors

  trait TechnologyManager[T <: Technology]:
    /** Adds `technology` if is valid and there no duplicate
      *
      * Returns `Right` of updated `TechnologyManager` if technology is added else [[Left]] of [[TechnologyAlreadyExists]] error
      */
    def add(technology: T): Either[TechErrors, TechnologyManager[T]]

    /** Remove technology with given `name` if exist
      *
      * Returns Right of updated `TechnologyManager` if technology is removed else [[Left]] of
      * [[TechnologyNotExists]] error
      */
    def remove(name: String): Either[TechErrors, TechnologyManager[T]]

    /** Returns List of saved technologies of type `T` */
    def technologiesList: List[T]

    /** Returns technology given its `name` */
    def getBy(name: String): Either[TechErrors, T]

  object TechnologyManager:
    /** Returns `TechnologyManager` initialized with `technologies` */
    def apply[T <: Technology](technologies: List[T]): TechnologyManager[T] =
      TechnologyManagerImpl(technologies.map(t => (t.name, t)).toMap)

    /** Creates a [[TechnologyManager]] with default train technologies */
    def createTrainTechnology(): TechnologyManager[TrainTechnology] =
      val defaultTrainTechnology = List(TrainTechnology("AV", 300, 2.0, 1.0), TrainTechnology("Normal", 160, 1, 0.5))
      TechnologyManager(defaultTrainTechnology)

    /** Create an empty [[TechnologyManager]] with [[TrainTechnology]]. */
    def empty(): TechnologyManager[TrainTechnology] = TechnologyManager(List.empty)

    private case class TechnologyManagerImpl[T <: Technology](technologies: Map[String, T])
        extends TechnologyManager[T]:
      def add(technology: T): Either[TechErrors, TechnologyManager[T]] =
        for
          t <- technology.validate
          ts <- technologies.get(t.name)
            .map(_ => TechErrors.TechnologyAlreadyExists(t.name)).toLeft(
              technologies.updated(technology.name, technology)
            )
        yield TechnologyManager[T](ts.values.toList)

      def technologiesList: List[T] = technologies.values.toList

      def remove(name: String): Either[TechErrors, TechnologyManager[T]] =
        for
          t <- getBy(name)
        yield TechnologyManager(technologies.removed(t.name).values.toList)

      def getBy(name: String): Either[TechErrors, T] =
        technologiesList.find(_.name.contentEquals(name)).toRight(TechErrors.TechnologyNotExists(
          name
        ))

      extension (t: Technology)
        private def validate: Either[TechErrors, Technology] =
          if t.maxSpeed > 0 then Right(t) else Left(TechErrors.InvalidSpeed(t.maxSpeed))
