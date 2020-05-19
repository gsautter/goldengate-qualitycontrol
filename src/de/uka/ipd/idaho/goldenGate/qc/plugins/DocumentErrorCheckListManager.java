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
package de.uka.ipd.idaho.goldenGate.qc.plugins;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.util.CountingSet;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol.DocumentError;
import de.uka.ipd.idaho.gamta.util.gPath.GPath;
import de.uka.ipd.idaho.gamta.util.gPath.GPathEditorPanel;
import de.uka.ipd.idaho.gamta.util.gPath.GPathParser;
import de.uka.ipd.idaho.gamta.util.gPath.exceptions.GPathException;
import de.uka.ipd.idaho.gamta.util.swing.AnnotationDisplayDialog;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.goldenGate.qc.DocumentErrorManager;
import de.uka.ipd.idaho.goldenGate.util.DataListListener;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.htmlXmlUtil.Parser;
import de.uka.ipd.idaho.htmlXmlUtil.TokenReceiver;
import de.uka.ipd.idaho.htmlXmlUtil.TreeNodeAttributeSet;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar;

/**
 * Document error check list manager provides error check lists as resources.
 * Client components can use these error check lists and their contained error
 * checks to populate error protocols for documents. This class only provides
 * IO as well as editing and testing facilities.
 * 
 * @author sautter
 */
public class DocumentErrorCheckListManager extends AbstractResourceManager {
	private static final String FILE_EXTENSION = ".errorCheckList";
	
	private static final String ERROR_CHECK_LEVEL_PAGE = "page";
	private static final String ERROR_CHECK_LEVEL_STREAM = "stream";
	private static final String[] errorCheckLevels = {
		ERROR_CHECK_LEVEL_STREAM,
		ERROR_CHECK_LEVEL_PAGE
	};
	
	private static final String[] errorSeverities = {
		DocumentError.SEVERITY_BLOCKER,
		DocumentError.SEVERITY_CRITICAL,
		DocumentError.SEVERITY_MAJOR,
		DocumentError.SEVERITY_MINOR,
	};
	
	private boolean errorMetadataLoaded = false;
	private DocumentErrorProtocol errorMetadataKeeper = new DocumentErrorProtocol() {
		public int getErrorCount() {
			return 0;
		}
		public int getErrorSeverityCount(String severity) {
			return 0;
		}
		public DocumentError[] getErrors() {
			return null;
		}
		public int getErrorCount(String category) {
			return 0;
		}
		public int getErrorSeverityCount(String category, String severity) {
			return 0;
		}
		public DocumentError[] getErrors(String category) {
			return null;
		}
		public int getErrorCount(String category, String type) {
			return 0;
		}
		public int getErrorSeverityCount(String category, String type, String severity) {
			return 0;
		}
		public DocumentError[] getErrors(String category, String type) {
			return null;
		}
		public Attributed findErrorSubject(Attributed doc, String[] data) {
			return null;
		}
		public void addError(String source, Attributed subject, Attributed parent, String category, String type, String description, String severity, boolean falsePositive) {}
		public void removeError(DocumentError error) {}
		public boolean isFalsePositive(DocumentError error) {
			return false;
		}
		public boolean markFalsePositive(DocumentError error) {
			return false;
		}
		public boolean unmarkFalsePositive(DocumentError error) {
			return false;
		}
		public DocumentError[] getFalsePositives() {
			return null;
		}
		public Comparator getErrorComparator() {
			return null;
		}
	};
	
	/** zero-argument constructor for class loading */
	public DocumentErrorCheckListManager() {}
	
	public String getPluginName() {
		return "Document Error CheckList Manager";
	}
	
	private void ensureErrorMetadataLoaded() {
		if (this.errorMetadataLoaded)
			return;
		
		//	load persisted error categories and types
		if (this.dataProvider.isDataAvailable("errorMetadata.txt")) try {
			DocumentErrorProtocol.fillErrorProtocol(this.errorMetadataKeeper, null, this.dataProvider.getInputStream("errorMetadata.txt"));
		}
		catch (IOException ioe) {
			System.out.println("Could not load error metadata: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		
		//	get error categories and types from error managers
		GoldenGatePlugin[] errorManagers = this.parent.getImplementingPlugins(DocumentErrorManager.class);
		for (int m = 0; m < errorManagers.length; m++) {
			DocumentErrorProtocol errorMetadata = ((DocumentErrorManager) errorManagers[m]).getErrorMetadata();
			String[] categories = errorMetadata.getErrorCategories();
			for (int c = 0; c < categories.length; c++) {
				String cLabel = errorMetadata.getErrorCategoryLabel(categories[c]);
				String cDescription = errorMetadata.getErrorCategoryDescription(categories[c]);
				if ((cLabel != null) && (cDescription != null))
					this.errorMetadataKeeper.addErrorCategory(categories[c], cLabel, cDescription);
				String[] types = errorMetadata.getErrorTypes(categories[c]);
				for (int t = 0; t < types.length; t++) {
					String tLabel = errorMetadata.getErrorTypeLabel(categories[c], types[t]);
					String tDescription = errorMetadata.getErrorTypeDescription(categories[c], types[t]);
					if ((tLabel != null) && (tDescription != null))
						this.errorMetadataKeeper.addErrorType(categories[c], types[t], tLabel, tDescription);
				}
			}
		}
		
		//	finally, load error categories and types from own check lists
		String[] eclNames = this.getResourceNames();
		for (int n = 0; n < eclNames.length; n++) {
			ErrorCheckList ecl = this.getErrorCheckList(eclNames[n]);
			if (ecl == null)
				continue;
			if (ecl.category == null)
				continue;
			if ((ecl.categoryLabel != null) && (ecl.categoryDescription != null))
				this.errorMetadataKeeper.addErrorCategory(ecl.category, ecl.categoryLabel, ecl.categoryDescription);
			ErrorType[] types = ecl.getErrorTypes();
			for (int t = 0; t < types.length; t++) {
				if ((types[t].name != null) && (types[t].label != null) && (types[t].description != null))
					this.errorMetadataKeeper.addErrorType(ecl.category, types[t].name, types[t].label, types[t].description);
			}
		}
		
		//	remember we've done all the hustle and dance
		this.errorMetadataLoaded = true;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#exit()
	 */
	public void exit() {
		
		//	persist error categories and types (if they were loaded before)
		if (this.errorMetadataLoaded && this.dataProvider.isDataEditable("errorMetadata.txt")) try {
			DocumentErrorProtocol.storeErrorProtocol(this.errorMetadataKeeper, this.dataProvider.getOutputStream("errorMetadata.txt"));
		}
		catch (IOException ioe) {
			System.out.println("Could not store error metadata: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.ResourceManager#getResourceTypeLabel()
	 */
	public String getResourceTypeLabel() {
		return "Document Error CheckList";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager#getFileExtension()
	 */
	protected String getFileExtension() {
		return FILE_EXTENSION;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#getMainMenuTitle()
	 */
	public String getMainMenuTitle() {
		return "Document Error CheckLists";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#getMainMenuItems()
	 */
	public JMenuItem[] getMainMenuItems() {
		if (!this.dataProvider.isDataEditable())
			return new JMenuItem[0];
		
		ArrayList collector = new ArrayList();
		JMenuItem mi;
		
		mi = new JMenuItem("Create");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				createErrorCheckList();
			}
		});
		collector.add(mi);
		mi = new JMenuItem("Edit");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				editErrorCheckLists();
			}
		});
		collector.add(mi);
		mi = new JMenuItem("Print");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				printErrorCheckLists();
			}
		});
		collector.add(mi);
		
		return ((JMenuItem[]) collector.toArray(new JMenuItem[collector.size()]));
	}
	
