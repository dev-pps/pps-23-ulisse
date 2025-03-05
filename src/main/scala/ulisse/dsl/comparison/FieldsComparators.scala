package ulisse.dsl.comparison

object FieldsComparators:

  trait Field[T <: Field[T, O], O <: Any]:
    self: T =>
    def values: Seq[T]

  trait FieldComparator[T <: Field[T, O], O <: Any]:
    def fields: Seq[T]
    final def compare(objects: List[O], ignoredFields: Seq[T]): Boolean =
      val fieldsToCompare = fields.filterNot(ignoredFields.contains)
      objects match
        case firstObject :: tail => tail.forall: otherObject =>
          fieldsToCompare.forall(_compare(firstObject, otherObject, _))
        case _ => false
    protected def _compare(obj: O, otherObj: O, field: T): Boolean

  given [T <: Field[T, O], O <: Any]: Conversion[ComparisonBuilder[T, O], Boolean] with
    def apply(builder: ComparisonBuilder[T, O]): Boolean =
      builder.compare

  case class ComparisonBuilder[T <: Field[T, O], O <: Any](objects: List[O], ignoredFields: Seq[T])(using
      fieldComparator: FieldComparator[T, O]
  ):
    def ignoring(field: T, fields: T*): ComparisonBuilder[T, O] =
      copy(ignoredFields = ignoredFields ++ (fields :+ field))

    def considering(field: T, fields: T*): ComparisonBuilder[T, O] =
      copy(ignoredFields = field.values.filterNot((fields :+ field).contains).toIndexedSeq)

    def andTo(nextObj: O): ComparisonBuilder[T, O] =
      copy(objects = nextObj +: objects)

    def compare: Boolean =
      fieldComparator.compare(objects, ignoredFields)

  extension [T <: Field[T, O], O <: Any](obj: O)
    def compareTo(otherObj: O)(using fieldComparator: FieldComparator[T, O]): ComparisonBuilder[T, O] =
      ComparisonBuilder(List(obj, otherObj), Seq[T]())
