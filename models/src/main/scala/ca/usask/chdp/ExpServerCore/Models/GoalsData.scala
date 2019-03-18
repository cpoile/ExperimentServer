package ca.usask.chdp.ExpServerCore.Models

import reflect.BeanProperty


case class GoalsData(@BeanProperty var p1DataGoal: Int = 99,
                     @BeanProperty var p2DataGoal: Int = 99,
                     @BeanProperty var p3DataGoal: Int = 99)
