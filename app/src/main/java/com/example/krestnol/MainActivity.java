package com.example.krestnol;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Switch;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private boolean isDarkTheme, isBotMode;
    private int playerWins, botWins, playerDraws, playerVsPlayerWinsX, playerVsPlayerWinsO, playerVsPlayerDraws;
    private int currentPlayer;
    private int[] gameState;
    private boolean gameActive;
    private GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("GameStats", MODE_PRIVATE);
        isDarkTheme = sharedPreferences.getBoolean("darkTheme", false);
        isBotMode = sharedPreferences.getBoolean("botMode", false);

        // Load statistics
        playerWins = sharedPreferences.getInt("playerWins", 0);
        botWins = sharedPreferences.getInt("botWins", 0);
        playerDraws = sharedPreferences.getInt("playerDraws", 0);
        playerVsPlayerWinsX = sharedPreferences.getInt("winsX", 0);
        playerVsPlayerWinsO = sharedPreferences.getInt("winsO", 0);
        playerVsPlayerDraws = sharedPreferences.getInt("draws", 0);

        setTheme(isDarkTheme ? R.style.DarkTheme : R.style.LightTheme);
        setContentView(R.layout.activity_main);

        Switch themeSwitch = findViewById(R.id.themeSwitch);
        themeSwitch.setChecked(isDarkTheme);
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("darkTheme", isChecked).apply();
            recreate();
        });

        Switch modeSwitch = findViewById(R.id.modeSwitch);
        modeSwitch.setChecked(isBotMode);
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isBotMode = isChecked;
            sharedPreferences.edit().putBoolean("botMode", isBotMode).apply();
            setupGame();
        });

        Button statsButton = findViewById(R.id.statsButton);
        statsButton.setOnClickListener(v -> showStats());

        gridLayout = findViewById(R.id.gridLayout);
        setupGame();
    }

    private void setupGame() {
        currentPlayer = 1;
        gameState = new int[9];
        gameActive = true;

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            final int index = i;
            Button cellButton = (Button) gridLayout.getChildAt(i);
            cellButton.setText("");
            cellButton.setEnabled(true);

            cellButton.setOnClickListener(v -> {
                if (gameActive && gameState[index] == 0) {
                    gameState[index] = currentPlayer;
                    cellButton.setText(currentPlayer == 1 ? "X" : "O");
                    cellButton.setEnabled(false);

                    if (checkWin()) {
                        handleGameEnd("Игрок " + (currentPlayer == 1 ? "X" : "O") + " победил!");
                    } else if (isDraw()) {
                        handleGameEnd("Ничья!");
                    } else {
                        if (isBotMode && currentPlayer == 1) {
                            currentPlayer = 2;
                            botMove();
                        } else {
                            currentPlayer = (currentPlayer == 1) ? 2 : 1;
                        }
                    }
                }
            });
        }
    }

    private void botMove() {
        int index;
        do {
            index = (int) (Math.random() * 9);
        } while (gameState[index] != 0);

        gameState[index] = currentPlayer;
        Button cellButton = (Button) gridLayout.getChildAt(index);
        cellButton.setText("O");
        cellButton.setEnabled(false);

        if (checkWin()) {
            handleGameEnd("Бот победил!");
        } else if (isDraw()) {
            handleGameEnd("Ничья!");
        } else {
            currentPlayer = 1;
        }
    }

    private boolean checkWin() {
        int[][] winPositions = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };

        for (int[] pos : winPositions) {
            if (gameState[pos[0]] == currentPlayer &&
                    gameState[pos[1]] == currentPlayer &&
                    gameState[pos[2]] == currentPlayer) {
                return true;
            }
        }
        return false;
    }

    private boolean isDraw() {
        for (int state : gameState) {
            if (state == 0) return false;
        }
        return true;
    }

    private void handleGameEnd(String message) {
        if (message.contains("X победил")) {
            if (isBotMode) {
                playerWins++;
            } else {
                playerVsPlayerWinsX++;
            }
        } else if (message.contains("O победил") || message.contains("Бот победил")) {
            if (isBotMode) {
                botWins++;
            } else {
                playerVsPlayerWinsO++;
            }
        } else if (message.contains("Ничья")) {
            if (isBotMode) {
                playerDraws++;
            } else {
                playerVsPlayerDraws++;
            }
        }

        sharedPreferences.edit()
                .putInt("playerWins", playerWins)
                .putInt("botWins", botWins)
                .putInt("playerDraws", playerDraws)
                .putInt("winsX", playerVsPlayerWinsX)
                .putInt("winsO", playerVsPlayerWinsO)
                .putInt("draws", playerVsPlayerDraws)
                .apply();

        Snackbar.make(gridLayout, message, Snackbar.LENGTH_LONG).show();
        setupGame();
    }

    private void showStats() {
        String statsMessage = "Режим Игрок против Бота:\n" +
                "Победы игрока: " + playerWins + "\n" +
                "Победы бота: " + botWins + "\n" +
                "Ничьи: " + playerDraws + "\n\n" +
                "Режим Игрок против Игрока:\n" +
                "Победы X: " + playerVsPlayerWinsX + "\n" +
                "Победы O: " + playerVsPlayerWinsO + "\n" +
                "Ничьи: " + playerVsPlayerDraws;

        new AlertDialog.Builder(this)
                .setTitle("Статистика")
                .setMessage(statsMessage)
                .setPositiveButton("ОК", null)
                .show();
    }
}
