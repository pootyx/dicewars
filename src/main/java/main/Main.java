package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class Main {

    // The size in pixels for the frame.
    private static final int SIZE = 500;

    private static final int NUMBER_OF_PLAYER = 4;

    private int gridSize;

    private Cell[][] cells;
    private Cell[] war = new Cell[2];

    private JFrame  frame;
    private JButton reset;
    private JButton giveUp;
    private JButton endTurn;
    private List<Player> players;

    private final ActionListener actionListener = actionEvent -> {
        Object source = actionEvent.getSource();
        if (source == reset) {
            createDices();
        } else if (source == giveUp) {
            revealBoardAndDisplay("You gave up.");
        } else if (source == endTurn) {
            endTurn();
        } else {
            handleCell((Cell) source);
        }
    };

    private Main(final int gridSize) {
        this.gridSize = gridSize;
        cells = new Cell[gridSize][gridSize];

        frame = new JFrame("Dice Wars");
        frame.setSize(SIZE, SIZE);
        frame.setLayout(new BorderLayout());

        initializeButtonPanel();
        initializeGrid();

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void removeLoosers() {
        for (Player player : players) {
            if (getPlayerCells(player).size() == 0) {
                players.remove(player);
            }
        }
    }

    private void endTurn() {
        removeLoosers();
        for (int i = 0; i != players.size(); i++) {
            if (players.get(i).isCurrentPlayer()) {
                addExtraDices(players.get(i));
                players.get(i).setCurrentPlayer(false);
                try {
                    players.get(i+1).setCurrentPlayer(true);
                    break;
                } catch (IndexOutOfBoundsException e) {
                    players.get(0).setCurrentPlayer(true);
                }
            }
        }
        disableAllCells();
        startTurn();
    }

    private void initializeButtonPanel() {
        JPanel buttonPanel = new JPanel();

        reset = new JButton("Reset");
        giveUp = new JButton("Give Up");
        endTurn = new JButton("End Turn");

        reset.addActionListener(actionListener);
        giveUp.addActionListener(actionListener);
        endTurn.addActionListener(actionListener);

        buttonPanel.add(reset);
        buttonPanel.add(giveUp);
        buttonPanel.add(endTurn);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void startTurn() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (cells[row][col].getPlayer().isCurrentPlayer() && cells[row][col].getValue() > 1) {
                    for (Cell cell : cells[row][col].getNeighbours(cells)) {
                        try {
                            if (!cell.getPlayer().isCurrentPlayer()){
                                cells[row][col].setEnabled(true);
                                break;
                            }
                        } catch (NullPointerException e) {
                            // edge cell
                            continue;
                        }
                    }
                }
            }
        }
    }

    private void initializeGrid() {
        Container grid = new Container();
        grid.setLayout(new GridLayout(gridSize, gridSize));

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col] = new Cell(row, col, actionListener);
                grid.add(cells[row][col]);
            }
        }
        createDices();
        startTurn();
        frame.add(grid, BorderLayout.CENTER);
    }

    private void resetAllCells() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col].reset();
            }
        }
    }

    private List<Player> createPlayerList() {

        /**
         * Get random color for player
         */
        List<Color> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        colors.add(Color.BLACK);
        colors.add(Color.RED);
        colors.add(Color.MAGENTA);


        /**
         * Add this random color to the player and remove to handle duplicate colors
         */
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_PLAYER; i++){
            Random rand = new Random();
            int index = rand.nextInt(colors.size());
            Player player = new Player();
            player.setColor(colors.get(index));
            colors.remove(index);
            player.setNpc(true);
            players.add(player);
        }

        /**
         * This will be the human player
         */
        players.get(0).setNpc(false);
        players.get(0).setCurrentPlayer(true);

        /**
         * Add the created player to the list
         */

        return players;
    }

    private void createDices() {
        resetAllCells();

        players = createPlayerList();

        // Map all (row, col) pairs to unique integers
        List<Integer> positions = new ArrayList<>(gridSize * gridSize);
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                positions.add(row * gridSize + col);
            }
        }

        Collections.shuffle(positions);

        // Initialize players
        int playerIndex = 0;
        for (int choice : positions) {
            int col = choice % gridSize;
            int row = choice / gridSize;
            cells[row][col].setPlayer(players.get(playerIndex));
            cells[row][col].setForeground(players.get(playerIndex).getColor());
            if (playerIndex < players.size() - 1) {
                playerIndex++;
            } else {
                playerIndex = 0;
            }
        }

        addStarterDices();

