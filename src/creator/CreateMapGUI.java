package creator;

import java.awt.Dimension;

import javax.swing.JFrame;

public class CreateMapGUI {

	private JFrame frame;
	private int mapDim;

	/**
	 * Create the application.
	 */
	public CreateMapGUI(int mapDim) {
		this.mapDim = mapDim;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Create Map");
		frame.setBounds(600, 250, 500, 550);
		frame.setPreferredSize(new Dimension(807, 880));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Back to main menu when closed
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				MainMenuGUI.mainWindow.setVisible(true);
			}
		});

		CreateMapPanel panel = new CreateMapPanel(this.frame, mapDim);
		frame.getContentPane().add(panel);

		frame.pack();

		frame.setResizable(false);

		frame.setVisible(true);

		panel.requestFocus();
	}

}
