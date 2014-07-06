package sudoku;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MaskFormatter;

public class SudokuFrame extends JFrame {
	
	/*
	 * Panels, buttons and other components that go in the window
	 */
	protected JPanel solvedPuzzlePanel;
	protected JPanel unsolvedPuzzlePanel;
	protected JPanel center;
	protected JComponent content;
	protected JPanel controls;
	protected JButton solveButton;
	protected JButton quitButton;
	protected JLabel numberOfSolutions;
	
	
	protected Sudoku game;		//internal game board
	protected int[][] gameBoard;//array that takes user input and is passed into Sudoku object
	
	
	protected JTextField[][] puzzleFields;	//fields that allow user input
	protected JTextField[][] solutionFields;//unalterable fields that show solution of puzzle
	
	public static int WINDOW_X_DIMENSION =  700;
	public static int WINDOW_Y_DIMENSION = 450;
	
	protected static String NUMBER_OF_SOLUTIONS = "Number of Solutions: ";
	
	public SudokuFrame() {
		
		/*
		 * Set up internal game structures
		 */
		super("Sudoku Solver");
		gameBoard = new int[Sudoku.SIZE][Sudoku.SIZE];
		puzzleFields = new JTextField[Sudoku.SIZE][Sudoku.SIZE];
		solutionFields = new JTextField[Sudoku.SIZE][Sudoku.SIZE];
		game = new Sudoku(gameBoard);
			
		
		//set up the left pane that has the unsolved puzzle
		unsolvedPuzzlePanel = getSinglePuzzleSquare(true);

		//setup right pane that will have the solution 
		solvedPuzzlePanel = getSinglePuzzleSquare(false);

		//Set up our center panel that will hold both puzzle panes
		center = new JPanel();
		center.setLayout(new GridLayout(1,2));
		center.setPreferredSize(new Dimension(30,40));

		//add puzzle panes to center
		center.add(unsolvedPuzzlePanel);
		center.add(solvedPuzzlePanel);
		
		
		content = (JComponent) getContentPane();
		content.setLayout(new BorderLayout(4,4));
		content.add(center,BorderLayout.CENTER);
		
		//give left and right panes appropriate borders
		unsolvedPuzzlePanel.setBorder(new TitledBorder("Puzzle"));
		solvedPuzzlePanel.setBorder(new TitledBorder("Solution"));

		//add panel with controls to bottom of main panel
		content.add(setupControlPanel(),BorderLayout.SOUTH);
		
		
		//setLocationByPlatform(true);
		setPreferredSize(new Dimension(700,450));	//arbitrary values that look good on my computer.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);	//make window appear in middle of screen
		setVisible(true);
	}
	
