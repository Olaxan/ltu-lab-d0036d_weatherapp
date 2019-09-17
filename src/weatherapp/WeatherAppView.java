package weatherapp;

import weatherapp.WeatherAppModel;
import weatherapp.Locality;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.beans.PropertyChangeListener;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.beans.PropertyChangeEvent;
import javax.swing.JSpinner;

public class WeatherAppView extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private final ButtonGroup placesGroup = new ButtonGroup();
	private JTable table;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WeatherAppView frame = new WeatherAppView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public WeatherAppView() {
		
		Path path = FileSystems.getDefault().getPath(".").toAbsolutePath();
		
		System.out.println(path);
		
		setTitle("OlaxaCo Weather-o-Matic");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				model.addRow(new Object[]{"New location", 0, 0, 0});
			}
		});
		btnAdd.setBounds(505, 159, 79, 23);
		contentPane.add(btnAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				int selected = table.getSelectedRow();
				
				if (selected != -1)
				{
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.removeRow(selected);
					
					if (table.getRowCount() > 0)
						table.setRowSelectionInterval(selected - 1, selected - 1);
				}
			}
		});
		btnRemove.setBounds(407, 159, 88, 23);
		contentPane.add(btnRemove);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 574, 137);
		contentPane.add(scrollPane);
		
		table = new JTable();
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		scrollPane.setViewportView(table);
		table.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Name", "Latitude", "Longitude", "Altitude"
			}
		));
		
		Locality[] locList = WeatherAppModel.ReadPlaces(path + "/places.xml");
		for (int i = 0; i < locList.length; i++)
		{
			Locality loc = locList[i];
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.addRow(new Object[] {loc.name, loc.latitude, loc.longitude, loc.altitude});
		}
		
		JButton btnFetchData = new JButton("Fetch data");
		btnFetchData.setBounds(470, 437, 114, 23);
		contentPane.add(btnFetchData);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 193, 574, 233);
		contentPane.add(scrollPane_1);
		
		JTextArea txtrResult = new JTextArea();
		txtrResult.setEditable(false);
		txtrResult.setText("Select a location from the list");
		scrollPane_1.setViewportView(txtrResult);
		
		JSpinner spinCacheTime = new JSpinner();
		spinCacheTime.setBounds(99, 438, 79, 20);
		contentPane.add(spinCacheTime);
		
		JLabel lblCacheTime = new JLabel("Cache time");
		lblCacheTime.setBounds(10, 441, 79, 14);
		contentPane.add(lblCacheTime);
		
		JLabel lblSeconds = new JLabel("seconds");
		lblSeconds.setBounds(188, 441, 46, 14);
		contentPane.add(lblSeconds);
	}
}
