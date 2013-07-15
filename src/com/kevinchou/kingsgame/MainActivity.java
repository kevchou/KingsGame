package com.kevinchou.kingsgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static String KEY_FIRST_RUN = "";
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	
    char[] SUIT = {'S', 'H', 'C', 'D'};
    char[] RANK = {'A', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K'};

    // Set up array of pointers to Card Images
    private int[][] deckImages = new int[][]{
            {R.drawable.sa, R.drawable.s2, R.drawable.s3, R.drawable.s4, R.drawable.s5, R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9, R.drawable.st, R.drawable.sj, R.drawable.sq, R.drawable.sk},
            {R.drawable.ha, R.drawable.h2, R.drawable.h3, R.drawable.h4, R.drawable.h5, R.drawable.h6, R.drawable.h7, R.drawable.h8, R.drawable.h9, R.drawable.ht, R.drawable.hj, R.drawable.hq, R.drawable.hk},
            {R.drawable.ca, R.drawable.c2, R.drawable.c3, R.drawable.c4, R.drawable.c5, R.drawable.c6, R.drawable.c7, R.drawable.c8, R.drawable.c9, R.drawable.ct, R.drawable.cj, R.drawable.cq, R.drawable.ck},
            {R.drawable.da, R.drawable.d2, R.drawable.d3, R.drawable.d4, R.drawable.d5, R.drawable.d6, R.drawable.d7, R.drawable.d8, R.drawable.d9, R.drawable.dt, R.drawable.dj, R.drawable.dq, R.drawable.dk}
    };

    // Allocate space for variables
    ImageView ivCard;
    TextView tvCardsLeft, tvCardText, tvCardRule, tvCardDescription, tvKingsLeft;
    LinearLayout llRulesLayout;
    
    Deck deck = new Deck();
    DBTools dbTools = new DBTools(this);
    ArrayList<HashMap<String, String>> rulesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(MODE_PRIVATE);
        if (!sharedPreferences.contains("KEY_FIRST_RUN")) {
            KEY_FIRST_RUN = "something";
            
            dbTools.setDefaultRules();

            editor = sharedPreferences.edit();
            editor.putString("KEY_FIRST_RUN", KEY_FIRST_RUN);
            editor.commit();
        }

        // Initialise layout elements
        ivCard = (ImageView) findViewById(R.id.ivCard);
        tvCardsLeft = (TextView) findViewById(R.id.tvCardsLeft);
        tvCardText = (TextView) findViewById(R.id.tvCardText);
        tvCardRule = (TextView) findViewById(R.id.tvCardRule);
        tvCardDescription = (TextView) findViewById(R.id.tvCardDescription);
        tvKingsLeft = (TextView) findViewById(R.id.tvKingsLeft);
        llRulesLayout = (LinearLayout) findViewById(R.id.llRulesLayout);

        // Get current rules
        rulesList = dbTools.getAllRules();

        // get screen width and height in pixels
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int cardWidth = (int) (metrics.widthPixels * .75);
        int cardHeight = (int) (metrics.heightPixels * .7);
        
        // Adjust size of card according to screen size
        LayoutParams params = ivCard.getLayoutParams();
        params.height = cardHeight;
        params.width = cardWidth;
        ivCard.setLayoutParams(params);

        redeal();
    }

    // set up onclick listeners
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.ivCard:

                if (!deck.isEmpty()) // As long as deck still has cards..
                {

                    // Only shows rules popup when game has started
                	if (deck.getNumberOfCardsLeft() == 52)
                		llRulesLayout.setVisibility(View.VISIBLE);

                    // deals new card
                    Card currentCard = deck.dealCard();

                    // Set image for dealt card
                    int currentCardImage = deckImages[currentCard.getSuitIndex()][currentCard.getRankIndex()];
                    ivCard.setImageResource(currentCardImage);                   

                    // display number of cards left in deck
                    tvCardsLeft.setText( String.format("%s %s", deck.getNumberOfCardsLeft(), getResources().getText(R.string.cards_left)) );

                    // display rank and suit of current card
                    tvCardText.setText(currentCard.toString());

                    // display rule of current card
                    String[] currentRules = getCurrentRule( currentCard.getRankIndex() );
                    tvCardRule.setText(currentRules[0]);
                    tvCardDescription.setText( currentRules[1] );

                    // display text about number of kings left
                    int numOfKingsLeft = deck.getNumOfKingsLeft();
                    if (numOfKingsLeft == 0 && currentCard.getRank() == 'K' )
                        tvKingsLeft.setText( getResources().getText(R.string.last_king) );
                    else
                        tvKingsLeft.setText( String.format("%s %s", deck.getNumOfKingsLeft(), getResources().getString(R.string.num_of_kings_left) ) );

                }
                else if (deck.isEmpty()) {
                	deckOverPopUp();
                	llRulesLayout.setVisibility(View.GONE);
                }
        }

    }

    // Alert dialog that pops up when deck is over
    private void deckOverPopUp() {
    	
    	// Set empty card imagine and messages
    	ivCard.setImageResource(R.drawable.empty);
        tvCardsLeft.setText(getResources().getString(R.string.no_cards_left));
        tvCardText.setText("");
        tvCardRule.setText("");
        tvCardDescription.setText("");
        tvKingsLeft.setText("");
        
        // Popup alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage(getResources().getString(R.string.deck_over))
        	   .setPositiveButton(getResources().getText(R.string.redeal_button), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
                       redeal();
                   }
               }
               );

        builder.show();
    }

    // Resets deck
    public void redeal() {
        deck.shuffle();
        ivCard.setImageResource(R.drawable.back);
        tvCardsLeft.setText(getResources().getString(R.string.fiftytwo_cards_left));
        tvCardText.setText(getResources().getString(R.string.start_text));
        tvCardRule.setText("");
        tvCardDescription.setText("");
        llRulesLayout.setVisibility(View.GONE);
        rulesList = dbTools.getAllRules();
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.card_flip);
        mp.start();
    }


    public String[] getCurrentRule(int rankIndex) {
        HashMap<String, String> ruleMap = rulesList.get(rankIndex);
        return new String[] { ruleMap.get("ruleTitle"), ruleMap.get("ruleDescription")};
    }

    // Card object
    class Card {

        char suit, rank;

        // constructor, creates a card with suit 's', rank 'r'
        public Card(char s, char r) {
            suit = s;
            rank = r;
        }

        // returns card suit
        public char getSuit(){
            return suit;
        }

        // returns card rank
        public char getRank(){
            return rank;
        }

        public int getSuitIndex() {
            String tempSuit = new String(SUIT);
            return tempSuit.indexOf(suit);
        }

        public int getRankIndex() {
            String tempRank = new String(RANK);
            return tempRank.indexOf(rank);
        }

        // text of current card
        @Override
        public String toString() {
            String rankToDisplay, suitToDisplay;

            switch(rank) {
                case('A'):
                    rankToDisplay = "Ace";
                    break;
                case('T'):
                    rankToDisplay = "10";
                    break;
                case('J'):
                    rankToDisplay = "Jack";
                    break;
                case('Q'):
                    rankToDisplay = "Queen";
                    break;
                case('K'):
                    rankToDisplay = "King";
                    break;
                default:
                    rankToDisplay = Character.toString(rank);
            }

            switch(suit) {
                case('D'):
                    suitToDisplay = "Diamonds";
                    break;
                case('C'):
                    suitToDisplay = "Clubs";
                    break;
                case('H'):
                    suitToDisplay = "Hearts";
                    break;
                case('S'):
                    suitToDisplay = "Spades";
                    break;
                default:
                    suitToDisplay = Character.toString(suit);
            }

            return (rankToDisplay + " of " + suitToDisplay);
        }

    }

    // Deck object that holds all 52 cards
    class Deck {

        Stack<Card> deck = new Stack<Card>();
        int numOfKingsLeft;

        // Constructor, creates deck of 52 cards
        public Deck() {
            for (char s : SUIT) {
                for (char r : RANK) {
                    deck.add(new Card(s, r));

                }
            }
            numOfKingsLeft = 4;
        }

        // Reset deck and shuffles cards
        public void shuffle() {
            deck = new Stack<Card>();
            for (char s : SUIT) {
                for (char r : RANK) {
                    deck.add(new Card(s, r));
                }
            }
            numOfKingsLeft = 4;
            Collections.shuffle(deck);
        }

        // Removes a card from the deck
        public Card dealCard() {
            Card dealtCard = deck.pop();
            numOfKingsLeft -= ( (dealtCard.getRank() == 'K') ? 1 : 0 );
            return dealtCard;
        }

        // Check if deck is empty
        public boolean isEmpty() {
            if (deck.isEmpty())
                return true;
            return false;
        }

        // how many cards left in deck
        public int getNumberOfCardsLeft() {
            return deck.size();
        }

        public int getNumOfKingsLeft() {
            return numOfKingsLeft;
        }

    }



    // Menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.redeal:
        		redeal();
        		break;
        		
            case R.id.view_rules:
                Intent intent = new Intent(getApplication(), RulesActivity.class);
                startActivity(intent);
                break;

            case R.id.toggleRuleDescription:
                if (deck.getNumberOfCardsLeft() <52) {
                    if (item.isChecked()) {
                        tvCardDescription.setVisibility(View.GONE);
                        item.setChecked(false);
                    } else {
                        tvCardDescription.setVisibility(View.VISIBLE);
                        item.setChecked(true);
                    }
                }
            	break;
            	
            case R.id.toggleRules:
                if (deck.getNumberOfCardsLeft() <52) {
                    if (item.isChecked()) {
                        llRulesLayout.setVisibility(View.GONE);
                        item.setChecked(false);
                    } else {
                        llRulesLayout.setVisibility(View.VISIBLE);
                        item.setChecked(true);
                    }
                }
            	break;
            	
            default:
                break;
        }
        return true;
    }
}
