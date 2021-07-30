
package de.fraunhofer.aisec.codyze.crymlin;

import de.fraunhofer.aisec.codyze.analysis.Finding;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ForbiddenTest extends AbstractMarkTest {

	@Test
	void testJava() throws Exception {
		Set<Finding> results = performTest(
			"unittests/forbidden.java", "unittests/forbidden.mark");

		Set<String> findings = results.stream()
				.map(Finding::toString)
				.collect(Collectors.toSet());
		assertEquals(
			5, findings.stream().filter(s -> s.contains("Violation against forbidden call")).count());

		assertTrue(
			findings.contains(
				"line 41: Violation against forbidden call(s) BotanF.set_key(_,_) in entity Forbidden. Call was b.set_key(nonce, iv);"));
		assertTrue(
			findings.contains(
				"line 36: Violation against forbidden call(s) BotanF.start(nonce: int,_) in entity Forbidden. Call was b.start(nonce, b);"));
		assertTrue(
			findings.contains(
				"line 35: Violation against forbidden call(s) BotanF.start() in entity Forbidden. Call was b.start();"));
		assertTrue(
			findings.contains(
				"line 38: Violation against forbidden call(s) BotanF.start_msg(...) in entity Forbidden. Call was b.start_msg(nonce);"));
		assertTrue(
			findings.contains(
				"line 39: Violation against forbidden call(s) BotanF.start_msg(...) in entity Forbidden. Call was b.start_msg(nonce, iv, b);"));
	}

	@Test
	void testCpp() throws Exception {
		var findings = performTest("unittests/forbidden.cpp", null);
		findings.forEach(System.out::println);
	}
}
