package ulisse.applications.useCases.train

import ulisse.entities.train.Technology
import ulisse.utils.Errors.{BaseError, ErrorMessage, ErrorNotExist, ErrorValidation}

object TechnologyManagers:

  sealed trait TechErrors extends BaseError
  object TechErrors:
    final case class InvalidSpeed(speed: Int) extends ErrorValidation(s"technology speed value $speed") with TechErrors
    final case class TechnologyAlreadyExists(name: String) extends ErrorMessage(s"technology $name") with TechErrors
    final case class TechnologyNotExists(name: String)     extends ErrorNotExist(s"technology $name") with TechErrors

  trait TechnologyManager:
    def add(technology: Technology): Either[TechErrors, TechnologyManager]
    def remove(name: String): Either[TechErrors, TechnologyManager]
    def technologiesList: List[Technology]

  object TechnologyManager:

    def apply(technologies: List[Technology]): TechnologyManager =
      TechnologyManagerImpl(technologies.map(t => (t.name, t)).toMap)

    private case class TechnologyManagerImpl(technologies: Map[String, Technology]) extends TechnologyManager:
      def add(technology: Technology): Either[TechErrors, TechnologyManager] =
        for
          t <- technology.validate
          ts <- technologies.get(t.name)
            .map(_ => TechErrors.TechnologyAlreadyExists(t.name)).toLeft(
              technologies.updated(technology.name, technology)
            )
        yield TechnologyManager(ts.values.toList)

      def technologiesList: List[Technology] = technologies.values.toList

      def remove(name: String): Either[TechErrors, TechnologyManager] =
        technologies.get(name).map(t =>
          TechnologyManager(technologies.removed(t.name).values.toList)
        ).toRight(TechErrors.TechnologyNotExists(name))

      extension (t: Technology)
        private def validate: Either[TechErrors, Technology] =
          t.maxSpeed > 0 match
            case true => Right(t)
            case _    => Left(TechErrors.InvalidSpeed(t.maxSpeed))
