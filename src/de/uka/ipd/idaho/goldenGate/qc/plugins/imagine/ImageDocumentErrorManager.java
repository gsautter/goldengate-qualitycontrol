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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.Token;
import de.uka.ipd.idaho.gamta.TokenSequenceUtils;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol.DocumentError;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol.Recorder;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.gPath.GPath;
import de.uka.ipd.idaho.gamta.util.gPath.GPathParser;
import de.uka.ipd.idaho.gamta.util.gPath.exceptions.GPathException;
import de.uka.ipd.idaho.gamta.util.gPath.types.GPathObject;
import de.uka.ipd.idaho.gamta.util.imaging.BoundingBox;
import de.uka.ipd.idaho.gamta.util.imaging.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.imaging.DocumentStyle.ParameterGroupDescription;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.gamta.util.swing.DocumentErrorProtocolDisplay;
import de.uka.ipd.idaho.gamta.util.swing.ProgressMonitorWindow;
import de.uka.ipd.idaho.goldenGate.observers.ResourceObserver;
import de.uka.ipd.idaho.goldenGate.qc.imagine.GgImagineErrorManager;
import de.uka.ipd.idaho.goldenGate.qc.plugins.DocumentErrorCheckListManager;
import de.uka.ipd.idaho.goldenGate.qc.plugins.DocumentErrorCheckListManager.ErrorCheck;
import de.uka.ipd.idaho.goldenGate.qc.plugins.DocumentErrorCheckListManager.ErrorCheckList;
import de.uka.ipd.idaho.goldenGate.qc.plugins.DocumentErrorCheckListManager.ErrorType;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.ImDocument.ImDocumentListener;
import de.uka.ipd.idaho.im.ImLayoutObject;
import de.uka.ipd.idaho.im.ImObject;
import de.uka.ipd.idaho.im.ImPage;
import de.uka.ipd.idaho.im.ImRegion;
import de.uka.ipd.idaho.im.ImSupplement;
import de.uka.ipd.idaho.im.ImWord;
import de.uka.ipd.idaho.im.gamta.ImDocumentRoot;
import de.uka.ipd.idaho.im.imagine.plugins.AbstractGoldenGateImaginePlugin;
import de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener;
import de.uka.ipd.idaho.im.imagine.plugins.ImageMarkupToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol.ImDocumentError;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.AtomicActionListener;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.DisplayExtension;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.DisplayExtensionGraphics;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.SelectionAction;
import de.uka.ipd.idaho.im.util.ImUtils;

/**
 * Document error manager applies error check lists to documents to populate
 * respective error protocols, and persists the latter in a dedicated
 * supplement. Further, it provides the facilities for displaying error
 * protocols and for visualizing individual errors inside the document.
 * 
 * @author sautter
 */
public class ImageDocumentErrorManager extends AbstractGoldenGateImaginePlugin implements GgImagineErrorManager, Recorder, LiteratureConstants, ImageMarkupToolProvider, SelectionActionProvider, DisplayExtensionProvider, GoldenGateImagineDocumentListener {
	private static final String ERROR_CHECK_LEVEL_PAGE = "page";
	private static final String ERROR_CHECK_LEVEL_STREAM = "stream";
	
	private static final String ERROR_CHECK_DEFAULT_APPLY = "apply";
	private static final String ERROR_CHECK_DEFAULT_SKIP = "skip";
	
	private static class ErrorProtocolSupplement extends ImSupplement {
		DocErrorProtocol idep;
		ImSupplement source;
		byte[] bytes = null;
		ErrorProtocolSupplement(ImDocument doc, DocErrorProtocol idep, ImSupplement source) {
			super(doc, ImDocumentErrorProtocol.errorProtocolSupplementName, "errorProtocol", "text/plain");
			this.idep = idep;
			this.source = source;
		}
		public InputStream getInputStream() throws IOException {
			
			//	clear any cached input
			if (this.idep.isDirty()) {
				this.source = null;
				this.bytes = null;
			}
			
			//	we can use the original data if there are no modifications
			if (this.source != null)
				return this.source.getInputStream();
			
			//	generate output bytes on demand
			if (this.bytes == null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImDocumentErrorProtocol.storeErrorProtocol(this.idep, baos);
				this.bytes = baos.toByteArray();
			}
			return new ByteArrayInputStream(this.bytes);
		}
		
		private static final String modCountAttribute = "modCount";
		void notifyErrorProtocolModified(int oldModCount) {
			this.notifyAttributeChanged(modCountAttribute, ("" + oldModCount));
		}
		
		public Object getAttribute(String name) {
			if (modCountAttribute.equals(name))
				return ("" + this.idep.modCount);
			else return super.getAttribute(name);
		}
		public Object getAttribute(String name, Object def) {
			if (modCountAttribute.equals(name))
				return ("" + this.idep.modCount);
			else return super.getAttribute(name, def);
		}
		public boolean hasAttribute(String name) {
			if (modCountAttribute.equals(name))
				return true;
			else return super.hasAttribute(name);
		}
		public Object setAttribute(String name, Object value) {
			if (modCountAttribute.equals(name)) {
				if (value == null)
					return ("" + this.idep.modCount);
				else try {
					return ("" + this.idep.resetToModCount(Integer.parseInt(value.toString())));
				}
				catch (NumberFormatException nfe) {
					return ("" + this.idep.modCount);
				}
			}
			else return super.setAttribute(name, value);
		}
	}
	
	private static final String APPROVE_DOCUMENT_IMT_NAME = "ApproveDocument";
	private static final String SHOW_DOCUMENT_ERRORS_IMT_NAME = "ShowDocumentErrors";
	private static final String CHECK_DOCUMENT_ERRORS_IMT_NAME = "CheckDocumentErrors";
	private static final String CHECK_SHOW_DOCUMENT_ERRORS_IMT_NAME = "CheckShowDocumentErrors";
	
