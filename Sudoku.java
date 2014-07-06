package sudoku;

import java.util.*;

/*
 * This class encapsulates a single game of Sudoku. 
 */
public class Sudoku {

	
	private int[][] grid;
	private int[][] firstSolutionGrid;
	private String firstSolutionString;
	private boolean isSolvable;
	
	//this holds a sorted list of spots where the spots with lowest number
	//of assignable spots are first
	private ArrayList<Spot> orderedAssignableNumbers;
	private ArrayList<Spot> fixedSpots;
	private Map<Spot, Spot> visitedSpots;
	
	private static Integer[] numbers = {
		1,2,3,4,5,6,7,8,9
	};
	
	
	public static final int SIZE = 9;  // size of the whole 9x9 puzzle
	public static final int PART = 3;  // size of each 3x3 part
	public static final int MAX_SOLUTIONS = 100;
	
	/**
	 * Sets up based on the given ints.
	 */
	public Sudoku(int[][] ints) {
		grid = ints;
		firstSolutionGrid = null;
		firstSolutionString = null;
		orderedAssignableNumbers = new ArrayList<Spot>();
		fixedSpots = new ArrayList<Spot>();
		visitedSpots = new HashMap<Spot, Spot>();
		/*
		 * 
		 */
		for(int i = 0; i < grid.length;i++){
			for(int j = 0; j < grid[i].length;j++){
				if(spaceIsEmpty(i,j)) {
					Spot temp = new Spot(i,j);
					visitedSpots.put(temp, temp);
					orderedAssignableNumbers.add(temp);
				}
				else{
					fixedSpots.add(new Spot(i,j,grid[i][j]));
				}
			}
		}
		
		isSolvable = checkIfSolvable();
		Collections.sort(orderedAssignableNumbers);
	}
	
	protected boolean spaceIsEmpty(int row, int column){
		return grid[row][column] == 0;
	}
	
	public Sudoku(String text){
		this(Grid.textToGrid(text));
	}
	
	/*
	 * 
	 */
	private boolean checkIfSolvable(){
		boolean king = true;
		for(Spot s: fixedSpots){
			grid[s.rowIndex][s.columnIndex] = 0;
			if(!s.calculatePossibleEntries().contains(s.getValue())) {
				king = false;
			}
			grid[s.rowIndex][s.columnIndex] = s.getValue();
			if(king == false) break;

		}
		return king;
	}
	
	/**
	 * Solves the puzzle, invoking the underlying recursive search.
	 * Returns the number of solution for this sudoku puzzle up to 101.
	 * If the puzzle can't be solved, returns -1.
	 */
	public int solve() {
		//start the recursion at the beginning of the list
		if(!isSolvable) return 0;
		return placeSingleSpot(0, new int[1]);
	}
	
	private int placeSingleSpot(int index, int[] currentTotal){
		//System.out.println(currentTotal[0]);
		if(currentTotal[0]>MAX_SOLUTIONS) return 0;
		if(index==orderedAssignableNumbers.size()) {
			if(firstSolutionString==null && firstSolutionGrid==null) {
				firstSolutionString = this.toString();
				firstSolutionGrid = Grid.textToGrid(firstSolutionString);
			}
			//System.out.println(this);
			currentTotal[0]++;
			return 1;
		}
		
		//if(firstSolutionString!=null) return 0;
		Spot curSpot = orderedAssignableNumbers.get(index);
		HashSet<Integer> possibleValues = curSpot.calculatePossibleEntries();
		
		//if its not possible to assign any numbers to the current spot, you've reached a
		//dead end so return.
			if(possibleValues.size()==0){
				return 0;
			}
		
			int total = 0;
			index++;

			for(Integer currentVal: possibleValues){
				curSpot.setValue(currentVal);
				total+=placeSingleSpot(index, currentTotal);
			}
		curSpot.setValue(0);
		return total;
	}
	
	/*
	 * Returns a solution 
	 */
	public String getSolutionText() {
		return firstSolutionString; 
	}
	
	public int[][] getSolutionGrid(){
		return firstSolutionGrid;
	}
	

	/*
	 * Returns a string representation of the board.
	 */
	@Override
	public String toString(){
		StringBuilder stringSolution = new StringBuilder();
		Spot temp = null;

		for(int i = 0; i<grid.length;i++){
			for(int j = 0; j<grid[i].length;j++){
				if(grid[i][j]!=0){
					stringSolution.append(grid[i][j]+ " ");
				}
				else{
					temp = visitedSpots.get(new Spot(i,j));
					stringSolution.append(temp.getValue() + " ");
				}
			}
			stringSolution.append("\n");
		}
		return new String(stringSolution);
	}
	/*
	 * This class represents a single spot in the sudoku grid.
	 */
	private class Spot implements Comparable<Spot>{
		private int rowIndex;
		private int columnIndex;
		private int curValue;
		private Set<Integer> possibleEntries;
		
