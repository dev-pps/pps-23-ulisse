package ulisse.dsl

/** DSL for comparing fields of objects. */
object FieldsComparators:
  /** Field of an object. */
  trait Field[F <: Field[F, O], O <: Any]:
    self: F =>

    /** Seq of all the field of the object. */
    def values: Seq[F]

  /** Comparator for fields of objects. */
  trait FieldComparator[F <: Field[F, O], O <: Any]:
    /** Seq of all the field of the object. */
    def fields: Seq[F]

    /** Compare the fields of the objects. */
    final def compare(objects: List[O], ignoredFields: Seq[F]): Boolean =
      val fieldsToCompare = fields.filterNot(ignoredFields.contains)
      objects match
        case firstObject :: tail => tail.forall: otherObject =>
            fieldsToCompare.forall(_compare(firstObject, otherObject, _))
        case _ => false

    /** Compare the given field for the two objects. */
    protected def _compare(obj: O, otherObj: O, field: F): Boolean

  /** Conversion from a comparison builder to a boolean. */
  given [F <: Field[F, O], O <: Any]: Conversion[ComparisonBuilder[F, O], Boolean] with
    def apply(builder: ComparisonBuilder[F, O]): Boolean =
      builder.compare

  /** Builder for structure comparison. */
  case class ComparisonBuilder[F <: Field[F, O], O <: Any](objects: List[O], ignoredFields: Seq[F])(using
      fieldComparator: FieldComparator[F, O]
  ):
    /** Ignore the given fields. */
    def ignoring(field: F, fields: F*): ComparisonBuilder[F, O] =
      copy(ignoredFields = ignoredFields ++ (fields :+ field))

    /** Consider only the given fields. */
    def considering(field: F, fields: F*): ComparisonBuilder[F, O] =
      copy(ignoredFields = field.values.filterNot((fields :+ field).contains).toIndexedSeq)

    /** Add the next object to compare. */
    def andTo(nextObj: O): ComparisonBuilder[F, O] =
      copy(objects = nextObj +: objects)

    /** Compare the objects. */
    def compare: Boolean =
      fieldComparator.compare(objects, ignoredFields)

  extension [F <: Field[F, O], O <: Any](obj: O)
    /** Extension for comparing two objects. */
    def compareTo(otherObj: O)(using fieldComparator: FieldComparator[F, O]): ComparisonBuilder[F, O] =
      ComparisonBuilder(List(obj, otherObj), Seq[F]())
