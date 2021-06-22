import tester.Tester;
import javalib.worldimages.*;
import javalib.impworld.*;
import java.util.*;
import java.awt.Color;

// Utility class
class ArrayUtils {
  // makes a deck of cards
  ArrayList<Card> makeDeck() {
    ArrayList<String> vals = new ArrayList<String>(
        Arrays.asList("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"));
    ArrayList<String> suits = new ArrayList<String>(Arrays.asList("♣", "♦", "♥", "♠"));
    ArrayList<Card> cards = new ArrayList<Card>();

    for (int i = 0; i < vals.size(); i++) {
      for (int j = 0; j < suits.size(); j++) {
        cards.add(new Card(vals.get(i), suits.get(j)));
      }
    }
    return cards;
  }
}

// Represents a card
class Card {
  String value;
  String suit;
  boolean flipped;
  boolean inplay;

  Card(String v, String s, boolean f, boolean i) {
    this.value = v;
    this.suit = s;
    this.flipped = f;
    this.inplay = i;
  }

  // convenience constructor
  Card(String v, String s) {
    this(v, s, false, true);
  }

  // draws this card
  WorldImage draw() {
    RectangleImage blankcard = new RectangleImage(40, 70, OutlineMode.OUTLINE, Color.BLACK);
    TextImage conc = new TextImage("❁", 40, Color.orange);

    TextImage suit = new TextImage(this.suit, 20, Color.PINK);
    TextImage rank = new TextImage(this.value, 20, Color.PINK);

    TextImage nosuit = new TextImage(this.suit, 20, Color.LIGHT_GRAY);
    TextImage norank = new TextImage(this.value, 20, Color.LIGHT_GRAY);

    // draws the card in the correct orientation (flipped or unflipped & inplay or
    // not inplay)
    if (flipped && inplay) {
      return new OverlayOffsetImage(rank, -3, 20, new OverlayImage(suit, blankcard));
    }
    if (flipped && !inplay) {
      return new OverlayOffsetImage(norank, -3, 20, new OverlayImage(nosuit, blankcard));
    } else {
      return new OverlayImage(conc, blankcard);
    }
  }
}

// World
class Concentration extends World {
  ArrayList<ArrayList<Card>> cards;
  ArrayList<Card> history;
  ArrayList<Card> row;
  ArrayList<Card> faceupcards;
  int score;
  Random rand;

  Concentration(Random rand) {
    this.rand = rand;
    this.history = new ArrayUtils().makeDeck();
    this.cards = new ArrayList<ArrayList<Card>>();
    this.faceupcards = new ArrayList<Card>();
    this.score = 26;

    for (int i = 0; i < 13; i++) {
      this.row = new ArrayList<Card>();
      for (int j = 0; j < 4; j++) {
        this.row.add(this.history.remove(rand.nextInt(this.history.size())));
      }
      this.cards.add(row);
    }
  }

  Concentration() {
    this(new Random());
  }

  Concentration(ArrayList<ArrayList<Card>> cards, ArrayList<Card> history, ArrayList<Card> row,
      Random rand) {
    this.cards = cards;
    this.history = history;
    this.row = row;
    this.rand = rand;
  }

  // draws the game
  public WorldScene makeScene() {
    WorldImage grid = new EmptyImage();

    for (int i = 0; i < this.row.size(); i++) {
      WorldImage row = new EmptyImage();
      for (int j = 0; j < this.cards.size(); j++) {
        Card curr = this.cards.get(j).get(i);
        // add this card image to the right of the current row image
        row = new BesideImage(row, curr.draw());
      }
      // add this row below the current grid image
      grid = new AboveImage(grid, row);
    }
    WorldScene scene = new WorldScene(520, 280);
    scene.placeImageXY(grid, 260, 140);
    return scene;
  }

  // compare if the two flipped cards are the same value
  public boolean isSameValue(Card card1, Card card2) {
    return card1.value.equals(card2.value) && !card1.suit.equals(card2.suit);
  }

  // method to flip card that will be on mouse movement
  public void onMouseClicked(Posn posn) {
    Card c = getCardAtPosn(posn);
    if (c != null && c.inplay) {
      if (this.faceupcards.size() < 2) {
        c.flipped = true;
        faceupcards.add(c);
      } else if (this.faceupcards.size() == 2) {
        if (this.isSameValue(this.faceupcards.get(0), this.faceupcards.get(1))) {
          this.score -= 1;
          this.faceupcards.get(0).inplay = false;
          this.faceupcards.get(1).inplay = false;
          // remove from possible options
          this.faceupcards.clear();
        } else {
          this.faceupcards.get(0).flipped = false;
          this.faceupcards.get(1).flipped = false;
          this.faceupcards.clear();
        }
      } else {
        this.faceupcards.clear();
      }
    }
  }

  // retrieves the card at this posn
  public Card getCardAtPosn(Posn posn) {
    for (int i = 0; i < this.cards.size(); i++) {
      for (int j = 0; j < this.row.size(); j++) {
        Card curr = this.cards.get(i).get(j);

        if (posn.x >= i * 40 && posn.x <= i * 40 + 40 && posn.y >= j * 70
            && posn.y <= j * 70 + 70) {
          return curr;
        }
      }
    }
    return null;
  }