	/**
	 * Retrieve an error check list by its name.
	 * @param name the name of the sought error check list
	 * @return the error check list with the specified name
	 */
	public ErrorCheckList getErrorCheckList(String name) {
		return this.loadErrorCheckList(name);
	}
	
	/**
	 * An error check list, consisting of one or more error checks organized by
	 * error types, belonging to the same error category.
	 * 
	 * @author sautter
	 */
	public static class ErrorCheckList {
		
		/** the label of the error check list proper */
		public final String label;
		
		/** the description of the error check list proper */
		public final String description;
		
		/** apply the error check list by default? */
		public final boolean applyByDefault;
		
		
		/** the category of errors the checks in this list look for */
		public final String category;
		
		/** the label of the error category */
		public final String categoryLabel;
		
		/** the description of the error category */
		public final String categoryDescription;
		
		final TreeMap typesByName = new TreeMap();
		
		ErrorCheckList(String label, String description, boolean applyByDefault, String category, String categoryLabel, String categoryDescription) {
			this.label = label;
			this.description = description;
			this.applyByDefault = applyByDefault;
			this.category = category;
			this.categoryLabel = categoryLabel;
			this.categoryDescription = categoryDescription;
		}
		
		void writeXml(BufferedWriter bw) throws IOException {
			bw.write("<errorCheckList");
			bw.write(" label=\"" + xml.escape(this.label) + "\"");
			bw.write(" description=\"" + xml.escape(this.description) + "\"");
			bw.write(" applyByDefault=\"" + this.applyByDefault + "\"");
			bw.write(" catName=\"" + xml.escape(this.category) + "\"");
			bw.write(" catLabel=\"" + xml.escape(this.categoryLabel) + "\"");
			bw.write(" catDescription=\"" + xml.escape(this.categoryDescription) + "\"");
			
			//	do we have types to write?
			if (this.typesByName.isEmpty()) {
				bw.write("/>");
				bw.newLine();
				return;
			}
			else {
				bw.write(">");
				bw.newLine();
			}
			
			//	store types
			for (Iterator tit = this.typesByName.keySet().iterator(); tit.hasNext();) {
				ErrorType type = ((ErrorType) this.typesByName.get(tit.next()));
				type.writeXml(bw);
			}
			
			bw.write("</errorCheckList>");
			bw.newLine();
		}
		
		/**
		 * Retrieve the names of all error types in the check list.
		 * @return an array holding the error type names
		 */
		public String[] getErrorTypeNames() {
			ArrayList etns = new ArrayList(this.typesByName.keySet());
			return ((String[]) etns.toArray(new String[etns.size()]));
		}
		
		/**
		 * Retrieve an individual error type by its name.
		 * @param name the name of the sought error type
		 * @return the error type with the specified name
		 */
		public ErrorType getErrorType(String name) {
			return ((ErrorType) this.typesByName.get(name));
		}
		
		/**
		 * Retrieve the error types in the check list.
		 * @return an array holding the error types
		 */
		public ErrorType[] getErrorTypes() {
			ArrayList ets = new ArrayList(this.typesByName.values());
			return ((ErrorType[]) ets.toArray(new ErrorType[ets.size()]));
		}
	}
	
	/**
	 * An error type, consisting of one or more error checks.
	 * 
	 * @author sautter
	 */
	public static class ErrorType {
		
		/** the name of the error type */
		public final String name;
		
		/** the label of the error type */
		public final String label;
		
		/** the description of the error type */
		public final String description;
		
		final ArrayList checks = new ArrayList();
		
		ErrorType(String name, String label, String description) {
			this.name = name;
			this.label = label;
			this.description = description;
		}
		
		void writeXml(BufferedWriter bw) throws IOException {
			bw.write("\t<errorType");
			bw.write(" name=\"" + xml.escape(this.name) + "\"");
			bw.write(" label=\"" + xml.escape(this.label) + "\"");
			bw.write(" description=\"" + xml.escape(this.description) + "\"");
			
			//	do we have checks to write?
			if (this.checks.isEmpty()) {
				bw.write("/>");
				bw.newLine();
				return;
			}
			else {
				bw.write(">");
				bw.newLine();
			}
			
			//	store types
			for (int c = 0; c < this.checks.size(); c++) {
				ErrorCheck check = ((ErrorCheck) this.checks.get(c));
				check.writeXml(bw);
			}
			
			bw.write("\t</errorType>");
			bw.newLine();
		}
		
		/**
		 * Retrieve the error checks belonging to this type.
		 * @return an array holding the error checks
		 */
		public ErrorCheck[] getErrorChecks() {
			return ((ErrorCheck[]) this.checks.toArray(new ErrorCheck[this.checks.size()]));
		}
	}
	
	/**
	 * An error check, amending an XPath based check with the XML normalization
	 * level of application, the severity of identified errors, and an error
	 * message template.
	 * 
	 * @author sautter
	 */
	public static class ErrorCheck {
		
		/** the level of XML normalization to apply this check with */
		public final String level;
		
		/** the severity of the errors identified by this check */
		public final String severity;
		
		/** the template for the error messages to produce from this check */
		public final String description;
		
		/** the XPath to use for error checking, as originally entered */
		public String test;
		
		ErrorCheck(String level, String severity, String description, String test) {
			this.level = level;
			this.severity = severity;
			this.description = description;
			this.test = test;
		}
		
		void writeXml(BufferedWriter bw) throws IOException {
			bw.write("\t\t<errorCheck");
			bw.write(" level=\"" + xml.escape(this.level) + "\"");
			bw.write(" severity=\"" + xml.escape(this.severity) + "\"");
			bw.write(" description=\"" + xml.escape(this.description) + "\"");
			bw.write(">");
			bw.write(xml.escape(this.test));
			bw.write("</errorCheck>");
			bw.newLine();
		}

		/**
		 * Retrieve the XPath test of the error check as a string.
		 * @return the XPath test
		 */
		public String getTest() {
			return this.test;
		}
	}
	
