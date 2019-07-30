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

import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.swing.DocumentErrorProtocolDisplay;
import de.uka.ipd.idaho.goldenGate.qc.DocumentErrorManager;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;

/**
 * Error manager specific to Image Markup documents and respective display and
 * editing facilities.
 * 
 * @author sautter
 */
public interface GgImagineErrorManager extends DocumentErrorManager {
	
	/**
	 * Retrieve the error protocol for a given document.
	 * @param doc the document to retrieve the error protocol for
	 * @return the error protocol
	 */
	public abstract ImDocumentErrorProtocol getErrorProtocolFor(ImDocument doc);
	
	/**
	 * Produce a display panel for an error protocol that interacts with a
	 * document markup panel by means of hinting users to the specific error
	 * subjects.
	 * @param idmp the markup panel holding the document
	 * @param idep the document error protocol to use
	 * @param pm a progress monitor to observe any actions taken
	 * @return an error protocol display
	 */
	public abstract DocumentErrorProtocolDisplay getErrorProtocolDisplay(ImDocumentMarkupPanel idmp, ImDocumentErrorProtocol idep, ProgressMonitor pm);
	
	/**
	 * Edit the approval status of a document, to approve it despite pending
	 * errors.
	 * @param idmp the markup panel holding the document
	 * @param pm a progress monitor to observe any actions taken
	 */
	public abstract void approveDocument(ImDocumentMarkupPanel idmp, ProgressMonitor pm);
}
