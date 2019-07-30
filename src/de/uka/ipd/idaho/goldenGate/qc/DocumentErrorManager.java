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
package de.uka.ipd.idaho.goldenGate.qc;

import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol;

/**
 * General interface to be implemented by all plug-ins that handle document
 * error protocols.
 * 
 * @author sautter
 */
public interface DocumentErrorManager {
	
	/** the document attribute holding the total number of errors that require approval */
	public static final String APPROVAL_REQUIRED_ATTRIBUTE_NAME = "approvalRequired";
	
	/** the prefix for document attributes holding the number of errors from individual categories that require approval */
	public static final String APPROVAL_REQUIRED_FOR_ATTRIBUTE_NAME_PREFIX = "approvalRequired_for_";
	
	/** the document attribute holding the name of the user who gave approval */
	public static final String APPROVED_BY_ATTRIBUTE_NAME = "approvedBy";
	
	/**
	 * Retrieve an error protocol with nothing but metadata in it, i.e., error
	 * categories and types with their respective labels and descriptions. Any
	 * actual errors included in the protocol will be ignored.
	 * @return an error protocol with all metadata known to the error manager
	 */
	public abstract DocumentErrorProtocol getErrorMetadata();
}
