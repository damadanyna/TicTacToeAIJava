import java.util.*;

public class TicTacToeAI{
   
   public static void main(String[] args){
   
      
      Game x = new Game();
      x.train();
      x.AITest();
      
   }
   
}

class Game{

   private String board;
   private String lastMove;
   private ArrayList<String> winningPatterns = new ArrayList<String>(Arrays.asList("012", "345", "678", "036", "147", "258", "048", "246"));
   private ArrayList<Double[]> qTable = new ArrayList<Double[]>();
   private int winReward = 1;
   private double drawReward = 0.5;
   private int loseReward = 0;
   private double alpha = 0.9;
   private double gamma = 0.95;
   
   public Game(){
      board = "         ";
      lastMove = "O";
   }
   
   public void display(){
      for(int i = 0; i < board.length(); i++){
         if(i != 0 && i % 3 == 0){
            System.out.println("-----");
         }
         System.out.print(board.substring(i, i+1));
         if(i % 3 == 2){
            System.out.println();
         }
         else{
            System.out.print("|");
         }
      }
   }
   
   public String getState(){
      return board;
   }
   
   public void play(){
      Scanner sc = new Scanner(System.in);
      boolean over = false;
      while(!over){
         String player = "";
         if(lastMove.equals("O")){
            player += "X";
            lastMove = "X";
         }
         else{
            player += "O";
            lastMove = "O";
         }
         State state = new State(board, player);
         boolean squareFilled = true;
         while(squareFilled){
            System.out.println("Which square (1-9, left to right, top to bottom)?");
            int index = Integer.parseInt(sc.next()) - 1;
            if(board.substring(index, index + 1).equals(" ")){
               squareFilled = false;
               board = board.substring(0, index) + player + board.substring(index + 1);
            }
            else{
               System.out.println("Square is filled. Pick another square.");
            }
         }
         this.display();
         if(this.isGameOver() == true){
            over = true;
         }
      }
      System.out.println();
      System.out.println(lastMove + " is the winner!");
   }
   
   public boolean isGameOver(){
      for(int i = 0; i < winningPatterns.size(); i++){
         int a = Integer.parseInt(winningPatterns.get(i).substring(0, 1));
         int b = Integer.parseInt(winningPatterns.get(i).substring(1, 2));
         int c = Integer.parseInt(winningPatterns.get(i).substring(2));
         String state = this.getState();
         String d = state.substring(a, a+1) + state.substring(b, b+1) + state.substring(c, c+1);
         if(d.equals("XXX") || d.equals("OOO")){
            return true;
         }
      }
      return false;
   }
   
   public ArrayList<String> randomPlay(){
      board = "         ";
      ArrayList<String> ret = new ArrayList<String>();
      boolean over = false;
      while(!over){
         String player = "";
         if(lastMove.equals("O")){
            player += "X";
            lastMove = "X";
         }
         else{
            player += "O";
            lastMove = "O";
         }
         State state = new State(board, player);
         ArrayList<Integer> moveSet = state.getMoveSet();
         if(moveSet.size() == 0){
            break;
         }
         int rand = (int)(moveSet.size() * Math.random());
         int index = moveSet.get(rand);
         board = board.substring(0, index) + player + board.substring(index + 1);
         boolean contains = false;
         for(int i = 0; i < ret.size(); i++){
            if(ret.get(i).equals(board)){
               contains = true;
               break;
            }
         }
         if(!contains){
            ret.add(board);
         }
         if(this.isGameOver() == true){
            over = true;
         }
      }
      return ret;
   }
   
   public ArrayList<String> randomTrain(){
      ArrayList<String> visitedStates = new ArrayList<String>();
      for(int i = 0; i < 50000; i++){
         ArrayList<String> newStates = this.randomPlay();
         for(int j = 0; j < newStates.size(); j++){
            String x = newStates.get(j);
            boolean contains = false;
            for(int k = 0; k < visitedStates.size(); k++){
               if(visitedStates.get(k).equals(x)){
                  contains = true;
                  break;
               }
            }
            if(!contains){
               visitedStates.add(x);
            }
         }
      }
      return visitedStates;
   }
   
