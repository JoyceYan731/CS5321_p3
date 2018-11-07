package operators;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IndexScanOperatorTest {

	@Test
	void test() {
		Operator test = new IndexScanOperator("Boats", "Boats", "E", 35, 10000);
		test.dump();
	}
}
