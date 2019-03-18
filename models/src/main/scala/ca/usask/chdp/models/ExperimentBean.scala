package ca.usask.chdp.models

import reflect.BeanProperty

case class ExperimentBean( _id: String,
                           @BeanProperty var experimentId: String = "")
