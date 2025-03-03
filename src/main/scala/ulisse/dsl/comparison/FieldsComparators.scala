package ulisse.dsl.comparison

object FieldsComparators:

  trait Field[T <: Field[T, O], O <: Any]:
    self: T =>
    def values: Seq[T]

  trait FieldComparator[T <: Field[T, O], O <: Any]:
    def compare(engines: List[O], ignoredFields: Seq[T]): Boolean

  given [T <: Field[T, O], O <: Any]: Conversion[ComparisonBuilder[T, O], Boolean] with
    def apply(builder: ComparisonBuilder[T, O]): Boolean =
      builder.compare

  case class ComparisonBuilder[T <: Field[T, O], O <: Any](engines: List[O], ignoredFields: Seq[T])(using
      fieldComparator: FieldComparator[T, O]
  ):
    def ignoring(field: T, fields: T*): ComparisonBuilder[T, O] =
      copy(ignoredFields = ignoredFields ++ (fields :+ field))

    def considering(field: T, fields: T*): ComparisonBuilder[T, O] =
      copy(ignoredFields = field.values.filterNot((fields :+ field).contains).toIndexedSeq)

    def andTo(nextEngine: O): ComparisonBuilder[T, O] =
      copy(engines = nextEngine +: engines)

    def compare: Boolean =
      fieldComparator.compare(engines, ignoredFields)

  extension [T <: Field[T, O], O <: Any](engineState: O)
    def compareTo(otherEngineState: O)(using fieldComparator: FieldComparator[T, O]): ComparisonBuilder[T, O] =
      ComparisonBuilder(List(engineState, otherEngineState), Seq[T]())
