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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol;
import de.uka.ipd.idaho.gamta.util.DocumentErrorSummary;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentList;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListElement;
import de.uka.ipd.idaho.goldenGate.qc.imagine.GgImagineQcToolDataBaseProvider;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.util.ImDocumentData;
import de.uka.ipd.idaho.im.util.ImDocumentData.FolderImDocumentData;
import de.uka.ipd.idaho.im.util.ImDocumentData.ImDocumentEntry;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol.ImDocumentError;
import de.uka.ipd.idaho.im.util.ImDocumentIO;
import de.uka.ipd.idaho.stringUtils.csvHandler.StringTupel;

/**
 * QC Tool data base provider working on local folder.
 * 
 * @author sautter
 */
public class FolderQcToolDataBaseProvider extends GgImagineQcToolDataBaseProvider implements LiteratureConstants {
	private JFileChooser fileChooser;
	
	/** zero-argument constructor for class loading */
	public FolderQcToolDataBaseProvider() {}
	
	public String getPluginName() {
		return "IM QcTool Folder Connector";
	}
	
	public String getDataSourceName() {
		return "Local File System";
	}
	
	public String[] getParameterDescriptions() {
		String[] pds = {
			"DATA:\tthe path to the data to process",
			"\t- set to a file path to process a singe document",
			"\t- set to a folder path to process all files in that folder"
		};
		return pds;
	}
	
