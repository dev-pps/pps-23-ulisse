package ulisse.infrastructures.view.page

trait Workspace

object Workspace:
  def apply(): Workspace = WorkspaceImpl()

  private case class WorkspaceImpl() extends Workspace
