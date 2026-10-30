package de.dfki.asr.ajan.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * Characterization of the CSV parsing in {@link AgentUtil#getCSVFromStream}.
 * Must stay identical across the opencsv 4.6 -> 5.12 bump.
 */
public class AgentUtilCsvTest {

	@Test
	public void csvRoundTripOfQuotedFieldsIsUnchanged() throws IOException {
		String csv = "id,text,note\n"
				+ "1,\"a, b\",plain\n"
				+ "2,\"say \"\"hi\"\"\",x\n"
				+ "3,\"line1\nline2\",y\n";
		CSVInput input = AgentUtil.getCSVFromStream(
				new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
		List<String[]> rows = input.getContent();
		assertEquals(rows.size(), 4);
		assertEquals(rows.get(0), new String[] {"id", "text", "note"});
		assertEquals(rows.get(1), new String[] {"1", "a, b", "plain"});
		assertEquals(rows.get(2), new String[] {"2", "say \"hi\"", "x"});
		assertEquals(rows.get(3), new String[] {"3", "line1\nline2", "y"});
	}
}
