package com.ingenuiq.note.utils

import java.io.File
import java.net.URLClassLoader

import org.clapper.classutil.{ ClassFinder, ClassInfo }

trait ClassUtils {

  def toClass: ClassInfo => Class[_] = impl => Class.forName(impl.name)

  def removeLastSymbolFromName(o: Any): String = o.getClass.getName.dropRight(1)

  def implementationsOf(clazz: Class[_], filter: Option[String] = None): List[ClassInfo] = {
    val classFiles = ClassFinder(
      (Thread.currentThread().getContextClassLoader match {
        case classLoader: URLClassLoader => classLoader.getURLs
        case classLoader => classLoader.getParent.asInstanceOf[URLClassLoader].getURLs
      }).toList
        .filter(x => filter.forall(x.getPath.contains))
        .map(x => new File(x.getPath.replaceAll("%20", " ")))
    )

    findImplementations(clazz.getName, ClassFinder.classInfoMap(classFiles.getClasses().iterator))
  }

  private def findImplementations(ancestor: String, classes: Map[String, ClassInfo]): List[ClassInfo] =
    classes.get(ancestor).fold(List.empty[ClassInfo]) { ancestorInfo =>
      def compare(info: ClassInfo): Boolean =
        info.name == ancestorInfo.name || (info.superClassName :: info.interfaces).exists(n => classes.get(n).exists(compare))

      classes.valuesIterator.filter(info => info.isConcrete && compare(info)).toList
    }
}
