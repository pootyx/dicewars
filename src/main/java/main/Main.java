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

    private Cell[][] cells;

    private Cell[] war = new Cell[2];

    private JFrame  frame;
    private JButton reset;
    private JButton endTurn;
    private List<Player> players;

    private final int gridSize = 4;

    private final ActionListener actionListener = actionEvent -> {
        Object source = actionEvent.getSource();
        if (source == reset) {
            initPlayers();
        } else if (source == endTurn) {
            endTurn();
        } else {
            handleCell((Cell) source);
        }
    };

    private Main() {
        cells = new Cell[gridSize][gridSize];
        frame = new JFrame("Dice Wars");
        frame.setSize(SIZE, SIZE);
        frame.setLayout(new BorderLayout());

        initializeButtonPanel();
        initializeGame();

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Remove the looser from the player Array so it will not have any more turn
     * Using removeIf() so I will not get ConcurrentModificationException
     */
    private void removeLoosers() {
        players.removeIf(player -> getPlayerCells(player).size() == 0);
    }

    /**
     * Add the extra dices at the end of the turn and set the next player as current
     */
    private void addExtraDicesAndPassTurn() {
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
    }

    /**
     * This will ensure to handle actions when the end turn button is pressed
     */
    private void endTurn() {
        removeLoosers();
        addExtraDicesAndPassTurn();
        enemyMovement();
        disableAllCells();
        startTurn();
    }

    private Player getNextPlayer(Player player) {
        try {
            return players.get(players.indexOf(player) + 1);
        } catch (IndexOutOfBoundsException e) {
            return players.get(0);
        }
    }

    /**
     * Handle the automated attacks from the machine in a very simple way.
     * Check every available cell's neighbours and attack whenever possible.
     */
    private void enemyMovement() {
        for (Player player : players) {
            if (player.isNpc() && player.isCurrentPlayer()){
                List<Cell> playerCells = getPlayerCells(player);
                for (Cell playerCell : playerCells) {
                    if (playerCell.getValue() > 1) {
                        war[0] = playerCell;
                        Cell[] neighbours = playerCell.getNeighbours(cells, gridSize);
                        for (Cell neighbour : neighbours) {
                            if (neighbour == null) { // weird flex but ok
                                continue;
                            }
                            if (neighbour.getPlayer() != player) {
                                war[1] = neighbour;
                                if (isAttackSuccessful(war)) {
                                    moveToWonField(neighbour, war);
                                    playerCells = getPlayerCells(player);
                                } else {
                                    playerCell.setValue(1);
                                    playerCell.setText("1");
                                }
                                break;
                            }
                        }
                    }
                }
                addExtraDicesAndPassTurn();
                if (!getNextPlayer(player).isNpc()) {
                    if (getPlayerCells(getNextPlayer(player)).size() == 0) {
                        youLost();
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private void youLost() {
        JOptionPane.showMessageDialog(
                frame, "You have lost!", "Game Over",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void initializeButtonPanel() {
        JPanel buttonPanel = new JPanel();

        reset = new JButton("Reset");
        endTurn = new JButton("End Turn");

        reset.addActionListener(actionListener);
        endTurn.addActionListener(actionListener);

        buttonPanel.add(reset);
        buttonPanel.add(endTurn);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * This will ensure to only enable the current player cells with more then 1 dice
     */
    private void startTurn() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (cells[row][col].getPlayer().isCurrentPlayer() && cells[row][col].getValue() > 1) {
                    for (Cell cell : cells[row][col].getNeighbours(cells, gridSize)) {
                        try {
                            if (!cell.getPlayer().isCurrentPlayer()){
                                cells[row][col].setEnabled(true);
                                break;
                            }
                        } catch (NullPointerException e) {
                            // edge cell
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize the cells and add them to the grid
     */
    private void initializeGrid() {
        Container grid = new Container();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col] = new Cell(row, col, actionListener);
                grid.add(cells[row][col]);
            }
        }
        grid.setLayout(new GridLayout(gridSize, gridSize));
        frame.add(grid, BorderLayout.CENTER);
    }

    /**
     * Initialize the playground (cells, and players)
     */
    private void initializeGame() {

        initializeGrid();
        resetAllCells();
        initPlayers();
        addStarterDices();
        startTurn();

    }

    /**
     * Reset ever cell to start a new game
     */
    private void resetAllCells() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col].reset();
            }
        }
    }

    /**
     * Get the available colors
     * @return list of colors
     */
    private List<Color> getColors() {
        List<Color> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        colors.add(Color.BLACK);
        colors.add(Color.RED);
        colors.add(Color.MAGENTA);
        return colors;
    }

    /**
     * Create a list of players with its required attributes
     * @return a list of players
     */
    private List<Player> createPlayerList() {
        List<Player> players = new ArrayList<>();
        List<Color> colors = getColors();

        for (int i = 0; i < NUMBER_OF_PLAYER; i++){
            Random rand = new Random();
            Player player = new Player();
            int index = rand.nextInt(colors.size());
            player.setColor(colors.get(index));
            colors.remove(index); //remove from the Array so there will be no duplicate
            player.setNpc(true); //TODO: there should be only one human player
            players.add(player);
        }

        /**
         * This will be the human player
         */
        players.get(0).setNpc(false); //TODO: finish the npc feature
        players.get(0).setCurrentPlayer(true);

        return players;
    }

    /**
     * Initialize the players and add them to the cells randomly
     */
    private void initPlayers() {

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

    /**
     * Add the extra dices at the end of the turn to the current player
     * @param player is the current player
     */
    private void addExtraDices(Player player){
        Random rand = new Random();
        List<Cell> playerCells = getPlayerCells(player);
        int numberOfDicesToGive = playerCells.size();
        List<Cell> cells = getPlayerCellsBelowValue(player, 8);
        if (cells.size() > 0) {
            for (int d = numberOfDicesToGive; d != 0; d--){
                int index = rand.nextInt(cells.size());
                cells.get(index).incrementValue();
                if (cells.get(index).getValue() == 8) {
                    cells.remove(index);
                    if(cells.size() == 0){
                        break;
                    }
                }
            }
        }
    }

    private List<Cell> getPlayerCellsBelowValue(Player player, int value) {
        List<Cell> result = new ArrayList<>();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (cells[row][col].getPlayer() == player && cells[row][col].getValue() < value) {
                    result.add(cells[row][col]);
                }
            }
        }
        return result;
    }

    /**
     * Get the cells of the specified player
     * @param player is the player whose cells will be returned
     * @return the cells of the given player
     */
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

    /**
     * Enable the cells which does not belong to the current player and next to the attack cell
     * @param cell is the cell where the attack started
     */
    private void enableEnemyCells(Cell cell){
        Cell[] neighbours = cell.getNeighbours(cells, gridSize);
        for (Cell neighbour : neighbours) {
            if (neighbour == null || neighbour.getPlayer().isCurrentPlayer()) {
                continue;
            }
            neighbour.setEnabled(true);
        }
    }

    /**
     * Disable every cell so only those will be enabled that should be picked
     */
    private void disableAllCells() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                cells[row][col].setEnabled(false);
            }
        }
    }

    /**
     * Roll with every dices on the given cell
     * @param cell is the cell where the rolls happen
     * @return the accumulated result of the rolls
     */
    private int roll(Cell cell){
        int result = 0;
        for (int i = 0; i < cell.getValue(); i++) {
            result += (int)(Math.random()*6+1);
        }
        return result;
    }

    /**
     * Check if the given war was successful or not
     * @param war is the current war with war[0] as the attacker and war[1] as the defender cell
     * @return true if the attack was successful
     */
    private boolean isAttackSuccessful(Cell [] war) {
        Cell attacker = war[0];
        Cell defender = war[1];

        return roll(attacker) > roll(defender);
    }

    /**
     * Set the properties of the won cell based on the attacker cell
     * @param currentCell is the cell which has been captured
     * @param war is the current war between two cells
     */
    private void moveToWonField(Cell currentCell, Cell[] war) {
        Cell attackerCell = war[0];
        currentCell.setPlayer(attackerCell.getPlayer());
        currentCell.setForeground(attackerCell.getForeground());
        currentCell.setValue(attackerCell.getValue()-1);
        currentCell.setText(String.valueOf(attackerCell.getValue()-1));
        attackerCell.setValue(1);
        attackerCell.setText("1");
    }

    /**
     * Enable the cells which belongs to the given player
     * @param player is the player which cells will be enabled
     */
    private void enablePlayerCells(Player player) {
        List<Cell> cells = getPlayerCells(player);
        for (Cell cell : cells) {
            if(cell.getValue() > 1) {
                cell.setEnabled(true);
            }
        }
    }

    /**
     * Handles the cell which has been clicked
     * @param cell is the currently clicked cell
     */
    private void handleCell(Cell cell) {

        if (cell.getPlayer().isCurrentPlayer()){
            war[0] = cell;
            disableAllCells();
            enableEnemyCells(cell);
        } else {
            war[1] = cell;
            if (isAttackSuccessful(war)) {
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

    /**
     * Check if the player won the game
     */
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

    private static void run() {
        try {
            // Totally optional. But this applies the look and
            // feel for the current OS to the a application,
            // making it look native.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) { }
        // Launch the program
        new Main();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> Main.run());
    }
}