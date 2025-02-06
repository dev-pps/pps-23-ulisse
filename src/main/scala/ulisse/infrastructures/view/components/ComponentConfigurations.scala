package ulisse.infrastructures.view.components

import scala.swing.Component

object ComponentConfigurations:
  enum Alignment:
    case Left, Right, Top, Bottom, Center

  trait ComponentConfiguration:
    val alignment: Option[Alignment]

  object ComponentConfiguration:
    def empty(): ComponentConfiguration                     = ComponentConfigurationImpl(None)
    def apply(alignment: Alignment): ComponentConfiguration = ComponentConfigurationImpl(Some(alignment))

    private case class ComponentConfigurationImpl(alignment: Option[Alignment]) extends ComponentConfiguration

  trait ComponentWithConfiguration[C <: Component]:
    val component: C
    val configuration: ComponentConfiguration

  object ComponentWithConfiguration:
    def apply[C <: Component](component: C, configuration: ComponentConfiguration): ComponentWithConfiguration[C] =
      ComponentWithConfigurationImpl(component, configuration)
    private case class ComponentWithConfigurationImpl[C <: Component](
        component: C,
        configuration: ComponentConfiguration
    ) extends ComponentWithConfiguration[C]
