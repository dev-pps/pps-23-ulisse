package ulisse.infrastructure.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyle

class StyleServiceTest extends AnyFlatSpec with Matchers:

  "create empty style service" should "have no style" in:
    val styleService = JStyle.empty()

    styleService.all must be(Seq.empty[JStyle])
    styleService.colorPalette must be(None)
    styleService.border must be(None)
  
  "create "