	private DocumentErrorProtocol errorMetadataKeeper = new DocumentErrorProtocol() {
		public int getErrorCount() {
			return 0;
		}
		public int getErrorSeverityCount(String severity) {
			return 0;
		}
		public DocumentError[] getErrors() {
			return new DocumentError[0];
		}
		public int getErrorCount(String category) {
			return 0;
		}
		public int getErrorSeverityCount(String category, String severity) {
			return 0;
		}
		public DocumentError[] getErrors(String category) {
			return new DocumentError[0];
		}
		public int getErrorCount(String category, String type) {
			return 0;
		}
		public int getErrorSeverityCount(String category, String type, String severity) {
			return 0;
		}
		public DocumentError[] getErrors(String category, String type) {
			return new DocumentError[0];
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
	
	private DocumentErrorCheckListManager errorCheckListProvider;
	
	private Map errorCheckListsByName = Collections.synchronizedMap(new HashMap());
	private Map errorChecksBySourceId = Collections.synchronizedMap(new HashMap());
	private Map errorCheckTargetsToRelated = Collections.synchronizedMap(new HashMap());
	private Set errorCheckTargets = Collections.synchronizedSet(this.errorCheckTargetsToRelated.keySet());
	private Set errorCheckTargetAttributes = Collections.synchronizedSet(new HashSet());
	private Map errorChecksByTargets = Collections.synchronizedMap(new HashMap());
	private ParameterGroupDescription errorCheckListParams;
	
	private ImDocumentErrorProtocolDisplay idepDisplay = null;
	private DisplayExtension[] idepDisplayExtension = null;
	
	private ImageMarkupTool approveDocument = new ImDocumentApprover();
	private ImageMarkupTool showErrors = new ImDocumentErrorShower();
	private ImageMarkupTool checkErrors = new ImDocumentErrorChecker();
	private ImageMarkupTool checkShowErrors = new ImDocumentErrorCheckerShower();
	
	/** zero-argument constructor for class loading */
	public ImageDocumentErrorManager() {}
	
	public String getPluginName() {
		return "IM Document Error Manager";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#init()
	 */
	public void init() {
		
		//	get error check list provider
		this.errorCheckListProvider = ((DocumentErrorCheckListManager) this.parent.getResourceProvider(DocumentErrorCheckListManager.class.getName()));
		
		//	add resource listener for error check lists
		this.parent.registerResourceObserver(new ResourceObserver() {
			public void resourcesChanged(String resourceProviderClassName) {
				if (!DocumentErrorCheckListManager.class.getName().equals(resourceProviderClassName))
					return;
				errorCheckListCache.clear(); // have to re-compile document error check lists after change
				ensureErrorCheckListsLoaded();
			}
			public void resourceUpdated(String resourceProviderClassName, String resourceName) {
				if (!DocumentErrorCheckListManager.class.getName().equals(resourceProviderClassName))
					return;
				ensureErrorCheckListLoaded(resourceName); // does all the cleanup right there
			}
			public void resourceDeleted(String resourceProviderClassName, String resourceName) {
				if (!DocumentErrorCheckListManager.class.getName().equals(resourceProviderClassName))
					return;
				errorCheckListsByName.remove(resourceName);
				errorCheckListCache.clear(); // have to re-compile document error check lists after change
			}
		});
		
		//	register as error protocol recorder
		DocumentErrorProtocol.addRecorder(this);
		
		//	add parameter group description (gets populated on loading)
		this.errorCheckListParams = new ParameterGroupDescription("errorChecks");
		this.errorCheckListParams.setLabel("Document Error Checking");
		this.errorCheckListParams.setDescription("This parameter group controls how automated error checking handles documents following this style. In particular, the individual parameters switch individual error check lists on or off.");
		DocumentStyle.addParameterGroupDescription(this.errorCheckListParams);
		
		//	add selector for minimum error severity requiring approval
		this.errorCheckListParams.setParamLabel("minApprovalErrorSeverity", "Minimum Error Severity Requiring Approval");
		this.errorCheckListParams.setParamDescription("minApprovalErrorSeverity", "The minimum severity of an error that requires approval from a user");
		this.errorCheckListParams.setParamValues("minApprovalErrorSeverity", errorSeverities);
		this.errorCheckListParams.setParamValueLabel("minApprovalErrorSeverity", DocumentError.SEVERITY_BLOCKER, "Blocker");
		this.errorCheckListParams.setParamValueLabel("minApprovalErrorSeverity", DocumentError.SEVERITY_CRITICAL, "Critical");
		this.errorCheckListParams.setParamValueLabel("minApprovalErrorSeverity", DocumentError.SEVERITY_MAJOR, "Major");
		this.errorCheckListParams.setParamValueLabel("minApprovalErrorSeverity", DocumentError.SEVERITY_MINOR, "Minor");
		
		//	load persisted error categories and types
		if (this.dataProvider.isDataAvailable("errorMetadata.txt")) try {
			DocumentErrorProtocol.fillErrorProtocol(this.errorMetadataKeeper, null, this.dataProvider.getInputStream("errorMetadata.txt"));
		}
		catch (IOException ioe) {
			System.out.println("Could not load error metadata: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		
		//	load error check list
		this.ensureErrorCheckListsLoaded();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#exit()
	 */
	public void exit() {
		
		//	un-register as error protocol recorder
		DocumentErrorProtocol.removeRecorder(this);
		
		//	persist error categories and types
		if (this.dataProvider.isDataEditable("errorMetadata.txt")) try {
			DocumentErrorProtocol.storeErrorProtocol(this.errorMetadataKeeper, this.dataProvider.getOutputStream("errorMetadata.txt"));
		}
		catch (IOException ioe) {
			System.out.println("Could not store error metadata: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.qc.ErrorManager#getErrorMetadata()
	 */
	public DocumentErrorProtocol getErrorMetadata() {
		return this.errorMetadataKeeper;
	}
	
	private static final String[] errorSeverities = {
		DocumentError.SEVERITY_BLOCKER,
		DocumentError.SEVERITY_CRITICAL,
		DocumentError.SEVERITY_MAJOR,
		DocumentError.SEVERITY_MINOR,
	};
	
	private static final String[] errorCheckDefaults = {
		ERROR_CHECK_DEFAULT_APPLY,
		ERROR_CHECK_DEFAULT_SKIP
	};
	
	public String[] getEditMenuItemNames() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageMarkupToolProvider#getToolsMenuItemNames()
	 */
	public String[] getToolsMenuItemNames() {
		String[] tmins = {
			SHOW_DOCUMENT_ERRORS_IMT_NAME,
			CHECK_DOCUMENT_ERRORS_IMT_NAME,
			CHECK_SHOW_DOCUMENT_ERRORS_IMT_NAME,
			APPROVE_DOCUMENT_IMT_NAME,
		};
		return tmins;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageMarkupToolProvider#getImageMarkupTool(java.lang.String)
	 */
	public ImageMarkupTool getImageMarkupTool(String name) {
		if (SHOW_DOCUMENT_ERRORS_IMT_NAME.equals(name))
			return this.showErrors;
		else if (CHECK_DOCUMENT_ERRORS_IMT_NAME.equals(name))
			return this.checkErrors;
		else if (CHECK_SHOW_DOCUMENT_ERRORS_IMT_NAME.equals(name))
			return this.checkShowErrors;
		else if (APPROVE_DOCUMENT_IMT_NAME.equals(name))
			return this.approveDocument;
		else return null;
	}
	
	private class ImDocumentErrorShower implements ImageMarkupTool {
		public String getLabel() {
			return "Show Document Errors";
		}
		public String getTooltip() {
			return "Show the markup errors in the document";
		}
		public String getHelpText() {
			return null; // for now ...
		}
		public void process(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
			if (annot != null)
				return;
			showDocumentErrorProtocol(idmp, ((DocErrorProtocol) errorProtocolCache.get(doc.docId)), true, pm);
		}
	}
	
	private class ImDocumentErrorChecker implements ImageMarkupTool {
		public String getLabel() {
			return "Check Document Errors";
		}
		public String getTooltip() {
			return "Check the document for markup errors";
		}
		public String getHelpText() {
			return null; // for now ...
		}
		public void process(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
			if (annot != null)
				return;
			checkDocumentErrors(doc, pm);
		}
	}
	
	private class ImDocumentErrorCheckerShower implements ImageMarkupTool {
		public String getLabel() {
			return "Check & Show Document Errors";
		}
		public String getTooltip() {
			return "Check the document for markup errors, and display the protocol afterwards";
		}
		public String getHelpText() {
			return null; // for now ...
		}
		public void process(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
			if (annot != null)
				return;
			checkDocumentErrors(doc, pm);
			showDocumentErrorProtocol(idmp, ((DocErrorProtocol) errorProtocolCache.get(doc.docId)), true, pm);
		}
	}
	
	private class ImDocumentApprover implements ImageMarkupTool {
		public String getLabel() {
			return "Approve Document ...";
		}
		public String getTooltip() {
			return "Approve the document for further processing and export";
		}
		public String getHelpText() {
			return null; // for now ...
		}
		public void process(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
			if (annot != null)
				return;
			approveDocument(doc, idmp, pm);
		}
	}
	
	public void approveDocument(ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
		this.approveDocument(idmp.document, idmp, pm);
	}
	
	void approveDocument(ImDocument doc, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
		
		//	get error protocol
		DocErrorProtocol idep = this.getErrorProtocolFor(doc);
		
		//	run all pending re-checks so protocol is up to date for approval
		idep.runPendingReChecks(pm);
		
		//	no errors left, approve it all
		if ((idep != null) && (idep.getErrorCount() == 0)) {
			String[] dans = doc.getAttributeNames();
			for (int a = 0; a < dans.length; a++) {
				if (APPROVAL_REQUIRED_ATTRIBUTE_NAME.equals(dans[a]))
					doc.removeAttribute(dans[a]);
				else if (dans[a].startsWith(APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX))
					doc.removeAttribute(dans[a]);
			}
			doc.setAttribute(APPROVED_BY_ATTRIBUTE_NAME, this.getCurrentUserName());
			return;
		}
		
		//	update approval status otherwise
		else {
			
			//	clean up attributes
			String[] dans = doc.getAttributeNames();
			for (int a = 0; a < dans.length; a++) {
				if (dans[a].startsWith(APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX))
					doc.removeAttribute(dans[a]);
			}
			
			//	re-check existing categories, resetting the attributes
			this.updateApprovalStatus(idep, null, true);
		}
		
		//	offer revisiting previously approved document
		if (doc.hasAttribute(APPROVED_BY_ATTRIBUTE_NAME)) {
			int choice = DialogFactory.confirm(("This document has previously been approved by '" + doc.getAttribute(APPROVED_BY_ATTRIBUTE_NAME) + "'. Re-check?"), "Re-Check Document Approval", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (choice != JOptionPane.YES_OPTION)
				return;
			
			//	re-compute all error counts on revisit
			int eCount = getApprovalErrorCount(idep, null, idep.minApprovalErrorSeverity);
			if (eCount == 0)
				doc.removeAttribute(APPROVAL_REQUIRED_ATTRIBUTE_NAME);
			else {
				doc.setAttribute(APPROVAL_REQUIRED_ATTRIBUTE_NAME, ("" + eCount));
				doc.removeAttribute(APPROVED_BY_ATTRIBUTE_NAME);
			}
			String[] categories = idep.getErrorCategories();
			for (int c = 0; c < categories.length; c++) {
				int ecCount = getApprovalErrorCount(idep, categories[c], idep.minApprovalErrorSeverity);
				if (ecCount == 0)
					doc.removeAttribute(APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX + categories[c]);
				else doc.setAttribute((APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX + categories[c]), ("" + ecCount));
			}
		}
		
		//	assemble approval dialog
		JPanel approvalPanel = new JPanel(new GridLayout(0, 1), true);
		final JCheckBox approveAll = new JCheckBox(("<HTML><B>This document requires approval for " + doc.getAttribute(APPROVAL_REQUIRED_ATTRIBUTE_NAME, "0") + " errors (" + idep.getErrorCount() + " in total):</B></HTML>"), true);
		approvalPanel.add(approveAll);
		String[] categories = idep.getErrorCategories();
		ArrayList approveCategoryList = new ArrayList();
		for (int c = 0; c < categories.length; c++) {
			if (!doc.hasAttribute(APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX + categories[c]))
				continue;
			ErrorCategoryApprover approveCategory = new ErrorCategoryApprover((doc.getAttribute((APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX + categories[c]), "0") + " in category " + idep.getErrorCategoryLabel(categories[c]) + " (" + idep.getErrorCount(categories[c]) + " in total)"), categories[c]);
			approveCategory.setEnabled(false);
			approveCategoryList.add(approveCategory);
			approvalPanel.add(approveCategory);
		}
		
		//	disable / enable category checkboxes accordingly
		final ErrorCategoryApprover[] approveCategory = ((ErrorCategoryApprover[]) approveCategoryList.toArray(new ErrorCategoryApprover[approveCategoryList.size()]));
		approveAll.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				for (int c = 0; c < approveCategory.length; c++)
					approveCategory[c].setEnabled(!approveAll.isSelected());
				
			}
		});
		
		//	add "open protocol" checkbox if protocol not visible already
		JCheckBox showErrorProtocol = null;
		if (idepDisplay == null) {
			showErrorProtocol = new JCheckBox("Show error protocol on 'Cancel'?");
			approvalPanel.add(showErrorProtocol);
		}
		
		//	show dialog
		int choice = DialogFactory.confirm(approvalPanel, "Approve Document", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if ((choice == JOptionPane.CANCEL_OPTION) && (showErrorProtocol != null) && showErrorProtocol.isSelected())
			showDocumentErrorProtocol(idmp, idep, true, pm);
		if (choice != JOptionPane.OK_OPTION)
			return;
		
		//	mark approval by current user name
		doc.setAttribute(APPROVED_BY_ATTRIBUTE_NAME, this.getCurrentUserName());
		if (approveAll.isSelected()) {
			doc.removeAttribute(APPROVAL_REQUIRED_ATTRIBUTE_NAME);
			for (int c = 0; c < approveCategory.length; c++)
				doc.removeAttribute(APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX + approveCategory[c].category);
		}
		else {
			int approveCount = 0;
			for (int c = 0; c < approveCategory.length; c++)
				if (approveCategory[c].isSelected()) {
					doc.removeAttribute(APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX + approveCategory[c].category);
					approveCount++;
				}
			if (approveCount == approveCategory.length) // all categories approved individually
				doc.removeAttribute(APPROVAL_REQUIRED_ATTRIBUTE_NAME);
		}
		return;
	}
	
	private void updateApprovalStatus(DocErrorProtocol dep, String category, boolean checkAll) {
		if (dep.subject == null)
			return;
		
		//	if the document has been approved, we don't need to keep these counts
		if (dep.subject.hasAttribute(APPROVED_BY_ATTRIBUTE_NAME))
			return;
		
		//	must be grouped cleanup
		if (category == null) {
			String[] dans = dep.subject.getAttributeNames();
			for (int a = 0; a < dans.length; a++) {
				if (APPROVAL_REQUIRED_ATTRIBUTE_NAME.equals(dans[a]))
					dep.subject.removeAttribute(dans[a]);
				else if (dans[a].startsWith(APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX))
					dep.subject.removeAttribute(dans[a]);
			}
		}
		
		//	individual error changed
		else {
			int eCount = getApprovalErrorCount(dep, null, dep.minApprovalErrorSeverity);
			if (eCount == 0)
				dep.subject.removeAttribute(APPROVAL_REQUIRED_ATTRIBUTE_NAME);
			else dep.subject.setAttribute(APPROVAL_REQUIRED_ATTRIBUTE_NAME, ("" + eCount));
			int ecCount = getApprovalErrorCount(dep, category, dep.minApprovalErrorSeverity);
			if (ecCount == 0)
				dep.subject.removeAttribute(APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX + category);
			else dep.subject.setAttribute((APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX + category), ("" + ecCount));
		}
		
		//	re-check all categories if requested
		if (checkAll) {
			String[] categories = dep.getErrorCategories();
			for (int c = 0; c < categories.length; c++)
				this.updateApprovalStatus(dep, categories[c], false);
		}
	}
	
	private String getCurrentUserName() {
		try {
			QueriableAnnotation userNameDummy = Gamta.newDocument(Gamta.newTokenSequence("DUMMY", null));
			GPathObject ggUserName = GPath.evaluateExpression("(ggServerUserName())", userNameDummy, null);
			if ((ggUserName != null) && ggUserName.asBoolean().value)
				return ggUserName.asString().value;
		}
		catch (Throwable t) {
			System.out.println("Could not get user name via GPath: " + t.getMessage());
			t.printStackTrace(System.out);
		}
		return System.getProperty("user.name");
	}
	
	private static class ErrorCategoryApprover extends JPanel {
		final String category;
		private JCheckBox approve;
		ErrorCategoryApprover(String text, String category) {
			super(new BorderLayout(), true);
			this.category = category;
			this.approve = new JCheckBox("", true);
			this.add(this.approve, BorderLayout.WEST);
			this.add(new JLabel(text), BorderLayout.CENTER);
			this.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		private boolean wasSelected = true; // cache selection value in "approve all" mode
		public void setEnabled(boolean enabled) {
			if (enabled)
				this.approve.setSelected(this.wasSelected);
			else {
				this.wasSelected = this.approve.isSelected();
				this.approve.setSelected(true);
			}
			this.approve.setEnabled(enabled);
		}
		public boolean isSelected() {
			return this.approve.isSelected();
		}
	}
	
	private void ensureErrorCheckListsLoaded() {
		String[] eclNames = this.errorCheckListProvider.getResourceNames();
		for (int n = 0; n < eclNames.length; n++)
			this.ensureErrorCheckListLoaded(eclNames[n]);
	}
	
	private void ensureErrorCheckListLoaded(final String eclName) {
		ErrorCheckList ecl = this.errorCheckListProvider.getErrorCheckList(eclName);
		
		String spEclName = getStyleParamName(eclName);
		this.errorCheckListParams.setParamLabel(spEclName, ecl.label);
		this.errorCheckListParams.setParamDescription(spEclName, ecl.description);
		this.errorCheckListParams.setParamValues(spEclName, errorCheckDefaults);
		this.errorCheckListParams.setParamValueLabel(spEclName, ERROR_CHECK_DEFAULT_APPLY, "Apply");
		this.errorCheckListParams.setParamValueLabel(spEclName, ERROR_CHECK_DEFAULT_SKIP, "Skip");
		
		this.errorMetadataKeeper.addErrorCategory(ecl.category, ecl.categoryLabel, ecl.categoryDescription);
		
		ImDocumentErrorCheckList idecl = new ImDocumentErrorCheckList(eclName, ecl.label, ecl.description, ecl.applyByDefault);
		ErrorType[] ets = ecl.getErrorTypes();
		for (int t = 0; t < ets.length; t++) {
			this.errorMetadataKeeper.addErrorType(ecl.category, ets[t].name, ets[t].label, ets[t].description);
			
			ErrorCheck[] ecs = ets[t].getErrorChecks();
			for (int c = 0; c < ecs.length; c++) {
				try {
					ImDocumentErrorCheck idec = new ImDocumentErrorCheck(idecl, ecl.category, ets[t].name, ecs[c].level, ecs[c].severity, ecs[c].description, GPathParser.parsePath(ecs[c].getTest()));
					idecl.errorChecks.add(idec);
					this.indexErrorCheck(idec);
				}
				catch (GPathException gpe) {
					System.out.println("Error in check " + ecs[c].description + ": " + gpe.getMessage());
					System.out.println("  test is " + ecs[c].test);
				}
			}
		}
		
		this.errorCheckListsByName.put(eclName, idecl);
		this.errorCheckListCache.clear(); // we need to account for changes
	}
	
	private void indexErrorCheck(ImDocumentErrorCheck idec) {
		this.errorChecksBySourceId.put(idec.sourceId, idec);
		for (Iterator tit = idec.targets.iterator(); tit.hasNext();) {
			String target = ((String) tit.next());
			if (target.startsWith("@")) {
				this.errorCheckTargetAttributes.add(target.substring("@".length()));
				continue;
			}
			Set rTargets = ((Set) this.errorCheckTargetsToRelated.get(target));
			if (rTargets == null) {
				rTargets = new ErrorCheckTargetSet();
				this.errorCheckTargetsToRelated.put(target, rTargets);
			}
			rTargets.addAll(idec.targets);
			DocErrorCheckList idecl = ((DocErrorCheckList) this.errorChecksByTargets.get(target));
			if (idecl == null) {
				idecl = new DocErrorCheckList();
				this.errorChecksByTargets.put(target, idecl);
			}
			idecl.addErrorCheck(idec);
		}
	}
	
	ErrorCheckTargetSet getRelatedTargets(String target) {
		return ((ErrorCheckTargetSet) this.errorCheckTargetsToRelated.get(target));
	}
	
	//	whole document initial check
	void checkDocumentErrors(ImDocument doc, ProgressMonitor pm) {
		this.checkDocumentErrors(doc, null, this.getErrorCheckListFor(doc), null, null, null, null, null, pm, true);
	}
	
	//	re-check by error category and type
	void checkDocumentErrors(ImDocument doc, String category, String type, ProgressMonitor pm) {
		this.checkDocumentErrors(doc, null, this.getErrorCheckListFor(doc), category, type, null, null, null, pm, false);
	}
	
	//	re-check by subject
	void checkDocumentErrors(ImDocument doc, ImObject checkDoc, DocErrorCheckList idecl, Set subjectTypes, Set typeIndependentSubjectIds, Set subjectAttributes, ProgressMonitor pm) {
		this.checkDocumentErrors(doc, checkDoc, idecl, null, null, subjectTypes, typeIndependentSubjectIds, subjectAttributes, pm, false);
	}
	
	private void checkDocumentErrors(ImDocument doc, ImObject checkDoc, DocErrorCheckList idecl, String category, String type, Set subjectTypes, Set typeIndependentSubjectIds, Set subjectAttributes, ProgressMonitor pm, boolean isInitialCheck) {
		DocErrorProtocol idep = this.getErrorProtocolFor(doc, isInitialCheck);
		ImDocumentRoot pageCheckDoc = null;
		ImDocumentRoot streamCheckDoc = null;
		for (int c = 0; c < idecl.size(); c++) {
			ImDocumentErrorCheck idec = idecl.getErrorCheck(c);
			System.out.println(" - checking " + idec.category + "/" + idec.type + " " + idec.description);
			if ((category != null) && !category.equals(idec.category)) {
				System.out.println("   ==> wrong category, not " + category);
				continue;
			}
			if ((type != null) && !type.equals(idec.type)) {
				System.out.println("   ==> wrong type, not " + type);
				continue;
			}
			if ((subjectTypes != null) && !idec.targets.containsAny(subjectTypes)) {
				System.out.println("   ==> wrong subject types " + subjectTypes + ", none in " + idec.targets);
				continue;
			}
			if ((subjectAttributes != null) && !idec.targets.containsAny(subjectAttributes)) {
				System.out.println("   ==> wrong subject attributes " + subjectAttributes + ", none in " + idec.targets);
				continue;
			}
			if (pm != null) {
				pm.setStep(this.errorMetadataKeeper.getErrorCategoryLabel(idec.category));
				pm.setInfo(this.errorMetadataKeeper.getErrorTypeLabel(idec.category, idec.type));
				pm.setProgress((c * 100) / idecl.size());
			}
			try {
				if (ERROR_CHECK_LEVEL_PAGE.equals(idec.level)) {
					if (pageCheckDoc == null)
						pageCheckDoc = buildCheckDocument(doc, checkDoc, (ImDocumentRoot.NORMALIZATION_LEVEL_RAW | ImDocumentRoot.NORMALIZE_CHARACTERS | (idecl.targets.contains(ImWord.WORD_ANNOTATION_TYPE) ? ImDocumentRoot.SHOW_TOKENS_AS_WORD_ANNOTATIONS : 0)));
					idec.checkDocument(pageCheckDoc, subjectTypes, typeIndependentSubjectIds, idep);
				}
				else if (ERROR_CHECK_LEVEL_STREAM.equals(idec.level)) {
					if (streamCheckDoc == null)
						streamCheckDoc = buildCheckDocument(doc, checkDoc, (ImDocumentRoot.NORMALIZATION_LEVEL_STREAMS | ImDocumentRoot.NORMALIZE_CHARACTERS | (idecl.targets.contains(ImWord.WORD_ANNOTATION_TYPE) ? ImDocumentRoot.SHOW_TOKENS_AS_WORD_ANNOTATIONS : 0)));
					idec.checkDocument(streamCheckDoc, subjectTypes, typeIndependentSubjectIds, idep);
				}
			}
			catch (GPathException gpe) {
				System.out.println("Error evaluating " + idec.level + " level check: " + gpe.getMessage());
			}
		}
		if (isInitialCheck) {
			ErrorProtocolSupplement eps = new ErrorProtocolSupplement(doc, idep, null);
			doc.addSupplement(eps);
			idep.supplement = eps;
			this.updateApprovalStatus(idep, null, true); // set approval status for all categories after newly creating and populating error protocol
		}
	}
	private static ImDocumentRoot buildCheckDocument(ImDocument doc, ImObject checkDoc, int flags) {
		if (checkDoc instanceof ImAnnotation)
			return new ImDocumentRoot(((ImAnnotation) checkDoc), flags);
		else if (checkDoc instanceof ImRegion)
			return new ImDocumentRoot(((ImRegion) checkDoc), flags);
		else return new ImDocumentRoot(doc, flags);
	}
	
	private static class ImDocumentErrorCheckList {
		final String name;
		final boolean applyByDefault;
		final ArrayList errorChecks = new ArrayList();
		ImDocumentErrorCheckList(String name, String label, String description, boolean applyByDefault) {
			this.name = name;
			this.applyByDefault = applyByDefault;
		}
	}
	
	private static class ImDocumentErrorCheck {
		private static final Pattern targetPattern = Pattern.compile("[a-z\\-]+\\:\\:([a-zA-Z0-9][a-zA-Z0-9\\_\\-]*\\:)?[a-zA-Z0-9][a-zA-Z0-9\\_\\-]*");
		
		final String sourceId;
		final ErrorCheckTargetSet subjects = new ErrorCheckTargetSet();
		final boolean isReferentialCheck;
		final ErrorCheckTargetSet targets = new ErrorCheckTargetSet();
		
		final String category;
		final String type;
		final String level;
		final String severity;
		final String description;
		final GPath test;
		
		ImDocumentErrorCheck(ImDocumentErrorCheckList parent, String category, String type, String level, String severity, String description, GPath test) {
			this.category = category;
			this.type = type;
			this.level = level;
			this.severity = severity;
			this.description = description;
			this.test = test;
			if (this.test == null) {
				this.sourceId = (parent.name + "null");
				this.isReferentialCheck = false;
			}
			else {
				String testString = this.test.toString();
				this.sourceId = (parent.name + testString.hashCode());
				System.out.println("Analyzing GPath test " + testString);
				
				//	figure out if referential test
				int predicateStart = testString.indexOf("[");
				int lastRootPathStart = testString.lastIndexOf("descendant-or-self::annotation()/");
				this.isReferentialCheck = ((predicateStart != -1) && (predicateStart < lastRootPathStart)); // path from document down has to be in predicate
				System.out.println(" - referential test is " + this.isReferentialCheck);
				
				//	extract target types and attributes
				for (Matcher targets = targetPattern.matcher(testString); targets.find();) {
					String target = targets.group();
					System.out.println(" - " + target);
					if (target.startsWith("attribute::")) {
						String attribute = target.substring(target.indexOf("::") + "::".length());
						if (Annotation.START_INDEX_ATTRIBUTE.equals(attribute))
							this.targets.add("@" + ImWord.FIRST_WORD_ATTRIBUTE);
						else if (Annotation.END_INDEX_ATTRIBUTE.equals(attribute))
							this.targets.add("@" + ImWord.LAST_WORD_ATTRIBUTE);
						else if (Annotation.SIZE_ATTRIBUTE.equals(attribute)) {
							this.targets.add("@" + ImWord.FIRST_WORD_ATTRIBUTE);
							this.targets.add("@" + ImWord.LAST_WORD_ATTRIBUTE);
						}
						else this.targets.add("@" + attribute);
					}
					else if (target.startsWith("token::")) {
						this.targets.add(ImWord.WORD_ANNOTATION_TYPE);
						this.targets.add("token");
						this.targets.add("@" + ImWord.STRING_ATTRIBUTE);
						this.targets.add("@" + ImWord.NEXT_RELATION_ATTRIBUTE);
						this.targets.add("@" + ImWord.PREVIOUS_RELATION_ATTRIBUTE);
						this.targets.add("@" + ImWord.NEXT_WORD_ATTRIBUTE);
						this.targets.add("@" + ImWord.PREVIOUS_WORD_ATTRIBUTE);
						this.targets.add("@" + ImWord.TEXT_STREAM_TYPE_ATTRIBUTE);
						this.targets.add("@" + ImWord.FONT_SIZE_ATTRIBUTE);
					}
					else if (target.endsWith("::annotation")) {}
					else {
						String targetType = target.substring(target.indexOf("::") + "::".length());
						this.targets.add(targetType);
					}
				}
				System.out.println(" ==> targets: " + this.targets);
				
				//	remove predicates
				int testLength;
				do {
					System.out.println(" - removing predicates: " + testString);
					testLength = testString.length();
					int sqIndex = testString.indexOf("'");
					int dqIndex = testString.indexOf("\"");
					if ((sqIndex == -1) && (dqIndex == -1)) {
						System.out.println("   - removing predicates");
						testString = testString.replaceAll("\\[[^\\[\\]]+\\]", "");
					}
					else if ((sqIndex == -1) || ((dqIndex != -1) && (dqIndex < sqIndex))) {
						System.out.println("   - removing double quoted strings");
						testString = testString.replaceAll("\\\"[^\\\"]+\\\"", "");
					}
					else if ((dqIndex == -1) || ((sqIndex != -1) && (sqIndex < dqIndex))) {
						System.out.println("   - removing single quoted strings");
						testString = testString.replaceAll("\\'[^\\']+\\'", "");
					}
				}
				while (testString.length() < testLength);
				
				//	extract subject types
				for (Matcher subjects = targetPattern.matcher(testString); subjects.find();) {
					String subject = subjects.group();
					System.out.println(" - " + subject);
					if (subject.startsWith("token::")) {
						this.subjects.add(ImWord.WORD_ANNOTATION_TYPE);
						this.subjects.add("token");
					}
					else if (subject.endsWith("::annotation")) {}
					else if (subject.startsWith("attribute::")) {}
					else {
						String subjectType = subject.substring(subject.indexOf("::") + "::".length());
						this.subjects.add(subjectType);
					}
				}
				System.out.println(" ==> subjects: " + this.subjects);
			}
		}
		void checkDocument(ImDocumentRoot doc, Set subjectTypes, Set typeIndependentSubjectIds, ImDocumentErrorProtocol idep) {
			QueriableAnnotation[] errors = GPath.evaluatePath(doc, this.test, null);
			for (int e = 0; e < errors.length; e++) {
				System.out.println("   - found " + errors[e].getType());
				if ((subjectTypes != null) && !subjectTypes.contains(errors[e].getType())) {
					System.out.println("   ==> wrong subject type, not in " + subjectTypes);
					continue;
				}
				if (typeIndependentSubjectIds == null) {}
				else {
					String eSubjectId = ImDocumentErrorProtocol.getTypeInternalErrorSubjectId(errors[e], doc);
					if (typeIndependentSubjectIds.contains(eSubjectId)) {}
					else if (!Token.TOKEN_ANNOTATION_TYPE.equals(errors[e].getType())) {
						System.out.println("   ==> wrong subject ID " + eSubjectId + ", not in " + typeIndependentSubjectIds);
						continue;
					}
					Token token = doc.tokenAt(errors[e].getAbsoluteStartIndex());
					ImWord firstWord = doc.firstWordOf(token);
					ImWord lastWord = doc.lastWordOf(token);
					boolean noWordIsSubject = true;
					for (ImWord imw = firstWord; imw != null; imw = imw.getNextWord()) {
						if (typeIndependentSubjectIds.contains(ImDocumentErrorProtocol.getTypeInternalErrorSubjectId(imw, doc))) {
							noWordIsSubject = false;
							break;
						}
						if (imw == lastWord)
							break;
					}
					if (noWordIsSubject) {
						System.out.println("   ==> wrong token subject ID range " + eSubjectId + ", no included word in " + typeIndependentSubjectIds);
						continue;
					}
				}
				idep.addError(this.sourceId, errors[e], doc, this.category, this.type, this.getErrorDescription(errors[e]), this.severity);
			}
		}
		private String getErrorDescription(Annotation annot) {
			return buildErrorDescription(this.description, this.buildAnnotationLabel(annot));
		}
		private String buildAnnotationLabel(Annotation annot) {
			if (annot.size() < 8)
				return TokenSequenceUtils.concatTokens(annot, false, true);
			StringBuffer value = new StringBuffer();
			value.append(TokenSequenceUtils.concatTokens(annot, 0, 3, false, true));
			value.append(" ... ");
			value.append(TokenSequenceUtils.concatTokens(annot, (annot.size() - 3), 3, false, true));
			return value.toString();
		}
	}
	private static String buildErrorDescription(String description, String annotLabel) {
		return description.replaceAll("\\$value", ("'" + annotLabel + "'"));
	}
	
	public DocumentErrorProtocolDisplay getErrorProtocolDisplay(ImDocumentMarkupPanel idmp, ImDocumentErrorProtocol idep, ProgressMonitor pm) {
		if (idep instanceof DocErrorProtocol)
			((DocErrorProtocol) idep).runPendingReChecks(pm);
		this.idepDisplay = new ImDocumentErrorProtocolDisplay(null, idmp, idep);
		this.idepDisplayExtension = new DisplayExtension[1];
		this.idepDisplayExtension[0] = this.idepDisplay;
		this.imagineParent.notifyDisplayExtensionsModified(idmp);
		return this.idepDisplay;
	}
	
	void showDocumentErrorProtocol(final ImDocumentMarkupPanel idmp, final DocErrorProtocol idep, boolean isImtCall, final ProgressMonitor pm) {
		
		//	do we have an error protocol?
		if (idep == null) {
			DialogFactory.alert("There is no error protocol for this document.\r\nRun 'Check Document Errors' first to create one.", "Error Protocol Unavailable", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//	catch re-opening the viewer (it's not null then)
		if (this.idepDisplay != null) {
			if (this.idepDisplay.dialog != null)
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						idepDisplay.dialog.setSize(600, 750);
						if (idepDisplay.dialog.isVisible())
							idepDisplay.dialog.toFront();
						else idepDisplay.dialog.setVisible(true);
					}
				});
			return;
		}
		
		//	run all pending re-checks so protocol is up to date then display opens
		idep.runPendingReChecks(pm);
		
		/* when calling from an IMT, we need to make sure error protocol opens
		 * _after_ splash screen is closed (we want that dialog owned by main
		 * window, not any splash screen) */
		if (isImtCall) {
			Window topWindow = ((pm instanceof ProgressMonitorWindow) ? ((ProgressMonitorWindow) pm).getWindow() : DialogPanel.getTopWindow());
			if (topWindow == null)
				this.showDocumentErrorProtocol(idmp, idep, false, pm);
			else topWindow.addWindowListener(new WindowAdapter() {
				public void windowOpened(WindowEvent we) {
					System.out.println("Top window opened");
				}
				public void windowClosing(WindowEvent we) {
					System.out.println("Top window closing");
					showDocumentErrorProtocol(idmp, idep, false, pm);
				}
				public void windowClosed(WindowEvent we) {
					System.out.println("Top window closed");
					showDocumentErrorProtocol(idmp, idep, false, pm);
				}
			});
		}
		
		/* make sure to show error protocol after splash screen closing is
		 * fully processed (needs to be created from Swing's event dispatcher
		 * _after_ any splash screen closes) */
		else SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				//	update visualization facilities
				ImDocumentErrorProtocolViewer idepViewer = new ImDocumentErrorProtocolViewer(idmp, idep);
				idepDisplay = idepViewer.errorPanel;
				idepDisplayExtension = new DisplayExtension[1];
				idepDisplayExtension[0] = idepDisplay;
				imagineParent.notifyDisplayExtensionsModified(idmp);
				
				//	show the whole thing
				idepViewer.setSize(idepViewerSize);
				if (idepViewerPos == null)
					idepViewer.setLocationRelativeTo(DialogPanel.getTopWindow());
				else idepViewer.setLocation(idepViewerPos);
				idepViewer.setVisible(true);
			}
		});
	}
	
	private Dimension idepViewerSize = new Dimension(600, 750);
	private Point idepViewerPos = null;
	private class ImDocumentErrorProtocolViewer extends DialogPanel {
		private ImDocumentErrorProtocolDisplay errorPanel;
		ImDocumentErrorProtocolViewer(ImDocumentMarkupPanel idmp, final ImDocumentErrorProtocol idep) {
			super("(Potential) Errors in Document", false);
			
			this.errorPanel = new ImDocumentErrorProtocolDisplay(this.getDialog(), idmp, idep);
			
			//	make sure we clean up after ourselves
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					errorPanel.dispose();
					idepDisplay = null; // make way on closing
					idepDisplayExtension = null;
					idepViewerSize = ImDocumentErrorProtocolViewer.this.getSize();
					idepViewerPos = ImDocumentErrorProtocolViewer.this.getLocation();
					imagineParent.notifyDisplayExtensionsModified(null);
					System.out.println("Error Display Closed");
				}
				public void windowOpened(WindowEvent we) {
					System.out.println("Error Display Opened");
				}
			});
			this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			//	assemble the whole stuff
			this.add(this.errorPanel, BorderLayout.CENTER);
		}
		
		public void dispose() {
			this.errorPanel.dispose();
			super.dispose();
		}
	}
	
	private static final Color defaultButtonBackgroundColor = (new JButton("Only For The Color")).getBackground(); // make sure we have the default button background color
	private class ImDocumentErrorProtocolDisplay extends DocumentErrorProtocolDisplay implements DisplayExtension {
		final JDialog dialog;
		
		final ImDocumentErrorProtocol errorProtocol;
		final ImDocumentMarkupPanel documentPanel;
		
		ImDocumentError currentError;
		
		private JSpinner errorHighlightMarginSpinner;
		private JSpinner errorOutlineThicknessSpinner;
		private JButton errorHighlightColorButton = new JButton("Change Color");
		private JCheckBox showErrorHighlights = new JCheckBox("Highlight Errors?", true);
		private JCheckBox autoSelectNextError = new JCheckBox("Auto-Select Next?", true);
		
		private JButton removeErrorButton = new JButton("<HTML>Remove Error</HTML>");
		private JButton falsePositiveButton = new JButton("<HTML>False Positive<BR>or Data Error</HTML>");
		private JButton removeErrorSubjectButton = new JButton("<HTML>Remove Subject</HTML>");
		private JButton editSubjectAttributesButton = new JButton("<HTML>Edit Attributes</HTML>");
		//	TODO add another button for custom configured likely actions
		//	TODO create such mapping from error source (TODO establish that) to actions in the first place
		//	TODO in the long haul, maybe even link errors to default actions (via source) and provide respective 'Correct' button
		
		//	TODO facilitate group (not range selection mass !!!) false positive removal of errors ...
		//	TODO ... in dedicated scope (e.g. text stream errors within a block) ...
		//	TODO ... and configure said scope in individual QC rules (no scope disables group removal)
		
		private JButton removeAllButton = new JButton("<HTML>Remove All</HTML>");
		private JButton reCheckButton = new JButton("<HTML>Re-Check</HTML>");
		private JButton reCheckAllButton = new JButton("<HTML>Re-Check All</HTML>");
		
		private boolean immediateUpdatesSuspended = false;
		private DocumentError lastSelectedError = null;
		
		ImDocumentErrorProtocolDisplay(JDialog dialog, ImDocumentMarkupPanel idmp, ImDocumentErrorProtocol idep) {
			super(null);
			this.dialog = dialog;
			
			//	make sure we can access UI
			this.errorProtocol = idep;
			this.documentPanel = idmp;
			
			//	enable error protocol to keep tabs on atomic actions
			if (this.errorProtocol instanceof DocErrorProtocol)
				((DocErrorProtocol) this.errorProtocol).setDocumentMarkupPanel(this.documentPanel);
			
			//	make error highlights customizable
			this.errorHighlightMarginSpinner = new JSpinner(new SpinnerNumberModel(errorHighlightMargin, 0, 10, 1));
			this.errorHighlightMarginSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					changeErrorHighlightMargin();
				}
			});
			this.errorOutlineThicknessSpinner = new JSpinner(new SpinnerNumberModel(((int) errorOutlineStroke.getLineWidth()), 0, 10, 1));
			this.errorOutlineThicknessSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					changeErrorOutlineThickness();
				}
			});
			this.errorHighlightColorButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(this.errorHighlightColorButton.getBackground(), 2), BorderFactory.createRaisedBevelBorder()));
			this.errorHighlightColorButton.setBackground(errorOutlineColor);
			this.errorHighlightColorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					changeErrorHighlightColor();
				}
			});
			
			//	make sure to notify document display when highlight changes
			this.showErrorHighlights.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					imagineParent.notifyDisplayExtensionsModified(documentPanel);
				}
			});
			this.autoSelectNextError.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					setAutoSelectNextError(autoSelectNextError.isSelected());
				}
			});
			
			//	add buttons with frequent resolution actions
			this.removeErrorButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.removeErrorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					removeCurrentError(false);
				}
			});
			this.falsePositiveButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.falsePositiveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					removeCurrentError(true);
				}
			});
			this.removeErrorSubjectButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.removeErrorSubjectButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					removeErrorSubject();
				}
			});
			this.editSubjectAttributesButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.editSubjectAttributesButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					editErrorSubjectAttributes();
				}
			});
			
			//	assemble display options
			JPanel highlightMarginPanel = new JPanel(new BorderLayout(), true);
			highlightMarginPanel.add(new JLabel(" Protrude by "), BorderLayout.WEST);
			highlightMarginPanel.add(this.errorHighlightMarginSpinner, BorderLayout.CENTER);
			highlightMarginPanel.setToolTipText("The frame around the subject of the selected error will be this many pixels away from the error subject porper");
			JPanel outlineThicknessPanel = new JPanel(new BorderLayout(), true);
			outlineThicknessPanel.add(new JLabel(" Frame Weight "), BorderLayout.WEST);
			outlineThicknessPanel.add(this.errorOutlineThicknessSpinner, BorderLayout.CENTER);
			outlineThicknessPanel.setToolTipText("The frame around the subject of the selected error will be this many pixels thick");
			JPanel optionPanel = new JPanel(new GridLayout(0, 5), true);
			this.errorHighlightColorButton.setToolTipText("Change the color of the frame around the selected error subject");
			optionPanel.add(this.errorHighlightColorButton);
			optionPanel.add(highlightMarginPanel);
			optionPanel.add(outlineThicknessPanel);
			this.showErrorHighlights.setToolTipText("Draw a frame around the subject of the selected error");
			optionPanel.add(this.showErrorHighlights);
			this.autoSelectNextError.setToolTipText("Automatically select next error when the selected error is resolved or otherwise removed?");
			optionPanel.add(this.autoSelectNextError);
			
			//	finish re-check and remove-all buttons
			this.reCheckAllButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(this.reCheckButton.getBackground(), 2), BorderFactory.createRaisedBevelBorder()));
			this.reCheckAllButton.setToolTipText("<HTML>Re-Check all errors</HTML>");
			this.reCheckAllButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					reCheckErrors(null, null);
				}
			});
			this.reCheckButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(this.reCheckButton.getBackground(), 2), BorderFactory.createRaisedBevelBorder()));
			this.reCheckButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					reCheckErrors(getErrorCategory(), getErrorType());
				}
			});
			this.removeAllButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(this.reCheckButton.getBackground(), 2), BorderFactory.createRaisedBevelBorder()));
			this.removeAllButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					removeErrors(getErrorCategory(), getErrorType());
				}
			});
			
			//	create button panel for bottom
			JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 2, 0), true);
			buttonPanel.add(this.reCheckAllButton);
			buttonPanel.add(this.reCheckButton);
			buttonPanel.add(this.removeAllButton);
			
			//	hide default buttons
			this.setResolveErrorButtonText(null);
			this.setFalsePositiveButtonText(null);
			
			//	make our custom buttons show
			JButton[] customButtons = {
				this.removeErrorButton,
				this.falsePositiveButton,
				this.removeErrorSubjectButton,
				this.editSubjectAttributesButton,
			};
			this.setCustomButtons(customButtons);
			
			//	assemble the whole stuff
			this.add(optionPanel, BorderLayout.NORTH);
			this.add(buttonPanel, BorderLayout.SOUTH);
			
			//	show content (only now that we're fully assembled)
			this.setErrorProtocol(idep);
		}
		
		void setImmediateUpdatesSuspended(boolean ius) {
			if (ius == this.immediateUpdatesSuspended)
				return;
			this.immediateUpdatesSuspended = ius;
			if (this.immediateUpdatesSuspended)
				return;
			this.showErrorSubject((ImDocumentError) this.lastSelectedError);
			this.lastSelectedError = null;
			this.adjustErrorSubjectActionButtons();
		}
		protected void errorCategorySelected(String category, int errorCount) {
			String type = null;
			if (category != null) {
				String[] types = this.errorProtocol.getErrorTypes(category);
				if (types.length == 1)
					type = types[0];
			}
			this.adjustGlobalButtons(category, type);
		}
		protected void errorTypeSelected(String type, String category, int errorCount) {
			this.adjustGlobalButtons(category, type);
		}
		protected void errorSelected(DocumentError error) {
			if (this.immediateUpdatesSuspended) {
				this.lastSelectedError = error;
				return;
			}
			this.adjustErrorSubjectActionButtons();
			this.showErrorSubject((ImDocumentError) error);
		}
		protected void errorRemoved(DocumentError error, boolean falsePositive) {
			if (falsePositive)
				this.errorProtocol.markFalsePositive((ImDocumentError) error);
			if (this.immediateUpdatesSuspended)
				return;
			this.adjustErrorSubjectActionButtons();
			this.showErrorSubject((ImDocumentError) null);
		}
		public void errorRemoved(DocumentError error) {
			super.errorRemoved(error);
			if (this.immediateUpdatesSuspended)
				return;
			this.adjustErrorSubjectActionButtons();
			if (error == currentError)
				this.showErrorSubject((ImDocumentError) null);
		}
		public void dispose() {
			if (this.errorProtocol instanceof DocErrorProtocol)
				((DocErrorProtocol) this.errorProtocol).setDocumentMarkupPanel(null);
			idepDisplay = null; // make way on closing
			idepDisplayExtension = null;
			imagineParent.notifyDisplayExtensionsModified(null);
			if ((this.dialog != null) && this.dialog.isVisible())
				this.dialog.dispose();
			System.out.println("Error Display Disposed");
		}
		
		void adjustErrorSubjectActionButtons() {
			Color color = defaultButtonBackgroundColor;
			if (this.currentError == null) {}
			else if ((this.currentError.subject instanceof ImWord) && this.documentPanel.documentBornDigital) {}
			else if (this.currentError.subject instanceof ImWord)
				color = this.documentPanel.getTextStreamTypeColor(((ImWord) this.currentError.subject).getTextStreamType());
			else if (this.currentError.subject instanceof ImAnnotation)
				color = this.documentPanel.getAnnotationColor(((ImAnnotation) this.currentError.subject).getType());
			else if (this.currentError.subject instanceof ImRegion)
				color = this.documentPanel.getAnnotationColor(((ImRegion) this.currentError.subject).getType());
			this.removeErrorButton.setBackground(color);
			this.falsePositiveButton.setBackground(color);
			this.removeErrorSubjectButton.setBackground(color);
			this.editSubjectAttributesButton.setBackground(color);
		}
		void removeCurrentError(boolean falsePos) {
			if (this.currentError == null) {}
			else try {
				this.documentPanel.beginAtomicAction(falsePos ? "Remove False Positive Error" : "Remove Error");
				ImDocumentError error = this.currentError;
				this.errorProtocol.removeError(error);
				if (falsePos)
					this.errorProtocol.markFalsePositive(error);
			}
			finally {
				this.documentPanel.endAtomicAction();
			}
		}
		void removeErrorSubject() {
			if (this.currentError == null) {}
			else if ((this.currentError.subject instanceof ImWord) && this.documentPanel.documentBornDigital) {}
			else try {
				this.documentPanel.beginAtomicAction("Remove Error Subject");
				if (this.currentError.subject instanceof ImWord) {
					ImWord subject = ((ImWord) this.currentError.subject);
					ImPage page = subject.getPage();
					if (page != null)
						page.removeWord(subject, true); // remove annotations as well, as this is only intended for hinky looking words in scanned documents
				}
				else if (this.currentError.subject instanceof ImAnnotation)
					this.documentPanel.document.removeAnnotation((ImAnnotation) this.currentError.subject);
				else if (this.currentError.subject instanceof ImRegion) {
					ImRegion subject = ((ImRegion) this.currentError.subject);
					ImPage page = subject.getPage();
					if (page != null)
						page.removeRegion(subject);
				}
			}
			finally {
				this.documentPanel.endAtomicAction();
			}
		}
		void editErrorSubjectAttributes() {
			if (this.currentError == null) {}
			else if (this.currentError.subject == null) {}
			else try {
				this.documentPanel.beginAtomicAction("Edit Error Subject Attributes");
				if (this.currentError.subject instanceof ImAnnotation) {
					ImAnnotation annot = ((ImAnnotation) this.currentError.subject);
					this.documentPanel.editAttributes(annot, annot.getType(), null); // editor panel gets value itself
				}
				else if (this.currentError.subject instanceof ImRegion) {
					ImRegion region = ((ImRegion) this.currentError.subject);
					this.documentPanel.editAttributes(region, region.getType(), null); // editor panel gets value itself
				}
			}
			finally {
				this.documentPanel.endAtomicAction();
			}
		}
		
		void adjustGlobalButtons(String category, String type) {
			if (this.reCheckButton != null) {
				if (category == null) {
					this.reCheckButton.setText("<HTML>Re-Check</HTML>");
					this.reCheckButton.setToolTipText("<HTML>Re-Check all errors</HTML>");
				}
				else if (type == null) {
					this.reCheckButton.setText("<HTML>Re-Check <I>" + this.errorProtocol.getErrorCategoryLabel(category) + "</I></HTML>");
					this.reCheckButton.setToolTipText("<HTML>Re-Check all errors of category <I>" + this.errorProtocol.getErrorCategoryLabel(category) + "</I></HTML>");
				}
				else {
					this.reCheckButton.setText("<HTML>Re-Check <I>" + this.errorProtocol.getErrorTypeLabel(category, type) + "</I></HTML>");
					this.reCheckButton.setToolTipText("<HTML>Re-Check all errors of category <I>" + this.errorProtocol.getErrorCategoryLabel(category) + "</I> and type <I>" + this.errorProtocol.getErrorTypeLabel(category, type) + "</I></HTML>");
				}
			}
			if (this.removeAllButton != null) {
				if (category == null) {
					this.removeAllButton.setText("<HTML>Remove All</HTML>");
					this.removeAllButton.setToolTipText("<HTML>Remove all errors</HTML>");
				}
				else if (type == null) {
					this.removeAllButton.setText("<HTML>Remove <I>" + this.errorProtocol.getErrorCategoryLabel(category) + "</I></HTML>");
					this.removeAllButton.setToolTipText("<HTML>Remove all errors of category <I>" + this.errorProtocol.getErrorCategoryLabel(category) + "</I></HTML>");
				}
				else {
					this.removeAllButton.setText("<HTML>Remove <I>" + this.errorProtocol.getErrorTypeLabel(category, type) + "</I></HTML>");
					this.removeAllButton.setToolTipText("<HTML>Remove all errors of category <I>" + this.errorProtocol.getErrorCategoryLabel(category) + "</I> and type <I>" + this.errorProtocol.getErrorTypeLabel(category, type) + "</I></HTML>");
				}
				this.removeAllButton.setEnabled(type != null);
			}
		}
		
		void reCheckErrors(final String category, final String type) {
			
			//	produce action label
			final String actionLabel;
			if (category == null)
				actionLabel = "Check Document Approval Status";
			else if (type == null)
				actionLabel = ("Re-Check Approval Status for '" + this.errorProtocol.getErrorCategoryLabel(category) + "'");
			else actionLabel = ("Re-Check Approval Status for '" + this.errorProtocol.getErrorCategoryLabel(category) + "' / '" + this.errorProtocol.getErrorTypeLabel(category, type) + "'");
			
			//	get progress monitor
			final ProgressMonitor pm = this.documentPanel.getProgressMonitor(("Running '" + actionLabel + "', Please Wait"), "", false, false);
			final ProgressMonitorWindow pmw = ((pm instanceof ProgressMonitorWindow) ? ((ProgressMonitorWindow) pm) : null);
			
			//	perform check in extra thread (seems to cause problems on Swing EDT)
			Thread rceThread = new Thread() {
				public void run() {
					try {
						
						//	wait for splash screen progress monitor to come up (we must not reach the dispose() line before the splash screen even comes up)
						while ((pmw != null) && !pmw.getWindow().isVisible()) try {
							Thread.sleep(10);
						} catch (InterruptedException ie) {}
						
						//	suspend immediate display updates
						setImmediateUpdatesSuspended(true);
						
						//	perform re-check as atomic action (might change approval status attributes)
						documentPanel.startAtomicAction(actionLabel, null, null, pm);
						errorProtocol.removeErrors(category, type);
						checkDocumentErrors(documentPanel.document, category, type, pm);
					}
					finally {
						
						//	finish atomic action
						documentPanel.finishAtomicAction(pm);
						
						//	dispose splash screen progress monitor
						if (pmw != null)
							pmw.close();
						
						//	reactivate immediate display updates and make changes show
						/* we need to do repainting on Swing EDT, as otherwise we
						 * might incur a deadlock between this thread and EDT on
						 * synchronized parts of UI or data structures */
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setImmediateUpdatesSuspended(false);
								setErrorType(category, type);
							}
						});
					}
				}
			};
			rceThread.start();
			
			//	open progress monitor (this waits)
			if (pmw != null)
				pmw.popUp(true);
		}
		
		void removeErrors(final String category, final String type) {
			try {
				
				//	suspend immediate display updates
				setImmediateUpdatesSuspended(true);
				
				//	perform removal as atomic action
				documentPanel.startAtomicAction(("Clear '" + this.errorProtocol.getErrorCategoryLabel(category) + "' / '" + this.errorProtocol.getErrorTypeLabel(category, type) + "' Errors"), null, null, ProgressMonitor.dummy);
				errorProtocol.removeErrors(category, type);
			}
			finally {
				
				//	finish atomic action
				documentPanel.finishAtomicAction(ProgressMonitor.dummy);
				
				//	reactivate immediate display updates and make changes show
				/* we need to do repainting on Swing EDT, as otherwise we
				 * might incur a deadlock between this thread and EDT on
				 * synchronized parts of UI or data structures */
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setImmediateUpdatesSuspended(false);
						setErrorType(category, type);
					}
				});
			}
		}
		
		void changeErrorHighlightColor() {
			Color color = JColorChooser.showDialog(this, "Change Color", errorOutlineColor);
			if (color == null)
				return;
			errorOutlineColor = color;
			this.errorHighlightColorButton.setBackground(errorOutlineColor);
			errorHighlightColor = new Color(errorOutlineColor.getRed(), errorOutlineColor.getGreen(), errorOutlineColor.getBlue(), 64);
			imagineParent.notifyDisplayExtensionsModified(documentPanel);
		}
		void changeErrorHighlightMargin() {
			errorHighlightMargin = ((Integer) this.errorHighlightMarginSpinner.getValue()).intValue();
			imagineParent.notifyDisplayExtensionsModified(documentPanel);
		}
		void changeErrorOutlineThickness() {
			int outlineThickness = ((Integer) this.errorOutlineThicknessSpinner.getValue()).intValue();
			errorOutlineStroke = new BasicStroke((outlineThickness < 1) ? Float.MIN_VALUE : outlineThickness);
			imagineParent.notifyDisplayExtensionsModified(documentPanel);
		}
		
		void showErrorSubject(ImDocumentError error) {
			if (this.documentPanel == null)
				return;
			this.currentError = error;
			if (this.currentError == null) {
				imagineParent.notifyDisplayExtensionsModified(this.documentPanel);
				return;
			}
			
			//	highlight subject passage
			if (error.subjectFirstWord != null) {
				if (error.subjectLastWord == null)
					this.documentPanel.setWordSelection(error.subjectFirstWord);
				else this.documentPanel.setWordSelection(error.subjectFirstWord, error.subjectLastWord);
			}
			
			//	update display extensions
			imagineParent.notifyDisplayExtensionsModified(this.documentPanel);
			
			//	show subject markup if applicable
			if ("annotation".equals(error.subjectClass))
				this.documentPanel.setAnnotationsPainted(error.subjectType, true);
			else if ("region".equals(error.subjectClass))
				this.documentPanel.setRegionsPainted(error.subjectType, true);
			
			//	get source error check and highlight all types involved
			ImDocumentErrorCheck idec = ((ImDocumentErrorCheck) errorChecksBySourceId.get(error.source));
			if (idec == null)
				return;
			for (Iterator stit = idec.subjects.iterator(); stit.hasNext();) {
				String subjectType = ((String) stit.next());
				if (ERROR_CHECK_LEVEL_PAGE.equals(idec.level))
					this.documentPanel.setRegionsPainted(subjectType, true);
				else if (ERROR_CHECK_LEVEL_STREAM.equals(idec.level))
					this.documentPanel.setAnnotationsPainted(subjectType, true);
			}
			for (Iterator ttit = idec.targets.iterator(); ttit.hasNext();) {
				String targetType = ((String) ttit.next());
				if (ERROR_CHECK_LEVEL_PAGE.equals(idec.level))
					this.documentPanel.setRegionsPainted(targetType, true);
				else if (ERROR_CHECK_LEVEL_STREAM.equals(idec.level))
					this.documentPanel.setAnnotationsPainted(targetType, true);
			}
		}

		public boolean isActive() {
			return ((idepDisplay != null) && (this.currentError != null) && this.showErrorHighlights.isSelected());
		}
		
		public DisplayExtensionGraphics[] getExtensionGraphics(ImPage page, ImDocumentMarkupPanel idmp) {
			if (page.pageId != this.currentError.subjectPageId)
				return NO_DISPLAY_EXTENSION_GRAPHICS;
			if (this.currentError.subject instanceof ImLayoutObject)
				return getErrorVisualizationGraphics(this, idmp, page, ((ImLayoutObject) this.currentError.subject).bounds);
			if (this.currentError.subject instanceof ImAnnotation) {
				ArrayList words = new ArrayList();
				for (ImWord imw = ((ImAnnotation) this.currentError.subject).getFirstWord(); imw != null; imw = imw.getNextWord()) {
					if (imw.pageId == page.pageId)
						words.add(imw);
					if (imw == ((ImAnnotation) this.currentError.subject).getLastWord())
						break;
				}
				return getErrorVisualizationGraphics(this, idmp, page, words);
			}
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider#getActions(de.uka.ipd.idaho.im.ImWord, de.uka.ipd.idaho.im.ImWord, de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel)
	 */
	public SelectionAction[] getActions(final ImWord start, final ImWord end, final ImDocumentMarkupPanel idmp) {
		
		//	we only work on individual text streams
		if (!start.getTextStreamId().equals(end.getTextStreamId()))
			return null;
		
		//	collect painted annotations spanning or overlapping whole selection
		ImAnnotation[] allOverlappingAnnots = idmp.document.getAnnotationsOverlapping(start, end);
		LinkedList spanningAnnotList = new LinkedList();
		for (int a = 0; a < allOverlappingAnnots.length; a++) {
			if (!idmp.areAnnotationsPainted(allOverlappingAnnots[a].getType()))
				continue;
			if (ImUtils.textStreamOrder.compare(start, allOverlappingAnnots[a].getFirstWord()) < 0)
				continue;
			if (ImUtils.textStreamOrder.compare(allOverlappingAnnots[a].getLastWord(), end) < 0)
				continue;
			spanningAnnotList.add(allOverlappingAnnots[a]);
		}
		final ImAnnotation[] spanningAnnots = ((ImAnnotation[]) spanningAnnotList.toArray(new ImAnnotation[spanningAnnotList.size()]));
		
		//	sort annotations to reflect nesting order
		Arrays.sort(spanningAnnots, annotationOrder);
		
		//	collect available actions
		LinkedList actions = new LinkedList();
		
		//	single word selection (offer editing word attributes above same for other annotations)
		if (start == end)
			actions.add(new SelectionAction("markErrorWord", "Mark Error on Word", ("Mark error on '" + start.getString() + "'.")) {
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					markError(idmp.document, start, idmp);
					return false;
				}
			});
		
		//	single annotation selected
		if (spanningAnnots.length == 1) {
			
			//	edit attributes of existing annotations
			actions.add(new SelectionAction("markErrorAnnot", ("Mark Error on " + spanningAnnots[0].getType()), ("Mark error on '" + spanningAnnots[0].getType() + "' annotation.")) {
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					markError(idmp.document, spanningAnnots[0], idmp);
					return false;
				}
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					JMenuItem mi = super.getMenuItem(invoker);
					Color annotTypeColor = idmp.getAnnotationColor(spanningAnnots[0].getType());
					if (annotTypeColor != null) {
						mi.setOpaque(true);
						mi.setBackground(annotTypeColor);
					}
					return mi;
				}
			});
		}
		
		//	multiple annotations selected
		else if ((spanningAnnots.length > 1) && (start == end)) {
			
			//	edit attributes of existing annotations
			actions.add(new SelectionAction("markErrorAnnot", "Mark Error on Annotation ...", "Mark an eror on selected annotations.") {
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					return false;
				}
				public JMenuItem getMenuItem(final ImDocumentMarkupPanel invoker) {
					JMenu pm = new JMenu("Mark Error on Annotation ...");
					JMenuItem mi;
					for (int a = 0; a < spanningAnnots.length; a++) {
						final ImAnnotation spanningAnnot = spanningAnnots[a];
						mi = new JMenuItem("- " + spanningAnnot.getType() + " '" + getAnnotationShortValue(spanningAnnot.getFirstWord(), spanningAnnot.getLastWord()) + "'");
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								markError(idmp.document, spanningAnnot, idmp);
							}
						});
						Color annotTypeColor = idmp.getAnnotationColor(spanningAnnot.getType());
						if (annotTypeColor != null) {
							mi.setOpaque(true);
							mi.setBackground(annotTypeColor);
						}
						pm.add(mi);
					}
					return pm;
				}
			});
		}
		
		//	finally ...
		return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
	}
	private static final Comparator annotationOrder = new Comparator() {
		public int compare(Object obj1, Object obj2) {
			ImAnnotation annot1 = ((ImAnnotation) obj1);
			ImAnnotation annot2 = ((ImAnnotation) obj2);
			int c = ImUtils.textStreamOrder.compare(annot1.getFirstWord(), annot2.getFirstWord());
			return ((c == 0) ? ImUtils.textStreamOrder.compare(annot2.getLastWord(), annot1.getLastWord()) : c);
		}
	};
	private static String getAnnotationShortValue(ImWord start, ImWord end) {
		if (start == end)
			return start.getString();
		else if (start.getNextWord() == end)
			return (start.getString() + (Gamta.insertSpace(start.getString(), end.getString()) ? " " : "") + end.getString());
		else return (start.getString() + " ... " + end.getString());
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider#getActions(java.awt.Point, java.awt.Point, de.uka.ipd.idaho.im.ImPage, de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel)
	 */
	public SelectionAction[] getActions(Point start, Point end, final ImPage page, final ImDocumentMarkupPanel idmp) {
		
		//	mark selection
		BoundingBox selectedBox = new BoundingBox(Math.min(start.x, end.x), Math.max(start.x, end.x), Math.min(start.y, end.y), Math.max(start.y, end.y));
		
		//	get selected and context regions
		ImRegion[] pageRegions = page.getRegions();
		LinkedList selectedRegionList = new LinkedList();
		LinkedList contextRegionList = new LinkedList();
		for (int r = 0; r < pageRegions.length; r++) {
			if (!idmp.areRegionsPainted(pageRegions[r].getType()))
				continue;
			if (pageRegions[r].bounds.includes(selectedBox, true))
				contextRegionList.add(pageRegions[r]);
			if (pageRegions[r].bounds.right < selectedBox.left)
				continue;
			if (selectedBox.right < pageRegions[r].bounds.left)
				continue;
			if (pageRegions[r].bounds.bottom < selectedBox.top)
				continue;
			if (selectedBox.bottom < pageRegions[r].bounds.top)
				continue;
			selectedRegionList.add(pageRegions[r]);
		}
		final ImRegion[] selectedRegions;
		if (selectedRegionList.isEmpty())
			selectedRegions = ((ImRegion[]) contextRegionList.toArray(new ImRegion[contextRegionList.size()]));
		else selectedRegions = ((ImRegion[]) selectedRegionList.toArray(new ImRegion[selectedRegionList.size()]));
		
		//	get selected words
		ImWord[] selectedWords = page.getWordsInside(selectedBox);
		
		//	collect actions
		LinkedList actions = new LinkedList();
		
		//	no words selected
		if (selectedWords.length == 0) {
			
			//	no region selected, either
			if (selectedRegions.length == 0) {
				actions.add(new SelectionAction("markErrorPage", ("Mark Error on Page"), ("Mark error on page.")) {
					public boolean performAction(ImDocumentMarkupPanel invoker) {
						markError(idmp.document, page, idmp);
						return false;
					}
				});
			}
		}
		
		//	single region selected
		if (selectedRegions.length == 1) {
			
			//	edit attributes of existing region
			actions.add(new SelectionAction("markErrorRegion", ("Mark Error on " + selectedRegions[0].getType()), ("Mark error on '" + selectedRegions[0].getType() + "' region.")) {
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					markError(idmp.document, selectedRegions[0], idmp);
					return false;
				}
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					JMenuItem mi = super.getMenuItem(invoker);
					Color regionTypeColor = idmp.getLayoutObjectColor(selectedRegions[0].getType());
					if (regionTypeColor != null) {
						mi.setOpaque(true);
						mi.setBackground(regionTypeColor);
					}
					return mi;
				}
			});
		}
		
		//	multiple regions selected
		else if (selectedRegions.length > 1) {
			
			//	edit region attributes
			actions.add(new SelectionAction("markErrorRegion", "Mark Error on Region ...", "Mark error on selected regions.") {
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					return false;
				}
				public JMenuItem getMenuItem(final ImDocumentMarkupPanel invoker) {
					JMenu pm = new JMenu("Mark Error on Region ...");
					JMenuItem mi;
					for (int t = 0; t < selectedRegions.length; t++) {
						final ImRegion selectedRegion = selectedRegions[t];
						mi = new JMenuItem("- " + selectedRegion.getType() + " at " + selectedRegion.bounds.toString());
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								markError(idmp.document, selectedRegion, idmp);
							}
						});
						Color regionTypeColor = idmp.getLayoutObjectColor(selectedRegion.getType());
						if (regionTypeColor != null) {
							mi.setOpaque(true);
							mi.setBackground(regionTypeColor);
						}
						pm.add(mi);
					}
					return pm;
				}
			});
		}
		
		//	finally ...
		return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
	}
	
	private void markError(ImDocument doc, ImObject subject, ImDocumentMarkupPanel idmp) {
		
		//	build error category selector
		VectorComboBoxModel categoryModel = new VectorComboBoxModel(new Vector());
		categoryModel.addElement("<>", "<Select Error Category>");
		String[] categories = this.errorMetadataKeeper.getErrorCategories();
		for (int c = 0; c < categories.length; c++)
			categoryModel.addElement(categories[c], this.errorMetadataKeeper.getErrorCategoryLabel(categories[c]));
		final JComboBox category = new JComboBox(categoryModel);
		category.setEditable(false);
		category.setSelectedIndex(0);
		
		//	build error type selector
		final VectorComboBoxModel typeModel = new VectorComboBoxModel(new Vector());
		typeModel.addElement("<>", "<Select Error Category First>");
		final JComboBox type = new JComboBox(typeModel);
		type.setEditable(false);
		type.setEnabled(false);
		type.setSelectedIndex(0);
		
		//	build error description field
		final JTextField description = new JTextField();
		description.setEditable(true);
		description.setEnabled(false);
		
		//	build error severity selector
		final VectorComboBoxModel severityModel = new VectorComboBoxModel(new Vector());
		severityModel.addElement(DocumentError.SEVERITY_BLOCKER, "Blocker");
		severityModel.addElement(DocumentError.SEVERITY_CRITICAL, "Critical");
		severityModel.addElement(DocumentError.SEVERITY_MAJOR, "Major");
		severityModel.addElement(DocumentError.SEVERITY_MINOR, "Minor");
		final JComboBox severity = new JComboBox(severityModel);
		severity.setEditable(false);
		severity.setEnabled(false);
		severity.setSelectedIndex(0);
		
		//	connect type to category
		category.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				String sCategory = ((ElementTray) category.getSelectedItem()).name;
				type.setSelectedIndex(0);
				typeModel.clear();
				if (sCategory.startsWith("<")) {
					typeModel.addElement("<>", "<Select Error Category First>");
					typeModel.fireContentsChanged();
					type.setEnabled(false);
				}
				else {
					typeModel.addElement("<>", "<Select Error Type>");
					String[] types = errorMetadataKeeper.getErrorTypes(sCategory);
					for (int t = 0; t < types.length; t++)
						typeModel.addElement(types[t], errorMetadataKeeper.getErrorTypeLabel(sCategory, types[t]));
					typeModel.fireContentsChanged();
					type.setEnabled(true);
				}
			}
		});
		
		//	connect description to type
		type.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				String sType = ((ElementTray) type.getSelectedItem()).name;
				description.setEnabled(!sType.startsWith("<"));
			}
		});
		
		//	assemble the whole stuff
		JPanel errorPanel = new JPanel(new GridLayout(0, 1));
		errorPanel.add(category);
		errorPanel.add(type);
		errorPanel.add(description);
		errorPanel.add(severity);
		
		//	prompt for input
		int choice = DialogFactory.confirm(errorPanel, "Enter Error Message", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION)
			return;
		
		//	collect error data
		String eCategory = ((ElementTray) category.getSelectedItem()).name;
		if (eCategory.startsWith("<"))
			return;
		String eType = ((ElementTray) type.getSelectedItem()).name;
		if (eType.startsWith("<"))
			return;
		String eDescription = description.getText();
		if (eDescription.trim().length() == 0)
			return;
		String eSeverity = ((ElementTray) severity.getSelectedItem()).name;
		if (eSeverity.trim().length() == 0)
			return;
		
		//	add error to protocol
		DocErrorProtocol dep = this.getErrorProtocolFor(doc);
		try {
			idmp.beginAtomicAction("Mark Error on '" + subject.getType() + "'");
			dep.addError(null, subject, doc, eCategory, eType, eDescription, eSeverity);
		}
		finally {
			idmp.endAtomicAction();
		}
		
		//	make visualization show
		this.imagineParent.notifyDisplayExtensionsModified(idmp);
	}
	private static class ElementTray {
		final String name;
		final String label;
		ElementTray(String name, String label) {
			this.name = name;
			this.label = label;
		}
		public String toString() {
			return this.label;
		}
	}
	private static class VectorComboBoxModel extends DefaultComboBoxModel {
		private Vector data;
		VectorComboBoxModel(Vector data) {
			super(data);
			this.data = data;
		}
		void addElement(String name, String label) {
			this.data.add(new ElementTray(name, label));
		}
		void clear() {
			this.data.clear();
		}
		public void fireContentsChanged() {
			super.fireContentsChanged(this, 0, this.data.size());
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionProvider#getDisplayExtensions()
	 */
	public DisplayExtension[] getDisplayExtensions() {
		return ((this.idepDisplay == null) ? NO_DISPLAY_EXTENSIONS : this.idepDisplayExtension);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentOpened(de.uka.ipd.idaho.im.ImDocument, java.lang.Object, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
	 */
	public void documentOpened(ImDocument doc, Object source, ProgressMonitor pm) {
		
		//	load and cache error protocol (if any)
		try {
			if (pm != null)
				pm.setInfo("Loading error protocol");
			DocErrorProtocol idep = this.readErrorProtocol(doc);
			if (idep != null) {
				this.errorProtocolCache.put(doc.docId, idep);
				if (pm != null)
					pm.setInfo("Error protocol loaded");
			}
		}
		catch (IOException ioe) {
			System.out.println("Could not load error protocol: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentSelected(de.uka.ipd.idaho.im.ImDocument)
	 */
	public void documentSelected(ImDocument doc) {
		if ((this.idepDisplay != null) && (this.idepDisplay.dialog != null) && ((this.idepDisplay.documentPanel == null) || !doc.docId.equals(this.idepDisplay.documentPanel.document.docId)))
			this.idepDisplay.dispose();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentSaving(de.uka.ipd.idaho.im.ImDocument, java.lang.Object, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
	 */
	public void documentSaving(ImDocument doc, Object dest, ProgressMonitor pm) {
		if (doc.getSupplement(ImDocumentErrorProtocol.errorProtocolSupplementName) != null)
			return; // we've equipped this one before
		DocErrorProtocol idep = ((DocErrorProtocol) this.errorProtocolCache.get(doc.docId));
		if (idep == null)
			return; // nothing to persist
		if (pm != null)
			pm.setInfo("Adding error protocol");
		idep.runPendingReChecks(pm); // make sure error protocol is up to date when saving
		doc.addSupplement(new ErrorProtocolSupplement(doc, idep, null));
		if (pm != null)
			pm.setInfo("Loading error added");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentSaved(de.uka.ipd.idaho.im.ImDocument, java.lang.Object, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
	 */
	public void documentSaved(ImDocument doc, Object dest, ProgressMonitor pm) {
		DocErrorProtocol idep = ((DocErrorProtocol) this.errorProtocolCache.get(doc.docId));
		if (idep != null)
			idep.setClean();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentClosed(java.lang.String)
	 */
	public void documentClosed(String docId) {
		this.errorProtocolCache.remove(docId);
		this.errorCheckListCache.remove(docId);
		if ((this.idepDisplay != null) && (this.idepDisplay.documentPanel != null) && docId.equals(this.idepDisplay.documentPanel.document.docId)) {
			this.idepDisplay.dispose();
			this.idepDisplay = null; // make way on closing
			this.idepDisplayExtension = null;
			this.imagineParent.notifyDisplayExtensionsModified(null);
			System.out.println("Error Display Closed with Document");
		}
	}
	
	private DocErrorProtocol readErrorProtocol(ImDocument doc) throws IOException {
		
		//	get persisted protocol
		ImSupplement ep = doc.getSupplement(ImDocumentErrorProtocol.errorProtocolSupplementName);
		if (ep == null)
			return null;
		
		//	get minimum error severity requiring approval
		DocumentStyle docStyle = DocumentStyle.getStyleFor(doc);
		DocumentStyle errorStyle = docStyle.getSubset("errorChecks");
		String minApprovalErrorSeverity = errorStyle.getProperty("minApprovalErrorSeverity", DocumentError.SEVERITY_CRITICAL);
		
		//	load error protocol, scoping error categories and types
		InputStream epIn = ep.getInputStream();
		DocErrorProtocol idep = new DocErrorProtocol(doc, minApprovalErrorSeverity);
		ImDocumentErrorProtocol.fillErrorProtocol(idep, epIn);
		
		//	clear loading changes
		idep.setClean();
		
		//	replace generic supplement with our own so any changes get persisted
		ErrorProtocolSupplement eps = new ErrorProtocolSupplement(doc, idep, ep);
		doc.addSupplement(eps);
		
		//	link protocol to supplement to activate change tracking
		idep.supplement = eps;
		
		//	finally ...
		return idep;
	}
	
	public int getPriority(Attributed doc) {
		ImDocument imDoc = this.findImDocument(doc);
		return ((imDoc == null) ? -1 : 10);
	}
	public DocumentErrorProtocol getErrorProtocolFor(Attributed doc) {
		
		//	get wrapped IM document, if any
		ImDocument imDoc = this.findImDocument(doc);
		if (imDoc == null)
			return null;
		
		//	get error protocol
		return this.getErrorProtocolFor(imDoc);
	}
	private ImDocument findImDocument(Attributed doc) {
		
		//	get root document
		if (doc instanceof QueriableAnnotation)
			doc = ((QueriableAnnotation) doc).getDocument();
		
		//	get wrapped IM document, if any
		if (doc instanceof ImDocument)
			return ((ImDocument) doc);
		else if (doc instanceof ImObject)
			return ((ImObject) doc).getDocument();
		else if (doc instanceof ImDocumentRoot)
			return ((ImDocumentRoot) doc).document();
		else return null;
	}
	
	public DocErrorProtocol getErrorProtocolFor(ImDocument doc) {
		return this.getErrorProtocolFor(doc, false);
	}
	
	private DocErrorProtocol getErrorProtocolFor(ImDocument doc, boolean waiveNewSupplementUntilAfterInitialCheck) {
		
		//	get error protocol from cache
		DocErrorProtocol idep = ((DocErrorProtocol) this.errorProtocolCache.get(doc.docId));
		if (idep != null)
			return idep;
		
		//	try and load protocol on demand
		try {
			idep = this.readErrorProtocol(doc);
			if (idep != null) {
				this.errorProtocolCache.put(doc.docId, idep);
				return idep;
			}
		}
		catch (IOException ioe) {
			System.out.println("Could not load error protocol for document " + doc.docId + ": " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		
		//	get minimum error severity requiring approval
		DocumentStyle docStyle = DocumentStyle.getStyleFor(doc);
		DocumentStyle errorStyle = docStyle.getSubset("errorChecks");
		String minApprovalErrorSeverity = errorStyle.getProperty("minApprovalErrorSeverity", DocumentError.SEVERITY_CRITICAL);
		
		//	create and cache new error protocol
		idep = new DocErrorProtocol(doc, minApprovalErrorSeverity);
		this.errorProtocolCache.put(doc.docId, idep);
		
		//	on initial check, add supplement (and activate change tracking) only after populating error protocol
		if (waiveNewSupplementUntilAfterInitialCheck)
			return idep;
		
		//	add supplement so content gets persisted
		ErrorProtocolSupplement eps = new ErrorProtocolSupplement(doc, idep, null);
		doc.addSupplement(eps);
		
		//	link protocol to supplement to activate change tracking
		idep.supplement = eps;
		
		//	finally ...
		return idep;
	}
	private Map errorProtocolCache = Collections.synchronizedMap(new HashMap());
	
	private class DocErrorProtocol extends ImDocumentErrorProtocol implements ImDocumentListener, AtomicActionListener {
		int modCount = 0;
		private int saveModCount = 0;
		private HashMap removedErrorsBySubject = new HashMap();
		
		ErrorProtocolSupplement supplement;
		
		final String minApprovalErrorSeverity;
		
		private ImDocumentMarkupPanel documentPanel = null;
		private long atomicActionID = 0;
		private ImAnnotation atomicActionTarget = null;
		private LinkedList restoreActions = new LinkedList();
		
		private LinkedHashSet subjectsAdded = new LinkedHashSet();
		private LinkedHashSet subjectsRemoved = new LinkedHashSet();
		private LinkedHashMap subjectTypesChanged = new LinkedHashMap();
		private LinkedHashSet subjectAttributesChanged = new LinkedHashSet();
		private LinkedHashSet errorCategoriesChanged = new LinkedHashSet();
		
		DocErrorProtocol(ImDocument subject, String minApprovalErrorSeverity) {
			super(subject);
			this.subject.addDocumentListener(this);
			this.minApprovalErrorSeverity = minApprovalErrorSeverity;
		}
		
		void setDocumentMarkupPanel(ImDocumentMarkupPanel idmp) {
			if (this.documentPanel == idmp)
				return;
			if (this.documentPanel != null)
				this.documentPanel.removeAtomicActionListener(this);
			this.documentPanel = idmp;
			if (this.documentPanel != null)
				this.documentPanel.addAtomicActionListener(this);
		}
		
		public void atomicActionStarted(long id, String label, ImageMarkupTool imt, ImAnnotation annot, ProgressMonitor pm) {
			this.atomicActionID = id;
			this.atomicActionTarget = annot; // remember scope of action for re-check context (if any)
		}
		public void atomicActionFinishing(long id, ProgressMonitor pm) {
			if (this.atomicActionID < 0)
				return;
			this.runPendingReChecks(pm); // update error protocol at end of atomic action
		}
		public void atomicActionFinished(long id, ProgressMonitor pm) {
			this.atomicActionID = 0;
			this.atomicActionTarget = null;
		}
		
		void runPendingReChecks(ProgressMonitor pm) {
			
			//	anything to re-check at all?
			if (this.subjectsAdded.isEmpty() && this.subjectsRemoved.isEmpty() && this.subjectTypesChanged.isEmpty() && this.subjectAttributesChanged.isEmpty())
				return;
			
			//	remember displaying category and type
			final String selectedCategory;
			final String selectedType;
			if (idepDisplay == null) {
				selectedCategory = null;
				selectedType = null;
			}
			else {
				selectedCategory = idepDisplay.getErrorCategory();
				selectedType = idepDisplay.getErrorType();
			}
			
			//	collect changed subject types and object types, as well as subjects proper
			if (pm != null)
				pm.setStep("Collecting subjects for error re-checks");
//			HashSet subjects = new HashSet();
//			HashSet subjectTypes = new HashSet();
			HashSet regionSubjects = new HashSet();
			HashSet regionSubjectTypes = new HashSet();
			HashSet annotSubjects = new HashSet();
			HashSet annotSubjectTypes = new HashSet();
			HashSet otherSubjects = new HashSet();
			HashSet otherSubjectTypes = new HashSet();
			ErrorCheckTargetSet addedOrModifiedTypes = new ErrorCheckTargetSet();
			ErrorCheckTargetSet removedTypes = new ErrorCheckTargetSet();
			for (Iterator sit = this.subjectsAdded.iterator(); sit.hasNext();) {
				ImObject subject = ((ImObject) sit.next());
//				subjects.add(subject);
//				subjectTypes.add(subject.getType());
				addedOrModifiedTypes.add(subject.getType());
				if (subject instanceof ImRegion) {
					regionSubjects.add(subject);
					regionSubjectTypes.add(subject.getType());
				}
				else if (subject instanceof ImAnnotation) {
					annotSubjects.add(subject);
					annotSubjectTypes.add(subject.getType());
				}
				else {
					otherSubjects.add(subject);
					otherSubjectTypes.add(subject.getType());
				}
			}
			for (Iterator sit = this.subjectsRemoved.iterator(); sit.hasNext();) {
				ImObject subject = ((ImObject) sit.next());
//				subjects.add(subject);
//				subjectTypes.add(subject.getType());
				removedTypes.add(subject.getType());
				if (subject instanceof ImRegion) {
					regionSubjects.add(subject);
					regionSubjectTypes.add(subject.getType());
				}
				else if (subject instanceof ImAnnotation) {
					annotSubjects.add(subject);
					annotSubjectTypes.add(subject.getType());
				}
				else {
					otherSubjects.add(subject);
					otherSubjectTypes.add(subject.getType());
				}
			}
			for (Iterator sit = this.subjectTypesChanged.keySet().iterator(); sit.hasNext();) {
				ImObject subject = ((ImObject) sit.next());
//				subjects.add(subject);
//				subjectTypes.add(subject.getType());
				addedOrModifiedTypes.add(subject.getType());
				removedTypes.add(this.subjectTypesChanged.get(subject));
				if (subject instanceof ImRegion) {
					regionSubjects.add(subject);
					regionSubjectTypes.add(subject.getType());
				}
				else if (subject instanceof ImAnnotation) {
					annotSubjects.add(subject);
					annotSubjectTypes.add(subject.getType());
				}
				else {
					otherSubjects.add(subject);
					otherSubjectTypes.add(subject.getType());
				}
			}
			for (Iterator sit = this.subjectAttributesChanged.iterator(); sit.hasNext();) {
				ImObject subject = ((ImObject) sit.next());
//				subjects.add(subject);
//				subjectTypes.add(subject.getType());
				addedOrModifiedTypes.add(subject.getType());
				if (subject instanceof ImRegion) {
					regionSubjects.add(subject);
					regionSubjectTypes.add(subject.getType());
				}
				else if (subject instanceof ImAnnotation) {
					annotSubjects.add(subject);
					annotSubjectTypes.add(subject.getType());
				}
				else {
					otherSubjects.add(subject);
					otherSubjectTypes.add(subject.getType());
				}
			}
			System.out.println("Re-check region types: " + regionSubjectTypes);
			System.out.println("Re-check annotation types: " + annotSubjectTypes);
			System.out.println("Re-check object types: " + otherSubjectTypes);
			System.out.println("Added or modified types: " + addedOrModifiedTypes);
			System.out.println("Removed types: " + removedTypes);
			
			//	TODO maybe also group/cluster subjects by related test targets (might even more localize re-checks)
			
			//	only words in regions, move them to annotations if latter not empty
			if ((annotSubjectTypes.size() != 0) && (regionSubjectTypes.size() == 1) && regionSubjectTypes.contains(ImWord.WORD_ANNOTATION_TYPE)) {
				annotSubjects.addAll(regionSubjects);
				annotSubjectTypes.addAll(regionSubjectTypes);
				regionSubjects.clear();
				regionSubjectTypes.clear();
			}
			
			//	suspend immediate protocol display updates
			if ((idepDisplay != null) && (idepDisplay.errorProtocol == this))
				idepDisplay.setImmediateUpdatesSuspended(true);
			
			//	clean up after removed subjects
			for (Iterator sit = this.subjectsRemoved.iterator(); sit.hasNext();) {
				ImObject subject = ((ImObject) sit.next());
				this.removeErrorsBySubject(subject);
			}
			
			//	re-check error subjects by object type
			if (regionSubjectTypes.size() != 0) {
				if (pm != null)
					pm.setStep("Re-checking region errors");
				this.reCheckErrors(regionSubjects, ImRegion.class, regionSubjectTypes, addedOrModifiedTypes, removedTypes, pm);
			}
			if (annotSubjectTypes.size() != 0) {
				if (pm != null)
					pm.setStep("Re-checking annotation errors");
				this.reCheckErrors(annotSubjects, ImAnnotation.class, annotSubjectTypes, addedOrModifiedTypes, removedTypes, pm);
			}
			if (otherSubjectTypes.size() != 0) {
				if (pm != null)
					pm.setStep("Re-checking other errors");
				this.reCheckErrors(otherSubjects, ImObject.class, otherSubjectTypes, addedOrModifiedTypes, removedTypes, pm);
			}
			
			//	update approval status of any pending categories
			if (pm != null)
				pm.setStep("Updating approval status on modified error categories");
			for (Iterator cit = this.errorCategoriesChanged.iterator(); cit.hasNext();) {
				String category = ((String) cit.next());
				this.updateCategoryApprovalStatus(category, true);
			}
			
			//	clean up
			if (pm != null)
				pm.setStep("Cleaning up error re-check subjects");
			this.subjectsAdded.clear();
			this.subjectsRemoved.clear();
			this.subjectTypesChanged.clear();
			this.subjectAttributesChanged.clear();
			this.errorCategoriesChanged.clear();
			
			//	re-activate immediate protocol display updates
			/* we need to do repainting on Swing EDT, as otherwise we might
			 * incur a deadlock between this thread and EDT on synchronized
			 * parts of UI or data structures */
			if ((idepDisplay != null) && (idepDisplay.errorProtocol == this))
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if ((idepDisplay == null) || (idepDisplay.errorProtocol != DocErrorProtocol.this))
							return; // need to re-check ...
						idepDisplay.setImmediateUpdatesSuspended(false);
						if (idepDisplay.errorProtocol.getErrorCount(selectedCategory, selectedType) != 0)
							idepDisplay.setErrorType(selectedCategory, selectedType);
						else if (idepDisplay.errorProtocol.getErrorCount(selectedCategory) != 0)
							idepDisplay.setErrorCategory(selectedCategory);
					}
				});
		}
		
		private void reCheckErrors(HashSet subjects, Class subjectClass, HashSet subjectTypes, ErrorCheckTargetSet addedOrModifiedTypes, ErrorCheckTargetSet removedTypes, ProgressMonitor pm) {
			
			//	get checks to re-run
			boolean removalOnly = (removedTypes.containsAll(subjectTypes) && !addedOrModifiedTypes.containsAny(subjectTypes));
			System.out.println("Re-checking " + subjectClass.getName() + ", removal-only is " + removalOnly);
			DocErrorCheckList idecl = getErrorCheckListFor(this.subject, subjectTypes, removalOnly, null);
			if (idecl == null)
				return;
			System.out.println("Got " + idecl.size() + " checks to re-run");
			System.out.println(" - target types are " + idecl.targets);
			System.out.println(" - subject types are " + idecl.subjects);
			
			//	we have referential checks, need to re-check whole document
			if (idecl.hasReferentialChecks()) {
				for (int c = 0; c < idecl.size(); c++)
					this.removeErrorsBySource(idecl.getErrorCheck(c).sourceId);
				System.out.println("Re-checking whole document (for referential checks)");
				checkDocumentErrors(this.subject, null, idecl, idecl.subjects, null, null, pm);
				return;
			}
			
			/* TODO Make sure error re-checking is more localized:
- when getting context region or annotation, add all ancestors (not only error check targets)
- group re-check subjects by outmost context objects (localId property from previous  mail should help here) ...
- ... and run re-checks by these groups ...
- ... moving scope inward as far as possible in each group
  ==> way  less effort on simple edits
  ==> use RecheckScope object for this ...
  ==> ... providing getScope() method for zooming in
- switch to whole document only if major portions of it affected (>50% ???, maybe even more)
==> maybe parallelize re-checks on individual contexts ...
==> ... synchronizing addition of errors on protocol
- for text stream re-checks, maybe use logical paragraphs as artificial (distached) context annotations ...
  ==> helps localizing re-check context even for cross-block and cross-page word relation edits

Speed up re-checks on physical paragraph splits:
- scope re-check to parent block ...
- ... maybe extending to continued and continuing blocks in other columns or pages (based upon word relations)

Speed up re-checks on cross-page and cross-column paragraph mergers (or splits)
- scope re-check as above

Implementation:
- catch paragraphs in region edits
- handle words separately in scope computation
- in both cases, add blocks even though rarely in error check queries ...
- ... and use temporary annotation for re-checking
			 */
			
			//	re-check regions
			if (ImRegion.class.equals(subjectClass)) {
				
				//	get common context (if any)
				//	TODO try and group regions by common context, and re-check individually (should be faster for multiple localized edits)
				//	TODO switch to global re-check soon as more than quarter of document pages affected
				//	TODO == best do this page by page !!!
				ImRegion[] cRegions = null;
				HashSet cSubjects = new HashSet();
				for (Iterator sit = subjects.iterator(); sit.hasNext();) {
					ImRegion subject = ((ImRegion) sit.next());
					ImRegion[] scRegions = this.getReCheckContext(subject, idecl);
					if (scRegions == null)
						continue;
					cSubjects.add(subject);
					for (int r = 0; r < scRegions.length; r++) {
						if (idecl.subjects.contains(scRegions[r].getType()))
							cSubjects.add(scRegions[r]);
						else if (WORD_ANNOTATION_TYPE.equals(subject.getType())) {
							if (PARAGRAPH_TYPE.equals(scRegions[r].getType()))
								cSubjects.add(scRegions[r]);
						}
						else if (PARAGRAPH_TYPE.equals(subject.getType())) {
							if (BLOCK_ANNOTATION_TYPE.equals(scRegions[r].getType()))
								cSubjects.add(scRegions[r]);
						}
						else if (BLOCK_ANNOTATION_TYPE.equals(subject.getType())) {
							if (COLUMN_ANNOTATION_TYPE.equals(scRegions[r].getType()))
								cSubjects.add(scRegions[r]);
						}
					}
					if (cRegions == null)
						cRegions = scRegions;
					else if ((cRegions[0].pageId == scRegions[0].pageId) && cRegions[0].getType().equals(scRegions[0].getType()) && cRegions[0].bounds.equals(scRegions[0].bounds)) {}
					else {
						cRegions = null;
						break;
					}
				}
				
				//	no subjects to re-check at all
				if (cSubjects.isEmpty()) {
					System.out.println("No subjects to re-check");
					return;
				}
				
				//	no common context, re-check globally
				if (cRegions == null) {
					for (int c = 0; c < idecl.size(); c++)
						this.removeErrorsBySource(idecl.getErrorCheck(c).sourceId);
					System.out.println("Re-checking whole document (no context region)");
					checkDocumentErrors(this.subject, null, idecl, idecl.subjects, null, null, pm);
				}
				
				//	re-check only shared context
				else {
					
					//	clean up errors by subject, and collect IDs and convex hull
					HashSet cSubjectIDs = new HashSet();
					BoundingBox cSubjectBox = null;
					for (Iterator sit = cSubjects.iterator(); sit.hasNext();) {
						ImRegion cSubject = ((ImRegion) sit.next());
						cSubjectIDs.add(getTypeInternalErrorSubjectId(cSubject, this.subject));
						if (cSubjectBox == null)
							cSubjectBox = cSubject.bounds;
						else cSubjectBox = cSubjectBox.union(cSubject.bounds);
						DocumentError[] tisErrors = this.getErrorsForSubject(cSubject);
						for (int e = 0; e < tisErrors.length; e++) {
							if (idecl.sourceIDs.contains(tisErrors[e].source))
								this.removeError(tisErrors[e]);
						}
					}
					
					//	move context region as far inward as possible, as long as convex hull of error subjects contained
					Arrays.sort(cRegions, new Comparator() {
						public int compare(Object obj1, Object obj2) {
							return (((ImRegion) obj2).bounds.getArea() - ((ImRegion) obj1).bounds.getArea());
						}
					});
					ImRegion cRegion = cRegions[0];
					for (int r = 1; r < cRegions.length; r++) {
						if (cRegions[r].bounds.includes(cSubjectBox, false))
							cRegion = cRegions[r];
					}
					
					//	perform re-checks on context
					System.out.println("Re-checking context region " + cRegion.getType() + " at " + cRegion.pageId + "." + cRegion.bounds + " containing " + cSubjectBox);
					checkDocumentErrors(this.subject, cRegion, idecl, idecl.subjects, cSubjectIDs, null, null);
				}
			}
			
			//	re-check annotations
			else if (ImAnnotation.class.equals(subjectClass)) {
				
				//	get common context (if any)
				ImAnnotation[] cAnnots = null;
				HashSet cSubjects = new HashSet();
				
				//	no specific target of atomic action given, seek context
				if (this.atomicActionTarget == null) {
					//	TODO try and group annotations by common context, and re-check individually (should be faster for multiple localized edits)
					//	TODO switch to global re-check soon as more than quarter of document pages affected
					//	==> TODO try and generically identify top level annotations (subSections, treatments) ...
					//	==> TODO ... include them in context (just like page structure above for regions) ...
					//	==> TODO ... and group re-checks by them
					for (Iterator sit = subjects.iterator(); sit.hasNext();) {
						ImAnnotation subject = ((ImAnnotation) sit.next());
						ImAnnotation[] scAnnots = this.getReCheckContext(subject, idecl);
						if (scAnnots == null)
							continue;
						cSubjects.add(subject);
						cSubjects.addAll(Arrays.asList(scAnnots));
						if (cAnnots == null)
							cAnnots = scAnnots;
						else if ((cAnnots[0].getFirstWord() == scAnnots[0].getFirstWord()) && (cAnnots[0].getLastWord() == scAnnots[0].getLastWord()) && cAnnots[0].getType().equals(scAnnots[0].getType())) {}
						else {
							cAnnots = null;
							break;
						}
					}
				}
				
				//	use target of atomic action
				else {
					cAnnots = this.subject.getAnnotationsOverlapping(this.atomicActionTarget.getFirstWord(), this.atomicActionTarget.getLastWord());
					ArrayList cAnnotList = new ArrayList();
					for (int a = 0; a < cAnnots.length; a++) {
						if (!idecl.targets.contains(cAnnots[a].getType()))
							continue;
						cAnnotList.add(cAnnots[a]);
						cSubjects.add(cAnnots[a]);
					}
					cAnnots = ((ImAnnotation[]) cAnnotList.toArray(new ImAnnotation[cAnnotList.size()]));
				}
				
				//	no subjects to re-check at all
				if (cSubjects.isEmpty()) {
					System.out.println("No subjects to re-check");
					return;
				}
				
				//	no common context, re-check globally
				if (cAnnots == null) {
					for (int c = 0; c < idecl.size(); c++)
						this.removeErrorsBySource(idecl.getErrorCheck(c).sourceId);
					System.out.println("Re-checking whole document (no context annotation)");
					checkDocumentErrors(this.subject, null, idecl, idecl.subjects, null, null, pm);
				}
				
				//	re-check only shared context
				else {
					
					//	clean up errors by subject, and collect IDs
					HashSet cSubjectIDs = new HashSet();
					for (Iterator sit = cSubjects.iterator(); sit.hasNext();) {
						ImAnnotation cSubject = ((ImAnnotation) sit.next());
						cSubjectIDs.add(getTypeInternalErrorSubjectId(cSubject, this.subject));
						DocumentError[] tisErrors = this.getErrorsForSubject(cSubject);
						for (int e = 0; e < tisErrors.length; e++) {
							if (idecl.sourceIDs.contains(tisErrors[e].source))
								this.removeError(tisErrors[e]);
						}
					}
					
					//	perform re-checks on context
					System.out.println("Re-checking context annotation " + cAnnots[0].getType() + " from " + cAnnots[0].getFirstWord().getLocalID() + " to " + cAnnots[0].getLastWord().getLocalID());
					checkDocumentErrors(this.subject, cAnnots[0], idecl, idecl.subjects, cSubjectIDs, null, null);
				}
			}
			
			//	to re-check whole document (if only for attribute related error sources, they are the only ones with document as subject)
			else {
				for (int c = 0; c < idecl.size(); c++)
					this.removeErrorsBySource(idecl.getErrorCheck(c).sourceId);
				System.out.println("Re-checking whole document (for document attributes)");
				checkDocumentErrors(this.subject, null, idecl, idecl.subjects, null, null, pm);
			}
		}
		
		private boolean runReChecksImmediately() {
			if (this.documentPanel == null)
				return false; // no display open, re-checks can wait
			if (this.documentPanel.isAtomicActionRunning())
				return false; // we only re-check once that atomic action finishes
			return true;
		}
		
		public void attributeChanged(ImObject object, String attributeName, Object oldValue) {
			if (this.atomicActionID < 0)
				return; // we're in an UNDO operation
			if (!errorCheckTargets.contains(object.getType()))
				return; // this one has no checks at all
			if (!errorCheckTargetAttributes.contains(attributeName))
				return; // this one has no checks at all
			if (APPROVAL_REQUIRED_ATTRIBUTE_NAME.equals(attributeName))
				return; // let's not get all fussed about attributes we set ourselves
			if ((attributeName != null) && attributeName.startsWith(APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX))
				return; // let's not get all fussed about attributes we set ourselves
			if ((attributeName != null) && attributeName.startsWith("_"))
				return; // let's not worry about debug and control attributes
			
			//	run any re-checks right away
			if (this.runReChecksImmediately()) {
				System.out.println("Re-checking errors for attribute " + attributeName + " change on " + object);
				if (object instanceof ImWord) {
					this.reCheckRegionContext(((ImWord) object), object.getType(), false, ("@" + attributeName));
					if (ImWord.NEXT_WORD_ATTRIBUTE.equals(attributeName)) {
						ImWord nextWord = ((ImWord) object).getNextWord();
						if (nextWord != null)
							this.reCheckRegionContext(nextWord, object.getType(), false, ("@" + ImWord.PREVIOUS_WORD_ATTRIBUTE));
						ImWord oldNextWord = ((ImWord) oldValue);
						if (oldNextWord != null)
							this.reCheckRegionContext(oldNextWord, object.getType(), false, ("@" + ImWord.PREVIOUS_WORD_ATTRIBUTE));
					}
					else if (ImWord.NEXT_RELATION_ATTRIBUTE.equals(attributeName)) {
						ImWord nextWord = ((ImWord) object).getNextWord();
						if (nextWord != null)
							this.reCheckRegionContext(nextWord, object.getType(), false, ("@" + ImWord.PREVIOUS_RELATION_ATTRIBUTE));
					}
					else if (ImWord.PREVIOUS_WORD_ATTRIBUTE.equals(attributeName)) {
						ImWord prevWord = ((ImWord) object).getPreviousWord();
						if (prevWord != null)
							this.reCheckRegionContext(prevWord, object.getType(), false, ("@" + ImWord.NEXT_WORD_ATTRIBUTE));
						ImWord oldPrevWord = ((ImWord) oldValue);
						if (oldPrevWord != null)
							this.reCheckRegionContext(oldPrevWord, object.getType(), false, ("@" + ImWord.NEXT_WORD_ATTRIBUTE));
					}
					else if (ImWord.PREVIOUS_RELATION_ATTRIBUTE.equals(attributeName)) {
						ImWord prevWord = ((ImWord) object).getPreviousWord();
						if (prevWord != null)
							this.reCheckRegionContext(prevWord, object.getType(), false, ("@" + ImWord.NEXT_RELATION_ATTRIBUTE));
					}
				}
				else if (object instanceof ImAnnotation) {
					this.reCheckAnnotationContext(((ImAnnotation) object), object.getType(), false, ("@" + attributeName));
				}
				else if (object instanceof ImRegion) {
					this.reCheckRegionContext(((ImRegion) object), object.getType(), false, ("@" + attributeName));
				}
				else if (object instanceof ImSupplement) { /* nothing to do about supplements (we cannot even check them) */ }
				else if (object instanceof ImDocument) {
					
					//	get error checks interested in attribute changes on document
					DocErrorCheckList idecl = getErrorCheckListFor(this.subject, object.getType(), false, attributeName);
					if (idecl == null)
						return;
					
					//	to re-check whole document (if only for attribute related error sources)
					for (int c = 0; c < idecl.size(); c++)
						this.removeErrorsBySource(idecl.getErrorCheck(c).sourceId);
					checkDocumentErrors(this.subject, null, idecl, idecl.subjects, null, null, null);
				}
				else {
					
					//	get error checks interested in attribute changes on whatever object we have
					DocErrorCheckList idecl = getErrorCheckListFor(this.subject, object.getType(), false, attributeName);
					if (idecl == null)
						return;
					
					//	to re-check whole document
					String tisId = getTypeInternalErrorSubjectId(object, this.subject);
					checkDocumentErrors(this.subject, null, idecl, Collections.singleton(object.getType()), Collections.singleton(tisId), Collections.singleton(attributeName), null);
				}
			}
			
			//	remember for bulk re-check (also predecessor or successor of words on respective changes)
			else {
				this.subjectAttributesChanged.add(object);
				if (object instanceof ImWord) {
					if (ImWord.NEXT_WORD_ATTRIBUTE.equals(attributeName)) {
						ImWord nextWord = ((ImWord) object).getNextWord();
						if (nextWord != null)
							this.subjectAttributesChanged.add(nextWord);
						ImWord oldNextWord = ((ImWord) oldValue);
						if (oldNextWord != null)
							this.subjectAttributesChanged.add(oldNextWord);
					}
					else if (ImWord.NEXT_RELATION_ATTRIBUTE.equals(attributeName)) {
						ImWord nextWord = ((ImWord) object).getNextWord();
						if (nextWord != null)
							this.subjectAttributesChanged.add(nextWord);
					}
					else if (ImWord.PREVIOUS_WORD_ATTRIBUTE.equals(attributeName)) {
						ImWord prevWord = ((ImWord) object).getPreviousWord();
						if (prevWord != null)
							this.subjectAttributesChanged.add(prevWord);
						ImWord oldPrevWord = ((ImWord) oldValue);
						if (oldPrevWord != null)
							this.subjectAttributesChanged.add(oldPrevWord);
					}
					else if (ImWord.PREVIOUS_RELATION_ATTRIBUTE.equals(attributeName)) {
						ImWord prevWord = ((ImWord) object).getPreviousWord();
						if (prevWord != null)
							this.subjectAttributesChanged.add(prevWord);
					}
				}
			}
		}
		public void supplementChanged(String supplementId, ImSupplement oldValue) { /* no error checks on supplements for now */ }
		public void typeChanged(ImObject object, String oldType) {
			if (this.atomicActionID < 0)
				return; // we're in an UNDO operation
			
			if (!errorCheckTargets.contains(object.getType()) && !errorCheckTargets.contains(oldType))
				return; // this one has no checks at all
			
			//	run any re-checks right away
			if (this.runReChecksImmediately()) {
				//	no need to worry about words or pages, they don't ever change their type, nor does the document proper
				
				//	treat as removal of old type ...
				if (object instanceof ImAnnotation)
					this.reCheckAnnotationContext(((ImAnnotation) object), oldType, true, null);
				else if (object instanceof ImRegion)
					this.reCheckRegionContext(((ImRegion) object), oldType, true, null);
				
				//	... followed by addition of new type
				if (object instanceof ImAnnotation)
					this.reCheckAnnotationContext(((ImAnnotation) object), object.getType(), false, null);
				else if (object instanceof ImRegion)
					this.reCheckRegionContext(((ImRegion) object), object.getType(), false, null);
			}
			
			//	remember for bulk re-check
			else if (!this.subjectTypesChanged.containsKey(object))
				this.subjectTypesChanged.put(object, oldType); // only remember original type, intermediate types are gone
		}
		public void regionAdded(ImRegion region) {
			if (this.atomicActionID < 0)
				return; // we're in an UNDO operation
			
			if (!errorCheckTargets.contains(region.getType()))
				return; // this one has no checks at all
			
			//	run any re-checks right away
			if (this.runReChecksImmediately()) {
				this.restoreErrors(region, region.getType()); // re-add any errors removed following region removal (Undo or user-revert !!!)
				this.reCheckRegionContext(region, region.getType(), false, null);
			}
			
			//	remember for bulk re-check
			else if (!this.subjectsRemoved.remove(region)) // try forgetting un-done or user-reverted removal first
				this.subjectsAdded.add(region); // remember original addition
		}
		public void regionRemoved(ImRegion region) {
			if (this.atomicActionID < 0)
				return; // we're in an UNDO operation
			
			if (!errorCheckTargets.contains(region.getType()))
				return; // this one has no checks at all
			
			//	run any context re-checks right away
			if (this.runReChecksImmediately()) {
				this.removeErrorsBySubject(region); // clean up errors pertaining to removed region
				this.reCheckRegionContext(region, region.getType(), true, null);
			}
			
			//	remember for bulk re-check
			else if (this.subjectsAdded.remove(region)) // try forgetting un-done or user-reverted addition first ...
				this.subjectAttributesChanged.remove(region); // ... and forget attribute changes along the way
			else this.subjectsRemoved.add(region); // remember original removal
		}
		private void reCheckRegionContext(ImRegion region, String type, boolean isRemoval, String attributeName) {
			if (this.atomicActionID < 0)
				return; // we're in an UNDO operation
			
			//	get error checks interested in region addition or removal
			DocErrorCheckList idecl = getErrorCheckListFor(this.subject, type, isRemoval, attributeName);
			if (idecl == null)
				return;
			
			//	we have referential checks, need to re-check whole document
			if (idecl.hasReferentialChecks()) {
				for (int c = 0; c < idecl.size(); c++)
					this.removeErrorsBySource(idecl.getErrorCheck(c).sourceId);
				checkDocumentErrors(this.subject, null, idecl, idecl.subjects, null, null, null);
				return;
			}
			
			//	get re-check context of region
			ImRegion[] reCheckRegions = this.getReCheckContext(region, idecl);
			if (reCheckRegions == null)
				return;
			
			//	clean up errors by re-check subject and source
			HashSet tisIDs = new HashSet();
			for (int r = 0; r < reCheckRegions.length; r++) {
				tisIDs.add(getTypeInternalErrorSubjectId(reCheckRegions[r], this.subject));
				DocumentError[] tisErrors = this.getErrorsForSubject(reCheckRegions[r]);
				for (int e = 0; e < tisErrors.length; e++) {
					if (idecl.sourceIDs.contains(tisErrors[e].source))
						this.removeError(tisErrors[e]);
				}
			}
			
			//	re-check broadest possible context
			checkDocumentErrors(this.subject, reCheckRegions[0], idecl, idecl.subjects, tisIDs, null, null);
		}
		private ImRegion[] getReCheckContext(ImRegion subject, DocErrorCheckList idecl) {
			ImPage page = this.subject.getPage(subject.pageId);
			ImRegion[] regions;
			ArrayList cRegions = new ArrayList();
			if (subject.getPage() != null)
				cRegions.add(subject.getPage()); // always add page
			else cRegions.add(subject.getDocument().getPage(subject.pageId));
			regions = page.getRegionsIncluding(subject.bounds, false);
			for (int r = 0; r < regions.length; r++) {
				if (idecl.subjects.contains(regions[r].getType()))
					cRegions.add(regions[r]);
				else if (WORD_ANNOTATION_TYPE.equals(subject.getType())) {
					if (COLUMN_ANNOTATION_TYPE.equals(regions[r].getType()))
						cRegions.add(regions[r]);
					else if (BLOCK_ANNOTATION_TYPE.equals(regions[r].getType()))
						cRegions.add(regions[r]);
					else if (PARAGRAPH_TYPE.equals(regions[r].getType()))
						cRegions.add(regions[r]);
				}
				else if (PARAGRAPH_TYPE.equals(subject.getType())) {
					if (COLUMN_ANNOTATION_TYPE.equals(regions[r].getType()))
						cRegions.add(regions[r]);
					else if (BLOCK_ANNOTATION_TYPE.equals(regions[r].getType()))
						cRegions.add(regions[r]);
				}
				else if (BLOCK_ANNOTATION_TYPE.equals(subject.getType())) {
					if (COLUMN_ANNOTATION_TYPE.equals(regions[r].getType()))
						cRegions.add(regions[r]);
				}
			}
			regions = page.getRegionsInside(subject.bounds, false);
			for (int r = 0; r < regions.length; r++) {
				if (idecl.subjects.contains(regions[r].getType()))
					cRegions.add(regions[r]);
				else if (BLOCK_ANNOTATION_TYPE.equals(subject.getType())) {
					if (PARAGRAPH_TYPE.equals(regions[r].getType()))
						cRegions.add(regions[r]);
				}
				else if (COLUMN_ANNOTATION_TYPE.equals(subject.getType())) {
					if (PARAGRAPH_TYPE.equals(regions[r].getType()))
						cRegions.add(regions[r]);
					else if (BLOCK_ANNOTATION_TYPE.equals(regions[r].getType()))
						cRegions.add(regions[r]);
				}
			}
			if (cRegions.isEmpty())
				return null;
			else return ((ImRegion[]) cRegions.toArray(new ImRegion[cRegions.size()]));
		}
		
		public void annotationAdded(ImAnnotation annotation) {
			if (this.atomicActionID < 0)
				return; // we're in an UNDO operation
			
			if (!errorCheckTargets.contains(annotation.getType()))
				return; // this one has no checks at all
			
			//	run any re-checks right away
			if (this.runReChecksImmediately()) {
				this.restoreErrors(annotation, annotation.getType()); // re-add any errors removed following annotation removal (Undo or user-revert !!!)
				this.reCheckAnnotationContext(annotation, annotation.getType(), false, null);
			}
			
			//	remember for bulk re-check
			else if (!this.subjectsRemoved.remove(annotation)) // try forgetting un-done or user-reverted removal first
				this.subjectsAdded.add(annotation); // remember original addition
		}
		public void annotationRemoved(ImAnnotation annotation) {
			if (this.atomicActionID < 0)
				return; // we're in an UNDO operation
			
			if (!errorCheckTargets.contains(annotation.getType()))
				return; // this one has no checks at all
			
			//	run any context re-checks right away
			if (this.runReChecksImmediately()) {
				this.removeErrorsBySubject(annotation); // clean up errors pertaining to removed annotation
				this.reCheckAnnotationContext(annotation, annotation.getType(), true, null);
			}
			
			//	remember for bulk re-check
			else if (this.subjectsAdded.remove(annotation)) // try forgetting un-done or user-reverted addition first ...
				this.subjectAttributesChanged.remove(annotation); // ... and forget attribute changes along the way
			else this.subjectsRemoved.add(annotation); // remember original removal
		}
		private void reCheckAnnotationContext(ImAnnotation annotation, String type, boolean isRemoval, String attributeName) {
			
			//	get error checks interested in annotation addition or removal
			DocErrorCheckList idecl = getErrorCheckListFor(this.subject, type, isRemoval, attributeName);
			if (idecl == null)
				return;
			
			//	we have referential checks, need to re-check whole document
			if (idecl.hasReferentialChecks()) {
				for (int c = 0; c < idecl.size(); c++)
					this.removeErrorsBySource(idecl.getErrorCheck(c).sourceId);
				checkDocumentErrors(this.subject, null, idecl, idecl.subjects, null, null, null);
				return;
			}
			
			//	get re-check context of annotation
			ImAnnotation[] reCheckAnnots = this.getReCheckContext(annotation, idecl);
			if (reCheckAnnots == null)
				return;
			
			//	clean up errors by re-check subject and source
			HashSet tisIDs = new HashSet();
			for (int a = 0; a < reCheckAnnots.length; a++) {
				tisIDs.add(getTypeInternalErrorSubjectId(reCheckAnnots[a], this.subject));
				DocumentError[] tisErrors = this.getErrorsForSubject(reCheckAnnots[a]);
				for (int e = 0; e < tisErrors.length; e++) {
					if (idecl.sourceIDs.contains(tisErrors[e].source))
						this.removeError(tisErrors[e]);
				}
			}
			
			//	re-check broadest possible context
			checkDocumentErrors(this.subject, reCheckAnnots[0], idecl, idecl.subjects, tisIDs, null, null);
		}
		private ImAnnotation[] getReCheckContext(ImAnnotation subject, DocErrorCheckList idecl) {
			ImAnnotation[] annots = this.subject.getAnnotationsOverlapping(subject.getFirstWord(), subject.getLastWord());
			ArrayList cAnnots = new ArrayList();
			for (int a = 0; a < annots.length; a++) {
				if (idecl.subjects.contains(annots[a].getType()))
					cAnnots.add(annots[a]);
			}
			if (cAnnots.isEmpty())
				return null;
			else return ((ImAnnotation[]) cAnnots.toArray(new ImAnnotation[cAnnots.size()]));
		}
		
		public void addErrorCategory(String category, String label, String description) {
			super.addErrorCategory(category, label, description);
			errorMetadataKeeper.addErrorCategory(category, label, description);
		}
		public void setErrorCategoryLabel(String category, String label) {
			super.setErrorCategoryLabel(category, label);
			errorMetadataKeeper.setErrorCategoryLabel(category, label);
		}
		public void setErrorCategoryDescription(String category, String description) {
			super.setErrorCategoryDescription(category, description);
			errorMetadataKeeper.setErrorCategoryDescription(category, description);
		}
		public void addErrorType(String category, String type, String label, String description) {
			super.addErrorType(category, type, label, description);
			errorMetadataKeeper.addErrorType(category, type, label, description);
		}
		public void setErrorTypeLabel(String category, String type, String label) {
			super.setErrorTypeLabel(category, type, label);
			errorMetadataKeeper.setErrorTypeLabel(category, type, label);
		}
		public void setErrorTypeDescription(String category, String type, String description) {
			super.setErrorTypeDescription(category, type, description);
			errorMetadataKeeper.setErrorTypeDescription(category, type, description);
		}
		
		public void addError(String source, Attributed subject, Attributed parent, String category, String type, String description, String severity, boolean falsePositive) {
			
			//	make sure we have the error category and type around
			this.copyErrorTypeMetadata(errorMetadataKeeper, category, type, false);
			
			//	add the error
			super.addError(source, subject, parent, category, type, description, severity, falsePositive);
		}
		
		//	helper class for restoring protocol on UNDO
		abstract class RestoreAction {
			final int rModCount;
			RestoreAction(int rModCount) {
				this.rModCount = rModCount;
			}
			abstract void execute();
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol#addError(de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol.ImDocumentError)
		 */
		public boolean addError(final ImDocumentError error) {
			if (super.addError(error)) {
				if (this.supplement != null) /* no need to do any of this while loading */ {
					this.restoreActions.addFirst(new RestoreAction(this.modCount) {
						void execute() {
							undoAddError(error, this.rModCount);
						}
					});
					this.updateCategoryApprovalStatus(error.category, false);
					this.supplement.notifyErrorProtocolModified(this.modCount++);
					if ((idepDisplay != null) && (idepDisplay.errorProtocol == this))
						idepDisplay.errorAdded(error);
				}
				return true;
			}
			else return false;
		}
		void undoAddError(ImDocumentError error, int rModCount) {
			if (super.removeError(error) && (idepDisplay != null) && (idepDisplay.errorProtocol == this))
				idepDisplay.errorRemoved(error);
			this.modCount = rModCount;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol#removeErrors(java.lang.String, java.lang.String)
		 */
		public boolean removeErrors(String category, String type) {
			if (category != null)
				return super.removeErrors(category, type); // this loops back to removeError(), doing the updates there
			
			final DocumentError[] errors = ((this.supplement == null) ? null : this.getErrors());
			if (!super.removeErrors(category, type))
				return false;
			
			if (this.supplement != null) /* no need to do any of this while loading */ {
				this.restoreActions.addFirst(new RestoreAction(this.modCount) {
					void execute() {
						undoClearErrors(errors, this.rModCount);
					}
				});
				this.updateCategoryApprovalStatus(category, false);
				this.supplement.notifyErrorProtocolModified(this.modCount++);
				if ((idepDisplay != null) && (idepDisplay.errorProtocol == this))
					idepDisplay.setErrorProtocol(this); // complete reset
			}
			return true;
//			if (super.removeErrors(category, type)) {
//				this.modCount++;
//				if ((category == null) && (idepDisplay != null) && (idepDisplay.errorProtocol == this))
//					idepDisplay.setErrorProtocol(this); // complete reset
//				this.updateCategoryApprovalStatus(category, false);
//				return true;
//			}
//			else return false;
		}
		void undoClearErrors(DocumentError[] errors, int rModCount) {
			boolean errorAdded = false;
			for (int e = 0; e < errors.length; e++) {
				if (super.addError((ImDocumentError) errors[e]))
					errorAdded = true;
			}
			if (errorAdded && (idepDisplay != null) && (idepDisplay.errorProtocol == this))
				idepDisplay.setErrorProtocol(this); // complete reset
			this.modCount = rModCount;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol#removeError(de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol.ImDocumentError)
		 */
		public boolean removeError(final ImDocumentError error) {
			if (super.removeError(error)) {
				if (this.supplement != null) /* no need to do any of this while loading */ {
					this.restoreActions.addFirst(new RestoreAction(this.modCount) {
						void execute() {
							undoRemoveError(error, this.rModCount);
						}
					});
					this.updateCategoryApprovalStatus(error.category, false);
					this.supplement.notifyErrorProtocolModified(this.modCount++);
					if ((idepDisplay != null) && (idepDisplay.errorProtocol == this))
						idepDisplay.errorRemoved(error);
				}
				return true;
			}
			else return false;
		}
		void undoRemoveError(ImDocumentError error, int rModCount) {
			if (super.addError(error) && (idepDisplay != null) && (idepDisplay.errorProtocol == this))
				idepDisplay.errorAdded(error);
			this.modCount = rModCount;
		}
		
		public boolean removeErrorsBySubject(ImObject subject, String type) {
			HashSet removedErrors = new HashSet(Arrays.asList(this.getErrorsForSubject(subject, type)));
			if (super.removeErrorsBySubject(subject, type)) {
				DocumentError[] retainedErrors = this.getErrorsForSubject(subject, type);
				removedErrors.removeAll(Arrays.asList(retainedErrors));
				if (removedErrors.size() != 0)
					getIndexList(this.removedErrorsBySubject, this.getErrorSubjectKey(subject, type), true).addAll(removedErrors);
				return true;
			}
			else return false;
		}
		
		public boolean markFalsePositive(final ImDocumentError error) {
			if (super.markFalsePositive(error)) {
				if (this.supplement != null) /* no need to do any of this while loading */ {
					this.restoreActions.addFirst(new RestoreAction(this.modCount) {
						void execute() {
							undoMarkFalsePositive(error, this.rModCount);
						}
					});
					this.supplement.notifyErrorProtocolModified(this.modCount++);
				}
				return true;
			}
			else return false;
		}
		void undoMarkFalsePositive(ImDocumentError error, int rModCount) {
			super.unmarkFalsePositive(error);
			this.modCount = rModCount;
		}
		
		private void updateCategoryApprovalStatus(String category, boolean isReCheck) {
			if (this.supplement == null)
				return; // make damn sure to not newly set these attributes on loading
			if (this.atomicActionID < 0)
				return; // we're in an UDO operation, which handles the attributes individually
			if (isReCheck || this.runReChecksImmediately())
//				updateApprovalStatus(this, category);
				updateApprovalStatus(this, category, false);
			else this.errorCategoriesChanged.add(category);
		}
		
		//	TODOne do we really need this?
		//	TODOne ==> not for UNDO, which we now handle directly, but helpful for reverting operations at hands of user
		private boolean restoreErrors(ImObject subject, String type) {
			ArrayList restoreErrors = getIndexList(this.removedErrorsBySubject, this.getErrorSubjectKey(subject, type), false);
			if ((restoreErrors == null) || restoreErrors.isEmpty())
				return false;
			for (int e = 0; e < restoreErrors.size(); e++)
				this.addError((ImDocumentError) restoreErrors.get(e));
			return true;
		}
		
		private String getErrorSubjectKey(ImObject subject, String type) {
			return (type + "@" + getTypeInternalErrorSubjectId(subject, this.subject));
		}
		
		boolean isDirty() {
			return (this.saveModCount < this.modCount);
		}
		
		void setClean() {
			this.saveModCount = this.modCount;
		}
		
		int resetToModCount(int toModCount) {
			while (this.restoreActions.size() != 0) {
				RestoreAction ra = ((RestoreAction) this.restoreActions.getFirst());
				if (ra.rModCount < toModCount)
					return ra.rModCount;
				ra.execute();
				this.restoreActions.removeFirst();
			}
			return -1;
		}
	}
	
	static int getApprovalErrorCount(ImDocumentErrorProtocol idep, String category, String minSeverity) {
		int count = 0;
		for (int s = 0; s < errorSeverities.length; s++) {
			if (minSeverity.compareTo(errorSeverities[s]) < 0)
				break; // exploiting alphabetical order of severities ...
			if (category == null)
				count += idep.getErrorSeverityCount(errorSeverities[s]);
			else count += idep.getErrorSeverityCount(category, errorSeverities[s]);
		}
		return count;
	}
	
	private static class DocErrorCheckList extends ArrayList {
		final ErrorCheckTargetSet targets = new ErrorCheckTargetSet();
		final ErrorCheckTargetSet subjects = new ErrorCheckTargetSet();
		final HashSet sourceIDs = new HashSet();
		private int referentialChecks = 0;
		boolean hasReferentialChecks() {
			return (this.referentialChecks != 0);
		}
		ImDocumentErrorCheck getErrorCheck(int index) {
			return ((ImDocumentErrorCheck) this.get(index));
		}
		void addErrorCheck(ImDocumentErrorCheck idec) {
			this.add(idec);
			this.targets.addAll(idec.targets);
			this.subjects.addAll(idec.subjects);
			if (idec.sourceId != null)
				this.sourceIDs.add(idec.sourceId);
			if (idec.isReferentialCheck)
				this.referentialChecks++;
		}
		void addContent(DocErrorCheckList idecl) {
			if (idecl == this)
				return;
			for (int c = 0; c < idecl.size(); c++) {
				ImDocumentErrorCheck idec = idecl.getErrorCheck(c);
				if (!this.sourceIDs.contains(idec.sourceId))
					this.addErrorCheck(idec);
			}
		}
		DocErrorCheckList unionWith(DocErrorCheckList idecl) {
			if ((idecl == null) || (idecl == this))
				return this;
			if (this.sourceIDs.containsAll(idecl.sourceIDs))
				return this;
			else if (idecl.sourceIDs.containsAll(this.sourceIDs))
				return idecl;
			DocErrorCheckList uIdecl = new DocErrorCheckList();
			uIdecl.addContent(this);
			uIdecl.addContent(idecl);
			return uIdecl;
		}
	}
	
	private static class ErrorCheckTargetSet extends LinkedHashSet {
		public boolean containsAny(Collection c) {
			for (Iterator e = c.iterator(); e.hasNext();) {
				if (contains(e.next()))
					return true;
			}
			return false;
		}
	}
	
	private DocErrorCheckList getErrorCheckListFor(ImDocument doc, String subjectType, boolean excludeIfSubject, String attributeName) {
		return this.getErrorCheckListFor(doc, Collections.singleton(subjectType), excludeIfSubject, attributeName);
	}
	
	private DocErrorCheckList getErrorCheckListFor(ImDocument doc, Set subjectTypes, boolean excludeIfSubject, String attributeName) {
		
		//	get target type specific error check list
		DocErrorCheckList typeIdecl = null;
		for (Iterator stit = subjectTypes.iterator(); stit.hasNext();) {
			String subjectType = ((String) stit.next());
			DocErrorCheckList sTypeIdecl = ((DocErrorCheckList) this.errorChecksByTargets.get(subjectType));
			if (typeIdecl == null)
				typeIdecl = sTypeIdecl;
			else typeIdecl = typeIdecl.unionWith(sTypeIdecl);
		}
		if ((typeIdecl == null) || typeIdecl.isEmpty())
			return null; // nothing to check for this type at all
		
		//	filter by attribute name (if any, for re-checks after attribute changes)
		if (attributeName != null) {
			DocErrorCheckList aTypeIdecl = new DocErrorCheckList();
			for (int c = 0; c < typeIdecl.size(); c++) {
				ImDocumentErrorCheck idec = typeIdecl.getErrorCheck(c);
				if (idec.targets.contains(attributeName))
					aTypeIdecl.addErrorCheck(idec);
			}
			if (aTypeIdecl.isEmpty())
				return null; // nothing to check for this type and attribute at all
			typeIdecl = aTypeIdecl;
		}
		
		//	exclude subject type checks if requested (for re-checks on removals and old type after type changes)
		if (excludeIfSubject && (subjectTypes.size() == 1)) {
			String subjectType = ((String) subjectTypes.iterator().next());
			DocErrorCheckList sTypeIdecl = new DocErrorCheckList();
			for (int c = 0; c < typeIdecl.size(); c++) {
				ImDocumentErrorCheck idec = typeIdecl.getErrorCheck(c);
				if (!idec.subjects.contains(subjectType))
					sTypeIdecl.addErrorCheck(idec);
			}
			if (sTypeIdecl.isEmpty())
				return null; // nothing to check for this type and attribute at all
			typeIdecl = sTypeIdecl;
		}
		
		//	get full error check list
		DocErrorCheckList docIdecl = this.getErrorCheckListFor(doc);
		if (docIdecl.sourceIDs.containsAll(typeIdecl.sourceIDs))
			return typeIdecl; // all type specific checks applicable to document
		
		//	filter by document style
		DocErrorCheckList dTypeIdecl = new DocErrorCheckList();
		for (int c = 0; c < typeIdecl.size(); c++) {
			ImDocumentErrorCheck idec = typeIdecl.getErrorCheck(c);
			if (docIdecl.sourceIDs.contains(idec.sourceId))
				dTypeIdecl.addErrorCheck(idec);
		}
		return (dTypeIdecl.isEmpty() ? null : dTypeIdecl); // return null for empty list
	}
	
	private DocErrorCheckList getErrorCheckListFor(ImDocument doc) {
		
		//	check cache first
		DocErrorCheckList idecl = ((DocErrorCheckList) this.errorCheckListCache.get(doc.docId));
		if (idecl != null)
			return idecl;
		
		//	use style template to assess which error check lists to apply
		DocumentStyle docStyle = DocumentStyle.getStyleFor(doc);
		DocumentStyle errorStyle = docStyle.getSubset("errorChecks");
		
		//	assort error checks
		idecl = new DocErrorCheckList();
		this.errorCheckListCache.put(doc.docId, idecl);
		for (Iterator eclit = this.errorCheckListsByName.keySet().iterator(); eclit.hasNext();) {
			String eclName = ((String) eclit.next());
			ImDocumentErrorCheckList ecl = ((ImDocumentErrorCheckList) this.errorCheckListsByName.get(eclName));
			String spEclName = getStyleParamName(eclName);
			String act = errorStyle.getProperty(spEclName, (ecl.applyByDefault ? ERROR_CHECK_DEFAULT_APPLY : ERROR_CHECK_DEFAULT_SKIP));
			if (!ERROR_CHECK_DEFAULT_APPLY.equals(act))
				continue;
			for (int c = 0; c < ecl.errorChecks.size(); c++) {
				ImDocumentErrorCheck ec = ((ImDocumentErrorCheck) ecl.errorChecks.get(c));
//				if (ERROR_CHECK_LEVEL_PAGE.equals(ec.level))
//					idecl.pageLevelErrorChecks.add(ec);
//				else idecl.streamLevelErrorChecks.add(ec);
				idecl.addErrorCheck(ec);
			}
		}
		
		//	finally ...
		return idecl;
	}
	private Map errorCheckListCache = Collections.synchronizedMap(new HashMap());
	
	private static String getStyleParamName(String eclName) {
		return ((eclName == null) ? null : eclName.replaceAll("[^a-zA-Z0-9\\_]+", "_"));
	}
	
	private static int errorHighlightMargin = 1;
	private static BasicStroke errorOutlineStroke = new BasicStroke(2);
	private static Color errorOutlineColor = Color.RED;
	private static Color errorHighlightColor = new Color(errorOutlineColor.getRed(), errorOutlineColor.getGreen(), errorOutlineColor.getBlue(), 64);
	
	private static DisplayExtensionGraphics[] getErrorVisualizationGraphics(ImDocumentErrorProtocolDisplay parent, ImDocumentMarkupPanel idmp, ImPage page, BoundingBox bounds) {
		Shape[] shapes = {new Rectangle2D.Float(
				(bounds.left - errorHighlightMargin),
				(bounds.top - errorHighlightMargin),
				(errorHighlightMargin + bounds.getWidth() + errorHighlightMargin),
				(errorHighlightMargin + bounds.getHeight() + errorHighlightMargin)
			)};
		DisplayExtensionGraphics[] degs = {new DisplayExtensionGraphics(parent, null, page, shapes, errorOutlineColor, errorOutlineStroke, errorHighlightColor) {
			public boolean isActive() {
				return true;
			}
		}};
		return degs;
	}
	
	private static DisplayExtensionGraphics[] getErrorVisualizationGraphics(ImDocumentErrorProtocolDisplay parent, ImDocumentMarkupPanel idmp, ImPage page, ArrayList words) {
		if (words.isEmpty())
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		Shape[] shapes = new Shape[words.size()];
		ImWord prevImw = null;
		ImWord imw = ((ImWord) words.get(0));
		for (int w = 1; w <= words.size(); w++) { // processing 1 behind loop index to facilitate rolling variables over three word sequence
			ImWord nextImw = ((w < words.size()) ? ((ImWord) words.get(w)) : null);
			int mRight = Math.max(0, Math.min(errorHighlightMargin, (
					areWordsInSequence(imw, nextImw)
					? (nextImw.bounds.left - imw.bounds.right) // distance to next word on same line
					: errorHighlightMargin // make sure margin setting prevails in min()
				)));
			int mLeft = Math.max(0, Math.min(errorHighlightMargin, (
					areWordsInSequence(prevImw, imw)
					? (imw.bounds.left - prevImw.bounds.right - mRight) // distance to previous word on same line, less what we've got coming our way from there
					: errorHighlightMargin // make sure margin setting prevails in min()
				)));
			shapes[w-1] = new Rectangle2D.Float(
					(imw.bounds.left - mLeft),
					(imw.bounds.top - errorHighlightMargin),
					(mLeft + imw.bounds.getWidth() + mRight),
					(errorHighlightMargin + imw.bounds.getHeight() + errorHighlightMargin)
				);
			prevImw = imw;
			imw = nextImw;
		}
		Color outlineColor = errorOutlineColor;
		BasicStroke outlineStroke = errorOutlineStroke;
		if (outlineStroke.getLineWidth() < 1) {
			outlineColor = null;
			outlineStroke = null;
		}
		DisplayExtensionGraphics[] degs = {new DisplayExtensionGraphics(parent, null, page, shapes, outlineColor, outlineStroke, errorHighlightColor) {
			public boolean isActive() {
				return true;
			}
		}};
		return degs;
	}
	private static final boolean areWordsInSequence(ImWord fImw, ImWord sImw) {
		if (fImw == null)
			return false;
		if (sImw == null)
			return false;
		if (fImw.pageId != sImw.pageId)
			return false; // different pages
		if (fImw.bounds.bottom < sImw.centerY)
			return false; // second word below first (most likely line break)
		if (sImw.bounds.bottom < fImw.centerY)
			return false; // second word above first (most likely column break)
		if (sImw.bounds.left < fImw.centerX)
			return false; // same line, wrong order, WTF ?!?
		return true; // these two look good
	}
//	
//	public static void main(String[] args) throws Exception {
//		ImageDocumentErrorManager dem = new ImageDocumentErrorManager();
//		dem.setDataProvider(new PluginDataProviderFileBased(new File("E:/GoldenGATEv3/Plugins/ImageDocumentErrorManagerData"), true, false));
//		dem.init();
//		System.out.println(dem.errorCheckListsByName);
////		dem.loadErrorCheckList(dem.dataProvider.getInputStream("errorCheckData.txt"));
//	}
}
