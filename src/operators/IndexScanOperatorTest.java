package operators;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IndexScanOperatorTest {

	@Test
	void test() {
		Operator test = new IndexScanOperator("Boats", "B", "E", 125, null);
		test.dump();
	}
}