	/*
	 * This method sets up a the panel that holds the controls of the application. 
	 */
	protected JComponent setupControlPanel(){
		controls = new JPanel(new GridBagLayout());

		numberOfSolutions = new JLabel(NUMBER_OF_SOLUTIONS);
		
		//Set up solve button and add listener to update the numberOfSolutions label to 
		//reflect the number of solutions to the puzzle
		solveButton = new JButton("Solve");
		solveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				//set up game and solve it
				game = new Sudoku(gameBoard);
				int numSolutions = game.solve();
				
				if(numSolutions==0){
					numberOfSolutions.setText(NUMBER_OF_SOLUTIONS + "None");
				}
				else if(numSolutions>Sudoku.MAX_SOLUTIONS){
					numberOfSolutions.setText(NUMBER_OF_SOLUTIONS + "100+");
				}
				else {
					numberOfSolutions.setText(NUMBER_OF_SOLUTIONS + numSolutions);
				}

				//update the fields on the solution panel of the GUI
				showPuzzleSolution();

			}
		});
		
		//set up quit button and add appropriate listener
		quitButton = new JButton("Quit");
		quitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});
		
		//add buttons and label to panel and return the panel
		controls.add(numberOfSolutions);
		controls.add(solveButton);
		controls.add(quitButton);
		return controls;
	}
	
	/*
	 * If the current Sudoku puzzle can be solved, this method sets the JTextFields of the 
	 * solution pane to show the first solution that was found. If the current puzzle can't
	 * be solved, it clears every field of the solution panel.
	 */
	protected void showPuzzleSolution(){
		int[][] solution = game.getSolutionGrid();
		
		for(int i = 0; i<Sudoku.SIZE;i++){
			for(int j = 0; j<Sudoku.SIZE;j++){
				
				//if the solution is null, just make every field blank. Otherwise, set it to the value
				//
				if(solution==null)	solutionFields[i][j].setText(" ");
				else 				solutionFields[i][j].setText(String.valueOf(solution[i][j]));
			}
		}
	}
	
	
	/*
	 * Returns a JPanel that represents a single representation of the puzzle. The 
	 * returned JPanel has 9x9 grid of JTextField's that represent the grid of a Sudoku game.
	 * If the passed parameter is true, then the user can modify the fields in the panel. If 
	 * the passed parameter is false, the user can't modify the fields in the panel.
	 * 
	 * Note: The internal grid of the board is 9x9 but, in order to make the GUI look good, 
	 * this method sets up the puzzle by sequentially adding 3x3 squares. In order to sync 
	 * up the listeners so that the value at grid[i][j] was reflected at the puzzleFields[i][j],
	 * I had to use some wonky and convoluted loops. 
	 */
	protected JPanel getSinglePuzzleSquare(boolean editable){
		JPanel puzzlePane = new JPanel(new GridLayout(Sudoku.PART,Sudoku.PART));
		puzzlePane.setPreferredSize(new Dimension(15,20));
		
		//create JPanel that represents single 3x3 'square' of the Sudoku board.
		JPanel square;
		
		int end = Sudoku.PART;
		int stop = Sudoku.PART;
		for(int i = 0; i<Sudoku.SIZE; i++){
			if((i+Sudoku.PART)%3==0 && i!=0){
				if(stop>=Sudoku.SIZE) stop = Sudoku.PART;
				end = end+ Sudoku.PART;
			}

			square = new JPanel();
			square.setBorder(BorderFactory.createEtchedBorder());
			square.setLayout(new GridLayout(Sudoku.PART,Sudoku.PART));
			for(int j = end - Sudoku.PART; j<end;j++){
				for(int k  = stop -Sudoku.PART; k<stop;k++){
					square.add(addTextField(editable, j, k));
				}
			}
			stop = stop + Sudoku.PART;
			puzzlePane.add(square);
		}
		
		return puzzlePane;
	}
	
	/**
	 * Returns a single JTextField that is to be added to the puzzle. 
	 * 
	 * @param editable Whether the user should be able to edit the returned JTextField.
	 * @param rowIndex An int representing the row in the grid the JTextField corresponds to.
	 * @param columnIndex An int representing the column in the grid the JTextField corresponds to.
	 * @return A single JTextField
	 */
	protected JTextField addTextField( boolean editable, final int rowIndex, final int columnIndex){
		JTextField curField;
		
		//if the text field is editable, add listeners to it.
		if(editable){
			
			//set up text field that will only accept a numeric character
			puzzleFields[rowIndex][columnIndex] = new JFormattedTextField(createFormatter("#"));
			curField = puzzleFields[rowIndex][columnIndex];
			curField.getDocument().addDocumentListener(new DocumentListener(){

				@Override
				public void changedUpdate(DocumentEvent arg0) {
					//do nothing because changedUpdate only fires from a StyledDocument
					//whereas this is a PlainDocument
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					updateCell(rowIndex,columnIndex, puzzleFields[rowIndex][columnIndex].getText());					
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					updateCell(rowIndex,columnIndex, " ");
				}
				
			});
			((JFormattedTextField) curField).setFocusLostBehavior(JFormattedTextField.COMMIT);
		}
		else{
			solutionFields[rowIndex][columnIndex] = new JFormattedTextField();
			curField = solutionFields[rowIndex][columnIndex];
		}
		curField.setHorizontalAlignment(JTextField.CENTER);
		curField.setEditable(editable);
		return curField;
}
	
	/*
	 * Attempts to update the given cell at the specified row and column with
	 * the passed text. If the text is not a numeric character, then that entry
	 * is just set to zero.
	 */
	protected void updateCell(int row, int column, String text){

		try{
			gameBoard[row][column] = Integer.parseInt(text);
		}
		catch(NumberFormatException e){
			gameBoard[row][column] = 0;
		}
	}
	
	/*
	 * Prints the board to the console. For testing purposes. 
	 */
	protected void printBoard(){
		for(int i =0; i<9;i++){
			for(int j = 0; j<9; j++){
				System.out.print(gameBoard[i][j]+ " ");
			}
			System.out.println();
		}
	}
	
	/*
	 * Returns a MaskFormatter that only allows the user to enter a single
	 * numeric character.
	 */
	protected MaskFormatter createFormatter(String s){
		MaskFormatter formatter = null;
		try{
			formatter = new MaskFormatter(s);
		}
		catch(ParseException e){
			System.err.println("Formatter is bad: " + e.getMessage());
			System.exit(1);
		}
		return formatter;
	}
	
	
	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		new SudokuFrame();
	}
}