//        // Initialize neighbour counts
//        for (int row = 0; row < gridSize; row++) {
//            for (int col = 0; col < gridSize; col++) {
//                if (!cells[row][col].isAMine()) {
//                    cells[row][col].updateNeighbourCount();
//                }
//            }
//        }
    }

    /**
     * Give the players dices based on their number of cells
     */
    private void addStarterDices() {
        Random rand = new Random();

        for (Player player : players) {
            List<Cell> cells = getPlayerCells(player);
            int size = cells.size();
            // if the player do not have any more cell
            if (!(size > 0)) {
                continue;
            }
            for (int d = size * 2; d != 0; d--){
                int index = rand.nextInt(size);
                cells.get(index).incrementValue();
            }
        }
    }

    private void addExtraDices(Player player){
        Random rand = new Random();
        List<Cell> cells = getPlayerCells(player);
        int size = cells.size();
        for (int d = size; d != 0; d--){
            int index = rand.nextInt(size);
            cells.get(index).incrementValue();
        }
    }

    private List<Cell> getPlayerCells(Player player) {
        List<Cell> result = new ArrayList<>();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
               if (cells[row][col].getPlayer() == player) {
                   result.add(cells[row][col]);
               }
            }
        }
        return result;
    }

    private void enableEnemyCells(Cell cell){
        Cell[] neighbours = cell.getNeighbours(cells);
        for (Cell neighbour : neighbours) {
            if (neighbour == null || neighbour.getPlayer().isCurrentPlayer()) {
                continue;
            }
            neighbour.setEnabled(true);
        }
    }

    private void disableAllCells() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col].setEnabled(false);
            }
        }
    }

    private boolean isAttackSuccessfull(Cell [] war) {
        Cell attacker = war[0];
        Cell defender = war[1];

        int resultOfAttacker = 0;
        int resultOfDeffender = 0;

        for (int i = 0; i < attacker.getValue(); i++) {
            resultOfAttacker += (int)(Math.random()*6+1);
        }

        for (int i = 0; i < defender.getValue(); i++) {
            resultOfDeffender += (int)(Math.random()*6+1);
        }

        System.out.println(resultOfAttacker);
        System.out.println(resultOfDeffender);
        return resultOfAttacker > resultOfDeffender;
    }

    private void moveToWonField(Cell cell, Cell[] war) {
        Cell attackerCell = war[0];
        cell.setPlayer(attackerCell.getPlayer());
        cell.setForeground(attackerCell.getForeground());
        cell.setValue(attackerCell.getValue()-1);
        cell.setText(String.valueOf(attackerCell.getValue()-1));
        attackerCell.setValue(1);
        attackerCell.setText("1");
    }

    private void enablePlayerCells(Player player) {
        List<Cell> cells = getPlayerCells(player);
        for (Cell cell : cells) {
            if(cell.getValue() > 1) {
                cell.setEnabled(true);
            }
        }
    }

    private void handleCell(Cell cell) {

        if (cell.getPlayer().isCurrentPlayer()){
            war[0] = cell;
            disableAllCells();
            enableEnemyCells(cell);
        } else {
            war[1] = cell;
            if (isAttackSuccessfull(war)) {
                moveToWonField(cell, war);
                disableAllCells();
                enablePlayerCells(cell.getPlayer());
            } else {
                war[0].setText("1");
                war[0].setValue(1);
                disableAllCells();
                enablePlayerCells(war[0].getPlayer());
            }
        }
        checkForWin();
    }

    private void revealBoardAndDisplay(String message) {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (!cells[row][col].isEnabled()) {
                    cells[row][col].getValue();
                }
            }
        }

        JOptionPane.showMessageDialog(
                frame, message, "Game Over",
                JOptionPane.ERROR_MESSAGE
        );

        createDices();
    }

    private void checkForWin() {
        boolean won = true;
        outer:
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                if (!cell.getPlayer().isCurrentPlayer()) {
                    won = false;
                    break outer;
                }
            }
        }

        if (won) {
            JOptionPane.showMessageDialog(
                    frame, "You have won!", "Congratulations",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private static void run(final int gridSize) {
        try {
            // Totally optional. But this applies the look and
            // feel for the current OS to the a application,
            // making it look native.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) { }
        // Launch the program
        new Main(gridSize);
    }

    public static void main(String[] args) {
        final int gridSize = 4;
        SwingUtilities.invokeLater(() -> Main.run(gridSize));
    }
}