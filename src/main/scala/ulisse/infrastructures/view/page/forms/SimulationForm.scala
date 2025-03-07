package ulisse.infrastructures.view.page.forms

trait SimulationForm

object SimulationForm:
  def apply(): SimulationForm = SimulationFormImpl()

  private case class SimulationFormImpl() extends SimulationForm