   public void AIPlay(double epsilon){
      board = "         ";
      ArrayList<String> statesVisited = new ArrayList<String>();
      ArrayList<Integer> stateActionPairs = new ArrayList<Integer>();
      boolean over = false;
      double rew = 0.5;
      while(!over){
         String player = "";
         if(lastMove.equals("O")){
            player += "X";
            lastMove = "X";
         }
         else{
            player += "O";
            lastMove = "O";
         }
         State state = new State(board, player);
         ArrayList<Integer> moveSet = state.getMoveSet();
         if(moveSet.size() == 0){
            rew = drawReward;
            break;
         }
         double ran = Math.random();
         int index;
         if(ran < epsilon || player.equals("O")){
            int rand = (int)(moveSet.size() * Math.random());
            index = moveSet.get(rand);
         }
         else{
            index = this.bestMove(hash(board), moveSet);
         }
         stateActionPairs.add(hash(board));
         stateActionPairs.add(index);
         board = board.substring(0, index) + player + board.substring(index + 1);
         boolean contains = false;
         for(int i = 0; i < statesVisited.size(); i++){
            if(statesVisited.get(i).equals(board)){
               contains = true;
               break;
            }
         }
         if(!contains){
            statesVisited.add(board);
         }
         //this.display();
         //System.out.println();
         if(this.isGameOver() == true){
            over = true;
            if(lastMove == "X"){
               rew = winReward;
            }
            else{
               rew = loseReward;
            }
         }
      }
      for(int i = 0; i < stateActionPairs.size(); i++){
         if(i % 2 == 0){
            //System.out.print("Hash: " + stateActionPairs.get(i));
         }
         else{
            //System.out.println(", Index played: " + stateActionPairs.get(i));
         }
      }
      this.updateQTable(stateActionPairs, rew);
   }
   
   public void AITest(){
      board = "         ";
      lastMove = "O";
      ArrayList<String> statesVisited = new ArrayList<String>();
      ArrayList<Integer> stateActionPairs = new ArrayList<Integer>();
      boolean over = false;
      double rew = 0.5;
      while(!over){
         String player = "";
         if(lastMove.equals("O")){
            player += "X";
            lastMove = "X";
         }
         else{
            player += "O";
            lastMove = "O";
         }
         if(player.equals("X")){
            State state = new State(board, player);
            ArrayList<Integer> moveSet = state.getMoveSet();
            if(moveSet.size() == 0){
               rew = drawReward;
               break;
            }
            int index;
            index = this.bestMove(hash(board), moveSet);
            stateActionPairs.add(hash(board));
            stateActionPairs.add(index);
            board = board.substring(0, index) + player + board.substring(index + 1);
         }
         else{
            if(board.indexOf(" ") == -1){
               rew = drawReward;
               break;
            }
            Scanner sc1 = new Scanner(System.in);
            boolean squareFilled = true;
            while(squareFilled){
               System.out.println("Which square (1-9, left to right, top to bottom)?");
               int index = Integer.parseInt(sc1.next()) - 1;
               if(board.substring(index, index + 1).equals(" ")){
                  squareFilled = false;
                  board = board.substring(0, index) + player + board.substring(index + 1);
               }
               else{
                  System.out.println("Square is filled. Pick another square.");
               }
            }
         }
         this.display();
         System.out.println();
         boolean contains = false;
         for(int i = 0; i < statesVisited.size(); i++){
            if(statesVisited.get(i).equals(board)){
               contains = true;
               break;
            }
         }
         if(!contains){
            statesVisited.add(board);
         }
         boolean visitedBefore = false;
         for(int j = 0; j < qTable.size(); j++){
            Double[] x = qTable.get(j);
            if(Math.floor(x[0]) == hash(board)){
               visitedBefore = true;
               break;
            }
         }
         if(visitedBefore){
            //System.out.println("Board is in Q-Table");
         }
         else{
            //System.out.println("Board not visited yet");
         }
         if(this.isGameOver() == true){
            over = true;
            if(lastMove == "X"){
               rew = winReward;
               System.out.println("AI Wins!");
            }
            else{
               rew = loseReward;
               System.out.println("User Wins!");
            }
         }
      }
      for(int i = 0; i < stateActionPairs.size(); i++){
         if(i % 2 == 0){
            //System.out.print("Hash: " + stateActionPairs.get(i));
         }
         else{
            //System.out.println(", Index played: " + stateActionPairs.get(i));
         }
      }
   }
   
