package ca.uhn.fhir.jpa.dao.search;

import ca.uhn.fhir.jpa.model.entity.ResourceIndexedSearchParamDate;
import ca.uhn.fhir.jpa.model.entity.ResourceIndexedSearchParamQuantity;
import ca.uhn.fhir.jpa.model.entity.ResourceIndexedSearchParamString;
import ca.uhn.fhir.jpa.model.entity.ResourceIndexedSearchParamToken;
import ca.uhn.fhir.jpa.model.search.CompositeSearchIndexData;
import ca.uhn.fhir.jpa.model.search.HSearchElementCache;
import ca.uhn.fhir.jpa.model.search.HSearchIndexWriter;
import ca.uhn.fhir.jpa.searchparam.extractor.ResourceIndexedSearchParamComposite;
import ca.uhn.fhir.model.api.Tag;
import ca.uhn.fhir.util.ObjectUtil;
import org.hibernate.search.engine.backend.document.DocumentElement;

/**
 * binding of HSearch apis into
 *
 * We have a diamond dependency pattern, and searchparam and hsearch aren't friends.  Bring them together here.
 */
class HSearchCompositeSearchIndexDataImpl implements CompositeSearchIndexData {

	final ResourceIndexedSearchParamComposite mySearchParamComposite;

	public HSearchCompositeSearchIndexDataImpl(ResourceIndexedSearchParamComposite theSearchParamComposite) {
		mySearchParamComposite = theSearchParamComposite;
	}

	/**
	 * Write a nested index document for this composite.
	 * We use a nested document to support correlation queries on the same parent element for
	 * proper composite SP semantics.  Each component will have a sub-node for each component SP.
	 *
	 * Example for component-code-value-quantity, which composes
	 * component-code and component-value-quantity:
	 <pre>
	 { "nsp: {
	 "component-code-value-quantity": [
	 {
	 "component-code": {
	 "token": {
	 "code": "8480-6",
	 "system": "http://loinc.org",
	 "code-system": "http://loinc.org|8480-6"
	 }
	 },
	 "component-value-quantity": {
	 "quantity": {
	 "code": "mmHg",
	 "system": "null",
	 "value": 60.0
	 }
	 }
	 },
	 {
	 "component-code": {
	 "token": {
	 "code": "3421-5",
	 "system": "http://loinc.org",
	 "code-system": "http://loinc.org|3421-5"
	 }
	 },
	 "component-value-quantity": {
	 "quantity": {
	 "code": "mmHg",
	 "system": "null",
	 "value": 100.0
	 }
	 }
	 }
	 ]
	 }
	 </pre>
	 *
	 * @param theRoot our cache wrapper around the root HSearch DocumentElement
	 */
	@Override
	public void writeIndexEntry(HSearchIndexWriter theHSearchIndexWriter, HSearchElementCache theRoot) {
		// optimization - An empty sub-component will never match.
		// Storing the rest only wastes resources
		boolean hasAnEmptyComponent =
			mySearchParamComposite.getComponents().stream()
				.anyMatch(c->c.getParamIndexValues().isEmpty());

		if (hasAnEmptyComponent) {
			return;
		}

		DocumentElement nestedParamRoot = theRoot.getObjectElement(HSearchIndexWriter.NESTED_SEARCH_PARAM_ROOT);

		// we want to re-use the `token`, `quantity` nodes for multiple values.
		DocumentElement compositeRoot = nestedParamRoot.addObject(mySearchParamComposite.getSearchParamName());


		for (ResourceIndexedSearchParamComposite.Component subParam : mySearchParamComposite.getComponents()) {
			// Write the various index nodes.
			// Note: we don't support modifiers with composites, so we don't bother to index :of-type, :text, etc.
			DocumentElement subParamElement = compositeRoot.addObject(subParam.getSearchParamName());
			switch (subParam.getSearchParameterType()) {
				case DATE:
					DocumentElement dateElement = subParamElement.addObject("dt");
					subParam.getParamIndexValues().stream()
						.flatMap(o->ObjectUtil.castIfInstanceof(o, ResourceIndexedSearchParamDate.class).stream())
						.map(ExtendedHSearchIndexExtractor::convertDate)
						.forEach(d-> theHSearchIndexWriter.writeDateFields(dateElement, d));
					break;

				case QUANTITY:
					DocumentElement quantityElement =  subParamElement.addObject(HSearchIndexWriter.QTY_IDX_NAME);
					subParam.getParamIndexValues().stream()
						.flatMap(o->ObjectUtil.castIfInstanceof(o, ResourceIndexedSearchParamQuantity.class).stream())
						.map(ExtendedHSearchIndexExtractor::convertQuantity)
						.forEach(q-> theHSearchIndexWriter.writeQuantityFields(quantityElement, q));
					break;

				case STRING:
					DocumentElement stringElement =  subParamElement.addObject("string");
					subParam.getParamIndexValues().stream()
						.flatMap(o->ObjectUtil.castIfInstanceof(o, ResourceIndexedSearchParamString.class).stream())
						.forEach(risps-> theHSearchIndexWriter.writeBasicStringFields(stringElement, risps.getValueExact()));
					break;

				case TOKEN:
					DocumentElement tokenElement =  subParamElement.addObject("token");
					subParam.getParamIndexValues().stream()
						.flatMap(o->ObjectUtil.castIfInstanceof(o, ResourceIndexedSearchParamToken.class).stream())
						.forEach(rispt-> theHSearchIndexWriter.writeTokenFields(tokenElement, new Tag(rispt.getSystem(), rispt.getValue())));
				break;

				// wipmb implement these
				case URI:
				case NUMBER:
				case REFERENCE:
					break;

				case COMPOSITE:
					assert false: "composite components can't be composite";
					break;

				// unsupported
				case SPECIAL:
				case HAS:
					break;
			}
		}
	}
}
