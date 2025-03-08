package ulisse.dsl

/** DSL for comparing fields of objects. */
object FieldsComparators:
  /** Field of an object. */
  trait Field[T <: Field[T, O], O <: Any]:
    self: T =>

    /** Seq of all the field of the object. */
    def values: Seq[T]

  /** Comparator for fields of objects. */
  trait FieldComparator[T <: Field[T, O], O <: Any]:
    /** Seq of all the field of the object. */
    def fields: Seq[T]

    /** Compare the fields of the objects. */
    final def compare(objects: List[O], ignoredFields: Seq[T]): Boolean =
      val fieldsToCompare = fields.filterNot(ignoredFields.contains)
      objects match
        case firstObject :: tail => tail.forall: otherObject =>
            fieldsToCompare.forall(_compare(firstObject, otherObject, _))
        case _ => false

    /** Compare the given field for the two objects. */
    protected def _compare(obj: O, otherObj: O, field: T): Boolean

  /** Conversion from a comparison builder to a boolean. */
  given [T <: Field[T, O], O <: Any]: Conversion[ComparisonBuilder[T, O], Boolean] with
    def apply(builder: ComparisonBuilder[T, O]): Boolean =
      builder.compare

  /** Builder for structure comparison. */
  case class ComparisonBuilder[T <: Field[T, O], O <: Any](objects: List[O], ignoredFields: Seq[T])(using
      fieldComparator: FieldComparator[T, O]
  ):
    /** Ignore the given fields. */
    def ignoring(field: T, fields: T*): ComparisonBuilder[T, O] =
      copy(ignoredFields = ignoredFields ++ (fields :+ field))

    /** Consider only the given fields. */
    def considering(field: T, fields: T*): ComparisonBuilder[T, O] =
      copy(ignoredFields = field.values.filterNot((fields :+ field).contains).toIndexedSeq)

    /** Add the next object to compare. */
    def andTo(nextObj: O): ComparisonBuilder[T, O] =
      copy(objects = nextObj +: objects)

    /** Compare the objects. */
    def compare: Boolean =
      fieldComparator.compare(objects, ignoredFields)

  /** Extension for comparing two objects. */
  extension [T <: Field[T, O], O <: Any](obj: O)
    def compareTo(otherObj: O)(using fieldComparator: FieldComparator[T, O]): ComparisonBuilder[T, O] =
      ComparisonBuilder(List(obj, otherObj), Seq[T]())
