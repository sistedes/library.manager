/*******************************************************************************
* Copyright (c) 2023 Sistedes
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
* Abel Gómez - initial API and implementation
*******************************************************************************/

package es.sistedes.library.manager;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.sistedes.library.manager.CliLauncher.Commands;
import es.sistedes.library.manager.proceedings.model.ConferenceData;
import es.sistedes.library.manager.proceedings.model.PdfImporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "split", 
		description = "(EXPERIMENTAL) Splits a single PDF file containing the full proceedings of a conference and sets up the JSON files "
				+ "required to generate the proceedings in the new Digital Library.")
// @formatter:on
class SplitCommand implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(SplitCommand.class);

	@ParentCommand
	private Commands mainCmd;

	@Spec
	CommandSpec spec;

	@Option(names = { "-a", "--acronym" }, paramLabel = "ACRONYM", required = true,
			description = "Acronym of the conference to be prepared.")
	private String acronym;
	
	private int year;
	@Option(names = { "-y", "--year" }, paramLabel = "YEAR", required = true,
			description = "Year of the edition to be prepared.")
	private void setYear(int year) {
		if (year < 2000 || year > 2099) {
			throw new ParameterException(spec.commandLine(), 
					MessageFormat.format("Invalid value ''{0}'' for option '--year': valid years range from 2000 to 2099.", year));
        }
        this.year = year;
	}

	@Option(names = { "-P", "--prefix" }, paramLabel = "PREFIX", required = true, description = "Handle prefix.")
	private String prefix;
	
	@Option(names = { "-i", "--input" }, paramLabel = "FILE", required = true, 
			description = "Input PDF file with the full proceedings.")
	private File inputFile;

	@Option(names = { "-o", "--output" }, paramLabel = "DIR", defaultValue = "",
			description = "Ouput directory where the generated conference files should be placed.")
	private File outputDir;
	
	@Option(names = { "-F", "--force" }, 
			description = "Force execution, even if submission files are overwritten.")
	private boolean force = false;
	
	private List<Range<Integer>> frontmatter = new ArrayList<>();
	@Option(names = { "-f", "--frontmatter-pages" }, paramLabel = "PAGES", required = false,
			description = "Comma-separated list of pages where each frontmatter section starts and optionally, ends, if a range (inclusive) is specified. "
					+ "The last element of the list must always be a range. E.g.: 1,3,4-5")
	private void setFrontMatterPages(String pages) {
		if (!pages.matches("(?=.*\\d+-\\d+$)\\d+(-\\d+)?(,\\d+(-\\d+)?)*")) {
			throw new ParameterException(spec.commandLine(), 
					MessageFormat.format("Invalid value ''{0}'' for option ''--frontmatter-pages'': expecting a comma-separated list of pages. "
							+ "Remember that the last value must be a range.", pages));
        } else {
        	Matcher matcher = Pattern.compile("(?<start>\\d+)((-(?<end>\\d+))?(,(?<nextStart>\\d+))?)").matcher(pages);
        	while (matcher.find()) {
        		Integer start = Integer.valueOf(matcher.group("start"));
        		Integer end = matcher.group("end") != null ? Integer.valueOf(matcher.group("end")) : Integer.valueOf(matcher.group("nextStart")) - 1;
        		frontmatter.add(Range.between(start, end));
        		matcher.region(Math.max(matcher.start("nextStart"), matcher.end("end")), matcher.regionEnd());
        	}
        }
	}
	
	private List<Range<Integer>> backmatter = new ArrayList<>();
	/*
	 * UNUSED UNTIL WE PROCESS A PDF FILE WITH BACKMATTER CONTENT
	 * 
	@Option(names = { "-b", "--backmatter-pages" }, paramLabel = "PAGES", required = false,
			description = "Comma-separated list of pages where each backmatter section starts and, optionally, ends, if a range (inclusive) is specified. "
					+ "The last element of the list must always be a range. E.g.: 100,103,104-105.")
	private void setBackMatterPages(String pages) {
		if (!pages.matches("(?=.*\\d+-\\d+$)\\d+(-\\d+)?(,\\d+(-\\d+)?)*")) {
			throw new ParameterException(spec.commandLine(), 
					MessageFormat.format("Invalid value ''{0}'' for option ''--backmatter-pages'': expecting a comma-separated list of pages. "
							+ "Remember that the last value must be a range.", pages));
        } else {
        	Matcher matcher = Pattern.compile("(?<start>\\d+)((-(?<end>\\d+))?(,(?<nextStart>\\d+))?)").matcher(pages);
        	while (matcher.find()) {
        		Integer start = Integer.valueOf(matcher.group("start"));
        		Integer end = matcher.group("end") != null ? Integer.valueOf(matcher.group("end")) : Integer.valueOf(matcher.group("nextStart")) - 1;
        		backmatter.add(Range.between(start, end));
        		matcher.region(Math.max(matcher.start("nextStart"), matcher.end("end")), matcher.regionEnd());
        	}
        }
	}
	*/
	
	private List<List<Range<Integer>>> contributions = new ArrayList<>();
	@Option(names = { "-c", "--contributions-pages" }, paramLabel = "PAGES", required = false,
			description = "List of the pages where each contribution starts and, optionally, ends, if a range (inclusive) is specified. "
					+ "Pages (or ranges) separated by comma denote contributions in the same session/track. "
					+ "Pages (or ranges) separated by semicolons denote papers in different sessions/tracks. "
					+ "The last element of the list must always be a range. E.g: 10,15,20;25-26,27-30;31,35-40")
	private void setContributionsPages(String pages) {
		if (!pages.matches("(?=.*\\d+-\\d+$)\\d+(-\\d+)?([,;]\\d+(-\\d+)?)*")) {
			throw new ParameterException(spec.commandLine(), 
					MessageFormat.format("Invalid value ''{0}'' for option ''--contributions-pages'': expecting a comma-separated list of page. "
							+ "Remember that the last value must be a range.", pages));
		} else {
        	Matcher matcher = Pattern.compile("(?<start>\\d+)((-(?<end>\\d+))?((?<separator>[;,])(?<nextStart>\\d+))?)").matcher(pages);
        	List<Range<Integer>> contributionsInSection = new ArrayList<>();
        	contributions.add(contributionsInSection);
        	while (matcher.find()) {
        		Integer start = Integer.valueOf(matcher.group("start"));
        		Integer end = matcher.group("end") != null ? Integer.valueOf(matcher.group("end")) : Integer.valueOf(matcher.group("nextStart")) - 1;
        		contributionsInSection.add(Range.between(start, end));
        		if (";".equals(matcher.group("separator"))) {
        			contributionsInSection = new ArrayList<>();
        			contributions.add(contributionsInSection);
        		}
        		matcher.region(Math.max(matcher.start("nextStart"), matcher.end("end")), matcher.regionEnd());
        	}
		}
	}
	
	@Option(names = { "-u", "--grobid-url" }, paramLabel = "URL", defaultValue = "",
			description = "Grobid service URL.")
	private URL url;

	@Override
	public Integer call() throws Exception {
		logger.info(MessageFormat.format("Splitting file ''{0}''", inputFile));
		ConferenceData conferenceData = new PdfImporter(inputFile, outputDir, prefix, acronym, year, frontmatter, contributions, backmatter, url, force).getData();
		
		logger.info(MessageFormat.format("Saving conference data to ''{0}''", outputDir));
		conferenceData.save(force);

		// Return success
		return 0;
	}


}
