package net.monolith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.JFrame;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.GridBagConstraints;
import javax.swing.border.EmptyBorder;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import java.awt.FlowLayout;
import java.util.Scanner;

public class Updater extends JFrame implements RBCWrapperDelegate {
	private static final long serialVersionUID = -2299542785765152084L;
	private CardLayout cardLayout = new CardLayout(0, 0);
	private JPanel downloadPanel, finishPanel;
	private JLabel lblStatustext, lblProgresstext;
	private JProgressBar progressBar;
	private JPanel errLogPanel;
	private JScrollPane scrollPane;
	private JTextArea txtErrortext;
	private GridBagConstraints gbc_panel_err;
	private JPanel panel;
	private JButton btnCancel;
	private JPanel promptPanel;
	private JLabel lblUpdatePrompt;
	private JPanel panel_1;
	private JButton btnClose;
	private JButton btnCheckUpdates;
	private FileOutputStream fos;
	private String updateDir, oldVersion, newVersion;
	private int oldBuild, newBuild;
	
	private int size, sizeSoFar;
	
	private final String ZIP_FILE_NAME = "Update.zip";
	private final String WEB_ADDRESS = "http://monolith-code.net.tiberius.sui-inter.net";
	
	private final ImageIcon icoDownload = new ImageIcon(Updater.class.getResource("/resources/download.png"));
	
	private boolean isUpdater = false;
	
	public Updater(){
		this(null, 0, null);
	}
	
