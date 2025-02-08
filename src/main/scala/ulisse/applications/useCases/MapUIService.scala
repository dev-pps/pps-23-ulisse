package ulisse.applications.useCases

trait MapUIService

object MapUIService:
  def apply(): MapUIService = MapUIServiceImpl()

  private case class MapUIServiceImpl() extends MapUIService