   public void train(){
      int gamesPlayed = 1;
      for(int i = 0; i < 100000; i++){
         this.AIPlay(1/gamesPlayed);
         gamesPlayed++;
         if(i % 1000 == 0)
            System.out.println("Games Played: " + i + ", Q-Table size: " + qTable.size());
      }
      System.out.println("Done, Q-Table size: " + qTable.size());
   }
   
   public void displayQTable(){
      for(int i = 0; i < qTable.size(); i++){
         Double[] x = qTable.get(i);
         System.out.println("Board HashCode: " + Math.floor(x[0]));
         for(int j = 1; j < x.length - 1; j++){
            System.out.print(" Move " + j + ": " + x[j] + ", ");
         }
         System.out.println(" Move " + (x.length - 1) + ": " + (x[x.length - 1]));
      }
      System.out.println("Done, Q-Table size: " + qTable.size());
   }
   
   public void updateQTable(ArrayList<Integer> sAPairs, double reward){
      for(int i = sAPairs.size() - 1; i > 0; i -= 2){
         boolean contains = false;
         int index = sAPairs.get(i) + 1;
         int qIndex = -1;
         for(int j = 0; j < qTable.size(); j++){
            Double[] x = qTable.get(j);
            if(Math.floor(x[0]) == sAPairs.get(i - 1)){
               contains = true;
               qIndex = j;
               break;
            }
         }
         if(!contains){
            Double[] y = new Double[10];
            Arrays.fill(y, 0.4);
            y[0] = (double) sAPairs.get(i - 1);
            if(i == sAPairs.size() - 1){
               y[index] = reward;
            }
            else{
               double newQValue = y[index] + alpha * (gamma * maxQValue(sAPairs.get(i + 1)) - y[index]);
               y[index] = newQValue;
            }
            qTable.add(y);
         }
         else{
            Double[] a = qTable.remove(qIndex);
            if(i == sAPairs.size() - 1){
               a[index] = reward;
            }
            else{
               double newQValue = a[index] + alpha * (gamma * maxQValue(sAPairs.get(i + 1)) - a[index]);
               a[index] = newQValue;
            }
            qTable.add(a);
         }
      }
   }
   
   public double maxQValue(int hashedBoard){
      double max = -999999.0;
      for(int i = 0; i < qTable.size(); i++){
         Double[] x = qTable.get(i);
         if(Math.floor(x[0]) == hashedBoard){
            for(int j = 1; j < x.length; j++){
               if(x[j] > max){
                  max = x[j];
               }
            }
         }
      }
      return max;
   }
      
