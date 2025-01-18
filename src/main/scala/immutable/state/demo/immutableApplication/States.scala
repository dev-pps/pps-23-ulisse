package immutable.state.demo.immutableApplication

import immutable.state.demo.immutableApplication.Monads.Monad

object States:

  //  // data structure for state (evolution)
  //  // a state is/has a function evolving S and producing a result A
  final case class State[S, A](run: S => (S, A))

  //
  //  // minimal set of algorithms
  object State:
    // a facility to run the state on an initial `s`
    extension [S, A](m: State[S, A])
      def apply(s: S): (S, A) = m match
        case State(run) => run(s)

  // define a given the works on all S, shall use "type lambdas"
  given stateMonad[S]: Monad[[A] =>> State[S, A]] with
    // unit: a state with no evolution, just the result
    def unit[A](a: A): State[S, A] = State(s => (s, a))

    // flatMap: runs the state, use result to create a new state
    extension [A](m: State[S, A])
      override def flatMap[B](f: A => State[S, B]): State[S, B] =
        State(s =>
          m.apply(s) match
            case (s2, a) => f(a).apply(s2)
        )
