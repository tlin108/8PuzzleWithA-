
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class EightPuzzle {

    // The Goal tiles
    static final byte [] goal = { 1, 2, 3, 8, 0, 4, 7, 6, 5};

    // A* priority queue.
    final PriorityQueue <State> queue = new PriorityQueue<State>(100, new Comparator<State>() {
        @Override
        public int compare(State a, State b) { 
            return a.priority() - b.priority();
        }
    });

    // The closed state set.
    final HashSet <State> closed = new HashSet <State>();
    
    // Need min for IDA* search
    static int count=0;
    static int min;

    // State of the puzzle including its priority and chain to start state.
    class State {
        final byte [] tiles;    // Tiles left to right, top to bottom in a 1D byte array
        final int emptyIndex;   // Index of empty tile in tiles  
        final int g;            // Actual move cost so far of the state
        final int h;            // Heuristic value of the state
        final State parent;     // Parent state of the current state

        // A* f function, returns g(actual cost) + h(heuristic cost) of the state.
        int priority() {
            return g + h;
        }

        // Build a start state.
        State(byte [] initial) {
            tiles = initial;
            emptyIndex = index(tiles, 0);
            g = 0;
            h = heuristicTwo(tiles); // to change heuristic, switch Two to One or vice versa
            parent = null;
        }

        // Build a successor to parent by sliding tile from given index.
        State(State parent, int slideFromIndex) {
            tiles = Arrays.copyOf(parent.tiles, parent.tiles.length);
            tiles[parent.emptyIndex] = tiles[slideFromIndex];
            tiles[slideFromIndex] = 0;
            emptyIndex = slideFromIndex;
            g = parent.g + 1;
            h = heuristicTwo(tiles); // to change heuristic, switch Two to One or vice versa
            this.parent = parent;
        }

        // Return true iif this is the goal state.
        boolean isGoal() {
            return Arrays.equals(tiles, goal);
        }

        // Successor states due to down, up, left, right moves.
        State moveDown() { return emptyIndex > 2 ? new State(this, emptyIndex - 3) : null; }       
        State moveUp() { return emptyIndex < 6 ? new State(this, emptyIndex + 3) : null; }       
        State moveRight() { return emptyIndex % 3 > 0 ? new State(this, emptyIndex - 1) : null; }       
        State moveLeft() { return emptyIndex % 3 < 2 ? new State(this, emptyIndex + 1) : null; }

        // Print this state.
        void print() {
            System.out.println("priority = " + priority() + " = g+h = " + g + "+" + h);
            for (int i = 0; i < 9; i += 3)
                System.out.println(tiles[i] + " " + tiles[i+1] + " " + tiles[i+2]);
        }

        // Print the solution chain with start state first.
        void printAll() {
            if (parent != null) parent.printAll();
            System.out.println();
            print();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof State) {
                State other = (State)obj;
                return Arrays.equals(tiles, other.tiles);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(tiles);
        }
    }

    // Add a valid (non-null and not closed) successor to the queue.
    void addSuccessor(State successor) {
        if (successor != null && !closed.contains(successor)) 
            queue.add(successor);
    }

    /*
     *  Solving using A* algorithm with either heuristic one (count the number of misplaced tiles) or heuristic two (Manhattan distance of all wrong tiles combined)
     *  To change which heuristic method to use, go to the State class's constructor to change the h line to h = heuristicTwo(tiles) or h = heuristicOne(tiles)
     *  It is initially set up to use heuristicTwo (Manhattan distance)
     */
    void solvebyAstar(byte [] initial) {
    	
        queue.clear();
        closed.clear();

        // Start the timer
        long start = System.currentTimeMillis();

        // Add initial state to queue.
        queue.add(new State(initial));
        
        // Counter to count how many node expanded
        int cnt = 0;
        
        while (!queue.isEmpty()) {

            // Get the lowest priority state.
            State state = queue.poll();
            
            // Increase node expanded by one
            cnt++;
            
            // Check if the current state is the goal state, if it is, print the solution and other information and end program
            if (state.isGoal()) {
                long elapsed = System.currentTimeMillis() - start;
                state.printAll();
                System.out.println("Elapsed (ms) = " + elapsed);
                System.out.println("Node Expanded = " + cnt);
                return;
            }

            // Add current state to close list so we don't revisit
            closed.add(state);

            // Attempt to add current state's possible successors to the queue.
            addSuccessor(state.moveDown());
            addSuccessor(state.moveUp());
            addSuccessor(state.moveLeft());
            addSuccessor(state.moveRight());
        }
    }
    
    // Add a valid (non-null and not closed) successor whose f value is lower than the bound to the queue.
    void addSuccessorDFBB(State successor, int L) {
        if (successor != null && !closed.contains(successor) && successor.priority() <= L) 
            queue.add(successor);
    }
    
    /*
     *  Solving using depth first branch and bound algorithm with heuristic two (Manhattan distance of all wrong tiles combined)
     */
    void solvebyDFBB(byte [] initial) {

        queue.clear();
        closed.clear();

        // Start the timer
        long start = System.currentTimeMillis();

        // Add initial state to queue.
        queue.add(new State(initial));
        
        // Counter to count how many node expanded
        int cnt = 0;
        
        // Score of the best solution so far.
        int L=9999;
        
        // Record the optimalTime of solution found
        long optimaltime = 999999;
        long elapsed = 0;
        
        while (!queue.isEmpty()) {

        	 // Get the lowest priority state.
            State state = queue.poll();
            
            // Increase node expanded by one
            cnt++;
            
            // Check if the current state is the goal state
            if (state.isGoal()) {
                elapsed = System.currentTimeMillis() - start;
                state.printAll();
                // If a better solution is found, record its time as optimal
                if(elapsed < optimaltime)
                	optimaltime = elapsed;
                L = state.priority();
            }

            // Add current state to close list so we don't revisit
            closed.add(state);

            // Attempt to add current state's possible successors with bound limit to the queue.
            addSuccessorDFBB(state.moveDown(), L);
            addSuccessorDFBB(state.moveUp(), L);
            addSuccessorDFBB(state.moveLeft(), L);
            addSuccessorDFBB(state.moveRight(), L);
        }
        
        elapsed = System.currentTimeMillis() - start;
        System.out.println("Elapsed (ms) = " + elapsed);
        System.out.println("Optimal Time = " + optimaltime);
        System.out.println("Node Expanded = " + cnt);
    }
    
    /*
     *   Solving using iterative deepening A* algorithm with heuristic two (Manhattan distance of all wrong tiles combined)
     */
    void solvebyIDAstar(byte [] initial) {

    	// Start the timer
        long start = System.currentTimeMillis();
        

        // Initialize start state
        State state = new State(initial);
        
        // Set upper bound to start state's f value
        int bound = state.priority();
        
        // place holder for min to be added to bound
        int t;
        
        while (true) {
        	queue.clear();
            closed.clear();
            queue.add(state);
            System.out.println("current bound is "+bound);
            /*
             *  Attempting to find a solution with the given bound, if found return -1, else the minimum f-value which
             *  exceeded f among states which were generated to increment the bound limit
             */
        	t = DFBBsearch(bound);
        	
        	// If t is -1, that means a solution is found and therefore break free from loop
        	if(t == -1){
        		long elapsed = System.currentTimeMillis() - start;
                System.out.println("Elapsed (ms) = " + elapsed);
                System.out.println("Node Expanded = " + count);
                break;
        	}
        	
        	// Else, add min return by search method to bound
        	bound += t;
        }
    }
    
    // DFBB search needed for IDA* search, returns -1 if solution found, else minimum to be added to bound
    int DFBBsearch(int bound){
    	int L = bound;
    	min = 99999;
    	while (!queue.isEmpty()) {
            // Get the lowest priority state.
            State state = queue.poll();
            
            // Increase node expanded by one
            count++;
            
            // Check if the current state is the goal state, if it is, print the solution and other information and return -1
            if (state.isGoal()) {
            	
                state.printAll();
                min=-1;
                L=state.priority();
            }
            // Add current state to close list so we don't revisit
            closed.add(state);

            // Attempt to add current state's possible successors with bound limit to the queue.
            addSuccessorDFBBforIDA(state.moveDown(), L);
            addSuccessorDFBBforIDA(state.moveUp(), L);
            addSuccessorDFBBforIDA(state.moveLeft(), L);
            addSuccessorDFBBforIDA(state.moveRight(), L);
            
        }
    	return min;
    }
    
 // Add a valid (non-null and not closed) successor whose f value is lower than the bound to the queue.
    void addSuccessorDFBBforIDA(State successor, int L) {
    	if (successor != null && !closed.contains(successor) && successor.priority() > L){
    		if(successor.priority() < min)
    			min = successor.priority()-L;
    	}
        if (successor != null && !closed.contains(successor) && successor.priority() <= L) 
            queue.add(successor);
    }

    // Return the index of val in given byte array or -1 if none found
    static int index(byte [] a, int val) {
        for (int i = 0; i < a.length; i++)
            if (a[i] == val) return i;
        return -1;
    }

    // Return the Manhattan distance between tiles
    static int manhattanDistance(int a, int b){
    	int goalpos = index (goal, a);
    	int diff = Math.abs(goalpos/3 - b/3) + Math.abs(goalpos%3 - b%3);
    	return diff;
    	
    }

    // First heuristic, determined by number of missing tiles
    static int heuristicOne(byte [] tiles) {
        int h = 0;
        for (int i = 0; i < tiles.length; i++)
            if (tiles[i] != 0 && tiles[i]!=goal[i])
                h += 1;
        return h;
    }
    
    // Second heuristic, determined by Manhattan distance of all tiles
    static int heuristicTwo(byte [] tiles) {
        int h = 0;
        for (int i = 0; i < tiles.length; i++)
            if (tiles[i] != 0 && tiles[i]!=goal[i])
            	h += manhattanDistance(tiles[i], i);
        return h;
    }

    public static void main(String[] args) {
    	
    	/*
    	 *  To test a desired puzzle state, uncomment it, and comment the other three states
    	 */
    	//Easy
        //byte [] initial = { 1, 3, 4, 8, 6, 2, 7, 0, 5};
        
        //Medium
        //byte [] initial = { 2, 8, 1, 0, 4, 3, 7, 6, 5};
        
        //Hard
        //byte [] initial = { 2, 8, 1, 4, 6, 3, 0, 7, 5};
        
        //Worst
        byte [] initial = { 5, 6, 7, 4, 0, 8, 3, 2, 1};
    	
        
        /*
         *  To test a desired solving algorithm, uncomment it, and comment the other two search method.
         *  To change heuristic method, go to the State's two constructors and change them there
         */
        //new EightPuzzle().solvebyAstar(initial);
        new EightPuzzle().solvebyDFBB(initial);
        //new EightPuzzle().solvebyIDAstar(initial);
    }
}