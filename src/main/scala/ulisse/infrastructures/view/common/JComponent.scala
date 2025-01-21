package ulisse.infrastructures.view.common

import scala.swing.{Button, ComboBox, Label, TextField}

object JComponent:

  trait JLabel(label: String)
  object JLabel:
    def apply(string: String): JLabel = JLabelImpl(string)
    private case class JLabelImpl(label: String) extends Label(label) with JLabel(label)

  trait JTextField(string: String)
  object JTextField:
    def apply(string: String): JTextField = JTextFieldImpl(string)
    private case class JTextFieldImpl(string: String) extends TextField(string) with JTextField(string)

  trait JComoBox[T](objs: Seq[T])
  object JComoBox:
    def apply[T](objs: Seq[T]): JComoBox[T] = JComoBoxImpl(objs)
    private case class JComoBoxImpl[T](objs: Seq[T]) extends ComboBox(objs) with JComoBox[T](objs)

  trait JButton(label: String)
  object JButton:
    def apply(string: String): JButton = JButtonImpl(string)
    private case class JButtonImpl(label: String) extends Button(label) with JButton(label)
