package ulisse.adapters.output

import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
import ulisse.infrastructures.commons.TimeProviders.TimeProvider

class UtilityAdaptersTest extends AnyWordSpec with Matchers:
  private val mockedTimeProvider  = mock[TimeProvider]
  private val timeProviderAdapter = TimeProviderAdapter(mockedTimeProvider)

  "TimeProviderAdapter" should:
    "call currentTimeMillis on TimeProvider when currentTimeMillis is called" in:
      val mockedCurrentTimeMillis = 1000L
      when(mockedTimeProvider.currentTimeMillis()).thenReturn(mockedCurrentTimeMillis)
      timeProviderAdapter.currentTimeMillis() shouldBe mockedCurrentTimeMillis
      verify(mockedTimeProvider).currentTimeMillis()
