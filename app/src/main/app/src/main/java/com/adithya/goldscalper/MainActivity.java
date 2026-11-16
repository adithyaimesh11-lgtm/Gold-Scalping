
package com.adithya.goldscalper;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

public class MainActivity extends Activity {

    private LinearLayout layout;
    private TextView signalText, priceText, rsiText;
    private TextView emaText, macdText, entryText;
    private TextView slText, tpText, statusText;

    private double[] prices;
    private double currentPrice;
    private double rsi, ema9, ema21, macd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 28, 24, 28);
        layout.setBackgroundColor(Color.rgb(15, 22, 35));
        scroll.addView(layout);
        setContentView(scroll);

        title("GOLD SCALPING AI BOT");
        subtitle("XAUUSD | Market Analysis Dashboard");

        statusText = text("DEMO MODE - NOT LIVE MARKET DATA",
                13, Color.YELLOW);
        add(statusText);

        priceText = text("Gold Price: --", 23, Color.WHITE);
        add(priceText);

        signalText = text("SIGNAL: WAIT", 30, Color.YELLOW);
        add(signalText);

        add(text("TECHNICAL INDICATORS", 18, Color.CYAN));

        rsiText = text("RSI (14): --", 17, Color.WHITE);
        emaText = text("EMA 9 / EMA 21: --", 17, Color.WHITE);
        macdText = text("MACD: --", 17, Color.WHITE);

        add(rsiText);
        add(emaText);
        add(macdText);

        add(text("TRADE PLAN (ILLUSTRATIVE)",
                18, Color.CYAN));

        entryText = text("Entry: --", 17, Color.WHITE);
        slText = text("Stop Loss: --", 17, Color.WHITE);
        tpText = text("Take Profit: --", 17, Color.WHITE);

        add(entryText);
        add(slText);
        add(tpText);

        Button analyze = new Button(this);
        analyze.setText("ANALYZE GOLD");
        add(analyze);

        analyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analyzeMarket();
            }
        });

        Button refresh = new Button(this);
        refresh.setText("RESET DEMO DATA");
        add(refresh);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDemoData();
                analyzeMarket();
            }
        });

        TextView warning = text(
                "WARNING: Demo prices only. No live broker feed, " +
                "AI prediction or real trades are connected. " +
                "Do not use these signals for live trading.",
                14, Color.LTGRAY);
        add(warning);

        createDemoData();
        analyzeMarket();
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(0, 12, 0, 12);
        return t;
    }

    private void add(View view) {
        layout.addView(view);
    }

    private void title(String value) {
        TextView t = text(value, 25, Color.WHITE);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        add(t);
    }

    private void subtitle(String value) {
        TextView t = text(value, 15, Color.LTGRAY);
        t.setGravity(Gravity.CENTER);
        add(t);
    }

    // Generate simulated gold candle closing prices.
    private void createDemoData() {
        prices = new double[100];

        for (int i = 0; i < prices.length; i++) {
            prices[i] = 4200.0
                    + (i * 0.18)
                    + (Math.sin(i * 0.65) * 2.0)
                    + (Math.sin(i * 0.17) * 3.0);
        }

        currentPrice = prices[prices.length - 1];
    }

    private double calculateEMA(int period) {
        double multiplier = 2.0 / (period + 1.0);
        double value = prices[0];

        for (int i = 1; i < prices.length; i++) {
            value = (prices[i] - value)
                    * multiplier + value;
        }

        return value;
    }

    private double calculateRSI(int period) {
        double gains = 0;
        double losses = 0;

        for (int i = prices.length - period;
             i < prices.length; i++) {

            double change = prices[i] - prices[i - 1];

            if (change > 0) {
                gains += change;
            } else {
                losses += Math.abs(change);
            }
        }

        if (losses == 0) return 100;

        double rs = (gains / period) / (losses / period);
        return 100.0 - (100.0 / (1.0 + rs));
    }

    private double calculateMACD() {
        return calculateEMAForPeriod(12)
                - calculateEMAForPeriod(26);
    }

    private double calculateEMAForPeriod(int period) {
        double multiplier = 2.0 / (period + 1.0);
        double value = prices[0];

        for (int i = 1; i < prices.length; i++) {
            value = (prices[i] - value)
                    * multiplier + value;
        }

        return value;
    }

    private void analyzeMarket() {
        currentPrice = prices[prices.length - 1];

        ema9 = calculateEMA(9);
        ema21 = calculateEMA(21);
        rsi = calculateRSI(14);
        macd = calculateMACD();

        String signal = "WAIT";
        int color = Color.YELLOW;

        if (ema9 > ema21
                && rsi > 50
                && rsi < 70
                && macd > 0) {

            signal = "BUY";
            color = Color.GREEN;

        } else if (ema9 < ema21
                && rsi < 50
                && rsi > 30
                && macd < 0) {

            signal = "SELL";
            color = Color.RED;
        }

        signalText.setText("SIGNAL: " + signal);
        signalText.setTextColor(color);

        priceText.setText(String.format(Locale.US,
                "Demo Gold Price: %.2f", currentPrice));

        rsiText.setText(String.format(Locale.US,
                "RSI (14): %.2f", rsi));

        emaText.setText(String.format(Locale.US,
                "EMA 9: %.2f | EMA 21: %.2f",
                ema9, ema21));

        macdText.setText(String.format(Locale.US,
                "MACD (12,26): %.4f", macd));

        double entry = currentPrice;
        double stopLoss;
        double takeProfit;

        if (signal.equals("BUY")) {
            stopLoss = entry * 0.995;
            takeProfit = entry * 1.010;
        } else if (signal.equals("SELL")) {
            stopLoss = entry * 1.005;
            takeProfit = entry * 0.990;
        } else {
            stopLoss = entry;
            takeProfit = entry;
        }

        entryText.setText(String.format(Locale.US,
                "Demo Entry: %.2f", entry));

        slText.setText(String.format(Locale.US,
                "Illustrative SL: %.2f", stopLoss));

        tpText.setText(String.format(Locale.US,
                "Illustrative TP: %.2f", takeProfit));
    }
}
