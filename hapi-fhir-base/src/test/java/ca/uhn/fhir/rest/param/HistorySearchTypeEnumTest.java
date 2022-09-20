package ca.uhn.fhir.rest.param;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistorySearchTypeEnumTest {
	@Test
	public void testParse(){
		assertNull(HistorySearchTypeEnum.parse(""));
		assertNull(HistorySearchTypeEnum.parse(null));
		assertNull(HistorySearchTypeEnum.parse("Anything"));
		assertEquals(HistorySearchTypeEnum.AT, HistorySearchTypeEnum.parse("_at"));
		assertEquals(HistorySearchTypeEnum.SINCE, HistorySearchTypeEnum.parse("_since"));
		assertEquals(HistorySearchTypeEnum.COUNT, HistorySearchTypeEnum.parse("_count"));
	}

	@Test
	public void testIsAt(){
		assertTrue(HistorySearchTypeEnum.AT.isAt());
		assertFalse(HistorySearchTypeEnum.SINCE.isAt());
	}
}