	private static final Grammar xml = new StandardGrammar();
	private static final Parser parser = new Parser(xml);
	
	private ErrorCheckList loadErrorCheckList(String eclName) {
		if (this.dataProvider.isDataAvailable(eclName)) try {
			BufferedReader br = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream(eclName), "UTF-8"));
			final ErrorCheckList[] tErrorCheckList = {null};
			parser.stream(br, new TokenReceiver() {
				private ErrorCheckList errorCheckList = null;
				private ErrorType errorType = null;
				private ErrorCheck errorCheck = null;
				public void storeToken(String token, int treeDepth) throws IOException {
					if (xml.isTag(token)) {
						String type = xml.getType(token);
						if (xml.isEndTag(token)) {
							if ("errorCheckList".equals(type)) {
								tErrorCheckList[0] = this.errorCheckList;
								this.errorCheckList = null;
							}
							else if ("errorType".equals(type)) {
								if (this.errorType != null)
									this.errorCheckList.typesByName.put(this.errorType.name, this.errorType);
								this.errorType = null;
							}
							else if ("errorCheck".equals(type)) {
								if (this.errorCheck != null)
									this.errorType.checks.add(this.errorCheck);
								this.errorCheck = null;
							}
						}
						else {
							TreeNodeAttributeSet tnas = TreeNodeAttributeSet.getTagAttributes(token, xml);
							if ("errorCheckList".equals(type)) {
								String label = tnas.getAttribute("label");
								String description = tnas.getAttribute("description");
								boolean applyByDefault = Boolean.parseBoolean(tnas.getAttribute("applyByDefault", "true"));
								String catName = tnas.getAttribute("catName");
								String catLabel = tnas.getAttribute("catLabel");
								String catDescription = tnas.getAttribute("catDescription");
								this.errorCheckList = new ErrorCheckList(label, description, applyByDefault, catName, catLabel, catDescription);
								if (xml.isSingularTag(token)) {
									tErrorCheckList[0] = this.errorCheckList;
									this.errorCheckList = null;
								}
							}
							else if ("errorType".equals(type) && (this.errorCheckList != null)) {
								String name = tnas.getAttribute("name");
								String label = tnas.getAttribute("label");
								String description = tnas.getAttribute("description");
								this.errorType = new ErrorType(name, label, description);
								if (xml.isSingularTag(token)) {
									this.errorCheckList.typesByName.put(this.errorType.name, this.errorType);
									this.errorType = null;
								}
							}
							else if ("errorCheck".equals(type) && (this.errorType != null)) {
								String level = tnas.getAttribute("level");
								String severity = tnas.getAttribute("severity");
								String description = tnas.getAttribute("description");
								this.errorCheck = new ErrorCheck(level, severity, description, null);
								if (xml.isSingularTag(token)) {
									this.errorType.checks.add(this.errorCheck);
									this.errorCheck = null;
								}
							}
						}
					}
					else if (this.errorCheck != null)
						this.errorCheck.test = xml.unescape(token.trim());
				}
				public void close() throws IOException {}
			});
			br.close();
			return tErrorCheckList[0];
		}
		catch (IOException ioe) {
			System.out.println("Could not load error check list '" + eclName + "':" + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		return null;
	}
	
	private boolean storeErrorCheckList(String eclName, ErrorCheckList ecl) throws IOException {
		if (this.dataProvider.isDataEditable(eclName)) {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.dataProvider.getOutputStream(eclName), "UTF-8"));
			ecl.writeXml(bw);
			bw.flush();
			bw.close();
			if (this.parent != null)
				this.parent.notifyResourceUpdated(this.getClass().getName(), eclName);
			return true;
		}
		else return false;
	}
	
	private boolean createErrorCheckList() {
		return (this.createErrorCheckList(null, null) != null);
	}
	
	private boolean cloneErrorCheckList() {
		String selectedName = this.resourceNameList.getSelectedName();
		if (selectedName == null)
			return this.createErrorCheckList();
		else {
			String name = "New " + selectedName;
			ErrorCheckList model = this.loadErrorCheckList(selectedName);
			ErrorCheckList errorChecker = ((model == null) ? new ErrorCheckList("", "", true, "", "", "") : model);
			return (this.createErrorCheckList(errorChecker, name) != null);
		}
	}
	
	private String createErrorCheckList(ErrorCheckList errorCheckList, String name) {
		this.ensureErrorMetadataLoaded(); // we only need it all for check list editing UI
		
		CreateErrorCheckListDialog cld = new CreateErrorCheckListDialog(name, errorCheckList, this.errorMetadataKeeper);
		cld.setVisible(true);
		
		if (cld.isCommitted()) {
			ErrorCheckList newErrorCheckList = cld.getErrorCheckList();
			String errorCheckListName = cld.getErrorCheckListName();
			if (!errorCheckListName.endsWith(FILE_EXTENSION))
				errorCheckListName += FILE_EXTENSION;
			try {
				if (this.storeErrorCheckList(errorCheckListName, newErrorCheckList)) {
					this.resourceNameList.refresh();
					return errorCheckListName;
				}
			} catch (IOException e) {}
		}
		return null;
	}
	
	private void editErrorCheckLists() {
		final ErrorCheckListEditorPanel[] editor = new ErrorCheckListEditorPanel[1];
		editor[0] = null;
		
		final DialogPanel editDialog = new DialogPanel("Edit Error Checkers", true);
		editDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		editDialog.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent we) {
				this.closeDialog();
			}
			public void windowClosing(WindowEvent we) {
				this.closeDialog();
			}
			private void closeDialog() {
				if ((editor[0] != null) && editor[0].isDirty()) try {
					storeErrorCheckList(editor[0].errorCheckListName, editor[0].getContent());
				} catch (IOException ioe) {}
				if (editDialog.isVisible())
					editDialog.dispose();
			}
		});
		
		JPanel editButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton button;
		
		button = new JButton("Create");
		button.setBorder(BorderFactory.createRaisedBevelBorder());
		button.setPreferredSize(new Dimension(80, 21));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createErrorCheckList();
			}
		});
		editButtons.add(button);
		
		button = new JButton("Clone");
		button.setBorder(BorderFactory.createRaisedBevelBorder());
		button.setPreferredSize(new Dimension(80, 21));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cloneErrorCheckList();
			}
		});
		editButtons.add(button);
		
		button = new JButton("Delete");
		button.setBorder(BorderFactory.createRaisedBevelBorder());
		button.setPreferredSize(new Dimension(80, 21));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String eclName = resourceNameList.getSelectedName();
				if (deleteResource(eclName))
					resourceNameList.refresh();
			}
		});
		editButtons.add(button);
		
		button = new JButton("Print");
		button.setBorder(BorderFactory.createRaisedBevelBorder());
		button.setPreferredSize(new Dimension(80, 21));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				printErrorCheckLists();
			}
		});
		editButtons.add(button);
		
		editDialog.add(editButtons, BorderLayout.NORTH);
		
		final JPanel editorPanel = new JPanel(new BorderLayout());
		String selectedName = this.resourceNameList.getSelectedName();
		if (selectedName == null)
			editorPanel.add(this.getExplanationLabel(), BorderLayout.CENTER);
		else {
			ErrorCheckList errorChecker = this.loadErrorCheckList(selectedName);
			if (errorChecker == null)
				editorPanel.add(this.getExplanationLabel(), BorderLayout.CENTER);
			else {
				this.ensureErrorMetadataLoaded(); // we only need it all for check list editing UI
				editor[0] = new ErrorCheckListEditorPanel(selectedName, errorChecker, this.errorMetadataKeeper, editDialog.getDialog());
				editorPanel.add(editor[0], BorderLayout.CENTER);
			}
		}
		editDialog.add(editorPanel, BorderLayout.CENTER);
		
		editDialog.add(this.resourceNameList, BorderLayout.EAST);
		DataListListener dll = new DataListListener() {
			public void selected(String dataName) {
				if ((editor[0] != null) && editor[0].isDirty()) try {
					storeErrorCheckList(editor[0].errorCheckListName, editor[0].getContent());
				}
				catch (IOException ioe) {
					if (JOptionPane.showConfirmDialog(editDialog, (ioe.getClass().getName() + " (" + ioe.getMessage() + ")\nwhile saving file to " + editor[0].errorCheckListName + "\nProceed?"), "Could Not Save Error Checker", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
						resourceNameList.setSelectedName(editor[0].errorCheckListName);
						editorPanel.validate();
						return;
					}
				}
				editorPanel.removeAll();
				
				if (dataName == null)
					editorPanel.add(getExplanationLabel(), BorderLayout.CENTER);
				else {
					ErrorCheckList errorChecker = loadErrorCheckList(dataName);
					if (errorChecker == null)
						editorPanel.add(getExplanationLabel(), BorderLayout.CENTER);
					else {
						ensureErrorMetadataLoaded(); // we only need it all for check list editing UI
						editor[0] = new ErrorCheckListEditorPanel(dataName, errorChecker, errorMetadataKeeper, editDialog.getDialog());
						editorPanel.add(editor[0], BorderLayout.CENTER);
					}
				}
				editorPanel.validate();
			}
		};
		this.resourceNameList.addDataListListener(dll);
		
		editDialog.setSize(DEFAULT_EDIT_DIALOG_SIZE);
		editDialog.setLocationRelativeTo(editDialog.getOwner());
		editDialog.setVisible(true);
		
		this.resourceNameList.removeDataListListener(dll);
	}
	
	private class CreateErrorCheckListDialog extends DialogPanel {
		private JTextField nameField;
		private ErrorCheckListEditorPanel editor;
		private ErrorCheckList errorCheckList = null;
		private String errorCheckListName = null;
		CreateErrorCheckListDialog(String name, ErrorCheckList errorCheckList, DocumentErrorProtocol errorMetadata) {
			super("Create Error Checker", true);
			
			this.nameField = new JTextField((name == null) ? "New Error Checker" : name);
			this.nameField.setBorder(BorderFactory.createLoweredBevelBorder());
			
			//	initialize main buttons
			JButton commitButton = new JButton("Create");
			commitButton.setBorder(BorderFactory.createRaisedBevelBorder());
			commitButton.setPreferredSize(new Dimension(100, 21));
			commitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CreateErrorCheckListDialog.this.errorCheckList = editor.getContent();
					errorCheckListName = nameField.getText();
					dispose();
				}
			});
			
			JButton abortButton = new JButton("Cancel");
			abortButton.setBorder(BorderFactory.createRaisedBevelBorder());
			abortButton.setPreferredSize(new Dimension(100, 21));
			abortButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CreateErrorCheckListDialog.this.errorCheckList = null;
					errorCheckListName = null;
					dispose();
				}
			});
			
			JPanel mainButtonPanel = new JPanel();
			mainButtonPanel.setLayout(new FlowLayout());
			mainButtonPanel.add(commitButton);
			mainButtonPanel.add(abortButton);
			
			//	initialize editor
			this.editor = new ErrorCheckListEditorPanel(name, errorCheckList, errorMetadata, this.getDialog());
			
			//	put the whole stuff together
			this.setLayout(new BorderLayout());
			this.add(this.nameField, BorderLayout.NORTH);
			this.add(this.editor, BorderLayout.CENTER);
			this.add(mainButtonPanel, BorderLayout.SOUTH);
			
			this.setResizable(true);
			this.setSize(new Dimension(600, 400));
			this.setLocationRelativeTo(DialogPanel.getTopWindow());
		}
		
		boolean isCommitted() {
			return (this.errorCheckList != null);
		}
		
		ErrorCheckList getErrorCheckList() {
			return this.errorCheckList;
		}
		
		String getErrorCheckListName() {
			return this.errorCheckListName;
		}
	}
	
	private class ErrorCheckListEditorPanel extends JPanel implements DocumentListener, ItemListener {
		private JTextField label = new JTextField();
		private JTextField description = new JTextField();
		private JCheckBox applyByDefault = new JCheckBox("Apply by Default?", true);
		
		private JComboBox catName;
		private JTextField catLabel = new JTextField();
		private JTextField catDescription = new JTextField();
		
		private JTabbedPane typeTabs = new JTabbedPane(JTabbedPane.LEFT);
		
		private boolean dirty = false;
		private String errorCheckListName;
		
		DocumentErrorProtocol errorMetadata;
		JDialog dialog;
		
		ErrorCheckListEditorPanel(String name, ErrorCheckList errorCheckList, DocumentErrorProtocol errorMetadata, JDialog dialog) {
			super(new BorderLayout(), true);
			this.errorCheckListName = name;
			this.errorMetadata = errorMetadata;
			this.dialog = dialog;
			
			//	add error checker data
			this.label.setPreferredSize(new Dimension(100, 12)); // need to set this, as otherwise field will be compressed to next to nothing
			this.label.getDocument().addDocumentListener(this);
			this.description.getDocument().addDocumentListener(this);
			this.applyByDefault.setToolTipText("Apply the error checker if a style template is not available or does not explicitly specify the behavior for the error checker?");
			JPanel dataPanel = new JPanel(new BorderLayout(), true);
			dataPanel.add(new LabeledInputFieldPanel("Label", "UI nice name for the error checker, used in style template editor", this.label), BorderLayout.WEST);
			dataPanel.add(new LabeledInputFieldPanel("Description", "Technical description for the error checker, used in style template editor", this.description), BorderLayout.CENTER);
			dataPanel.add(this.applyByDefault, BorderLayout.EAST);
			
			//	add error category data
			this.catName = new JComboBox(errorMetadata.getErrorCategories());
			this.catName.setEditable(true);
			((JTextComponent) this.catName.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
			this.catName.addItemListener(this);
			this.catName.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					String cat = getCategoryName();
					if (cat == null)
						return;
					String lab = ErrorCheckListEditorPanel.this.errorMetadata.getErrorCategoryLabel(cat);
					if ((lab != null) && (catLabel.getDocument().getLength() == 0))
						catLabel.setText(lab);
					String desc = ErrorCheckListEditorPanel.this.errorMetadata.getErrorCategoryDescription(cat);
					if ((desc != null) && (catDescription.getDocument().getLength() == 0))
						catDescription.setText(desc);
				}
			});
			this.catLabel.getDocument().addDocumentListener(this);
			this.catDescription.getDocument().addDocumentListener(this);
			JPanel categoryTopPanel = new JPanel(new GridLayout(1, 0), true);
			categoryTopPanel.add(new LabeledInputFieldPanel("Category", "Technical name for the error category, for sorting and grouping errors in protocols", this.catName));
			categoryTopPanel.add(new LabeledInputFieldPanel("Label", "UI nice name for the error category, used in error protocols", this.catLabel));
			JPanel categoryPanel = new JPanel(new GridLayout(0, 1), true);
			categoryPanel.add(categoryTopPanel);
			categoryPanel.add(new LabeledInputFieldPanel("Description", "Verbal description of the error category, used in error protocols", this.catDescription));
			
			//	complete top panel
			JPanel topPanel = new JPanel(new BorderLayout(), true);
			topPanel.add(dataPanel, BorderLayout.NORTH);
			topPanel.add(categoryPanel, BorderLayout.CENTER);
			
			//	create buttons
			JButton addTypeButton = new JButton("Add Type");
			addTypeButton.setBorder(BorderFactory.createRaisedBevelBorder());
			addTypeButton.setPreferredSize(new Dimension(100, 21));
			addTypeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					addErrorType();
				}
			});
			
			JButton cloneTypeButton = new JButton("Clone Type");
			cloneTypeButton.setBorder(BorderFactory.createRaisedBevelBorder());
			cloneTypeButton.setPreferredSize(new Dimension(100, 21));
			cloneTypeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					cloneErrorType();
				}
			});
			
			JButton removeTypeButton = new JButton("Remove Type");
			removeTypeButton.setBorder(BorderFactory.createRaisedBevelBorder());
			removeTypeButton.setPreferredSize(new Dimension(100, 21));
			removeTypeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					removeErrorType();
				}
			});
			
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER), true);
			buttonPanel.add(addTypeButton);
			buttonPanel.add(cloneTypeButton);
			buttonPanel.add(removeTypeButton);
			topPanel.add(buttonPanel, BorderLayout.SOUTH);
			
			//	put the whole stuff together
			this.add(topPanel, BorderLayout.NORTH);
			this.add(this.typeTabs, BorderLayout.CENTER);
			this.setContent((errorCheckList == null) ? new ErrorCheckList("", "", true, "", "", "") : errorCheckList);
		}
		
		void addErrorType() {
			this.addErrorType(null);
		}
		void cloneErrorType() {
			this.addErrorType((ErrorTypeEditorPanel) this.typeTabs.getSelectedComponent());
		}
		void addErrorType(ErrorTypeEditorPanel model) {
			ErrorType modelType = ((model == null) ? new ErrorType("", "", "") : model.getContent());
			String modelSeverity = ((model == null) ? DocumentError.SEVERITY_CRITICAL : model.dataPanel.getDefaultSeverity());
			ArrayList modelChecks = new ArrayList(modelType.checks);
			ErrorTypeDataPanel dataPanel = new ErrorTypeDataPanel(null, modelType, modelSeverity);
			while (true) {
				int choice = DialogFactory.confirm(dataPanel, ((model == null) ? "Add Error Type" : "Clone Error Type"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (choice != JOptionPane.OK_OPTION)
					return;
				modelType = dataPanel.getContent();
				if (modelType == null) {
					DialogFactory.alert("The error type must not be empty.", "Invalid Error Type Name", JOptionPane.ERROR_MESSAGE);
					continue;
				}
				modelSeverity = dataPanel.getDefaultSeverity();
				modelType.checks.addAll(modelChecks);
				break;
			}
			ErrorTypeEditorPanel typePanel = new ErrorTypeEditorPanel(modelType, modelSeverity);
			this.typeTabs.addTab(modelType.name, typePanel);
			this.typeTabs.setSelectedComponent(typePanel);
			this.dirty = true;
		}
		void removeErrorType() {
			ErrorTypeEditorPanel typePanel = ((ErrorTypeEditorPanel) this.typeTabs.getSelectedComponent());
			if (typePanel == null)
				return;
			this.typeTabs.remove(typePanel);
			this.dirty = true;
		}
		
		String getCategoryName() {
			Object name = this.catName.getSelectedItem();
			return ((name == null) ? null : name.toString());
		}
		ErrorCheckList getContent() {
			String label = this.label.getText().trim();
			if (label.length() == 0)
				return null;
			String category = this.getCategoryName();
			if ((category == null) || (category.trim().length() == 0))
				return null;
			ErrorCheckList ecl = new ErrorCheckList(label, this.description.getText().trim(), this.applyByDefault.isSelected(), category, this.catLabel.getText().trim(), this.catDescription.getText().trim());
			for (int t = 0; t < this.typeTabs.getComponentCount(); t++) {
				Component tab = this.typeTabs.getComponent(t);
				if (tab instanceof ErrorTypeEditorPanel) {
					ErrorType et = ((ErrorTypeEditorPanel) tab).getContent();
					if (et != null)
						ecl.typesByName.put(et.name, et);
					//	TODO reconcile on name collisions?
				}
			}
			return ecl;
		}
		
		void setContent(ErrorCheckList errorChecker) {
			this.label.setText(errorChecker.label);
			this.description.setText(errorChecker.description);
			this.applyByDefault.setSelected(errorChecker.applyByDefault);
			this.catName.setSelectedItem(errorChecker.category);
			this.catLabel.setText(errorChecker.categoryLabel);
			this.catDescription.setText(errorChecker.categoryDescription);
			this.typeTabs.removeAll();
			for (Iterator tit = errorChecker.typesByName.keySet().iterator(); tit.hasNext();) {
				ErrorType errorType = ((ErrorType) errorChecker.typesByName.get(tit.next()));
				this.typeTabs.addTab(errorType.name, new ErrorTypeEditorPanel(errorType, null));
			}
			this.dirty = false;
		}
		
		boolean isDirty() {
			return this.dirty;
		}
		
		public void changedUpdate(DocumentEvent de) {
			//	attribute changes are not of interest for now
		}
		public void insertUpdate(DocumentEvent de) {
			this.dirty = true;
		}
		public void removeUpdate(DocumentEvent de) {
			this.dirty = true;
		}
		public void itemStateChanged(ItemEvent ie) {
			this.dirty = true;
		}
		
		private class ErrorTypeDataPanel extends JPanel implements DocumentListener, ItemListener {
			private ErrorTypeEditorPanel parent;
			JComboBox name;
			JTextField label = new JTextField();
			JTextField description = new JTextField();
			JComboBox defaultSeverity;
			ErrorTypeDataPanel(ErrorTypeEditorPanel parent, ErrorType errorType, String defaultSeverity) {
				super(new GridLayout(0, 1), true);
				this.parent = parent;
				
				this.name = new JComboBox(ErrorCheckListEditorPanel.this.errorMetadata.getErrorTypes(String.valueOf(catName.getSelectedItem())));
				this.name.setEditable(true);
				this.name.setSelectedItem(errorType.name);
				((JTextComponent) this.name.getEditor().getEditorComponent()).getDocument().addDocumentListener(ErrorCheckListEditorPanel.this);
				((JTextComponent) this.name.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
				this.name.addItemListener(ErrorCheckListEditorPanel.this);
				this.name.addItemListener(this);
				this.name.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent ie) {
						String cat = getCategoryName();
						if (cat == null)
							return;
						String type = getTypeName();
						if (type == null)
							return;
						String lab = ErrorCheckListEditorPanel.this.errorMetadata.getErrorTypeLabel(cat, type);
						if ((lab != null) && (label.getDocument().getLength() == 0))
							label.setText(lab);
						String desc = ErrorCheckListEditorPanel.this.errorMetadata.getErrorTypeDescription(cat, type);
						if ((desc != null) && (description.getDocument().getLength() == 0))
							description.setText(desc);
					}
				});
				
				this.label.setText(errorType.label);
				this.label.getDocument().addDocumentListener(ErrorCheckListEditorPanel.this);
				
				this.defaultSeverity = new JComboBox(errorSeverities);
				this.defaultSeverity.setEditable(false);
				this.defaultSeverity.setSelectedItem(defaultSeverity);
				this.defaultSeverity.addItemListener(ErrorCheckListEditorPanel.this);
				
				this.description.setText(errorType.description);
				this.description.getDocument().addDocumentListener(ErrorCheckListEditorPanel.this);
				
				JPanel topPanel = new JPanel(new GridLayout(1, 0), true);
				topPanel.add(new LabeledInputFieldPanel("Name", "Technical name for the error type, for sorting and grouping errors in protocols", this.name));
				topPanel.add(new LabeledInputFieldPanel("Label", "UI nice name for the error type, used in error protocols", this.label));
				topPanel.add(new LabeledInputFieldPanel("Default Severity", "Default severity for the individual error checks in this type", this.defaultSeverity));
				this.add(topPanel);
				this.add(new LabeledInputFieldPanel("Description", "Verbal description of the error type, used in error protocols", this.description));
			}
			
			String getTypeName() {
				Object name = this.name.getSelectedItem();
				return ((name == null) ? null : name.toString());
			}
			String getDefaultSeverity() {
				Object ds = this.defaultSeverity.getSelectedItem();
				return ((ds == null) ? DocumentError.SEVERITY_CRITICAL : ds.toString());
			}
			ErrorType getContent() {
				String name = this.getTypeName();
				if ((name == null) || (name.trim().length() == 0))
					return null;
				else return new ErrorType(name.toString(), label.getText(), description.getText());
			}
			
			public void changedUpdate(DocumentEvent de) {
				//	attribute changes are not of interest for now
			}
			public void insertUpdate(DocumentEvent de) {
				if (this.parent != null)
					this.parent.updateTypeTab();
			}
			public void removeUpdate(DocumentEvent de) {
				if (this.parent != null)
					this.parent.updateTypeTab();
			}
			public void itemStateChanged(ItemEvent ie) {
				if (this.parent != null)
					this.parent.updateTypeTab();
			}
		}
		private class ErrorTypeEditorPanel extends JPanel {
			ErrorTypeDataPanel dataPanel;
			VectorListModel checkListModel = new VectorListModel();
			JList checkList = new JList(this.checkListModel);
			JPanel checkPanel = new JPanel(new BorderLayout(), true);
			ErrorTypeEditorPanel(ErrorType errorType, String defaultSeverity) {
				super(new BorderLayout(), true);
				
				//	compute default severity from checks if null
				if (defaultSeverity == null) {
					CountingSet severities = new CountingSet(new TreeMap());
					for (int c = 0; c < errorType.checks.size(); c++) {
						ErrorCheck ec = ((ErrorCheck) errorType.checks.get(c));
						if (ec.severity != null)
							severities.add(ec.severity);
					}
					defaultSeverity = ((String) severities.max());
					if (defaultSeverity == null)
						defaultSeverity = DocumentError.SEVERITY_CRITICAL;
				}
				
				//	create data panel
				this.dataPanel = new ErrorTypeDataPanel(this, errorType, defaultSeverity);
				
				//	populate check list
				for (int c = 0; c < errorType.checks.size(); c++) {
					ErrorCheck ec = ((ErrorCheck) errorType.checks.get(c));
					this.checkListModel.addElement(new ErrorCheckEditorPanel(this, ec));
				}
				if (this.checkListModel.getSize() != 0)
					this.checkList.setSelectedIndex(0);
				
				//	create buttons
				JButton upCheckButton = new JButton("Up");
				upCheckButton.setBorder(BorderFactory.createRaisedBevelBorder());
				upCheckButton.setPreferredSize(new Dimension(50, 21));
				upCheckButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						moveErrorCheck(-1);
					}
				});
				
				JButton addCheckButton = new JButton("Add");
				addCheckButton.setBorder(BorderFactory.createRaisedBevelBorder());
				addCheckButton.setPreferredSize(new Dimension(50, 21));
				addCheckButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						addErrorCheck();
					}
				});
				
				JButton cloneCheckButton = new JButton("Clone");
				cloneCheckButton.setBorder(BorderFactory.createRaisedBevelBorder());
				cloneCheckButton.setPreferredSize(new Dimension(50, 21));
				cloneCheckButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						cloneErrorCheck();
					}
				});
				
				JButton removeCheckButton = new JButton("Remove");
				removeCheckButton.setBorder(BorderFactory.createRaisedBevelBorder());
				removeCheckButton.setPreferredSize(new Dimension(50, 21));
				removeCheckButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						removeErrorCheck();
					}
				});
				
				JButton downCheckButton = new JButton("Down");
				downCheckButton.setBorder(BorderFactory.createRaisedBevelBorder());
				downCheckButton.setPreferredSize(new Dimension(50, 21));
				downCheckButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						moveErrorCheck(1);
					}
				});
				
				JPanel checkListButtonPanel = new JPanel(new GridLayout(0, 1), true);
				checkListButtonPanel.add(upCheckButton);
				checkListButtonPanel.add(addCheckButton);
				checkListButtonPanel.add(cloneCheckButton);
				checkListButtonPanel.add(removeCheckButton);
				checkListButtonPanel.add(downCheckButton);
				
				//	add error check list (top/bottom split pane)
				this.checkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				this.checkList.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent lse) {
						int si = checkList.getSelectedIndex();
						System.out.println("Selected check at " + si);
						if (si < 0)
							return;
						if (checkListModel.getSize() <= si)
							return;
						ErrorCheckEditorPanel ecep = ((ErrorCheckEditorPanel) checkList.getSelectedValue());
						checkPanel.removeAll();
						checkPanel.add(ecep, BorderLayout.CENTER);
						checkPanel.validate();
						checkPanel.repaint();
					}
				});
				JScrollPane checkListBox = new JScrollPane(this.checkList);
				checkListBox.getVerticalScrollBar().setUnitIncrement(50);
				checkListBox.getVerticalScrollBar().setBlockIncrement(50);
				checkListBox.getHorizontalScrollBar().setUnitIncrement(50);
				checkListBox.getHorizontalScrollBar().setBlockIncrement(50);
				JPanel checkListPanel = new JPanel(new BorderLayout(), true);
				checkListPanel.add(checkListButtonPanel, BorderLayout.WEST);
				checkListPanel.add(checkListBox, BorderLayout.CENTER);
				
				JSplitPane checkSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, checkListPanel, this.checkPanel);
				if (checkListModel.size() != 0) {
					this.checkList.setSelectedIndex(0);
					ErrorCheckEditorPanel ecep = ((ErrorCheckEditorPanel) checkList.getSelectedValue());
					checkPanel.removeAll();
					checkPanel.add(ecep, BorderLayout.CENTER);
				}
				
				//	add explanatory spacer ta bottom of data panel
				this.dataPanel.add(new JLabel("Error Checks in this Type (manipulate on left of list, select to edit below)", JLabel.CENTER));
				
				//	put the whole stuff together
				this.add(this.dataPanel, BorderLayout.NORTH);
				this.add(checkSplit, BorderLayout.CENTER);
			}
			
			ErrorType getContent() {
				ErrorType et = this.dataPanel.getContent();
				if (et == null)
					return et;
				for (int c = 0; c < this.checkListModel.getSize(); c++) {
					ErrorCheckEditorPanel ecep = ((ErrorCheckEditorPanel) checkListModel.elementAt(c));
					ErrorCheck ec = ecep.getContent();
					if (ec != null)
						et.checks.add(ec);
				}
				return et;
			}
			
			void moveErrorCheck(int by) {
				int from = checkList.getSelectedIndex();
				if (from < 0)
					return;
				if (checkListModel.getSize() <= from)
					return;
				int to = (from + by);
				if (to < 0)
					return;
				if (checkListModel.getSize() <= to)
					return;
				ErrorCheckEditorPanel fEcep = ((ErrorCheckEditorPanel) checkListModel.elementAt(from));
				ErrorCheckEditorPanel tEcep = ((ErrorCheckEditorPanel) checkListModel.elementAt(to));
				this.checkListModel.setElementAt(fEcep, to);
				this.checkListModel.setElementAt(tEcep, from);
				this.checkListModel.fireContentsChanged();
				this.checkList.setSelectedIndex(to);
				dirty = true;
			}
			void addErrorCheck() {
				this.addErrorCheck(null);
			}
			void cloneErrorCheck() {
				this.addErrorCheck((ErrorCheckEditorPanel) this.checkList.getSelectedValue());
			}
			void addErrorCheck(ErrorCheckEditorPanel model) {
				ErrorCheck modelCheck = ((model == null) ? new ErrorCheck(ERROR_CHECK_LEVEL_STREAM, this.dataPanel.getDefaultSeverity(), "", "") : model.getContent());
				ErrorCheckDataPanel dataPanel = new ErrorCheckDataPanel(null, modelCheck);
				while (true) {
					int choice = DialogFactory.confirm(dataPanel, ((model == null) ? "Add Error Check" : "Clone Error Check"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
					if (choice != JOptionPane.OK_OPTION)
						return;
					modelCheck = dataPanel.getContent();
					if (modelCheck == null)
						DialogFactory.alert("The error description must not be empty.", "Invalid Error Description", JOptionPane.ERROR_MESSAGE);
					else break;
				}
				ErrorCheckEditorPanel checkPanel = new ErrorCheckEditorPanel(this, modelCheck);
				this.checkListModel.addElement(checkPanel);
				this.checkListModel.fireContentsChanged();
				this.checkList.setSelectedIndex(this.checkListModel.getSize()-1);
				dirty = true;
			}
			void removeErrorCheck() {
				int si = this.checkList.getSelectedIndex();
				if (si < 0)
					return;
				if (this.checkListModel.getSize() <= si)
					return;
				this.checkListModel.remove(si);
				this.checkListModel.fireContentsChanged();
				if ((this.checkListModel.size() == si) && (si != 0))
					this.checkList.setSelectedIndex(this.checkListModel.getSize()-1);
				dirty = true;
			}
			void updateTypeTab() {
				int index = typeTabs.indexOfComponent(this);
				if (index == -1)
					return;
				String type = this.dataPanel.getTypeName();
				if (type == null)
					return;
				typeTabs.setTitleAt(index, type);
			}
		}
		
		private class ErrorCheckDataPanel extends JPanel implements DocumentListener, ItemListener {
			private ErrorTypeEditorPanel parent;
			JComboBox level = new JComboBox(errorCheckLevels);
			JComboBox severity = new JComboBox(errorSeverities);
			JTextField description = new JTextField();
			ErrorCheckDataPanel(ErrorTypeEditorPanel parent, ErrorCheck errorCheck) {
				super(new GridLayout(0, 1), true);
				this.parent = parent;
				
				this.level.setEditable(false);
				this.level.setSelectedItem(errorCheck.level);
				this.level.addItemListener(ErrorCheckListEditorPanel.this);
				this.level.addItemListener(this);
				this.severity.setEditable(false);
				this.severity.setSelectedItem(errorCheck.severity);
				this.severity.addItemListener(ErrorCheckListEditorPanel.this);
				this.severity.addItemListener(this);
				
				this.description.setText(errorCheck.description);
				this.description.getDocument().addDocumentListener(ErrorCheckListEditorPanel.this);
				this.description.getDocument().addDocumentListener(this);
				
				//	add description test button
				JButton testButton = new JButton("Test");
				testButton.setBorder(BorderFactory.createRaisedBevelBorder());
				testButton.setPreferredSize(new Dimension(50, 21));
				testButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						testDescription();
					}
				});
				
				//	put the whole stuff together
				JPanel topPanel = new JPanel(new GridLayout(1, 0), true);
				topPanel.add(new LabeledInputFieldPanel("Normalization Level", "The normalization level to use for the error check", this.level));
				topPanel.add(new LabeledInputFieldPanel("Error Severity", "The severity of an error found by this check, for sorting and filtering errors in protocols", this.severity));
				JPanel descriptionPanel = new LabeledInputFieldPanel("Description", "The description of an error found by this check, used as a template for the error message (use '$value' to reference the textual content of the error subject)", this.description);
				descriptionPanel.add(testButton, BorderLayout.EAST);
				this.add(topPanel);
				this.add(descriptionPanel);
			}
			
			String getLevel() {
				Object level = this.level.getSelectedItem();
				return ((level == null) ? ERROR_CHECK_LEVEL_STREAM : level.toString());
			}
			String getSeverity() {
				Object severity = this.severity.getSelectedItem();
				return ((severity == null) ? DocumentError.SEVERITY_CRITICAL : severity.toString());
			}
			ErrorCheck getContent() {
				String description = this.description.getText().trim();
				if (description.length() == 0)
					return null;
				return new ErrorCheck(this.getLevel(), this.getSeverity(), description, "");
			}
			
			void testDescription() {
				String description = this.description.getText().trim();
				if (description.length() == 0)
					return;
				String errorDescription = buildErrorDescription(description, "<Error Subject>");
				DialogFactory.alert((description + "\r\n==>\r\n" + errorDescription), "Test Error Description", JOptionPane.PLAIN_MESSAGE);
			}
			
			public void changedUpdate(DocumentEvent de) {
				//	attribute changes are not of interest for now
			}
			public void insertUpdate(DocumentEvent de) {
				if (this.parent != null)
					this.parent.checkListModel.fireContentsChanged();
			}
			public void removeUpdate(DocumentEvent de) {
				if (this.parent != null)
					this.parent.checkListModel.fireContentsChanged();
			}
			public void itemStateChanged(ItemEvent ie) {
				if (this.parent != null)
					this.parent.checkListModel.fireContentsChanged();
			}
			
			public String toString() {
				return (this.getLevel().substring(0, 2).toUpperCase() + " " + this.getSeverity().substring(0, 2).toUpperCase() + " - " + this.description.getText());
			}
		}
		private class ErrorCheckEditorPanel extends JPanel {
			ErrorCheckDataPanel dataPanel;
			GPathEditorPanel testPanel;
			ErrorCheckEditorPanel(ErrorTypeEditorPanel parent, ErrorCheck errorCheck) {
				super(new BorderLayout(), true);
				
				//	create data panel
				this.dataPanel = new ErrorCheckDataPanel(parent, errorCheck);
				
				//	add check test button
				JButton testButton = new JButton("Test");
				testButton.setBorder(BorderFactory.createRaisedBevelBorder());
				testButton.setPreferredSize(new Dimension(50, 21));
				testButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						testTest();
					}
				});
				JButton[] testButtons = {testButton};
				
				//	create GPath editor
				this.testPanel = new GPathEditorPanel(errorCheck.test, BorderLayout.NORTH, testButtons) {
					public void insertUpdate(DocumentEvent de) {
						super.insertUpdate(de);
						ErrorCheckListEditorPanel.this.insertUpdate(de);
					}
					public void removeUpdate(DocumentEvent de) {
						super.removeUpdate(de);
						ErrorCheckListEditorPanel.this.removeUpdate(de);
					}
				};
				
				//	put the whole stuff together
				this.add(this.dataPanel, BorderLayout.NORTH);
				this.add(this.testPanel, BorderLayout.CENTER);
			}
			
			void testTest() {
				boolean selected = true;
				String test = this.testPanel.getSelectedContent();
				if ((test == null) || (test.length() == 0)) {
					test = this.testPanel.getContent();
					selected = false;
				}
				String error = GPathParser.validatePath(test);
				if (error != null)
					DialogFactory.alert(("The " + (selected ? "selected expression part" : "expression") + " is not valid:\n" + error), "GPath Validation", JOptionPane.ERROR_MESSAGE);
				else try {
					Annotation[] annotations = testPath(test, this.dataPanel.getLevel());
					if (annotations != null) {
						AnnotationDisplayDialog add = new AnnotationDisplayDialog(dialog, "Matches of GPath", annotations, true);
						add.setLocationRelativeTo(this);
						add.setVisible(true);
					}
				}
				catch (GPathException gpe) {
					DialogFactory.alert(gpe, "GPath Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			private Annotation[] testPath(String gPath, String level) throws GPathException {
				QueriableAnnotation data = DocumentErrorCheckListManager.this.parent.getActiveDocument();
				return ((data == null) ? null : GPath.evaluatePath(data, gPath, null));
			}
			
			ErrorCheck getContent() {
				ErrorCheck ec = this.dataPanel.getContent();
				if (ec == null)
					return null;
				String test = this.testPanel.getContent();
				if (test.trim().length() == 0)
					return null;
				ec.test = test;
				return ec;
			}
			
			public String toString() {
				return this.dataPanel.toString();
			}
		}
	}
	
	private static class LabeledInputFieldPanel extends JPanel {
		LabeledInputFieldPanel(String label, String toolTip, JComponent input) {
			super(new BorderLayout(), true);
			JLabel l = new JLabel(label + ":");
			if (toolTip != null)
				l.setToolTipText(toolTip);
			this.add(l, BorderLayout.WEST);
			this.add(input, BorderLayout.CENTER);
		}
	}
	
	private static class VectorListModel extends DefaultListModel {
		VectorListModel() {}
		public void fireContentsChanged() {
			super.fireContentsChanged(this, 0, this.getSize());
		}
	}
	
	private static String buildErrorDescription(String description, String annotLabel) {
		return description.replaceAll("\\$value", ("'" + annotLabel + "'"));
	}
	
	private void printErrorCheckLists() {
		String[] eclNames = this.getResourceNames();
		for (int n = 0; n < eclNames.length; n++) {
			ErrorCheckList ecl = this.getErrorCheckList(eclNames[n]);
			System.out.println(eclNames[n] + "\t" + ecl.label + "\t" + ecl.description);
			System.out.println(" + " + ecl.category + "\t" + ecl.categoryLabel + "\t" + ecl.categoryDescription);
			ErrorType[] ets = ecl.getErrorTypes();
			for (int t = 0; t < ets.length; t++) {
				System.out.println(" |  + " + ets[t].name + "\t" + ets[t].label + "\t" + ets[t].description);
				ErrorCheck[] ecs = ets[t].getErrorChecks();
				for (int c = 0; c < ecs.length; c++)
					System.out.println(" |  |  + " + ecs[c].severity + "\t" + ecs[c].description + "\t" + GPath.normalizePath(ecs[c].test));
			}
		}
	}
}
