package game;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.HashSet;
import java.util.Set;

class Field extends GridPane {
    private static final double fieldSize = 576.0;
    private Selection selection = new Selection();
    private Piece[][] pieces = new Piece[8][8];
    private Side playerSide = Side.WHITE;
    private boolean mustJump = false;
    private boolean multipleJump = false;
    private Set<Piece> whitePieces = new HashSet<>();
    private Set<Piece> blackPieces = new HashSet<>();
    private Set<Piece> capturedPieces = new HashSet<>();

    Field() {
        Image blackPieceImg = new Image("img/blackPiece.png");
        Image whitePieceImg = new Image("img/whitePiece.png");
        this.setPrefSize(fieldSize, fieldSize);
        this.setLayoutX(38.0);
        this.setLayoutY(38.0);
        selection.setFitHeight(fieldSize / 8.0);
        selection.setFitWidth(fieldSize / 8.0);
        int i, j;
        for(i = 0; i < 8; i++) {
            this.getColumnConstraints().add(new ColumnConstraints(fieldSize / 8.0));
            this.getRowConstraints().add(new RowConstraints(fieldSize / 8.0));
        }
        for(i = 0; i < pieces.length; i++) {
            j = (i % 2 == 0) ? 1 : 0;
            while(j < pieces.length) {
                if (i < 3) {
                    pieces[i][j] = new Piece(Side.BLACK, blackPieceImg, fieldSize, i, j);
                    this.add(pieces[i][j], j, i);
                    blackPieces.add(pieces[i][j]);
                }
                else if (i > 4) {
                    pieces[i][j] = new Piece(Side.WHITE, whitePieceImg, fieldSize, i, j);
                    this.add(pieces[i][j], j, i);
                    whitePieces.add(pieces[i][j]);
                }
                j += 2;
            }
        }
        this.setOnMouseClicked((final MouseEvent click) -> {
            int row = (int) (click.getY() * 8 / fieldSize);
            int col = (int) (click.getX() * 8 / fieldSize);
            if (squareContainsPiece(row, col) && !multipleJump)
                selectPiece(pieces[row][col]);
            else move(row, col);
        });
    }


    private void selectPiece(Piece piece) {
        if (piece.hasSide(playerSide)) {
            selection.target = piece;
            this.getChildren().remove(selection);
            this.add(selection, piece.col, piece.row);
        }
    }


    private boolean squareExists(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }


    private boolean squareContainsPiece(int row, int col) {
        return pieces[row][col] != null;
    }


    private void move(int row, int col) {
        if (row % 2 != col % 2 && selection.isSet()) {
            int diffR = Math.abs(row - selection.target.row);
            int diffC = Math.abs(col - selection.target.col);
            if (diffR == 1 && diffC == 1) simpleMove(row, col);
            else if(diffR == 2 && diffC == 2) jump(row, col);
        }
    }


    private void simpleMove(int row, int col) {
        if (!mustJump) {
            Piece piece = selection.target;
            boolean moveBack = (playerSide == Side.BLACK) != (row - piece.row > 0);
            if(!moveBack || piece.isKing()) {
                movePiece(piece, row, col);
                piece.tryToBecomeKing();
                switchPlayer();
            }
        }
    }


    private void movePiece(Piece piece, int row, int col) {
        pieces[piece.row][piece.col] = null;
        pieces[row][col] = piece;
        this.getChildren().remove(piece);
        this.add(piece, col, row);
        piece.row = row;
        piece.col = col;
    }


    private void removeCapturedPieces() {
        for(Piece captured : capturedPieces) {
            pieces[captured.row][captured.col] = null;
            if(playerSide == Side.WHITE) blackPieces.remove(captured);
            else whitePieces.remove(captured);
            this.getChildren().remove(captured);
        }
    }


    private void jump(int row, int col) {
        Piece piece = selection.target;
        int capturedX = piece.col + (col - piece.col) / 2;
        int capturedY = piece.row + (row - piece.row) / 2;
        if(squareContainsPiece(capturedY, capturedX)) {
            Piece captured = pieces[capturedY][capturedX];
            if (!captured.hasSide(playerSide) && capturedPieces.add(captured)) {
                movePiece(piece, row, col);
                piece.tryToBecomeKing();
                if (canJump(piece)) {
                    selectPiece(piece);
                    multipleJump = true;
                } else {
                    removeCapturedPieces();
                    multipleJump = false;
                    switchPlayer();
                }
            }
        }
    }

    private boolean canJump(Piece piece) {
        int rowShift = 1, colShift = 1, row, col;
        for(int i = 0, c = 1; i < 4; i++, c *= (-1)) {
            rowShift *= c;
            colShift *= -c;
            row = piece.row + rowShift;
            col = piece.col + colShift;
            if(squareExists(row, col) && squareContainsPiece(row, col)
                    && !pieces[row][col].hasSide(playerSide)
                    && !capturedPieces.contains(pieces[row][col])) {
                row += rowShift;
                col += colShift;
                if(squareExists(row, col) && !squareContainsPiece(row, col)) return true;
            }
        }
        return false;
    }


    private void switchPlayer() {
        playerSide = playerSide == Side.WHITE ? Side.BLACK : Side.WHITE;
        this.getChildren().remove(selection);
        selection.target = null;
        capturedPieces.clear();
        Set<Piece> playerPieces = playerSide == Side.WHITE ? whitePieces : blackPieces;
        mustJump = false;
        for(Piece piece : playerPieces) {
            if(canJump(piece)) {
                System.out.println(piece.row + "\t" + piece.col);
                mustJump = true;
                break;
            }
        }
    }
}