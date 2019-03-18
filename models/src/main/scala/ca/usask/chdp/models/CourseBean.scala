package ca.usask.chdp.models

import reflect.BeanProperty

case class CourseBean( _id: String,
                       @BeanProperty var courseId: String = "",
                       @BeanProperty var name: String = "",
                      @BeanProperty var section: String = "",
                      @BeanProperty var courseTime: String = "",
                      @BeanProperty var professor: String = "") {

  override def toString = if (courseId == "") "" else name + ", sec " + section + ", Time: " + courseTime
}
