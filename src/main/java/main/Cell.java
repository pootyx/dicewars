package main;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Set;

public class Cell extends JButton {

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    private final int row;
    private final int col;
    private       int value;
    private Player player;

    // This fixed amount of memory is to avoid repeatedly declaring
    // new arrays every time a cell's neighbours are to be retrieved.
    private static Cell[] reusableStorage = new Cell[8];

    private static final int MINE = 10;

    private static final int gridSize = 4;

    Cell(final int row, final int col,
         final ActionListener actionListener) {
        this.row = row;
        this.col = col;
        addActionListener(actionListener);
        setText("");

    }

    int getValue() {
        return value;
    }

    void setValue(int value) {
        this.value = value;
    }

    void incrementValue() {
        this.value++;
        this.setText(String.valueOf(value));
    }

    boolean isAMine() {
        return value == MINE;
    }

    void reset() {
        setValue(1);
        setEnabled(false);
        setText("1");
    }

    void reveal() {
        setEnabled(false);
        setText(isAMine() ? "X" : String.valueOf(value));
    }

    public Cell[] getNeighbours(Cell[][] cells) {
        // Empty all elements first
        for (int i = 0; i < reusableStorage.length; i++) {
            reusableStorage[i] = null;
        }

        int index = 0;

        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                // Make sure that we don't count ourselves
                if (rowOffset == 0 && colOffset == 0) {
                    continue;
                }
                int rowValue = row + rowOffset;
                int colValue = col + colOffset;

                if (rowValue < 0 || rowValue >= gridSize
                        || colValue < 0 || colValue >= gridSize) {
                    continue;
                }

                reusableStorage[index++] = cells[rowValue][colValue];
            }
        }

        return reusableStorage;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Cell cell = (Cell) obj;
        return row == cell.row &&
                col == cell.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }


    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer(){
        return player;
    }

    public void cascade(Set<Cell> positionsToClear, Cell[][] cells) {
        while (!positionsToClear.isEmpty()) {
            // Set does not have a clean way for retrieving
            // a single element. This is the best way I could think of.
            Cell cell = positionsToClear.iterator().next();
            positionsToClear.remove(cell);
            reveal();

//            getNeighbours(reusableStorage, cells);
            for (Cell neighbour : reusableStorage) {
                if (neighbour == null) {
                    break;
                }
                if (neighbour.getValue() == 0
                        && neighbour.isEnabled()) {
                    positionsToClear.add(neighbour);
                } else {
                    neighbour.reveal();
                }
            }
        }
    }
}
