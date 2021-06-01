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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.easyIO.utilities.ApplicationHttpsEnabler;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol;
import de.uka.ipd.idaho.gamta.util.DocumentErrorProtocol.DocumentError;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.gamta.util.swing.DocumentErrorProtocolDisplay;
import de.uka.ipd.idaho.gamta.util.swing.DocumentListPanel;
import de.uka.ipd.idaho.gamta.util.swing.ProgressMonitorDialog;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListBuffer;
import de.uka.ipd.idaho.goldenGate.GoldenGateConfiguration;
import de.uka.ipd.idaho.goldenGate.GoldenGateConstants;
import de.uka.ipd.idaho.goldenGate.configuration.AbstractConfiguration;
import de.uka.ipd.idaho.goldenGate.configuration.FileConfiguration;
import de.uka.ipd.idaho.goldenGate.configuration.UrlConfiguration;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePluginDataProvider;
import de.uka.ipd.idaho.goldenGate.qc.imagine.GgImagineQcToolDataBaseProvider.GgImagineQcToolDataBase;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagineConstants;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentIoProvider;
import de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupDialog;
import de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupUI.ImageDocumentEditorTab;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol;
import de.uka.ipd.idaho.im.util.ImDocumentErrorProtocol.ImDocumentError;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.stringUtils.StringVector;
import de.uka.ipd.idaho.stringUtils.csvHandler.StringTupel;

/**
 * Command line tool for mass upload of IMFs to GG Server IMS.
 * 
 * @author sautter
 */
public class GgImagineQcTool implements GoldenGateImagineConstants, LiteratureConstants {
	private static final String BASE_PATH_PARAMETER = "PATH";
	private static final String CONFIG_PATH_PARAMETER = "CONF";
	private static final String CACHE_PATH_PARAMETER = "CACHE";
	
	private static final String HELP_PARAMETER = "HELP";
	
	private static final String LOG_TIMESTAMP_DATE_FORMAT = "yyyyMMdd-HHmm";
	private static final DateFormat LOG_TIMESTAMP_FORMATTER = new SimpleDateFormat(LOG_TIMESTAMP_DATE_FORMAT);
	
	private static File BASE_PATH = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		//	adjust basic parameters
		String basePath = ".";
		String configPath = "GgImagineQcTool.cnfg";
		String cachePath = null;
		String logPath = null;
		String logFileName = ("GgImagineQcTool." + LOG_TIMESTAMP_FORMATTER.format(new Date()) + ".log");
		Properties parameters = new Properties();
		boolean printHelp = false;
		
		//	parse remaining args
		for (int a = 0; a < args.length; a++) {
			if (args[a] == null)
				continue;
			if (args[a].startsWith(BASE_PATH_PARAMETER + "="))
				basePath = args[a].substring((BASE_PATH_PARAMETER + "=").length());
			else if (args[a].startsWith(CONFIG_PATH_PARAMETER + "="))
				configPath = args[a].substring((CONFIG_PATH_PARAMETER + "=").length());
			else if (args[a].startsWith(CACHE_PATH_PARAMETER + "="))
				cachePath = args[a].substring((CACHE_PATH_PARAMETER + "=").length());
			else if (args[a].equals(LOG_PARAMETER + "=IDE"))
				logFileName = null;
			else if (args[a].startsWith(LOG_PARAMETER + "="))
				logPath = args[a].substring((LOG_PARAMETER + "=").length());
			else if (args[a].equals(HELP_PARAMETER)) {
				printHelp = true;
				break;
			}
			else if (args[a].indexOf('=') != -1) {
				String pn = args[a].substring(0, args[a].indexOf('=')).trim();
				String pv = args[a].substring(args[a].indexOf('=') + "=".length()).trim();
				if ((pn.length() != 0) && (pv.length() != 0))
					parameters.setProperty(pn, pv);
			}
		}
		
		//	print help and exit if asked to
		if (printHelp) {
			System.out.println("GoldenGATE Imagine Uploader can take the following parameters:");
			System.out.println("");
			System.out.println("PATH:\tthe folder to run GoldenGATE Imagine QcTool in (defaults to the\r\n\tinstallation folder)");
			System.out.println("CONF:\tthe (path and) name of the configuration file to run GoldenGATE\r\n\tImagine QC Tool with (defaults to 'GgImagineQcTool.cnfg' in the folder\r\n\tGoldenGATE Imagine QC Tool is running in)");
			System.out.println("CACHE:\tthe root folder for all data caching folders (defaults to the path\r\n\tfolder, useful for directing caching to a RAM disc, etc.)");
			System.out.println("HELP:\tprint this help text");
			return;
		}
		
		//	remember program base path
		BASE_PATH = new File(basePath).getAbsoluteFile();
		
		//	enable HTTPS (just in case)
		File certFolder = new File(BASE_PATH, "HttpsCerts");
		certFolder.mkdirs();
		ApplicationHttpsEnabler https = new ApplicationHttpsEnabler(certFolder, true, false);
		https.init();
		
		//	load GoldenGATE Imagine specific settings
		File configFile;
		if (configPath.startsWith("/") || (configPath.indexOf(":\\") != -1) || (configPath.indexOf(":/") != -1))
			configFile = new File(configPath);
		else configFile = new File(BASE_PATH, configPath);
		Settings config = Settings.loadSettings(configFile);
		
