package dk.dma.ais.analysis.coverage.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class SettingsGUI {

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SettingsGUI window = new SettingsGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SettingsGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 365, 292);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblLatSize = new JLabel("Lat size");
		lblLatSize.setBounds(140, 11, 46, 14);
		frame.getContentPane().add(lblLatSize);
		
		JLabel lblLonSize = new JLabel("Lon size");
		lblLonSize.setBounds(236, 11, 46, 14);
		frame.getContentPane().add(lblLonSize);
		
		textField = new JTextField();
		textField.setBounds(140, 36, 86, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setBounds(236, 36, 86, 20);
		frame.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		textField_2 = new JTextField();
		textField_2.setBounds(140, 92, 200, 128);
		frame.getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		JLabel lblSourceInformation = new JLabel("Source information");
		lblSourceInformation.setBounds(140, 67, 117, 14);
		frame.getContentPane().add(lblSourceInformation);
		
		JLabel lblDataSource = new JLabel("Data source");
		lblDataSource.setBounds(10, 11, 86, 14);
		frame.getContentPane().add(lblDataSource);
		
		JRadioButton rdbtnFromLiveStream = new JRadioButton("From live stream");
		rdbtnFromLiveStream.setBounds(10, 35, 109, 23);
		frame.getContentPane().add(rdbtnFromLiveStream);
		
		JRadioButton rdbtnFromFile = new JRadioButton("From file");
		rdbtnFromFile.setBounds(10, 61, 109, 23);
		frame.getContentPane().add(rdbtnFromFile);
		
		JTextArea txtrGurligris = new JTextArea();
		txtrGurligris.setText("gurligris");
		txtrGurligris.setBounds(10, 93, 109, 128);
		frame.getContentPane().add(txtrGurligris);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenuItem mntmLoadSettingsfile = new JMenuItem("Load settingsfile");
		menuBar.add(mntmLoadSettingsfile);
		
		JMenuItem mntmSaeSettings = new JMenuItem("Sae Settings");
		menuBar.add(mntmSaeSettings);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		menuBar.add(mntmAbout);
	}
}
