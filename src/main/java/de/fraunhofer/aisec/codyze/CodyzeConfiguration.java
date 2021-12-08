
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.checkerframework.checker.nullness.qual.NonNull;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

public class CodyzeConfiguration implements Callable<Integer> {
	// TODO: names

	@JsonIgnore
	@CommandLine.ArgGroup(exclusive = true, multiplicity = "1", heading = "Execution mode\n")
	private ExecutionMode executionMode;

	@CommandLine.ArgGroup(exclusive = false, heading = "Analysis settings\n")
	private AnalysisMode typestateAnalysis;

	@Option(names = { "-s", "--source" }, paramLabel = "<path>", description = "Source file or folder to analyze.")
	private File source;

	@Option(names = { "-m",
			"--mark" }, paramLabel = "<path>", description = "Loads MARK policy files\n\t(Default: ${DEFAULT-VALUE})", split = ",")
	@NonNull
	private static File[] mark = { new File("./") };

	// TODO output standard stdout?
	@Option(names = { "-o",
			"--output" }, paramLabel = "<file>", description = "Write results to file. Use - for stdout.\n\t(Default: ${DEFAULT-VALUE})")
	private static String output = "findings.json";

	@Option(names = {
			"--timeout" }, paramLabel = "<minutes>", description = "Terminate analysis after timeout\n\t(Default: ${DEFAULT-VALUE})")
	private static Long timeout = 120L;

	@Option(names = {
			"--no-good-findings" }, description = "Disable output of \"positive\" findings which indicate correct implementations\n\t(Default: ${DEFAULT-VALUE})")
	private static boolean noGoodFindings;

	public ExecutionMode getExecutionMode() {
		return executionMode;
	}

	public File getSource() {
		return source;
	}

	public void setSource(File source) {
		this.source = source;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public File[] getMark() {
		return mark;
	}

	public void setMark(File[] markModelFiles) {
		this.mark = markModelFiles;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public boolean isNoGoodFindings() {
		return noGoodFindings;
	}

	public void setNoGoodFindings(boolean noGoodFindings) {
		this.noGoodFindings = noGoodFindings;
	}

	public AnalysisMode getTypestateAnalysis() {
		return typestateAnalysis;
	}

	public void setTypestateAnalysis(AnalysisMode typestateAnalysis) {
		this.typestateAnalysis = typestateAnalysis;
	}

	@Override
	public Integer call() throws Exception {
		return 0;
	}
}