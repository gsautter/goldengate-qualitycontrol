/*
 * Copyright (c) 2006-, IPD Boehm, Universitaet Karlsruhe (TH) / KIT, by Guido Sautter
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Universitaet Karlsruhe (TH) / KIT nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITAET KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uka.ipd.idaho.goldenGate.qc.plugins.imagine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.util.CountingSet;
import de.uka.ipd.idaho.gamta.util.DocumentErrorChecker;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol.DocumentError;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.imaging.BoundingBox;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.ImPage;
import de.uka.ipd.idaho.im.ImRegion;
import de.uka.ipd.idaho.im.ImWord;
import de.uka.ipd.idaho.im.imagine.plugins.AbstractImageMarkupToolProvider;
import de.uka.ipd.idaho.im.util.ImDocumentErrorChecker;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;
import de.uka.ipd.idaho.im.util.ImUtils;

/**
 * Plug-in checking paragraph breaks for potential errors.
 * 
 * @author sautter
 */
public class TextFlowBreakErrorChecker extends AbstractImageMarkupToolProvider {
	private LinkedHashMap paragraphWordContextsToSeverities = new LinkedHashMap();
	private LinkedHashMap paragraphStartContextsToSeverities = new LinkedHashMap();
	private LinkedHashMap paragraphContinueContextsToSeverities = new LinkedHashMap();
	private LinkedHashMap paragraphEndContextsToSeverities = new LinkedHashMap();
	
	private ArrayList paragraphCheckFilterLinePatterns = new ArrayList();
	private ArrayList paragraphStartFilterLinePatterns = new ArrayList();
	private ArrayList paragraphEndFilterLinePatterns = new ArrayList();
	
