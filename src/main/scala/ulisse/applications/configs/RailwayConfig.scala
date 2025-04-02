package ulisse.applications.configs

import ulisse.applications.AppState

/** Railway configuration. */
trait RailwayConfig:
  def config: AppState

/** Factory for [[RailwayConfig]]. */
object RailwayConfig:
  private val simpleRailway: AppState  = RailwayBaseConfig.config
  private val complexRailway: AppState = RailwayComplexConfig.config
  private val exampleRailway: AppState = RailwayExamplesConfig.config

  /** Get the simple railway configuration. */
  def simpleRailwayConfig: AppState = simpleRailway

  /** Get the complex railway configuration. */
  def complexRailwayConfig: AppState = complexRailway

  /** Get the example railway configuration. */
  def exampleRailwayConfig: AppState = exampleRailway
