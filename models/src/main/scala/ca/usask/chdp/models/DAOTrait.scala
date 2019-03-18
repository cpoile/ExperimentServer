package ca.usask.chdp.models

trait DAOTrait[T] {
  def findAll: List[T]
  def insertUpdate(bean: T): T
  def remove(bean: T): Boolean
  def validate(bean: T): (Boolean, T)
}