  // Ends the game
  public WorldEnd worldEnds() {
    if (this.score <= 0) {
      return new WorldEnd(true, this.makeAFinalScene());
    } else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // creates the end scene of the game
  public WorldScene makeAFinalScene() {
    AboveImage endscreen = new AboveImage(
        new TextImage("  ☆♬○♩●♪✧♩((ヽ( ᐛ )ﾉ))♩✧♪●♩○♬☆              ☆", 20, Color.ORANGE),
        new TextImage("✧･ﾟ: *✧･ﾟ:* You found all 26 pairs! *:･ﾟ✧*:･ﾟ✧", 20, Color.PINK));
    WorldScene scene = new WorldScene(520, 280);
    scene.placeImageXY(endscreen, 260, 140);
    return scene;
  }

  // ends the world on "r" key
  public void onKeyEvent(String key) {
    if (key.equalsIgnoreCase("r")) {
      this.score = 26;
      this.faceupcards.clear();
      this.history = new ArrayUtils().makeDeck();
      this.cards = new ArrayList<ArrayList<Card>>();
      this.faceupcards = new ArrayList<Card>();
      this.score = 26;

      for (int i = 0; i < 13; i++) {
        this.row = new ArrayList<Card>();
        for (int j = 0; j < 4; j++) {
          this.row.add(this.history.remove(rand.nextInt(this.history.size())));
        }
        this.cards.add(row);
      }

    }
  }

  // runs the world on tick
  public void onTick() {
    this.worldEnds();

  }
}

class ExamplesGame {

  ArrayList<String> lovals;
  ArrayList<String> losuits;

  Card card1;
  Card card2;
  Card card3;
  Card card4;
  Card card5;
  Card card6;
  Card card7;
  Card card8;
  Card card9;

  RectangleImage blankcard;
  TextImage conc;

  ArrayUtils au;

  Posn posn1;
  Posn posn2;
  Posn posn3;
  Posn posn4;

  void initData() {
    this.lovals = new ArrayList<String>(
        Arrays.asList("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"));
    this.losuits = new ArrayList<String>(Arrays.asList("♣", "♦", "♥", "♠"));

    this.card1 = new Card("A", "♣", true, true);
    this.card2 = new Card("2", "♥", true, true);
    this.card3 = new Card("3", "♠", true, true);
    this.card4 = new Card("J", "♦", true, false);
    this.card5 = new Card("K", "♣", false, true);
    this.card6 = new Card("J", "♦", false, true);
    this.card7 = new Card("10", "♠", false, false);
    this.card8 = new Card("Q", "♥", false, false);
    this.card9 = new Card("2", "♦", false, false);

    this.blankcard = new RectangleImage(40, 70, OutlineMode.OUTLINE, Color.BLACK);
    this.conc = new TextImage("❁", 40, Color.orange);

    this.au = new ArrayUtils();

    this.posn1 = new Posn(30, 60);
    this.posn2 = new Posn(500, 270);
    this.posn3 = new Posn(140, 80);
    this.posn4 = new Posn(215, 126);
  }

  void testMakeDeck(Tester t) {
    this.initData();
    ArrayList<Card> mtlocards = new ArrayList<Card>();
    for (int i = 0; i < 13; i++) {
      for (int j = 0; j < 4; j++) {
        mtlocards.add(new Card(lovals.get(i), losuits.get(j), false, true));
      }
    }

    t.checkExpect(au.makeDeck().size(), 52);
    t.checkExpect(au.makeDeck(), mtlocards);
  }

  // test draw method
  void testDraw(Tester t) {
    this.initData();
    t.checkExpect(this.card1.draw(),
        (new OverlayOffsetImage(new TextImage(this.card1.value, 20, Color.PINK), -3, 20,
            new OverlayImage(new TextImage(this.card1.suit, 20, Color.PINK), this.blankcard))));
    t.checkExpect(this.card2.draw(),
        (new OverlayOffsetImage(new TextImage(this.card2.value, 20, Color.PINK), -3, 20,
            new OverlayImage(new TextImage(this.card2.suit, 20, Color.PINK), this.blankcard))));
    t.checkExpect(this.card3.draw(),
        new OverlayOffsetImage(new TextImage(this.card3.value, 20, Color.PINK), -3, 20,
            new OverlayImage(new TextImage(this.card3.suit, 20, Color.PINK), this.blankcard)));
    t.checkExpect(this.card4.draw(),
        new OverlayOffsetImage(new TextImage(this.card4.value, 20, Color.LIGHT_GRAY), -3, 20,
            new OverlayImage(new TextImage(this.card4.suit, 20, Color.LIGHT_GRAY), blankcard)));

    // test the cards that are not flipped
    t.checkExpect(this.card5.draw(), new OverlayImage(conc, blankcard));
    t.checkExpect(this.card6.draw(), new OverlayImage(conc, blankcard));
  }

