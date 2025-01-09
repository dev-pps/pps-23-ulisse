package scala.view

import scala.swing.{FlowPanel, Label, TextField}

trait FieldPanel:
  def setLabel(text: String): FieldPanel
  def text: String

object FieldPanel:
  def apply(text: String): FieldPanel = FieldPanelImpl(text)

  private case class FieldPanelImpl(title: String) extends FlowPanel,
        FieldPanel:
    private val field: TextField = TextField(5)
    private val label: Label     = Label(title)

    contents += field
    contents += label

    override def setLabel(text: String): FieldPanel = FieldPanel(text)

    override def text: String = field.text
