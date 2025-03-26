package gui;
//Tenkaichi ANM Swap (GUI) by ViveTheModder
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import cmd.AnmPak;

public class Main 
{
	private static final Font BOLD = new Font("Tahoma", 1, 24);
	private static final Font MED = new Font("Tahoma", 0, 18);
	private static final Font BOLD_S = new Font("Tahoma", 1, 12);
	private static final String HTML_A_START = "<html><a href=''>";
	private static final String HTML_A_END = "</a></html>";
	private static final String WINDOW_TITLE = "Tenkaichi ANM Swap";
	private static final Toolkit DEF_TOOLKIT = Toolkit.getDefaultToolkit();
	
	private static AnmPak getPakFromChooser() throws IOException
	{
		AnmPak pak=null;
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Animation Container/Pack (.PAK)", "pak");
		chooser.addChoosableFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(filter);
		chooser.setDialogTitle("Open ANM PAK...");
		
		while (pak==null)
		{
			int result = chooser.showOpenDialog(chooser);
			if (result==0)
			{
				pak = new AnmPak(chooser.getSelectedFile());
				if (!pak.isValidAnmPak()) 
				{
					pak=null;
					errorBeep();
					JOptionPane.showMessageDialog(chooser, "Invalid ANM PAK! The reason could be an incorrect number of entries,\n"
					+ "file size from the last index, or file name (must end with "+'"'+"_anm.pak"+'"'+").", WINDOW_TITLE, 0);
				}
			}
			else if (result==1) break;
		}
		return pak;
	}
	private static String[] getAnmNames(boolean applyBt3Names) throws IOException
	{
		File csv;
		String[] anmNames = new String[AnmPak.NUM_ANMS];
		if (applyBt3Names) csv = new File("./csv/anm-bt3.csv");
		else csv = new File("./csv/anm-bt2.csv");
		
		Scanner sc = new Scanner(csv);
		while (sc.hasNextLine())
		{
			String line = sc.nextLine();
			String[] inputs = line.split(",");
			int index = Integer.parseInt(inputs[0]);
			anmNames[index] = index+"_"+inputs[1]+".canm";
		}
		sc.close();
		return anmNames;
	}
	private static void errorBeep()
	{
		Runnable runWinErrorSnd = (Runnable) DEF_TOOLKIT.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd!=null) runWinErrorSnd.run();
	}
	private static void setApplication() throws IOException
	{
		AnmPak[] paks = new AnmPak[2];
		String[] anmNames = getAnmNames(true);
		String[] anmSelect = {"Animation Selection Start","Animation Selection End"};
		String[] tooltips = {"The first animation to swap in the selection.","The last animation to swap in the selection."};
		//initialize components
		Box chkboxBox = Box.createHorizontalBox();
		GridBagConstraints gbc = new GridBagConstraints();
		JButton swapBtn = new JButton("Swap Animations");
		JComboBox<String>[] dropdowns = new JComboBox[4];
		JCheckBox[] checkboxes = new JCheckBox[2];
		JFrame frame = new JFrame();
		JLabel[] anmLabels = new JLabel[2];
		JLabel[] dropdownLabels = new JLabel[4];
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		JMenuItem[] openPaks = {new JMenuItem("Open 1st ANM PAK..."),new JMenuItem("Open 2nd ANM PAK...")};
		JPanel panel = new JPanel();
		//set components
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.setLayout(new GridBagLayout());
		swapBtn.setFont(MED);
		swapBtn.setToolTipText("<html><div style='text-align: center;'>Applies ANMs within each file's ANM selection from one PAK to another,<br>"
		+ "AND vice-versa. Yes, this is an ACTUAL swapping procedure.</div></html>");
		for (int i=0; i<4; i++) 
		{
			anmLabels[i/2] = new JLabel("ANM PAK File "+(i/2+1));
			checkboxes[i/2] = new JCheckBox("Apply BT2 ANM Names to "+anmLabels[i/2].getText());
			dropdownLabels[i] = new JLabel(anmSelect[i%2]);
			dropdowns[i] = new JComboBox<String>(anmNames);
			
			anmLabels[i/2].setFont(BOLD);
			dropdownLabels[i].setToolTipText(tooltips[i%2]);
			dropdownLabels[i].setFont(BOLD_S);
			dropdowns[i].setFont(MED);
			//thank goodness for the default renderer
			((JLabel)dropdowns[i].getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		}
		//add event listeners
		about.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Box box = Box.createHorizontalBox();
				String authorLink = "https://github.com/ViveTheModder";
				JLabel author = new JLabel(HTML_A_START+"ViveTheModder"+HTML_A_END);
				box = Box.createHorizontalBox();
				JLabel text = new JLabel("Made by: ");
				text.setFont(BOLD_S);
				author.setFont(BOLD_S);
				box.add(text);
				box.add(author);					
				author.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						try {
							Desktop.getDesktop().browse(new URI(authorLink));
						} catch (IOException | URISyntaxException e1) {
							errorBeep();
							JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
						}
					}});
				JOptionPane.showMessageDialog(null, box, WINDOW_TITLE, 1);
			}
		});
		swapBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int[] anmSelect = new int[2];
				int[] anmIds = new int[4];
				String errorMsg="";
				
				for (int i=0; i<4; i++) anmIds[i] = dropdowns[i].getSelectedIndex();
				anmSelect[0] = anmIds[1]-anmIds[0]+1;
				anmSelect[1] = anmIds[3]-anmIds[2]+1;

				for (int i=0; i<2; i++)
				{
					String anmLabelText = anmLabels[i].getText();
					if (paks[i]==null) errorMsg+=anmLabelText+" has not been provided!\n";
					if (anmSelect[i]<2) errorMsg+="Invalid selection for "+anmLabelText+" (must contain 2 or more ANMs)!\n";
				}
				if (anmSelect[0]!=anmSelect[1]) errorMsg+="Animation selections for both ANM files must match!\n";
				if (!errorMsg.equals("")) 
				{
					errorBeep();
					JOptionPane.showMessageDialog(null, errorMsg, WINDOW_TITLE, 0);
				}
				else
				{
					try 
					{
						long start = System.currentTimeMillis();
						cmd.Main.swap(paks[0],paks[1],anmIds);
						long end = System.currentTimeMillis();
						double time = (end-start)/1000.0;
						DEF_TOOLKIT.beep();
						JOptionPane.showMessageDialog(null, "Animation swap performed successfully in "+time+" seconds!");
					} 
					catch (IOException ex) 
					{
						errorBeep();
						JOptionPane.showMessageDialog(frame, ex.getClass().getSimpleName()+": "+ex.getMessage(), "Exception", 0);
					}
				}
			}
		});
		for (int i=0; i<2; i++)
		{
			final int index=i;
			openPaks[i].addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					try 
					{
						paks[index] = getPakFromChooser();
						//change file labels to display file name in green, otherwise just reset to default
						if (paks[index]!=null)
						{
							anmLabels[index].setForeground(new Color(83,187,17));
							anmLabels[index].setText(paks[index].getFileName());
						}
						else 
						{
							anmLabels[index].setForeground(Color.BLACK);
							anmLabels[index].setText("ANM PAK File "+(index+1));
						}
					} 
					catch (IOException ex) 
					{
						JOptionPane.showMessageDialog(frame, ex.getClass().getSimpleName()+": "+ex.getMessage(), "Exception", 0);
					}
				}
			});
			checkboxes[i].addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					try
					{
						String[] newNames = new String[AnmPak.NUM_ANMS];
						if (checkboxes[index].isSelected()) newNames=getAnmNames(false);
						else newNames=getAnmNames(true);
						for (int j=0; j<2; j++)
						{
							if (index==j) 
							{
								//maintain selected index when changing animation names
								for (int k=2*j; k<2*j+2; k++)
								{
									int lastSelComboIndex = dropdowns[k].getSelectedIndex();
									dropdowns[k].setModel(new DefaultComboBoxModel<String>(newNames));
									dropdowns[k].setSelectedIndex(lastSelComboIndex);
								}
							}
						}
					}
					catch (IOException ex)
					{
						JOptionPane.showMessageDialog(frame, ex.getClass().getSimpleName()+": "+ex.getMessage(), "Exception", 0);
					}
				}
			});
		}
		//add components
		fileMenu.add(openPaks[0]);
		fileMenu.add(openPaks[1]);
		helpMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		for (int i=0; i<4; i++)
		{
			if (i%2==0)
			{
				panel.add(new JLabel(" "),gbc);
				panel.add(anmLabels[i/2],gbc);
				panel.add(new JLabel(" "),gbc);
			}
			panel.add(dropdownLabels[i],gbc);
			panel.add(dropdowns[i],gbc);
			panel.add(new JLabel(" "),gbc);
		}
		chkboxBox.add(checkboxes[0]);
		chkboxBox.add(checkboxes[1]);
		panel.add(chkboxBox,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(swapBtn,gbc);
		//set frame properties
		frame.add(panel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		frame.setLocationRelativeTo(null);
		frame.setSize(512,512);
		frame.setTitle(WINDOW_TITLE);
		frame.setVisible(true);
	}
	public static void main(String[] args) 
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setApplication();
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | IOException e) 
		{
			e.printStackTrace();
		}
	}
}