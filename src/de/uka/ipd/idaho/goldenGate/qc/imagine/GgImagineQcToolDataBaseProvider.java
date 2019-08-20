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
package de.uka.ipd.idaho.goldenGate.qc.imagine;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;

import de.uka.ipd.idaho.gamta.util.DocumentErrorSummary;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentList;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListBuffer;
import de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol;
import de.uka.ipd.idaho.stringUtils.csvHandler.StringTupel;

/**
 * Plug-in that creates a QC Tool Data Base from command line program
 * parameters or from user input.
 * 
 * @author sautter
 */
public abstract class GgImagineQcToolDataBaseProvider extends AbstractGoldenGatePlugin {
	
	/**
	 * Provide the name of the data source backing the QcToolDataBase
	 * returned from the <code>getDataBase()</code> method, for use in
	 * the UI, specifically in selector prompts.
	 * @return the name of the backing data source
	 */
	public abstract String getDataSourceName();
	
	/**
	 * Provide descriptions for the command line program parameters used by
	 * the data base provider, for use in UI. The returned array should be
	 * at most 80 character for output in the console.
	 * @return an array of parameter descriptions.
	 */
	public abstract String[] getParameterDescriptions();
	
	/**
	 * Create a QcToolDataBase from command line program parameters. If the
	 * required parameters are absent, this method should return null. A
	 * null method argument indicates the provider should prompt the user
	 * for its required parameters.
	 * @param cacheFolder the cache folder for the data base to use
	 * @param params the parameters to use
	 * @return a QcToolDataBase
	 */
	public abstract GgImagineQcToolDataBase getDataBase(File cacheFolder, Properties parameters) throws IOException;
	
	/**
	 * The foundation through which the QC Tool does document IO.
	 * 
	 * @author sautter
	 */
	public static abstract class GgImagineQcToolDataBase {
		DocumentListBuffer docList = null;
		File cacheFolder;
		
		/**
		 * Constructor
		 * @param cacheFolder the cache folder to use for storing temporary
		 *        document entries
		 */
		protected GgImagineQcToolDataBase(File cacheFolder) {
			this.cacheFolder = cacheFolder;
		}
		
		DocumentListBuffer getDocumentList(ProgressMonitor pm) throws IOException {
			if (this.docList == null) {
				DocumentList dl = this.loadDocumentList(pm);
				pm.setInfo("Got document list, caching content ...");
				this.docList = new DocumentListBuffer(dl, pm);
			}
			return this.docList;
		}
		
		/**
		 * Provide a name for the backing data source, for use in the UI.
		 * @return a name for the backing data source
		 */
		protected abstract String getDataSourceName();
		
		/**
		 * Load a list of the documents requiring QC checks from the backing
		 * data source.
		 * @param pm a progress monitor for observing the loading process
		 * @return the document list
		 * @throws IOException
		 */
		protected abstract DocumentList loadDocumentList(ProgressMonitor pm) throws IOException;
		
		/**
		 * Indicate whether or not a field in the document list returned from
		 * <code>loadDocumentList()</code> contains a UTC timestamp.
		 * @param fieldName the name of the field in question
		 * @return true if the field represents a UTC timestamp
		 */
		protected abstract boolean isUtcTimeField(String fieldName);
		
		/**
		 * Contribute buttons to the UI. This method is meant for implementing
		 * classes to provide additional functionality.
		 * @return the buttons to add to a UI using this data base
		 */
		protected abstract JButton[] getButtons();
		
		/**
		 * Provide a summary of the errors in a document.
		 * @param docId the ID of the document in the backing data source
		 * @return the error summary
		 * @throws IOException
		 */
		protected abstract DocumentErrorSummary getErrorSummary(String docId) throws IOException;
		
		/**
		 * Provide a the comprehensive protocol of the errors in a document.
		 * @param docId the ID of the document in the backing data source
		 * @param doc the document proper, if loaded before (may be null)
		 * @return the error protocol
		 * @throws IOException
		 */
		protected abstract ImDocumentErrorProtocol getErrorProtocol(String docId, ImDocument doc) throws IOException;
		
		/**
		 * Check whether or not a given document is editable, base upon the
		 * data available from a document list entry. This is a pre-check to
		 * (likely more costly) calls to <code>loadDocument()</code>.
		 * @param docData the document data to check for counter indications
		 * @return true if the document described by the argument list entry is
		 *        editable
		 */
		protected abstract boolean isDocumentEditable(StringTupel docData);
		
		/**
		 * Load a document from the backing source for editing.
		 * @param docId the ID of the document in the backing data source
		 * @param pm a progress monitor for observing the loading process
		 * @return the document with the argument ID
		 * @throws IOException
		 */
		protected abstract ImDocument loadDocument(String docId, ProgressMonitor pm) throws IOException;
		
		/**
		 * Save a document to the backing source after fixing errors.
		 * @param docId the ID of the document in the backing data source
		 * @param doc the document proper
		 * @param pm a progress monitor for observing the saving process
		 * @throws IOException
		 */
		protected abstract void saveDocument(String docId, ImDocument doc, ProgressMonitor pm) throws IOException;
		
		/**
		 * Close a document in the backing source after done with it.
		 * @param docId the ID of the document in the backing data source
		 * @param doc the document proper
		 * @throws IOException
		 */
		protected abstract void closeDocument(String docId, ImDocument doc) throws IOException;
		
		/**
		 * Obtain (and potentially) create a cache folder for the entries of a
		 * document. If the cache folder docsn't exist and <code>create</code>
		 * is false, this method returns null.
		 * @param docId the ID of the document in the backing data source
		 * @param create create the cache folder if not existing?
		 * @return the cache folder
		 */
		protected File getDocumentCacheFolder(String docId, boolean create) {
			if (this.cacheFolder == null)
				return null;
			File docCacheFolder = new File(this.cacheFolder, docId);
			if (create)
				docCacheFolder.mkdirs();
			return docCacheFolder;
		}
		
		/**
		 * Clean a cache folder after done working with it. This method works
		 * recursively.
		 * @param folder the cache folder to clean
		 */
		protected static void cleanCacheFolder(File folder) {
			File[] folderContent = folder.listFiles();
			for (int c = 0; c < folderContent.length; c++) try {
				if (folderContent[c].isDirectory())
					cleanCacheFolder(folderContent[c]);
				else folderContent[c].delete();
			}
			catch (Throwable t) {
				System.out.println("Error cleaning up cached file '" + folderContent[c].getAbsolutePath() + "': " + t.getMessage());
				t.printStackTrace(System.out);
			}
			try {
				folder.delete();
			}
			catch (Throwable t) {
				System.out.println("Error cleaning up cache folder '" + folder.getAbsolutePath() + "': " + t.getMessage());
				t.printStackTrace(System.out);
			}
		}
	}
}
