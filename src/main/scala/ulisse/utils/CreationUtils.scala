package ulisse.utils

object CreationUtils:

  def updateWith[A, B <: A](obj: A)(transform: A => B): A = transform(obj)