  // tests the makeScene method
  void testMakeScene(Tester t) {
    this.initData();
    ArrayList<Card> l1 = new ArrayList<Card>(Arrays.asList(this.card1, this.card2, this.card6));
    ArrayList<Card> l2 = new ArrayList<Card>(Arrays.asList(this.card3, this.card4, this.card5));
    ArrayList<ArrayList<Card>> l3 = new ArrayList<ArrayList<Card>>(Arrays.asList(l1, l2));

    Concentration world = new Concentration(l3, l1, l2, new Random(5));
    WorldScene scene = new WorldScene(520, 280);
    WorldImage grid = new EmptyImage();

    ArrayList<Card> locards = new ArrayList<Card>();
    for (int i = 0; i < 13; i++) {
      for (int j = 0; j < 4; j++) {
        locards.add(new Card(lovals.get(i), losuits.get(j), false, true));
      }
    }

    for (int i = 0; i < 3; i++) {
      WorldImage row = new EmptyImage();
      for (int j = 0; j < 2; j++) {
        Card curr = l3.get(j).get(i);
        // add this card image to the right of the current row image
        row = new BesideImage(row, curr.draw());
      }
      // add this row below the current grid image
      grid = new AboveImage(grid, row);
    }
    scene.placeImageXY(grid, 260, 140);

    t.checkExpect(world.makeScene(), scene);
  }

  // tests the isSameValue method
  void testIsSameValue(Tester t) {
    this.initData();
    t.checkExpect(new Concentration().isSameValue(card1, card2), false);
    t.checkExpect(new Concentration().isSameValue(card2, card9), true);
    t.checkExpect(new Concentration().isSameValue(card4, card4), false);
  }

  // test onMouseClicked method
  void testOnMouseClicked(Tester t) {
    this.initData();
    Concentration world = new Concentration();
    Card c1 = world.getCardAtPosn(this.posn1);

    t.checkExpect(c1.flipped, false);
    world.onMouseClicked(this.posn1);
    t.checkExpect(c1.flipped, true);

    Card c2 = world.getCardAtPosn(this.posn2);
    t.checkExpect(c2.flipped, false);
    world.onMouseClicked(this.posn2);
    t.checkExpect(c2.flipped, true);

    Card c3 = world.getCardAtPosn(this.posn3);
    t.checkExpect(c3.flipped, false);
    world.onMouseClicked(this.posn3);
    t.checkExpect(c3.flipped, false);

    Card c4 = world.getCardAtPosn(this.posn4);
    t.checkExpect(c4.flipped, false);
    world.onMouseClicked(this.posn4);
    t.checkExpect(c4.flipped, true);
  }

  // test getCardAtPosn method
  void testGetCardAtPosn(Tester t) {
    this.initData();
    Concentration world = new Concentration(new Random(3));
    t.checkExpect(world.getCardAtPosn(this.posn1), new Card("5", "♥", false, true));
    t.checkExpect(world.getCardAtPosn(this.posn2), new Card("4", "♠", false, true));
    t.checkExpect(world.getCardAtPosn(this.posn3), new Card("9", "♠", false, true));
  }

  // test worldEnds method
  void testWorldEnds(Tester t) {
    this.initData();
    Concentration world = new Concentration();
    t.checkExpect(world.worldEnds(), new WorldEnd(false, world.makeScene()));
    world.score = 0;
    t.checkExpect(world.worldEnds(), new WorldEnd(true, world.makeAFinalScene()));

  }

  // test onKeyEvent method
  void testOnKeyEvent(Tester t) {
    this.initData();
    Concentration world1 = new Concentration();
    t.checkExpect(world1.score, 26);
    world1.onKeyEvent("r");
    t.checkExpect(world1.score, 26);

    Concentration world2 = new Concentration();
    world2.score = 16;
    t.checkExpect(world2.score, 16);
    world2.onKeyEvent("r");
    t.checkExpect(world2.score, 26);

  }

  // test onTick method
  void testOnTick(Tester t) {
    this.initData();
    Concentration world1 = new Concentration();
    t.checkExpect(world1, world1);
    world1.score = 0;
    world1.onTick();
    t.checkExpect(world1.score, 0);
    t.checkExpect(world1.faceupcards.size(), 0);

  }

  // tests the makeAFinalScene method
  void testMakeAFinalScene(Tester t) {
    this.initData();
    Concentration world1 = new Concentration();
    AboveImage endscreen = new AboveImage(
        new TextImage("  ☆♬○♩●♪✧♩((ヽ( ᐛ )ﾉ))♩✧♪●♩○♬☆              ☆", 20, Color.ORANGE),
        new TextImage("✧･ﾟ: *✧･ﾟ:* You found all 26 pairs! *:･ﾟ✧*:･ﾟ✧", 20, Color.PINK));
    WorldScene scene = new WorldScene(520, 280);
    scene.placeImageXY(endscreen, 260, 140);
    t.checkExpect(world1.makeAFinalScene(), scene);
  }

  // runs the world big bang
  void testBigBang(Tester t) {
    Concentration game = new Concentration();
    game.bigBang(520, 280, .001);
  }

}