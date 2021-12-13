
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.passes.Pass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

public class CpgConfiguration implements Callable<Integer> {

	private boolean debugParser;
	// TODO: put into translation settings?
	private List<String> includeWhitelist = new ArrayList<>();
	private List<String> includeBlacklist = new ArrayList<>();

	private boolean disableCleanup;
	private boolean codeInNodes;
	private boolean processAnnotations;
	private boolean failOnError;
	private Map<String, String> symbols = new HashMap<>();
	// TODO: names!
	private boolean useUnityBuild;
	private boolean useParallelFrontends;
	private boolean typeSystemActiveInFrontend;
	private boolean defaultPasses = true;

	// TODO: how turn string into pass?
	private List<? extends Pass> passes = new ArrayList<>();

	@JsonDeserialize(using = LanguageDeseralizer.class)
	private EnumSet<Language> additionalLanguages = EnumSet.noneOf(Language.class);

	// TODO: maybe change to enum set instead of booleans for each language
	@JsonIgnore
	@Option(names = {
			"--enable-python-support" }, description = "Enables the experimental Python support. Additional files need to be placed in certain locations. Please follow the CPG README.")
	private boolean enablePython;

	@JsonIgnore
	@Option(names = {
			"--enable-go-support" }, description = "Enables the experimental Go support. Additional files need to be placed in certain locations. Please follow the CPG README.")
	private boolean enableGo;

	@CommandLine.ArgGroup(exclusive = false, heading = "Translation settings\n")
	private TranslationSettings translationSettings = new TranslationSettings();

	private static final Logger log = LoggerFactory.getLogger(CpgConfiguration.class);

	public TranslationConfiguration buildTranslationSettings(String url) {
		List<File> files = new ArrayList<>();
		File f = new File(url);
		if (f.isDirectory()) {
			File[] list = f.listFiles();
			if (list != null) {
				files.addAll(Arrays.asList(list));
			} else {
				log.error("Null file list");
			}
		} else {
			files.add(f);
		}

		TranslationConfiguration.Builder translationConfig = new TranslationConfiguration.Builder()
				.debugParser(debugParser)
				.loadIncludes(translationSettings.analyzeIncludes)
				.codeInNodes(codeInNodes)
				.processAnnotations(processAnnotations)
				.failOnError(failOnError)
				.symbols(symbols)
				.useUnityBuild(useUnityBuild)
				.useParallelFrontends(useParallelFrontends)
				.typeSystemActiveInFrontend(typeSystemActiveInFrontend)
				.sourceLocations(files.toArray(new File[0]));

		for (String w : includeWhitelist)
			translationConfig.includeWhitelist(w);
		for (String b : includeBlacklist)
			translationConfig.includeBlacklist(b);

		if (disableCleanup)
			translationConfig.disableCleanup();

		if (defaultPasses)
			translationConfig.defaultPasses();
		for (Pass p : passes)
			translationConfig.registerPass(p);

		for (Language l : additionalLanguages)
			translationConfig.registerLanguage(l.languageFrontend, l.fileTypes);

		if (translationSettings.includesPath != null)
			for (File file : translationSettings.includesPath)
				translationConfig.includePath(file.getAbsolutePath());

		return translationConfig.build();
	}

	public void setTranslationSettings(TranslationSettings translationSettings) {
		this.translationSettings = translationSettings;
	}

	public void setAdditionalLanguages(EnumSet<Language> additionalLanguages) {
		this.additionalLanguages = additionalLanguages;
	}

	public void setDebugParser(boolean debugParser) {
		this.debugParser = debugParser;
	}

	public void setIncludeWhitelist(List<String> includeWhitelist) {
		this.includeWhitelist = includeWhitelist;
	}

	public void setIncludeBlacklist(List<String> includeBlacklist) {
		this.includeBlacklist = includeBlacklist;
	}

	public void setDefaultPasses(boolean defaultPasses) {
		this.defaultPasses = defaultPasses;
	}

	public void setPasses(List<Pass> passes) {
		this.passes = passes;
	}

	public void setSymbols(Map<String, String> symbols) {
		this.symbols = symbols;
	}

	public void setDisableCleanup(boolean disableCleanup) {
		this.disableCleanup = disableCleanup;
	}

	public void setCodeInNodes(boolean codeInNodes) {
		this.codeInNodes = codeInNodes;
	}

	public void setProcessAnnotations(boolean processAnnotations) {
		this.processAnnotations = processAnnotations;
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	public void setUseUnityBuild(boolean useUnityBuild) {
		this.useUnityBuild = useUnityBuild;
	}

	public void setUseParallelFrontends(boolean useParallelFrontends) {
		this.useParallelFrontends = useParallelFrontends;
	}

	public void setTypeSystemActiveInFrontend(boolean typeSystemActiveInFrontend) {
		this.typeSystemActiveInFrontend = typeSystemActiveInFrontend;
	}

	@Override
	public Integer call() throws Exception {
		return 0;
	}

}

class TranslationSettings {
	@Option(names = {
			"--analyze-includes" }, description = "Enables parsing of include files. By default, if --includes are given, the parser will resolve symbols/templates from these include, but not load their parse tree.")
	protected boolean analyzeIncludes = false;

	@Option(names = { "--includes" }, description = "Path(s) containing include files. Path must be separated by : (Mac/Linux) or ; (Windows)", split = ":|;")
	protected File[] includesPath;

	public void setAnalyzeIncludes(boolean analyzeIncludes) {
		this.analyzeIncludes = analyzeIncludes;
	}

	public void setIncludes(File[] includesPath) {
		this.includesPath = includesPath;
	}
}