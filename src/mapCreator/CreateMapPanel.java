package mapCreator;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class CreateMapPanel extends JPanel {

	private enum objectSelected{
		NONE,
		WALL,
		ROCK,
		EXIT,
		DELETE
	};

	public final int WIDTH = 800;
	public final int HEIGHT = 850;

	private BufferedImage wall;
	private BufferedImage rock;
	private BufferedImage exit;
	private BufferedImage delete;
	//private int x=0, y=0;//, width=100, height=100;
	private JButton confirmBtn;
	private JTextField fileNameText;

	private int mapDim;
	private double squareLength;
	private char[][] map;
	private objectSelected selected = objectSelected.NONE;

	public CreateMapPanel(JFrame parent, int mapDim) {
		this.setLayout(null);

		try {
			wall = ImageIO.read(new File("img/wall.png"));
			rock = ImageIO.read(new File("img/rock.png"));
			exit = ImageIO.read(new File("img/exit.png"));
			delete = ImageIO.read(new File("img/delete.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}	

		this.mapDim = mapDim;
		map = new char[mapDim][mapDim];
		squareLength = WIDTH/mapDim;

		//Initializes map
		for(int x = 0; x < mapDim; x++){
			map[0][x] = 'X';
			map[mapDim-1][x] = 'X';
		}
		for(int y = 1; y < mapDim-1; y++){
			map[y][0] = 'X';
			map[y][mapDim-1] = 'X';
		}
		for(int x = 1; x < mapDim-1; x++){
			for(int y = 1; y < mapDim-1; y++){
				map[y][x] = ' ';
			}
		}

		JLabel fileNameLbl = new JLabel("Map name");
		fileNameLbl.setHorizontalAlignment(SwingConstants.CENTER);
		fileNameLbl.setBounds(200, 800, 300, 25);
		this.add(fileNameLbl);

		fileNameText = new JTextField();
		fileNameText.setHorizontalAlignment(SwingConstants.CENTER);
		fileNameText.setText("10");
		fileNameText.setBounds(200, 825, 300, 25);
		this.add(fileNameText);

		confirmBtn = new JButton("Save Map");
		confirmBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(checkMapValidity() == 0){
					try {
						BufferedWriter writer = new BufferedWriter(new FileWriter(fileNameText.getText()));
						for ( int y = 0; y < map.length; y++)
						{
							for ( int x = 0; x < map[y].length; x++)
							{    
								writer.write(map[y][x]);
							}
							if(y != map.length-1)
								writer.write("\n");
						}
						writer.close();

						JOptionPane.showMessageDialog(getParent(), "Map was saved to:\n" + System.getProperty("user.dir") + System.getProperty("file.separator")  + fileNameText.getText(),
								"Map saved",
								JOptionPane.INFORMATION_MESSAGE);
					} catch(IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		confirmBtn.setBounds(500, 800, 300, 50);
		this.add(confirmBtn);

		addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getY() > WIDTH){
					if (e.getX() < 50)
						selected = objectSelected.WALL;
					else if (e.getX() < 100)
						selected = objectSelected.ROCK;
					else if (e.getX() < 150)
						selected = objectSelected.EXIT;
					else if (e.getX() < 200)
						selected = objectSelected.DELETE;
				} else {
					char tmp = ' ';
					if (e.getX() > squareLength && e.getX() < squareLength * (mapDim - 1) &&
							e.getY() > squareLength && e.getY() < squareLength * (mapDim - 1)){
						switch (selected){
						case WALL:
							tmp = 'X';
							break;

						case ROCK:
							tmp = 'R';
							break;

						case EXIT:
							tmp = 'E';
							break;

						case DELETE:
							tmp = ' ';
							break;

						default:
							break;
						}
						map[(int)(e.getY()/squareLength)][(int)(e.getX()/squareLength)] = tmp;
					} else {
						//BORDERS CAN ONLY HAVE WALLS OR EXIT
						switch (selected){
						case EXIT:
							tmp = 'E';
							map[(int)(e.getY()/squareLength)][(int)(e.getX()/squareLength)] = tmp;
							break;

						case DELETE:
							tmp = 'X';
							map[(int)(e.getY()/squareLength)][(int)(e.getX()/squareLength)] = tmp;
							break;

						default:
							break;
						}
					}
				}
				repaint();

			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}	
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		//		//Draws walls on borders
		//		for(int x = 0; x < 10; x++){
		//			g.drawImage(wall, x*50, 0, (x+1)*50, 50, 0, 0, wall.getWidth(), wall.getHeight(), null);
		//			g.drawImage(wall, x*50, 450, (x+1)*50, 500, 0, 0, wall.getWidth(), wall.getHeight(), null);
		//		}
		//
		//		for(int y = 1; y < 9; y++){
		//			g.drawImage(wall, 0, y*50, 50, (y+1)*50, 0, 0, wall.getWidth(), wall.getHeight(), null);
		//			g.drawImage(wall, 450, y*50, 500, (y+1)*50, 0, 0, wall.getWidth(), wall.getHeight(), null);
		//		}

		//Draws grid
		for(int x = 0; x < mapDim; x++){
			for(int y = 0; y < mapDim; y++){
				g.drawRect((int)(x*squareLength), (int)(y*squareLength), (int)squareLength, (int)squareLength);
			}
		}

		//Draws map
		for(int x = 0; x < mapDim; x++){
			for(int y = 0; y < mapDim; y++){
				switch (map[y][x]){
				case 'X':
					g.drawImage(wall, (int)(x*squareLength), (int)(y*squareLength), (int)((x+1)*squareLength), (int)((y+1)*squareLength), 0, 0, wall.getWidth(), wall.getHeight(), null);
					break;
				case 'R':
					g.drawImage(rock, (int)(x*squareLength), (int)(y*squareLength), (int)((x+1)*squareLength), (int)((y+1)*squareLength), 0, 0, rock.getWidth(), rock.getHeight(), null);
					break;
				case 'E':
					g.drawImage(exit, (int)(x*squareLength), (int)(y*squareLength), (int)((x+1)*squareLength), (int)((y+1)*squareLength), 0, 0, rock.getWidth(), rock.getHeight(), null);
					break;
				default:
					break;
				}

			}
		}

		//Draws down "toolbar"
		g.drawImage(wall, 0, HEIGHT-49, 49, HEIGHT, 0, 0, wall.getWidth(), wall.getHeight(), null);
		g.drawImage(rock, 50, HEIGHT-49, 99, HEIGHT, 0, 0, rock.getWidth(), rock.getHeight(), null);
		g.drawImage(exit, 100, HEIGHT-49, 149, HEIGHT, 0, 0, exit.getWidth(), exit.getHeight(), null);
		g.drawImage(delete, 150, HEIGHT-49, 199, HEIGHT, 0, 0, delete.getWidth(), delete.getHeight(), null);
	}

	public int checkMapValidity(){
		int exitCounter = 0;
		for(int x = 0; x < mapDim; x++){
			for(int y = 0; y < mapDim; y++){
				if (map[y][x] == 'E'){
					exitCounter++;
				}
			}
		}

		if (exitCounter == 0){
			JOptionPane.showMessageDialog(getParent(), "Place at least one exit.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return 7;
		} else if (exitCounter > 1){
			JOptionPane.showMessageDialog(getParent(), "Place only one exit.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return 8;
		}

		for(int x = 0; x < mapDim; x++){
			for(int y = 0; y < mapDim; y++){
				if (map[y][x] == 'E'){
					if (((x == 0 || x == mapDim - 1) && y == x) || (x == 0 &&  y == mapDim - 1) || (y == 0 &&  x == mapDim - 1)){
						JOptionPane.showMessageDialog(getParent(), "Exit cannot be placed in a corner.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
						return 10;
					}
				}
			}
		}

		return 0;
	}
}
