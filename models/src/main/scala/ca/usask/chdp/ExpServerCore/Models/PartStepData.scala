package ca.usask.chdp.ExpServerCore.Models

import reflect.BeanProperty

/**
 * PartStep is the data structure for each day. Tells us what each part will look like on that day of work.
 * Part1 = Player A: Engine           / Player B: Air Intake System
 * Part2 = Player A: Powertrain       / Player B: Wing Aerodynamics
 * Part3 = Player A: Wheel Assembly   / Player B: Suspension
 */
case class PartStepData(@BeanProperty var curName: String = "MarkI",
                        @BeanProperty var curData: Int = 0,
                        @BeanProperty var nextName: String = "MarkI",
                        @BeanProperty var nextData: Int = 0,
                        @BeanProperty var chance: Int = 0,
                        @BeanProperty var statusBar: String = "",
                        @BeanProperty var isComplAfterThis: Boolean = false)