	private ParagraphBreakErrorChecker errorChecker = null;
	private ParagraphBreakErrorCheckTool errorCheckTool = null;
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#getPluginName()
	 */
	public String getPluginName() {
		return "IM Text Flow Break Checker";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#init()
	 */
	public void init() {
		
		//	load filter line patterns
		this.loadFilterLinePatternList("paragraphCheckFilters.cnfg", this.paragraphCheckFilterLinePatterns);
		this.loadFilterLinePatternList("paragraphStartFilters.cnfg", this.paragraphStartFilterLinePatterns);
		this.loadFilterLinePatternList("paragraphEndFilters.cnfg", this.paragraphEndFilterLinePatterns);
		
		//	load mapping from context annotation types to error severities
		this.loadContextToSeverityMapping("paragraphWordSeverities.cnfg", this.paragraphWordContextsToSeverities);
		this.loadContextToSeverityMapping("paragraphStartSeverities.cnfg", this.paragraphStartContextsToSeverities);
		this.loadContextToSeverityMapping("paragraphContinueSeverities.cnfg", this.paragraphContinueContextsToSeverities);
		this.loadContextToSeverityMapping("paragraphEndSeverities.cnfg", this.paragraphEndContextsToSeverities);
		
		//	create and register error checker
		ParagraphBreakErrorChecker errorChecker = new ParagraphBreakErrorChecker();
		DocumentErrorChecker.registerErrorChecker(errorChecker);
		if (LOCAL_MASTER_CONFIG_NAME.equals(this.parent.getConfigurationName()))
			this.errorChecker = errorChecker;
	}
	private boolean loadContextToSeverityMapping(String dataName, Map mapping) {
		if (this.dataProvider.isDataAvailable(dataName)) try {
			BufferedReader mbr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream(dataName), "UTF-8"));
			for (String ml; (ml = mbr.readLine()) != null;) {
				ml = ml.trim();
				if (ml.length() == 0)
					continue;
				if (ml.startsWith("//"))
					continue;
				String[] md = ml.split("\\s+");
				String mt = md[0];
				String ms;
				if (md.length == 1)
					ms = null;
				else if (DocumentError.SEVERITY_BLOCKER.equalsIgnoreCase(md[1]))
					ms = DocumentError.SEVERITY_BLOCKER;
				else if (DocumentError.SEVERITY_CRITICAL.equalsIgnoreCase(md[1]))
					ms = DocumentError.SEVERITY_CRITICAL;
				else if (DocumentError.SEVERITY_MAJOR.equalsIgnoreCase(md[1]))
					ms = DocumentError.SEVERITY_MAJOR;
				else if (DocumentError.SEVERITY_MINOR.equalsIgnoreCase(md[1]))
					ms = DocumentError.SEVERITY_MINOR;
				else {
					System.out.println("Invalid mapped error severity '" + md[1] + "' in line " + ml);
					continue;
				}
				mapping.put(mt, ms);
				System.out.println("Mapped error context type " + mt + " to severity " + ms);
			}
			mbr.close();
			return true;
		}
		catch (IOException ioe) {
			System.out.println("Error reading mapping from '" + dataName + "': " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		return false;
	}
	private void loadFilterLinePatternList(String dataName, ArrayList patterns) {
		if (this.dataProvider.isDataAvailable(dataName)) try {
			BufferedReader pbr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream(dataName), "UTF-8"));
			for (String pl; (pl = pbr.readLine()) != null;) {
				pl = pl.trim();
				if (pl.length() == 0)
					continue;
				if (pl.startsWith("//"))
					continue;
				try {
					Pattern fp = Pattern.compile(pl);
					patterns.add(fp);
					System.out.println("Added filter pattern " + pl);
				}
				catch (Exception e) {
					System.out.println("Error compiling pattern '" + pl + "': " + e.getMessage());
					e.printStackTrace(System.out);
				}
			}
			pbr.close();
		}
		catch (IOException ioe) {
			System.out.println("Error reading patterns from '" + dataName + "': " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.AbstractImageMarkupToolProvider#getEditMenuItemNames()
	 */
	public String[] getEditMenuItemNames() {
		if (this.errorChecker == null)
			return null;
		String[] emints = {this.errorChecker.name};
		return emints;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageMarkupToolProvider#getImageMarkupTool(java.lang.String)
	 */
	public ImageMarkupTool getImageMarkupTool(String name) {
		if ((this.errorChecker != null) && this.errorChecker.name.equals(name)) {
			if (this.errorCheckTool == null)
				this.errorCheckTool = new ParagraphBreakErrorCheckTool();
			return this.errorCheckTool;
		}
		return null;
	}
	
	private class ParagraphBreakErrorCheckTool implements ImageMarkupTool {
		ParagraphBreakErrorCheckTool() {
			super();
		}
		public String getLabel() {
			return "Check Paragraph Breaks (TEST)";
		}
		public String getTooltip() {
			return "Run paragraph break checking and log the process";
		}
		public String getHelpText() {
			return null; // no need for this ...
		}
		public void process(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
			ImDocumentErrorProtocol idep = new ImDocumentErrorProtocol(doc) {
				public void addError(String source, Attributed subject, Attributed parent, String category, String type, String description, String severity, boolean falsePositive) {
					ImWord word = ((ImWord) subject);
					this.subject.addAnnotation(word, word, (category + "-" + type));
				}
			};
			if (annot == null)
				errorChecker.addDocumentErrors(doc, idep, null, null);
			else errorChecker.addAnnotationErrors(annot, idep, null, null);
		}
	}
	
	private class ParagraphBreakErrorChecker extends ImDocumentErrorChecker {
		ParagraphBreakErrorChecker() {
			super("TextFlowErrorChecker");
		}
		public String getLabel() {
			return "Text Flow";
		}
		public String getDescription() {
			return "Errors in document text flow";
		}
		public String[] getErrorCategories() {
			String[] ecs = {"textStreams"};
			return ecs;
		}
		public String getErrorCategoryLabel(String category) {
			if ("textStreams".equals(category))
				return "Text Streams";
			else return null;
		}
		public String getErrorCategoryDescription(String category) {
			if ("textStreams".equals(category))
				return "Errors in logical text streams";
			else return null;
		}
		private boolean categoryMatch(String category) {
			return ((category == null) || "textStreams".equals(category));
		}
		public String[] getCheckedSubjects(String category, String type) {
			String[] css = {ImWord.WORD_ANNOTATION_TYPE};
			return css;
		}
		public boolean requiresTopLevelDocument(String category, String type) {
			return false;
		}
		public String getCheckLevel(String category, String type) {
			return CHECK_LEVEL_SEMANTICS;
		}
		public boolean isDefaultErrorChecker() {
			return true;
		}
		public String[] getErrorTypes(String category) {
			if (this.categoryMatch(category)) {
				String[] ecs = {"paragraphStart", "paragraphMingle", "paragraphEnd", "hyphenation"};
				return ecs;
			}
			else return new String[0];
		}
		public String getErrorTypeLabel(String category, String type) {
			if ("textStreams".equals(category)) {
				if ("paragraphStart".equals(type))
					return "Paragraph Start";
				if ("paragraphMingle".equals(type))
					return "Mingled Paragraphs";
				if ("paragraphEnd".equals(type))
					return "Paragraph End";
				if ("hyphenation".equals(type))
					return "Hyphenation";
			}
			return null;
		}
		public String getErrorTypeDescription(String category, String type) {
			if ("textStreams".equals(category)) {
				if ("paragraphStart".equals(type))
					return "Morphologically invalid paragraph starts";
				if ("paragraphMingle".equals(type))
					return "Paragraphs mingled for lack of a breaks";
				if ("paragraphEnd".equals(type))
					return "Morphologically invalid paragraph ends";
				if ("hyphenation".equals(type))
					return "Missed morphological hyphenations";
			}
			return null;
		}
		private boolean typeMatch(String type) {
			return ((type == null) || "paragraphStart".equals(type) || "paragraphMingle".equals(type) || "paragraphEnd".equals(type) || "hyphenation".equals(type));
		}
		public String[] getObservedTargets(String category, String type) {
			if (this.categoryMatch(category) && this.typeMatch(type)) {
				LinkedHashSet ots = new LinkedHashSet();
				if ((type == null) || "paragraphStart".equals(type))
					ots.addAll(paragraphStartContextsToSeverities.keySet());
				if ((type == null) || "paragraphMingle".equals(type))
					ots.addAll(paragraphContinueContextsToSeverities.keySet());
				if ((type == null) || "paragraphEnd".equals(type))
					ots.addAll(paragraphEndContextsToSeverities.keySet());
				ots.add(ImWord.WORD_ANNOTATION_TYPE);
				ots.add("@" + ImWord.NEXT_WORD_ATTRIBUTE);
				ots.add("@" + ImWord.NEXT_RELATION_ATTRIBUTE);
				ots.add("@" + ImWord.PREVIOUS_WORD_ATTRIBUTE);
				ots.add("@" + ImWord.PREVIOUS_RELATION_ATTRIBUTE);
				return ((String[]) ots.toArray(new String[ots.size()]));
			}
			else return new String[0];
		}
		public DocumentErrorMetadata[] getErrorMetadata(String category, String type) {
			if (!this.categoryMatch(category))
				return null;
			ArrayList dems = new ArrayList();
			if ((type == null) || "paragraphStart".equals(type)) {
				TreeSet severities = this.getErrorSeverities(paragraphStartContextsToSeverities);
				for (Iterator sit = severities.iterator(); sit.hasNext();) {
					String severity = ((String) sit.next());
					dems.add(new DocumentErrorMetadata(this.name, "textStreams", "Text Streams", "paragraphStart", "Paragraph Start", severity, "Word '$value' starts a paragraph, but is in lower case"));
					dems.add(new DocumentErrorMetadata(this.name, "textStreams", "Text Streams", "paragraphStart", "Paragraph Start", severity, "Word '$value' starts a paragraph, but is a terminating punctuation mark"));
				}
			}
			if ((type == null) || "paragraphMingle".equals(type)) {
				TreeSet severities = this.getErrorSeverities(paragraphContinueContextsToSeverities);
				for (Iterator sit = severities.iterator(); sit.hasNext();) {
					String severity = ((String) sit.next());
					dems.add(new DocumentErrorMetadata(this.name, "textStreams", "Text Streams", "paragraphMingle", "Mingled Paragraph", severity, "Word '$value' appears to be a paragraph end, but is not marked as such"));
				}
			}
			if ((type == null) || "paragraphEnd".equals(type)) {
				TreeSet severities = this.getErrorSeverities(paragraphEndContextsToSeverities);
				for (Iterator sit = severities.iterator(); sit.hasNext();) {
					String severity = ((String) sit.next());
					dems.add(new DocumentErrorMetadata(this.name, "textStreams", "Text Streams", "paragraphEnd", "Paragraph End", severity, "Word '$value' ends a paragraph, but is too close to block and column edge"));
					dems.add(new DocumentErrorMetadata(this.name, "textStreams", "Text Streams", "paragraphEnd", "Paragraph End", severity, "Word '$value' ends a paragraph, but is no respective punctuation mark"));
				}
			}
			if ((type == null) || "hyphenation".equals(type)) {
				TreeSet severities = this.getErrorSeverities(paragraphWordContextsToSeverities);
				for (Iterator sit = severities.iterator(); sit.hasNext();) {
					String severity = ((String) sit.next());
					dems.add(new DocumentErrorMetadata(this.name, "textStreams", "Text Streams", "hyphenation", "Hyphenation", severity, "Word '$value' appears to be hyphenated, but is not marked accordingly"));
				}
			}
			return ((DocumentErrorMetadata[]) dems.toArray(new DocumentErrorMetadata[dems.size()]));
		}
		private TreeSet getErrorSeverities(LinkedHashMap specificContextsToSeverities) {
			TreeSet severities = new TreeSet();
			severities.add(DocumentError.SEVERITY_CRITICAL);
			for (Iterator sit = specificContextsToSeverities.values().iterator(); sit.hasNext();) {
				String severity = ((String) sit.next());
				if (severity != null)
					severities.add(severity);
			}
			for (Iterator sit = paragraphWordContextsToSeverities.values().iterator(); sit.hasNext();) {
				String severity = ((String) sit.next());
				if (severity != null)
					severities.add(severity);
			}
			return severities;
		}
		public int addDocumentErrors(ImDocument doc, ImDocumentErrorProtocol dep, String category, String type) {
			boolean checkStart = ((type == null) || "paragraphStart".equals(type));
			boolean checkMingle = ((type == null) || "paragraphMingle".equals(type));
			boolean checkEnd = ((type == null) || "paragraphEnd".equals(type));
			boolean checkHyphenation = ((type == null) || "hyphenation".equals(type));
			ImWord[] tshs = doc.getTextStreamHeads();
			int errors = 0;
			for (int h = 0; h < tshs.length; h++) {
				if (tshs[h].getTextStreamType() == null)
					errors += this.addTextStreamErrors(tshs[h], null, true, doc, dep, checkStart, checkMingle, checkEnd, checkHyphenation);
				else if (ImWord.TEXT_STREAM_TYPE_MAIN_TEXT.equals(tshs[h].getTextStreamType()))
					errors += this.addTextStreamErrors(tshs[h], null, true, doc, dep, checkStart, checkMingle, checkEnd, checkHyphenation);
				else if (ImWord.TEXT_STREAM_TYPE_CAPTION.equals(tshs[h].getTextStreamType()))
					errors += this.addTextStreamErrors(tshs[h], null, false, doc, dep, checkStart, checkMingle, checkEnd, checkHyphenation);
				else if (ImWord.TEXT_STREAM_TYPE_FOOTNOTE.equals(tshs[h].getTextStreamType()))
					errors += this.addTextStreamErrors(tshs[h], null, false, doc, dep, checkStart, checkMingle, checkEnd, checkHyphenation);
			}
			return errors;
		}
		public int addAnnotationErrors(ImAnnotation annot, ImDocumentErrorProtocol dep, String category, String type) {
			boolean checkStart = ((type == null) || "paragraphStart".equals(type));
			boolean checkMingle = ((type == null) || "paragraphMingle".equals(type));
			boolean checkEnd = ((type == null) || "paragraphEnd".equals(type));
			boolean checkHyphenation = ((type == null) || "hyphenation".equals(type));
			return this.addTextStreamErrors(annot.getFirstWord(), annot.getLastWord(), false, annot.getDocument(), dep, checkStart, checkMingle, checkEnd, checkHyphenation);
		}
		public int addRegionErrors(ImRegion region, ImDocumentErrorProtocol dep, String category, String type) {
			return 0; // not checking regions for now
		}
		private int addTextStreamErrors(ImWord fromWord, ImWord toWord, boolean fullDoc, ImDocument doc, ImDocumentErrorProtocol dep, boolean checkStart, boolean checkMingle, boolean checkEnd, boolean checkHyphenation) {
			WordPositionStats wordPosStats = null;
			if (fullDoc && checkMingle) {
				wordPosStats = new WordPositionStats();
				for (ImWord imw = fromWord; imw != null; imw = imw.getNextWord())
					wordPosStats.add(imw);
				this.log("Got word position stats with " + wordPosStats.getWordCount() + " words");
				if (wordPosStats.getWordCount() < 1000) // TODO scrutinize this threshold !!!
					wordPosStats = null;
			}
			int columnPageId = -1;
			ArrayList pageColumns = new ArrayList();
			ImRegion wordBlock = null;
			BoundingBox wordColumn = null;
			ImWord paraStartWord = null;
			int errors = 0;
			for (ImWord imw = fromWord; imw != null; imw = imw.getNextWord()) {
				if (imw.pageId != columnPageId) {
					pageColumns.clear();
					this.getPageColumns(imw, pageColumns);
					columnPageId = imw.pageId;
				}
				if ((imw.getNextRelation() == ImWord.NEXT_RELATION_PARAGRAPH_END) || (imw == toWord)) {
					if (paraStartWord == null)
						paraStartWord = fromWord; // fallback for smaller annotations
					if (checkEnd && (imw.getNextRelation() == ImWord.NEXT_RELATION_PARAGRAPH_END)) {
						wordBlock = this.getBlockAt(imw, wordBlock);
						wordColumn = this.getColumnAt(imw, wordColumn, pageColumns);
						if (this.isParagraphEndError(imw, wordBlock, wordColumn, paraStartWord)) {
							LinkedHashSet contextAnnotTypes = this.getContextAnnotationTypes(imw);
							String severity = this.getErrorSeverity(contextAnnotTypes, paragraphEndContextsToSeverities);
							if (severity != null) {
								String wordStr = Gamta.normalize(imw.getString()).trim();
								if (wordStr.endsWith(".") || wordStr.endsWith("!") || wordStr.endsWith("?") || wordStr.endsWith(":"))
									dep.addError(this.name, imw, doc, "textStreams", "paragraphEnd", ("Word '" + imw.getString() + "' ends a paragraph, but is too close to block and column edge"), severity, false);
								else dep.addError(this.name, imw, doc, "textStreams", "paragraphEnd", ("Word '" + imw.getString() + "' ends a paragraph, but is no respective punctuation mark"), severity, false);
								errors++;
							}
						}
					}
					if (checkStart && (paraStartWord.getPreviousRelation() == ImWord.NEXT_RELATION_PARAGRAPH_END)) {
						if (this.isParagraphStartError(paraStartWord, imw)) {
							LinkedHashSet contextAnnotTypes = this.getContextAnnotationTypes(paraStartWord);
							String severity = this.getErrorSeverity(contextAnnotTypes, paragraphStartContextsToSeverities);
							if (severity != null) {
								String wordStr = Gamta.normalize(paraStartWord.getString()).trim();
								if (Gamta.isWord(wordStr))
									dep.addError(this.name, paraStartWord, doc, "textStreams", "paragraphStart", ("Word '" + paraStartWord.getString() + "' starts a paragraph, but is in lower case"), severity, false);
								else dep.addError(this.name, paraStartWord, doc, "textStreams", "paragraphStart", ("Word '" + paraStartWord.getString() + "' starts a paragraph, but is a terminating punctuation mark"), severity, false);
								errors++;
							}
						}
					}
					paraStartWord = imw.getNextWord();
				}
				else if (checkMingle && (imw.getNextRelation() == ImWord.NEXT_RELATION_SEPARATE) && (imw.getNextWord() != null) && ImUtils.areTextFlowBreak(imw, imw.getNextWord())) {
					wordBlock = this.getBlockAt(imw, wordBlock);
					if (this.isParagraphContinueError(imw, imw.getNextWord(), wordBlock, wordColumn)) {
						LinkedHashSet contextAnnotTypes = this.getContextAnnotationTypes(imw);
						String severity = this.getErrorSeverity(contextAnnotTypes, paragraphContinueContextsToSeverities);
						if (severity != null) {
							dep.addError(this.name, imw, doc, "textStreams", "paragraphMingle", ("Word '" + imw.getString() + "' appears to be a paragraph end, but is not marked as such"), severity, false);
							errors++;
						}
					}
				}
				else if ((wordPosStats != null) && (imw.getPreviousRelation() == ImWord.NEXT_RELATION_SEPARATE) && (imw.getPreviousWord() != null) && ImUtils.areTextFlowBreak(imw.getPreviousWord(), imw)) {
					this.log("Checking '" + imw.getString() + "' at " + imw.getLocalID() + " as paragraph continuation");
					this.log(" - got " + wordPosStats.getCount(imw) + " occurrences, " + wordPosStats.getLineStartCount(imw) + " at line start, " + wordPosStats.getParaStartCount(imw) + " at paragraph start");
					if (wordPosStats.getCount(imw) == -1) 
						this.log(" ==> not a suitable word");
					else if (wordPosStats.getCount(imw) < 10) // too few instances TODO scrutinize this threshold
						this.log(" ==> too few occurrences, only " + wordPosStats.getCount(imw));
					else if (wordPosStats.getLineStartPercent(imw) < 90) // too few line starts TODO scrutinize this threshold
						this.log(" ==> too rarely at line start, only " + wordPosStats.getLineStartPercent(imw) + "%");
					else if (wordPosStats.getParaStartPercent(imw) < 50) // too few paragraph starts TODO scrutinize this threshold
						this.log(" ==> too rarely at paragraph start, only " + wordPosStats.getParaStartPercent(imw) + "%");
					else if (Gamta.normalize(ImUtils.getStringFrom(imw)).matches("[a-z\\'\\-]{0,}[A-Z][a-zA-Z\\'\\-]+")) {
						LinkedHashSet contextAnnotTypes = this.getContextAnnotationTypes(imw);
						String severity = this.getErrorSeverity(contextAnnotTypes, paragraphContinueContextsToSeverities);
						if (severity != null) {
							dep.addError(this.name, imw, doc, "textStreams", "paragraphMingle", ("Word '" + imw.getString() + "' appears to be a paragraph start, but is not marked as such"), severity, false);
							errors++;
						}
					}
				}
				if (checkHyphenation && ((imw.getNextRelation() == ImWord.NEXT_RELATION_SEPARATE) || (imw.getNextRelation() == ImWord.NEXT_RELATION_PARAGRAPH_END)) && (imw.getString() != null) && (imw.getNextWord() != null) && ImUtils.areTextFlowBreak(imw, imw.getNextWord())) {
//					this.log("Checking '" + imw.getString() + "' at " + imw.getLocalID() + " as for hyphenation");
//					String imwStr = Gamta.normalize(imw.getString());
//					if (imwStr.endsWith("-")) {
//						this.log(" ==> ends with dash, potentially hyphenated");
//						LinkedHashSet contextAnnotTypes = this.getContextAnnotationTypes(imw);
//						String severity = this.getErrorSeverity(contextAnnotTypes, paragraphWordContextsToSeverities);
//						if (severity != null) {
//							dep.addError(this.name, imw, doc, "textStreams", "hyphenation", ("Word '" + imw.getString() + "' appears to be hyphenated, but is not marked accordingly"), severity, false);
//							errors++;
//						}
//					}
					String imwStr = Gamta.normalize(imw.getString());
					String toImwStr = Gamta.normalize(ImUtils.getStringUpTo(imw));
					this.log("Checking '" + imw.getString() + "'" + ((imwStr.length() < toImwStr.length()) ? (" ['" + toImwStr + "']") : "") + " at " + imw.getLocalID() + " as for hyphenation");
					if (imwStr.endsWith("-") && Gamta.isWord(toImwStr)) {
						this.log(" ==> ends with dash, potentially hyphenated");
						LinkedHashSet contextAnnotTypes = this.getContextAnnotationTypes(imw);
						String severity = this.getErrorSeverity(contextAnnotTypes, paragraphWordContextsToSeverities);
						if (severity != null) {
							dep.addError(this.name, imw, doc, "textStreams", "hyphenation", ("Word '" + imw.getString() + "' appears to be hyphenated, but is not marked accordingly"), severity, false);
							errors++;
						}
					}
					else this.log(" ==> not ending with dash");
				}
				if (imw == toWord)
					break;
			}
			return errors;
		}
		private void getPageColumns(ImWord startWord, ArrayList pageColumns) {
			this.log("Getting columns for text stream " + startWord.getTextStreamId() + " in page " + startWord.pageId);
			/*
Improve handling of right distance measurements in single-line blocks:
- synthesize columns from blocks above and below that:
- belong to same text stream
- share same left edge (give or take a few pixels, maybe, say DPI/50)
OR
- share same X-axis center point (give or take a few pixels, maybe, say DPI/25)
- use that for computing right block edge distance
==> should prevent another good bunch of false positives
==> should be pretty safe if only used for single-line paragraphs
- compute as bounding box in text stream walking method ...
- ... and pass as argument to paragraph end check method
- use same inclusion test and re-computation style as for blocks proper
			 */
			LinkedHashSet pageBlockBounds = new LinkedHashSet();
			ImRegion wordBlock = null;
			//	TODO_NOT extend bounding box to count number of words and number of mergers
			for (ImWord imw = startWord; imw != null; imw = imw.getNextWord()) {
				if (imw.pageId != startWord.pageId)
					break;
				wordBlock = this.getBlockAt(imw, wordBlock);
				if (wordBlock != null)
					pageBlockBounds.add(wordBlock.bounds);
			}
			pageColumns.addAll(pageBlockBounds);
			if (pageColumns.size() < 2)
				return; // nothing to merge here
			
			//	merge bounding boxes that overlap with at least 2/3 of their horizontal extent ...
			//	... unless merged bounding box would intersect with wider bounding box protruding on either side
			this.log(" - starting column assmebly with " + pageColumns.size() + " bounding boxes");
			int hSlack = (startWord.getPage().getImageDPI() / 12);
			for (int c = 0; c < pageColumns.size(); c++) {
				BoundingBox pageColumn = ((BoundingBox) pageColumns.get(c));
				this.log(" - seeking merge partner for " + pageColumn);
				for (int cc = (c+1); cc < pageColumns.size(); cc++) {
					BoundingBox cPageColumn = ((BoundingBox) pageColumns.get(cc));
					this.log("   - checking " + cPageColumn);
					if (pageColumn.right <= cPageColumn.left) {
						this.log("     ==> no overlap");
						continue;
					}
					if (cPageColumn.right <= pageColumn.left) {
						this.log("     ==> no overlap");
						continue;
					}
					
					int overlap = (Math.min(pageColumn.right, cPageColumn.right) - Math.max(pageColumn.left, cPageColumn.left));
					this.log("   - overlap is " + overlap);
					if ((overlap * 3) < (pageColumn.getWidth() * 2)) {
						this.log("     ==> less than 2/3 of " + pageColumn.getWidth());
						continue;
					}
					if ((overlap * 3) < (cPageColumn.getWidth() * 2)) {
						this.log("     ==> less than 2/3 of " + cPageColumn.getWidth());
						continue;
					}
					
					BoundingBox mPageColumn = new BoundingBox(
							Math.min(pageColumn.left, cPageColumn.left),
							Math.max(pageColumn.right, cPageColumn.right),
							Math.min(pageColumn.top, cPageColumn.top),
							Math.max(pageColumn.bottom, cPageColumn.bottom)
						);
					this.log("   - merged bounds are " + mPageColumn);
					for (int mcc = 0; mcc < pageColumns.size(); mcc++) {
						if (mcc == c)
							continue; // no need to check basis of merger
						if (mcc == cc)
							continue; // no need to check basis of merger
						BoundingBox mcPageColumn = ((BoundingBox) pageColumns.get(mcc));
						if (!mPageColumn.overlaps(mcPageColumn))
							continue; // no overlap at all
						if (mcPageColumn.liesIn(mPageColumn, false))
							continue; // completely contained
						if ((mPageColumn.top <= mcPageColumn.top) && (mcPageColumn.bottom <= mPageColumn.bottom) && ((mPageColumn.left - mcPageColumn.left) < hSlack) && ((mcPageColumn.right - mPageColumn.right) < hSlack))
							continue; // cut some DPI/12 or so (2mm) of slack to left and right to accommodate sloppy layout
						this.log("     ==> refused for intersecting with " + mcPageColumn);
						mPageColumn = null;
						break;
					}
					if (mPageColumn == null)
						continue;
					
					pageColumns.set(c, mPageColumn);
					pageColumns.remove(cc);
					for (int rc = 0; rc < pageColumns.size(); rc++) {
						BoundingBox rPageColumn = ((BoundingBox) pageColumns.get(rc));
						if (rPageColumn == mPageColumn)
							continue;
						if (rPageColumn.liesIn(mPageColumn, false)) {
							pageColumns.remove(rc--);
							this.log("   - removed contained bounding box " + rPageColumn);
						}
					}
					c = -1; // start over at 0 after loop increment
					this.log("     ==> " + pageColumns.size() + " bounding boxes remaining");
					break;
				}
			}
			this.log(" ==> ended up with " + pageColumns.size() + " bounding boxes: " + pageColumns);
			if (pageColumns.size() < 2)
				return; // nothing left to merge
			
			//	merge if horizontal center of A (smaller) contained in horizontal extent of B (larger) ...
			//	... unless we have a C that is beside A and also overlaps with horizontal extent of B
			for (int c = 0; c < pageColumns.size(); c++) {
				BoundingBox pageColumn = ((BoundingBox) pageColumns.get(c));
				this.log(" - seeking merge partner for " + pageColumn);
				for (int cc = (c+1); cc < pageColumns.size(); cc++) {
					BoundingBox cPageColumn = ((BoundingBox) pageColumns.get(cc));
					this.log("   - checking " + cPageColumn);
					if (pageColumn.right <= cPageColumn.left) {
						this.log("     ==> no overlap");
						continue;
					}
					if (cPageColumn.right <= pageColumn.left) {
						this.log("     ==> no overlap");
						continue;
					}
					
					if ((pageColumn.left < (cPageColumn.left + hSlack)) && ((cPageColumn.right - hSlack) < pageColumn.right)) {
						for (int occ = 0; occ < pageColumns.size(); occ++) {
							if (occ == cc)
								continue; // no use self-scrutinizing
							if (occ == c)
								continue; // no use self-scrutinizing
							BoundingBox ocPageColumn = ((BoundingBox) pageColumns.get(occ));
							if (ocPageColumn.bottom <= cPageColumn.top)
								continue;
							if (cPageColumn.bottom <= ocPageColumn.top)
								continue;
							if (ocPageColumn.right <= pageColumn.left)
								continue;
							if (pageColumn.right <= ocPageColumn.left)
								continue;
							this.log("     ==> refused because " + ocPageColumn + " side by side with " + cPageColumn);
							cPageColumn = null;
							break;
						}
						if (cPageColumn == null)
							continue;
					}
					else if ((cPageColumn.left < (pageColumn.left + hSlack)) && ((pageColumn.right - hSlack) < cPageColumn.right)) {
						for (int occ = 0; occ < pageColumns.size(); occ++) {
							if (occ == cc)
								continue; // no use self-scrutinizing
							if (occ == c)
								continue; // no use self-scrutinizing
							BoundingBox ocPageColumn = ((BoundingBox) pageColumns.get(occ));
							if (ocPageColumn.bottom <= pageColumn.top)
								continue;
							if (pageColumn.bottom <= ocPageColumn.top)
								continue;
							if (ocPageColumn.right <= cPageColumn.left)
								continue;
							if (cPageColumn.right <= ocPageColumn.left)
								continue;
							this.log("     ==> refused because " + ocPageColumn + " side by side with " + pageColumn);
							cPageColumn = null;
							break;
						}
						if (cPageColumn == null)
							continue;
					}
					else continue;
					
					BoundingBox mPageColumn = new BoundingBox(
							Math.min(pageColumn.left, cPageColumn.left),
							Math.max(pageColumn.right, cPageColumn.right),
							Math.min(pageColumn.top, cPageColumn.top),
							Math.max(pageColumn.bottom, cPageColumn.bottom)
						);
					this.log("   - merged bounds are " + mPageColumn);
					for (int mcc = 0; mcc < pageColumns.size(); mcc++) {
						if (mcc == c)
							continue; // no need to check basis of merger
						if (mcc == cc)
							continue; // no need to check basis of merger
						BoundingBox mcPageColumn = ((BoundingBox) pageColumns.get(mcc));
						if (!mPageColumn.overlaps(mcPageColumn))
							continue; // no overlap at all
						if (mcPageColumn.liesIn(mPageColumn, false))
							continue; // completely contained
						if ((mPageColumn.top <= mcPageColumn.top) && (mcPageColumn.bottom <= mPageColumn.bottom) && ((mPageColumn.left - mcPageColumn.left) < hSlack) && ((mcPageColumn.right - mPageColumn.right) < hSlack))
							continue; // cut some DPI/12 or so (2mm) of slack to left and right to accommodate sloppy layout
						this.log("     ==> refused for intersecting with " + mcPageColumn);
						mPageColumn = null;
						break;
					}
					if (mPageColumn == null)
						continue;
					
					pageColumns.set(c, mPageColumn);
					pageColumns.remove(cc);
					for (int rc = 0; rc < pageColumns.size(); rc++) {
						BoundingBox rPageColumn = ((BoundingBox) pageColumns.get(rc));
						if (rPageColumn == mPageColumn)
							continue;
						if (rPageColumn.liesIn(mPageColumn, false)) {
							pageColumns.remove(rc--);
							this.log("   - removed contained bounding box " + rPageColumn);
						}
					}
					c = -1; // start over at 0 after loop increment
					this.log("     ==> " + pageColumns.size() + " bounding boxes remaining");
					break;
				}
			}
			this.log(" ==> ended up with " + pageColumns.size() + " bounding boxes: " + pageColumns);
		}
		private void log(String output) {
			/*
Log decision process (at least in master configuration):
==> TODO maybe try that in other places as well ...
==> ... using "verbose" parameter or running under master configuration
  ==> GPath error check analysis is prime example
  ==> document structure analysis (especially without template) very same
  ==> caption citation handling
  ==> key handling
  ==> region actions (maybe with added debug argument to library methods)
  ==> XHTML previewers/validators
			 */
			if (errorChecker == this)
				System.out.println(output);
		}
		private BoundingBox getColumnAt(ImWord word, BoundingBox column, ArrayList pageColumns) {
			if ((column != null) && column.includes(word.bounds, true))
				return column;
			for (int c = 0; c < pageColumns.size(); c++) {
				column = ((BoundingBox) pageColumns.get(c));
				if (column.includes(word.bounds, true))
					return column;
			}
			return null;
		}
		private ImRegion getBlockAt(ImWord word, ImRegion block) {
			if ((block != null) && (block.pageId == word.pageId) && block.bounds.includes(word.bounds, true))
				return block;
			ImRegion[] wordBlocks = word.getPage().getRegionsIncluding(ImRegion.BLOCK_ANNOTATION_TYPE, word.bounds, true);
			return ((wordBlocks.length == 0) ? null : wordBlocks[0]);
		}
		private boolean isParagraphStartError(ImWord word, ImWord paraEndWord) {
			this.log("Checking '" + word.getString() + "' at " + word.getLocalID() + " as paragraph start");
			ImWord prevWord = word.getPreviousWord();
			if (prevWord == null) {
				this.log(" =OK=> text stream start");
				return false; // this one isn't coming from anywhere ...
			}
			
			String wordStr = Gamta.normalize(word.getString()).trim();
			this.log(" - string is '" + wordStr + "'");
			if (wordStr.startsWith(",") || wordStr.startsWith(";") || wordStr.startsWith(".") || wordStr.startsWith(":") || wordStr.startsWith(")") || wordStr.startsWith("]") || wordStr.startsWith("}")) {
				this.log(" =BAD=> tailing punctuation or closing bracket");
				return true; // no way (might be OCR error as well)
			}
			if ((prevWord.pageId == word.pageId) && (prevWord.centerX < word.bounds.left) && this.areSameLine(prevWord, word)) {
				this.log(" =BAD=> predecessor on same line");
				return true; // predecessor in same line to left (mid-line paragraph break)
			}
			
			if (this.areSameLine(word, paraEndWord)) {
				this.log(" - paragraph ends at '" + paraEndWord.getString() + "' at " + paraEndWord.getLocalID());
				String lineStr = Gamta.normalize(ImUtils.getString(word, paraEndWord, true)).trim();
				this.log(" - line is '" + lineStr + "'");
				if (this.filterLine(lineStr, paragraphCheckFilterLinePatterns))
					return false;
				if (this.filterLine(lineStr, paragraphStartFilterLinePatterns))
					return false;
			}
			
			//	TODO check if predecessor in different font size (especially if font char codes present, i.e., born-digital)
			
			if (Gamta.isNumber(wordStr)) {
				this.log(" =OK=> Arabic number");
				return false;
			}
			if (Gamta.isRomanNumber(wordStr)) {
				this.log(" =OK=> Roman number");
				return false;
			}
			if (wordStr.startsWith("(") || wordStr.startsWith("[") || wordStr.startsWith("{")) {
				this.log(" =OK=> opening bracket");
				return false;
			}
			if (wordStr.startsWith("§") || wordStr.startsWith("$") || wordStr.startsWith("#") || wordStr.startsWith("-")) {
				this.log(" =OK=> bulletin point");
				return false;
			}
			if (wordStr.startsWith("\"")) {
				this.log(" =BAD=> double quote");
				return true;
			}
			if (wordStr.matches("[a-z]{2,}((\\-|\\')[a-z]+)?")) {
				this.log(" =BAD=> lower case start");
				return true;
			}
			if ((wordStr.length() == 1) && Character.isLowerCase(wordStr.charAt(0))) {
				ImWord nextWord = word.getNextWord();
				if ((nextWord != null) && (nextWord.getString() != null) && (")].:".indexOf(nextWord.getString()) == -1)) {
					this.log(" =BAD=> lower case letter start");
					return true;
				}
			}
			if (Gamta.isWord(wordStr)) {
				this.log(" =OK=> word");
				return false;
			}
			
			this.log(" =BAD=> whatever else");
			return true;
		}
		private boolean isParagraphContinueError(ImWord word, ImWord nextWord, ImRegion block, BoundingBox column) {
			this.log("Checking '" + word.getString() + "' at " + word.getLocalID() + " as paragraph continuation");
			
			String wordStr = Gamta.normalize(word.getString()).trim();
			this.log(" - string is '" + wordStr + "'");
			if (!wordStr.endsWith(".") && !wordStr.endsWith("!") && !wordStr.endsWith("?")) {
				this.log(" =OK=> no terminating punctuation");
				return false;
			}
			
			BoundingBox nextWordBlock = this.getWordBlockBoundsFrom(nextWord);
			this.log(" - next word block at " + nextWordBlock);
			if (block != null) {
				this.log(" - block is " + block.bounds);
				int blockEdgeDist = (block.bounds.right - word.bounds.right);
				this.log(" - right block edge distance is " + blockEdgeDist);
				if (nextWordBlock.getWidth() < blockEdgeDist) {
					this.log(" =BAD=> short line (block)");
					return true; // line short in block, as coherent successor(s) would have fit
				}
			}
			if (column != null) {
				this.log(" - column is " + column);
				int columnEdgeDist = (column.right - word.bounds.right);
				this.log(" - right column edge distance is " + columnEdgeDist);
				if (nextWordBlock.getWidth() < columnEdgeDist) {
					this.log(" =BAD=> short line (column)");
					return true; // line short in column, as coherent successor(s) would have fit (if at cost of extending single-line block)
				}
			}
			
			return false;
		}
		private boolean isParagraphEndError(ImWord word, ImRegion block, BoundingBox column, ImWord paraStartWord) {
			this.log("Checking '" + word.getString() + "' at " + word.getLocalID() + " as paragraph end");
			ImWord nextWord = word.getNextWord();
			if (nextWord == null) {
				this.log(" =OK=> text stream end");
				return false; // this one isn't leading anywhere ...
			}
			
			String wordStr = Gamta.normalize(word.getString()).trim();
			this.log(" - string is '" + wordStr + "'");
			if (wordStr.endsWith(",") || wordStr.endsWith(";") || wordStr.endsWith("(") || wordStr.endsWith("[") || wordStr.endsWith("{")) {
				this.log(" =BAD=> in-sentence punctuation or opening bracket");
				return true; // no way (might be OCR error as well)
			}
			if ((word.pageId == nextWord.pageId) && (word.centerX < nextWord.bounds.left) && this.areSameLine(word, nextWord)) {
				this.log(" =BAD=> successor on same line");
				return true; // successor in same line to right (mid-line paragraph break)
			}
			
			if (this.areSameLine(paraStartWord, word)) {
				this.log(" - paragraph starts at '" + paraStartWord.getString() + "' at " + paraStartWord.getLocalID());
				String lineStr = Gamta.normalize(ImUtils.getString(paraStartWord, word, true)).trim();
				this.log(" - line is '" + lineStr + "'");
				if (this.filterLine(lineStr, paragraphCheckFilterLinePatterns))
					return false;
				if (this.filterLine(lineStr, paragraphEndFilterLinePatterns))
					return false;
			}
			
			if (block != null) {
				this.log(" - block is " + block.bounds);
				int blockEdgeDist = (block.bounds.right - word.bounds.right);
				this.log(" - right block edge distance is " + blockEdgeDist);
				BoundingBox nextWordBlock = this.getWordBlockBoundsFrom(nextWord);
				this.log(" - next word block at " + nextWordBlock);
				if (nextWordBlock.getWidth() < blockEdgeDist) {
					this.log(" =OK=> short line (block)");
					return false; // line short in block, as coherent successor(s) would have fit
				}
				if (((block.pageId != nextWord.pageId) || !block.bounds.includes(nextWord.bounds, true)) && ((blockEdgeDist * 3) < word.bounds.getHeight())) {
					boolean nextWordClearLeftDown = false;
					if (!nextWordClearLeftDown && (word.pageId == nextWord.pageId) && (word.bounds.bottom < nextWord.bounds.top) && (nextWord.bounds.left < word.bounds.right)) {
						BoundingBox wordSpace = new BoundingBox(
								nextWord.bounds.left,
								word.bounds.right,
								word.bounds.bottom,
								nextWord.bounds.top
							);
						this.log(" - space to next word is " + wordSpace);
						if (this.isBlockOrParagraphSpace(wordSpace, word.getPage()))
							nextWordClearLeftDown = true;
					}
					if (!nextWordClearLeftDown && this.areSameLine(paraStartWord, word) && (column != null)) {
						int columnEdgeDist = (column.right - word.bounds.right);
						this.log(" - right column edge distance is " + columnEdgeDist);
						if (nextWordBlock.getWidth() < columnEdgeDist) {
							this.log(" =OK=> short line (column)");
							return false; // line short in column, as coherent successor(s) would have fit (if at cost of extending single-line block)
						}
					}
					if (nextWordClearLeftDown)
						this.log(" - snug on block edge, but successor clear to lower left");
					else {
						this.log(" =BAD=> snug on block edge");
						return true; // just too close to block edge to be last line, at least in (by far most common) justified layout
					}
				}
			}
			
			//	TODO check if successor in different font size (especially if font char codes present, i.e., born-digital)
			
			if (wordStr.endsWith(".") || wordStr.endsWith("!") || wordStr.endsWith("?") || wordStr.endsWith(":")) {
				this.log(" =OK=> terminating punctuation");
				return false; // this one looks like a legit paragraph end
			}
			if (wordStr.endsWith("\"") && (word.getPreviousWord() != null)) {
				ImWord prevWord = word.getPreviousWord();
				this.log(" - pre-quote word is '" + prevWord.getString() + "' at " + prevWord.getLocalID());
				String prevWordStr = Gamta.normalize(word.getString()).trim();
				this.log(" - pre-quote string is '" + prevWordStr + "'");
				if (prevWordStr.endsWith(".") || prevWordStr.endsWith("!") || prevWordStr.endsWith("?")) {
					this.log(" =OK=> terminating punctuation followed by double quote");
					return false; // this one looks like a legit paragraph end
				}
			}
			
			this.log(" =BAD=> whatever else");
			return true;
		}
		private boolean areSameLine(ImWord imw1, ImWord imw2) {
			if ((imw1.bounds.top <= imw2.centerY) && (imw2.centerY < imw1.bounds.bottom))
				return true;
			else if ((imw2.bounds.top <= imw1.centerY) && (imw1.centerY < imw2.bounds.bottom))
				return true;
			else return false;
		}
		private BoundingBox getWordBlockBoundsFrom(ImWord imw) {
			int left = imw.bounds.left;
			int right = imw.bounds.right;
			int top = imw.bounds.top;
			int bottom = imw.bounds.bottom;
			StringBuffer wordBlock = new StringBuffer(imw.getString());
			for (ImWord nextImw = imw.getNextWord(); nextImw != null; nextImw = nextImw.getNextWord()) {
				if (ImUtils.areTextFlowBreak(imw, nextImw))
					break;
				int dist = (nextImw.bounds.left - right);
				if (imw.getNextRelation() == ImWord.NEXT_RELATION_CONTINUE) {}
				else if (Math.max(imw.bounds.getHeight(), nextImw.bounds.getHeight()) < (dist * 4))
					break;
				if (imw.getNextRelation() == ImWord.NEXT_RELATION_CONTINUE) {}
				else if (!Gamta.insertSpace(imw.getString(), nextImw.getString())) {}
				else if ((".".equals(imw.getString()) || ",".equals(imw.getString())) && Gamta.isNumber(nextImw.getString())) {}
				else if ((".".equals(imw.getString()) || ",".equals(imw.getString())) && Gamta.isNumber(nextImw.getString())) {}
				else break;
				right = Math.max(right, nextImw.bounds.right);
				top = Math.min(top, nextImw.bounds.top);
				bottom = Math.max(bottom, nextImw.bounds.bottom);
				imw = nextImw;
				wordBlock.append(imw.getString());
			}
			this.log(" - word block is " + wordBlock);
			return new BoundingBox(left, right, top, bottom);
		}
		private boolean isBlockOrParagraphSpace(BoundingBox bounds, ImPage page) {
			if (page.getImageDPI() < bounds.getHeight()) {
				this.log(" - over full inch tall at " + bounds.getHeight() + ", too large to make sense");
				return false;
			}
			if ((bounds.getHeight() * 4) < page.getImageDPI()) {
				this.log(" - less than quarter inch tall at " + bounds.getHeight() + ", too small for meaningful content");
				return true;
			}
			ImWord[] words = page.getWordsInside(bounds);
			if (words.length != 0) {
				this.log(" - found " + words.length + " words");
				return false;
			}
			ImRegion[] regions = page.getRegionsInside(bounds, true);
			for (int r = 0; r < regions.length; r++) {
				if (ImRegion.IMAGE_TYPE.equals(regions[r].getType())) {
					this.log(" - found image at " + regions[r].bounds);
					return false;
				}
				else if (ImRegion.GRAPHICS_TYPE.equals(regions[r].getType())) {
					this.log(" - found graphics at " + regions[r].bounds);
					return false;
				}
			}
			this.log(" - found to be empty");
			return true;
		}
		private boolean filterLine(String lineStr, ArrayList patterns) {
			for (int p = 0; p < patterns.size(); p++) {
				Pattern pattern = ((Pattern) patterns.get(p));
				if (pattern.matcher(lineStr).matches()) {
					this.log(" =OK=> filtered as matching " + pattern.toString());
					return true;
				}
			}
			/* TODO Do use dedicated LineFilterPattern class:
			 * - denote required tokens as "<fromStart>/<fromEnd>" in first column of config file
			 * - aggregate numbers on pattern loading ...
			 * - ... put them better in LineFilterPatternList class (derived from ArrayList)
			 * - use "-1" for "all" and "0" for "none on this end"
			 *   ==> use text stream position to decide what to skip
			 * - generalize from line to paragraph ...
			 * - ... and assess matching on line breaks in loading process ...
			 * - ... setting respective flag in pattern list ...
			 * - ... maybe using line mode "S" for "single line" and "M" for "multi line"
			 *   ==> way higher flexibility
			 * ==> generally reduced string assembly effort in practice
			 */
			return false;
		}
		private LinkedHashSet getContextAnnotationTypes(ImWord word) {
			LinkedHashSet cats = new LinkedHashSet();
			ImAnnotation[] cas = word.getDocument().getAnnotationsOverlapping(word);
			for (int a = 0; a < cas.length; a++)
				cats.add(cas[a].getType());
			return cats;
		}
		private String getErrorSeverity(LinkedHashSet contextAnnotTypes, LinkedHashMap severityMapping) {
			for (Iterator atit = severityMapping.keySet().iterator(); atit.hasNext();) {
				String cat = ((String) atit.next());
				if (contextAnnotTypes.contains(cat)) {
					String catSeverity = ((String) severityMapping.get(cat));
					this.log(" ==> mapped severity on context " + cat + " --> " + ((catSeverity == null) ? "filtered" : catSeverity));
					return catSeverity;
				}
			}
			if (severityMapping.containsKey("*")) {
				String defSeverity = ((String) severityMapping.get("*"));
				this.log(" ==> mapped severity on default --> " + ((defSeverity == null) ? "filtered" : defSeverity));
				return defSeverity;
			}
			return ((severityMapping == paragraphWordContextsToSeverities) ? DocumentError.SEVERITY_CRITICAL : this.getErrorSeverity(contextAnnotTypes, paragraphWordContextsToSeverities));
		}
	}
	
	private static class WordPositionStats {
		private CountingSet wordCounts = new CountingSet(new HashMap());
		private CountingSet lineStartWordCounts = new CountingSet(new HashMap());
		private CountingSet paraStartWordCounts = new CountingSet(new HashMap());
		void add(ImWord word) {
			String wordKey = this.getWordKey(word);
			if (wordKey == null)
				return;
			this.wordCounts.add(wordKey);
			if ((word.getPreviousWord() == null) || ImUtils.areTextFlowBreak(word.getPreviousWord(), word))
				this.lineStartWordCounts.add(wordKey);
			if (word.getPreviousRelation() == ImWord.NEXT_RELATION_PARAGRAPH_END)
				this.paraStartWordCounts.add(wordKey);
		}
		
		int getWordCount() {
			return this.wordCounts.size();
		}
		
		int getCount(ImWord word) {
			String wordKey = this.getWordKey(word);
			return ((wordKey == null) ? -1 : this.wordCounts.getCount(wordKey));
		}
		int getLineStartCount(ImWord word) {
			String wordKey = this.getWordKey(word);
			return ((wordKey == null) ? -1 : this.lineStartWordCounts.getCount(wordKey));
		}
		int getParaStartCount(ImWord word) {
			String wordKey = this.getWordKey(word);
			return ((wordKey == null) ? -1 : this.paraStartWordCounts.getCount(wordKey));
		}
		
		int getLineStartPercent(ImWord word) {
			String wordKey = this.getWordKey(word);
			if (wordKey == null)
				return -1;
			int count = this.wordCounts.getCount(wordKey);
			if (count == 0)
				return -1;
			int lsCount = this.lineStartWordCounts.getCount(wordKey);
			return (((lsCount * 100) + (count / 2)) / count);
		}
		int getParaStartPercent(ImWord word) {
			String wordKey = this.getWordKey(word);
			if (wordKey == null)
				return -1;
			int count = this.wordCounts.getCount(wordKey);
			if (count == 0)
				return -1;
			int psCount = this.paraStartWordCounts.getCount(wordKey);
			return (((psCount * 100) + (count / 2)) / count);
		}
		
		private String getWordKey(ImWord word) {
			if (word.getPreviousRelation() == ImWord.NEXT_RELATION_CONTINUE)
				return null;
			if (word.getPreviousRelation() == ImWord.NEXT_RELATION_HYPHENATED)
				return null;
			if (word.getString() == null)
				return null;
			String wordStr = Gamta.normalize(ImUtils.getStringFrom(word));
			if (!Gamta.isWord(wordStr))
				return null;
			StringBuffer wordKey = new StringBuffer();
			if (word.hasAttribute(ImWord.BOLD_ATTRIBUTE))
				wordKey.append("b");
			if (word.hasAttribute(ImWord.ITALICS_ATTRIBUTE))
				wordKey.append("i");
			wordKey.append(":");
			wordKey.append(wordStr);
			return wordKey.toString();
		}
	}
}
