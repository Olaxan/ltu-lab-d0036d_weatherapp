package weatherapp;

import weatherapp.WeatherAppModel;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import javax.swing.JSpinner;
import javax.swing.event.*;
import javax.swing.table.TableModel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.JTextPane;

public class WeatherAppView extends JFrame implements TableModelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private final ButtonGroup placesGroup = new ButtonGroup();
	private JTable table;
	
	WeatherAppModel appModel;
	Boolean isInitialized = false;

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

	void initModel()
	{
		appModel = new WeatherAppModel();
		
		Path path = FileSystems.getDefault().getPath(".").toAbsolutePath();
		int countLoaded = appModel.load(path + "/places.xml");
		
		System.out.println(countLoaded > -1 ? String.format("Loaded %o places from places.xml", countLoaded) : "Error loading places.xml - does the file exist?");
		
		appModel.addToTable(table);
		
		isInitialized = true;
	}
	
	/**
	 * Create the frame.
	 */
	public WeatherAppView() {
		
		setTitle("Good Weather App");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				model.addRow(new Object[]{"New location", 0, 0, 0});
			}
		});
		btnAdd.setBounds(505, 159, 79, 23);
		contentPane.add(btnAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				
				int selected = table.getSelectedRow();
				
				if (selected != -1)
				{
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.removeRow(selected);
					
					if (table.getRowCount() > 0)
					{
						int next = selected == 0 ? 0 : selected - 1;
						table.setRowSelectionInterval(next, next);
					}
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
		
		table.getModel().addTableModelListener(this);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 193, 574, 233);
		contentPane.add(scrollPane_1);
		
		JTextArea txtrResult = new JTextArea();
		txtrResult.setLineWrap(true);
		txtrResult.setEditable(false);
		txtrResult.setText("Select a location from the list");
		scrollPane_1.setViewportView(txtrResult);
		
		JSlider slider = new JSlider();
		slider.setEnabled(false);
		slider.setPaintLabels(true);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setValue(0);
		slider.setMaximum(24);
		slider.setBounds(10, 159, 397, 23);
		contentPane.add(slider);
		slider.addChangeListener(new ChangeListener() 
		{
		      public void stateChanged(ChangeEvent e) 
		      {
		    	  System.out.println("yas " + slider.getValue());
		      }
		});
		
		JButton btnFetchData = new JButton("Fetch data");
		btnFetchData.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				int selected = table.getSelectedRow();
				
				txtrResult.setText("Working...");
				
				if (selected != -1)
				{
					try
					{
						txtrResult.setText(appModel.fetchWeatherData(selected));
						slider.setEnabled(true);
						return;
					} 
					catch (Exception e)
					{
						txtrResult.setText(e.getMessage());
					}
				}
				else
					txtrResult.setText("Please select a location from the list.");
				
				slider.setEnabled(false);
			}
		});
		btnFetchData.setBounds(470, 437, 114, 23);
		contentPane.add(btnFetchData);
		
		JSpinner spinCacheTime = new JSpinner();
		spinCacheTime.setBounds(99, 438, 79, 20);
		contentPane.add(spinCacheTime);
		spinCacheTime.addChangeListener(new ChangeListener() 
		{
		      public void stateChanged(ChangeEvent e) 
		      {
		    	  appModel.setCacheTimeMS((Integer) spinCacheTime.getValue());
		      }
		});
		
		JLabel lblCacheTime = new JLabel("Cache time");
		lblCacheTime.setBounds(10, 441, 79, 14);
		contentPane.add(lblCacheTime);
		
		JLabel lblSeconds = new JLabel("MS");
		lblSeconds.setBounds(188, 441, 272, 14);
		contentPane.add(lblSeconds);
		
		initModel();
		spinCacheTime.setValue(appModel.getCacheTimeMS());
	}
	
	public void tableChanged(TableModelEvent e)
	{
		
		if (!isInitialized)
			return;
		
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        
        switch (e.getType())
        {
        case TableModelEvent.INSERT:
        	appModel.addLocation("New location", 0, 0, 0);
        	System.out.println("INSERT at " + row + " / " + column);
        	break;
        	
        case TableModelEvent.DELETE:
        	appModel.deleteLocation(row);
        	System.out.println("DELETE at " + row + " / " + column);
        	break;
        	
        case TableModelEvent.UPDATE:
        	appModel.updateLocation(row, 
        			model.getValueAt(row, 0).toString(),
        			Float.parseFloat(model.getValueAt(row, 1).toString()),
        			Float.parseFloat(model.getValueAt(row, 2).toString()),
        			Integer.parseInt(model.getValueAt(row, 3).toString())
        	);
        	System.out.println("UPDATE at " + row + " / " + column);
        	break;
        }
    }
}