	public GgImagineQcToolDataBase getDataBase(File cacheFolder, Properties parameters) throws IOException {
		
		//	this is what we ultimately need
		File dataPath;
		
		//	we're in interactive mode
		if (parameters == null) {
			if (this.fileChooser == null) {
				this.fileChooser = new JFileChooser();
				this.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				this.fileChooser.setMultiSelectionEnabled(false);
				if (cacheFolder != null)
					this.fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
						public String getDescription() {
							return "Image Markup Files";
						}
						public boolean accept(File file) {
							return (file.isFile() && file.getName().endsWith(".imf"));
						}
					});
				this.fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
					public String getDescription() {
						return "Image Markup Directories";
					}
					public boolean accept(File file) {
						return (file.isFile() && file.getName().endsWith(".imd"));
					}
				});
				this.fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
					public String getDescription() {
						return "Folders Only";
					}
					public boolean accept(File file) {
						return file.isDirectory();
					}
				});
			}
			int choice = this.fileChooser.showOpenDialog(null);
			if (choice != JFileChooser.APPROVE_OPTION)
				return null;
			dataPath = this.fileChooser.getSelectedFile();
		}
		
		//	we're on command line arguments
		else {
			
			//	get parameter
			String dataPathStr = parameters.getProperty("DATA");
			if (dataPathStr == null)
				return null;
			dataPath = new File(dataPathStr);
		}
		
		//	we're all set
		return new FolderQcToolDataBase(cacheFolder, dataPath);
	}
	
	private static class FolderQcToolDataBase extends GgImagineQcToolDataBase {
		private static final String LAST_MODIFIED_TIME = "lastModified";
		private static final String[] documentListFieldNames = {
			DOCUMENT_ID_ATTRIBUTE,
			DOCUMENT_NAME_ATTRIBUTE,
			LAST_MODIFIED_TIME,
			DocumentErrorProtocol.ERROR_COUNT_ATTRIBUTE,
			DocumentErrorProtocol.ERROR_CATEGORY_COUNT_ATTRIBUTE,
			DocumentErrorProtocol.ERROR_TYPE_COUNT_ATTRIBUTE,
			DocumentErrorProtocol.BLOCKER_ERROR_COUNT_ATTRIBUTE,
			DocumentErrorProtocol.CRITICAL_ERROR_COUNT_ATTRIBUTE,
			DocumentErrorProtocol.MAJOR_ERROR_COUNT_ATTRIBUTE,
			DocumentErrorProtocol.MINOR_ERROR_COUNT_ATTRIBUTE
		};
		private File dataFolder;
		private String[] dataFileNames;
		FolderQcToolDataBase(File cacheFolder, File dataPath) throws IOException {
			super(cacheFolder);
			
			//	check if we have a cache
			final boolean gotCacheFolder = (cacheFolder != null);
			if (!gotCacheFolder)
				System.out.println("Warning: cannot process Image Markup Files without cache folder, going directories only");
			
			//	get list of files ('.imf' and '.imd')
			File[] dataFiles = null;
			
			//	get data to upload
			if (dataPath.isDirectory()) {
				this.dataFolder = dataPath;
				dataFiles = this.dataFolder.listFiles(new FileFilter() {
					public boolean accept(File file) {
						return (file.isFile() && ((file.getName().toLowerCase().endsWith(".imf") && gotCacheFolder) || file.getName().toLowerCase().endsWith(".imd")));
					}
				});
			}
			else if ((dataPath.getName().toLowerCase().endsWith(".imf") && gotCacheFolder) || dataPath.getName().toLowerCase().endsWith(".imd")) {
				this.dataFolder = dataPath.getAbsoluteFile().getParentFile();
				dataFiles = new File[1];
				dataFiles[0] = dataPath;
			}
			
			//	collect files with associated error protocol
			ArrayList errorDocNames = new ArrayList();
			for (int f = 0; f < dataFiles.length; f++) {
				String docName = dataFiles[f].getName();
				InputStream epIn = this.getErrorProtocolInputStream(docName);
				if (epIn != null) {
					errorDocNames.add(docName);
					epIn.close();
				}
			}
			
			//	anything to work with?
			if (errorDocNames.isEmpty())
				throw new IOException("Unable to find any documents with error protocols in " + dataPath.getAbsolutePath());
			
			//	store file list
			this.dataFileNames = ((String[]) errorDocNames.toArray(new String[errorDocNames.size()]));
		}
		protected String getDataSourceName() {
			return "Local File System";
		}
		protected JButton[] getButtons() {
			return null;
		}
		protected DocumentList loadDocumentList(ProgressMonitor pm) throws IOException {
			return new DocumentList(documentListFieldNames) {
				private Iterator dfnit = Arrays.asList(dataFileNames).iterator();
				private DocumentListElement next;
				public boolean hasNextDocument() {
					if (this.next != null) return true;
					else if (this.dfnit == null) return false;
					else if (this.dfnit.hasNext()) {
						String dfn = ((String) this.dfnit.next());
						DocumentErrorSummary des;
						try {
							des = getErrorSummary(dfn);
							if (des == null)
								return this.hasNextDocument();
						}
						catch (IOException ioe) {
							System.out.println("Could not load error summary for '" + dfn + "': " + ioe.getMessage());
							ioe.printStackTrace(System.out);
							return this.hasNextDocument();
						}
						
						this.next = new DocumentListElement();
						this.next.setAttribute(DOCUMENT_ID_ATTRIBUTE, dfn);
						this.next.setAttribute(DOCUMENT_NAME_ATTRIBUTE, dfn);
						File docFile = new File(dataFolder, dfn);
						this.next.setAttribute(LAST_MODIFIED_TIME, ("" + docFile.lastModified()));
						
						this.next.setAttribute(DocumentErrorProtocol.ERROR_COUNT_ATTRIBUTE, ("" + des.getErrorCount()));
						this.next.setAttribute(DocumentErrorProtocol.ERROR_CATEGORY_COUNT_ATTRIBUTE, ("" + des.getErrorCategoryCount()));
						this.next.setAttribute(DocumentErrorProtocol.ERROR_TYPE_COUNT_ATTRIBUTE, ("" + des.getErrorTypeCount()));
						this.next.setAttribute(DocumentErrorProtocol.BLOCKER_ERROR_COUNT_ATTRIBUTE, ("" + des.getErrorSeverityCount(ImDocumentError.SEVERITY_BLOCKER)));
						this.next.setAttribute(DocumentErrorProtocol.CRITICAL_ERROR_COUNT_ATTRIBUTE, ("" + des.getErrorSeverityCount(ImDocumentError.SEVERITY_CRITICAL)));
						this.next.setAttribute(DocumentErrorProtocol.MAJOR_ERROR_COUNT_ATTRIBUTE, ("" + des.getErrorSeverityCount(ImDocumentError.SEVERITY_MAJOR)));
						this.next.setAttribute(DocumentErrorProtocol.MINOR_ERROR_COUNT_ATTRIBUTE, ("" + des.getErrorSeverityCount(ImDocumentError.SEVERITY_MINOR)));
						
						this.addListFieldValues(this.next);
						return true;
					}
					else {
						this.dfnit = null;
						return false;
					}
				}
				public DocumentListElement getNextDocument() {
					if (!this.hasNextDocument()) return null;
					DocumentListElement next = this.next;
					this.next = null;
					return next;
				}
				public boolean hasNoSummary(String listFieldName) {
					return true; // no summaries required
				}
				public boolean isNumeric(String listFieldName) {
					return (LAST_MODIFIED_TIME.equals(listFieldName) || listFieldName.endsWith("Count") || listFieldName.startsWith("error"));
				}
				public boolean isFilterable(String listFieldName) {
					return DOCUMENT_NAME_ATTRIBUTE.equals(listFieldName);
				}
			};
		}
		protected boolean isUtcTimeField(String fieldName) {
			return LAST_MODIFIED_TIME.equals(fieldName);
		}
		protected DocumentErrorSummary getErrorSummary(String docId) throws IOException {
			InputStream epIn = this.getErrorProtocolInputStream(docId);
			if (epIn == null)
				return null;
			DocumentErrorSummary des = new DocumentErrorSummary(docId);
			DocumentErrorProtocol.fillErrorProtocol(des, null, epIn);
			return des;
		}
		protected ImDocumentErrorProtocol getErrorProtocol(String docId, ImDocument doc) throws IOException {
			ImDocumentErrorProtocol idep = new ImDocumentErrorProtocol(doc);
			InputStream epIn = this.getErrorProtocolInputStream(docId);
			ImDocumentErrorProtocol.fillErrorProtocol(idep, epIn);
			return idep;
		}
		private InputStream getErrorProtocolInputStream(String docId) throws IOException {
			File errorProtocolFile = new File(this.dataFolder, (docId + ".ep.zip"));
			if (errorProtocolFile.exists()) {
				ZipInputStream zin = new ZipInputStream(new FileInputStream(errorProtocolFile));
				zin.getNextEntry();
				return zin;
			}
			else if (docId.endsWith(".imd")) {
				ImDocumentData docData = new FolderImDocumentData(new File(this.dataFolder, (docId + "ir")), null);
				if (docData.hasEntry(ImDocumentErrorProtocol.errorProtocolSupplementName))
					docData.getInputStream(ImDocumentErrorProtocol.errorProtocolSupplementName);
			}
			return null;
		}
		protected boolean isDocumentEditable(StringTupel docData) {
			return true;
		}
		protected ImDocument loadDocument(String docId, ProgressMonitor pm) throws IOException {
			if (docId.endsWith(".imd")) {
				File docFile = new File(this.dataFolder, (docId + "ir"));
				return ImDocumentIO.loadDocument(docFile, pm);
			}
			else {
				File docFile = new File(this.dataFolder, docId);
				InputStream docIn = new BufferedInputStream(new FileInputStream(docFile));
				ImDocument doc = ImDocumentIO.loadDocument(docIn, this.getDocumentCacheFolder(docId, true), pm, docFile.length());
				docIn.close();
				return doc;
			}
		}
		protected void saveDocument(String docId, ImDocument doc, ProgressMonitor pm) throws IOException {
			
			//	create output file and make way
			File docFile = new File(this.dataFolder, docId);
			if (docFile.exists()) {
				String fileName = docFile.getAbsolutePath();
				docFile.renameTo(new File(fileName + "." + System.currentTimeMillis() + ".old"));
				docFile = new File(fileName);
			}
			
			//	save document to folder, and entry list as file
			if (docId.endsWith(".imd")) {
				File docFolder = new File(docFile.getAbsolutePath() + "ir");
				if (!docFolder.exists())
					docFolder.mkdirs();
				ImDocumentEntry[] docEntries = ImDocumentIO.storeDocument(doc, docFolder, pm);
				BufferedWriter eOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile), "UTF-8"));
				for (int e = 0; e < docEntries.length; e++) {
					eOut.write(docEntries[e].toTabString());
					eOut.newLine();
				}
				eOut.flush();
				eOut.close();
			}
			
			//	save document to zip archive
			else ImDocumentIO.storeDocument(doc, docFile, pm);
			
			//	create error protocol file and make way
			File docEpFile = new File(this.dataFolder, (docId + ".ep.zip"));
			if (docEpFile.exists()) {
				String fileName = docEpFile.getAbsolutePath();
				docEpFile.renameTo(new File(fileName + "." + System.currentTimeMillis() + ".old"));
				docEpFile = new File(fileName);
			}
			
			//	get error protocol
			ImDocumentErrorProtocol idep = ImDocumentErrorProtocol.loadErrorProtocol(doc);
			if (idep == null)
				return;
			if (idep.getErrorCount() == 0)
				return;
			
			//	create ZIP
			File destCreateFile = new File(docEpFile.getParentFile(), (docEpFile.getName() + ".exporting"));
			if (destCreateFile.exists())
				destCreateFile.delete();
			destCreateFile.createNewFile();
			ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destCreateFile)));
			
			//	export error protocol
			ZipEntry epZip = new ZipEntry(ImDocumentErrorProtocol.errorProtocolSupplementName);
			zip.putNextEntry(epZip);
			BufferedWriter epBw = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"));
			
			//	store error protocol
			ImDocumentErrorProtocol.storeErrorProtocol(idep, epBw);
			
			//	finish ZIP
			zip.closeEntry();
			zip.close();
			
			//	activate zip (replace old one with it)
			destCreateFile.renameTo(docEpFile);
		}
		protected void closeDocument(String docId, ImDocument doc) throws IOException {
			File docCacheFolder = this.getDocumentCacheFolder(docId, false);
			if ((docCacheFolder != null) && docCacheFolder.exists())
				cleanCacheFolder(docCacheFolder);
		}
	}
}
