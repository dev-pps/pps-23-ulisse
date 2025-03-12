package ulisse.entities

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ulisse.entities.train.Trains.TrainTechnology

class TrainTechnologyTest extends AnyFlatSpec with Matchers:
  private val name         = "HighSpeed"
  private val maxSpeed     = 300
  private val acceleration = 1.0
  private val deceleration = 0.5
  private val technology   = TrainTechnology(name, maxSpeed, acceleration, deceleration)

  "A TrainTechnology" should "provide name, max speed, acceleration and deceleration" in:
    technology.name should be(name)
    technology.maxSpeed should be(maxSpeed)
    technology.acceleration should be(acceleration)
    technology.deceleration should be(deceleration)