   public int bestMove(int hashedBoard, ArrayList<Integer> moveSet){
      Double[] qValues = new Double[10];
      boolean contains = false;
      int index = -1;
      for(int i = 0; i < qTable.size(); i++){
         Double[] x = qTable.get(i);
         if(Math.floor(x[0]) == hashedBoard){
            for(int k = 0; k < qValues.length; k++){
               qValues[k] = x[k];
            }
            contains = true;
            break;
         }
      }
      if(!contains){
         int rand = (int)(moveSet.size() * Math.random());
         index = moveSet.get(rand);
      }
      else{
         double max = -999999.0;
         for(int j = 1; j < qValues.length; j++){
            boolean inMoveSet = false;
            for(int l = 0; l < moveSet.size(); l++){
               if(moveSet.get(l).equals(j - 1)){
                  inMoveSet = true;
                  break;
               }
            }
            if(qValues[j] > max && inMoveSet){
               max = qValues[j];
               index = j;
            }
         }
         ArrayList<Integer> maxQValueIndices = new ArrayList<Integer>();
         for(int m = 1; m < qValues.length; m++){
            boolean inMoveSet2 = false;
            for(int n = 0; n < moveSet.size(); n++){
               if(moveSet.get(n).equals(m - 1)){
                  inMoveSet2 = true;
                  break;
               }
            }
            if(qValues[m] == max && inMoveSet2){
               maxQValueIndices.add(m);
            }
         }
         int rand2 = (int)(maxQValueIndices.size() * Math.random());
         if(maxQValueIndices.size() > 3){
            System.out.println("HERE " + maxQValueIndices.size() + ", " + rand2);
         }
         index = maxQValueIndices.get(rand2);
         index = index - 1;
      }
      return index;
   }
   
   public static int hash(String board){
      int ret = 0;
      for(int i = 0; i < board.length(); i++){
         String x = board.substring(i, i+1);
         if(x.equals(" ")){
            ret += 1 * Math.pow(10, board.length() - 1 - i);
         }
         else if (x.equals("X")){
            ret += 2 * Math.pow(10, board.length() - 1 - i);
         }
         else{
            ret += 3 * Math.pow(10, board.length() - 1 - i);
         }
      }
      return ret;
   }
   
   public static String unHash(int hashedBoard){
      String ret = "";
      String x = Integer.toString(hashedBoard);
      for(int i = 0; i < x.length(); i++){
         String y = x.substring(i, i+1);
         if(y.equals("1")){
            ret += " ";
         }
         else if(y.equals("2")){
            ret += "X";
         }
         else{
            ret += "O";
         }
      }
      return ret;
   }
   
   public static void display(int hashedBoard){
      String unHashedBoard = unHash(hashedBoard);
      for(int i = 0; i < unHashedBoard.length(); i++){
         if(i != 0 && i % 3 == 0){
            System.out.println("-----");
         }
         System.out.print(unHashedBoard.substring(i, i+1));
         if(i % 3 == 2){
            System.out.println();
         }
         else{
            System.out.print("|");
         }
      }
   }
   
}

class State{

   private String id;
   private ArrayList<String> nextStates = new ArrayList<String>();
   private ArrayList<Integer> moveSet = new ArrayList<Integer>();
   
   public State(String x, String y){
      id = x;
      for(int i = 0; i < 9; i++){
         if(x.substring(i, i+1).equals(" ")){
            String a = "";
            if(i != 8){
               a += x.substring(0,i) + y + x.substring(i+1);
            }
            else{
               a += x.substring(0,i) + y;
            }
            nextStates.add(a);
            moveSet.add(i);
         }
         else{
            nextStates.add("-");
         }
      }
   }
   
   public ArrayList<String> getNextStates(){
      return nextStates;
   }
   
   public ArrayList<Integer> getMoveSet(){
      return moveSet;
   }
   
   public void printBoards(){
      for(int i = 0; i < nextStates.size(); i++){
         String board = nextStates.get(i);
         String a = "";
         for(int j = 0; j < board.length(); j++){
            if(j != 0 && j % 3 == 0){
               a += "-----" + "\n";
            }
            a += board.substring(j, j+1);

            if(j % 3 == 2){
               a += "\n";
            }
            else{
               a += "|";
            }
         }
         System.out.println(a);
      }
   }
   
   public String getID(){
      return id;
   }   
   
}
