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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;

import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol.DocumentError;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.imagine.plugins.AbstractGoldenGateImaginePlugin;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentFileExporter;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol;

/**
 * This plug-in exports error protocols from Image Markup documents to ZIP
 * Archives.
 * 
 * @author sautter
 */
public class ImageDocumentErrorProtocolExporter extends AbstractGoldenGateImaginePlugin implements ImageDocumentFileExporter, LiteratureConstants {
	private JFileChooser fileChooser = null;
	private JRadioButton useRaw = null;
	private JRadioButton useTabSeparated = null;
	private JRadioButton usePlainCsv = null;
	private JRadioButton useExcelCsv = null;
	
	/** public zero-argument constructor for class loading */
	public ImageDocumentErrorProtocolExporter() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#getPluginName()
	 */
	public String getPluginName() {
		return "IM Document Error Protocol Exporter";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentExporter#getExportMenuLabel()
	 */
	public String getExportMenuLabel() {
		return "Export Error Protocol";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentExporter#getExportMenuTooltip()
	 */
	public String getExportMenuTooltip() {
		return "Export the error protocol of this document to a ZIP Archive";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentExporter#getHelpText()
	 */
	public String getHelpText() {
		return null; // for now ...
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentExporter#exportDocument(de.uka.ipd.idaho.im.ImDocument, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
	 */
	public void exportDocument(ImDocument doc, ProgressMonitor pm) {
		this.exportDocument(null, doc, pm);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentFileExporter#exportDocument(java.io.File, de.uka.ipd.idaho.im.ImDocument, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
	 */
	public void exportDocument(File likelyDest, ImDocument doc, ProgressMonitor pm) {
		
		//	create file chooser only on demand
		if (this.fileChooser == null) {
			this.fileChooser = new JFileChooser();
			this.fileChooser.addChoosableFileFilter(zipFileFilter);
			if (likelyDest != null)
				this.fileChooser.setSelectedFile(likelyDest);
			
			this.useRaw = new JRadioButton("Raw", false);
			this.useTabSeparated = new JRadioButton("Tab Separated", true);
			this.usePlainCsv = new JRadioButton("CSV", false);
			this.useExcelCsv = new JRadioButton("Excel CSV", false);
			
			ButtonGroup sepButtonGroup = new ButtonGroup();
			sepButtonGroup.add(this.useRaw);
			sepButtonGroup.add(this.useTabSeparated);
			sepButtonGroup.add(this.usePlainCsv);
			sepButtonGroup.add(this.useExcelCsv);
			
			JPanel sepPanel = new JPanel(new GridLayout(0, 1));
			sepPanel.add(new JLabel("Table Export Format"));
			sepPanel.add(this.useRaw);
			sepPanel.add(this.useTabSeparated);
			sepPanel.add(this.usePlainCsv);
			sepPanel.add(this.useExcelCsv);
			sepPanel.setBorder(BorderFactory.createEtchedBorder());
			
			JPanel sepPosPanel = new JPanel(new BorderLayout());
			sepPosPanel.add(sepPanel, BorderLayout.SOUTH);
			
			this.fileChooser.setAccessory(sepPosPanel);
		}
		
		//	select destination via FileChooser
		if (this.fileChooser.showSaveDialog(DialogFactory.getTopWindow()) != JFileChooser.APPROVE_OPTION)
			return;
		File file = this.fileChooser.getSelectedFile();
		if (file.isDirectory())
			return;
		
		//	make sure file name ends with '.zip'
		if (!file.exists() && !file.getName().endsWith(".zip"))
			file = new File(file.getAbsolutePath() + ".zip");
		
		//	get table separator
		char columnSeparator = '\t';
		if (this.usePlainCsv.isSelected())
			columnSeparator = ',';
		else if (this.useExcelCsv.isSelected())
			columnSeparator = ';';
		else if (this.useRaw.isSelected())
			columnSeparator = ((char) 0);
		
		//	do export
		try {
			exportDocument(doc, file, columnSeparator, pm, true);
		}
		catch (IOException ioe) {
			DialogFactory.alert(("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
			ioe.printStackTrace(System.out);
		}
	}
	
	private static final FileFilter zipFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".zip"));
		}
		public String getDescription() {
			return "ZIP Archives";
		}
	};
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentFileExporter#exportDocument(de.uka.ipd.idaho.im.ImDocument, java.io.File, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
	 */
	public File exportDocument(ImDocument doc, File destFile, ProgressMonitor pm) throws IOException {
		return this.exportDocument(doc, destFile, ((char) 0), pm, false);
	}
	
	private File exportDocument(ImDocument doc, File destFile, char columnSeparator, ProgressMonitor pm, boolean allowPrompt) throws IOException {
		
		//	get error protocol
		ImDocumentErrorProtocol idep = ImDocumentErrorProtocol.loadErrorProtocol(doc);
		if (idep == null) {
			if (allowPrompt) {
				DialogFactory.alert("The document cannot be exported yet because there is no error protocol.", "Cannot Export Document", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			else throw new IOException("No error protocol to export.");
		}
		
		//	check for actual errors
		if (idep.getErrorCount() == 0) {
			if (allowPrompt) {
				DialogFactory.alert("The document cannot be exported because there are no errors on protocol.", "Cannot Export Document", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			else throw new IOException("No errors in protocol to export.");
		}
		
		//	make sure file name ends with '.zip'
		if (!destFile.getName().endsWith(".ep.zip"))
			destFile = new File(destFile.getAbsolutePath() + ".ep.zip");
		
		//	create ZIP
		pm.setStep("Preparing export file");
		pm.setBaseProgress(0);
		pm.setMaxProgress(5);
		File destCreateFile = new File(destFile.getParentFile(), (destFile.getName() + ".exporting"));
		if (destCreateFile.exists())
			destCreateFile.delete();
		destCreateFile.createNewFile();
		ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destCreateFile)));
		
		//	export error protocol
		pm.setStep("Exporting error protocol");
		pm.setBaseProgress(5);
		pm.setMaxProgress(95);
		ZipEntry epZip = new ZipEntry("errorProtocol." + (((columnSeparator == ',') || (columnSeparator == ';')) ? "csv" : "txt"));
		zip.putNextEntry(epZip);
		BufferedWriter epBw = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"));
		
		//	store only label data on manual export
		if (columnSeparator != 0) {
			String[] categories = idep.getErrorCategories();
			for (int c = 0; c < categories.length; c++) {
				pm.setProgress((c * 100) / categories.length);
				if (idep.getErrorCount(categories[c]) == 0)
					continue;
				
				//	store category proper
				epBw.write(quoteValue(idep.getErrorCategoryLabel(categories[c]), columnSeparator));
				epBw.write(columnSeparator + quoteValue(idep.getErrorCategoryDescription(categories[c]), columnSeparator));
				epBw.newLine();
				
				//	store error types in current category
				String[] types = idep.getErrorTypes(categories[c]);
				for (int t = 0; t < types.length; t++) {
					if (idep.getErrorCount(categories[c], types[t]) == 0)
						continue;
					
					//	store type proper
					epBw.write(quoteValue(idep.getErrorTypeLabel(categories[c], types[t]), columnSeparator));
					epBw.write(columnSeparator + quoteValue(idep.getErrorTypeDescription(categories[c], types[t]), columnSeparator));
					epBw.newLine();
					
					//	store actual errors
					DocumentError[] errors = idep.getErrors(categories[c], types[t]);
					for (int e = 0; e < errors.length; e++) {
						epBw.write(quoteValue(errors[e].severity, columnSeparator));
						epBw.write(columnSeparator + quoteValue(errors[e].description, columnSeparator));
						epBw.newLine();
					}
					
					//	add spacer after error type
					epBw.newLine();
				}
				
				//	add spacer after error category
				epBw.newLine();
			}
			epBw.flush();
		}
		
		//	store whole protocol on batch export
		else ImDocumentErrorProtocol.storeErrorProtocol(idep, epBw);
		
		//	finish ZIP
		zip.closeEntry();
		zip.close();
		
		//	activate zip (replace old one with it)
		pm.setStep("Finishing export");
		pm.setBaseProgress(95);
		pm.setMaxProgress(100);
		if (destFile.exists()) {
			String destFileName = destFile.getAbsolutePath();
			destFile.renameTo(new File(destFileName + "." + System.currentTimeMillis() + ".old"));
			destFile = new File(destFileName);
		}
		destCreateFile.renameTo(destFile);
		pm.setProgress(100);
		
		//	finally ...
		return destFile;
	}
	
	private static String quoteValue(String value, char columnSeparator) {
		if (columnSeparator == '\t')
			return value;
		StringBuffer quotValue = new StringBuffer();
		quotValue.append('"');
		for (int c = 0; c < value.length(); c++) {
			char ch = value.charAt(c);
			if (ch == '"')
				quotValue.append('"');
			quotValue.append(ch);
		}
		quotValue.append('"');
		return quotValue.toString();
	}
}