	public Updater(String version, int build, String hasUpdate) {
		super("Monolith Updater");
		oldVersion = version;
		oldBuild = build;
		isUpdater = (version != null);


		if(hasUpdate != null && hasUpdate.equals("true")){
			if(shouldUpdate()) {
				System.out.println("1");
			}else{
				System.out.println("0");
			}
			return;
		}

		
		// ------------------------------------ Look And Feel ------------------------------------ //
		try {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0) {
				// Special Linux style
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(this);

		
		//-------------------------------------- Set Variables --------------------------------------//
		updateDir = System.getProperty("user.dir");
		
		
		//-------------------------------------------- UI --------------------------------------------//
		getContentPane().setLayout(cardLayout);

		downloadPanel = new JPanel();
		downloadPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
		getContentPane().add(downloadPanel, "name_316704720299860");
		GridBagLayout gbl_downloadPanel = new GridBagLayout();
		gbl_downloadPanel.columnWidths = new int[] { 0, 0 };
		gbl_downloadPanel.rowHeights = new int[] { 0, 0, 0, -21, 0 };
		gbl_downloadPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_downloadPanel.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		downloadPanel.setLayout(gbl_downloadPanel);

		JLabel lblDownloadingUpdate = new JLabel("Downloading update...");
		GridBagConstraints gbc_lblDownloadingUpdate = new GridBagConstraints();
		gbc_lblDownloadingUpdate.insets = new Insets(0, 0, 5, 0);
		gbc_lblDownloadingUpdate.gridx = 0;
		gbc_lblDownloadingUpdate.gridy = 0;
		downloadPanel.add(lblDownloadingUpdate, gbc_lblDownloadingUpdate);

		lblProgresstext = new JLabel("");
		GridBagConstraints gbc_lblProgresstext = new GridBagConstraints();
		gbc_lblProgresstext.anchor = GridBagConstraints.WEST;
		gbc_lblProgresstext.insets = new Insets(0, 0, 5, 0);
		gbc_lblProgresstext.gridx = 0;
		gbc_lblProgresstext.gridy = 1;
		downloadPanel.add(lblProgresstext, gbc_lblProgresstext);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 2;
		downloadPanel.add(progressBar, gbc_progressBar);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.EAST;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 3;
		downloadPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		panel.add(btnCancel, gbc_btnCancel);

		finishPanel = new JPanel();
		finishPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
		getContentPane().add(finishPanel, "name_317042283753855");
		GridBagLayout gbl_finishPanel = new GridBagLayout();
		gbl_finishPanel.columnWidths = new int[] { 0, 0 };
		gbl_finishPanel.rowHeights = new int[] { 28, 18, 0, 0 };
		gbl_finishPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_finishPanel.rowWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
		finishPanel.setLayout(gbl_finishPanel);

		lblStatustext = new JLabel("");
		GridBagConstraints gbc_lblStatustext = new GridBagConstraints();
		gbc_lblStatustext.insets = new Insets(0, 0, 5, 0);
		gbc_lblStatustext.gridx = 0;
		gbc_lblStatustext.gridy = 0;
		finishPanel.add(lblStatustext, gbc_lblStatustext);

		errLogPanel = new JPanel();
		errLogPanel.setPreferredSize(new Dimension (200, 60));
		errLogPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		gbc_panel_err = new GridBagConstraints();
		gbc_panel_err.insets = new Insets(0, 0, 5, 0);
		gbc_panel_err.fill = GridBagConstraints.BOTH;
		gbc_panel_err.gridx = 0;
		gbc_panel_err.gridy = 1;
		errLogPanel.setLayout(new BorderLayout(0, 0));

		scrollPane = new JScrollPane();
		errLogPanel.add(scrollPane, BorderLayout.CENTER);
		
		txtErrortext = new JTextArea();
		txtErrortext.setText("");
		txtErrortext.setEditable(false);
		scrollPane.setViewportView(txtErrortext);
		txtErrortext.setColumns(30);

		JButton btnOk = new JButton("OK");
		GridBagConstraints gbc_btnOk = new GridBagConstraints();
		gbc_btnOk.anchor = GridBagConstraints.EAST;
		gbc_btnOk.gridx = 0;
		gbc_btnOk.gridy = 2;
		finishPanel.add(btnOk, gbc_btnOk);
		
		promptPanel = new JPanel();
		promptPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
		getContentPane().add(promptPanel, "name_589317401047879");
		GridBagLayout gbl_promptPanel = new GridBagLayout();
		gbl_promptPanel.columnWidths = new int[]{0, 0, 0};
		gbl_promptPanel.rowHeights = new int[]{77, 0, 0};
		gbl_promptPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_promptPanel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		promptPanel.setLayout(gbl_promptPanel);
		
		JLabel lblIcon = new JLabel("");
		lblIcon.setIcon(icoDownload);
		GridBagConstraints gbc_lblIcon = new GridBagConstraints();
		gbc_lblIcon.gridheight = 2;
		gbc_lblIcon.insets = new Insets(0, 0, 5, 5);
		gbc_lblIcon.gridx = 0;
		gbc_lblIcon.gridy = 0;
		promptPanel.add(lblIcon, gbc_lblIcon);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(0, 0, 0, 0));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.VERTICAL;
		gbc_panel_2.gridx = 1;
		gbc_panel_2.gridy = 0;
		promptPanel.add(panel_2, gbc_panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		panel_1 = new JPanel();
		panel_2.add(panel_1, BorderLayout.SOUTH);
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setVgap(0);
		panel_1.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		btnCheckUpdates = new JButton("Check Updates");
		panel_1.add(btnCheckUpdates);
		
		btnClose = new JButton("Close");
		panel_1.add(btnClose);
		
		lblUpdatePrompt = new JLabel("Would you like to check for updates?");
		panel_2.add(lblUpdatePrompt, BorderLayout.CENTER);
		lblUpdatePrompt.setIcon(null);

		
		//------------------------------------ Listener ------------------------------------//

		// Check Update Button
		btnCheckUpdates.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent event) {
				checkUpdate();
			}
		});
		
		// Close Button
		btnClose.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Updater.this.dispose();
			}
		});
		
		// Cancel Button
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent event) {
				abortDownload();
			}
		});
		
		// OK Button
		btnOk.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Updater.this.dispose();
			}
		});

		// After UI
		setPromptCard();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(300, 120));
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

		getLatestInfo();

		// Start
		if(oldVersion != null){
			checkUpdate();
		}
	}
	
	public void checkUpdate(){
		// Was started by Monolith Editor and contains Version information
		if(checkConnectin()){
			ReleaseInfo info = getLatestInfo();
			if(info != null){
				//Is new Version
				int res;
				if(oldVersion == null)
					res = JOptionPane.showOptionDialog(this, "Monolith Text is not installed. Would you like to download and install?", "Download", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
				else
					res = JOptionPane.showOptionDialog(this, 	"<html>There is a new version available. Would you like to update?<br><br>" +
																"<i>Old</i>: &nbsp;Monolith Code v" + oldVersion + " : " + oldBuild + "<br>" +
																"<i>New</i>: Monolith Code v" + info.version + " : " + info.build + "<br><br>" +
																"<b>Release Notes</b> " +
																"<small>" + info.date + "</small><br><hr>" +
																info.releaseNotes + "<br><br></html>"
							, "New Vesion Available", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
				
				if(res == JOptionPane.OK_OPTION){
					installUpdate();
				}
			}else{
				// Is no new Version
				JOptionPane.showMessageDialog(this, "You already have the latest version.\n Monolith Text v" + oldVersion + " : " + oldBuild, "Monolith " + oldVersion, JOptionPane.INFORMATION_MESSAGE);				
			}
		}else{
			// No internet
			JOptionPane.showMessageDialog(this, "No internet connection!\nPlease reconnect to the internet and try again.", "No internet connection", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void installUpdate() {		
		setProgressCard();

		// Download file
		System.out.print("STAGE 3: Downloading");
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				try {
					if(!downloadUpdate(updateDir +  "/" + ZIP_FILE_NAME, WEB_ADDRESS + "/release/latest"))
						return;
					System.out.println(" - Successful!");
					
					// Unzip
					System.out.print("STAGE 4: Unzip");
					UnZip zip = new UnZip();
					zip.unZipIt(updateDir + "/" + ZIP_FILE_NAME, updateDir);
					
					// Delete Zip
					System.out.print("STAGE 5: Delete Zip file");
					File f = new File(updateDir + "/" + ZIP_FILE_NAME);
					f.delete();
					System.out.println(" - Successful!");
					
					setFinishCard("");
				
				} catch (IOException e) {
					setFinishCard(e.toString());
					e.printStackTrace();
					return;
				}
			}
		});
		t1.start();
	}
	
	// Set Panel to Prompt Panel
	private void setPromptCard(){
		cardLayout.show(getContentPane(), "name_589317401047879");
		pack();
	}
	
	// Set Panel to Progress Panel
	private void setProgressCard(){
		cardLayout.show(getContentPane(), "name_316704720299860");
		pack();
	}

	// Set Panel to Finish Panel
	private void setFinishCard(String err) {
		cardLayout.show(getContentPane(), "name_317042283753855");

		if (err.equals("")) {
			if(isUpdater)
				lblStatustext.setText("<html><br>Update was successful!<br>Please restart the Application</html>");
			else
				lblStatustext.setText("<html><br>Installation was successful!</html>");
		} else {
			lblStatustext.setText("Update failed!");
			txtErrortext.setText(err);
			finishPanel.add(errLogPanel, gbc_panel_err);
		}
		pack();
	}
	
	// Abort Download
	private void abortDownload(){
		try {
			if(fos != null){
				fos.getChannel().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		File f = new File(updateDir + "/" + ZIP_FILE_NAME);
		f.delete();
		setFinishCard("Update was aborted.");
		System.out.println(" - Cancel!");
	}
	
	// Updates Progress bar
	private void updateBar(int value, long size){
		progressBar.setValue(value);
		lblProgresstext.setText("Downloading " + sizeConverter(size) + " / " + sizeConverter(this.size));
		pack();
	}

	// Downloads File
	private boolean downloadUpdate(String localPath, String remoteURL) throws IOException {	
		ReadableByteChannel rbc;
		URL url; 

		url = new URL(remoteURL);
		size = contentLength(url);
		rbc = new RBCWrapper(Channels.newChannel(url.openStream()), contentLength(url), this);
		fos = new FileOutputStream(localPath);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		
		System.out.print(" (" + sizeSoFar + "/" + size + ")");
		
		if(sizeSoFar < size) return false;
		return true;

	}

	/*
	private boolean shouldUpdate(){
		try{
			URL url = new URL(WEB_ADDRESS + "/release/isLatest/" + oldBuild);
			URLConnection con = url.openConnection();
			InputStream is =con.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String result = br.readLine();

			is.close();


			if(result.equals("false")){
				url = new URL(WEB_ADDRESS + "/release/latestInfo");
				con = url.openConnection();
				is = con.getInputStream();


				return true;
			}else{
				return false;
			}
		}catch (IOException e){
			return false;
		}
	}
*/
	private boolean shouldUpdate(){
		ReleaseInfo info = getLatestInfo();
		return info != null;
	}

	private ReleaseInfo getLatestInfo() {

		JSONParser parser = new JSONParser();

		try {
			String out = new Scanner(new URL(WEB_ADDRESS + "/release/isLatest/" + oldBuild).openStream(), "UTF-8").useDelimiter("\\A").next();

			Object obj = parser.parse(out);
			JSONObject jsonObject = (JSONObject) obj;


			if(!(boolean) jsonObject.get("latest")){
				String version = (String) jsonObject.get("version");
				int build = Integer.parseInt((String) jsonObject.get("build"));
				String timestamp = (String) jsonObject.get("timestamp");
				String releaseNotes = (String) jsonObject.get("release_notes");

				return new ReleaseInfo(version, build, timestamp, releaseNotes);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Gets latest version
	/*
	private boolean compareVersions() {
		// fetch new version number
		String version = "";
		String build = "";
     
		try{
			URL url = new URL(WEB_ADDRESS + "version.txt");
		
			// Get the input stream through URL Connection
		    URLConnection con = url.openConnection();
		    InputStream is =con.getInputStream();
		
		    BufferedReader br = new BufferedReader(new InputStreamReader(is));
		    version = br.readLine();
		    build = br.readLine();
		
		}catch (IOException e){
			return true;
		}
		
		newVersion = version;
		newBuild = Integer.parseInt(build);
		
		return (oldBuild >= newBuild);

	}
	*/

	// Get file size
	private int contentLength(URL url) {
		HttpURLConnection connection;
		int contentLength = -1;

		try {
			HttpURLConnection.setFollowRedirects(false);

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");

			contentLength = connection.getContentLength();
		} catch (Exception e) {
		}

		return contentLength;
	}
	
	// Returns converted size
	private String sizeConverter(long size){
		if (size < 1024.0f){
			return size + " Byte";
		}else if(size < 1024.0f * 1024.0f){
			return String.format("%.02f KB", (size/1024.0f));
		}else if(size < 1024.0f * 1024.0f * 1024.0f){
			return String.format("%.02f MB", (size/(1024.0f * 1024.0f)));
		}else if(size < 1024.0f * 1024.0f * 1024.0f * 1024.0f){
			return String.format("%.02f GB", (size/(1024.0f * 1024.0f * 1024.0f)));
		}else{			
			return "ERROR MESSAGE!";
		}
	}
	
	// Checks internet connection
	public boolean checkConnectin() {
		try {
			try {
				URL url = new URL("http://www.google.com");
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.connect();
				if (con.getResponseCode() == 200){
					return true;
				}
			} catch (Exception exception) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	

	@Override
	public void rbcProgressCallback(RBCWrapper rbc, double progress) {
		updateBar((int)progress, rbc.getReadSoFar());
		sizeSoFar = (int) rbc.getReadSoFar();
	}

	public static void main(String[] args) {
		if (args.length == 3)
			new Updater(args[0], Integer.parseInt(args[1]), args[2]);
		else
			new Updater();
	}
}