		//	check for config file specified cache root and log path
		if (cachePath == null)
			cachePath = config.getSetting("cacheFolder");
		if ((logPath == null) && (logFileName != null))
			logPath = config.getSetting("logFolder");
		if ((logPath == null) && (logFileName != null))
			logPath = LOG_FOLDER_NAME;
		
		//	use cache path for un-zipping IMFs and for buffering server loaded documents
		File cacheFolder = null;
		if (cachePath != null) {
			if (cachePath.startsWith("./"))
				cachePath = cachePath.substring("./".length());
			if (!cachePath.startsWith("/") && (cachePath.indexOf(":") == -1))
				cacheFolder = new File(new File(basePath), cachePath);
			else cacheFolder = new File(cachePath);
			if (cacheFolder.exists() && !cacheFolder.isDirectory())
				cacheFolder = null;
			else cacheFolder.mkdirs();
		}
		
		//	set look & feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		
		//	create log files if required
		File logFolder = null;
		File logFileOut = null;
		File logFileErr = null;
		if (logFileName != null) try {
			
			//	truncate log file extension
			if (logFileName.endsWith(".log"))
				logFileName = logFileName.substring(0, (logFileName.length() - ".log".length()));
			
			//	create absolute log files
			if (logFileName.startsWith("/") || (logFileName.indexOf(':') != -1)) {
				logFileOut = new File(logFileName + ".out.log");
				logFileErr = new File(logFileName + ".err.log");
				logFolder = logFileOut.getAbsoluteFile().getParentFile();
			}
			
			//	create relative log files (the usual case)
			else {
				
				//	get log path
				if (logPath.startsWith("/") || (logPath.indexOf(':') != -1))
					logFolder = new File(logPath);
				else logFolder = new File(BASE_PATH, logPath);
				logFolder = logFolder.getAbsoluteFile();
				logFolder.mkdirs();
				
				//	create log files
				logFileOut = new File(logFolder, (logFileName + ".out.log"));
				logFileErr = new File(logFolder, (logFileName + ".err.log"));
			}
			
			//	redirect System.out
			logFileOut.getParentFile().mkdirs();
			logFileOut.createNewFile();
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFileOut)), true, "UTF-8"));
			
			//	redirect System.err
			logFileErr.getParentFile().mkdirs();
			logFileErr.createNewFile();
			System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFileErr)), true, "UTF-8"));
		}
		catch (Exception e) {
			DialogFactory.alert("Could not create log files in folder '" + logFolder.getAbsolutePath() + "'." +
					"\nCommon reasons are a full hard drive, lack of write permissions to the folder, or system protection software." +
					"\nEdit the 'logFolder' entry in 'GgImagineQcTool.cnfg' to specify a different log location." +
					"\nThen exit and re-start GoldenGATE Imagine QC Tool to apply the change." +
					"\n\nNote that you can work normally without the log files, it's just that in case of an error, there are" +
					"\nno log files to to help investigate what exactly went wrong and help developers fix the problem.", "Error Creating Log Files", JOptionPane.ERROR_MESSAGE);
		}
		
		//	create progress monitor
		final ProgressMonitorDialog pmd = new ProgressMonitorDialog(false, true, null, "Loading Document List ...");
		pmd.setAbortExceptionMessage("ABORTED BY USER");
		pmd.setInfoLineLimit(1);
		pmd.getWindow().setSize(400, 150);
		pmd.getWindow().setLocationRelativeTo(null);
		
		//	use configuration specified in settings (default to 'Default.imagine' for now)
		String ggiConfigurationName = config.getSetting("configName");
		
		//	get GoldenGATE Imagine configuration
		GoldenGateConfiguration ggiConfiguration = null;
		
		//	local master configuration selected
		if (ggiConfigurationName == null)
			ggiConfiguration = new FileConfiguration("Local Master Configuration", BASE_PATH, true, true, null);
		
		//	other local configuration selected
		else if (ggiConfigurationName.startsWith("http://") || ggiConfigurationName.startsWith("https://"))
			ggiConfiguration = new UrlConfiguration(ggiConfigurationName);
		
		//	remote configuration selected
		else ggiConfiguration = new FileConfiguration(ggiConfigurationName, new File(new File(BASE_PATH, GoldenGateConstants.CONFIG_FOLDER_NAME), ggiConfigurationName), false, true, null);
		
		//	load GoldenGATE Imagine specific settings
		final Settings ggiConfig = Settings.loadSettings(new File(BASE_PATH, "GgImagine.cnfg"));
		
		//	wrap configuration to filter out ImageDocumentIoProvider implementations (IO happens exclusively via data base) and grab error manager
		QcToolConfiguration qctConfiguration = new QcToolConfiguration(ggiConfiguration);
		
		//	load GoldenGTAE Imagine core
		GoldenGateImagine ggImagine = GoldenGateImagine.openGoldenGATE(qctConfiguration, BASE_PATH, true);
		
		//	get document error manager
		GgImagineErrorManager errorMgr = qctConfiguration.getErrorManager();
		if (errorMgr == null) {
			System.out.println("Cannot work without Error Manager, please check configuration " + qctConfiguration.getName());
			System.exit(0);
		}
		
		//	get data base providers
		GgImagineQcToolDataBaseProvider[] dataBaseProviders = qctConfiguration.getDataBaseProviders();
		if ((dataBaseProviders == null) || (dataBaseProviders.length == 0)) {
			System.out.println("Cannot work without Data Base Provider, please check configuration " + qctConfiguration.getName());
			System.exit(0);
		}
		
		//	create base
		GgImagineQcToolDataBase db = null;
		
		//	try parameter base approach first
		for (int p = 0; p < dataBaseProviders.length; p++) {
			db = dataBaseProviders[p].getDataBase(cacheFolder, parameters); // if that throws an exception, parameters are partial, so let it pass
			if (db != null)
				break;
		}
		
		//	prompt use if parameters don't cut it
		if (db == null) {
			GgImagineQcToolDataBaseProvider useDbp;
			if (dataBaseProviders.length == 1)
				useDbp = dataBaseProviders[0];
			else {
				QcToolDbProviderTray[] dbpTrays = new QcToolDbProviderTray[dataBaseProviders.length];
				for (int p = 0; p < dataBaseProviders.length; p++)
					dbpTrays[p] = new QcToolDbProviderTray(dataBaseProviders[p]);
				JComboBox dbpSelector = new JComboBox(dbpTrays);
				dbpSelector.setBorder(BorderFactory.createLoweredBevelBorder());
				dbpSelector.setEditable(false);
				JPanel dbpPanel = new JPanel(new BorderLayout(), true);
				dbpPanel.add(new JLabel("Connect To "), BorderLayout.WEST);
				dbpPanel.add(dbpSelector, BorderLayout.CENTER);
				int choice = DialogFactory.confirm(dbpPanel, "Parameter Based Data Locatization Failed, Choose Data Base", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				useDbp = ((choice == JOptionPane.OK_OPTION) ? ((QcToolDbProviderTray) dbpSelector.getSelectedItem()).dbp : null);
			}
			if (useDbp != null)
				db = useDbp.getDataBase(cacheFolder, null);
		}
		if (db == null) {
			System.out.println("Cannot work without Data Base, please check prameters and configuration " + qctConfiguration.getName());
			System.out.println("");
			System.out.println("GoldenGATE Imagine Uploader can take the following parameters:");
			System.out.println("");
			System.out.println("PATH:\tthe folder to run GoldenGATE Imagine QcTool in (defaults to the\r\n\tinstallation folder)");
			System.out.println("CONF:\tthe (path and) name of the configuration file to run GoldenGATE\r\n\tImagine QC Tool with (defaults to 'GgImagineQcTool.cnfg' in the folder\r\n\tGoldenGATE Imagine QC Tool is running in)");
			System.out.println("CACHE:\tthe root folder for all data caching folders (defaults to the path\r\n\tfolder, useful for directing caching to a RAM disc, etc.)");
			System.out.println("HELP:\tprint this help text");
			System.out.println("");
			System.out.println("The installed DataBaseProviders take the following arguments:");
			for (int p = 0; p < dataBaseProviders.length; p++) {
				System.out.println("");
				System.out.println("Provider connecting to " + dataBaseProviders[p].getDataSourceName() + ":");
				String[] dbpHelp = dataBaseProviders[p].getParameterDescriptions();
				for (int l = 0; l < dbpHelp.length; l++)
					System.out.println(dbpHelp[l]);
			}
			System.exit(0);
		}
		final GgImagineQcToolDataBase dataBase = db;
		
		//	load document list in separate thread
		final DocumentListBuffer[] docList = {null};
		Thread dll = new Thread() {
			public void run() {
				
				//	wait for progress monitor to show
				while (!pmd.getWindow().isVisible()) try {
					sleep(10);
				} catch (InterruptedException ie) {}
				
				//	open document
				try {
					docList[0] = dataBase.getDocumentList(pmd);
				}
				catch (RuntimeException re) {
					if (!"ABORTED BY USER".equals(re.getMessage()))
						throw re;
				}
				catch (IOException ioe) {
					DialogFactory.alert(("An error occurred while loading the document list:\n" + ioe.getMessage()), ("Error Loading Document List"), JOptionPane.ERROR_MESSAGE);
				}
				finally {
					pmd.close();
				}
			}
		};
		dll.start();
		pmd.popUp(true);
		
		//	did we get that list?
		if (docList[0] == null) {
			return;
		}
		
		//	open document list UI
		QcToolUI qctUi = new QcToolUI(config, dataBase, docList[0], ggImagine, ggiConfig, errorMgr);
		qctUi.setIconImage(qctConfiguration.getIconImage());
		qctUi.setVisible(true);
		
		//	store configuration and exit when window closed
		qctUi.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent we) {
				try {
					Settings.storeSettingsAsText(new File(BASE_PATH, "GgImagine.cnfg"), ggiConfig);
				}
				catch (IOException ioe) {
					ioe.printStackTrace(System.out);
				}
				System.exit(0);
			}
		});
	}
	
	private static class QcToolConfiguration extends AbstractConfiguration {
		private GoldenGateConfiguration config;
		private GoldenGatePlugin[] plugins = null;
		private GgImagineErrorManager errorManager;
		private GgImagineQcToolDataBaseProvider[] dataBaseProviders;
		QcToolConfiguration(GoldenGateConfiguration config) {
			super(config.getName());
			this.config = config;
		}
		public String getPath() {
			return this.config.getPath();
		}
		public String getAbsolutePath() {
			return this.config.getAbsolutePath();
		}
		public boolean isDataEditable() {
			return this.config.isDataEditable();
		}
		public boolean allowWebAccess() {
			return this.config.allowWebAccess();
		}
		public boolean isDataAvailable(String dataName) {
			return this.config.isDataAvailable(dataName);
		}
		public InputStream getInputStream(String dataName) throws IOException {
			return this.config.getInputStream(dataName);
		}
		public URL getURL(String dataName) throws IOException {
			return this.config.getURL(dataName);
		}
		public boolean isDataEditable(String dataName) {
			return this.config.isDataEditable(dataName);
		}
		public OutputStream getOutputStream(String dataName) throws IOException {
			return this.config.getOutputStream(dataName);
		}
		public boolean deleteData(String dataName) {
			return this.config.deleteData(dataName);
		}
		public String[] getDataNames() {
			return this.config.getDataNames();
		}
		public void finalize() throws Throwable {
			this.config.finalize();
		}
		public void writeLog(String entry) {
			this.config.writeLog(entry);
		}
		public String getName() {
			return this.config.getName();
		}
		public boolean isMasterConfiguration() {
			return this.config.isMasterConfiguration();
		}
		public JMenuItem[] getFileMenuItems() {
			return this.config.getFileMenuItems();
		}
		public JMenuItem[] getWindowMenuItems() {
			return this.config.getWindowMenuItems();
		}
		public Settings getSettings() {
			return this.config.getSettings();
		}
		public void storeSettings(Settings settings) throws IOException {
			this.config.storeSettings(settings);
		}
		public GoldenGatePluginDataProvider getHelpDataProvider() {
			return this.config.getHelpDataProvider();
		}
		public Image getIconImage() {
			return this.config.getIconImage();
		}
		public GoldenGatePlugin[] getPlugins() {
			if (this.plugins != null)
				return this.plugins;
			GoldenGatePlugin[] plugins = this.config.getPlugins();
			ArrayList pluginList = new ArrayList();
			ArrayList dataBaseProviders = new ArrayList();
			for (int p = 0; p < plugins.length; p++) {
				if (plugins[p] instanceof ImageDocumentIoProvider)
					continue; // IO happens exclusively via data base
				//	TODO filter anything else?
				if (plugins[p] instanceof GgImagineErrorManager)
					this.errorManager = ((GgImagineErrorManager) plugins[p]);
				if (plugins[p] instanceof GgImagineQcToolDataBaseProvider)
					dataBaseProviders.add(plugins[p]);
				//	TODO need anything else?
				pluginList.add(plugins[p]);
			}
			this.plugins = ((GoldenGatePlugin[]) pluginList.toArray(new GoldenGatePlugin[pluginList.size()]));
			this.dataBaseProviders = ((GgImagineQcToolDataBaseProvider[]) dataBaseProviders.toArray(new GgImagineQcToolDataBaseProvider[dataBaseProviders.size()]));
			return this.plugins;
		}
		GgImagineErrorManager getErrorManager() {
			if (this.errorManager == null)
				this.getPlugins();
			return this.errorManager;
		}
		GgImagineQcToolDataBaseProvider[] getDataBaseProviders() {
			if (this.dataBaseProviders == null)
				this.getPlugins();
			return this.dataBaseProviders;
		}
	}
	
	private static class QcToolDbProviderTray {
		final GgImagineQcToolDataBaseProvider dbp;
		 QcToolDbProviderTray(GgImagineQcToolDataBaseProvider dbp) {
			this.dbp = dbp;
		}
		public String toString() {
			return this.dbp.getDataSourceName();
		}
	}
	
	private static class QcToolUI extends JFrame {
		private static final String ERROR_PANEL_LEFT = "on Left of Document";
		private static final String ERROR_PANEL_RIGHT = "on Right of Document";
		private static final String ERROR_PANEL_WINDOW = "in Separate Window";
		private static final String[] errorPanelPositions = {
			ERROR_PANEL_LEFT,
			ERROR_PANEL_RIGHT,
			ERROR_PANEL_WINDOW
		};
		
		private GgImagineQcToolDataBase dataBase;
		
		private GoldenGateImagine ggImagine;
		private Settings ggiConfig;
		private GgImagineErrorManager errorMgrs;
		
		private StringVector listFieldOrder = new StringVector();
		private StringVector listFields = new StringVector();
		
		private Properties listFieldLabels = new Properties();
		
		private QcToolDocumentListPanel docListPanel;
		private DocumentErrorProtocolDisplay docErrorPanel;
		private DocumentError selectedDocError;
		
		private JComboBox errorPanelPosition = new JComboBox(errorPanelPositions);
		
		QcToolUI(Settings config, GgImagineQcToolDataBase dataBase, DocumentListBuffer docList, GoldenGateImagine ggImagine, Settings ggiConfig, GgImagineErrorManager errorMgrs) {
			super("GoldenGATE Imagine QC Tool on " + dataBase.getDataSourceName());
			this.dataBase = dataBase;
			this.ggImagine = ggImagine;
			this.ggiConfig = ggiConfig;
			this.errorMgrs = errorMgrs;
			
			//	set window icon
			this.setIconImage(this.ggImagine.getGoldenGateIcon());
			
			//	read display and fast-fetch configuration
			this.listFieldOrder.parseAndAddElements(config.getSetting("listFieldOrder"), " ");
			this.listFields.parseAndAddElements(config.getSetting("listFields"), " ");
			
			Settings listFieldLabels = config.getSubset("listFieldLabel");
			String[] listFieldNames = listFieldLabels.getKeys();
			for (int f = 0; f < listFieldNames.length; f++)
				this.listFieldLabels.setProperty(listFieldNames[f], listFieldLabels.getSetting(listFieldNames[f], listFieldNames[f]));
			
			//	build document table
			this.docListPanel = new QcToolDocumentListPanel(docList, ("GoldenGATE Imagine QC Tool on " + dataBase.getDataSourceName()));
			
			//	create buttons (open, exit, fast fetch options and read timeout if in server mode)
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			
			if (this.docListPanel.getFilterFieldCount() != 0) {
				JButton filterButton = new JButton("Filter");
				filterButton.setBorder(BorderFactory.createRaisedBevelBorder());
				filterButton.setPreferredSize(new Dimension(100, 21));
				filterButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						docListPanel.filterDocumentList();
					}
				});
				buttonPanel.add(filterButton);
			}
			
			JButton openButton = new JButton("Open");
			openButton.setBorder(BorderFactory.createRaisedBevelBorder());
			openButton.setPreferredSize(new Dimension(100, 21));
			openButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					docListPanel.openSelected();
				}
			});
			buttonPanel.add(openButton);
			
			//	get custom buttons from data base
			JButton[] dbButtons = this.dataBase.getButtons();
			if (dbButtons != null)
				for (int b = 0; b < dbButtons.length; b++) {
					dbButtons[b].setBorder(BorderFactory.createRaisedBevelBorder());
					dbButtons[b].setPreferredSize(new Dimension(100, 21));
					buttonPanel.add(dbButtons[b]);
				}
			
			JButton closeButton = new JButton("Close");
			closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
			closeButton.setPreferredSize(new Dimension(100, 21));
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					dispose();
				}
			});
			buttonPanel.add(closeButton);
			
			JPanel errorPositionPanel = new JPanel(new BorderLayout(), true);
			errorPositionPanel.add(new JLabel("Show Error Protocol "), BorderLayout.WEST);
			errorPositionPanel.add(this.errorPanelPosition, BorderLayout.CENTER);
			buttonPanel.add(errorPositionPanel);
			
			//	add error protocol
			this.docErrorPanel = new DocumentErrorProtocolDisplay(null, true) {
				protected void errorSelected(DocumentError error) {
					selectedDocError = error;
				}
			};
			
			//	put the whole stuff together
			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add(this.docListPanel, BorderLayout.CENTER);
			this.getContentPane().add(this.docErrorPanel, BorderLayout.EAST);
			this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			this.setSize(1200, 800);
			this.setLocationRelativeTo(null);
		}
		
		void selectDocument(String docId, String docName) {
			ImDocumentErrorProtocol idep = null;
			try {
				idep = dataBase.getErrorProtocol(docId, null);
			}
			catch (IOException ioe) {
				System.out.println("Failed to load error protocol for document '" + docName + "' (" + docId + "): " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
			this.docErrorPanel.setErrorProtocol(idep);
		}
		
		void openDocument(final String docId, final String docName) {
			
			//	create progress monitor
			final ProgressMonitorDialog pmd = new ProgressMonitorDialog(false, true, this, "Loading Document " + docName + " ...");
			pmd.setAbortExceptionMessage("ABORTED BY USER");
			pmd.setInfoLineLimit(1);
			pmd.getWindow().setSize(400, 150);
			pmd.getWindow().setLocationRelativeTo(this);
			
			//	load in separate thread
			final ImDocument[] doc = {null};
			Thread dl = new Thread() {
				public void run() {
					
					//	wait for progress monitor to show
					while (!pmd.getWindow().isVisible()) try {
						sleep(10);
					} catch (InterruptedException ie) {}
					
					//	open document
					try {
						pmd.setStep("Loading document '" + docName + "'");
						doc[0] = dataBase.loadDocument(docId, pmd);
						QcToolUI.this.ggImagine.notifyDocumentOpened(doc[0], dataBase, pmd);
					}
					catch (RuntimeException re) {
						if (!"ABORTED BY USER".equals(re.getMessage()))
							throw re;
					}
					catch (IOException ioe) {
						ioe.printStackTrace(System.out);
						DialogFactory.alert(("An error occurred while loading document '" + docName + "':\n" + ioe.getMessage()), ("Error Loading Document"), JOptionPane.ERROR_MESSAGE);
					}
					finally {
						pmd.close();
					}
				}
			};
			dl.start();
			pmd.popUp(true);
			
			//	do we have what we came for?
			if (doc[0] == null)
				return;
			
			//	get error panel position
			Object selErrorPos = this.errorPanelPosition.getSelectedItem();
			String errorPos;
			if (ERROR_PANEL_LEFT.equals(selErrorPos))
				errorPos = BorderLayout.WEST;
			else if (ERROR_PANEL_RIGHT.equals(selErrorPos))
				errorPos = BorderLayout.EAST;
			else errorPos = null;
			
			//	open GGI dialog
			QcToolDoumentEditorTab docTab = new QcToolDoumentEditorTab(doc[0], docId, docName, this.ggImagine, this.ggiConfig);
			QcToolDocumentMarkupDialog docDialog = new QcToolDocumentMarkupDialog(this.ggImagine, this.ggiConfig, this.errorMgrs, errorPos, docTab);
			docDialog.setVisible(true);
			
			//	close document in data base
			try {
				this.dataBase.closeDocument(docId, doc[0]);
				this.ggImagine.notifyDocumentClosed(docId);
				doc[0].dispose();
			}
			catch (IOException ioe) {
				ioe.printStackTrace(System.out);
				DialogFactory.alert(("An error occurred while cleaning up document '" + docName + "':\n" + ioe.getMessage()), ("Error Loading Document"), JOptionPane.ERROR_MESSAGE);
			}
		}
		
		private class QcToolDoumentEditorTab extends ImageDocumentEditorTab {
			final ImDocument doc;
			final String docId;
			QcToolDoumentEditorTab(ImDocument doc, String docId, String docName, GoldenGateImagine ggImagine, Settings ggiConfig) {
				super(doc, docName, ggImagine, ggiConfig);
				this.doc = doc;
				this.docId = docId;
			}
		}
		
		private int errorSplitRatio = -1;
		private Dimension docDialogSize = null;
		private Point docDialogPos = null;
		private int docRenderingDpi = -1;
		private int docSideBySidePages = -1;
		private Dimension errorDialogSize = new Dimension(500, 500);
		private Point errorDialogPos = null;
		private class QcToolDocumentMarkupDialog extends ImageDocumentMarkupDialog {
			private DocumentErrorProtocolDisplay errorDisplay;
			private DialogPanel errorDialog;
			private JSplitPane errorSplit;
			QcToolDocumentMarkupDialog(GoldenGateImagine ggImagine, Settings ggiConfig, final GgImagineErrorManager errorMgr, String errorPos, QcToolDoumentEditorTab docTab) {
				super(ggImagine, ggiConfig, docTab);
				final ImDocumentMarkupPanel idmp = docTab.getMarkupPanel();
				
				//	obtain error protocol and display panel from document error manager (we implicitly depend on that thing anyway ...)
				ImDocumentErrorProtocol idep = errorMgr.getErrorProtocolFor(docTab.doc);
				this.errorDisplay = errorMgr.getErrorProtocolDisplay(idmp, idep, ProgressMonitor.dummy);
				
				//	add buttons
				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
				
				//	add Approve, Save, Save & Close, Close, and Cancel button at button
				JButton approveButton = new JButton("Approve");
				approveButton.setBorder(BorderFactory.createRaisedBevelBorder());
				approveButton.setPreferredSize(new Dimension(100, 21));
				approveButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						try {
							idmp.beginAtomicAction("Approve Document");
							errorMgr.approveDocument(idmp, ProgressMonitor.dummy);
						}
						finally {
							idmp.endAtomicAction();
						}
					}
				});
				buttonPanel.add(approveButton);
				
				JButton saveButton = new JButton("Save");
				saveButton.setBorder(BorderFactory.createRaisedBevelBorder());
				saveButton.setPreferredSize(new Dimension(100, 21));
				saveButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						QcToolDocumentMarkupDialog.this.getMarkupUi().getActiveDocument().save();
					}
				});
				buttonPanel.add(saveButton);
				
				JButton saveCloseButton = new JButton("Save & Close");
				saveCloseButton.setBorder(BorderFactory.createRaisedBevelBorder());
				saveCloseButton.setPreferredSize(new Dimension(100, 21));
				saveCloseButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						QcToolDocumentMarkupDialog.this.getMarkupUi().getActiveDocument().save();
						QcToolDocumentMarkupDialog.this.getMarkupUi().close();
					}
				});
				buttonPanel.add(saveCloseButton);
				
				JButton closeButton = new JButton("Close");
				closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
				closeButton.setPreferredSize(new Dimension(100, 21));
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						QcToolDocumentMarkupDialog.this.getMarkupUi().close();
					}
				});
				buttonPanel.add(closeButton);
				
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
				cancelButton.setPreferredSize(new Dimension(100, 21));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						QcToolDocumentMarkupDialog.this.getMarkupUi().getActiveDocument().dispose(true);
						QcToolDocumentMarkupDialog.this.dispose();
					}
				});
				buttonPanel.add(cancelButton);
				
				if (this.errorDisplay != null) {
					if (BorderLayout.WEST.equals(errorPos) || BorderLayout.EAST.equals(errorPos)) {
//						this.add(this.errorDisplay, errorPos);
						JComponent leftPanel;
						JComponent rightPanel;
						if (BorderLayout.WEST.equals(errorPos)) {
							leftPanel = this.errorDisplay;
							rightPanel = this.getMarkupUI();
						}
						else {
							leftPanel = this.getMarkupUI();
							rightPanel = this.errorDisplay;
						}
						this.errorSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
						this.errorSplit.setDividerSize(5);
						this.errorSplit.setDividerLocation(errorSplitRatio); // setting to -1 actually uses preferred component sizes, which is ideal
						this.errorSplit.setResizeWeight(BorderLayout.WEST.equals(errorPos) ? 0 : 1); // keep error protocol width stable, resizing only document
						this.add(this.errorSplit, BorderLayout.CENTER);
					}
					else {
						this.errorDialog = new DialogPanel(this.getDialog(), "Document Error Protocol", false);
						this.errorDialog.setDefaultCloseOperation(HIDE_ON_CLOSE);
						this.errorDialog.add(this.errorDisplay, BorderLayout.CENTER);
						this.errorDialog.setSize(errorDialogSize);
						if (errorDialogPos == null)
							this.errorDialog.setLocationRelativeTo(QcToolUI.this);
						else this.errorDialog.setLocation(errorDialogPos);
						
						//	add button for re-opening error dialog
						final JButton errorsButton = new JButton("Error Protocol");
						errorsButton.setBorder(BorderFactory.createRaisedBevelBorder());
						errorsButton.setPreferredSize(new Dimension(100, 21));
						errorsButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								errorDialog.setVisible(true);
							}
						});
						buttonPanel.add(errorsButton);
						
						//	make sure to activate and de-active button when error window is hidden or shown, respectively
						this.errorDialog.getDialog().addComponentListener(new ComponentAdapter() {
							public void componentHidden(ComponentEvent ce) {
								errorsButton.setEnabled(true);
								errorDialogSize = errorDialog.getSize();
								errorDialogPos = errorDialog.getLocation();
							}
							public void componentShown(ComponentEvent ce) {
								errorsButton.setEnabled(false);
							}
						});
					}
				}
				
				this.add(buttonPanel, BorderLayout.SOUTH);
				
				//	make sure of a controlled exit
				this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
				
				//	remember size and position
				this.addWindowListener(new WindowAdapter() {
					public void windowOpened(WindowEvent we) {
						if (selectedDocError == null) {
							errorDisplay.setErrorType(docErrorPanel.getErrorCategory(), docErrorPanel.getErrorType());
							errorDisplay.setSeverityFilterState(docErrorPanel.getSeverityFilterState());
						}
						else errorDisplay.setError(selectedDocError);
						if (errorDialog != null)
							errorDialog.setVisible(true);
					}
					public void windowClosing(WindowEvent we) {
						docDialogSize = QcToolDocumentMarkupDialog.this.getSize();
						docDialogPos = QcToolDocumentMarkupDialog.this.getLocation();
						docRenderingDpi = idmp.getRenderingDpi();
						docSideBySidePages = idmp.getSideBySidePages();
						if (errorSplit != null)
							errorSplitRatio = errorSplit.getDividerLocation();
						if (errorDialog != null) {
							errorDialogSize = errorDialog.getSize();
							errorDialogPos = errorDialog.getLocation();
							errorDialog.dispose();
						}
					}
				});
				
				//	adjust size and position to parent window
				this.setSize((docDialogSize == null) ? QcToolUI.this.getSize() : docDialogSize);
				if (docDialogPos == null)
					this.setLocationRelativeTo(QcToolUI.this);
				else this.setLocation(docDialogPos);
				
				//	adjust settings of document display (zoom, page layout direction)
				if (docRenderingDpi != -1)
					docTab.setRenderingDpi(docRenderingDpi);
				if (docSideBySidePages != -1)
					docTab.setSideBySidePages(docSideBySidePages);
			}
			
			protected boolean saveDocument(ImageDocumentEditorTab idet) {
				final QcToolDoumentEditorTab qidet = ((QcToolDoumentEditorTab) idet);
				System.out.println("Saving document " + idet.getDocName() + " ...");
				
				//	create progress monitor
				final ProgressMonitorDialog pmd = new ProgressMonitorDialog(false, true, QcToolDocumentMarkupDialog.this.getDialog(), "Saving Document " + idet.getDocName() + " ...");
				pmd.setAbortExceptionMessage("ABORTED BY USER");
				pmd.setInfoLineLimit(1);
				pmd.getWindow().setSize(400, 150);
				pmd.getWindow().setLocationRelativeTo(this);
				System.out.println(" - splash screen created");
				
				//	save document, in separate thread
				final boolean[] saveSuccess = {false};
				System.out.println(" - save success initialized to " + saveSuccess[0]);
				Thread saveThread = new Thread() {
					public void run() {
						try {
							System.out.println("Saving document " + qidet.getDocName() + " ...");
							
							//	wait for splash screen to come up (we must not reach the dispose() line before the splash screen even comes up)
							while (!pmd.getWindow().isVisible()) try {
								Thread.sleep(10);
							} catch (InterruptedException ie) {}
							System.out.println(" - splash screen opened");
							
							//	notify listeners that saving is imminent
							QcToolUI.this.ggImagine.notifyDocumentSaving(qidet.doc, dataBase, pmd);
							System.out.println(" - pre-save notification done");
							
							//	save document
							dataBase.saveDocument(qidet.docId, qidet.doc, pmd);
							System.out.println(" - document saved to data base");
							
							//	remember saving
							qidet.savedAs(qidet.getDocName());
							System.out.println(" - document tab notified");
							saveSuccess[0] = true;
							System.out.println(" - save success stored");
							
							//	notify listeners of saving success
							QcToolUI.this.ggImagine.notifyDocumentSaved(qidet.doc, dataBase, pmd);
							System.out.println(" - post-save notification done");
							
							//	update document list and protocol preview with current error protocol
							ImDocumentErrorProtocol idep = ImDocumentErrorProtocol.loadErrorProtocol(qidet.doc);
							System.out.println(" - error protocol reloaded");
							docListPanel.updateCurrentRow(idep);
							System.out.println(" - error protocol previed updated");
							
							//	update error protocol view
							docErrorPanel.setErrorProtocol((((idep == null) || (idep.getErrorCount() == 0)) ? null : idep), true);
							System.out.println(" - error protocol display updated");
						}
						
						//	catch whatever might happen
						catch (Throwable t) {
							t.printStackTrace(System.out);
							DialogFactory.alert(("An error occurred while saving the document:\n" + t.getMessage()), "Error Saving Document", JOptionPane.ERROR_MESSAGE);
						}
						
						//	dispose splash screen
						finally {
							pmd.close();
						}
					}
				};
				System.out.println(" - save thread created");
				saveThread.start();
				System.out.println(" - save thread started");
				System.out.println(" - opening splash screen");
				pmd.popUp(true);
				System.out.println(" - splash screen closed");
				
				//	finally ...
				System.out.println(" - returning save success of " + saveSuccess[0]);
				return saveSuccess[0];
			}
			
			public void dispose() {
				if (this.errorDisplay != null)
					this.errorDisplay.dispose();
				docDialogSize = this.getSize();
				docDialogPos = this.getLocation();
				super.dispose();
			}
		}
		
		private class QcToolDocumentListPanel extends DocumentListPanel {
			private DocumentListBuffer docList;
			private String title;
			
			QcToolDocumentListPanel(DocumentListBuffer docList, String title) {
				super(docList, true);
				this.title = title;
				this.docList = docList;
				this.setListFields(listFields);
				this.setListFieldOrder(listFieldOrder);
				this.refreshDocumentList(); // have to do refresh to make configured columns show
			}
			
			protected void documentListChanged() {
				if (this.title == null)
					return; // have to catch call coming in from super constructor
				int docCount = this.getDocumentCount();
				int vDocCount = this.getVisibleDocumentCount();
				if (docCount == vDocCount)
					setTitle(this.title + " (" + docCount + " documents)");
				else setTitle(this.title + " (" + vDocCount + " of " + docCount + " documents matching filter)");
			}
			
			public String getListFieldLabel(String fieldName) {
				return listFieldLabels.getProperty(fieldName, super.getListFieldLabel(fieldName));
			}
			
			protected boolean isUtcTimeField(String fieldName) {
				return dataBase.isUtcTimeField(fieldName);
			}
			
			void updateCurrentRow(ImDocumentErrorProtocol idep) {
				
				//	get current row
				StringTupel docData = this.getSelectedDocument();
				if (docData == null)
					return;
				
				//	this one's done, remove document
				if ((idep == null) || (idep.getErrorCount() == 0)) {
					int selected = this.getSelectedIndex();
					this.docList.remove(docData);
					this.refreshDocumentList();
					if (selected == this.getVisibleDocumentCount())
						selected--;
					this.setSelectedDocument(selected);
				}
				
				//	update numbers and side error protocol
				else {
					String[] categories = idep.getErrorCategories();
					int ecCount = 0;
					int etCount = 0;
					for (int c = 0; c < categories.length; c++) {
						if (idep.getErrorCount(categories[c]) == 0)
							continue;
						ecCount++;
						String[] types = idep.getErrorTypes(categories[c]);
						for (int t = 0; t < types.length; t++) {
							if (idep.getErrorCount(categories[c], types[t]) == 0)
								continue;
							etCount++;
						}
					}
					docData.setValue(DocumentErrorProtocol.ERROR_COUNT_ATTRIBUTE, ("" + idep.getErrorCount()));
					docData.setValue(DocumentErrorProtocol.ERROR_CATEGORY_COUNT_ATTRIBUTE, ("" + ecCount));
					docData.setValue(DocumentErrorProtocol.ERROR_TYPE_COUNT_ATTRIBUTE, ("" + etCount));
					docData.setValue(DocumentErrorProtocol.BLOCKER_ERROR_COUNT_ATTRIBUTE, ("" + idep.getErrorSeverityCount(ImDocumentError.SEVERITY_BLOCKER)));
					docData.setValue(DocumentErrorProtocol.CRITICAL_ERROR_COUNT_ATTRIBUTE, ("" + idep.getErrorSeverityCount(ImDocumentError.SEVERITY_CRITICAL)));
					docData.setValue(DocumentErrorProtocol.MAJOR_ERROR_COUNT_ATTRIBUTE, ("" + idep.getErrorSeverityCount(ImDocumentError.SEVERITY_MAJOR)));
					docData.setValue(DocumentErrorProtocol.MINOR_ERROR_COUNT_ATTRIBUTE, ("" + idep.getErrorSeverityCount(ImDocumentError.SEVERITY_MINOR)));
					this.refreshDocumentList();
				}
			}
			
			protected void documentSelected(StringTupel docData, boolean doubleClick) {
				if (doubleClick)
					this.open(docData, 0);
				
				//	get document data
				String docId = docData.getValue(DOCUMENT_ID_ATTRIBUTE);
				if (docId == null)
					return;
				String docName = docData.getValue(DOCUMENT_NAME_ATTRIBUTE);
				
				//	show document
				selectDocument(docId, docName);
			}
			
			void openSelected() {
				StringTupel docData = this.getSelectedDocument();
				if (docData != null)
					open(docData, 0);
			}
			
			void open(StringTupel docData, int version) {
				if (docData == null)
					return;
				
				//	get document data
				String docId = docData.getValue(DOCUMENT_ID_ATTRIBUTE);
				if (docId == null)
					return;
				String docName = docData.getValue(DOCUMENT_NAME_ATTRIBUTE);
				
				//	open document
				openDocument(docId, docName);
			}
			
			protected JPopupMenu getContextMenu(final StringTupel docData, MouseEvent me) {
				if (docData == null)
					return null;
				
				JPopupMenu menu = new JPopupMenu();
				JMenuItem mi = null;
				
				//	load document (have to exclude non-editable ones)
				if (dataBase.isDocumentEditable(docData)) {
					mi = new JMenuItem("Load Document");
					mi.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							open(docData, 0);
						}
					});
					menu.add(mi);
					return menu;
				}
				
				return ((menu.getComponentCount() == 0) ? null : menu);
			}
		}
	}
}