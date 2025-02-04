package ulisse.applications.managers

import ulisse.entities.Technology
import ulisse.utils.Errors.{BaseError, ErrorMessage, ErrorNotExist, ErrorValidation}

object TechnologyManagers:
  /** Train TechnologyManager errors that can be returned after a request.
    */
  sealed trait TechErrors extends BaseError
  object TechErrors:
    final case class InvalidSpeed(speed: Int) extends ErrorValidation(s"technology speed value $speed") with TechErrors
    final case class TechnologyAlreadyExists(name: String) extends ErrorMessage(s"technology $name") with TechErrors
    final case class TechnologyNotExists(name: String)     extends ErrorNotExist(s"technology $name") with TechErrors

  trait TechnologyManager[T <: Technology]:
    /** Add new technology if is valid and there no duplicate
      * @param technology
      *   Technology to add
      * @return
      *   Returns [[Right]] of updated `TechnologyManager` if technology is added else [[Left]] of
      *   [[TechnologyAlreadyExists]] error
      */
    def add(technology: T): Either[TechErrors, TechnologyManager[T]]

    /** Remove technology if exist
      *
      * @param name
      *   Name of technology
      * @return
      *   Returns [[Right]] of updated `TechnologyManager` if technology is removed else [[Left]] of
      *   [[TechnologyNotExists]] error
      */
    def remove(name: String): Either[TechErrors, TechnologyManager[T]]

    /** @return
      *   List of saved technologies
      */
    def technologiesList: List[T]

    /** @param name
      *   name of technology
      */
    def getBy(name: String): Either[TechErrors, T]

  object TechnologyManager:
    /** @param technologies
      *   Technologies saved
      * @return
      *   `TechnologyManager`
      */
    def apply[T <: Technology](technologies: List[T]): TechnologyManager[T] =
      TechnologyManagerImpl(technologies.map(t => (t.name, t)).toMap)

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
