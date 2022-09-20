package ca.uhn.fhir.rest.param;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SearchDateRangeParamTest {
	@Test
	public void testSearchDateRangeParam(){
		DateRangeParam dateRangeParam = new DateRangeParam();
		int theOffset = 100;
		SearchDateRangeParam param = new SearchDateRangeParam(Map.of("Some key", new String[]{"value"}), dateRangeParam, theOffset);
		assertNull(param.getSearchParameterType());
		assertEquals(theOffset, param.getTheOffset());

		param = new SearchDateRangeParam(Map.of("_at", new String[]{"value"}), dateRangeParam, theOffset);
		assertEquals(HistorySearchTypeEnum.AT, param.getSearchParameterType());
		assertEquals(theOffset, param.getTheOffset());

		param = new SearchDateRangeParam(Map.of("_since", new String[]{"value"}), dateRangeParam, theOffset);
		assertEquals(HistorySearchTypeEnum.SINCE, param.getSearchParameterType());
		assertEquals(theOffset, param.getTheOffset());

		param = new SearchDateRangeParam(Map.of("_count", new String[]{"value"}), dateRangeParam, theOffset);
		assertEquals(HistorySearchTypeEnum.COUNT, param.getSearchParameterType());
		assertEquals(theOffset, param.getTheOffset());

	}
}
