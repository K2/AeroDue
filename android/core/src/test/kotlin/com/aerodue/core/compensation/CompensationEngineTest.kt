package com.aerodue.core.compensation

import com.aerodue.core.demo.DemoFixtures
import com.aerodue.core.domain.CompensationSource
import org.junit.Assert.assertTrue
import org.junit.Test

class CompensationEngineTest {

    @Test
    fun delayedConnectionFindsMultipleSources() {
        val claims = CompensationEngine.assess(
            DemoFixtures.delayedConnectionEvent,
            DemoFixtures.demoProfile,
        )
        val sources = claims.map { it.source }.toSet()
        assertTrue(CompensationSource.REGULATION_DOT in sources)
        assertTrue(CompensationSource.CREDIT_CARD in sources)
        assertTrue(CompensationSource.BUSINESS_POLICY in sources)
    }
}