		public Spot(int row, int column){
			//this.rowIndex = row;
			//this.columnIndex = column;
			//curValue = 0;
			//possibleEntries = null;
			this(row,column,0);
		}
		
		public Spot(int row, int column, int value){
			this.rowIndex = row;
			this.columnIndex = column;
			curValue = value;
			possibleEntries = null;
		}
		
		
		/*
		 * Returns a set of integers this spot can take given the current state of the board. 
		 */
		public HashSet<Integer> calculatePossibleEntries(){
			
			HashSet<Integer> currentPossibleEntries = new HashSet<Integer>();
			Collections.addAll(currentPossibleEntries, numbers);
			clearConflictingRowValues(currentPossibleEntries);
			clearConflictingColumnValues(currentPossibleEntries);
			clearConflictingSquaresValues(currentPossibleEntries);
			
			if(possibleEntries==null) possibleEntries = currentPossibleEntries;
			return currentPossibleEntries;
		}
		
		/*
		 * Remove all numbers in the same column as this spot.
		 */
		private void clearConflictingRowValues(Set<Integer> entries){

			//remove all spots that have been filled in
			Spot temp = null;
			for(int i = 0; i<SIZE;i++){
				if(grid[rowIndex][i]!=0){
					entries.remove(grid[rowIndex][i]);
				}
				else{
					temp = visitedSpots.get(new Spot(rowIndex, i));
					if(temp != null) entries.remove(temp.getValue());
				}
			}
		}
		
		/*
		 * Remove all numbers in the same row as the this spot.
		 */
		private void clearConflictingColumnValues(Set<Integer> entries){
			Spot temp = null;
			for(int i = 0; i<SIZE; i++){
				if(grid[i][columnIndex]!=0){
					entries.remove(grid[i][columnIndex]); 
				}
				else{
					temp = visitedSpots.get(new Spot(i, columnIndex));
					if(temp != null) entries.remove(temp.getValue());
				}
			}
		}
		
		
		/*
		 * Removes all numbers in the same 3x3 box as this spot.
		 */
		private void clearConflictingSquaresValues(Set<Integer> entries){
			
			//first find which of the nine squares the spot is in
			int rowBoundary, columnBoundary;
			if(rowIndex>=2*PART) 		rowBoundary = 3*PART;
			else if(rowIndex>=PART) 	rowBoundary =2*PART;
			else						rowBoundary =PART;
			
			if(columnIndex>=2*PART)		columnBoundary=3*PART;
			else if(columnIndex>=PART)	columnBoundary=2*PART;
			else						columnBoundary=PART;
			
			//go through the square and eliminate any possible entries that may
			Spot temp = null;
			for(int i = rowBoundary-PART;i<rowBoundary;i++){
				for(int j = columnBoundary-PART;j<columnBoundary;j++){
					temp = visitedSpots.get(new Spot(i,j));
					if(temp != null) entries.remove(temp.getValue());
					entries.remove(grid[i][j]);
				}
			}
		}
		
		/*
		 * Returns the current value of the spot.
		 */
		public int getValue(){
			return curValue;
		}
		
		/*
		 * Sets the current value of the spot to the passed parameter.
		 */
		public void setValue(int newValue){
			this.curValue = newValue;
		}
		
		public int compareTo(Spot otherSpot){
			if(this.possibleEntries==null) this.calculatePossibleEntries();
			if(otherSpot.possibleEntries==null) otherSpot.calculatePossibleEntries();
			return this.possibleEntries.size() - otherSpot.possibleEntries.size();
		}
		
		/*
		 * hashCode() and equals() are overridden in accordance with the criteria
		 * set forth in Josh Bloch's "Effective Java", 2006, pages 33-50
		 */
		
		@Override
		public int hashCode(){
			int result = 17;
			result = 31 * result + columnIndex;
			result = 31 * result + rowIndex;
			return result;
		}
		
		@Override
		public boolean equals(Object o){
			if(this==o) return true;
			if(o instanceof Spot){
				Spot anotherSpot = (Spot)o;
				if(anotherSpot.rowIndex==this.rowIndex&&anotherSpot.columnIndex==this.columnIndex){
					return true;
				}
			}
			return false;
		}
		
		@Override 
		public String toString(){
			return "row " + rowIndex + " column " + columnIndex;
		}
	}
	


}
