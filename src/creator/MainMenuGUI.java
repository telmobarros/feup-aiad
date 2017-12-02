package creator;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class MainMenuGUI extends JFrame {

	public static MainMenuGUI mainWindow;

	private JPanel contentPane;
	private JTextField mapDimensionText;
	private JTextField fileNameText;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainWindow = new MainMenuGUI();
					mainWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainMenuGUI() {
		setResizable(false);
		setTitle("Map Creator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(600, 250, 250, 240);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel mapDimensionLbl = new JLabel("Space Dimension");
		mapDimensionLbl.setHorizontalAlignment(SwingConstants.CENTER);
		mapDimensionLbl.setBounds(10, 78, 174, 14);
		contentPane.add(mapDimensionLbl);

		mapDimensionText = new JTextField();
		mapDimensionText.setHorizontalAlignment(SwingConstants.CENTER);
		mapDimensionText.setText("10");
		mapDimensionText.setBounds(74, 99, 46, 20);
		contentPane.add(mapDimensionText);
		mapDimensionText.setColumns(10);

		JButton createMapBtn = new JButton("Create Map");
		createMapBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkValuesInserted()) {
					new CreateMapGUI(Integer.parseInt(mapDimensionText.getText()));
					mainWindow.setVisible(false);
				}
			}
		});
		createMapBtn.setBounds(10, 130, 230, 30);
		contentPane.add(createMapBtn);
		
		
		fileNameText = new JTextField();
		fileNameText.setHorizontalAlignment(SwingConstants.CENTER);
		fileNameText.setText("map.txt");
		fileNameText.setBounds(140, 170, 100, 30);
		contentPane.add(fileNameText);
		
		JButton createMapRandBtn = new JButton("Random Map");
		createMapRandBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkValuesInserted()) {
					new MazeBuilder().buildMazetoTXT(fileNameText.getText(), Integer.parseInt(mapDimensionText.getText()));
					JOptionPane.showMessageDialog(getParent(),
							"Map was saved to:\n" + System.getProperty("user.dir")
									+ System.getProperty("file.separator") + fileNameText.getText(),
							"Map saved", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		createMapRandBtn.setBounds(10, 170, 130, 30);
		contentPane.add(createMapRandBtn);


		JLabel titleLbl = new JLabel("Map Creator");
		titleLbl.setForeground(Color.BLACK);
		titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
		titleLbl.setFont(new Font("Century Gothic", Font.BOLD, 29));
		titleLbl.setBounds(0, 0, 194, 80);
		contentPane.add(titleLbl);

	}

	public boolean checkValuesInserted() {
		if (Integer.parseInt(mapDimensionText.getText()) < 4 || Integer.parseInt(mapDimensionText.getText()) > 300) {
			JOptionPane.showMessageDialog(this, "Map dimension should be a value between 4 and 300", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
